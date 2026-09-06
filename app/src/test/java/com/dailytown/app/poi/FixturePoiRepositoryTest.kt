package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FixturePoiRepositoryTest {
    @Test
    fun filtersPoisByRadius() = runBlocking {
        val repo = FixturePoiRepository()
        val nearby = repo.nearby(GeoPoint(37.56650, 126.97800), 120.0)
        assertEquals(setOf("seoul-city-hall", "seoul-plaza"), nearby.map { it.id }.toSet())
    }

    @Test
    fun expandedSeoulFieldTestFixturesAreAvailable() {
        val ids = defaultFixturePois().map { it.id }.toSet()

        assertEquals(
            setOf(
                "euljiro-1ga-station",
                "sk-seorin-building",
                "gwanghwamun-gate",
                "insadong",
            ),
            ids.intersect(
                setOf(
                    "euljiro-1ga-station",
                    "sk-seorin-building",
                    "gwanghwamun-gate",
                    "insadong",
                ),
            ),
        )
    }

    @Test
    fun expandedSeoulFieldTestFixturesAreDiscoverableAtTheirAnchors() = runBlocking {
        val repo = FixturePoiRepository()
        val anchors = mapOf(
            "euljiro-1ga-station" to GeoPoint(37.566110, 126.982500),
            "sk-seorin-building" to GeoPoint(37.5696896560345, 126.98030408661),
            "gwanghwamun-gate" to GeoPoint(37.575930, 126.976820),
            "insadong" to GeoPoint(37.5729518, 126.9865200),
        )

        anchors.forEach { (id, center) ->
            val nearbyIds = repo.nearby(center, 60.0).map { it.id }.toSet()
            assertEquals(true, id in nearbyIds)
        }
    }

    @Test
    fun seongnamFieldTestFixturesAreReachableInOneNearbyQuery() = runBlocking {
        val repo = FixturePoiRepository()
        val nearby = repo.nearby(GeoPoint(37.43450117, 127.137906833), 900.0)

        assertEquals(
            setOf("seongnam-sports-complex", "starbucks-seongnam-moran-dt"),
            nearby.map { it.id }.toSet(),
        )
    }
}
