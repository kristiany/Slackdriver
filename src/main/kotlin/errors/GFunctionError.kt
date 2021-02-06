package errors

import SlackReporter
import slack.Blocks
import slack.SectionBlock
import java.time.Instant

data class GFunctionError(val location: String?,
                          val function: String?,
                          val timestamp: Instant?,
                          val stacktrace: String,
                          val count: Long) : Error {

    override fun report(reporter: SlackReporter) {
        reporter.post("default", slackMessage())
    }

    override fun slackMessage(): String {
        return Blocks(SectionBlock("*${function}* (${location}) :point_left: Function\n" +
                "${displayTimestamp(timestamp)}\n" +
                displayCount(count)),
                SectionBlock("```${stacktrace}```")).asJson()
    }

    override fun toString(): String {
        val messageTitle = stacktrace.substringBefore("\n")
        return "errors.GFunctionError(region=$location, function=$function, timestamp=$timestamp, messageTitle=$messageTitle, count=$count)"
    }
}