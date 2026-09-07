package com.dailytown.app.poi

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint

enum class PoiCategory { PARK, CULTURE, LANDMARK, STREET, PUBLIC_SPACE, OTHER }

enum class PoiSourceRole { FIXTURE, CANONICAL, ENRICHMENT }

data class PoiSourceMetadata(
    val id: String,
    val displayName: String,
    val role: PoiSourceRole,
    val attributionText: String,
    val licenseSummary: String,
    val sourceUrl: String? = null,
)

data class Poi(
    val id: String,
    val name: String,
    val position: GeoPoint,
    val districtKey: String,
    val category: PoiCategory,
)

interface PoiRepository {
    suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi>

    /**
     * Human-readable source/licensing metadata for settings, review, and future attribution UI.
     * Credentials and provider exception payloads must never be exposed here.
     */
    fun sourceMetadata(): List<PoiSourceMetadata> = emptyList()
}

class FixturePoiRepository(
    private val items: List<Poi> = defaultFixturePois(),
    private val distance: ExplorationEngine = ExplorationEngine(),
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> =
        items.filter { distance.distanceMeters(center, it.position) <= radiusMeters }

    override fun sourceMetadata(): List<PoiSourceMetadata> = listOf(
        PoiSourceMetadata(
            id = "fixture",
            displayName = "Daily Town field-test fixture",
            role = PoiSourceRole.FIXTURE,
            attributionText = "개발/필드테스트용 고정 POI",
            licenseSummary = "Production 데이터 소스로 사용하지 않음",
        ),
    )
}

fun defaultFixturePois(): List<Poi> = listOf(
    Poi("seoul-city-hall", "서울시청", GeoPoint(37.56650, 126.97800), "jung-gu", PoiCategory.LANDMARK),
    Poi("deoksugung-wall", "덕수궁 돌담길", GeoPoint(37.56711, 126.97676), "jung-gu", PoiCategory.STREET),
    Poi("deoksugung", "덕수궁", GeoPoint(37.56580, 126.97515), "jung-gu", PoiCategory.CULTURE),
    Poi("seoul-plaza", "서울광장", GeoPoint(37.56560, 126.97798), "jung-gu", PoiCategory.PUBLIC_SPACE),
    // Central-Seoul field-test fixtures. These anchors intentionally use stable, recognizable
    // public places so the same NEW_AREA -> REPEAT_AREA route can be repeated on a real device.
    Poi(
        "euljiro-1ga-station",
        "을지로입구역",
        GeoPoint(37.566110, 126.982500),
        "jung-gu",
        PoiCategory.LANDMARK,
    ),
    Poi(
        "sk-seorin-building",
        "SK서린빌딩",
        GeoPoint(37.5696896560345, 126.98030408661),
        "jongno-gu",
        PoiCategory.LANDMARK,
    ),
    // "광화문" is anchored to Gwanghwamun Gate itself rather than the broader square/district.
    Poi(
        "gwanghwamun-gate",
        "광화문",
        GeoPoint(37.575930, 126.976820),
        "jongno-gu",
        PoiCategory.CULTURE,
    ),
    // Insadong is an area, so the fixture uses a representative central point on Insadong-gil
    // near 24 Insadong-gil instead of pretending the entire neighborhood is a single point.
    Poi(
        "insadong",
        "인사동",
        GeoPoint(37.5729518, 126.9865200),
        "jongno-gu",
        PoiCategory.STREET,
    ),
    // Jungwon-gu Office uses Seongnam City's published Jungwon-gu Office stop anchor
    // (중원구청 06-015). The office address is 중원구 제일로 36; visually confirm the marker
    // against the actual office frontage during the first physical run before treating it as a
    // trusted building-centroid reference.
    Poi(
        "jungwon-gu-office",
        "중원구청",
        GeoPoint(37.43079833, 127.13670250),
        "seongnam-jungwon",
        PoiCategory.LANDMARK,
    ),
    Poi(
        "seongnam-sports-complex",
        "성남종합운동장",
        GeoPoint(37.43450117, 127.137906833),
        "seongnam-jungwon",
        PoiCategory.LANDMARK,
    ),
    // Field-test fixture for 스타벅스 성남모란DT점 (경기 성남시 중원구 둔촌대로 131).
    // The point is centered between the verified adjacent road-address coordinates and must be
    // visually rechecked against the real storefront during the first physical field-test run.
    Poi(
        "starbucks-seongnam-moran-dt",
        "스타벅스 성남모란DT점",
        GeoPoint(37.42919320, 127.13345424),
        "seongnam-jungwon",
        PoiCategory.OTHER,
    ),
)
