package com.dailytown.app.location

enum class LocationPriorityMode { LOW_POWER, BALANCED, HIGH_ACCURACY }

data class LocationTrackingConfig(
    val priorityMode: LocationPriorityMode,
    val intervalMillis: Long,
    val minUpdateIntervalMillis: Long,
    val minUpdateDistanceMeters: Float,
    val maxAcceptedAccuracyMeters: Float,
)

enum class LocationTrackingPreset(val config: LocationTrackingConfig) {
    BATTERY_SAVER(
        LocationTrackingConfig(
            priorityMode = LocationPriorityMode.LOW_POWER,
            intervalMillis = 12_000L,
            minUpdateIntervalMillis = 6_000L,
            minUpdateDistanceMeters = 15f,
            maxAcceptedAccuracyMeters = 100f,
        ),
    ),
    BALANCED(
        LocationTrackingConfig(
            priorityMode = LocationPriorityMode.BALANCED,
            intervalMillis = 5_000L,
            minUpdateIntervalMillis = 2_500L,
            minUpdateDistanceMeters = 8f,
            maxAcceptedAccuracyMeters = 75f,
        ),
    ),
    PRECISE(
        LocationTrackingConfig(
            priorityMode = LocationPriorityMode.HIGH_ACCURACY,
            intervalMillis = 3_000L,
            minUpdateIntervalMillis = 1_500L,
            minUpdateDistanceMeters = 5f,
            maxAcceptedAccuracyMeters = 65f,
        ),
    ),
}
