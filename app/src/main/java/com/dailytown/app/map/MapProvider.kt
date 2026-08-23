package com.dailytown.app.map

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint

enum class MapProviderId { NAVER, GOOGLE }

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
