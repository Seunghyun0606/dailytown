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

    @Test
    fun `direct provider credential is ignored when caller disables direct provider`() = runBlocking {
        val repository = ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = "unit-test-key",
            allowDirectProvider = false,
            allowFixtureFallback = false,
        )

        assertTrue(repository.nearby(seoul, 900.0).isEmpty())
        assertTrue(repository.sourceMetadata().isEmpty())
    }

    @Test
    fun `https app owned gateway becomes canonical without provider credential`() {
        val repository = ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = null,
            proxyBaseUrl = "https://poi.dailytown.example",
            allowDirectProvider = false,
            allowFixtureFallback = false,
        )

        assertTrue(repository.sourceMetadata().any { it.id == "dailytown-poi-gateway" })
        assertTrue(repository.sourceMetadata().none { it.role == PoiSourceRole.FIXTURE })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `gateway rejects non https endpoint`() {
        ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = null,
            proxyBaseUrl = "http://poi.dailytown.example",
            allowDirectProvider = false,
            allowFixtureFallback = false,
        )
    }
}
