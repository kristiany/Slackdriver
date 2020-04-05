package slack

data class CodeBlock(val code: Map<String, String>) : SectionBlock(code) {
    constructor(text: String) : this(mapOf("type" to "mrkdwn", "text" to truncateAndAddCodeBlock(text)))

    companion object {
        private val CODE_MARKER = "```"
        fun truncateAndAddCodeBlock(text: String): String {
            val markerLength = CODE_MARKER.length * 2
            if (text.length + markerLength <= SectionBlock.SLACK_MAX_LENGTH) {
                return mark(text)
            }
            println("Text is too long, truncating too ${SectionBlock.SLACK_MAX_LENGTH} characters including " +
                    "code-block markers, original is: '${text}'")
            val truncated = text.substring(0, SectionBlock.SLACK_MAX_LENGTH - markerLength)
            return "```${truncated}```"
        }

        private fun mark(text: String): String {
            return "${CODE_MARKER}${text}${CODE_MARKER}"
        }
    }
}