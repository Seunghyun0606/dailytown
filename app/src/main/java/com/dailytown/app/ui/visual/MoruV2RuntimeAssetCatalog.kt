package com.dailytown.app.ui.visual

import android.content.res.AssetManager
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.MoruV2SemanticAvailability
import com.dailytown.app.visual.MoruV2SemanticKey
import java.io.InputStream
import org.json.JSONObject

internal data class MoruV2PackagedRecord(
    val kind: String,
    val semantic: String,
    val assetPath: String,
    val sourceAuthority: String,
    val expectedSha256: String,
    val width: Int,
    val height: Int,
    val mode: String,
)

internal data class MoruV2AuthorityRecord(
    val assetPath: String,
    val expectedSha256: String,
)

internal data class MoruV2UsageTransform(
    val outputWidth: Int,
    val outputHeight: Int,
    val sourceCrop: IntArray,
    val resizedWidth: Int,
    val resizedHeight: Int,
    val pasteX: Int,
    val pasteY: Int,
)

internal data class MoruV2LightingTransfer(
    val muLight: DoubleArray,
    val scale: DoubleArray,
    val delta: DoubleArray,
    val blend: Double,
)

internal data class MoruV2SemanticExpectation(
    val key: String,
    val width: Int,
    val height: Int,
    val rgbaPixelSha256: String,
    val alphaPixelSha256: String,
)

/** Reads the copied design authority directly from APK assets; no 432-key truth is re-authored in Kotlin. */
internal class AndroidMoruV2RuntimeAssetCatalog(
    private val assets: AssetManager,
) : MoruV2SemanticAvailability {
    private val packageManifest by lazy { json(PACKAGE_MANIFEST) }
    private val resolverManifest by lazy { json(RESOLVER_MANIFEST) }
    private val usageMetrics by lazy { json(USAGE_METRICS) }
    private val lightingMetrics by lazy { json(LIGHTING_METRICS) }

    private val sources by lazy {
        val array = packageManifest.getJSONArray("sources")
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val dimensions = item.getJSONArray("dimensions")
                add(
                    MoruV2PackagedRecord(
                        kind = item.getString("kind"),
                        semantic = item.getString("semantic"),
                        assetPath = item.getString("asset_path"),
                        sourceAuthority = item.getString("source_authority"),
                        expectedSha256 = item.getString("expected_sha256"),
                        width = dimensions.getInt(0),
                        height = dimensions.getInt(1),
                        mode = item.getString("mode"),
                    ),
                )
            }
        }
    }

    private val semanticExpectations by lazy {
        val array = resolverManifest.getJSONArray("entries")
        buildMap {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val dimensions = item.getJSONArray("dimensions")
                val key = item.getString("semantic_key")
                put(
                    key,
                    MoruV2SemanticExpectation(
                        key = key,
                        width = dimensions.getInt(0),
                        height = dimensions.getInt(1),
                        rgbaPixelSha256 = item.getString("rgba_pixel_sha256"),
                        alphaPixelSha256 = item.getString("alpha_pixel_sha256"),
                    ),
                )
            }
        }
    }

    init {
        // Package metadata itself is development-owned, but it must preserve design activation state.
        require(packageManifest.getString("semantic_profile") == PROFILE_V2)
        require(!packageManifest.getBoolean("runtime_activation"))
    }

    override fun contains(key: MoruV2SemanticKey): Boolean = semanticExpectations.containsKey(key.value)

    fun semanticExpectations(): Collection<MoruV2SemanticExpectation> = semanticExpectations.values

    fun expectation(key: MoruV2SemanticKey): MoruV2SemanticExpectation? = semanticExpectations[key.value]

    fun sourceRecords(): List<MoruV2PackagedRecord> = sources

    fun authorityRecords(): List<MoruV2AuthorityRecord> {
        val array = packageManifest.getJSONArray("authorities")
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(MoruV2AuthorityRecord(item.getString("asset_path"), item.getString("expected_sha256")))
            }
        }
    }

    fun designAuthorityHead(): String = packageManifest.getJSONObject("design_authority").getString("head")

    fun openSource(record: MoruV2PackagedRecord): InputStream = assets.open(record.assetPath)

    fun openAuthority(record: MoruV2AuthorityRecord): InputStream = assets.open(record.assetPath)

    fun expressionRecord(expression: CompanionExpression): MoruV2PackagedRecord =
        sources.single { it.kind == "expression" && it.semantic == expression.semantic }

    fun affinityRecord(affinity: AppearanceProfile): MoruV2PackagedRecord? = when (affinity) {
        AppearanceProfile.BASE -> null
        else -> sources.single { it.kind == "affinity" && it.semantic == affinity.semantic }
    }

    fun usageTransform(usage: CompanionUsageContext): MoruV2UsageTransform {
        val asset = usageMetrics.getJSONObject("assets").getJSONObject(usage.semantic)
        val dimensions = asset.getJSONArray("dimensions")
        val transform = asset.getJSONObject("transform")
        val crop = transform.getJSONArray("source_crop_xyxy")
        val resized = transform.getJSONArray("resized_dimensions")
        val paste = transform.getJSONArray("paste_offset")
        return MoruV2UsageTransform(
            outputWidth = dimensions.getInt(0),
            outputHeight = dimensions.getInt(1),
            sourceCrop = IntArray(4) { crop.getInt(it) },
            resizedWidth = resized.getInt(0),
            resizedHeight = resized.getInt(1),
            pasteX = paste.getInt(0),
            pasteY = paste.getInt(1),
        )
    }

    fun lightingTransfer(lighting: CompanionLightingFamily): MoruV2LightingTransfer? {
        if (lighting == CompanionLightingFamily.LIGHT) return null
        val item = lightingMetrics.getJSONObject("transfer").getJSONObject(lighting.name)
        return MoruV2LightingTransfer(
            muLight = item.getJSONArray("mu_light").toDoubleArray(),
            scale = item.getJSONArray("scale").toDoubleArray(),
            delta = item.getJSONArray("delta").toDoubleArray(),
            blend = item.getDouble("blend"),
        )
    }

    private fun json(path: String): JSONObject =
        assets.open(path).bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }

    companion object {
        const val PROFILE_V2 = "companion.moru.canonical.v2"
        const val PACKAGE_MANIFEST = "moru-v2/runtime-package-manifest.v1.json"
        const val RESOLVER_MANIFEST = "moru-v2/authority/semantic-resolver-export-manifest.v1.json"
        const val USAGE_METRICS = "moru-v2/authority/moru_native_usage_acceptance_metrics_v1.json"
        const val LIGHTING_METRICS = "moru-v2/authority/moru_native_lighting_acceptance_metrics_v1.json"
    }
}

private fun org.json.JSONArray.toDoubleArray(): DoubleArray = DoubleArray(length()) { getDouble(it) }
