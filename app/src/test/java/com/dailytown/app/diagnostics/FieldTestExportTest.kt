package com.dailytown.app.diagnostics

import com.dailytown.app.map.MapHealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestExportTest {
    @Test
    fun `structured export includes sessions policies protocol and missing evidence as null`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(
            summary(
                profile = FieldTestAreaProfile.NEW_AREA,
                battery = null,
                fatigue = null,
                review = FieldTestRunReviewStatus.REFERENCE_ONLY,
            ),
        )
        recorder.record(
            summary(
                profile = FieldTestAreaProfile.REPEAT_AREA,
                battery = 7,
                fatigue = 40,
                review = FieldTestRunReviewStatus.REVIEWABLE,
            ),
        )
        val protocolCriteria = FieldTestProtocolCriteria(
            minimumSessionsPerCohort = 1,
            requireMatchingTrackingPreset = true,
            requiredEvidence = setOf(
                FieldTestProtocolEvidence.DISTANCE_ERROR,
                FieldTestProtocolEvidence.BATTERY_DRAIN,
            ),
        )
        val report = recorder.report()
        val protocol = FieldTestProtocolEvaluator().evaluate(report, protocolCriteria)

        val json = FieldTestExportBundle(
            appVersion = "0.7.0",
            packageId = "com.dailytown.app",
            sessions = recorder.snapshot(),
            comparison = report,
            acceptanceCriteria = FieldTestAcceptanceCriteria(
                minimumSessionDurationSeconds = 600,
                requiredMapHealth = MapHealthStatus.READY,
                maximumDistanceErrorPercent = 10,
            ),
            protocolCriteria = protocolCriteria,
            protocolAssessment = protocol,
        ).renderJson()

        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"schema\":\"dailytown.field_test_export\""))
        assertTrue(json.contains("\"schemaVersion\":1"))
        assertTrue(json.contains("\"packageId\":\"com.dailytown.app\""))
        assertTrue(json.contains("\"runReviewStatus\":\"REFERENCE_ONLY\""))
        assertTrue(json.contains("\"runReviewStatus\":\"REVIEWABLE\""))
        assertTrue(json.contains("\"batteryDrainPercentPerHour\":null"))
        assertTrue(json.contains("\"requiredEvidence\":[\"BATTERY_DRAIN\",\"DISTANCE_ERROR\"]"))
        assertTrue(json.contains("\"status\":\"COMPARABLE\""))
        assertTrue(json.contains("\"evidenceCount\":1"))
        assertTrue(json.contains("\"appPersistence\":false"))
    }

    @Test
    fun `structured export excludes location event credential device and session identifiers`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, review = FieldTestRunReviewStatus.NEEDS_ATTENTION))
        val report = recorder.report()
        val protocolCriteria = FieldTestProtocolCriteria()

        val json = FieldTestExportBundle(
            appVersion = "test",
            packageId = "com.dailytown.app",
            sessions = recorder.snapshot(),
            comparison = report,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            protocolCriteria = protocolCriteria,
            protocolAssessment = FieldTestProtocolEvaluator().evaluate(report, protocolCriteria),
        ).renderJson()

        assertFalse(json.contains("latitude", ignoreCase = true))
        assertFalse(json.contains("longitude", ignoreCase = true))
        assertFalse(json.contains("poiId", ignoreCase = true))
        assertFalse(json.contains("encounterId", ignoreCase = true))
        assertFalse(json.contains("templateId", ignoreCase = true))
        assertFalse(json.contains("sessionToken", ignoreCase = true))
        assertFalse(json.contains("generatedAt", ignoreCase = true))
        assertFalse(json.contains("deviceId", ignoreCase = true))
        assertFalse(json.contains("NAVER_MAP_NCP_KEY_ID"))
        assertFalse(json.contains("wGAo", ignoreCase = true))
    }

    @Test
    fun `recorder snapshot is detached bounded and preserves review state`() {
        val recorder = FieldTestComparisonRecorder(maximumSessions = 2)
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 1, review = FieldTestRunReviewStatus.REFERENCE_ONLY))
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, discovered = 2, review = FieldTestRunReviewStatus.REVIEWABLE))
        val detached = recorder.snapshot()
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, discovered = 3, review = FieldTestRunReviewStatus.NEEDS_ATTENTION))

        assertEquals(2, detached.size)
        assertEquals(1, detached.first().discoveredEncountersPerSession)
        assertEquals(FieldTestRunReviewStatus.REFERENCE_ONLY, detached.first().runReviewStatus)

        val latest = recorder.snapshot()
        assertEquals(2, latest.size)
        assertEquals(2, latest.first().discoveredEncountersPerSession)
        assertEquals(FieldTestRunReviewStatus.REVIEWABLE, latest.first().runReviewStatus)
        assertEquals(FieldTestAreaProfile.REPEAT_AREA, latest.last().areaProfile)
        assertEquals(FieldTestRunReviewStatus.NEEDS_ATTENTION, latest.last().runReviewStatus)
    }

    @Test
    fun `summary keeps missing evidence missing`() {
        val summary = summary(
            profile = FieldTestAreaProfile.REPEAT_AREA,
            battery = null,
            fatigue = null,
        )

        assertNull(summary.batteryDrainPercentPerHour)
        assertNull(summary.repeatAreaFatigueProxyPercent)
    }

    private fun summary(
        profile: FieldTestAreaProfile,
        discovered: Int? = 2,
        battery: Int? = 5,
        fatigue: Int? = 30,
        review: FieldTestRunReviewStatus? = null,
    ): FieldTestSessionSummary = FieldTestSessionSummary(
        areaProfile = profile,
        trackingPreset = "BALANCED",
        mapHealthStatus = "READY",
        sessionDurationSeconds = 600,
        sessionDistanceMeters = 1000,
        gpsRejectionRatePercent = 5,
        distanceErrorPercent = 4,
        batteryDrainPercentPerHour = battery,
        discoveredEncountersPerSession = discovered,
        encounterResolutionRatePercent = 50,
        revisitSharePercent = if (profile == FieldTestAreaProfile.REPEAT_AREA) 60 else 0,
        repeatAreaFatigueProxyPercent = fatigue,
        acceptanceOverall = AcceptanceCheckStatus.NOT_EVALUATED.name,
        runReviewStatus = review,
    )
}
