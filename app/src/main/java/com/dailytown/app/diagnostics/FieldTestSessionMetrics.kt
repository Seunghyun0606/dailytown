package com.dailytown.app.diagnostics

import kotlin.math.abs
import kotlin.math.roundToInt

data class BatterySnapshot(
    val levelPercent: Int?,
    val chargeCounterMicroAh: Int?,
    val charging: Boolean,
)

fun interface BatterySnapshotSource {
    fun read(): BatterySnapshot
}

enum class BatteryMeasurementStatus {
    NOT_STARTED,
    UNAVAILABLE,
    CHARGING,
    VALID,
}

data class FieldTestSessionMetrics(
    val sessionDistanceMeters: Int,
    val referenceDistanceMeters: Int?,
    val distanceErrorPercent: Int?,
    val batteryMeasurementStatus: BatteryMeasurementStatus,
    val batteryStartPercent: Int?,
    val batteryEndPercent: Int?,
    val batteryDrainPercentPoints: Int?,
    val batteryDrainPercentPerHour: Int?,
    val batteryChargeConsumedMah: Int?,
)

/**
 * Captures only coarse battery snapshots plus derived distance/battery metrics.
 * It never receives or stores raw GPS coordinates.
 */
class FieldTestSessionMonitor(
    private val batterySource: BatterySnapshotSource,
) {
    private var batteryStart: BatterySnapshot? = null
    private var batteryEnd: BatterySnapshot? = null

    fun begin() {
        batteryStart = batterySource.read()
        batteryEnd = null
    }

    fun end() {
        if (batteryStart != null && batteryEnd == null) {
            batteryEnd = batterySource.read()
        }
    }

    fun reset() {
        batteryStart = null
        batteryEnd = null
    }

    fun metrics(
        sessionDistanceMeters: Double,
        sessionDurationSeconds: Int,
        referenceDistanceMeters: Int? = null,
    ): FieldTestSessionMetrics {
        val start = batteryStart
        val end = batteryEnd ?: start?.let { batterySource.read() }
        val battery = batteryMetrics(start, end, sessionDurationSeconds)
        val measuredDistance = sessionDistanceMeters.coerceAtLeast(0.0).roundToInt()
        val reference = referenceDistanceMeters?.takeIf { it > 0 }
        val distanceError = reference?.let {
            ((abs(measuredDistance - it).toDouble() / it) * 100.0).roundToInt()
        }

        return FieldTestSessionMetrics(
            sessionDistanceMeters = measuredDistance,
            referenceDistanceMeters = reference,
            distanceErrorPercent = distanceError,
            batteryMeasurementStatus = battery.status,
            batteryStartPercent = start?.levelPercent,
            batteryEndPercent = end?.levelPercent,
            batteryDrainPercentPoints = battery.drainPercentPoints,
            batteryDrainPercentPerHour = battery.drainPercentPerHour,
            batteryChargeConsumedMah = battery.chargeConsumedMah,
        )
    }

    private fun batteryMetrics(
        start: BatterySnapshot?,
        end: BatterySnapshot?,
        durationSeconds: Int,
    ): BatteryDerivedMetrics {
        if (start == null || end == null) {
            return BatteryDerivedMetrics(BatteryMeasurementStatus.NOT_STARTED)
        }
        if (start.charging || end.charging) {
            return BatteryDerivedMetrics(BatteryMeasurementStatus.CHARGING)
        }

        val startPercent = start.levelPercent
        val endPercent = end.levelPercent
        val drain = if (startPercent != null && endPercent != null) {
            (startPercent - endPercent).coerceAtLeast(0)
        } else {
            null
        }
        val drainPerHour = if (drain != null && durationSeconds > 0) {
            (drain * 3600.0 / durationSeconds).roundToInt()
        } else {
            null
        }
        val chargeConsumedMah = start.chargeCounterMicroAh
            ?.let { startCharge ->
                end.chargeCounterMicroAh?.let { endCharge ->
                    ((startCharge - endCharge).coerceAtLeast(0) / 1000.0).roundToInt()
                }
            }

        val status = if (drain != null || chargeConsumedMah != null) {
            BatteryMeasurementStatus.VALID
        } else {
            BatteryMeasurementStatus.UNAVAILABLE
        }
        return BatteryDerivedMetrics(
            status = status,
            drainPercentPoints = drain,
            drainPercentPerHour = drainPerHour,
            chargeConsumedMah = chargeConsumedMah,
        )
    }

    private data class BatteryDerivedMetrics(
        val status: BatteryMeasurementStatus,
        val drainPercentPoints: Int? = null,
        val drainPercentPerHour: Int? = null,
        val chargeConsumedMah: Int? = null,
    )
}
