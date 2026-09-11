package com.dailytown.app.poi

import com.dailytown.app.domain.GeoDistance
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.domain.HaversineGeoDistance
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.visual.MarkerSemantic

/**
 * Projects a nearby POI snapshot into the bounded marker layer consumed by the map.
 *
 * This class deliberately has no data-source responsibility. It only throttles presentation updates
 * by movement and POI identity so high-frequency accepted location samples do not churn map markers.
 */
class NearbyPoiMarkerPublisher(
    private val publish: (List<MapMarkerSpec>) -> Unit,
    private val minPublishMovementMeters: Double = 60.0,
    private val maxPublishedMarkers: Int = 24,
    private val distance: GeoDistance = HaversineGeoDistance,
) {
    private var lastPublishedCenter: GeoPoint? = null
    private var lastPublishedIds: List<String> = emptyList()

    fun publish(center: GeoPoint, pois: List<Poi>) {
        val visible = selectVisiblePoiMarkers(
            center = center,
            pois = pois,
            maxMarkers = maxPublishedMarkers,
            distance = distance,
        )
        val ids = visible.map { it.id }
        val movedEnough = lastPublishedCenter?.let {
            distance.meters(it, center) >= minPublishMovementMeters
        } ?: true
        if (movedEnough || ids != lastPublishedIds) {
            publish(visible)
            lastPublishedCenter = center
            lastPublishedIds = ids
        }
    }
}

internal fun selectVisiblePoiMarkers(
    center: GeoPoint,
    pois: List<Poi>,
    maxMarkers: Int = 24,
    distance: GeoDistance = HaversineGeoDistance,
): List<MapMarkerSpec> = pois
    .distinctBy { it.id }
    .sortedBy { distance.meters(center, it.position) }
    .take(maxMarkers.coerceAtLeast(0))
    .map(Poi::toMapMarkerSpec)

internal fun Poi.toMapMarkerSpec(): MapMarkerSpec = MapMarkerSpec(
    id = "poi:$id",
    title = name,
    position = position,
    semantic = when (category) {
        PoiCategory.PARK -> MarkerSemantic.POI_PARK
        PoiCategory.CULTURE -> MarkerSemantic.POI_CULTURE
        PoiCategory.LANDMARK -> MarkerSemantic.POI_LANDMARK
        PoiCategory.STREET, PoiCategory.PUBLIC_SPACE -> MarkerSemantic.POI_DAILY_LIFE
        PoiCategory.OTHER -> MarkerSemantic.POI_OTHER
    },
)
