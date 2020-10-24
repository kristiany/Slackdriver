import com.google.cloud.errorreporting.v1beta1.ErrorStatsServiceClient
import com.google.cloud.logging.LogEntry
import com.google.cloud.logging.Logging
import com.google.cloud.logging.LoggingOptions
import com.google.devtools.clouderrorreporting.v1beta1.*
import com.google.protobuf.Timestamp
import errors.Error
import java.time.Instant

class ErrorChecker(config: Config) : Runnable {
    companion object {
        private val PERIOD = QueryTimeRange.Period.PERIOD_1_HOUR
    }

    private val projectId = config.projectId
    private val projectName = ProjectName.of(projectId);
    private val seen = Seen(config.cacheEvictionPeriod)
    private val slackReporter = SlackReporter(config)

    override fun run() {
        ErrorStatsServiceClient.create().use {
            val groups = getGroups(it, projectName)
            //groups.forEach { println(it) }
            println("${Instant.now()} Found ${groups.size} groups")
            if (groups.isEmpty()) {
                println("${Instant.now()} No new errors found, skipping until next scheduled run")
                return
            }
            val recentErrors = mostRecentErrorPerGroup(groups, it)
            val newErrors = recentErrors.filter(seen)
            //newErrors.forEach { println(it) }
            println("${Instant.now()} Got ${newErrors.size} new unseen errors")
            if (newErrors.isEmpty()) {
                println("${Instant.now()} No new errors found, sleeping until next scheduled run")
                return
            }

            errorToLogMapping(groups)
                    .map { Error.from(it) }
                    .forEach { report(it) }

            seen.update(newErrors)
        }
    }

    private fun errorToLogMapping(groups: List<ErrorGroupStats>): List<ErrorMappedToLog> =
            LoggingOptions.newBuilder().setProjectId(projectId).build().service.use {
                return groups.map { group ->
                    val representative = group.representative
                    val messageTitle = representative.message.substringBefore("\n")
                    val logEntries = findLogEntries(group.lastSeenTime, messageTitle)
                    logEntries.forEach { println(it) }
                    val possibleLogEntry = logEntries.firstOrNull()

                    ErrorMappedToLog(representative, group.count, possibleLogEntry)
                }.toList()
            }

    private fun findLogEntries(lastSeen: Timestamp, messageTitle: String): List<LogEntry> {
        val timestamp = Instant.ofEpochSecond(lastSeen.seconds)
        val escapedMessageTitle = messageTitle.replace("\"", "\\\"")
        return LoggingOptions.newBuilder().setProjectId(projectId).build().service.use {
            it.listLogEntries(Logging.EntryListOption.filter(
                            " resource.type=k8s_container OR resource.type=cloud_function OR resource.type=k8s_cluster" +
                                    " timestamp > \"${timestamp.minusSeconds(1L)}\"" +
                                    " timestamp < \"${timestamp.plusSeconds(1L)}\"" +
                                    " textPayload : \"${escapedMessageTitle}\""
                    ))
                    .iterateAll().toList()
        }
    }

    private fun mostRecentErrorPerGroup(groups: List<ErrorGroupStats>, client: ErrorStatsServiceClient): List<ErrorEvent> {
        val requestBuilder = ListEventsRequest.newBuilder()
                .setProjectName(projectName.toString())
                .setTimeRange(QueryTimeRange.newBuilder().setPeriod(PERIOD))
                .setPageSize(1)
        return groups.flatMap {
            client.listEvents(requestBuilder.setGroupId(it.group.groupId).build()).iterateAll()
        }.toList()
    }

    private fun getGroups(client: ErrorStatsServiceClient, projectName: ProjectName?): List<ErrorGroupStats> {
        return client.listGroupStats(projectName, QueryTimeRange.newBuilder().setPeriod(PERIOD).build())
                .iterateAll().toList()
    }

    fun report(error: Error) {
        println("${Instant.now()} $error")
        println(error.slackMessage())
        error.report(slackReporter)
    }
}