import config.Config
import khttp.post
import java.lang.RuntimeException
import java.net.URL
import java.time.Instant

class SlackReporter(config: Config) {
    private val channelRoutes = config.routes

    fun post(channelTypeSuggestion: String?, message: String) {
        val channelType = channelTypeOrDefault(channelTypeSuggestion)
        channelRoutes[channelType]?.let { hook ->
            postToSlack(channelType, message, hook)
        } ?: throw RuntimeException("Channel type '${channelType}' not found in the channel router")
    }

    private fun channelTypeOrDefault(channelType: String?): String {
        if (channelType != null && channelRoutes.containsKey(channelType)) {
            return channelType
        }
        return "default"
    }

    private fun postToSlack(channelType: String, payload: String, hook: URL) {
        val headers = mapOf("Content-type" to "application/json")
        val response = post(url = hook.toString(), headers = headers, data = payload)
        if (response.statusCode == 200) {
            println("${Instant.now()} Message posted to Slack!")
            return
        }
        val errorMessage = "Error posting message '${payload}' to Slack channel type ${channelType}, " +
                "response: ${response.statusCode} - '${response.text}'"
        println(errorMessage)
        throw RuntimeException(errorMessage)
    }
}