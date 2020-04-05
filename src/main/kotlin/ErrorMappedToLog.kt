import com.google.devtools.clouderrorreporting.v1beta1.ErrorEvent
import com.google.cloud.logging.LogEntry

data class ErrorMappedToLog(val error: ErrorEvent, val count: Long, val logEntry: LogEntry? ) {
}