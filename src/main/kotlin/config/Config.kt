package config

import java.time.Duration

class Config {
    val projectId = System.getenv("PROJECT_ID")
    val notificationsConfig = NotificationsConfig()
    val cacheEvictionPeriod = System.getenv("CACHE_EVICTION_PERIOD")?.let { Duration.parse(it) } ?: Duration.ofDays(1)
}