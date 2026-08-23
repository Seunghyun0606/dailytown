package com.dailytown.app.diagnostics

import kotlin.math.roundToInt

/**
 * Privacy-safe, session-local gameplay telemetry used only for field-test diagnostics.
 *
 * It stores counters rather than POI, encounter, template, route, or user identifiers. The
 * repeat-area fatigue value is deliberately a proxy: the unresolved share of discovered revisit
 * encounters. It is useful for comparing representative routes, but is not a direct sentiment
 * measurement.
 */
data class GameplaySessionMetrics(
    val encounterOfferedCount: Int = 0,
    val hintedEncounterCount: Int = 0,
    val discoveredEncounterCount: Int = 0,
    val resolvedEncounterCount: Int = 0,
    val cluesCollectedCount: Int = 0,
    val revisitOfferedCount: Int = 0,
    val revisitDiscoveredCount: Int = 0,
    val revisitResolvedCount: Int = 0,
) {
    val encounterDiscoveryRatePercent: Int?
        get() = percent(discoveredEncounterCount, encounterOfferedCount)

    val encounterResolutionRatePercent: Int?
        get() = percent(resolvedEncounterCount, discoveredEncounterCount)

    val revisitSharePercent: Int?
        get() = percent(revisitOfferedCount, encounterOfferedCount)

    val revisitResolutionRatePercent: Int?
        get() = percent(revisitResolvedCount, revisitDiscoveredCount)

    val repeatAreaFatigueProxyPercent: Int?
        get() = revisitResolutionRatePercent?.let { (100 - it).coerceIn(0, 100) }

    private fun percent(numerator: Int, denominator: Int): Int? {
        if (denominator <= 0) return null
        return ((numerator.coerceAtLeast(0) * 100.0) / denominator)
            .roundToInt()
            .coerceIn(0, 100)
    }
}

/** Framework-free counter owner; no event IDs or raw event payloads are retained. */
class GameplaySessionMonitor {
    private var encounterOfferedCount = 0
    private var hintedEncounterCount = 0
    private var discoveredEncounterCount = 0
    private var resolvedEncounterCount = 0
    private var cluesCollectedCount = 0
    private var revisitOfferedCount = 0
    private var revisitDiscoveredCount = 0
    private var revisitResolvedCount = 0

    fun reset() {
        encounterOfferedCount = 0
        hintedEncounterCount = 0
        discoveredEncounterCount = 0
        resolvedEncounterCount = 0
        cluesCollectedCount = 0
        revisitOfferedCount = 0
        revisitDiscoveredCount = 0
        revisitResolvedCount = 0
    }

    fun recordEncounterOffered(isRevisit: Boolean) {
        encounterOfferedCount += 1
        if (isRevisit) revisitOfferedCount += 1
    }

    fun recordHinted() {
        hintedEncounterCount += 1
    }

    fun recordDiscovered(isRevisit: Boolean) {
        discoveredEncounterCount += 1
        if (isRevisit) revisitDiscoveredCount += 1
    }

    fun recordClueCollected() {
        cluesCollectedCount += 1
    }

    fun recordResolved(isRevisit: Boolean) {
        resolvedEncounterCount += 1
        if (isRevisit) revisitResolvedCount += 1
    }

    fun snapshot(): GameplaySessionMetrics = GameplaySessionMetrics(
        encounterOfferedCount = encounterOfferedCount,
        hintedEncounterCount = hintedEncounterCount,
        discoveredEncounterCount = discoveredEncounterCount,
        resolvedEncounterCount = resolvedEncounterCount,
        cluesCollectedCount = cluesCollectedCount,
        revisitOfferedCount = revisitOfferedCount,
        revisitDiscoveredCount = revisitDiscoveredCount,
        revisitResolvedCount = revisitResolvedCount,
    )
}
