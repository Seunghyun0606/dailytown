package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CachingPoiRepositoryTest {
    private val center = GeoPoint(37.56650, 126.97800)
    private val nearby = Poi(
        id = "nearby",
        name = "nearby",
        position = GeoPoint(37.56660, 126.97800),
        districtKey = "test",
        category = PoiCategory.LANDMARK,
    )
    private val edge = Poi(
        id = "edge",
        name = "edge",
        position = GeoPoint(37.56800, 126.97800),
        districtKey = "test",
        category = PoiCategory.PUBLIC_SPACE,
    )

    @Test
    fun `nearby query reuses padded fresh cache and filters requested radius`() = runBlocking {
        var calls = 0
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                calls++
                return listOf(nearby, edge)
            }
        }
        val cache = CachingPoiRepository(delegate = delegate)

        val first = cache.nearby(center, 100.0)
        val shifted = cache.nearby(GeoPoint(37.56655, 126.97800), 100.0)

        assertEquals(1, calls)
        assertEquals(listOf("nearby"), first.map { it.id })
        assertEquals(listOf("nearby"), shifted.map { it.id })
    }

    @Test
    fun `expired fresh entry refreshes upstream`() = runBlocking {
        var now = 1_000L
        var calls = 0
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                calls++
                return listOf(nearby)
            }
        }
        val cache = CachingPoiRepository(
            delegate = delegate,
            clockMillis = { now },
            freshTtlMillis = 100L,
            staleFallbackMillis = 1_000L,
        )

        cache.nearby(center, 100.0)
        now += 101L
        cache.nearby(center, 100.0)

        assertEquals(2, calls)
    }

    @Test
    fun `upstream failure uses recent stale covering entry`() = runBlocking {
        var now = 1_000L
        var shouldFail = false
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                if (shouldFail) error("network down")
                return listOf(nearby)
            }
        }
        val cache = CachingPoiRepository(
            delegate = delegate,
            clockMillis = { now },
            freshTtlMillis = 100L,
            staleFallbackMillis = 1_000L,
        )

        cache.nearby(center, 100.0)
        now += 200L
        shouldFail = true

        assertEquals(listOf("nearby"), cache.nearby(center, 100.0).map { it.id })
    }

    @Test
    fun `upstream failure is rethrown when stale entry is too old`() {
        var now = 1_000L
        var shouldFail = false
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                if (shouldFail) error("network down")
                return listOf(nearby)
            }
        }
        val cache = CachingPoiRepository(
            delegate = delegate,
            clockMillis = { now },
            freshTtlMillis = 100L,
            staleFallbackMillis = 300L,
        )

        runBlocking { cache.nearby(center, 100.0) }
        now += 301L
        shouldFail = true

        assertThrows(IllegalStateException::class.java) {
            runBlocking { cache.nearby(center, 100.0) }
        }
    }
}
