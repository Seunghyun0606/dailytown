package com.dailytown.app.poi

/**
 * Runtime POI composition for the adopted production direction:
 * - TourAPI is the canonical source when configured.
 * - Optional live enrichment providers can be injected later without replacing canonical records.
 * - Field-test fixtures are permitted only when the caller explicitly allows development fallback.
 */
object ProductionPoiRepositoryFactory {
    fun create(
        tourApiServiceKey: String?,
        enrichments: List<PoiRepository> = emptyList(),
        allowFixtureFallback: Boolean = true,
    ): PoiRepository {
        val key = tourApiServiceKey.orEmpty().trim()
        if (key.isBlank() || key.startsWith("TODO_")) {
            return if (allowFixtureFallback) {
                CachingPoiRepository(FixturePoiRepository())
            } else {
                EmptyPoiRepository
            }
        }

        val canonical = TourApiPoiRepository(
            HttpTourApiNearbySource(serviceKey = key),
        )
        val merged = EnrichingPoiRepository(
            canonical = canonical,
            enrichments = enrichments,
        )
        // Caching gets first chance to serve a stale-but-covering entry during brief outages;
        // only a first-fetch/no-cache failure degrades to an empty candidate set.
        return DegradingPoiRepository(CachingPoiRepository(merged))
    }
}

/** Release-safe no-content fallback. Production builds must never silently ship fixture POIs. */
private object EmptyPoiRepository : PoiRepository {
    override suspend fun nearby(
        center: com.dailytown.app.domain.GeoPoint,
        radiusMeters: Double,
    ): List<Poi> = emptyList()
}
