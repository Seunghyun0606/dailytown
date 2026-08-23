package com.dailytown.app.location

import com.dailytown.app.domain.GeoPoint

data class LocationSample(
    val point: GeoPoint,
    val accuracyMeters: Float,
    val bearingDegrees: Float? = null,
    val elapsedRealtimeMillis: Long,
)

interface LocationSource {
    val name: String
    fun start(onLocation: (LocationSample) -> Unit, onError: (Throwable) -> Unit = {})
    fun stop()
}
