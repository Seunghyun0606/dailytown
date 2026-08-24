package com.dailytown.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestProtocolTest {
    private val evaluator = FieldTestProtocolEvaluator()

    @Test
    fun `missing cohort is data insufficient`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA))

        val assessment = evaluator.evaluate(recorder.report(), FieldTestProtocolCriteria())

        assertEquals(FieldTestProtocolStatus.DATA_INSUFFICIENT, assessment.status)
        assertTrue(assessment.issues.any { it.key == "repeatAreaSessions" })
    }

    @Test
    fun `two cohorts with shared evidence are comparable when product protocol is unset`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA))

        val assessment = evaluator.evaluate(recorder.report(), FieldTestProtocolCriteria())

        assertEquals(FieldTestProtocolStatus.COMPARABLE, assessment.status)
        assertFalse(assessment.configured)
        assertTrue(assessment.issues.isEmpty())
    }

    @Test
    fun `configured minimum sample and evidence gates can make product review ready`() {
        val recorder = FieldTestComparisonRecorder()
        repeat(3) {
            recorder.record(summary(FieldTestAreaProfile.NEW_AREA, preset = "BALANCED", battery = 5))
            recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, preset = "BALANCED", battery = 7, fatigue = 50))
        }
        val criteria = FieldTestProtocolCriteria(
            minimumSessionsPerCohort = 3,
            requireMatchingTrackingPreset = true,
            requiredEvidence = setOf(
                FieldTestProtocolEvidence.BATTERY_DRAIN,
                FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS,
                FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION,
            ),
        )

        val assessment = evaluator.evaluate(recorder.report(), criteria)

        assertEquals(FieldTestProtocolStatus.PRODUCT_REVIEW_READY, assessment.status)
        assertTrue(assessment.configured)
        assertTrue(assessment.issues.isEmpty())
    }

    @Test
    fun `comparison remains comparable when configured cohort size is not met`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA))

        val assessment = evaluator.evaluate(
            recorder.report(),
            FieldTestProtocolCriteria(minimumSessionsPerCohort = 3),
        )

        assertEquals(FieldTestProtocolStatus.COMPARABLE, assessment.status)
        assertTrue(assessment.issues.any { it.key == "newAreaMinimumSessions" && it.detail == "1/3" })
        assertTrue(assessment.issues.any { it.key == "repeatAreaMinimumSessions" && it.detail == "1/3" })
    }

    @Test
    fun `required evidence uses minimum cohort count instead of converting missing values to zero`() {
        val recorder = FieldTestComparisonRecorder()
        repeat(2) {
            recorder.record(summary(FieldTestAreaProfile.NEW_AREA, battery = if (it == 0) 5 else null))
            recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, battery = 7))
        }

        val assessment = evaluator.evaluate(
            recorder.report(),
            FieldTestProtocolCriteria(
                minimumSessionsPerCohort = 2,
                requiredEvidence = setOf(FieldTestProtocolEvidence.BATTERY_DRAIN),
            ),
        )

        assertEquals(FieldTestProtocolStatus.COMPARABLE, assessment.status)
        assertTrue(
            assessment.issues.any {
                it.key == "newAreaEvidence.BATTERY_DRAIN" && it.detail == "1/2"
            },
        )
        assertFalse(assessment.issues.any { it.key == "repeatAreaEvidence.BATTERY_DRAIN" })
    }

    @Test
    fun `matching preset gate rejects mixed preset cohorts without invalidating comparison itself`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, preset = "BALANCED"))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, preset = "PRECISE"))

        val assessment = evaluator.evaluate(
            recorder.report(),
            FieldTestProtocolCriteria(requireMatchingTrackingPreset = true),
        )

        assertEquals(FieldTestProtocolStatus.COMPARABLE, assessment.status)
        assertTrue(assessment.issues.any { it.key == "trackingPresetConsistency" })
    }

    @Test
    fun `protocol render contains only safe status and issue metadata`() {
        val recorder = FieldTestComparisonRecorder()
        recorder.record(summary(FieldTestAreaProfile.NEW_AREA, preset = "BALANCED"))
        recorder.record(summary(FieldTestAreaProfile.REPEAT_AREA, preset = "PRECISE"))
        val text = evaluator.evaluate(
            recorder.report(),
            FieldTestProtocolCriteria(requireMatchingTrackingPreset = true),
        ).render()

        assertTrue(text.contains("protocolStatus=COMPARABLE"))
        assertTrue(text.contains("trackingPresetConsistency"))
        assertFalse(text.contains("latitude", ignoreCase = true))
        assertFalse(text.contains("longitude", ignoreCase = true))
        assertFalse(text.contains("poiId", ignoreCase = true))
        assertFalse(text.contains("encounterId", ignoreCase = true))
        assertFalse(text.contains("NAVER_MAP_NCP_KEY_ID"))
    }

    @Test
    fun `evidence csv parser accepts enum keys and blank input`() {
        assertTrue(FieldTestProtocolEvidence.parseCsv("").isEmpty())
        assertEquals(
            setOf(
                FieldTestProtocolEvidence.BATTERY_DRAIN,
                FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION,
            ),
            FieldTestProtocolEvidence.parseCsv("BATTERY_DRAIN, ENCOUNTER_RESOLUTION"),
        )
    }

    private fun summary(
        profile: FieldTestAreaProfile,
        preset: String = "BALANCED",
        battery: Int? = 5,
        fatigue: Int? = if (profile == FieldTestAreaProfile.REPEAT_AREA) 50 else null,
    ) = FieldTestSessionSummary(
        areaProfile = profile,
        trackingPreset = preset,
        mapHealthStatus = "READY",
        sessionDurationSeconds = 600,
        sessionDistanceMeters = 1000,
        gpsRejectionRatePercent = 10,
        distanceErrorPercent = 5,
        batteryDrainPercentPerHour = battery,
        discoveredEncountersPerSession = 3,
        encounterResolutionRatePercent = 67,
        revisitSharePercent = if (profile == FieldTestAreaProfile.REPEAT_AREA) 100 else 0,
        repeatAreaFatigueProxyPercent = fatigue,
        acceptanceOverall = AcceptanceCheckStatus.NOT_EVALUATED.name,
    )
}
