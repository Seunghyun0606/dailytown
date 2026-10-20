package com.dailytown.app.progress

import com.dailytown.app.persistence.ExplorationProgress
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * End-of-walk summary (scenario v2 candidate §10). Derived only from progress that already exists —
 * no new persistence and no raw location — by diffing the progress snapshot taken when the walk
 * started against the progress when it stopped.
 */
data class WalkSessionSummary(
    val distanceMeters: Int,
    val durationSeconds: Long,
    val newPlaces: Int,
    val revisitedPlaces: Int,
    val cluesFound: Int,
    val mysteriesResolved: Int,
    val memoriesGained: Int,
    val bondBefore: Int,
    val bondAfter: Int,
    val goalsCompleted: List<String>,
    val moruClosingLine: String,
) {
    val bondDelta: Int get() = bondAfter - bondBefore
    val hasDiscoveries: Boolean get() = newPlaces + revisitedPlaces > 0
}

object WalkSessionSummaryBuilder {
    fun build(
        start: ExplorationProgress,
        end: ExplorationProgress,
        sessionDiscoveredPoiIds: Collection<String>,
        distanceMeters: Double,
        durationSeconds: Long,
        bondBefore: Int,
        bondAfter: Int,
        goals: List<GoalDefinition>,
        date: LocalDate,
        evaluator: GoalProgressEvaluator = GoalProgressEvaluator(),
    ): WalkSessionSummary {
        val discovered = sessionDiscoveredPoiIds.distinct()
        val revisited = discovered.count { it in start.encounterVisitedPoiIds }
        val startNormalized = start.normalizePeriods(date)
        val endNormalized = end.normalizePeriods(date)
        val completedGoals = goals.filter { goal ->
            evaluator.evaluate(goal, endNormalized, date).isComplete &&
                !evaluator.evaluate(goal, startNormalized, date).isComplete
        }.map { goalTitle(it) }
        val resolved = (end.resolvedEncounterIds - start.resolvedEncounterIds).size
        val clues = (end.inventoryClueIds - start.inventoryClueIds).size
        val distance = distanceMeters.coerceAtLeast(0.0).roundToInt()
        return WalkSessionSummary(
            distanceMeters = distance,
            durationSeconds = durationSeconds.coerceAtLeast(0L),
            newPlaces = discovered.size - revisited,
            revisitedPlaces = revisited,
            cluesFound = clues,
            mysteriesResolved = resolved,
            memoriesGained = (end.companionMemoryKeys - start.companionMemoryKeys).size,
            bondBefore = bondBefore,
            bondAfter = bondAfter,
            goalsCompleted = completedGoals,
            moruClosingLine = closingLine(resolved, discovered.size, distance),
        )
    }

    fun goalTitle(goal: GoalDefinition): String = when (goal.metric) {
        GoalMetric.WALK_DISTANCE_METERS -> "${goal.target}m 걷기"
        GoalMetric.DISCOVER_SPOT -> "새 장소 ${goal.target}곳 발견"
        GoalMetric.RESOLVE_MYSTERY -> "동네 이야기 ${goal.target}건 해결"
        GoalMetric.COLLECT_CLUE -> "단서 ${goal.target}개 살펴보기"
    }

    private fun closingLine(resolved: Int, discovered: Int, distance: Int): String = when {
        resolved > 0 -> "오늘 같이 찾은 이야기, 오래 기억할게."
        discovered > 0 -> "궁금한 게 남았네. 다음엔 끝까지 같이 풀어보자."
        distance >= 200 -> "걷는 것만으로도 좋았어. 또 같이 나가자."
        else -> "오늘은 짧았지만, 다음엔 조금 더 걸어보자."
    }
}
