package com.dailytown.app.poi

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.visual.MarkerSemantic

/**
 * Publishes a bounded, distance-sorted view of nearby POIs for the map while preserving the full
 * repository result for encounter selection. Publishing is intentionally throttled by walking
 * distance and POI identity so high-frequency GPS samples do not churn map markers.
 */
class MapPublishingPoiRepository(
    private val delegate: PoiRepository,
    private val publish: (List<MapMarkerSpec>) -> Unit,
    private val minPublishMovementMeters: Double = 60.0,
    private val maxPublishedMarkers: Int = 24,
    private val distance: ExplorationEngine = ExplorationEngine(),
) : PoiRepository {
    private var lastPublishedCenter: GeoPoint? = null
    private var lastPublishedIds: List<String> = emptyList()

    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
        val pois = delegate.nearby(center, radiusMeters)
        val visible = selectVisiblePoiMarkers(
            center = center,
            pois = pois,
            maxMarkers = maxPublishedMarkers,
            distance = distance,
        )
        val ids = visible.map { it.id }
        val movedEnough = lastPublishedCenter?.let {
            distance.distanceMeters(it, center) >= minPublishMovementMeters
        } ?: true
        if (movedEnough || ids != lastPublishedIds) {
            publish(visible)
            lastPublishedCenter = center
            lastPublishedIds = ids
        }
        return pois
    }

    override fun sourceMetadata(): List<PoiSourceMetadata> = delegate.sourceMetadata()
}

internal fun selectVisiblePoiMarkers(
    center: GeoPoint,
    pois: List<Poi>,
    maxMarkers: Int = 24,
    distance: ExplorationEngine = ExplorationEngine(),
): List<MapMarkerSpec> = pois
    .distinctBy { it.id }
    .sortedBy { distance.distanceMeters(center, it.position) }
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
