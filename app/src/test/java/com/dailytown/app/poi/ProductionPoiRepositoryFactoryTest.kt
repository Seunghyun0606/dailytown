package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionPoiRepositoryFactoryTest {
    private val seoul = GeoPoint(37.5665, 126.9780)

    @Test
    fun `missing credential can use fixture only when explicitly allowed`() = runBlocking {
        val repository = ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = null,
            allowFixtureFallback = true,
        )

        assertTrue(repository.nearby(seoul, 900.0).isNotEmpty())
        assertTrue(repository.sourceMetadata().any { it.role == PoiSourceRole.FIXTURE })
    }

    @Test
    fun `missing credential fails closed when fixture fallback is disabled`() = runBlocking {
        val repository = ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = null,
            allowFixtureFallback = false,
        )

        assertTrue(repository.nearby(seoul, 900.0).isEmpty())
        assertTrue(repository.sourceMetadata().isEmpty())
    }
}
