package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TourApiPoiRepositoryTest {
    @Test
    fun `TourAPI record maps to provider neutral POI`() = runBlocking {
        val source = TourApiNearbySource { _, _ ->
            listOf(
                TourApiNearbyItem(
                    contentId = "12345",
                    title = "광화문",
                    longitude = 126.97682,
                    latitude = 37.57593,
                    areaCode = "1",
                    sigunguCode = "23",
                    contentTypeId = "12",
                ),
            )
        }
        val repository = TourApiPoiRepository(source)

        val result = repository.nearby(GeoPoint(37.57593, 126.97682), 900.0)

        assertEquals(1, result.size)
        assertEquals("tourapi:12345", result.single().id)
        assertEquals("광화문", result.single().name)
        assertEquals("tourapi:1:23", result.single().districtKey)
        assertEquals(PoiCategory.LANDMARK, result.single().category)
        assertEquals(PoiSourceRole.CANONICAL, repository.sourceMetadata().single().role)
    }

    @Test
    fun `TourAPI request snaps raw GPS to neighborhood grid and pads provider radius`() {
        val url = buildTourApiLocationUrl(
            baseUrl = "https://example.test/KorService2",
            serviceKey = "decoded key/+",
            mobileApp = "DailyTown",
            mobileOs = "AND",
            center = GeoPoint(37.57593, 126.97682),
            radiusMeters = 900.0,
        )
        val query = url.substringAfter('?')
            .split('&')
            .associate { pair ->
                val (rawKey, rawValue) = pair.split('=', limit = 2)
                decode(rawKey) to decode(rawValue)
            }

        assertTrue(url.startsWith("https://example.test/KorService2/locationBasedList2?"))
        assertEquals("decoded key/+", query["serviceKey"])
        assertEquals("126.98", query["mapX"])
        assertEquals("37.58", query["mapY"])
        assertEquals("2500", query["radius"])
        assertEquals("json", query["_type"])
    }

    @Test
    fun `TourAPI provider radius still respects official maximum`() {
        val url = buildTourApiLocationUrl(
            baseUrl = "https://example.test/KorService2",
            serviceKey = "key",
            mobileApp = "DailyTown",
            mobileOs = "AND",
            center = GeoPoint(37.57593, 126.97682),
            radiusMeters = 50_000.0,
        )
        val query = url.substringAfter('?')
            .split('&')
            .associate { pair ->
                val (rawKey, rawValue) = pair.split('=', limit = 2)
                decode(rawKey) to decode(rawValue)
            }

        assertEquals("20000", query["radius"])
    }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
