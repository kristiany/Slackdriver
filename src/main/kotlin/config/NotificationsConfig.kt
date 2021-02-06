package config

import java.lang.IllegalArgumentException
import java.net.URL

class NotificationsConfig {
    private val HOOK_PREFIX = "ROUTE_SLACK_HOOK_";
    private val FILTER_PREFIX = "ROUTE_FILTER_";
    val defaultHook = URL(System.getenv("SLACK_HOOK_DEFAULT"))
    val filteredRoutes = parseRoutes()

    private fun parseRoutes(): Map<String, URL> {
        val suffixToHook: Map<String, URL> = System.getenv().filter { it.key.startsWith(HOOK_PREFIX) }
            .mapKeys { it.key.replace(HOOK_PREFIX, "") }
            .mapValues { URL(it.value) }
        return System.getenv().filter { it.key.startsWith(FILTER_PREFIX) }
            .flatMap { entry ->
                val suffix = entry.key.replace(FILTER_PREFIX, "")
                val hook = suffixToHook[suffix]
                    ?: throw IllegalArgumentException("Missing corresponding hook config for $suffix")
                entry.value.split(",")
                    .map { it.trim() to hook }
            }.toMap()
    }
}