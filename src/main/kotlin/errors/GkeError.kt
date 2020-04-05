package errors

import SlackReporter
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
        return Blocks(SectionBlock("GKE Cluster: *${cluster}* (${region})\n" +
                "Pod: *${pod} ${container}*\n" +
                "Time: *${timestamp}*\n" +
                "Count: *${count}*"),
                CodeBlock(stacktrace)).asJson()
    }

    override fun toString(): String {
        val messageTitle = stacktrace.substringBefore("\n")
        return "errors.GkeError(region=$region, cluster=$cluster, namespace=$namespace, pod=$pod, container=$container, " +
                "timestamp=$timestamp, messageTitle=$messageTitle, count=$count)"
    }
}