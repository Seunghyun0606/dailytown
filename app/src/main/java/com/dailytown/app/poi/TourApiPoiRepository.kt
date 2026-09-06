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

private const val TOUR_API_BASE_URL = "https://apis.data.go.kr/B551011/KorService2"
private const val TOUR_API_MAX_RADIUS_METERS = 20_000

internal data class TourApiNearbyItem(
    val contentId: String,
    val title: String,
    val longitude: Double,
    val latitude: Double,
    val areaCode: String?,
    val sigunguCode: String?,
    val contentTypeId: String?,
)

internal fun interface TourApiNearbySource {
    suspend fun fetch(center: GeoPoint, radiusMeters: Double): List<TourApiNearbyItem>
}

internal class TourApiPoiRepository(
    private val source: TourApiNearbySource,
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> =
        source.fetch(center, radiusMeters).mapNotNull(::toPoi)

    override fun sourceMetadata(): List<PoiSourceMetadata> = listOf(
        PoiSourceMetadata(
            id = "kto-tourapi-korservice2",
            displayName = "한국관광공사 TourAPI",
            role = PoiSourceRole.CANONICAL,
            attributionText = "출처: 한국관광공사 TourAPI",
            licenseSummary = "공공데이터포털 이용허락범위 제한 없음. 이미지 자료는 공공누리 유형/피사체 권리 별도 확인.",
            sourceUrl = "https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578",
        ),
    )

    private fun toPoi(item: TourApiNearbyItem): Poi? {
        if (item.contentId.isBlank() || item.title.isBlank()) return null
        if (item.latitude !in -90.0..90.0 || item.longitude !in -180.0..180.0) return null
        val districtKey = buildString {
            append("tourapi")
            item.areaCode?.takeIf { it.isNotBlank() }?.let { append(":").append(it) }
            item.sigunguCode?.takeIf { it.isNotBlank() }?.let { append(":").append(it) }
        }
        return Poi(
            id = "tourapi:${item.contentId}",
            name = item.title.trim(),
            position = GeoPoint(item.latitude, item.longitude),
            districtKey = districtKey,
            category = contentTypeToCategory(item.contentTypeId),
        )
    }

    private fun contentTypeToCategory(contentTypeId: String?): PoiCategory = when (contentTypeId) {
        "12" -> PoiCategory.LANDMARK
        "14", "15" -> PoiCategory.CULTURE
        "25" -> PoiCategory.STREET
        "28" -> PoiCategory.PARK
        else -> PoiCategory.OTHER
    }
}

internal class HttpTourApiNearbySource(
    private val serviceKey: String,
    private val mobileApp: String = "DailyTown",
    private val mobileOs: String = "AND",
    private val baseUrl: String = TOUR_API_BASE_URL,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 7_000,
) : TourApiNearbySource {
    init {
        require(serviceKey.isNotBlank()) { "TourAPI service key is required." }
    }

    override suspend fun fetch(center: GeoPoint, radiusMeters: Double): List<TourApiNearbyItem> =
        withContext(Dispatchers.IO) {
            val url = buildTourApiLocationUrl(
                baseUrl = baseUrl,
                serviceKey = serviceKey,
                mobileApp = mobileApp,
                mobileOs = mobileOs,
                center = center,
                radiusMeters = radiusMeters,
            )
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMillis
                readTimeout = readTimeoutMillis
                setRequestProperty("Accept", "application/json")
            }
            try {
                val code = connection.responseCode
                if (code !in 200..299) {
                    error("TourAPI HTTP $code")
                }
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                parseTourApiNearbyResponse(payload)
            } finally {
                connection.disconnect()
            }
        }
}

internal fun buildTourApiLocationUrl(
    baseUrl: String,
    serviceKey: String,
    mobileApp: String,
    mobileOs: String,
    center: GeoPoint,
    radiusMeters: Double,
): String {
    val radius = radiusMeters.toInt().coerceIn(1, TOUR_API_MAX_RADIUS_METERS)
    val query = linkedMapOf(
        "serviceKey" to serviceKey,
        "MobileOS" to mobileOs,
        "MobileApp" to mobileApp,
        "_type" to "json",
        "pageNo" to "1",
        "numOfRows" to "100",
        "arrange" to "E",
        "mapX" to center.longitude.toString(),
        "mapY" to center.latitude.toString(),
        "radius" to radius.toString(),
    ).entries.joinToString("&") { (key, value) ->
        "${encodeQuery(key)}=${encodeQuery(value)}"
    }
    return "${baseUrl.trimEnd('/')}/locationBasedList2?$query"
}

private fun encodeQuery(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.name())

internal fun parseTourApiNearbyResponse(payload: String): List<TourApiNearbyItem> {
    val root = JSONObject(payload)
    val response = root.optJSONObject("response") ?: error("TourAPI response missing response object")
    val header = response.optJSONObject("header") ?: error("TourAPI response missing header")
    val resultCode = header.opt("resultCode")?.toString().orEmpty()
    if (resultCode != "0000") {
        val message = header.opt("resultMsg")?.toString().orEmpty()
        error("TourAPI resultCode=$resultCode${if (message.isNotBlank()) ": $message" else ""}")
    }

    val body = response.optJSONObject("body") ?: return emptyList()
    val itemsObject = body.opt("items") as? JSONObject ?: return emptyList()
    val rawItems = itemsObject.opt("item") ?: return emptyList()
    val objects: List<JSONObject> = when (rawItems) {
        is JSONArray -> buildList {
            for (index in 0 until rawItems.length()) {
                rawItems.optJSONObject(index)?.let(::add)
            }
        }
        is JSONObject -> listOf(rawItems)
        else -> emptyList()
    }

    return objects.mapNotNull { item ->
        val contentId = item.opt("contentid")?.toString()?.trim().orEmpty()
        val title = item.opt("title")?.toString()?.trim().orEmpty()
        val longitude = item.opt("mapx")?.toString()?.toDoubleOrNull()
        val latitude = item.opt("mapy")?.toString()?.toDoubleOrNull()
        if (contentId.isBlank() || title.isBlank() || longitude == null || latitude == null) {
            null
        } else {
            TourApiNearbyItem(
                contentId = contentId,
                title = title,
                longitude = longitude,
                latitude = latitude,
                areaCode = item.opt("areacode")?.toString(),
                sigunguCode = item.opt("sigungucode")?.toString(),
                contentTypeId = item.opt("contenttypeid")?.toString(),
            )
        }
    }
}
