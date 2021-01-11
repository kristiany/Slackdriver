package errors

import SlackReporter
import errors.Error.Companion.displayConfig
import slack.Blocks
import slack.CodeBlock
import slack.SectionBlock
import java.time.Instant

data class GkeError(val region: String?,
                    val cluster: String?,
                    val namespace: String?,
                    val pod: String?,
                    val container: String?,
                    val timestamp: Instant?,
                    val stacktrace: String,
                    val count: Long) : Error {

    override fun report(reporter: SlackReporter) {
        reporter.post(cluster, slackMessage())
    }

    override fun slackMessage(): String {
        return Blocks(SectionBlock("*${cluster}* (${region}) :point_left: GKE Cluster\n" +
                "*${pod} ${container}* :point_left: Pod/container\n" +
                "${displayTimestamp(timestamp)}\n" +
                displayCount(count)),
                CodeBlock(stacktrace, displayConfig)).asJson()
    }

    override fun toString(): String {
        val messageTitle = stacktrace.substringBefore("\n")
        return "errors.GkeError(region=$region, cluster=$cluster, namespace=$namespace, pod=$pod, container=$container, " +
                "timestamp=$timestamp, messageTitle=$messageTitle, count=$count)"
    }
}