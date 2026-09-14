package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Design-only Android usage-context QA for the locked Old Ginkgo clue refs.
 *
 * The assets live under androidTest and are never packaged in the runtime APK. This verifies
 * Android decoding, density scaling, alpha compositing, and review captures without activation.
 */
@RunWith(AndroidJUnit4::class)
class OldGinkgoClueAssetQaTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val density = instrumentation.targetContext.resources.displayMetrics.density

    @Test
    fun lockedV2CluesRenderAcrossAndroidDensityAndContexts() {
        val note = decode("old-ginkgo-runtime-v2/folded_note_runtime_candidate_v2_ref.webp")
        val leaf = decode("old-ginkgo-runtime-v2/ginkgo_leaf_runtime_candidate_v2_ref.webp")

        assertEquals(128, note.width)
        assertEquals(128, note.height)
        assertEquals(96, leaf.width)
        assertEquals(96, leaf.height)
        assertTrue("folded note must retain alpha", note.hasAlpha())
        assertTrue("ginkgo leaf must retain alpha", leaf.hasAlpha())
        assertTransparentCorners(note, "folded note")
        assertTransparentCorners(leaf, "ginkgo leaf")

        renderBoard(
            source = note,
            title = "Old Ginkgo · Folded note",
            targetsDp = listOf(48, 64, 96, 144),
        ).writeToTestStorage("visual/old-ginkgo-clue-qa/folded-note.android-density")

        renderBoard(
            source = leaf,
            title = "Old Ginkgo · Ginkgo leaf",
            targetsDp = listOf(32, 48, 64, 96),
        ).writeToTestStorage("visual/old-ginkgo-clue-qa/ginkgo-leaf.android-density")
    }

    private fun decode(path: String): Bitmap =
        instrumentation.context.assets.open(path).use(BitmapFactory::decodeStream)
            ?: error("Unable to decode Android test asset: $path")

    private fun assertTransparentCorners(bitmap: Bitmap, label: String) {
        val corners = listOf(
            bitmap.getPixel(0, 0),
            bitmap.getPixel(bitmap.width - 1, 0),
            bitmap.getPixel(0, bitmap.height - 1),
            bitmap.getPixel(bitmap.width - 1, bitmap.height - 1),
        )
        assertTrue("$label corners must be transparent", corners.all { Color.alpha(it) == 0 })
    }

    private fun renderBoard(source: Bitmap, title: String, targetsDp: List<Int>): Bitmap {
        val width = dp(411)
        val maxTarget = targetsDp.max()
        val rowHeight = dp(maxTarget + 58)
        val headerHeight = dp(92)
        val height = headerHeight + rowHeight * SurfaceContext.entries.size + dp(16)
        val board = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(board)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(69, 59, 48)
            textSize = dp(13).toFloat()
        }
        val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(116, 104, 91)
            textSize = dp(10).toFloat()
        }

        canvas.drawColor(Color.rgb(244, 235, 221))
        canvas.drawText(title, dp(12).toFloat(), dp(24).toFloat(), textPaint)
        canvas.drawText(
            "density=${"%.3f".format(density)} · source=${source.width}×${source.height}px",
            dp(12).toFloat(),
            dp(43).toFloat(),
            smallTextPaint,
        )
        val maxTargetPx = dp(maxTarget)
        val sourceStatus = if (source.width >= maxTargetPx && source.height >= maxTargetPx) {
            "source-quality=adequate"
        } else {
            "source-quality=BLOCKED (${maxTarget}dp=${maxTargetPx}px exceeds ref)"
        }
        canvas.drawText(sourceStatus, dp(12).toFloat(), dp(60).toFloat(), smallTextPaint)
        canvas.drawText(
            "androidTest-only · runtime activation=false",
            dp(12).toFloat(),
            dp(77).toFloat(),
            smallTextPaint,
        )

        SurfaceContext.entries.forEachIndexed { row, surface ->
            val top = headerHeight + row * rowHeight
            drawSurface(canvas, paint, surface, top, rowHeight, width)
            textPaint.color = surface.labelColor
            canvas.drawText(surface.label, dp(10).toFloat(), (top + dp(19)).toFloat(), textPaint)

            var left = dp(8)
            targetsDp.forEach { targetDp ->
                val targetPx = dp(targetDp)
                smallTextPaint.color = surface.labelColor
                canvas.drawText(
                    "${targetDp}dp/${targetPx}px",
                    left.toFloat(),
                    (top + dp(38)).toFloat(),
                    smallTextPaint,
                )
                val assetTop = top + dp(45)
                canvas.drawBitmap(
                    source,
                    null,
                    RectF(
                        left.toFloat(),
                        assetTop.toFloat(),
                        (left + targetPx).toFloat(),
                        (assetTop + targetPx).toFloat(),
                    ),
                    paint,
                )
                left += targetPx + dp(8)
            }
        }
        return board
    }

    private fun drawSurface(
        canvas: Canvas,
        paint: Paint,
        surface: SurfaceContext,
        top: Int,
        height: Int,
        width: Int,
    ) {
        paint.style = Paint.Style.FILL
        paint.color = surface.background
        canvas.drawRect(0f, top.toFloat(), width.toFloat(), (top + height).toFloat(), paint)
        if (surface != SurfaceContext.MAP_HEAVY) return

        paint.color = Color.rgb(244, 239, 229)
        paint.strokeWidth = dp(18).toFloat()
        canvas.drawLine(0f, (top + dp(75)).toFloat(), width.toFloat(), (top + dp(118)).toFloat(), paint)
        canvas.drawLine(dp(95).toFloat(), top.toFloat(), dp(165).toFloat(), (top + height).toFloat(), paint)
        paint.color = Color.rgb(170, 185, 168)
        paint.strokeWidth = dp(2).toFloat()
        canvas.drawLine(0f, (top + dp(64)).toFloat(), width.toFloat(), (top + dp(107)).toFloat(), paint)
        paint.color = Color.rgb(177, 139, 69)
        listOf(54 to 92, 225 to 130, 350 to 72).forEach { (x, y) ->
            canvas.drawCircle(dp(x).toFloat(), (top + dp(y)).toFloat(), dp(4).toFloat(), paint)
        }
    }

    private fun dp(value: Int): Int = (value * density).roundToInt()

    private enum class SurfaceContext(
        val label: String,
        val background: Int,
        val labelColor: Int,
    ) {
        CREAM(
            label = "cream journal",
            background = Color.rgb(255, 248, 235),
            labelColor = Color.rgb(69, 59, 48),
        ),
        DARK(
            label = "dark surface",
            background = Color.rgb(31, 40, 34),
            labelColor = Color.rgb(244, 235, 221),
        ),
        MAP_HEAVY(
            label = "live/map-heavy fixture",
            background = Color.rgb(221, 231, 220),
            labelColor = Color.rgb(69, 59, 48),
        ),
    }
}
