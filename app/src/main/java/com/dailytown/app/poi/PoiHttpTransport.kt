package com.dailytown.app.poi

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class PoiHttpResponse(
    val statusCode: Int,
    val body: String,
)

internal fun interface PoiHttpTransport {
    suspend fun get(url: String): PoiHttpResponse
}

/** Small shared URLConnection transport; provider-specific status handling and parsing stay separate. */
internal class UrlConnectionPoiHttpTransport(
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 7_000,
) : PoiHttpTransport {
    override suspend fun get(url: String): PoiHttpResponse = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            val body = if (code in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            PoiHttpResponse(statusCode = code, body = body)
        } finally {
            connection.disconnect()
        }
    }
}
