package com.dailytown.app.visualqa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import com.dailytown.app.visual.MapQaMotionMode
import com.dailytown.app.visual.VisualThemeProfile
import kotlin.math.min

/**
 * Screenshot-only full-stack fixture used by NAVER real-map QA.
 *
 * Geometry in this view is engineering capture framing, not a production visual token. The semantic
 * language is approved: following route, concentric active halo, botanical discovery shell/static
 * fallback, compact companion avatar, and HUD surfaces. Exact motion timing remains human-gated,
 * so NORMAL is a representative still frame rather than an invented animation timeline.
 */
internal class NaverMapOverlayQaSceneView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density

    private var profile: VisualThemeProfile? = null
    private var motionMode: MapQaMotionMode = MapQaMotionMode.REDUCED_MOTION
    private var companionBitmap: Bitmap? = null
    private var hudDeepNavyMix: Float = 0f

    fun bind(
        profile: VisualThemeProfile,
        motionMode: MapQaMotionMode,
        companionBitmap: Bitmap,
        hudDeepNavyMix: Float,
    ) {
        this.profile = profile
        this.motionMode = motionMode
        this.companionBitmap = companionBitmap
        this.hudDeepNavyMix = hudDeepNavyMix.coerceIn(0f, 1f)
        visibility = VISIBLE
        invalidate()
    }

    fun hideForBaseMapProof() {
        visibility = INVISIBLE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val theme = profile ?: return
        drawFollowingRoute(canvas, theme)
        drawActiveHalo(canvas, theme)
        drawDiscoverySample(canvas, theme)
        drawCompanionAvatar(canvas)
        drawHud(canvas)
    }

    private fun drawFollowingRoute(canvas: Canvas, theme: VisualThemeProfile) {
        val path = Path().apply {
            moveTo(width * .12f, height * .78f)
            cubicTo(width * .28f, height * .68f, width * .34f, height * .58f, width * .50f, height * .50f)
            cubicTo(width * .63f, height * .43f, width * .72f, height * .38f, width * .88f, height * .28f)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = dp(5f)
        paint.color = theme.route.toAndroidInt()
        paint.alpha = 220
        canvas.drawPath(path, paint)

        // Following-direction cue sampled as a still frame; no timing/easing is invented here.
        val arrowX = width * .72f
        val arrowY = height * .38f
        val arrow = Path().apply {
            moveTo(arrowX - dp(5f), arrowY - dp(4f))
            lineTo(arrowX + dp(5f), arrowY)
            lineTo(arrowX - dp(5f), arrowY + dp(4f))
        }
        paint.strokeWidth = dp(2.5f)
        paint.alpha = 235
        canvas.drawPath(arrow, paint)
    }

    private fun drawActiveHalo(canvas: Canvas, theme: VisualThemeProfile) {
        val cx = width * .50f
        val cy = height * .50f
        val routeColor = theme.route.toAndroidInt()
        paint.style = Paint.Style.STROKE
        paint.color = routeColor

        if (motionMode.reducedMotion) {
            // Approved reduced-motion grammar: static ring count/weight/luminance.
            listOf(36f to 3f, 47f to 2f).forEach { (radiusDp, strokeDp) ->
                paint.strokeWidth = dp(strokeDp)
                paint.alpha = if (radiusDp < 40f) 150 else 90
                canvas.drawCircle(cx, cy, dp(radiusDp), paint)
            }
        } else {
            // Representative normal-motion frame only. No pulse duration/frequency is encoded.
            listOf(34f to 3f, 44f to 2.5f, 55f to 2f).forEachIndexed { index, (radiusDp, strokeDp) ->
                paint.strokeWidth = dp(strokeDp)
                paint.alpha = listOf(165, 105, 55)[index]
                canvas.drawCircle(cx, cy, dp(radiusDp), paint)
            }
        }
        paint.alpha = 255
    }

    private fun drawDiscoverySample(canvas: Canvas, theme: VisualThemeProfile) {
        val cx = width * .66f
        val cy = height * .42f
        val warm = mixColor(BUTTER, WARM_POINT, theme.warmLocalPointWeight.coerceIn(0f, 1f))

        paint.style = Paint.Style.FILL
        paint.color = warm
        paint.alpha = if (motionMode.reducedMotion) 180 else 220
        canvas.drawCircle(cx, cy, dp(if (motionMode.reducedMotion) 5f else 7f), paint)

        paint.color = LEAF_PRIMARY
        paint.alpha = if (motionMode.reducedMotion) 175 else 210
        val leafCount = if (motionMode.reducedMotion) 2 else 5
        repeat(leafCount) { index ->
            val x = cx + dp((index - (leafCount - 1) / 2f) * 9f)
            val y = cy - dp(10f + (index % 2) * 6f)
            val leaf = RectF(x - dp(4f), y - dp(2.5f), x + dp(4f), y + dp(2.5f))
            canvas.save()
            canvas.rotate(if (index % 2 == 0) -25f else 25f, x, y)
            canvas.drawOval(leaf, paint)
            canvas.restore()
        }

        if (!motionMode.reducedMotion) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1.5f)
            paint.color = Color.WHITE
            paint.alpha = 215
            listOf(-18f to -17f, 19f to -12f, 14f to 14f).forEach { (dx, dy) ->
                drawSparkle(canvas, cx + dp(dx), cy + dp(dy))
            }
        }
        paint.alpha = 255
    }

    private fun drawSparkle(canvas: Canvas, x: Float, y: Float) {
        val half = dp(3.5f)
        canvas.drawLine(x - half, y, x + half, y, paint)
        canvas.drawLine(x, y - half, x, y + half, paint)
    }

    private fun drawCompanionAvatar(canvas: Canvas) {
        val bitmap = companionBitmap ?: return
        val side = dp(48f)
        val left = dp(16f)
        val top = height - dp(112f)
        val destination = RectF(left, top, left + side, top + side)
        paint.style = Paint.Style.FILL
        paint.alpha = 255
        canvas.drawBitmap(bitmap, null, destination, paint)
    }

    private fun drawHud(canvas: Canvas) {
        val surface = mixColor(IVORY, DEEP_NAVY, hudDeepNavyMix)
        val text = if (relativeLuminance(surface) < .45f) ON_DARK else INK
        val radius = dp(14f)

        paint.style = Paint.Style.FILL
        paint.color = surface
        paint.alpha = 235
        val topHud = RectF(dp(12f), dp(12f), width - dp(12f), dp(64f))
        canvas.drawRoundRect(topHud, radius, radius, paint)

        val bottomHud = RectF(dp(78f), height - dp(68f), width - dp(12f), height - dp(12f))
        canvas.drawRoundRect(bottomHud, radius, radius, paint)

        // Semantic information bars stand in for product copy; this is not production typography/layout.
        paint.color = text
        paint.alpha = 225
        canvas.drawRoundRect(RectF(dp(28f), dp(29f), width * .45f, dp(34f)), dp(2.5f), dp(2.5f), paint)
        paint.alpha = 145
        canvas.drawRoundRect(RectF(dp(28f), dp(43f), width * .32f, dp(47f)), dp(2f), dp(2f), paint)
        canvas.drawRoundRect(
            RectF(dp(94f), height - dp(49f), width * .70f, height - dp(44f)),
            dp(2.5f),
            dp(2.5f),
            paint,
        )
        paint.alpha = 255
    }

    private fun dp(value: Float): Float = value * density

    private fun mixColor(a: Int, b: Int, t: Float): Int {
        val p = t.coerceIn(0f, 1f)
        fun channel(color: Int, shift: Int) = (color shr shift) and 0xFF
        fun mix(x: Int, y: Int) = (x + (y - x) * p).toInt().coerceIn(0, 255)
        return Color.rgb(
            mix(channel(a, 16), channel(b, 16)),
            mix(channel(a, 8), channel(b, 8)),
            mix(channel(a, 0), channel(b, 0)),
        )
    }

    private fun relativeLuminance(color: Int): Float {
        fun linear(channel: Int): Double {
            val c = channel / 255.0
            return if (c <= .04045) c / 12.92 else Math.pow((c + .055) / 1.055, 2.4)
        }
        return (
            .2126 * linear(Color.red(color)) +
                .7152 * linear(Color.green(color)) +
                .0722 * linear(Color.blue(color))
            ).toFloat()
    }

    private companion object {
        val IVORY = Color.rgb(0xF7, 0xF3, 0xE8)
        val DEEP_NAVY = Color.rgb(0x1E, 0x2A, 0x44)
        val INK = Color.rgb(0x1A, 0x1F, 0x1C)
        val ON_DARK = Color.rgb(0xF7, 0xFA, 0xF5)
        val LEAF_PRIMARY = Color.rgb(0x6B, 0x8F, 0x7A)
        val BUTTER = Color.rgb(0xFF, 0xD3, 0x6B)
        val WARM_POINT = Color.rgb(0xFF, 0xBE, 0x68)
    }
}
