package com.dailytown.app.visualqa

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
import org.junit.Assert.assertNull
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
    fun familyAwareRendererCanRenderCurrentApprovedShapeWithoutPromotingIt() {
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
    fun wiredProductionSourceStaysFailClosedWhileRegistryIsEmpty() {
        val source = ProductionMarkerSvgVisualSource(AndroidProductionMarkerAssetCatalog(targetAssets))
        val rendered = source.resolve(marker(), MapThemeSpec(markerFamily = MarkerFamily.DAY))

        assertEquals(0, ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT)
        assertTrue(ProductionMarkerAssetRegistry.records().isEmpty())
        assertNull(rendered)
    }

    private fun candidateCatalog() = ProductionMarkerSvgCatalog { record ->
        candidateAssets.open("markers/v1/${record.assetPath}")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    private fun marker(selected: Boolean = false) = MapMarkerSpec(
        id = "marker-runtime-boundary",
        title = "QA",
        position = GeoPoint(37.5665, 126.9780),
        semantic = MarkerSemantic.ENCOUNTER_ACTIVE,
        selected = selected,
    )

    private fun hasOpaquePixel(bitmap: android.graphics.Bitmap): Boolean {
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
        }
        return false
    }
}
