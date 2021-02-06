package errors

import ErrorMappedToLog
import SlackReporter
import com.google.cloud.logging.LogEntry
import config.DisplayConfig
import java.time.Instant
import java.time.format.DateTimeFormatter

interface Error {
    fun report(reporter: SlackReporter)

    fun slackMessage(): String

    fun displayTimestamp(t: Instant?) = "*${formatter.format(t)}* (${t}) :point_left: Time"

    fun displayCount(count: Long) = "*${count}* error${if (count > 1) "s" else ""}"

    companion object {
        val displayConfig = DisplayConfig()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(displayConfig.timeZoneId);

        fun from(e: ErrorMappedToLog): Error {
            if (e.logEntry?.resource?.type.equals("cloud_function")) {
                return GFunctionError(extractLabel(e.logEntry, "region"),
                        extractLabel(e.logEntry, "function_name"),
                        getErrorTimestamp(e),
                        e.error.message,
                        e.count)
            }
            if (e.logEntry?.resource?.type.equals("cloud_run_revision")) {
                return CloudRunError(extractLabel(e.logEntry, "location"),
                    extractLabel(e.logEntry, "service_name"),
                    extractLabel(e.logEntry, "revision_name"),
                    extractLabel(e.logEntry, "configuration_name"),
                    getErrorTimestamp(e),
                    e.error.message,
                    e.count
                )
            }
            return GkeError(extractLabel(e.logEntry, "location"),
                    extractLabel(e.logEntry, "cluster_name"),
                    extractLabel(e.logEntry, "namespace_name"),
                    extractLabel(e.logEntry, "pod_name"),
                    extractLabel(e.logEntry, "container_name"),
                    getErrorTimestamp(e),
                    e.error.message,
                    e.count)
        }

        private fun getErrorTimestamp(e: ErrorMappedToLog) = Instant.ofEpochSecond(e.error.eventTime.getSeconds(),
                e.error.eventTime.getNanos().toLong())

        private fun extractLabel(log: LogEntry?, property: String) =
                log?.resource?.labels?.get(property) ?: "Unknown"
    }
}