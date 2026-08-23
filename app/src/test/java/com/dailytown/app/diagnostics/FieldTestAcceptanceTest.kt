package com.dailytown.app.diagnostics

import com.dailytown.app.map.MapHealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestAcceptanceTest {
    private val evaluator = FieldTestAcceptanceEvaluator()

    @Test
    fun `unset criteria never invent a pass`() {
        val result = evaluator.evaluate(
            criteria = FieldTestAcceptanceCriteria(),
            input = FieldTestAcceptanceInput(
                sessionDurationSeconds = 1_000,
                gpsRejectionRatePercent = 0,
                mapHealth = MapHealthStatus.READY,
                distanceErrorPercent = 1,
                batteryDrainPercentPerHour = 3,
            ),
        )

        assertEquals(AcceptanceCheckStatus.NOT_EVALUATED, result.overall)
        assertTrue(result.checks.isEmpty())
    }

    @Test
    fun `configured criteria pass when all measured values meet thresholds`() {
        val result = evaluator.evaluate(
            criteria = FieldTestAcceptanceCriteria(
                minimumSessionDurationSeconds = 600,
                maximumGpsRejectionRatePercent = 15,
                requiredMapHealth = MapHealthStatus.READY,
                maximumDistanceErrorPercent = 10,
                maximumBatteryDrainPercentPerHour = 8,
            ),
            input = FieldTestAcceptanceInput(
                sessionDurationSeconds = 900,
                gpsRejectionRatePercent = 8,
                mapHealth = MapHealthStatus.READY,
                distanceErrorPercent = 6,
                batteryDrainPercentPerHour = 5,
            ),
        )

        assertEquals(AcceptanceCheckStatus.PASS, result.overall)
        assertEquals(5, result.checks.size)
        assertTrue(result.failedKeys.isEmpty())
    }

    @Test
    fun `failed distance and battery metrics report their keys`() {
        val result = evaluator.evaluate(
            criteria = FieldTestAcceptanceCriteria(
                maximumDistanceErrorPercent = 10,
                maximumBatteryDrainPercentPerHour = 6,
            ),
            input = FieldTestAcceptanceInput(
                distanceErrorPercent = 18,
                batteryDrainPercentPerHour = 9,
            ),
        )

        assertEquals(AcceptanceCheckStatus.FAIL, result.overall)
        assertEquals(
            listOf("distanceErrorPercent", "batteryDrainPercentPerHour"),
            result.failedKeys,
        )
    }

    @Test
    fun `one failed metric fails overall result and reports the metric key`() {
        val result = evaluator.evaluate(
            criteria = FieldTestAcceptanceCriteria(
                minimumSessionDurationSeconds = 600,
                maximumGpsRejectionRatePercent = 10,
                requiredMapHealth = MapHealthStatus.READY,
            ),
            input = FieldTestAcceptanceInput(
                sessionDurationSeconds = 900,
                gpsRejectionRatePercent = 18,
                mapHealth = MapHealthStatus.READY,
            ),
        )

        assertEquals(AcceptanceCheckStatus.FAIL, result.overall)
        assertEquals(listOf("gpsRejectionRatePercent"), result.failedKeys)
    }

    @Test
    fun `missing evidence is not evaluated rather than failed`() {
        val result = evaluator.evaluate(
            criteria = FieldTestAcceptanceCriteria(
                minimumSessionDurationSeconds = 600,
                maximumGpsRejectionRatePercent = 10,
                maximumDistanceErrorPercent = 10,
                maximumBatteryDrainPercentPerHour = 8,
            ),
            input = FieldTestAcceptanceInput(
                sessionDurationSeconds = 900,
                gpsRejectionRatePercent = null,
                distanceErrorPercent = null,
                batteryDrainPercentPerHour = null,
            ),
        )

        assertEquals(AcceptanceCheckStatus.NOT_EVALUATED, result.overall)
        assertEquals(
            AcceptanceCheckStatus.NOT_EVALUATED,
            result.checks.first { it.key == "gpsRejectionRatePercent" }.status,
        )
        assertEquals(
            AcceptanceCheckStatus.NOT_EVALUATED,
            result.checks.first { it.key == "distanceErrorPercent" }.status,
        )
        assertEquals(
            AcceptanceCheckStatus.NOT_EVALUATED,
            result.checks.first { it.key == "batteryDrainPercentPerHour" }.status,
        )
    }
}
