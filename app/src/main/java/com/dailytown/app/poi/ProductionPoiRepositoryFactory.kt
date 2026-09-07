package com.dailytown.app.poi

/**
 * Runtime POI composition for the adopted production direction:
 * - Release prefers the app-owned HTTPS POI gateway, keeping upstream credentials off-device.
 * - Internal/debug may call TourAPI directly when a service key is configured.
 * - Optional live enrichment providers can be injected without replacing canonical records.
 * - Field-test fixtures are permitted only when the caller explicitly allows development fallback.
 */
object ProductionPoiRepositoryFactory {
    fun create(
        tourApiServiceKey: String?,
        proxyBaseUrl: String? = null,
        enrichments: List<PoiRepository> = emptyList(),
        allowFixtureFallback: Boolean = true,
    ): PoiRepository {
        val proxyUrl = proxyBaseUrl.orEmpty().trim().trimEnd('/')
        val directKey = tourApiServiceKey.orEmpty().trim()

        val canonical: PoiRepository = when {
            proxyUrl.isNotBlank() -> {
                require(proxyUrl.startsWith("https://")) {
                    "Daily Town POI gateway must use HTTPS."
                }
                DailyTownPoiProxyRepository(HttpDailyTownPoiProxySource(proxyUrl))
            }
            directKey.isNotBlank() && !directKey.startsWith("TODO_") -> {
                TourApiPoiRepository(HttpTourApiNearbySource(serviceKey = directKey))
            }
            allowFixtureFallback -> FixturePoiRepository()
            else -> EmptyPoiRepository
        }

        if (canonical === EmptyPoiRepository) return canonical
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
