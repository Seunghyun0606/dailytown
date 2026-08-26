package com.dailytown.app.visualqa

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.ui.visual.AndroidProductionVisualAssetCatalog
import com.dailytown.app.ui.visual.ProductionCompanionCanvasRenderer
import com.dailytown.app.ui.visual.ProductionVisualAssetRegistry
import com.dailytown.app.visual.A3AssetResolver
import com.dailytown.app.visual.A3MotionTreatment
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualFallback
import com.dailytown.app.visual.CompanionVisualRequest
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductionVisualAssetBindingTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetAssets = instrumentation.targetContext.assets
    private val candidateCatalog by lazy { CandidateAssetCatalog(instrumentation.context.assets) }
    private val productionCatalog by lazy { AndroidProductionVisualAssetCatalog(targetAssets) }

    @Test
    fun promotedCompanionAndA3AssetsArePackagedWithAuthoritativeChecksums() {
        val records = ProductionVisualAssetRegistry.records()
        assertEquals(ProductionVisualAssetRegistry.PROMOTED_ASSET_COUNT, records.size)
        assertEquals(
            ProductionVisualAssetRegistry.PROMOTED_COMPANION_COUNT,
            records.count { it.family == ProductionVisualAssetRegistry.Family.COMPANION },
        )
        assertEquals(
            ProductionVisualAssetRegistry.PROMOTED_A3_COUNT,
            records.count { it.family == ProductionVisualAssetRegistry.Family.A3 },
        )
        assertFalse(records.any { it.semanticKey.value.startsWith("marker.") })

        val candidateBySemantic = candidateCatalog.allEntries()
            .filter { entry -> !entry.semanticKey.startsWith("marker.") }
            .groupBy { it.semanticKey }

        records.forEach { record ->
            val expected = candidateBySemantic[record.semanticKey.value]
                ?.singleOrNull()
                ?: error("Expected one promoted candidate for ${record.semanticKey.value}")
            val bytes = productionCatalog.open(record.semanticKey).use { it.readBytes() }
            assertEquals("Checksum mismatch for ${record.semanticKey.value}", expected.sha256, sha256(bytes))
            val svg = bytes.toString(Charsets.UTF_8)
            assertTrue("Semantic metadata missing from packaged SVG: ${record.semanticKey.value}", svg.contains(record.semanticKey.value))
        }
    }

    @Test
    fun productionSemanticResolversPreserveCompanionStateAndA3Fallbacks() {
        val companionResolver = CompanionAssetResolver(ProductionVisualAssetRegistry)

        listOf("moru", "luca").forEach { companionId ->
            CompanionExpression.entries.forEach { expression ->
                CompanionLightingFamily.entries.forEach { lighting ->
                    val resolved = companionResolver.resolve(
                        CompanionVisualRequest(
                            companionId = companionId,
                            expression = expression,
                            lightingFamily = lighting,
                            usageContext = CompanionUsageContext.HUD_PORTRAIT,
                        ),
                    )
                    assertEquals(companionId, resolved.companionId)
                    assertEquals(expression, resolved.expression)
                    assertEquals(lighting, resolved.lightingFamily)
                    assertFalse(CompanionVisualFallback.EXPRESSION_TO_NEUTRAL in resolved.fallbacks)
                    assertFalse(CompanionVisualFallback.LIGHTING_TO_LIGHT in resolved.fallbacks)
                    assertNotNull(ProductionVisualAssetRegistry.resolve(resolved.expressionAsset))
                    assertNotNull(ProductionVisualAssetRegistry.resolve(resolved.lightingAsset))
                }
            }
        }

        AppearanceProfile.entries.forEach { appearance ->
            val resolved = companionResolver.resolve(
                CompanionVisualRequest(
                    companionId = "moru",
                    expression = CompanionExpression.CURIOUS,
                    lightingFamily = CompanionLightingFamily.WARM_DUSK,
                    appearanceProfile = appearance,
                    usageContext = CompanionUsageContext.RESULT_LARGE,
                ),
            )
            assertEquals(appearance, resolved.appearanceProfile)
            assertFalse(CompanionVisualFallback.APPEARANCE_TO_BASE in resolved.fallbacks)
            assertNotNull(resolved.appearanceAsset)
            assertNotNull(ProductionVisualAssetRegistry.resolve(resolved.appearanceAsset!!))
        }

        A3Screen.entries.forEach { screen ->
            val normal = A3AssetResolver.resolve(screen, widthDp = 412, reducedMotion = false)
            val reduced = A3AssetResolver.resolve(screen, widthDp = 412, reducedMotion = true)
            assertEquals(A3MotionTreatment.STATIC_REDUCED, reduced.motionTreatment)
            assertEquals(normal.assets, reduced.assets)
            normal.assets.forEach { key ->
                assertTrue("A3 semantic key not promoted: ${key.value}", ProductionVisualAssetRegistry.contains(key))
                assertNotNull(productionCatalog.readSvg(key))
            }
        }
    }

    @Test
    fun productionCompanionCanvasRendererCoversPromotedExpressionLightingMatrix() {
        val resolver = CompanionAssetResolver(ProductionVisualAssetRegistry)
        val renderer = ProductionCompanionCanvasRenderer(productionCatalog)

        listOf("moru", "luca").forEach { companionId ->
            CompanionExpression.entries.forEach { expression ->
                CompanionLightingFamily.entries.forEach { lighting ->
                    val resolved = resolver.resolve(
                        CompanionVisualRequest(
                            companionId = companionId,
                            expression = expression,
                            lightingFamily = lighting,
                            usageContext = CompanionUsageContext.HUD_PORTRAIT,
                        ),
                    )
                    val bitmap = renderer.render(resolved, targetPx = 96)
                    assertTrue(
                        "Production renderer output too sparse for $companionId/$expression/$lighting",
                        opaquePixelRatio(bitmap) > .03,
                    )
                }
            }
        }

        AppearanceProfile.entries.forEach { appearance ->
            val resolved = resolver.resolve(
                CompanionVisualRequest(
                    companionId = "moru",
                    expression = CompanionExpression.NEUTRAL,
                    lightingFamily = CompanionLightingFamily.WARM_DUSK,
                    appearanceProfile = appearance,
                    usageContext = CompanionUsageContext.RESULT_LARGE,
                ),
            )
            assertTrue("Moru affinity render is transparent: $appearance", opaquePixelRatio(renderer.render(resolved, 128)) > .03)
        }
    }

    private fun opaquePixelRatio(bitmap: Bitmap): Double {
        var opaque = 0
        val total = bitmap.width * bitmap.height
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) opaque++
        }
        return opaque.toDouble() / total.coerceAtLeast(1)
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
