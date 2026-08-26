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
    private val rootGroup = Regex("""<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""", setOf(RegexOption.DOT_MATCHES_ALL))
    private val simpleChild = Regex("""<(?:ellipse|circle|path|rect)\b[^>]*(?:/>|></(?:ellipse|circle|path|rect)>)""")
    private val compactScale = Regex("""scale\(\.(\d+)\)""")

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
        val targetTransform = groupTransform.find(body)?.groupValues?.get(1)
            ?: error("Missing lighting group transform for $lightingKey")
        val expressionSvg = normalizeExpression(catalog.text(expressionKey), targetTransform)
        val layers = mutableListOf(body)
        if (companionId == "moru" && appearance != AppearanceProfile.BASE) {
            val base = catalog.text("appearance.moru.base")
            val profile = catalog.text("appearance.moru.${appearance.semantic}")
            appearanceDelta(base, profile, targetTransform)?.let(layers::add)
        }
        layers += expressionSvg
        return renderLayers(layers, targetPx, targetPx)
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
        val targetTransform = groupTransform.find(body)?.groupValues?.get(1) ?: error("Missing body transform")
        val expressionSvg = normalizeExpression(catalog.text(expressionKey), targetTransform)
        return opaqueBounds(renderLayers(listOf(body), targetPx, targetPx), lightingKey) to
            opaqueBounds(renderLayers(listOf(expressionSvg), targetPx, targetPx), expressionKey)
    }

    fun lightingTransform(catalog: CandidateAssetCatalog, companionId: String, lighting: CompanionLightingFamily): String =
        groupTransform.find(catalog.text("lighting.$companionId.${lighting.name.lowercase()}"))?.groupValues?.get(1)
            ?: error("Missing lighting transform")

    fun renderMarker(catalog: CandidateAssetCatalog, semanticKey: String, family: MarkerFamily, widthPx: Int = 48, heightPx: Int = 64): Bitmap =
        renderLayers(listOf(catalog.text(semanticKey, family)), widthPx, heightPx)

    fun renderAsset(catalog: CandidateAssetCatalog, semanticKey: String, widthPx: Int, heightPx: Int): Bitmap =
        renderLayers(listOf(catalog.text(semanticKey)), widthPx, heightPx)

    private fun normalizeExpression(svg: String, targetTransform: String): String {
        val normalizedRoot = svg.replaceFirst(
            Regex("""<svg[^>]*>"""),
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"320\" viewBox=\"0 0 256 320\">",
        )
        return groupTransform.replaceFirst(normalizedRoot, "<g transform=\"$targetTransform\"")
    }

    private fun appearanceDelta(base: String, profile: String, targetTransform: String): String? {
        val baseBody = rootGroup.find(base)?.groupValues?.get(1) ?: return null
        val profileBody = rootGroup.find(profile)?.groupValues?.get(1) ?: return null
        val baseChildren = simpleChild.findAll(baseBody).map { it.value }.toList()
        val profileChildren = simpleChild.findAll(profileBody).map { it.value }.toList()
        val delta = profileChildren.filterIndexed { index, child ->
            index >= baseChildren.size || canonical(child) != canonical(baseChildren[index])
        }
        if (delta.isEmpty()) return null
        val profileDefs = defs.find(profile)?.value.orEmpty()
        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"320\" viewBox=\"0 0 256 320\">")
            append(profileDefs)
            append("<g transform=\"").append(targetTransform).append("\">")
            delta.forEach(::append)
            append("</g></svg>")
        }
    }

    private fun canonical(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    /**
     * AndroidSVG 1.4 accepts the authored SVG structure but its transform-number parser does not
     * reliably render compact leading-dot scale values such as `scale(.78)` on the managed-device
     * path. Normalize only that syntactic form here; the numeric transform and authored art stay
     * unchanged. This is an adapter compatibility fix, not an export/design mutation.
     */
    private fun androidSvgCompatible(svg: String): String =
        compactScale.replace(svg) { match -> "scale(0.${match.groupValues[1]})" }

    private fun renderLayers(svgLayers: List<String>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val destination = RectF(0f, 0f, width.toFloat(), height.toFloat())
        svgLayers.forEach { SVG.getFromString(androidSvgCompatible(it)).renderToCanvas(canvas, destination) }
        return bitmap
    }

    private fun opaqueBounds(bitmap: Bitmap, label: String): android.graphics.Rect {
        var left = bitmap.width
        var top = bitmap.height
        var right = -1
        var bottom = -1
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) {
                left = minOf(left, x); top = minOf(top, y); right = maxOf(right, x); bottom = maxOf(bottom, y)
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
