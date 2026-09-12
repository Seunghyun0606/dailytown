package com.dailytown.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameplaySessionMetricsTest {
    @Test
    fun `derived gameplay rates use counters without retaining event identifiers`() {
        val monitor = GameplaySessionMonitor()

        monitor.recordEncounterOffered(isRevisit = false)
        monitor.recordHinted()
        monitor.recordDiscovered(isRevisit = false)
        monitor.recordClueCollected()
        monitor.recordResolved(isRevisit = false)

        monitor.recordEncounterOffered(isRevisit = true)
        monitor.recordHinted()
        monitor.recordDiscovered(isRevisit = true)
        monitor.recordClueCollected()

        val metrics = monitor.snapshot()

        assertEquals(2, metrics.encounterOfferedCount)
        assertEquals(2, metrics.hintedEncounterCount)
        assertEquals(2, metrics.discoveredEncounterCount)
        assertEquals(1, metrics.resolvedEncounterCount)
        assertEquals(2, metrics.cluesCollectedCount)
        assertEquals(1, metrics.revisitOfferedCount)
        assertEquals(1, metrics.revisitDiscoveredCount)
        assertEquals(0, metrics.revisitResolvedCount)
        assertEquals(100, metrics.encounterDiscoveryRatePercent)
        assertEquals(50, metrics.encounterResolutionRatePercent)
        assertEquals(50, metrics.revisitSharePercent)
        assertEquals(0, metrics.revisitResolutionRatePercent)
        assertEquals(100, metrics.repeatAreaFatigueProxyPercent)
    }

    @Test
    fun `revisit completion lowers repeat area fatigue proxy`() {
        val monitor = GameplaySessionMonitor()

        repeat(2) {
            monitor.recordEncounterOffered(isRevisit = true)
            monitor.recordDiscovered(isRevisit = true)
        }
        monitor.recordResolved(isRevisit = true)

        val metrics = monitor.snapshot()

        assertEquals(50, metrics.revisitResolutionRatePercent)
        assertEquals(50, metrics.repeatAreaFatigueProxyPercent)
    }

    @Test
    fun `no discovered revisit leaves fatigue not evaluated`() {
        val monitor = GameplaySessionMonitor()
        monitor.recordEncounterOffered(isRevisit = false)

        val metrics = monitor.snapshot()

        assertEquals(0, metrics.discoveredEncounterCount)
        assertNull(metrics.encounterResolutionRatePercent)
        assertNull(metrics.revisitResolutionRatePercent)
        assertNull(metrics.repeatAreaFatigueProxyPercent)
    }

    @Test
    fun `reset clears only session telemetry`() {
        val monitor = GameplaySessionMonitor()
        monitor.recordEncounterOffered(isRevisit = true)
        monitor.recordDiscovered(isRevisit = true)
        monitor.recordResolved(isRevisit = true)
        monitor.recordClueCollected()

        monitor.reset()
        val metrics = monitor.snapshot()

        assertEquals(GameplaySessionMetrics(), metrics)
    }
}
