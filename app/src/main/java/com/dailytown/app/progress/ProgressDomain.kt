package com.dailytown.app.progress

enum class GoalMetric { WALK_DISTANCE_METERS, DISCOVER_SPOT, RESOLVE_MYSTERY, COLLECT_CLUE }
enum class GoalPeriod { DAILY, WEEKLY }

data class GoalDefinition(
    val id: String,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val target: Int,
)

data class NeighborhoodProgress(
    val districtKey: String,
    val visitedPoiIds: Set<String> = emptySet(),
    val resolvedEncounterIds: Set<String> = emptySet(),
    val distanceWalkedMeters: Double = 0.0,
) {
    val discoveryCount: Int get() = visitedPoiIds.size
    val resolvedCount: Int get() = resolvedEncounterIds.size
}

class GoalPlanner {
    fun plan(
        periodKey: String,
        catalog: List<GoalDefinition>,
        recentlyUsedIds: Set<String>,
        count: Int,
    ): List<GoalDefinition> {
        if (count <= 0) return emptyList()
        val fresh = catalog.filterNot { it.id in recentlyUsedIds }
        val pool = if (fresh.size >= count) fresh else catalog
        return pool
            .distinctBy { it.id }
            .sortedBy { stableScore(periodKey, it.id) }
            .take(count)
    }

    private fun stableScore(periodKey: String, id: String): Long {
        var result = 1125899906842597L
        (periodKey + ":" + id).forEach { char -> result = result * 31 + char.code }
        return result and Long.MAX_VALUE
    }
}

object GoalCatalog {
    fun defaults(): List<GoalDefinition> = listOf(
        GoalDefinition("daily-walk-800", GoalPeriod.DAILY, GoalMetric.WALK_DISTANCE_METERS, 800),
        GoalDefinition("daily-discover-2", GoalPeriod.DAILY, GoalMetric.DISCOVER_SPOT, 2),
        GoalDefinition("daily-clue-3", GoalPeriod.DAILY, GoalMetric.COLLECT_CLUE, 3),
        GoalDefinition("daily-resolve-1", GoalPeriod.DAILY, GoalMetric.RESOLVE_MYSTERY, 1),
        GoalDefinition("weekly-walk-6000", GoalPeriod.WEEKLY, GoalMetric.WALK_DISTANCE_METERS, 6000),
        GoalDefinition("weekly-discover-8", GoalPeriod.WEEKLY, GoalMetric.DISCOVER_SPOT, 8),
        GoalDefinition("weekly-resolve-4", GoalPeriod.WEEKLY, GoalMetric.RESOLVE_MYSTERY, 4),
    )
}
