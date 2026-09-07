package com.dailytown.app.map

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint

private val LEGACY_DEMO_MARKER_IDS = setOf("cityhall-echo", "stone-trace", "hidden-note")
private const val ACTIVE_MARKER_OVERLAP_METERS = 12.0
private const val BASE_POI_DEDUPE_METERS = 10.0

/**
 * Provider-neutral POI overlay decorator used by both production exploration and physical QA.
 *
 * `nearbyPoiMarkers` are supplied by the active PoiRepository (TourAPI in configured builds,
 * fixture fallback otherwise). Optional field-test fixtures can be enabled for a dedicated QA
 * build so known Seoul/Jungwon anchors remain visible without becoming production content.
 *
 * The old hard-coded demo mystery markers are intentionally filtered from map output. The
 * encounter pipeline still owns its selected/active marker and always wins marker precedence.
 *
 * Camera behavior follows the first fresh location of a tracking session only. Subsequent location
 * updates refresh the user-location overlay without stealing manual map panning.
 */
class FixturePoiOverlayMapAdapter(
    private val delegate: MapViewAdapter,
    fixtureMarkers: List<MapMarkerSpec>,
    private val showFixtureMarkers: Boolean = true,
    private val suppressLegacyDemoMarkers: Boolean = true,
    private val distance: ExplorationEngine = ExplorationEngine(),
) : MapViewAdapter by delegate {
    private var fixtureMarkers: List<MapMarkerSpec> = fixtureMarkers
    private var nearbyPoiMarkers: List<MapMarkerSpec> = emptyList()
    private var runtimeMarkers: List<MapMarkerSpec> = emptyList()
    private var cameraFollowArmed: Boolean = true

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

    private fun renderMergedMarkers() {
        delegate.setMarkers(mergeMarkers())
    }

    private fun mergeMarkers(): List<MapMarkerSpec> {
        val gameplayMarkers = if (suppressLegacyDemoMarkers) {
            runtimeMarkers.filterNot { it.id in LEGACY_DEMO_MARKER_IDS }
        } else {
            runtimeMarkers
        }
        val activePositions = gameplayMarkers.filter { it.selected || it.id.startsWith("active-") }
            .map { it.position }

        // Production/nearby markers win over fixtures. This lets a debug field-test build show both
        // sources without rendering two markers for Seoul City Hall or another shared anchor.
        val baseMarkers = buildList {
            addAll(nearbyPoiMarkers)
            if (showFixtureMarkers) addAll(fixtureMarkers)
        }.fold(mutableListOf<MapMarkerSpec>()) { accepted, candidate ->
            val duplicatesExisting = accepted.any { existing ->
                existing.title == candidate.title ||
                    distance.distanceMeters(existing.position, candidate.position) <= BASE_POI_DEDUPE_METERS
            }
            if (!duplicatesExisting) accepted.add(candidate)
            accepted
        }.filterNot { marker ->
            activePositions.any { active ->
                distance.distanceMeters(marker.position, active) <= ACTIVE_MARKER_OVERLAP_METERS
            }
        }

        val gameplayIds = gameplayMarkers.mapTo(mutableSetOf()) { it.id }
        return buildList {
            addAll(baseMarkers.filterNot { it.id in gameplayIds })
            addAll(gameplayMarkers)
        }
    }
}
