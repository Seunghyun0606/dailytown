package com.dailytown.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FieldTestSessionMetricsTest {
    @Test
    fun `distance error and battery drain are derived without raw locations`() {
        val readings = ArrayDeque(
            listOf(
                BatterySnapshot(levelPercent = 80, chargeCounterMicroAh = 3_000_000, externallyPowered = false),
                BatterySnapshot(levelPercent = 78, chargeCounterMicroAh = 2_940_000, externallyPowered = false),
            ),
        )
        val monitor = FieldTestSessionMonitor { readings.removeFirst() }

        monitor.begin()
        monitor.end()
        val metrics = monitor.metrics(
            sessionDistanceMeters = 950.0,
            sessionDurationSeconds = 1800,
            referenceDistanceMeters = 1000,
        )

        assertEquals(950, metrics.sessionDistanceMeters)
        assertEquals(1000, metrics.referenceDistanceMeters)
        assertEquals(5, metrics.distanceErrorPercent)
        assertEquals(BatteryMeasurementStatus.VALID, metrics.batteryMeasurementStatus)
        assertEquals(2, metrics.batteryDrainPercentPoints)
        assertEquals(4, metrics.batteryDrainPercentPerHour)
        assertEquals(60, metrics.batteryChargeConsumedMah)
    }

    @Test
    fun `external power invalidates battery drain but keeps distance evidence`() {
        val readings = ArrayDeque(
            listOf(
                BatterySnapshot(levelPercent = 50, chargeCounterMicroAh = 2_000_000, externallyPowered = true),
                BatterySnapshot(levelPercent = 52, chargeCounterMicroAh = 2_050_000, externallyPowered = true),
            ),
        )
        val monitor = FieldTestSessionMonitor { readings.removeFirst() }

        monitor.begin()
        monitor.end()
        val metrics = monitor.metrics(
            sessionDistanceMeters = 1020.0,
            sessionDurationSeconds = 600,
            referenceDistanceMeters = 1000,
        )

        assertEquals(2, metrics.distanceErrorPercent)
        assertEquals(BatteryMeasurementStatus.EXTERNALLY_POWERED, metrics.batteryMeasurementStatus)
        assertNull(metrics.batteryDrainPercentPoints)
        assertNull(metrics.batteryDrainPercentPerHour)
        assertNull(metrics.batteryChargeConsumedMah)
    }

    @Test
    fun `unsupported battery properties stay unavailable instead of inventing consumption`() {
        val monitor = FieldTestSessionMonitor {
            BatterySnapshot(levelPercent = null, chargeCounterMicroAh = null, externallyPowered = false)
        }

        monitor.begin()
        val metrics = monitor.metrics(
            sessionDistanceMeters = 10.0,
            sessionDurationSeconds = 60,
        )

        assertEquals(BatteryMeasurementStatus.UNAVAILABLE, metrics.batteryMeasurementStatus)
        assertNull(metrics.distanceErrorPercent)
        assertNull(metrics.batteryDrainPercentPerHour)
    }
}
