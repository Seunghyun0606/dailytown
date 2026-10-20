package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.LruCache
import com.dailytown.app.visual.MoruV2SemanticKey
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Raster-to-raster v2 renderer. It never redraws Moru with vector/Canvas primitives: Canvas is used
 * only to crop/scale/composite the authoritative PNG layers. Results are cached by semantic key and
 * target size so the deterministic materialization does not run every frame.
 */
internal class MoruV2RasterRenderer(
    private val catalog: AndroidMoruV2RuntimeAssetCatalog,
) {
    private val outputCache = object : LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
    }

    fun render(key: MoruV2SemanticKey, targetLongEdgePx: Int): Bitmap {
        require(targetLongEdgePx > 0)
        val cacheKey = "${key.value}@$targetLongEdgePx"
        outputCache.get(cacheKey)?.let { return it }

        val usage = catalog.usageTransform(key.usage)
        val longEdge = max(usage.outputWidth, usage.outputHeight)
        val outputScale = targetLongEdgePx.toDouble() / longEdge.toDouble()
        val outputWidth = (usage.outputWidth * outputScale).roundToInt().coerceAtLeast(1)
        val outputHeight = (usage.outputHeight * outputScale).roundToInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val expression = catalog.expressionRecord(key.expression)
        val sampleSize = chooseSampleSize(expression.width, expression.height, targetLongEdgePx)
        val affinity = catalog.affinityRecord(key.affinity)
        decode(expression, sampleSize).useBitmap { base ->
            if (affinity == null) {
                drawLayer(canvas, paint, base, sampleSize, usage, outputScale)
            } else {
                decode(affinity, sampleSize).useBitmap { overlay ->
                    val composite = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
                    Canvas(composite).apply {
                        drawBitmap(base, 0f, 0f, paint)
                        drawBitmap(overlay, 0f, 0f, paint)
                    }
                    composite.useBitmap { source ->
                        drawLayer(canvas, paint, source, sampleSize, usage, outputScale)
                    }
                }
            }
        }

        catalog.lightingTransfer(key.lighting)?.let { applyLighting(bitmap, it) }
        outputCache.put(cacheKey, bitmap)
        return bitmap
    }

    private fun decode(record: MoruV2PackagedRecord, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
            inSampleSize = sampleSize
        }
        return catalog.openSource(record).use { stream ->
            requireNotNull(BitmapFactory.decodeStream(stream, null, options)) {
                "Unable to decode ${record.assetPath}"
            }
        }
    }

    private fun drawLayer(
        canvas: Canvas,
        paint: Paint,
        source: Bitmap,
        sampleSize: Int,
        usage: MoruV2UsageTransform,
        outputScale: Double,
    ) {
        val crop = usage.sourceCrop
        val sourceRect = Rect(
            (crop[0].toDouble() / sampleSize).roundToInt().coerceIn(0, source.width),
            (crop[1].toDouble() / sampleSize).roundToInt().coerceIn(0, source.height),
            (crop[2].toDouble() / sampleSize).roundToInt().coerceIn(0, source.width),
            (crop[3].toDouble() / sampleSize).roundToInt().coerceIn(0, source.height),
        )
        val destination = RectF(
            (usage.pasteX * outputScale).toFloat(),
            (usage.pasteY * outputScale).toFloat(),
            ((usage.pasteX + usage.resizedWidth) * outputScale).toFloat(),
            ((usage.pasteY + usage.resizedHeight) * outputScale).toFloat(),
        )
        canvas.drawBitmap(source, sourceRect, destination, paint)
    }

    private fun applyLighting(bitmap: Bitmap, transfer: MoruV2LightingTransfer) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (index in pixels.indices) {
            val argb = pixels[index]
            val alpha = argb ushr 24 and 0xFF
            if (alpha == 0) continue
            val rgb = doubleArrayOf(
                (argb ushr 16 and 0xFF).toDouble(),
                (argb ushr 8 and 0xFF).toDouble(),
                (argb and 0xFF).toDouble(),
            )
            val lab = rgbToLab(rgb)
            for (axis in 0..2) {
                val transferred = (lab[axis] - transfer.muLight[axis]) * transfer.scale[axis] +
                    transfer.muLight[axis] + transfer.delta[axis]
                lab[axis] = lab[axis] * (1.0 - transfer.blend) + transferred * transfer.blend
            }
            val out = labToRgb(lab)
            pixels[index] = (alpha shl 24) or
                (out[0].roundToInt().coerceIn(0, 255) shl 16) or
                (out[1].roundToInt().coerceIn(0, 255) shl 8) or
                out[2].roundToInt().coerceIn(0, 255)
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun rgbToLab(rgb255: DoubleArray): DoubleArray {
        val linear = DoubleArray(3) { axis ->
            val value = rgb255[axis] / 255.0
            if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        val x = (0.4124564 * linear[0] + 0.3575761 * linear[1] + 0.1804375 * linear[2]) / 0.95047
        val y = 0.2126729 * linear[0] + 0.7151522 * linear[1] + 0.0721750 * linear[2]
        val z = (0.0193339 * linear[0] + 0.1191920 * linear[1] + 0.9503041 * linear[2]) / 1.08883
        fun f(value: Double): Double = if (value > 0.008856) Math.cbrt(value) else 7.787 * value + 16.0 / 116.0
        val fx = f(x)
        val fy = f(y)
        val fz = f(z)
        return doubleArrayOf(116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz))
    }

    private fun labToRgb(lab: DoubleArray): DoubleArray {
        val fy = (lab[0] + 16.0) / 116.0
        val fx = lab[1] / 500.0 + fy
        val fz = fy - lab[2] / 200.0
        fun inverse(value: Double): Double {
            val cube = value * value * value
            return if (cube > 0.008856) cube else (value - 16.0 / 116.0) / 7.787
        }
        val x = 0.95047 * inverse(fx)
        val y = inverse(fy)
        val z = 1.08883 * inverse(fz)
        val linear = doubleArrayOf(
            3.2404542 * x - 1.5371385 * y - 0.4985314 * z,
            -0.9692660 * x + 1.8760108 * y + 0.0415560 * z,
            0.0556434 * x - 0.2040259 * y + 1.0572252 * z,
        )
        return DoubleArray(3) { axis ->
            val value = linear[axis]
            val srgb = if (value <= 0.0031308) 12.92 * value else 1.055 * value.coerceAtLeast(0.0).pow(1.0 / 2.4) - 0.055
            srgb * 255.0
        }
    }

    private fun chooseSampleSize(width: Int, height: Int, targetLongEdgePx: Int): Int {
        val sourceLongEdge = max(width, height)
        var sample = 1
        while (sourceLongEdge / (sample * 2) >= targetLongEdgePx * 2) sample *= 2
        return sample
    }
}

private inline fun <T> Bitmap.useBitmap(block: (Bitmap) -> T): T = try {
    block(this)
} finally {
    recycle()
}
