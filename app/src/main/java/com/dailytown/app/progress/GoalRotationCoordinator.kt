package com.dailytown.app.progress

import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.dailyPeriodKey
import com.dailytown.app.persistence.pushRecentUnique
import com.dailytown.app.persistence.weeklyPeriodKey
import java.time.LocalDate

data class GoalRotationResult(
    val progress: ExplorationProgress,
    val dailyGoals: List<GoalDefinition>,
    val weeklyGoals: List<GoalDefinition>,
)

class GoalRotationCoordinator(
    private val planner: GoalPlanner = GoalPlanner(),
    private val catalog: List<GoalDefinition> = GoalCatalog.defaults(),
) {
    fun ensure(progress: ExplorationProgress, date: LocalDate): GoalRotationResult {
        var updated = progress.normalizePeriods(date)
        val dayKey = dailyPeriodKey(date)
        val weekKey = weeklyPeriodKey(date)

        val dailyCatalog = catalog.filter { it.period == GoalPeriod.DAILY }
        val weeklyCatalog = catalog.filter { it.period == GoalPeriod.WEEKLY }

        val currentDaily = updated.dailyGoalIds.mapNotNull { id -> dailyCatalog.firstOrNull { it.id == id } }
        val dailyNeedsRotation = updated.dailyGoalPeriodKey != dayKey || currentDaily.size != 2
        val dailyGoals = if (dailyNeedsRotation) {
            val recent = pushRecentUnique(updated.recentDailyGoalIds, updated.dailyGoalIds, maxSize = 6)
            val selected = planner.plan(dayKey, dailyCatalog, recent.toSet(), count = 2)
            updated = updated.copy(
                dailyGoalPeriodKey = dayKey,
                dailyGoalIds = selected.map { it.id },
                recentDailyGoalIds = recent,
            )
            selected
        } else currentDaily

        val currentWeekly = updated.weeklyGoalIds.mapNotNull { id -> weeklyCatalog.firstOrNull { it.id == id } }
        val weeklyNeedsRotation = updated.weeklyGoalPeriodKey != weekKey || currentWeekly.size != 1
        val weeklyGoals = if (weeklyNeedsRotation) {
            val recent = pushRecentUnique(updated.recentWeeklyGoalIds, updated.weeklyGoalIds, maxSize = 4)
            val selected = planner.plan(weekKey, weeklyCatalog, recent.toSet(), count = 1)
            updated = updated.copy(
                weeklyGoalPeriodKey = weekKey,
                weeklyGoalIds = selected.map { it.id },
                recentWeeklyGoalIds = recent,
            )
            selected
        } else currentWeekly

        return GoalRotationResult(updated, dailyGoals, weeklyGoals)
    }
}
