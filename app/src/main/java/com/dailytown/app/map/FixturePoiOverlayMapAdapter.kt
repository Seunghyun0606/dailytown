package com.dailytown.app.map

import com.dailytown.app.domain.GeoDistance
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.domain.HaversineGeoDistance

/**
 * Provider-neutral POI overlay decorator used by both production exploration and physical QA.
 *
 * Marker precedence/dedupe rules are delegated to [MapMarkerLayerComposer]. This adapter owns only
 * mutable map-facing layer inputs, first-fix camera-follow behavior, user-location forwarding and the
 * provider delegate lifecycle surface.
 */
class FixturePoiOverlayMapAdapter(
    private val delegate: MapViewAdapter,
    fixtureMarkers: List<MapMarkerSpec>,
    showFixtureMarkers: Boolean = true,
    suppressLegacyDemoMarkers: Boolean = true,
    distance: GeoDistance = HaversineGeoDistance,
) : MapViewAdapter by delegate {
    private val markerComposer = MapMarkerLayerComposer(
        showFixtureMarkers = showFixtureMarkers,
        suppressLegacyDemoMarkers = suppressLegacyDemoMarkers,
        distance = distance,
    )
    private var fixtureMarkers: List<MapMarkerSpec> = fixtureMarkers
    private var nearbyPoiMarkers: List<MapMarkerSpec> = emptyList()
    private var runtimeMarkers: List<MapMarkerSpec> = emptyList()
    private var cameraFollowArmed: Boolean = true
    private var userLocationListener: ((GeoPoint) -> Unit)? = null

    override fun setMarkers(markers: List<MapMarkerSpec>) {
        runtimeMarkers = markers
        renderMergedMarkers()
    }

    fun setFixtureMarkers(markers: List<MapMarkerSpec>) {
        fixtureMarkers = markers
        renderMergedMarkers()
    }

    fun setNearbyPoiMarkers(markers: List<MapMarkerSpec>) {
        nearbyPoiMarkers = markers
        renderMergedMarkers()
    }

    /**
     * Application-owned hook for refreshing the nearby POI feed from accepted location samples.
     * The callback receives only the in-memory point already used by gameplay; the map provider
     * itself does not own POI/network policy.
     */
    fun setUserLocationListener(listener: ((GeoPoint) -> Unit)?) {
        userLocationListener = listener
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
        } else {
            userLocationListener?.invoke(location.position)
        }
        delegate.setUserLocation(location)
    }

    private fun renderMergedMarkers() {
        delegate.setMarkers(
            markerComposer.compose(
                runtimeMarkers = runtimeMarkers,
                nearbyPoiMarkers = nearbyPoiMarkers,
                fixtureMarkers = fixtureMarkers,
            ),
        )
    }
}
