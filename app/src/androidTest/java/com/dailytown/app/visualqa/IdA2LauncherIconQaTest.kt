package com.dailytown.app.visualqa

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Development-owned launcher QA for the locked ID-A2 Sprout Gate identity
 * (design/review/final-human-gates-v3/id-a/ID_A_ICON_LOCK_REVIEW.md, "Small-size gate").
 * Pass `dailytownCapture=true` to also write review PNGs to the app's external files dir.
 */
@RunWith(AndroidJUnit4::class)
class IdA2LauncherIconQaTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val capture = InstrumentationRegistry.getArguments().getString("dailytownCapture") == "true"

    @Test
    fun applicationIconIsTheAdaptiveIdA2Icon() {
        val icon = context.packageManager.getApplicationIcon(context.packageName)
        assertTrue("launcher icon must be adaptive", icon is AdaptiveIconDrawable)
        icon as AdaptiveIconDrawable
        assertNotNull(icon.foreground)
        assertNotNull(icon.background)
        if (Build.VERSION.SDK_INT >= 33) assertNotNull("themed-icon monochrome layer", icon.monochrome)
    }

    @Test
    fun foregroundMarkStaysInsideTheCentredSafeZone() {
        val px = 216 // 2x of the 108-unit canvas
        val bitmap = render(drawable(R.drawable.ic_launcher_foreground), px)
        val bounds = opaqueBounds(bitmap)
        val safeMin = px * 21 / 108
        val safeMax = px * 87 / 108
        assertTrue("mark must not touch the mask area: $bounds", bounds[0] >= safeMin && bounds[1] >= safeMin)
        assertTrue("mark must not touch the mask area: $bounds", bounds[2] <= safeMax && bounds[3] <= safeMax)
        val markHeightUnits = (bounds[3] - bounds[1]) * 108f / px
        assertTrue("core mark should use the 48-66 unit target, was $markHeightUnits", markHeightUnits in 48f..66f)
        save("foreground-216", bitmap)
    }

    @Test
    fun lightAndDarkLayersUseTheLockedPalette() {
        assertEquals(Color.parseColor("#F4EBDD"), backgroundColor(Configuration.UI_MODE_NIGHT_NO))
        assertEquals(Color.parseColor("#2D342C"), backgroundColor(Configuration.UI_MODE_NIGHT_YES))
    }

    @Test
    fun maskedIconStaysRecognizableAt48And32And24Pixels() {
        listOf(Configuration.UI_MODE_NIGHT_NO to "light", Configuration.UI_MODE_NIGHT_YES to "dark").forEach { (mode, label) ->
            val themed = configured(mode)
            val icon = ContextCompat.getDrawable(themed, R.mipmap.ic_launcher) as AdaptiveIconDrawable
            listOf(48, 32, 24).forEach { size ->
                val bitmap = render(icon, size)
                // Inside any launcher mask and above the sprout (leaf tops start at 24.5% of the canvas).
                val background = bitmap.getPixel(size / 2, size * 12 / 100)
                val markPixels = countDiffering(bitmap, background)
                assertTrue("$label ${size}px: mark collapsed ($markPixels px)", markPixels >= size * size / 12)
                save("launcher-$label-$size", bitmap)
            }
        }
    }

    @Test
    fun notificationIconIsAWhiteSilhouetteThatFillsItsFrame() {
        val px = 72
        val bitmap = render(drawable(R.drawable.ic_stat_daily_town), px)
        val bounds = opaqueBounds(bitmap)
        assertTrue("mark should fill most of the 24dp frame: $bounds", bounds[3] - bounds[1] >= px * 0.8f)
        for (y in 0 until px) for (x in 0 until px) {
            val color = bitmap.getPixel(x, y)
            if (Color.alpha(color) > 0) {
                assertEquals("status-bar icons must be white silhouettes", 0xFFFFFF, color and 0xFFFFFF)
            }
        }
        save("notification-72", bitmap)
    }

    private fun configured(nightMode: Int): Context {
        val config = Configuration(context.resources.configuration)
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        return context.createConfigurationContext(config)
    }

    private fun backgroundColor(nightMode: Int): Int {
        val icon = ContextCompat.getDrawable(configured(nightMode), R.mipmap.ic_launcher) as AdaptiveIconDrawable
        return (icon.background as ColorDrawable).color
    }

    private fun drawable(id: Int): Drawable = requireNotNull(ContextCompat.getDrawable(context, id))

    private fun render(drawable: Drawable, px: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, px, px)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    /** [left, top, right, bottom] of pixels with alpha > 0, inclusive. */
    private fun opaqueBounds(bitmap: Bitmap): IntArray {
        var left = bitmap.width
        var top = bitmap.height
        var right = -1
        var bottom = -1
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if (Color.alpha(bitmap.getPixel(x, y)) > 0) {
                left = minOf(left, x); top = minOf(top, y); right = maxOf(right, x); bottom = maxOf(bottom, y)
            }
        }
        assertTrue("drawable rendered empty", right >= 0)
        return intArrayOf(left, top, right, bottom)
    }

    private fun countDiffering(bitmap: Bitmap, background: Int): Int {
        var count = 0
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            val color = bitmap.getPixel(x, y)
            if (Color.alpha(color) == 255 && colorDistance(color, background) > 60) count++
        }
        return count
    }

    private fun colorDistance(a: Int, b: Int): Int =
        Math.abs(Color.red(a) - Color.red(b)) + Math.abs(Color.green(a) - Color.green(b)) + Math.abs(Color.blue(a) - Color.blue(b))

    private fun save(name: String, bitmap: Bitmap) {
        if (!capture) return
        val dir = File(context.getExternalFilesDir(null), "captures/id-a2").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
