import java.net.URL
import java.time.Duration

class Config {
    val projectId = System.getenv("PROJECT_ID")
    val defaultHook = URL(System.getenv("SLACK_HOOK"))
    val routes = mapOf("default" to defaultHook)
    val cacheEvictionPeriod = System.getenv("CACHE_EVICTION_PERIOD")?.let { Duration.parse(it) } ?: Duration.ofDays(1)
}