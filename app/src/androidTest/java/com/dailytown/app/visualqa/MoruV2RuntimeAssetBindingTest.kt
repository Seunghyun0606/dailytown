package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.ui.visual.AndroidMoruV2RuntimeAssetCatalog
import com.dailytown.app.ui.visual.MoruV2RasterRenderer
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.MoruV2SemanticContract
import com.dailytown.app.visual.MoruV2SemanticKey
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoruV2RuntimeAssetBindingTest {
    private val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
    private val catalog by lazy { AndroidMoruV2RuntimeAssetCatalog(assets) }

    @Test
    fun packagedAuthoritiesAndNineSourceRastersMatchDeclaredBinaryIntegrity() {
        assertEquals("4e53243fe01df54ae4b85d8504c678cf6a81d7af", catalog.designAuthorityHead())
        assertEquals(9, catalog.sourceRecords().size)
        assertEquals(4, catalog.authorityRecords().size)

        catalog.authorityRecords().forEach { record ->
            val actual = catalog.openAuthority(record).use { sha256(it.readBytes()) }
            assertEquals("authority bytes ${record.assetPath}", record.expectedSha256, actual)
        }

        val semanticFamily = assets.open("moru-v2/authority/manifest.native-semantic.v1.json")
            .bufferedReader().use { JSONObject(it.readText()) }
        catalog.sourceRecords().forEach { record ->
            val actual = catalog.openSource(record).use { sha256(it.readBytes()) }
            assertEquals("source bytes ${record.assetPath}", record.expectedSha256, actual)
            val designRecord = when (record.kind) {
                "expression" -> semanticFamily.getJSONObject("expressions").getJSONObject(record.semantic)
                "affinity" -> semanticFamily.getJSONObject("affinity").getJSONObject(record.semantic)
                else -> error("Unexpected Moru v2 source kind ${record.kind}")
            }
            assertEquals("design SHA ${record.semantic}", designRecord.getString("sha256"), record.expectedSha256)
            assertTrue("source authority must point at design reference", record.sourceAuthority.startsWith("design/reference/moru-v2/"))
            assertEquals(
                "source authority filename ${record.semantic}",
                designRecord.getString("path").substringAfterLast('/'),
                record.sourceAuthority.substringAfterLast('/'),
            )
            val bitmap = catalog.openSource(record).use { BitmapFactory.decodeStream(it) }
            requireNotNull(bitmap) { "decode failed for ${record.assetPath}" }
            assertEquals(record.width, bitmap.width)
            assertEquals(record.height, bitmap.height)
            assertTrue("${record.assetPath} must retain alpha", bitmap.hasAlpha())
            val alpha = alphaRange(bitmap)
            assertEquals("${record.assetPath} transparent alpha", 0, alpha.first)
            assertTrue("${record.assetPath} must contain visible alpha", alpha.second > 0)
            if (record.kind == "expression") assertEquals("${record.assetPath} opaque alpha", 255, alpha.second)
            bitmap.recycle()
        }
    }

    @Test
    fun packagedResolverManifestIsThe432KeyAuthorityAndUsageDimensionsMatch() {
        val expectations = catalog.semanticExpectations()
        assertEquals(MoruV2SemanticContract.EXPECTED_KEY_COUNT, expectations.size)
        assertEquals(
            MoruV2SemanticContract.allKeys.map { it.value }.toSet(),
            expectations.map { it.key }.toSet(),
        )
        assertTrue(expectations.all { it.rgbaPixelSha256.length == 64 && it.alphaPixelSha256.length == 64 })

        CompanionUsageContext.entries.forEach { usage ->
            val transform = catalog.usageTransform(usage)
            val expectation = requireNotNull(
                catalog.expectation(
                    MoruV2SemanticKey(
                        usage,
                        CompanionExpression.NEUTRAL,
                        CompanionLightingFamily.LIGHT,
                        AppearanceProfile.BASE,
                    ),
                ),
            )
            assertEquals("usage width ${usage.semantic}", expectation.width, transform.outputWidth)
            assertEquals("usage height ${usage.semantic}", expectation.height, transform.outputHeight)
        }
    }

    @Test
    fun representativeRuntimeMaterializationUsesExpectedAspectAndAllLightingFamilies() {
        val renderer = MoruV2RasterRenderer(catalog)
        CompanionUsageContext.entries.forEach { usage ->
            val key = MoruV2SemanticKey(
                usage,
                CompanionExpression.NEUTRAL,
                CompanionLightingFamily.LIGHT,
                AppearanceProfile.BASE,
            )
            val expected = requireNotNull(catalog.expectation(key))
            val bitmap = renderer.render(key, 192)
            val scale = 192.0 / maxOf(expected.width, expected.height).toDouble()
            assertEquals((expected.width * scale).toIntWithTolerance(), bitmap.width)
            assertEquals((expected.height * scale).toIntWithTolerance(), bitmap.height)
            assertTrue("${usage.semantic} must have visible pixels", alphaRange(bitmap).second > 0)
        }

        CompanionLightingFamily.entries.forEach { lighting ->
            val bitmap = renderer.render(
                MoruV2SemanticKey(
                    CompanionUsageContext.HUD_PORTRAIT,
                    CompanionExpression.CLUE_FOUND,
                    lighting,
                    AppearanceProfile.BEST_FRIEND,
                ),
                192,
            )
            assertTrue(alphaRange(bitmap).second > 0)
            bitmap.writeToTestStorage("visual/moru-v2/runtime/hud-clue-found-${lighting.name.lowercase()}")
        }
    }

    private fun alphaRange(bitmap: Bitmap): Pair<Int, Int> {
        var min = 255
        var max = 0
        val row = IntArray(bitmap.width)
        for (y in 0 until bitmap.height) {
            bitmap.getPixels(row, 0, bitmap.width, 0, y, bitmap.width, 1)
            row.forEach { pixel ->
                val alpha = pixel ushr 24 and 0xFF
                min = minOf(min, alpha)
                max = maxOf(max, alpha)
            }
        }
        return min to max
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun Double.toIntWithTolerance(): Int = kotlin.math.round(this).toInt()
}
