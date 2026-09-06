package com.dailytown.app.map

import com.dailytown.app.domain.GeoPoint

/**
 * Development field-test decorator that keeps fixture POI markers visible regardless of the
 * short-lived gameplay marker set emitted by DailyTownApp.
 *
 * It follows the first camera request of a location session only. Subsequent location updates keep
 * the user-location overlay fresh without forcing the camera back after the tester manually pans
 * the map. An explicit recenter moves immediately and arms one fresh follow for the next tracking
 * session; setUserLocation(null) also re-arms that first-camera follow.
 */
class FixturePoiOverlayMapAdapter(
    private val delegate: MapViewAdapter,
    fixtureMarkers: List<MapMarkerSpec>,
) : MapViewAdapter by delegate {
    private var fixtureMarkers: List<MapMarkerSpec> = fixtureMarkers
    private var runtimeMarkers: List<MapMarkerSpec> = emptyList()
    private var cameraFollowArmed: Boolean = true

    override fun setMarkers(markers: List<MapMarkerSpec>) {
        runtimeMarkers = markers
        delegate.setMarkers(mergeMarkers())
    }

    fun setFixtureMarkers(markers: List<MapMarkerSpec>) {
        fixtureMarkers = markers
        delegate.setMarkers(mergeMarkers())
    }

    override fun setCamera(target: GeoPoint, zoom: Double) {
        if (!cameraFollowArmed) return
        cameraFollowArmed = false
        delegate.setCamera(target, zoom)
    }

    fun recenter(target: GeoPoint, zoom: Double = 16.0) {
        delegate.setCamera(target, zoom)
        // Initial one-shot centering is not a tracking session. Keep one camera follow available so
        // pressing "실제 위치" or starting replay can center on its first fresh sample exactly once.
        cameraFollowArmed = true
    }

    override fun setUserLocation(location: UserLocationSpec?) {
        if (location == null) {
            cameraFollowArmed = true
        }
        delegate.setUserLocation(location)
    }

    private fun mergeMarkers(): List<MapMarkerSpec> = buildList {
        val runtimeIds = runtimeMarkers.mapTo(mutableSetOf()) { it.id }
        addAll(fixtureMarkers.filterNot { it.id in runtimeIds })
        addAll(runtimeMarkers)
    }
}
