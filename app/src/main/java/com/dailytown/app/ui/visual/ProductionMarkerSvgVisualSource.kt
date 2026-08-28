package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.LruCache
import com.caverock.androidsvg.SVG
import com.dailytown.app.map.MapMarkerBitmap
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapMarkerVisualSource
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.visual.MarkerAssetResolver
import kotlin.math.roundToInt

/**
 * Android-owned semantic marker renderer. It is safe to wire before promotion because the
 * production registry is empty; missing records return null and the map provider keeps its
 * default marker. Candidate files are never opened by this class.
 */
class ProductionMarkerSvgVisualSource(
    private val catalog: ProductionMarkerSvgCatalog,
    private val lookup: ProductionMarkerAssetLookup = ProductionMarkerAssetRegistry,
) : MapMarkerVisualSource {
    private val resolver = MarkerAssetResolver(lookup)
    private val bitmapCache = object : LruCache<String, Bitmap>(lookup.records().size.coerceAtLeast(1)) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    override fun resolve(marker: MapMarkerSpec, theme: MapThemeSpec): MapMarkerBitmap? {
        val resolved = resolver.resolve(
            semantic = marker.semantic,
            family = theme.markerFamily,
            selected = marker.selected,
        )
        val record = lookup.resolve(resolved.family, resolved.semanticKey) ?: return null
        val cacheKey = "${record.family.name}|${record.semanticKey.value}"
        val bitmap = bitmapCache.get(cacheKey)
            ?: render(record)?.also { bitmapCache.put(cacheKey, it) }
            ?: return null
        return MapMarkerBitmap(
            bitmap = bitmap,
            anchorX = resolved.anchorX,
            anchorY = resolved.anchorY,
        )
    }

    fun clearCache() = bitmapCache.evictAll()

    private fun render(record: ProductionMarkerAssetRecord): Bitmap? = runCatching {
        val svg = SVG.getFromString(catalog.readSvg(record))
        val documentWidth = svg.documentWidth
        val documentHeight = svg.documentHeight
        check(documentWidth.isFinite() && documentHeight.isFinite() && documentWidth > 0f && documentHeight > 0f) {
            "Production marker SVG must declare finite positive dimensions: ${record.assetPath}"
        }
        val widthPx = documentWidth.roundToInt().coerceAtLeast(1)
        val heightPx = documentHeight.roundToInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        svg.renderToCanvas(
            Canvas(bitmap),
            RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
        )
        check(hasOpaquePixel(bitmap)) {
            "Production marker rendered transparent: family=${record.family} semantic=${record.semanticKey.value}"
        }
        bitmap
    }.getOrNull()

    private fun hasOpaquePixel(bitmap: Bitmap): Boolean {
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }
}
