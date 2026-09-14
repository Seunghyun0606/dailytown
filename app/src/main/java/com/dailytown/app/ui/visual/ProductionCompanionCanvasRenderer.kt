package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.LruCache
import androidx.core.graphics.PathParser
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.ResolvedCompanionVisual
import com.dailytown.app.visual.SemanticAssetKey
import kotlin.math.min

/**
 * Production raster adapter for the promoted companion SVG subset.
 *
 * AndroidSVG is deliberately not used for companion rendering because managed-device QA proved
 * that the promoted companion exports can become fully transparent on that parser path. This
 * implementation is the same restricted primitive parser proven by candidate QA: path/circle/
 * ellipse/rect, CSS class declarations, one uniform root translate+scale, and primitive rotate.
 * Unsupported transforms fail closed and source/design values are never rewritten.
 */
class ProductionCompanionCanvasRenderer(
    private val catalog: AndroidProductionVisualAssetCatalog,
    cacheEntries: Int = 32,
) {
    private data class CacheKey(
        val companionId: String,
        val expression: String,
        val lighting: String,
        val appearance: String,
        val targetPx: Int,
    )

    private data class Layer(
        val svg: String,
        val rootTransform: String,
    )

    private data class UniformTransform(
        val translateX: Float,
        val translateY: Float,
        val scale: Float,
    )

    private val bitmapCache = object : LruCache<CacheKey, Bitmap>(cacheEntries.coerceAtLeast(1)) {
        override fun sizeOf(key: CacheKey, value: Bitmap): Int = 1
    }

    fun render(resolved: ResolvedCompanionVisual, targetPx: Int = 256): Bitmap {
        require(targetPx > 0)
        val key = CacheKey(
            companionId = resolved.companionId,
            expression = resolved.expressionAsset.value,
            lighting = resolved.lightingAsset.value,
            appearance = resolved.appearanceAsset?.value.orEmpty(),
            targetPx = targetPx,
        )
        bitmapCache.get(key)?.let { return it }

        val lightingSvg = catalog.readSvg(resolved.lightingAsset)
        val targetTransform = transformOf(lightingSvg, resolved.lightingAsset)
        val layers = mutableListOf(Layer(lightingSvg, targetTransform))

        if (
            resolved.companionId == "moru" &&
            resolved.appearanceProfile != AppearanceProfile.BASE &&
            resolved.appearanceAsset != null
        ) {
            val base = catalog.readSvg(CompanionAssetResolver.appearanceKey("moru", AppearanceProfile.BASE))
            val profile = catalog.readSvg(resolved.appearanceAsset)
            appearanceDelta(base, profile, targetTransform)?.let { layers += Layer(it, targetTransform) }
        }

        layers += Layer(catalog.readSvg(resolved.expressionAsset), targetTransform)
        val bitmap = rasterize(layers, targetPx, targetPx)
        check(hasOpaquePixel(bitmap)) {
            "Production companion rendered transparent: ${resolved.companionId}/${resolved.expression}/${resolved.lightingFamily}"
        }
        bitmapCache.put(key, bitmap)
        return bitmap
    }

    fun clearCache() = bitmapCache.evictAll()

    private fun rasterize(layers: List<Layer>, widthPx: Int, heightPx: Int): Bitmap {
        require(widthPx > 0 && heightPx > 0)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        layers.forEach { layer -> drawLayer(canvas, layer, widthPx, heightPx) }
        return bitmap
    }

    private fun drawLayer(canvas: Canvas, layer: Layer, widthPx: Int, heightPx: Int) {
        require(!layer.svg.contains("<!DOCTYPE", ignoreCase = true)) {
            "DOCTYPE is forbidden in promoted companion SVG"
        }
        val body = ROOT_GROUP.find(layer.svg)?.groupValues?.get(1)
            ?: error("Promoted companion is missing its single root transform group")
        val classes = parseCssClasses(layer.svg)
        val transform = parseUniformTransform(layer.rootTransform)

        val viewportScale = min(widthPx / DOCUMENT_WIDTH, heightPx / DOCUMENT_HEIGHT)
        val offsetX = (widthPx - DOCUMENT_WIDTH * viewportScale) / 2f
        val offsetY = (heightPx - DOCUMENT_HEIGHT * viewportScale) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(viewportScale, viewportScale)
        canvas.translate(transform.translateX, transform.translateY)
        canvas.scale(transform.scale, transform.scale)

        PRIMITIVE.findAll(body).forEach { match ->
            val tag = match.groupValues[1].lowercase()
            val attrs = parseAttributes(match.groupValues[2], classes)
            canvas.save()
            attrs["transform"]?.let { applyLocalTransform(canvas, it) }
            drawPrimitive(canvas, tag, attrs)
            canvas.restore()
        }
        canvas.restore()
    }

    private fun parseCssClasses(svg: String): Map<String, Map<String, String>> =
        CSS_CLASS_RULE.findAll(svg).associate { match ->
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

    private fun parseAttributes(
        raw: String,
        classes: Map<String, Map<String, String>>,
    ): Map<String, String> {
        val explicit = ATTRIBUTE.findAll(raw)
            .associateTo(linkedMapOf()) { it.groupValues[1] to it.groupValues[2] }
        val resolved = linkedMapOf<String, String>()
        explicit["class"]
            ?.split(Regex("\\s+"))
            ?.filter(String::isNotBlank)
            .orEmpty()
            .forEach { className -> classes[className]?.let(resolved::putAll) }
        explicit.filterKeys { it != "class" }.forEach(resolved::put)
        return resolved
    }

    private fun transformOf(svg: String, key: SemanticAssetKey): String =
        ROOT_TRANSFORM.find(svg)?.groupValues?.get(1)
            ?: error("Missing promoted companion root transform: ${key.value}")

    private fun parseUniformTransform(value: String): UniformTransform {
        val match = UNIFORM_TRANSFORM.matchEntire(value.trim())
            ?: error("Unsupported promoted companion root transform: $value")
        return UniformTransform(
            translateX = match.groupValues[1].toFloat(),
            translateY = match.groupValues[2].toFloat(),
            scale = match.groupValues[3].toFloat(),
        ).also { require(it.scale > 0f) }
    }

    private fun applyLocalTransform(canvas: Canvas, value: String) {
        val match = ROTATE_TRANSFORM.matchEntire(value.trim())
            ?: error("Unsupported promoted companion primitive transform: $value")
        val degrees = match.groupValues[1].toFloat()
        val cx = match.groupValues[2].takeIf(String::isNotEmpty)?.toFloat() ?: 0f
        val cy = match.groupValues[3].takeIf(String::isNotEmpty)?.toFloat() ?: 0f
        canvas.rotate(degrees, cx, cy)
    }

    private fun drawPrimitive(canvas: Canvas, tag: String, attrs: Map<String, String>) {
        val fill = paint(attrs, Paint.Style.FILL)
        val stroke = paint(attrs, Paint.Style.STROKE)
        when (tag) {
            "path" -> {
                val pathData = attrs["d"] ?: error("Promoted companion path is missing d")
                val path = PathParser.createPathFromPathData(pathData)
                fill?.let { canvas.drawPath(path, it) }
                stroke?.let { canvas.drawPath(path, it) }
            }
            "circle" -> {
                val cx = number(attrs, "cx")
                val cy = number(attrs, "cy")
                val radius = number(attrs, "r")
                fill?.let { canvas.drawCircle(cx, cy, radius, it) }
                stroke?.let { canvas.drawCircle(cx, cy, radius, it) }
            }
            "ellipse" -> {
                val cx = number(attrs, "cx")
                val cy = number(attrs, "cy")
                val rx = number(attrs, "rx")
                val ry = number(attrs, "ry")
                val oval = RectF(cx - rx, cy - ry, cx + rx, cy + ry)
                fill?.let { canvas.drawOval(oval, it) }
                stroke?.let { canvas.drawOval(oval, it) }
            }
            "rect" -> {
                val x = attrs["x"]?.toFloat() ?: 0f
                val y = attrs["y"]?.toFloat() ?: 0f
                val width = number(attrs, "width")
                val height = number(attrs, "height")
                val rx = attrs["rx"]?.toFloat() ?: 0f
                val ry = attrs["ry"]?.toFloat() ?: rx
                val rect = RectF(x, y, x + width, y + height)
                if (rx > 0f || ry > 0f) {
                    fill?.let { canvas.drawRoundRect(rect, rx, ry, it) }
                    stroke?.let { canvas.drawRoundRect(rect, rx, ry, it) }
                } else {
                    fill?.let { canvas.drawRect(rect, it) }
                    stroke?.let { canvas.drawRect(rect, it) }
                }
            }
            else -> error("Unsupported promoted companion primitive: $tag")
        }
    }

    private fun paint(attrs: Map<String, String>, style: Paint.Style): Paint? {
        val value = if (style == Paint.Style.FILL) attrs["fill"] ?: "#000000" else attrs["stroke"]
        if (value == null || value.equals("none", ignoreCase = true)) return null
        val opacity = (attrs["opacity"]?.toFloat() ?: 1f).coerceIn(0f, 1f)
        val channelOpacity = when (style) {
            Paint.Style.FILL -> attrs["fill-opacity"]?.toFloat() ?: 1f
            Paint.Style.STROKE -> attrs["stroke-opacity"]?.toFloat() ?: 1f
            else -> 1f
        }.coerceIn(0f, 1f)
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = style
            color = Color.parseColor(value)
            alpha = (255f * opacity * channelOpacity).toInt().coerceIn(0, 255)
            if (style == Paint.Style.STROKE) {
                strokeWidth = attrs["stroke-width"]?.toFloat() ?: 1f
                strokeCap = when (attrs["stroke-linecap"]?.lowercase()) {
                    "round" -> Paint.Cap.ROUND
                    "square" -> Paint.Cap.SQUARE
                    else -> Paint.Cap.BUTT
                }
                strokeJoin = when (attrs["stroke-linejoin"]?.lowercase()) {
                    "round" -> Paint.Join.ROUND
                    "bevel" -> Paint.Join.BEVEL
                    else -> Paint.Join.MITER
                }
            }
        }
    }

    private fun appearanceDelta(base: String, profile: String, targetTransform: String): String? {
        val baseBody = ROOT_GROUP.find(base)?.groupValues?.get(1) ?: return null
        val profileBody = ROOT_GROUP.find(profile)?.groupValues?.get(1) ?: return null
        val baseChildren = SIMPLE_CHILD.findAll(baseBody).map { it.value }.toList()
        val profileChildren = SIMPLE_CHILD.findAll(profileBody).map { it.value }.toList()
        val delta = profileChildren.filterIndexed { index, child ->
            index >= baseChildren.size || canonical(child) != canonical(baseChildren[index])
        }
        if (delta.isEmpty()) return null
        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"320\" viewBox=\"0 0 256 320\">")
            append(DEFS.find(profile)?.value.orEmpty())
            append("<g transform=\"").append(targetTransform).append("\">")
            delta.forEach(::append)
            append("</g></svg>")
        }
    }

    private fun canonical(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    private fun number(attrs: Map<String, String>, name: String): Float =
        attrs[name]?.toFloat() ?: error("Promoted companion primitive is missing $name")

    private fun hasOpaquePixel(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 24).coerceAtLeast(1)
        val stepY = (bitmap.height / 24).coerceAtLeast(1)
        for (y in 0 until bitmap.height step stepY) for (x in 0 until bitmap.width step stepX) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }

    companion object {
        private val CSS_CLASS_RULE = Regex("""\.([A-Za-z_][A-Za-z0-9_-]*)\s*\{([^}]*)\}""")
        private val ROOT_TRANSFORM = Regex("""<g\s+transform=\"([^\"]+)\"""")
        private val DEFS = Regex("""<defs>.*?</defs>""", setOf(RegexOption.DOT_MATCHES_ALL))
        private val ROOT_GROUP = Regex(
            """<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""",
            setOf(RegexOption.DOT_MATCHES_ALL),
        )
        private val SIMPLE_CHILD = Regex("""<(?:ellipse|circle|path|rect)\b[^>]*(?:/>|></(?:ellipse|circle|path|rect)>)""")
        private val PRIMITIVE = Regex(
            """<(path|ellipse|circle|rect)\b([^>]*)/?>""",
            setOf(RegexOption.IGNORE_CASE),
        )
        private val ATTRIBUTE = Regex("""([A-Za-z_:][A-Za-z0-9_.:-]*)\s*=\s*\"([^\"]*)\"""")
        private val UNIFORM_TRANSFORM = Regex(
            """translate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)\s*scale\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)""",
        )
        private val ROTATE_TRANSFORM = Regex(
            """rotate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))(?:[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+)))?\s*\)""",
        )
        private const val DOCUMENT_WIDTH = 256f
        private const val DOCUMENT_HEIGHT = 320f
    }
}
