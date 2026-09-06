package com.dailytown.app.poi

/**
 * Runtime POI composition for the adopted production direction:
 * - TourAPI is the canonical source when configured.
 * - Optional live enrichment providers can be injected later without replacing canonical records.
 * - Field-test fixtures remain the development fallback when no TourAPI credential is supplied.
 */
object ProductionPoiRepositoryFactory {
    fun create(
        tourApiServiceKey: String?,
        enrichments: List<PoiRepository> = emptyList(),
    ): PoiRepository {
        val key = tourApiServiceKey.orEmpty().trim()
        if (key.isBlank() || key.startsWith("TODO_")) {
            return CachingPoiRepository(FixturePoiRepository())
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
