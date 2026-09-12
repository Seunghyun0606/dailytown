package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.PathParser
import kotlin.math.min

/**
 * Deterministic QA-only renderer for the primitive subset used by the approved companion split exports.
 *
 * This intentionally does not become the production renderer. It exists so promotion evidence does not
 * depend on AndroidSVG parser quirks. Source SVG bytes, semantic keys, coordinates and paint values stay
 * authoritative and unchanged; this adapter only interprets their current path/circle/ellipse/rect subset.
 */
internal object CompanionCandidateCanvasRenderer {
    data class Layer(
        val svg: String,
        val rootTransform: String,
    )

    private data class UniformTransform(
        val translateX: Float,
        val translateY: Float,
        val scale: Float,
    )

    private val cssClassRule = Regex("""\.([A-Za-z_][A-Za-z0-9_-]*)\s*\{([^}]*)\}""")
    private val rootGroup = Regex(
        """<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""",
        setOf(RegexOption.DOT_MATCHES_ALL),
    )
    private val primitive = Regex(
        """<(path|ellipse|circle|rect)\b([^>]*)/?>""",
        setOf(RegexOption.IGNORE_CASE),
    )
    private val attribute = Regex("""([A-Za-z_:][A-Za-z0-9_.:-]*)\s*=\s*\"([^\"]*)\"""")
    private val uniformTransform = Regex(
        """translate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)\s*scale\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)""",
    )
    private val rotateTransform = Regex(
        """rotate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))(?:[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+)))?\s*\)""",
    )

    fun render(layers: List<Layer>, widthPx: Int, heightPx: Int): Bitmap {
        require(widthPx > 0 && heightPx > 0)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        layers.forEach { layer -> drawLayer(canvas, layer, widthPx, heightPx) }
        return bitmap
    }

    fun renderSingle(svg: String, rootTransform: String, widthPx: Int, heightPx: Int): Bitmap =
        render(listOf(Layer(svg, rootTransform)), widthPx, heightPx)

    private fun drawLayer(canvas: Canvas, layer: Layer, widthPx: Int, heightPx: Int) {
        val body = rootGroup.find(layer.svg)?.groupValues?.get(1)
            ?: error("Companion candidate is missing its single root transform group")
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

        primitive.findAll(body).forEach { match ->
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
        cssClassRule.findAll(svg).associate { match ->
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

    private fun parseAttributes(raw: String, classes: Map<String, Map<String, String>>): Map<String, String> {
        val explicit = attribute.findAll(raw).associateTo(linkedMapOf()) { it.groupValues[1] to it.groupValues[2] }
        val resolved = linkedMapOf<String, String>()
        explicit["class"]
            ?.split(Regex("\\s+"))
            ?.filter(String::isNotBlank)
            .orEmpty()
            .forEach { className -> classes[className]?.let(resolved::putAll) }
        explicit.filterKeys { it != "class" }.forEach(resolved::put)
        return resolved
    }

    private fun parseUniformTransform(value: String): UniformTransform {
        val match = uniformTransform.matchEntire(value.trim())
            ?: error("Unsupported companion root transform: $value")
        return UniformTransform(
            translateX = match.groupValues[1].toFloat(),
            translateY = match.groupValues[2].toFloat(),
            scale = match.groupValues[3].toFloat(),
        ).also { require(it.scale > 0f) }
    }

    private fun applyLocalTransform(canvas: Canvas, value: String) {
        val match = rotateTransform.matchEntire(value.trim())
            ?: error("Unsupported companion primitive transform: $value")
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
                val pathData = attrs["d"] ?: error("Companion path is missing d")
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
            else -> error("Unsupported companion primitive: $tag")
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

    private fun number(attrs: Map<String, String>, name: String): Float =
        attrs[name]?.toFloat() ?: error("Companion primitive is missing $name")

    private const val DOCUMENT_WIDTH = 256f
    private const val DOCUMENT_HEIGHT = 320f
}
