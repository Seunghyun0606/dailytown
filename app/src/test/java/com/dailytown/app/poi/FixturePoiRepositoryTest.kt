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
}
