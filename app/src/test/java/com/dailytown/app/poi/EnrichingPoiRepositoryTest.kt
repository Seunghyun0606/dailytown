package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EnrichingPoiRepositoryTest {
    private val center = GeoPoint(37.5665, 126.9780)

    @Test
    fun `canonical POI wins equivalent live enrichment`() = runBlocking {
        val canonical = repositoryOf(
            Poi("canonical-cityhall", "서울시청", center, "jung-gu", PoiCategory.LANDMARK),
            metadata = PoiSourceMetadata(
                id = "canonical",
                displayName = "canonical",
                role = PoiSourceRole.CANONICAL,
                attributionText = "canonical",
                licenseSummary = "canonical",
            ),
        )
        val enrichment = repositoryOf(
            Poi(
                "live-cityhall",
                "서울 시청",
                GeoPoint(37.56651, 126.97801),
                "live",
                PoiCategory.OTHER,
            ),
            metadata = PoiSourceMetadata(
                id = "live",
                displayName = "live",
                role = PoiSourceRole.ENRICHMENT,
                attributionText = "live",
                licenseSummary = "live",
            ),
        )

        val result = EnrichingPoiRepository(canonical, listOf(enrichment)).nearby(center, 900.0)

        assertEquals(listOf("canonical-cityhall"), result.map { it.id })
    }

    @Test
    fun `distinct enrichment fills canonical coverage gap and exposes both sources`() = runBlocking {
        val canonical = repositoryOf(
            Poi("canonical-cityhall", "서울시청", center, "jung-gu", PoiCategory.LANDMARK),
            metadata = source("canonical", PoiSourceRole.CANONICAL),
        )
        val enrichment = repositoryOf(
            Poi(
                "live-cafe",
                "테스트 카페",
                GeoPoint(37.5670, 126.9790),
                "jung-gu",
                PoiCategory.OTHER,
            ),
            metadata = source("live", PoiSourceRole.ENRICHMENT),
        )
        val repository = EnrichingPoiRepository(canonical, listOf(enrichment))

        val result = repository.nearby(center, 900.0)

        assertEquals(setOf("canonical-cityhall", "live-cafe"), result.map { it.id }.toSet())
        assertEquals(setOf("canonical", "live"), repository.sourceMetadata().map { it.id }.toSet())
    }

    @Test
    fun `optional enrichment failure degrades without replacing canonical results`() = runBlocking {
        val canonical = repositoryOf(
            Poi("canonical-cityhall", "서울시청", center, "jung-gu", PoiCategory.LANDMARK),
            metadata = source("canonical", PoiSourceRole.CANONICAL),
        )
        val failingEnrichment = failingRepository(IllegalStateException("provider down"))

        val result = EnrichingPoiRepository(canonical, listOf(failingEnrichment)).nearby(center, 900.0)

        assertEquals(listOf("canonical-cityhall"), result.map { it.id })
    }

    @Test
    fun `enrichment cancellation propagates instead of degrading to canonical only`() {
        val canonical = repositoryOf(
            Poi("canonical-cityhall", "서울시청", center, "jung-gu", PoiCategory.LANDMARK),
            metadata = source("canonical", PoiSourceRole.CANONICAL),
        )
        val cancelledEnrichment = failingRepository(CancellationException("request cancelled"))
        val repository = EnrichingPoiRepository(canonical, listOf(cancelledEnrichment))

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.nearby(center, 900.0) }
        }
    }

    @Test
    fun `degrading repository keeps ordinary failure fallback`() = runBlocking {
        val repository = DegradingPoiRepository(failingRepository(IllegalStateException("provider down")))

        assertEquals(emptyList<Poi>(), repository.nearby(center, 900.0))
    }

    @Test
    fun `degrading repository propagates cancellation`() {
        val repository = DegradingPoiRepository(failingRepository(CancellationException("request cancelled")))

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.nearby(center, 900.0) }
        }
    }

    private fun repositoryOf(
        poi: Poi,
        metadata: PoiSourceMetadata,
    ): PoiRepository = object : PoiRepository {
        override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = listOf(poi)
        override fun sourceMetadata(): List<PoiSourceMetadata> = listOf(metadata)
    }

    private fun failingRepository(error: RuntimeException): PoiRepository = object : PoiRepository {
        override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = throw error
    }

    private fun source(id: String, role: PoiSourceRole) = PoiSourceMetadata(
        id = id,
        displayName = id,
        role = role,
        attributionText = id,
        licenseSummary = id,
    )
}
