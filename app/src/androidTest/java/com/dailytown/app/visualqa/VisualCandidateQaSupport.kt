package com.dailytown.app.visualqa

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import com.caverock.androidsvg.SVG
import com.dailytown.app.map.MapMarkerBitmap
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapMarkerVisualSource
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.ResolvedMarkerAsset
import java.nio.charset.StandardCharsets
import org.json.JSONObject

internal data class CandidateEntry(
    val semanticKey: String,
    val assetPath: String,
    val family: String? = null,
    val sha256: String,
)

internal class CandidateAssetCatalog(private val assets: AssetManager) {
    private val entries: List<CandidateEntry> = listOf(
        "production-promotion-batch-01.v1.json",
        "production-promotion-batch-01-luca-derivatives.v1.json",
        "marker-split-export-v1.json",
        "a3-split-export-v1.json",
    ).flatMap(::readManifest)

    fun text(semanticKey: String, family: MarkerFamily? = null): String {
        val entry = entries.singleOrNull {
            it.semanticKey == semanticKey && (family == null || it.family == family.name)
        } ?: error("No candidate for semantic=$semanticKey family=$family")
        return assets.open(entry.assetPath).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    fun allEntries(): List<CandidateEntry> = entries

    private fun readManifest(name: String): List<CandidateEntry> {
        val json = assets.open(name).bufferedReader(StandardCharsets.UTF_8).use { JSONObject(it.readText()) }
        val values = json.getJSONArray("assets")
        return buildList(values.length()) {
            for (index in 0 until values.length()) {
                val value = values.getJSONObject(index)
                val repositoryPath = value.getString("path")
                add(
                    CandidateEntry(
                        semanticKey = value.getString("semantic_key"),
                        assetPath = repositoryPath.removePrefix("design/production/"),
                        family = value.optString("family").takeIf(String::isNotBlank),
                        sha256 = value.getString("sha256"),
                    ),
                )
            }
        }
    }
}

internal object CandidateSvgRenderer {
    private val groupTransform = Regex("""<g\s+transform=\"([^\"]+)\"""")
    private val defs = Regex("""<defs>.*?</defs>""", setOf(RegexOption.DOT_MATCHES_ALL))
    private val rootGroup = Regex(
        """<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""",
        setOf(RegexOption.DOT_MATCHES_ALL),
    )
    private val simpleChild = Regex("""<(?:ellipse|circle|path|rect)\b[^>]*(?:/>|></(?:ellipse|circle|path|rect)>)""")

    fun renderCompanion(
        catalog: CandidateAssetCatalog,
        companionId: String,
        expression: CompanionExpression,
        lighting: CompanionLightingFamily,
        appearance: AppearanceProfile = AppearanceProfile.BASE,
        targetPx: Int,
    ): Bitmap {
        val lightingKey = "lighting.$companionId.${lighting.name.lowercase()}"
        val expressionKey = "companion.$companionId.expression.${expression.semantic}"
        val body = catalog.text(lightingKey)
        val targetTransform = transformOf(body, lightingKey)
        val layers = mutableListOf(
            CompanionCandidateCanvasRenderer.Layer(body, targetTransform),
        )

        if (companionId == "moru" && appearance != AppearanceProfile.BASE) {
            val base = catalog.text("appearance.moru.base")
            val profile = catalog.text("appearance.moru.${appearance.semantic}")
            appearanceDelta(base, profile, targetTransform)?.let {
                layers += CompanionCandidateCanvasRenderer.Layer(it, targetTransform)
            }
        }

        layers += CompanionCandidateCanvasRenderer.Layer(catalog.text(expressionKey), targetTransform)
        return CompanionCandidateCanvasRenderer.render(layers, targetPx, targetPx)
    }

    fun renderCompanionLayerBounds(
        catalog: CandidateAssetCatalog,
        companionId: String,
        expression: CompanionExpression,
        lighting: CompanionLightingFamily,
        targetPx: Int,
    ): Pair<android.graphics.Rect, android.graphics.Rect> {
        val lightingKey = "lighting.$companionId.${lighting.name.lowercase()}"
        val expressionKey = "companion.$companionId.expression.${expression.semantic}"
        val body = catalog.text(lightingKey)
        val transform = transformOf(body, lightingKey)
        val bodyBitmap = CompanionCandidateCanvasRenderer.renderSingle(body, transform, targetPx, targetPx)
        val expressionBitmap = CompanionCandidateCanvasRenderer.renderSingle(
            catalog.text(expressionKey),
            transform,
            targetPx,
            targetPx,
        )
        return opaqueBounds(bodyBitmap, lightingKey) to opaqueBounds(expressionBitmap, expressionKey)
    }

    fun lightingTransform(
        catalog: CandidateAssetCatalog,
        companionId: String,
        lighting: CompanionLightingFamily,
    ): String {
        val key = "lighting.$companionId.${lighting.name.lowercase()}"
        return transformOf(catalog.text(key), key)
    }

    fun renderMarker(
        catalog: CandidateAssetCatalog,
        semanticKey: String,
        family: MarkerFamily,
        widthPx: Int = 48,
        heightPx: Int = 64,
    ): Bitmap = renderSvg(catalog.text(semanticKey, family), widthPx, heightPx)

    fun renderAsset(
        catalog: CandidateAssetCatalog,
        semanticKey: String,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap = renderSvg(catalog.text(semanticKey), widthPx, heightPx)

    private fun transformOf(svg: String, label: String): String =
        groupTransform.find(svg)?.groupValues?.get(1)
            ?: error("Missing companion root transform for $label")

    private fun appearanceDelta(base: String, profile: String, targetTransform: String): String? {
        val baseBody = rootGroup.find(base)?.groupValues?.get(1) ?: return null
        val profileBody = rootGroup.find(profile)?.groupValues?.get(1) ?: return null
        val baseChildren = simpleChild.findAll(baseBody).map { it.value }.toList()
        val profileChildren = simpleChild.findAll(profileBody).map { it.value }.toList()
        val delta = profileChildren.filterIndexed { index, child ->
            index >= baseChildren.size || canonical(child) != canonical(baseChildren[index])
        }
        if (delta.isEmpty()) return null
        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"320\" viewBox=\"0 0 256 320\">")
            append(defs.find(profile)?.value.orEmpty())
            append("<g transform=\"").append(targetTransform).append("\">")
            delta.forEach(::append)
            append("</g></svg>")
        }
    }

    private fun canonical(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    private fun renderSvg(svg: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        SVG.getFromString(svg).renderToCanvas(canvas, RectF(0f, 0f, width.toFloat(), height.toFloat()))
        return bitmap
    }

    private fun opaqueBounds(bitmap: Bitmap, label: String): android.graphics.Rect {
        var left = bitmap.width
        var top = bitmap.height
        var right = -1
        var bottom = -1
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) {
                left = minOf(left, x)
                top = minOf(top, y)
                right = maxOf(right, x)
                bottom = maxOf(bottom, y)
            }
        }
        check(right >= left && bottom >= top) { "Rendered layer is transparent: $label" }
        return android.graphics.Rect(left, top, right + 1, bottom + 1)
    }
}

internal class CandidateMarkerVisualSource(
    private val catalog: CandidateAssetCatalog,
) : MapMarkerVisualSource {
    override fun resolve(marker: MapMarkerSpec, theme: MapThemeSpec): MapMarkerBitmap {
        val bitmap = CandidateSvgRenderer.renderMarker(catalog, marker.semantic.key.value, theme.markerFamily)
        return MapMarkerBitmap(
            bitmap = bitmap,
            anchorX = ResolvedMarkerAsset.GEO_ANCHOR_X,
            anchorY = ResolvedMarkerAsset.GEO_ANCHOR_Y,
        )
    }
}
