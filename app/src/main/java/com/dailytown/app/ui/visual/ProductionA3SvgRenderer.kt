package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.LruCache
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.caverock.androidsvg.SVG
import com.dailytown.app.visual.SemanticAssetKey

/**
 * Production renderer for the promoted A-3 SVG family only.
 *
 * A-3 candidate QA used AndroidSVG across the approved 360/412/600dp matrix. Companion rendering
 * deliberately remains on its separate restricted canvas renderer; this class fails closed if a
 * non-A3 semantic key crosses the boundary.
 */
class ProductionA3SvgRenderer(
    private val catalog: AndroidProductionVisualAssetCatalog,
    cacheEntries: Int = ProductionVisualAssetRegistry.PROMOTED_A3_COUNT,
) {
    private val svgCache = object : LruCache<String, SVG>(cacheEntries.coerceAtLeast(1)) {
        override fun sizeOf(key: String, value: SVG): Int = 1
    }

    fun draw(key: SemanticAssetKey, canvas: Canvas, viewport: RectF) {
        require(viewport.width() > 0f && viewport.height() > 0f)
        svg(key).renderToCanvas(canvas, viewport)
    }

    fun render(key: SemanticAssetKey, widthPx: Int, heightPx: Int): Bitmap {
        require(widthPx > 0 && heightPx > 0)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        draw(key, Canvas(bitmap), RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()))
        check(hasOpaquePixel(bitmap)) { "Production A-3 rendered transparent: ${key.value}" }
        return bitmap
    }

    fun clearCache() = svgCache.evictAll()

    private fun svg(key: SemanticAssetKey): SVG {
        val record = ProductionVisualAssetRegistry.require(key)
        check(record.family == ProductionVisualAssetRegistry.Family.A3) {
            "Production A-3 renderer rejects non-A3 semantic key: ${key.value}"
        }
        svgCache.get(key.value)?.let { return it }
        val parsed = SVG.getFromString(catalog.readSvg(key))
        svgCache.put(key.value, parsed)
        return parsed
    }

    private fun hasOpaquePixel(bitmap: Bitmap): Boolean {
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }
}

/** Semantic Compose adapter; raw asset paths remain inside ProductionVisualAssetRegistry. */
@Composable
fun rememberProductionA3AssetRenderer(): SemanticAssetRenderer {
    val assets = LocalContext.current.assets
    val renderer = remember(assets) {
        ProductionA3SvgRenderer(AndroidProductionVisualAssetCatalog(assets))
    }
    return remember(renderer) {
        { key, modifier ->
            ComposeCanvas(modifier = modifier) {
                if (size.width > 0f && size.height > 0f) {
                    drawIntoCanvas { composeCanvas ->
                        renderer.draw(
                            key = key,
                            canvas = composeCanvas.nativeCanvas,
                            viewport = RectF(0f, 0f, size.width, size.height),
                        )
                    }
                }
            }
        }
    }
}
