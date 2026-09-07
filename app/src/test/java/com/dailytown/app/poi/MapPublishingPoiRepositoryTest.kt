package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.visual.MarkerSemantic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MapPublishingPoiRepositoryTest {
    private val center = GeoPoint(37.5665, 126.9780)

    @Test
    fun `publishes nearest bounded POI markers with semantic mapping`() = runBlocking {
        val pois = listOf(
            Poi("far", "far", GeoPoint(37.5700, 126.9780), "test", PoiCategory.CULTURE),
            Poi("near", "near", GeoPoint(37.5666, 126.9780), "test", PoiCategory.PARK),
            Poi("mid", "mid", GeoPoint(37.5680, 126.9780), "test", PoiCategory.LANDMARK),
        )
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = pois
        }
        var published = emptyList<com.dailytown.app.map.MapMarkerSpec>()
        val repository = MapPublishingPoiRepository(
            delegate = delegate,
            publish = { published = it },
            maxPublishedMarkers = 2,
        )

        val returned = repository.nearby(center, 900.0)

        assertEquals(listOf("far", "near", "mid"), returned.map { it.id })
        assertEquals(listOf("poi:near", "poi:mid"), published.map { it.id })
        assertEquals(MarkerSemantic.POI_PARK, published[0].semantic)
        assertEquals(MarkerSemantic.POI_LANDMARK, published[1].semantic)
    }

    @Test
    fun `does not republish stable markers for tiny GPS movement`() = runBlocking {
        val poi = Poi("one", "one", GeoPoint(37.5666, 126.9780), "test", PoiCategory.OTHER)
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = listOf(poi)
        }
        var publishCount = 0
        val repository = MapPublishingPoiRepository(
            delegate = delegate,
            publish = { publishCount++ },
            minPublishMovementMeters = 60.0,
        )

        repository.nearby(center, 900.0)
        repository.nearby(GeoPoint(37.56655, 126.9780), 900.0)

        assertEquals(1, publishCount)
    }

    @Test
    fun `republishes when POI identity changes even without movement`() = runBlocking {
        var generation = 0
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                generation++
                return listOf(
                    Poi("poi-$generation", "poi", GeoPoint(37.5666, 126.9780), "test", PoiCategory.OTHER),
                )
            }
        }
        var publishCount = 0
        val repository = MapPublishingPoiRepository(
            delegate = delegate,
            publish = { publishCount++ },
            minPublishMovementMeters = 500.0,
        )

        repository.nearby(center, 900.0)
        repository.nearby(center, 900.0)

        assertEquals(2, publishCount)
    }
}
