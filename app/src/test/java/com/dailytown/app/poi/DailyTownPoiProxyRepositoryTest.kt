package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTownPoiProxyRepositoryTest {
    @Test
    fun `gateway query uses snapped center padded radius and no provider credential`() {
        val url = buildDailyTownPoiProxyUrl(
            baseUrl = "https://poi.dailytown.example/",
            center = GeoPoint(37.57593, 126.97682),
            radiusMeters = 900.0,
        )

        assertTrue(url.startsWith("https://poi.dailytown.example/v1/pois/nearby?"))
        assertTrue(url.contains("latitude=37.58"))
        assertTrue(url.contains("longitude=126.98"))
        assertTrue(url.contains("radiusMeters=2500"))
        assertTrue(url.contains("schemaVersion=1"))
        assertFalse(url.contains("serviceKey", ignoreCase = true))
        assertFalse(url.contains("37.57593"))
        assertFalse(url.contains("126.97682"))
    }

    @Test
    fun `gateway parser keeps normalized valid POIs and drops malformed records`() {
        val payload = """
            {
              "schemaVersion": 1,
              "pois": [
                {
                  "id": "tourapi:1001",
                  "name": "서울광장",
                  "latitude": 37.5656,
                  "longitude": 126.97798,
                  "districtKey": "tourapi:1:24",
                  "category": "PUBLIC_SPACE"
                },
                {
                  "id": "tourapi:1001",
                  "name": "중복 서울광장",
                  "latitude": 37.5656,
                  "longitude": 126.97798,
                  "category": "OTHER"
                },
                {
                  "id": "broken",
                  "name": "잘못된 좌표",
                  "latitude": 137.0,
                  "longitude": 126.9
                }
              ]
            }
        """.trimIndent()

        val pois = parseDailyTownPoiProxyResponse(payload)

        assertEquals(1, pois.size)
        assertEquals("tourapi:1001", pois.single().id)
        assertEquals("서울광장", pois.single().name)
        assertEquals(PoiCategory.PUBLIC_SPACE, pois.single().category)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `gateway parser rejects unsupported schema`() {
        parseDailyTownPoiProxyResponse("{\"schemaVersion\":2,\"pois\":[]}")
    }
}
