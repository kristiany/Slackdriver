import cache.Seen
import cache.SeenKey
import com.google.cloud.errorreporting.v1beta1.ErrorStatsServiceClient
import com.google.cloud.logging.LogEntry
import com.google.cloud.logging.Logging
import com.google.cloud.logging.LoggingOptions
import com.google.devtools.clouderrorreporting.v1beta1.ErrorEvent
import com.google.devtools.clouderrorreporting.v1beta1.ErrorGroupStats
import com.google.devtools.clouderrorreporting.v1beta1.ListEventsRequest
import com.google.devtools.clouderrorreporting.v1beta1.ProjectName
import com.google.devtools.clouderrorreporting.v1beta1.QueryTimeRange
import com.google.protobuf.Timestamp
import config.Config
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

            val unseenKeys = groups.map { g -> toKey(g) }.filter(seen)
            val unseenGroups = groups.filter { g -> unseenKeys.contains(toKey(g)) }

            val recentErrors = mostRecentErrorPerGroup(unseenGroups, it)
            val newErrors = recentErrors.filter(seen)
            //newErrors.forEach { println(it) }
            println("${Instant.now()} Got ${newErrors.size} new unseen errors")
            if (newErrors.isEmpty()) {
                println("${Instant.now()} No new errors found, sleeping until next scheduled run")
                return
            }

            errorToLogMapping(unseenGroups)
                    .map { Error.from(it) }
                    .forEach { report(it) }

            seen.update(unseenKeys)
        }
    }

    private fun toKey(it: ErrorGroupStats) =
            SeenKey(it.affectedServicesList, it.group.groupId)

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
        val resourceTypes = listOf("k8s_container", "cloud_function", "k8s_cluster", "cloud_run_revision")
        return LoggingOptions.newBuilder().setProjectId(projectId).build().service.use {
            it.listLogEntries(Logging.EntryListOption.filter(
                    resourceTypes.joinToString { t -> " OR resource.type=$t" } +
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