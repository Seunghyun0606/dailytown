package com.dailytown.app.progress

import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.PeriodProgress
import java.time.LocalDate

data class GoalProgress(
    val current: Int,
    val target: Int,
) {
    val isComplete: Boolean get() = current >= target
}

class GoalProgressEvaluator {
    fun evaluate(
        goal: GoalDefinition,
        progress: ExplorationProgress,
        date: LocalDate = LocalDate.now(),
    ): GoalProgress {
        val normalized = progress.normalizePeriods(date)
        val period = when (goal.period) {
            GoalPeriod.DAILY -> normalized.daily
            GoalPeriod.WEEKLY -> normalized.weekly
        }
        val current = currentValue(goal.metric, period)
        return GoalProgress(current = current.coerceAtMost(goal.target), target = goal.target)
    }

    private fun currentValue(metric: GoalMetric, period: PeriodProgress): Int = when (metric) {
        GoalMetric.WALK_DISTANCE_METERS -> period.distanceWalkedMeters.toInt()
        GoalMetric.DISCOVER_SPOT -> period.discoveredPoiIds.size
        GoalMetric.RESOLVE_MYSTERY -> period.resolvedEncounterIds.size
        GoalMetric.COLLECT_CLUE -> period.clueIds.size
    }
}
