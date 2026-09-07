package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val PROXY_QUERY_PADDING_METERS = 1_600.0
private const val PROXY_MAX_RADIUS_METERS = 20_000

/**
 * Provider-neutral client for the app-owned POI gateway used by release builds.
 *
 * The gateway owns provider credentials and upstream-specific mapping. The Android client receives
 * only Daily Town's normalized POI contract, so changing the upstream provider does not require
 * changing the map/gameplay boundary. The query origin is snapped before leaving the device; exact
 * gameplay coordinates remain local and CachingPoiRepository filters the padded response back to
 * the requested radius.
 */
internal class DailyTownPoiProxyRepository(
    private val source: DailyTownPoiProxySource,
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> =
        source.fetch(center, radiusMeters)

    override fun sourceMetadata(): List<PoiSourceMetadata> = listOf(
        PoiSourceMetadata(
            id = "dailytown-poi-gateway",
            displayName = "Daily Town POI Gateway",
            role = PoiSourceRole.CANONICAL,
            attributionText = "Daily Town 중계 · 원천 데이터: 한국관광공사 TourAPI",
            licenseSummary = "앱에는 원천 API 인증키를 포함하지 않음. 원천 데이터 이용조건과 이미지 권리는 별도 준수.",
        ),
        PoiSourceMetadata(
            id = "kto-tourapi-korservice2",
            displayName = "한국관광공사 TourAPI",
            role = PoiSourceRole.CANONICAL,
            attributionText = "출처: 한국관광공사 TourAPI",
            licenseSummary = "공공데이터포털 이용허락범위 제한 없음. 이미지 자료는 공공누리 유형/피사체 권리 별도 확인.",
            sourceUrl = "https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578",
        ),
    )
}

internal fun interface DailyTownPoiProxySource {
    suspend fun fetch(center: GeoPoint, radiusMeters: Double): List<Poi>
}

internal class HttpDailyTownPoiProxySource(
    baseUrl: String,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 7_000,
) : DailyTownPoiProxySource {
    private val baseUrl = baseUrl.trim().trimEnd('/').also { normalized ->
        require(normalized.startsWith("https://")) {
            "Daily Town POI gateway must use HTTPS."
        }
    }

    override suspend fun fetch(center: GeoPoint, radiusMeters: Double): List<Poi> =
        withContext(Dispatchers.IO) {
            require(radiusMeters > 0.0) { "radiusMeters must be positive" }
            val url = buildDailyTownPoiProxyUrl(baseUrl, center, radiusMeters)
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMillis
                readTimeout = readTimeoutMillis
                setRequestProperty("Accept", "application/json")
            }
            try {
                val code = connection.responseCode
                if (code !in 200..299) error("Daily Town POI gateway HTTP $code")
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                parseDailyTownPoiProxyResponse(payload)
            } finally {
                connection.disconnect()
            }
        }
}

internal fun buildDailyTownPoiProxyUrl(
    baseUrl: String,
    center: GeoPoint,
    radiusMeters: Double,
): String {
    require(baseUrl.trim().startsWith("https://")) { "Daily Town POI gateway must use HTTPS." }
    require(radiusMeters > 0.0) { "radiusMeters must be positive" }
    // Reuse the same approved neighborhood-scale minimization as the internal TourAPI bridge.
    val outboundCenter = snapTourApiQueryCenter(center)
    val outboundRadius = (radiusMeters + PROXY_QUERY_PADDING_METERS)
        .toInt()
        .coerceIn(1, PROXY_MAX_RADIUS_METERS)
    val query = linkedMapOf(
        "latitude" to outboundCenter.latitude.toString(),
        "longitude" to outboundCenter.longitude.toString(),
        "radiusMeters" to outboundRadius.toString(),
        "schemaVersion" to "1",
    ).entries.joinToString("&") { (key, value) ->
        "${proxyEncode(key)}=${proxyEncode(value)}"
    }
    return "${baseUrl.trimEnd('/')}/v1/pois/nearby?$query"
}

internal fun parseDailyTownPoiProxyResponse(payload: String): List<Poi> {
    val root = JSONObject(payload)
    val schemaVersion = root.optInt("schemaVersion", -1)
    require(schemaVersion == 1) { "Unsupported Daily Town POI gateway schemaVersion=$schemaVersion" }
    val raw = root.optJSONArray("pois") ?: JSONArray()
    return buildList {
        for (index in 0 until raw.length()) {
            val item = raw.optJSONObject(index) ?: continue
            val id = item.optString("id").trim()
            val name = item.optString("name").trim()
            val latitude = item.opt("latitude")?.toString()?.toDoubleOrNull()
            val longitude = item.opt("longitude")?.toString()?.toDoubleOrNull()
            if (
                id.isBlank() || name.isBlank() ||
                latitude == null || longitude == null ||
                latitude !in -90.0..90.0 || longitude !in -180.0..180.0
            ) continue
            val category = runCatching {
                PoiCategory.valueOf(item.optString("category", PoiCategory.OTHER.name).uppercase())
            }.getOrDefault(PoiCategory.OTHER)
            val districtKey = item.optString("districtKey").trim().ifBlank { "gateway" }
            add(
                Poi(
                    id = id,
                    name = name,
                    position = GeoPoint(latitude, longitude),
                    districtKey = districtKey,
                    category = category,
                ),
            )
        }
    }.distinctBy { it.id }
}

private fun proxyEncode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.name())
