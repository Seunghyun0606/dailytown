package com.dailytown.app.location

import com.dailytown.app.domain.ExplorationEngine

class LocationQualityPolicy(
    private val maxAccuracyMeters: Float = 65f,
    private val maxWalkingSpeedMetersPerSecond: Double = 12.5,
) {
    private val distance = ExplorationEngine()

    fun accepts(previous: LocationSample?, next: LocationSample): Boolean {
        if (next.accuracyMeters <= 0f || next.accuracyMeters > maxAccuracyMeters) return false
        if (previous == null) return true

        val elapsedSeconds = (next.elapsedRealtimeMillis - previous.elapsedRealtimeMillis) / 1_000.0
        if (elapsedSeconds <= 0.0) return false
        val meters = distance.distanceMeters(previous.point, next.point)
        return meters / elapsedSeconds <= maxWalkingSpeedMetersPerSecond
    }
}
