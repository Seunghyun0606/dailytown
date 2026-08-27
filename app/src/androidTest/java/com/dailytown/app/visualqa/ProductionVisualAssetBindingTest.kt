package com.dailytown.app.visualqa

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.ui.visual.AndroidProductionMarkerAssetCatalog
import com.dailytown.app.ui.visual.AndroidProductionVisualAssetCatalog
import com.dailytown.app.ui.visual.ProductionA3SvgRenderer
import com.dailytown.app.ui.visual.ProductionCompanionCanvasRenderer
import com.dailytown.app.ui.visual.ProductionMarkerAssetRegistry
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
import java.io.IOException
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductionVisualAssetBindingTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetAssets = instrumentation.targetContext.assets
    private val candidateCatalog by lazy { CandidateAssetCatalog(instrumentation.context.assets) }
    private val productionCatalog by lazy { AndroidProductionVisualAssetCatalog(targetAssets) }
    private val productionMarkerCatalog by lazy { AndroidProductionMarkerAssetCatalog(targetAssets) }

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
    fun productionMarkerTargetApkBindingIsCandidateExcludedOrChecksumExactAfterPromotion() {
        val markerCandidates = candidateCatalog.allEntries()
            .filter { entry -> entry.semanticKey.startsWith("marker.") }
        assertEquals(24, markerCandidates.size)

        val candidateByPair = markerCandidates.groupBy { entry ->
            val family = entry.family ?: error("Marker candidate missing family: ${entry.semanticKey}")
            family to entry.semanticKey
        }
        assertEquals("DAY/DARK marker candidate pairs must stay unique", 24, candidateByPair.size)

        val productionRecords = ProductionMarkerAssetRegistry.records()
        assertEquals(ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT, productionRecords.size)
        assertTrue(
            "Marker promotion must be atomic: only 0 or 24 production records are valid",
            ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT == 0 ||
                ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT == 24,
        )

        if (ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT == 0) {
            assertTrue(productionRecords.isEmpty())
            markerCandidates.forEach { candidate ->
                assertTrue(
                    "Marker candidate path must stay under markers/v1 before promotion: ${candidate.assetPath}",
                    candidate.assetPath.startsWith("markers/v1/"),
                )
                val runtimePath = candidate.assetPath.removePrefix("markers/v1/")
                assertThrows(
                    "Candidate marker leaked into target APK: $runtimePath",
                    IOException::class.java,
                ) {
                    targetAssets.open(runtimePath).use { it.readBytes() }
                }
            }
            return
        }

        assertEquals(24, productionRecords.size)
        productionRecords.forEach { record ->
            val expected = candidateByPair[record.family.name to record.semanticKey.value]
                ?.singleOrNull()
                ?: error("Expected one marker candidate for ${record.family}/${record.semanticKey.value}")
            assertTrue(
                "Marker candidate path must stay under markers/v1: ${expected.assetPath}",
                expected.assetPath.startsWith("markers/v1/"),
            )
            val expectedRuntimePath = expected.assetPath.removePrefix("markers/v1/")
            assertEquals(
                "Production marker runtime path changed for ${record.family}/${record.semanticKey.value}",
                expectedRuntimePath,
                record.assetPath,
            )
            val bytes = productionMarkerCatalog.open(record).use { it.readBytes() }
            assertEquals(
                "Marker checksum mismatch for ${record.family}/${record.semanticKey.value}",
                expected.sha256,
                sha256(bytes),
            )
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

    @Test
    fun productionA3RendererCoversPromotedFamilyAndRejectsOtherFamilies() {
        val renderer = ProductionA3SvgRenderer(productionCatalog)
        val a3Records = ProductionVisualAssetRegistry.records()
            .filter { it.family == ProductionVisualAssetRegistry.Family.A3 }

        assertEquals(ProductionVisualAssetRegistry.PROMOTED_A3_COUNT, a3Records.size)
        a3Records.forEach { record ->
            val bitmap = renderer.render(record.semanticKey, widthPx = 440, heightPx = 560)
            assertTrue(
                "Production A-3 renderer output too sparse for ${record.semanticKey.value}",
                opaquePixelRatio(bitmap) > .005,
            )
        }

        val companionKey = ProductionVisualAssetRegistry.records()
            .first { it.family == ProductionVisualAssetRegistry.Family.COMPANION }
            .semanticKey
        assertThrows(IllegalStateException::class.java) {
            renderer.render(companionKey, widthPx = 96, heightPx = 96)
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
