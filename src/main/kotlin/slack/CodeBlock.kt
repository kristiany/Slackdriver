package slack

import config.DisplayConfig

data class CodeBlock(val code: Map<String, String>, val config: DisplayConfig) : SectionBlock(code) {
    constructor(text: String, config: DisplayConfig) : this(
            mapOf("type" to "mrkdwn", "text" to truncateAndAddCodeBlock(text, config)), config)

    companion object {
        fun truncateAndAddCodeBlock(text: String, config: DisplayConfig): String {
            val updated = collapseAndMark(text, config)
            if (updated.length <= SLACK_MAX_LENGTH) {
                return updated
            }
            println("Text is too long, truncating too $SLACK_MAX_LENGTH characters including " +
                    "code-block markers, original is: '${updated}'")
            return updated.substring(0, SLACK_MAX_LENGTH - 1) + "_"
        }

        private fun collapseAndMark(text: String, config: DisplayConfig): String {
            val highlighted = text.replace("Caused by:", "*Caused by:*")
            return highlighted.trim().split("\n")
                    .map { "_${if (!it.contains("Caused by")) abbreviate(it, config.stackCollapseFilter) else it}_" }
                    .fold(StringBuilder(), { acc, line ->
                        if (!acc.endsWith("$line\n")) acc.append("$line\n") else acc
                    })
                    .toString().trim()
        }

        private fun abbreviate(text: String, stackCollapseFilter: List<String>): String {
            var result = text
            for (filter in stackCollapseFilter) {
                result = result.replace(Regex("${Regex.escape(filter)}[^\\s]+"), "${filter}* ...")
            }
            return result
        }
    }
}