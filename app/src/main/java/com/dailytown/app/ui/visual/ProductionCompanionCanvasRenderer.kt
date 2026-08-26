package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.LruCache
import androidx.core.graphics.PathParser
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.ResolvedCompanionVisual
import com.dailytown.app.visual.SemanticAssetKey
import java.io.StringReader
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

/**
 * Production raster adapter for the promoted companion SVG subset.
 *
 * AndroidSVG is deliberately not used for companion runtime rendering because managed-device QA
 * proved that the promoted companion exports can become fully transparent on that parser path.
 * This adapter supports only the primitive subset already present in promoted companion exports
 * (path/circle/ellipse/rect plus uniform root transform and primitive rotate), and therefore fails
 * closed if an unsupported element enters the production pack.
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

    private data class Layer(val svg: String, val rootTransform: String)

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
        val rootTransform = transformOf(lightingSvg, resolved.lightingAsset)
        val layers = mutableListOf(Layer(lightingSvg, rootTransform))

        if (
            resolved.companionId == "moru" &&
            resolved.appearanceProfile != AppearanceProfile.BASE &&
            resolved.appearanceAsset != null
        ) {
            val baseKey = CompanionAssetResolver.appearanceKey("moru", AppearanceProfile.BASE)
            val base = catalog.readSvg(baseKey)
            val profile = catalog.readSvg(resolved.appearanceAsset)
            appearanceDelta(base, profile, rootTransform)?.let { layers += Layer(it, rootTransform) }
        }

        layers += Layer(catalog.readSvg(resolved.expressionAsset), rootTransform)
        val bitmap = rasterize(layers, targetPx, targetPx)
        check(hasOpaquePixel(bitmap)) {
            "Production companion rendered transparent: ${resolved.companionId}/${resolved.expression}/${resolved.lightingFamily}"
        }
        bitmapCache.put(key, bitmap)
        return bitmap
    }

    fun clearCache() = bitmapCache.evictAll()

    private fun transformOf(svg: String, key: SemanticAssetKey): String =
        ROOT_TRANSFORM.find(svg)?.groupValues?.get(1)
            ?: error("Missing promoted companion root transform: ${key.value}")

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

    private fun rasterize(layers: List<Layer>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        layers.forEach { layer -> drawLayer(canvas, layer, width, height) }
        return bitmap
    }

    private fun drawLayer(canvas: Canvas, layer: Layer, width: Int, height: Int) {
        val document = XML_FACTORY.newDocumentBuilder().parse(InputSource(StringReader(layer.svg)))
        val svg = document.documentElement
        val viewBox = parseViewBox(svg.getAttribute("viewBox"))
        val root = parseUniformTransform(layer.rootTransform)
        val classStyles = parseClassStyles(document)

        val destinationScale = minOf(width / viewBox.width(), height / viewBox.height())
        val destinationLeft = (width - viewBox.width() * destinationScale) / 2f
        val destinationTop = (height - viewBox.height() * destinationScale) / 2f

        val matrix = Matrix().apply {
            postTranslate(-viewBox.left, -viewBox.top)
            postScale(destinationScale, destinationScale)
            postTranslate(destinationLeft, destinationTop)
            postTranslate(root.translateX, root.translateY)
            postScale(root.scale, root.scale, root.translateX, root.translateY)
        }

        val group = firstRenderableRootGroup(svg)
            ?: error("Missing promoted companion render group")
        drawChildren(group, canvas, matrix, classStyles)
    }

    private fun drawChildren(parent: Element, canvas: Canvas, parentMatrix: Matrix, classes: Map<String, Style>) {
        val children = parent.childNodes
        for (index in 0 until children.length) {
            val node = children.item(index)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val element = node as Element
            when (element.tagName.substringAfterLast(':')) {
                "g" -> {
                    val local = Matrix(parentMatrix)
                    element.getAttribute("transform").takeIf(String::isNotBlank)?.let { transform ->
                        local.postConcat(parseElementTransform(transform))
                    }
                    drawChildren(element, canvas, local, classes)
                }
                "path", "circle", "ellipse", "rect" -> drawPrimitive(element, canvas, parentMatrix, classes)
                "title", "desc", "metadata", "defs", "style" -> Unit
                else -> error("Unsupported promoted companion SVG element: ${element.tagName}")
            }
        }
    }

    private fun drawPrimitive(element: Element, canvas: Canvas, matrix: Matrix, classes: Map<String, Style>) {
        val path = when (element.tagName.substringAfterLast(':')) {
            "path" -> PathParser.createPathFromPathData(element.getAttribute("d"))
                ?: error("Invalid promoted companion path data")
            "circle" -> Path().apply {
                val cx = number(element, "cx")
                val cy = number(element, "cy")
                val r = number(element, "r")
                addCircle(cx, cy, r, Path.Direction.CW)
            }
            "ellipse" -> Path().apply {
                val cx = number(element, "cx")
                val cy = number(element, "cy")
                val rx = number(element, "rx")
                val ry = number(element, "ry")
                addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
            }
            "rect" -> Path().apply {
                val x = number(element, "x", 0f)
                val y = number(element, "y", 0f)
                val width = number(element, "width")
                val height = number(element, "height")
                val rx = number(element, "rx", 0f)
                val ry = number(element, "ry", rx)
                if (rx > 0f || ry > 0f) addRoundRect(RectF(x, y, x + width, y + height), rx, ry, Path.Direction.CW)
                else addRect(x, y, x + width, y + height, Path.Direction.CW)
            }
            else -> error("Unsupported primitive: ${element.tagName}")
        }

        val elementMatrix = Matrix(matrix)
        element.getAttribute("transform").takeIf(String::isNotBlank)?.let { transform ->
            elementMatrix.postConcat(parseElementTransform(transform))
        }
        path.transform(elementMatrix)

        val style = resolveStyle(element, classes)
        val opacity = element.getAttribute("opacity").takeIf(String::isNotBlank)?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 1f
        style.fill?.let { fill ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.FILL
                color = fill
                alpha = (Color.alpha(fill) * opacity).toInt().coerceIn(0, 255)
            }
            canvas.drawPath(path, paint)
        }
        style.stroke?.let { stroke ->
            val scale = matrixScale(elementMatrix)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                color = stroke
                strokeWidth = style.strokeWidth * scale
                strokeCap = when (style.strokeLineCap) {
                    "round" -> Paint.Cap.ROUND
                    "square" -> Paint.Cap.SQUARE
                    else -> Paint.Cap.BUTT
                }
                strokeJoin = when (style.strokeLineJoin) {
                    "round" -> Paint.Join.ROUND
                    "bevel" -> Paint.Join.BEVEL
                    else -> Paint.Join.MITER
                }
                alpha = (Color.alpha(stroke) * opacity).toInt().coerceIn(0, 255)
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun parseClassStyles(document: org.w3c.dom.Document): Map<String, Style> {
        val styles = linkedMapOf<String, Style>()
        val nodes = document.getElementsByTagName("style")
        for (index in 0 until nodes.length) {
            val css = nodes.item(index).textContent.orEmpty()
            CSS_CLASS_RULE.findAll(css).forEach { match ->
                styles[match.groupValues[1]] = parseDeclarations(match.groupValues[2], Style())
            }
        }
        return styles
    }

    private fun resolveStyle(element: Element, classes: Map<String, Style>): Style {
        var style = Style()
        element.getAttribute("class")
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .forEach { className -> classes[className]?.let { style = style.merge(it) } }
        style = parseDeclarations(element.getAttribute("style"), style)
        if (element.hasAttribute("fill")) style = style.copy(fill = parsePaint(element.getAttribute("fill")))
        if (element.hasAttribute("stroke")) style = style.copy(stroke = parsePaint(element.getAttribute("stroke")))
        if (element.hasAttribute("stroke-width")) style = style.copy(strokeWidth = element.getAttribute("stroke-width").toFloat())
        if (element.hasAttribute("stroke-linecap")) style = style.copy(strokeLineCap = element.getAttribute("stroke-linecap"))
        if (element.hasAttribute("stroke-linejoin")) style = style.copy(strokeLineJoin = element.getAttribute("stroke-linejoin"))
        return style
    }

    private fun parseDeclarations(raw: String, base: Style): Style {
        var result = base
        raw.split(';').forEach { declaration ->
            val separator = declaration.indexOf(':')
            if (separator <= 0) return@forEach
            val name = declaration.substring(0, separator).trim()
            val value = declaration.substring(separator + 1).trim()
            result = when (name) {
                "fill" -> result.copy(fill = parsePaint(value))
                "stroke" -> result.copy(stroke = parsePaint(value))
                "stroke-width" -> result.copy(strokeWidth = value.toFloat())
                "stroke-linecap" -> result.copy(strokeLineCap = value)
                "stroke-linejoin" -> result.copy(strokeLineJoin = value)
                else -> result
            }
        }
        return result
    }

    private fun parsePaint(value: String): Int? = when (value.trim().lowercase(Locale.US)) {
        "none", "transparent", "" -> null
        else -> Color.parseColor(value.trim())
    }

    private fun parseElementTransform(value: String): Matrix {
        val rotate = ROTATE.matchEntire(value.trim())
        if (rotate != null) {
            val angle = rotate.groupValues[1].toFloat()
            val cx = rotate.groupValues[2].takeIf(String::isNotBlank)?.toFloat() ?: 0f
            val cy = rotate.groupValues[3].takeIf(String::isNotBlank)?.toFloat() ?: 0f
            return Matrix().apply { postRotate(angle, cx, cy) }
        }
        val transform = parseUniformTransform(value)
        return Matrix().apply {
            postTranslate(transform.translateX, transform.translateY)
            postScale(transform.scale, transform.scale, transform.translateX, transform.translateY)
        }
    }

    private fun parseUniformTransform(value: String): UniformTransform {
        val match = UNIFORM_TRANSFORM.matchEntire(value.trim())
            ?: error("Unsupported promoted companion transform: $value")
        return UniformTransform(
            match.groupValues[1].toFloat(),
            match.groupValues[2].toFloat(),
            match.groupValues[3].toFloat(),
        )
    }

    private fun parseViewBox(value: String): RectF {
        val parts = value.trim().split(Regex("[\\s,]+"))
        require(parts.size == 4) { "Invalid promoted companion viewBox: $value" }
        val x = parts[0].toFloat()
        val y = parts[1].toFloat()
        val width = parts[2].toFloat()
        val height = parts[3].toFloat()
        require(width > 0f && height > 0f)
        return RectF(x, y, x + width, y + height)
    }

    private fun firstRenderableRootGroup(svg: Element): Element? {
        val children = svg.childNodes
        for (index in 0 until children.length) {
            val node = children.item(index)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                if (element.tagName.substringAfterLast(':') == "g") return element
            }
        }
        return null
    }

    private fun number(element: Element, name: String, default: Float? = null): Float {
        val value = element.getAttribute(name)
        if (value.isBlank()) return default ?: error("Missing promoted companion attribute $name")
        return value.toFloat()
    }

    private fun matrixScale(matrix: Matrix): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        val sx = kotlin.math.sqrt(values[Matrix.MSCALE_X] * values[Matrix.MSCALE_X] + values[Matrix.MSKEW_Y] * values[Matrix.MSKEW_Y])
        val sy = kotlin.math.sqrt(values[Matrix.MSKEW_X] * values[Matrix.MSKEW_X] + values[Matrix.MSCALE_Y] * values[Matrix.MSCALE_Y])
        return (sx + sy) / 2f
    }

    private fun hasOpaquePixel(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 24).coerceAtLeast(1)
        val stepY = (bitmap.height / 24).coerceAtLeast(1)
        for (y in 0 until bitmap.height step stepY) for (x in 0 until bitmap.width step stepX) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }

    private data class UniformTransform(val translateX: Float, val translateY: Float, val scale: Float)

    private data class Style(
        val fill: Int? = null,
        val stroke: Int? = null,
        val strokeWidth: Float = 1f,
        val strokeLineCap: String = "butt",
        val strokeLineJoin: String = "miter",
    ) {
        fun merge(other: Style): Style = Style(
            fill = other.fill ?: fill,
            stroke = other.stroke ?: stroke,
            strokeWidth = if (other.strokeWidth != 1f) other.strokeWidth else strokeWidth,
            strokeLineCap = if (other.strokeLineCap != "butt") other.strokeLineCap else strokeLineCap,
            strokeLineJoin = if (other.strokeLineJoin != "miter") other.strokeLineJoin else strokeLineJoin,
        )
    }

    companion object {
        private val XML_FACTORY = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        private val ROOT_TRANSFORM = Regex("""<g\s+transform=\"([^\"]+)\"""")
        private val DEFS = Regex("""<defs>.*?</defs>""", setOf(RegexOption.DOT_MATCHES_ALL))
        private val ROOT_GROUP = Regex("""<g\s+transform=\"[^\"]+\">(.*)</g>\s*</svg>""", setOf(RegexOption.DOT_MATCHES_ALL))
        private val SIMPLE_CHILD = Regex("""<(?:ellipse|circle|path|rect)\b[^>]*(?:/>|></(?:ellipse|circle|path|rect)>)""")
        private val CSS_CLASS_RULE = Regex("""\.([A-Za-z_][A-Za-z0-9_-]*)\s*\{([^}]*)\}""")
        private val UNIFORM_TRANSFORM = Regex(
            """translate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)\s*scale\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*\)""",
        )
        private val ROTATE = Regex(
            """rotate\(\s*([+-]?(?:\d+(?:\.\d*)?|\.\d+))(?:[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+))[\s,]+([+-]?(?:\d+(?:\.\d*)?|\.\d+)))?\s*\)""",
        )
    }
}
