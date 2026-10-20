package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NearbyPoiCoordinatorTest {
    private val center = GeoPoint(37.5665, 126.9780)

    @Test
    fun `publishes latest successful nearby snapshot`() = runBlocking {
        val pois = listOf(
            Poi("one", "one", GeoPoint(37.5666, 126.9780), "test", PoiCategory.PARK),
        )
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = pois
        }
        val coordinator = NearbyPoiCoordinator(delegate)

        val returned = coordinator.nearby(center, 900.0)
        val snapshot = coordinator.snapshots.value

        assertEquals(pois, returned)
        assertEquals(center, snapshot?.center)
        assertEquals(900.0, snapshot?.radiusMeters ?: 0.0, 0.0)
        assertEquals(pois, snapshot?.pois)
    }

    @Test
    fun `serializes concurrent callers before entering mutable delegate`() = runBlocking {
        val activeCalls = AtomicInteger(0)
        val overlapped = AtomicBoolean(false)
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                if (activeCalls.incrementAndGet() > 1) overlapped.set(true)
                try {
                    delay(25)
                    return listOf(
                        Poi(
                            id = "${center.latitude}:${center.longitude}",
                            name = "poi",
                            position = center,
                            districtKey = "test",
                            category = PoiCategory.OTHER,
                        ),
                    )
                } finally {
                    activeCalls.decrementAndGet()
                }
            }
        }
        val coordinator = NearbyPoiCoordinator(delegate)

        val first = async { coordinator.nearby(center, 900.0) }
        val second = async { coordinator.nearby(GeoPoint(37.5670, 126.9780), 900.0) }
        first.await()
        second.await()

        assertFalse(overlapped.get())
        assertEquals(0, activeCalls.get())
        assertNotNull(coordinator.snapshots.value)
    }

    @Test
    fun `coalesces identical request that completed while caller waited`() = runBlocking {
        val delegateCalls = AtomicInteger(0)
        val pois = listOf(Poi("one", "one", center, "test", PoiCategory.OTHER))
        val delegate = object : PoiRepository {
            override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
                delegateCalls.incrementAndGet()
                delay(25)
                return pois
            }
        }
        val coordinator = NearbyPoiCoordinator(delegate)

        val first = async { coordinator.nearby(center, 900.0) }
        val second = async { coordinator.nearby(center, 900.0) }

        assertEquals(pois, first.await())
        assertEquals(pois, second.await())
        assertEquals(1, delegateCalls.get())
    }
}
