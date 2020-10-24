package errors

import ErrorMappedToLog
import SlackReporter
import com.google.cloud.logging.LogEntry
import java.time.Instant

interface Error {
    fun report(reporter: SlackReporter)

    fun slackMessage(): String

    companion object {
        fun from(e: ErrorMappedToLog): Error {
            if (e.logEntry?.resource?.type.equals("cloud_function")) {
                return GFunctionError(extractLabel(e.logEntry, "region"),
                        extractLabel(e.logEntry, "function_name"),
                        getErrorTimestamp(e),
                        e.error.message,
                        e.count)
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