package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertEquals
import org.junit.Test

class NearbyPoiMarkerPublisherTest {
    private val center = GeoPoint(37.5665, 126.9780)

    @Test
    fun `publishes nearest bounded POI markers with semantic mapping`() {
        val pois = listOf(
            Poi("far", "far", GeoPoint(37.5700, 126.9780), "test", PoiCategory.CULTURE),
            Poi("near", "near", GeoPoint(37.5666, 126.9780), "test", PoiCategory.PARK),
            Poi("mid", "mid", GeoPoint(37.5680, 126.9780), "test", PoiCategory.LANDMARK),
        )
        var published = emptyList<com.dailytown.app.map.MapMarkerSpec>()
        val publisher = NearbyPoiMarkerPublisher(
            publish = { published = it },
            maxPublishedMarkers = 2,
        )

        publisher.publish(center, pois)

        assertEquals(listOf("poi:near", "poi:mid"), published.map { it.id })
        assertEquals(MarkerSemantic.POI_PARK, published[0].semantic)
        assertEquals(MarkerSemantic.POI_LANDMARK, published[1].semantic)
    }

    @Test
    fun `does not republish stable markers for tiny GPS movement`() {
        val poi = Poi("one", "one", GeoPoint(37.5666, 126.9780), "test", PoiCategory.OTHER)
        var publishCount = 0
        val publisher = NearbyPoiMarkerPublisher(
            publish = { publishCount++ },
            minPublishMovementMeters = 60.0,
        )

        publisher.publish(center, listOf(poi))
        publisher.publish(GeoPoint(37.56655, 126.9780), listOf(poi))

        assertEquals(1, publishCount)
    }

    @Test
    fun `republishes when POI identity changes even without movement`() {
        var publishCount = 0
        val publisher = NearbyPoiMarkerPublisher(
            publish = { publishCount++ },
            minPublishMovementMeters = 500.0,
        )

        publisher.publish(
            center,
            listOf(Poi("poi-1", "poi", GeoPoint(37.5666, 126.9780), "test", PoiCategory.OTHER)),
        )
        publisher.publish(
            center,
            listOf(Poi("poi-2", "poi", GeoPoint(37.5666, 126.9780), "test", PoiCategory.OTHER)),
        )

        assertEquals(2, publishCount)
    }
}
