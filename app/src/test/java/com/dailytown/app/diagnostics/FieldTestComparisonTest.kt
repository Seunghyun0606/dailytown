package com.dailytown.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestComparisonTest {
    @Test
    fun `comparison averages cohorts and reports repeat minus new deltas`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 4, resolution = 75, fatigue = null, gps = 10))
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 2, resolution = 50, fatigue = null, gps = 20))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, discovered = 2, resolution = 50, fatigue = 50, gps = 30))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, discovered = 2, resolution = 0, fatigue = 100, gps = 10))

        val report = recorder.report()

        assertEquals(2, report.newArea.sessionCount)
        assertEquals(3, report.newArea.discoveredEncountersPerSession.average)
        assertEquals(63, report.newArea.encounterResolutionRatePercent.average)
        assertEquals(2, report.repeatArea.discoveredEncountersPerSession.average)
        assertEquals(25, report.repeatArea.encounterResolutionRatePercent.average)
        assertEquals(75, report.repeatArea.repeatAreaFatigueProxyPercent.average)
        assertEquals(
            -1,
            report.deltas.first { it.key == "discoveredEncountersPerSession" }.repeatMinusNew,
        )
        assertEquals(
            -38,
            report.deltas.first { it.key == "encounterResolutionRatePercent" }.repeatMinusNew,
        )
    }

    @Test
    fun `missing evidence keeps evidence count and does not fabricate delta`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, battery = null, fatigue = null))
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, battery = 6, fatigue = null))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, battery = null, fatigue = null))

        val report = recorder.report()

        assertEquals(1, report.newArea.batteryDrainPercentPerHour.evidenceCount)
        assertEquals(2, report.newArea.batteryDrainPercentPerHour.sessionCount)
        assertEquals(6, report.newArea.batteryDrainPercentPerHour.average)
        assertEquals(0, report.repeatArea.batteryDrainPercentPerHour.evidenceCount)
        assertNull(report.repeatArea.batteryDrainPercentPerHour.average)
        assertNull(report.deltas.first { it.key == "batteryDrainPercentPerHour" }.repeatMinusNew)
        assertEquals(0, report.newArea.repeatAreaFatigueProxyPercent.evidenceCount)
    }

    @Test
    fun `bounded recorder drops oldest derived summary`() {
        val recorder = FieldTestComparisonRecorder(maximumSessions = 2)
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 1))
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 3))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, discovered = 5))

        val report = recorder.report()

        assertEquals(2, recorder.sessionCount())
        assertEquals(1, report.newArea.sessionCount)
        assertEquals(3, report.newArea.discoveredEncountersPerSession.average)
        assertEquals(1, report.repeatArea.sessionCount)
        assertEquals(5, report.repeatArea.discoveredEncountersPerSession.average)
    }

    @Test
    fun `comparison render contains only aggregate evidence and no place or event identifiers`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 3, resolution = 67))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, discovered = 2, resolution = 50, fatigue = 50))

        val text = recorder.report().render()

        assertTrue(text.contains("newArea.sessionCount=1"))
        assertTrue(text.contains("repeatArea.sessionCount=1"))
        assertTrue(text.contains("repeatArea.repeatAreaFatigueProxyPercent.average=50"))
        assertTrue(text.contains("evidence=1/1"))
        assertTrue(text.contains("comparison=repeat_area_minus_new_area"))
        assertTrue(text.contains("privacy=derived_session_summaries_only"))
        assertFalse(text.contains("latitude", ignoreCase = true))
        assertFalse(text.contains("longitude", ignoreCase = true))
        assertFalse(text.contains("poiId", ignoreCase = true))
        assertFalse(text.contains("encounterId", ignoreCase = true))
        assertFalse(text.contains("templateId", ignoreCase = true))
        assertFalse(text.contains("NAVER_MAP_NCP_KEY_ID"))
    }

    @Test
    fun `reset clears all comparison summaries`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA))

        recorder.reset()

        assertEquals(0, recorder.sessionCount())
        assertEquals(0, recorder.report().newArea.sessionCount)
        assertEquals(0, recorder.report().repeatArea.sessionCount)
    }

    private fun summary(
        profile: FieldTestAreaProfile,
        discovered: Int? = 2,
        resolution: Int? = 50,
        fatigue: Int? = 50,
        gps: Int? = 10,
        battery: Int? = 5,
    ) = FieldTestSessionSummary(
        areaProfile = profile,
        trackingPreset = "BALANCED",
        mapHealthStatus = "READY",
        sessionDurationSeconds = 600,
        sessionDistanceMeters = 1000,
        gpsRejectionRatePercent = gps,
        distanceErrorPercent = 5,
        batteryDrainPercentPerHour = battery,
        discoveredEncountersPerSession = discovered,
        encounterResolutionRatePercent = resolution,
        revisitSharePercent = if (profile == FieldTestAreaProfile.REPEAT_AREA) 100 else 0,
        repeatAreaFatigueProxyPercent = fatigue,
        acceptanceOverall = AcceptanceCheckStatus.NOT_EVALUATED.name,
    )
}
