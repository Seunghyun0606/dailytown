package com.dailytown.app.persistence

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState
import com.dailytown.app.mystery.MysteryEncounter
import java.time.LocalDate
import java.time.temporal.IsoFields

data class PeriodProgress(
    val periodKey: String = "",
    val distanceWalkedMeters: Double = 0.0,
    val discoveredPoiIds: Set<String> = emptySet(),
    val clueIds: Set<String> = emptySet(),
    val resolvedEncounterIds: Set<String> = emptySet(),
) {
    fun resetIfNeeded(expectedKey: String): PeriodProgress =
        if (periodKey == expectedKey) this else PeriodProgress(periodKey = expectedKey)

    fun recordDistance(deltaMeters: Double): PeriodProgress =
        copy(distanceWalkedMeters = distanceWalkedMeters + deltaMeters.coerceAtLeast(0.0))

    fun recordDiscovery(poiId: String): PeriodProgress = copy(discoveredPoiIds = discoveredPoiIds + poiId)
    fun recordClue(clueId: String): PeriodProgress = copy(clueIds = clueIds + clueId)
    fun recordResolution(encounterId: String): PeriodProgress = copy(resolvedEncounterIds = resolvedEncounterIds + encounterId)
}

data class ExplorationProgress(
    val visitedSpotIds: Set<String> = emptySet(),
    val distanceWalkedMeters: Double = 0.0,
    val cluesFound: Int = 0,
    val companionBond: Int = 0,
    val inventoryClueIds: Set<String> = emptySet(),
    val resolvedEncounterIds: Set<String> = emptySet(),
    val encounterVisitedPoiIds: Set<String> = emptySet(),
    val recentPoiIds: List<String> = emptyList(),
    val recentTemplateIds: List<String> = emptyList(),
    val recentPairKeys: List<String> = emptyList(),
    val companionMemoryKeys: Set<String> = emptySet(),
    val daily: PeriodProgress = PeriodProgress(),
    val weekly: PeriodProgress = PeriodProgress(),
    val dailyGoalPeriodKey: String = "",
    val dailyGoalIds: List<String> = emptyList(),
    val recentDailyGoalIds: List<String> = emptyList(),
    val weeklyGoalPeriodKey: String = "",
    val weeklyGoalIds: List<String> = emptyList(),
    val recentWeeklyGoalIds: List<String> = emptyList(),
) {
    fun normalizePeriods(date: LocalDate): ExplorationProgress = copy(
        daily = daily.resetIfNeeded(dailyPeriodKey(date)),
        weekly = weekly.resetIfNeeded(weeklyPeriodKey(date)),
    )

    fun syncExploration(state: ExplorationState, date: LocalDate): ExplorationProgress {
        val normalized = normalizePeriods(date)
        val distanceDelta = (state.distanceWalkedMeters - normalized.distanceWalkedMeters).coerceAtLeast(0.0)
        return normalized.copy(
            visitedSpotIds = state.visitedSpotIds,
            distanceWalkedMeters = state.distanceWalkedMeters,
            cluesFound = state.cluesFound,
            companionBond = state.companion.bond,
            daily = normalized.daily.recordDistance(distanceDelta),
            weekly = normalized.weekly.recordDistance(distanceDelta),
        )
    }

    fun recordClue(clueId: String, date: LocalDate): ExplorationProgress {
        val normalized = normalizePeriods(date)
        if (clueId in normalized.inventoryClueIds) return normalized
        return normalized.copy(
            inventoryClueIds = normalized.inventoryClueIds + clueId,
            daily = normalized.daily.recordClue(clueId),
            weekly = normalized.weekly.recordClue(clueId),
        )
    }

    fun recordEncounterVisit(poiId: String, templateId: String, date: LocalDate): ExplorationProgress {
        val normalized = normalizePeriods(date)
        return normalized.copy(
            encounterVisitedPoiIds = normalized.encounterVisitedPoiIds + poiId,
            recentPoiIds = pushRecentUnique(normalized.recentPoiIds, listOf(poiId), maxSize = 12),
            recentTemplateIds = pushRecentUnique(normalized.recentTemplateIds, listOf(templateId), maxSize = 12),
            recentPairKeys = pushRecentUnique(normalized.recentPairKeys, listOf("$poiId:$templateId"), maxSize = 12),
            daily = normalized.daily.recordDiscovery(poiId),
            weekly = normalized.weekly.recordDiscovery(poiId),
        )
    }

    fun recordResolution(encounter: MysteryEncounter, date: LocalDate): ExplorationProgress {
        val normalized = normalizePeriods(date)
        if (encounter.id in normalized.resolvedEncounterIds) return normalized
        return normalized.copy(
            resolvedEncounterIds = normalized.resolvedEncounterIds + encounter.id,
            daily = normalized.daily.recordResolution(encounter.id),
            weekly = normalized.weekly.recordResolution(encounter.id),
        )
    }

    fun recordMemory(memoryKey: String): ExplorationProgress =
        if (memoryKey.isBlank()) this else copy(companionMemoryKeys = companionMemoryKeys + memoryKey)
}

internal fun pushRecentUnique(existing: List<String>, values: List<String>, maxSize: Int): List<String> =
    (values + existing).distinct().take(maxSize)

interface ProgressStore {
    suspend fun load(): ExplorationProgress
    suspend fun save(progress: ExplorationProgress)
}

fun ExplorationState.toProgress(
    previous: ExplorationProgress = ExplorationProgress(),
    date: LocalDate = LocalDate.now(),
): ExplorationProgress = previous.syncExploration(this, date)

fun ExplorationProgress.toState(defaultCompanion: Companion): ExplorationState = ExplorationState(
    companion = defaultCompanion.copy(bond = companionBond.takeIf { it > 0 } ?: defaultCompanion.bond),
    visitedSpotIds = visitedSpotIds,
    distanceWalkedMeters = distanceWalkedMeters,
    cluesFound = cluesFound,
)

fun dailyPeriodKey(date: LocalDate): String = date.toString()

fun weeklyPeriodKey(date: LocalDate): String =
    "${date.get(IsoFields.WEEK_BASED_YEAR)}-W${date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString().padStart(2, '0')}"
