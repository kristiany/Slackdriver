import config.Config
import khttp.post
import java.lang.RuntimeException
import java.net.URL
import java.time.Instant

class SlackReporter(config: Config) {
    private val channelRoutes = config.notificationsConfig.filteredRoutes
    private val defaultHook = config.notificationsConfig.defaultHook

    fun post(channelSuggestions: List<String?>, message: String) {
        val validFilter = channelSuggestions.filterNotNull()
            .filterNot { channelRoutes[it] == null }
            .firstOrNull()
        validFilter?.let {
                    postToSlack(it, message, channelRoutes[it]!!)
                    return
                }
        println("No specific route found for resource names '$channelSuggestions', sending to default.")
        postToSlack("default", message, defaultHook)
    }

    private fun postToSlack(resourceName: String, payload: String, hook: URL) {
        val headers = mapOf("Content-type" to "application/json")
        val response = post(url = hook.toString(), headers = headers, data = payload)
        if (response.statusCode == 200) {
            println("${Instant.now()} Message posted to Slack!")
            return
        }
        val errorMessage = "Error posting message '${payload}' to Slack with routing filter ${resourceName}, " +
                "response: ${response.statusCode} - '${response.text}'"
        println(errorMessage)
        throw RuntimeException(errorMessage)
    }
}