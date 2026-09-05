package com.dailytown.app.visualqa

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.ui.visual.AndroidProductionMarkerAssetCatalog
import com.dailytown.app.ui.visual.MarkerProductionAssetIndex
import com.dailytown.app.ui.visual.ProductionMarkerAssetRecord
import com.dailytown.app.ui.visual.ProductionMarkerAssetRegistry
import com.dailytown.app.ui.visual.ProductionMarkerSvgCatalog
import com.dailytown.app.ui.visual.ProductionMarkerSvgVisualSource
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import com.dailytown.app.visual.ResolvedMarkerAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductionMarkerRuntimeBoundaryTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val candidateAssets = instrumentation.context.assets
    private val targetAssets = instrumentation.targetContext.assets
    private val active = MarkerSemantic.ENCOUNTER_ACTIVE.key

    @Test
    fun familyAwareRendererCanRenderCurrentApprovedShape() {
        val index = MarkerProductionAssetIndex(
            listOf(
                ProductionMarkerAssetRecord(
                    family = MarkerFamily.DAY,
                    semanticKey = active,
                    assetPath = "day/day-encounter-active.v1.svg",
                ),
            ),
        )
        val source = ProductionMarkerSvgVisualSource(candidateCatalog(), index)
        val rendered = source.resolve(marker(selected = true), MapThemeSpec(markerFamily = MarkerFamily.DAY))

        assertNotNull(rendered)
        val value = requireNotNull(rendered)
        assertEquals(96, value.bitmap.width)
        assertEquals(128, value.bitmap.height)
        assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_X, value.anchorX, 0f)
        assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_Y, value.anchorY, 0f)
        assertTrue(hasOpaquePixel(value.bitmap))
    }

    @Test
    fun darkRequestUsesResolverDayFallbackWhenOnlyDayRecordExists() {
        val index = MarkerProductionAssetIndex(
            listOf(
                ProductionMarkerAssetRecord(
                    family = MarkerFamily.DAY,
                    semanticKey = active,
                    assetPath = "day/day-encounter-active.v1.svg",
                ),
            ),
        )
        val rendered = ProductionMarkerSvgVisualSource(candidateCatalog(), index)
            .resolve(marker(), MapThemeSpec(markerFamily = MarkerFamily.DARK))

        assertNotNull(rendered)
        val value = requireNotNull(rendered)
        assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_X, value.anchorX, 0f)
        assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_Y, value.anchorY, 0f)
    }

    @Test
    fun promotedProductionSourceRendersExactDayDarkMatrixWithoutNegativeViewBoxClipping() {
        val records = ProductionMarkerAssetRegistry.records()
        assertEquals(24, ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT)
        assertEquals(24, records.size)
        assertEquals(12, records.count { it.family == MarkerFamily.DAY })
        assertEquals(12, records.count { it.family == MarkerFamily.DARK })

        val source = ProductionMarkerSvgVisualSource(AndroidProductionMarkerAssetCatalog(targetAssets))
        MarkerFamily.entries.forEach { family ->
            MarkerSemantic.entries.forEach { semantic ->
                val record = ProductionMarkerAssetRegistry.resolve(family, semantic.key)
                assertNotNull("Missing production record for $family/${semantic.key.value}", record)

                val rendered = requireNotNull(
                    source.resolve(
                        marker(semantic = semantic, selected = semantic == MarkerSemantic.ENCOUNTER_ACTIVE),
                        MapThemeSpec(markerFamily = family),
                    ),
                )
                assertEquals(96, rendered.bitmap.width)
                assertEquals(128, rendered.bitmap.height)
                assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_X, rendered.anchorX, 0f)
                assertEquals(ResolvedMarkerAsset.GEO_ANCHOR_Y, rendered.anchorY, 0f)
                assertTrue(hasOpaquePixel(rendered.bitmap))

                val bounds = opaqueBounds(rendered.bitmap)
                assertTrue("Production marker clipped at left edge: $family/${semantic.key.value} bounds=$bounds", bounds.left > 0)
                assertTrue("Production marker clipped at top edge: $family/${semantic.key.value} bounds=$bounds", bounds.top > 0)
            }
        }
    }

    private fun candidateCatalog() = ProductionMarkerSvgCatalog { record ->
        candidateAssets.open("markers/v1/${record.assetPath}")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    private fun marker(
        semantic: MarkerSemantic = MarkerSemantic.ENCOUNTER_ACTIVE,
        selected: Boolean = false,
    ) = MapMarkerSpec(
        id = "marker-runtime-boundary",
        title = "QA",
        position = GeoPoint(37.5665, 126.9780),
        semantic = semantic,
        selected = selected,
    )

    private fun hasOpaquePixel(bitmap: android.graphics.Bitmap): Boolean {
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }

    private fun opaqueBounds(bitmap: android.graphics.Bitmap): Rect {
        var left = bitmap.width
        var top = bitmap.height
        var right = -1
        var bottom = -1
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) {
                left = minOf(left, x)
                top = minOf(top, y)
                right = maxOf(right, x)
                bottom = maxOf(bottom, y)
            }
        }
        check(right >= left && bottom >= top) { "Production marker rendered fully transparent" }
        return Rect(left, top, right + 1, bottom + 1)
    }
}
