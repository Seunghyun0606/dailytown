package com.dailytown.app.map

import com.dailytown.app.domain.GeoDistance
import com.dailytown.app.domain.HaversineGeoDistance

private val LEGACY_DEMO_MARKER_IDS = setOf("cityhall-echo", "stone-trace", "hidden-note")
private const val ACTIVE_MARKER_OVERLAP_METERS = 12.0
private const val BASE_POI_DEDUPE_METERS = 10.0

/**
 * Pure provider-neutral marker layer composition.
 *
 * Production nearby POIs win over optional debug fixtures, while gameplay markers always win over
 * base POI layers. Selected/active gameplay markers suppress an underlying POI marker within the
 * overlap threshold so the map never renders two competing interaction anchors in the same spot.
 */
internal class MapMarkerLayerComposer(
    private val showFixtureMarkers: Boolean = true,
    private val suppressLegacyDemoMarkers: Boolean = true,
    private val distance: GeoDistance = HaversineGeoDistance,
) {
    fun compose(
        runtimeMarkers: List<MapMarkerSpec>,
        nearbyPoiMarkers: List<MapMarkerSpec>,
        fixtureMarkers: List<MapMarkerSpec>,
    ): List<MapMarkerSpec> {
        val gameplayMarkers = if (suppressLegacyDemoMarkers) {
            runtimeMarkers.filterNot { it.id in LEGACY_DEMO_MARKER_IDS }
        } else {
            runtimeMarkers
        }
        val activePositions = gameplayMarkers
            .filter { it.selected || it.id.startsWith("active-") }
            .map { it.position }

        val baseMarkers = buildList {
            addAll(nearbyPoiMarkers)
            if (showFixtureMarkers) addAll(fixtureMarkers)
        }.fold(mutableListOf<MapMarkerSpec>()) { accepted, candidate ->
            val duplicatesExisting = accepted.any { existing ->
                existing.title == candidate.title ||
                    distance.meters(existing.position, candidate.position) <= BASE_POI_DEDUPE_METERS
            }
            if (!duplicatesExisting) accepted.add(candidate)
            accepted
        }.filterNot { marker ->
            activePositions.any { active ->
                distance.meters(marker.position, active) <= ACTIVE_MARKER_OVERLAP_METERS
            }
        }

        val gameplayIds = gameplayMarkers.mapTo(mutableSetOf()) { it.id }
        return buildList {
            addAll(baseMarkers.filterNot { it.id in gameplayIds })
            addAll(gameplayMarkers)
        }
    }
}
