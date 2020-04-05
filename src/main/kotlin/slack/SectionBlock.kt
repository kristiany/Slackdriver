package slack

import kotlinx.serialization.Serializable

@Serializable
open class SectionBlock(val text: Map<String, String>) {
    private val type: String = "section"

    constructor(text: String) : this(mapOf("type" to "mrkdwn", "text" to truncate(text)))

    companion object {
        val SLACK_MAX_LENGTH = 3000;

        fun truncate(text: String): String {
            if (text.length <= SLACK_MAX_LENGTH) {
                return text
            }
            println("Text is too long, truncating too ${SLACK_MAX_LENGTH} characters, original is: '${text}'")
            return text.substring(0, SLACK_MAX_LENGTH)
        }
    }
}
