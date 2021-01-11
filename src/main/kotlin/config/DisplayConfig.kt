package config

import java.time.ZoneId

class DisplayConfig(val timeZoneId: ZoneId = DisplayConfig.timeZoneId,
                    val stackCollapseFilter: List<String> = DisplayConfig.stackCollapseFilter) {
    companion object {
        val timeZoneId = System.getenv("ZONE_ID")?.let { ZoneId.of(it) } ?: ZoneId.of("CET")
        val stackCollapseFilter = System.getenv("STACKTRACE_COLLAPSE_FILTERLIST")?.split(",") ?: listOf()
    }
}