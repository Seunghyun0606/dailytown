package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint

/** Debug-only authored anchors used by repeatable physical and emulator field-test routes. */
internal object BuildVariantFixtureCatalog {
    val items: List<Poi> = listOf(
        Poi("seoul-city-hall", "서울시청", GeoPoint(37.56650, 126.97800), "jung-gu", PoiCategory.LANDMARK),
        Poi("deoksugung-wall", "덕수궁 돌담길", GeoPoint(37.56711, 126.97676), "jung-gu", PoiCategory.STREET),
        Poi("deoksugung", "덕수궁", GeoPoint(37.56580, 126.97515), "jung-gu", PoiCategory.CULTURE),
        Poi("seoul-plaza", "서울광장", GeoPoint(37.56560, 126.97798), "jung-gu", PoiCategory.PUBLIC_SPACE),
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
        Poi(
            "gwanghwamun-gate",
            "광화문",
            GeoPoint(37.575930, 126.976820),
            "jongno-gu",
            PoiCategory.CULTURE,
        ),
        Poi(
            "insadong",
            "인사동",
            GeoPoint(37.5729518, 126.9865200),
            "jongno-gu",
            PoiCategory.STREET,
        ),
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
        Poi(
            "starbucks-seongnam-moran-dt",
            "스타벅스 성남모란DT점",
            GeoPoint(37.42919320, 127.13345424),
            "seongnam-jungwon",
            PoiCategory.OTHER,
        ),
    )
}
