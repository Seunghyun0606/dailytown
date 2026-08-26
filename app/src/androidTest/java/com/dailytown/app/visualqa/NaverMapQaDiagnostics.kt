package com.dailytown.app.visualqa

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import com.dailytown.app.BuildConfig
import com.dailytown.app.map.MapHealth
import kotlin.math.sqrt
import org.json.JSONArray
import org.json.JSONObject

/**
 * Value-blind NAVER visual-QA diagnostics.
 *
 * Never records the NCP key, raw provider exceptions, GPS/location data, fixture coordinates,
 * SSIDs, device serials, or other durable identifiers. The output exists only to distinguish
 * auth/network/emulator-rendering failures while the marker promotion gate remains fail-closed.
 */
internal class NaverMapQaDiagnostics(
    private val context: Context,
) {
    private val events = JSONArray()
    private val attempts = JSONArray()
    private val matrixCaptures = JSONArray()
    private val networkInitial = readNetworkSnapshot()
    private val runnerHint = InstrumentationRegistry.getArguments().getString("dailytownQaRunner").orEmpty()
    private var readyLatencyMs: Long? = null

    fun recordReady(latencyMs: Long, health: MapHealth) {
        readyLatencyMs = latencyMs
        recordHealth("ready", health)
    }

    fun recordHealth(stage: String, health: MapHealth) {
        events.put(
            JSONObject()
                .put("type", "health")
                .put("stage", stage)
                .put("status", health.status.name)
                .putNullable("errorCode", health.errorCode),
        )
    }

    fun recordEvidence(
        stage: String,
        attempt: Int,
        bitmap: Bitmap,
        evidence: BaseMapEvidence,
        passed: Boolean,
    ) {
        attempts.put(
            JSONObject()
                .put("stage", stage)
                .put("attempt", attempt)
                .put("widthPx", bitmap.width)
                .put("heightPx", bitmap.height)
                .put("quantizedColors", evidence.quantizedColors)
                .put("luminanceStdDev", evidence.luminanceStdDev)
                .put("strongEdgeRatio", evidence.strongEdgeRatio)
                .put("passed", passed),
        )
    }

    fun recordMatrixCapture(
        kind: String,
        id: String,
        phase: String,
        mapComplexity: String,
        motionMode: String,
        markerFamily: String,
        companionId: String,
        storageName: String,
    ) {
        matrixCaptures.put(
            JSONObject()
                .put("kind", kind)
                .put("id", id)
                .put("phase", phase)
                .put("mapComplexity", mapComplexity)
                .put("motionMode", motionMode)
                .put("markerFamily", markerFamily)
                .put("companionId", companionId)
                .put("technicalCaptureCompleted", true)
                .put("humanVisualReview", "required")
                .put("storageName", storageName),
        )
    }

    fun isNetworkValidated(): Boolean = readNetworkSnapshot().optBoolean("validated", false)

    fun write(outcome: String, failureCategory: String?) {
        val root = JSONObject()
            .put("schemaVersion", 3)
            .put("outcome", outcome)
            .putNullable("failureCategory", failureCategory)
            .put("packageName", context.packageName)
            .put("naverCredentialConfigured", BuildConfig.NAVER_MAP_CONFIGURED)
            .put("naverClient", naverClientJson())
            .put("environment", environmentJson())
            .put("networkInitial", networkInitial)
            .put("networkFinal", readNetworkSnapshot())
            .putNullable("readyLatencyMs", readyLatencyMs)
            .put("events", events)
            .put("baseMapAttempts", attempts)
            .put("matrixCaptures", matrixCaptures)

        PlatformTestStorageRegistry.getInstance()
            .openOutputFile("visual/naver-diagnostics/session.json")
            .bufferedWriter(Charsets.UTF_8)
            .use { it.write(root.toString(2)) }
    }

    private fun naverClientJson(): JSONObject = JSONObject()
        .put("mode", "NCP_KEY_ID")
        .put("expectedRegisteredAndroidPackage", BuildConfig.APPLICATION_ID)
        .put("packageMatchesExpected", context.packageName == BuildConfig.APPLICATION_ID)

    private fun environmentJson(): JSONObject = JSONObject()
        .put("runnerHint", runnerHint)
        .put("sdkInt", Build.VERSION.SDK_INT)
        .put("androidRelease", Build.VERSION.RELEASE ?: "")
        .put("manufacturer", Build.MANUFACTURER ?: "")
        .put("model", Build.MODEL ?: "")
        .put("product", Build.PRODUCT ?: "")
        .put("supportedAbis", JSONArray(Build.SUPPORTED_ABIS?.toList().orEmpty()))
        .put("emulator", isEmulator())

    private fun readNetworkSnapshot(): JSONObject {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = manager?.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)
        return JSONObject()
            .put("activeNetwork", network != null)
            .put("internet", capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
            .put("validated", capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true)
            .put("wifi", capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)
            .put("cellular", capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
            .put("ethernet", capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true)
            .put("vpn", capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true)
    }

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.orEmpty().lowercase()
        val model = Build.MODEL.orEmpty().lowercase()
        val product = Build.PRODUCT.orEmpty().lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            product.contains("sdk") ||
            product.contains("emulator") ||
            product.contains("atd")
    }
}

internal data class BaseMapEvidence(
    val quantizedColors: Int,
    val luminanceStdDev: Double,
    val strongEdgeRatio: Double,
) {
    /** Engineering blank/tile guard only; these are not product-readability thresholds. */
    fun hasProviderTexture(): Boolean =
        quantizedColors >= 6 && luminanceStdDev >= 3.0 && strongEdgeRatio >= .005
}

internal object BaseMapEvidenceAnalyzer {
    fun analyze(bitmap: Bitmap): BaseMapEvidence {
        val left = bitmap.width * 2 / 10
        val right = bitmap.width * 8 / 10
        val top = bitmap.height * 2 / 10
        val bottom = bitmap.height * 8 / 10
        val stepX = ((right - left) / 120).coerceAtLeast(1)
        val stepY = ((bottom - top) / 120).coerceAtLeast(1)
        val luminances = mutableListOf<Double>()
        val colorBins = hashSetOf<Int>()
        var strongEdges = 0
        var edgeComparisons = 0
        var previousRow = mutableListOf<Double>()

        for (y in top until bottom step stepY) {
            val currentRow = mutableListOf<Double>()
            var previousInRow: Double? = null
            var column = 0
            for (x in left until right step stepX) {
                val color = bitmap.getPixel(x, y)
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val luma = .2126 * r + .7152 * g + .0722 * b
                luminances += luma
                currentRow += luma
                colorBins += ((r shr 5) shl 6) or ((g shr 5) shl 3) or (b shr 5)

                previousInRow?.let {
                    edgeComparisons++
                    if (kotlin.math.abs(luma - it) >= 12.0) strongEdges++
                }
                if (column < previousRow.size) {
                    edgeComparisons++
                    if (kotlin.math.abs(luma - previousRow[column]) >= 12.0) strongEdges++
                }
                previousInRow = luma
                column++
            }
            previousRow = currentRow
        }

        val mean = luminances.average()
        val variance = luminances.map { (it - mean) * (it - mean) }.average()
        return BaseMapEvidence(
            quantizedColors = colorBins.size,
            luminanceStdDev = sqrt(variance),
            strongEdgeRatio = strongEdges.toDouble() / edgeComparisons.coerceAtLeast(1),
        )
    }
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject =
    put(name, value ?: JSONObject.NULL)
