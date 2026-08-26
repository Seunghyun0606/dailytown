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
import java.util.Locale
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
    private data class UniformTransform(val translateX: Double, val translateY: Double, val scale: Double)

    private val groupTransform = Regex("""<g\s+transform=\"([^\"]+)\"""")
    private val defs = Regex("""<defs>.*?</defs>""", setOf(RegexOption.DOT_MATCHES_ALL))
    private val rootGroup = Regex("""<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""", setOf(RegexOption.DOT_MATCHES_ALL))
    private val simpleChild = Regex("""<(?:ellipse|circle|path|rect)\b[^>]*(?:/>|></(?:ellipse|circle|path|rect)>)""")
    private val compactScale = Regex("""scale\(\.(\d+)\)""")
    private val cssClassRule = Regex("""\.([A-Za-z_][A-Za-z0-9_-]*)\s*\{([^}]*)\}""")
    private val elementWithClass = Regex("""<([A-Za-z][A-Za-z0-9:_-]*)([^<>]*\sclass=\"[^\"]+\"[^<>]*?)(/?)>""")
    private val classAttribute = Regex("""\sclass=\"([^\"]+)\"""")
    private val attributeName = Regex("""\s([A-Za-z_:][A-Za-z0-9_.:-]*)\s*=""")
    private val uniformTransform = Regex(
        """translate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)\s*scale\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)""",
    )
    private val rotateWithWhitespace = Regex(
        """rotate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)""",
    )

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
        val bodySource = catalog.text(lightingKey)
        val targetTransform = groupTransform.find(bodySource)?.groupValues?.get(1)
            ?: error("Missing lighting group transform for $lightingKey")
        val body = flattenCompanionLayer(bodySource, targetTransform)
        val expressionSvg = flattenCompanionLayer(catalog.text(expressionKey), targetTransform)
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
        val bodySource = catalog.text(lightingKey)
        val targetTransform = groupTransform.find(bodySource)?.groupValues?.get(1) ?: error("Missing body transform")
        val body = flattenCompanionLayer(bodySource, targetTransform)
        val expressionSvg = flattenCompanionLayer(catalog.text(expressionKey), targetTransform)
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

    /**
     * Companion exports place all canonical geometry under one uniform translate+scale root group.
     * AndroidSVG 1.4 renders the marker exports correctly on the managed device but drops that
     * companion root transform on this path. Convert the same transform into an equivalent viewBox
     * instead of changing any authored coordinates: final = translate + scale * source.
     */
    private fun flattenCompanionLayer(svg: String, targetTransform: String): String {
        val body = rootGroup.find(svg)?.groupValues?.get(1)
            ?: error("Missing companion root group")
        val transform = parseUniformTransform(targetTransform)
        return buildFlattenedLayer(defs.find(svg)?.value.orEmpty(), body, transform)
    }

    private fun buildFlattenedLayer(definitions: String, body: String, transform: UniformTransform): String {
        require(transform.scale > 0.0)
        val viewBoxX = -transform.translateX / transform.scale
        val viewBoxY = -transform.translateY / transform.scale
        val viewBoxWidth = 256.0 / transform.scale
        val viewBoxHeight = 320.0 / transform.scale
        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"320\" viewBox=\"")
            append(svgNumber(viewBoxX)).append(' ')
            append(svgNumber(viewBoxY)).append(' ')
            append(svgNumber(viewBoxWidth)).append(' ')
            append(svgNumber(viewBoxHeight)).append("\">")
            append(definitions)
            append(body)
            append("</svg>")
        }
    }

    private fun parseUniformTransform(value: String): UniformTransform {
        val match = uniformTransform.matchEntire(value.trim())
            ?: error("Unsupported companion root transform: $value")
        return UniformTransform(
            translateX = match.groupValues[1].toDouble(),
            translateY = match.groupValues[2].toDouble(),
            scale = match.groupValues[3].toDouble(),
        )
    }

    private fun svgNumber(value: Double): String = String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')

    private fun appearanceDelta(base: String, profile: String, targetTransform: String): String? {
        val baseBody = rootGroup.find(base)?.groupValues?.get(1) ?: return null
        val profileBody = rootGroup.find(profile)?.groupValues?.get(1) ?: return null
        val baseChildren = simpleChild.findAll(baseBody).map { it.value }.toList()
        val profileChildren = simpleChild.findAll(profileBody).map { it.value }.toList()
        val delta = profileChildren.filterIndexed { index, child ->
            index >= baseChildren.size || canonical(child) != canonical(baseChildren[index])
        }
        if (delta.isEmpty()) return null
        return buildFlattenedLayer(
            definitions = defs.find(profile)?.value.orEmpty(),
            body = delta.joinToString(separator = ""),
            transform = parseUniformTransform(targetTransform),
        )
    }

    private fun canonical(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    /**
     * Keep parser-compatibility rewrites inside this QA adapter only. The design source bytes,
     * semantic manifests and checksums are never mutated.
     */
    private fun androidSvgCompatible(svg: String): String {
        val normalizedScale = compactScale.replace(svg) { match -> "scale(0.${match.groupValues[1]})" }
        val normalizedRotate = rotateWithWhitespace.replace(normalizedScale) { match ->
            "rotate(${match.groupValues[1]},${match.groupValues[2]},${match.groupValues[3]})"
        }
        return inlineCssClassPresentationAttributes(normalizedRotate)
    }

    private fun inlineCssClassPresentationAttributes(svg: String): String {
        val stylesByClass = cssClassRule.findAll(svg).associate { match ->
            val declarations = linkedMapOf<String, String>()
            match.groupValues[2].split(';').forEach { declaration ->
                val separator = declaration.indexOf(':')
                if (separator > 0) {
                    val name = declaration.substring(0, separator).trim()
                    val value = declaration.substring(separator + 1).trim()
                    if (name.isNotEmpty() && value.isNotEmpty()) declarations[name] = value
                }
            }
            match.groupValues[1] to declarations
        }
        if (stylesByClass.isEmpty()) return svg

        return elementWithClass.replace(svg) { match ->
            val tagName = match.groupValues[1]
            val attributes = match.groupValues[2]
            val classNames = classAttribute.find(attributes)?.groupValues?.get(1)
                ?.split(Regex("\\s+"))
                ?.filter(String::isNotBlank)
                .orEmpty()
            if (classNames.isEmpty()) return@replace match.value

            val withoutClass = classAttribute.replace(attributes, "")
            val existingAttributes = attributeName.findAll(withoutClass)
                .map { it.groupValues[1] }
                .toHashSet()
            val resolved = linkedMapOf<String, String>()
            classNames.forEach { className ->
                stylesByClass[className]?.forEach { (name, value) -> resolved[name] = value }
            }
            val additions = resolved.entries.joinToString(separator = "") { (name, value) ->
                if (name in existingAttributes) "" else " $name=\"$value\""
            }
            val selfClosing = if (match.groupValues[3].isNotEmpty()) "/" else ""
            "<$tagName$withoutClass$additions$selfClosing>"
        }
    }

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
