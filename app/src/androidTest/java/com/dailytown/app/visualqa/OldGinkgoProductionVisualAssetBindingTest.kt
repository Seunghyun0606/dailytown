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
import com.dailytown.app.ui.visual.AndroidProductionScenarioAssetCatalog
import com.dailytown.app.ui.visual.ProductionScenarioAssetRegistry
import com.dailytown.app.visual.OldGinkgoVisualAssets
import com.dailytown.app.visual.ScenarioAssetUsage
import com.dailytown.app.visual.SemanticAssetKey
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OldGinkgoProductionVisualAssetBindingTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val catalog = AndroidProductionScenarioAssetCatalog(instrumentation.targetContext.assets)

    @Test
    fun packagedOldGinkgoRuntimeBytesMatchApprovedDesignChecksumsAndDimensions() {
        val records = ProductionScenarioAssetRegistry.records()
        assertEquals(ProductionScenarioAssetRegistry.PROMOTED_OLD_GINKGO_ASSET_COUNT, records.size)

        records.forEach { record ->
            val bytes = catalog.open(record).use { it.readBytes() }
            assertEquals(
                "Packaged checksum mismatch for ${record.semanticKey.value}/${record.usage}",
                record.expectedSha256,
                sha256(bytes),
            )
            val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) {
                "Android could not decode ${record.assetPath}"
            }
            assertEquals(record.widthPx, bitmap.width)
            assertEquals(record.heightPx, bitmap.height)
            if (record.hasAlpha) {
                assertTrue("Expected alpha for ${record.assetPath}", bitmap.hasAlpha())
                assertTransparentCorners(bitmap, record.assetPath)
            }
        }

        assertNull(
            ProductionScenarioAssetRegistry.resolve(
                OldGinkgoVisualAssets.FoldedNote,
                ScenarioAssetUsage.RECORDS_HEADER,
            ),
        )
    }

    @Test
    fun packagedCluesRenderInCreamDarkAndMapHeavyContexts() {
        val note = decode(OldGinkgoVisualAssets.FoldedNote, ScenarioAssetUsage.CLUE_ART)
        val leaf = decode(OldGinkgoVisualAssets.GinkgoLeaf, ScenarioAssetUsage.CLUE_ART)

        renderContextBoard(note, "folded-note").writeToTestStorage("visual/old-ginkgo-runtime/folded-note-contexts")
        renderContextBoard(leaf, "ginkgo-leaf").writeToTestStorage("visual/old-ginkgo-runtime/ginkgo-leaf-contexts")
    }

    private fun decode(key: SemanticAssetKey, usage: ScenarioAssetUsage): Bitmap =
        catalog.open(key, usage).use { stream -> BitmapFactory.decodeStream(stream) }
            ?: error("Unable to decode ${key.value}/$usage")

    private fun assertTransparentCorners(bitmap: Bitmap, label: String) {
        val corners = listOf(
            bitmap.getPixel(0, 0),
            bitmap.getPixel(bitmap.width - 1, 0),
            bitmap.getPixel(0, bitmap.height - 1),
            bitmap.getPixel(bitmap.width - 1, bitmap.height - 1),
        )
        assertTrue("$label corners must remain transparent", corners.all { Color.alpha(it) == 0 })
    }

    private fun renderContextBoard(source: Bitmap, label: String): Bitmap {
        val width = 960
        val rowHeight = 360
        val board = Bitmap.createBitmap(width, rowHeight * 3, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(board)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        ContextSurface.entries.forEachIndexed { index, surface ->
            val top = index * rowHeight
            paint.color = surface.background
            canvas.drawRect(0f, top.toFloat(), width.toFloat(), (top + rowHeight).toFloat(), paint)
            if (surface == ContextSurface.MAP_HEAVY) {
                paint.color = Color.rgb(244, 239, 229)
                paint.strokeWidth = 44f
                canvas.drawLine(0f, (top + 160).toFloat(), width.toFloat(), (top + 245).toFloat(), paint)
                paint.color = Color.rgb(170, 185, 168)
                paint.strokeWidth = 5f
                canvas.drawLine(0f, (top + 140).toFloat(), width.toFloat(), (top + 225).toFloat(), paint)
            }
            val target = 288f
            canvas.drawBitmap(
                source,
                null,
                RectF(36f, top + 36f, 36f + target, top + 36f + target),
                paint,
            )
            paint.color = surface.label
            paint.textSize = 30f
            canvas.drawText("$label · ${surface.name.lowercase()}", 360f, (top + 90).toFloat(), paint)
        }
        return board
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private enum class ContextSurface(val background: Int, val label: Int) {
        CREAM(Color.rgb(255, 248, 235), Color.rgb(69, 59, 48)),
        DARK(Color.rgb(31, 40, 34), Color.rgb(244, 235, 221)),
        MAP_HEAVY(Color.rgb(221, 231, 220), Color.rgb(69, 59, 48)),
    }
}
