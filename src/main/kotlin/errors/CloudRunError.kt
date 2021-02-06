package errors

import SlackReporter
import slack.Blocks
import slack.SectionBlock
import java.time.Instant

data class CloudRunError(val location: String?,
                         val serviceName: String?,
                         val revisionName: String?,
                         val configName: String?,
                         val timestamp: Instant?,
                         val stacktrace: String,
                         val count: Long) : Error {

    override fun report(reporter: SlackReporter) {
        reporter.post("default", slackMessage())
    }

    override fun slackMessage(): String {
        return Blocks(SectionBlock("*${serviceName}* / *${revisionName} / *${configName} (${location}) :point_left: Cloud Run\n" +
                "${displayTimestamp(timestamp)}\n" +
                displayCount(count)),
            SectionBlock("```${stacktrace}```")).asJson()
    }

    override fun toString(): String {
        val messageTitle = stacktrace.substringBefore("\n")
        return "errors.CloudRunError(region=$location, serviceName=$serviceName, revisionName=$revisionName, configName=$configName, timestamp=$timestamp, messageTitle=$messageTitle, count=$count)"
    }
}