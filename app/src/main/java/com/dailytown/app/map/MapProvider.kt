package com.dailytown.app.map

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.flow.StateFlow

enum class MapProviderId { NAVER, GOOGLE }

enum class MapHealthStatus {
    UNCONFIGURED,
    INITIALIZING,
    READY,
    AUTH_ERROR,
    ERROR,
    DESTROYED,
}

/**
 * Provider-neutral health signal for UI and field-test diagnostics.
 * `errorCode` is provider-safe metadata only; credentials and raw provider exceptions never cross this boundary.
 */
data class MapHealth(
    val status: MapHealthStatus,
    val errorCode: String? = null,
    val userMessage: String? = null,
)

data class MapMarkerSpec(
    val id: String,
    val title: String,
    val position: GeoPoint,
)

data class UserLocationSpec(
    val position: GeoPoint,
    val bearingDegrees: Float? = null,
)

/** Provider-neutral contract consumed by UI/application code. */
interface MapViewAdapter {
    val providerId: MapProviderId
    val health: StateFlow<MapHealth>
    fun createView(context: Context): View
    fun setCamera(target: GeoPoint, zoom: Double = 16.0)
    fun setMarkers(markers: List<MapMarkerSpec>)
    fun setUserLocation(location: UserLocationSpec?)
    fun onStart() = Unit
    fun onResume() = Unit
    fun onPause() = Unit
    fun onStop() = Unit
    fun onDestroy() = Unit
    fun onLowMemory() = Unit
}
