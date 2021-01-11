package slack

data class CodeBlock(val code: Map<String, String>) : SectionBlock(code) {
    constructor(text: String) : this(mapOf("type" to "mrkdwn", "text" to truncateAndAddCodeBlock(text)))

    companion object {
        fun truncateAndAddCodeBlock(text: String): String {
            val highlighted = highlight(text)
            if (highlighted.length <= SLACK_MAX_LENGTH) {
                return highlighted
            }
            println("Text is too long, truncating too $SLACK_MAX_LENGTH characters including " +
                    "code-block markers, original is: '${highlighted}'")
            return highlighted.substring(0, SLACK_MAX_LENGTH - 1) + "_"
        }

        private fun highlight(text: String): String {
            val quoted = text.replace("\n", "_\n_")
            return "_${quoted.replace("Caused by:", "*Caused by:*")}_"
        }
    }
}