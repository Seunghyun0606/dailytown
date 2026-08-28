package com.dailytown.app.ui.visual

import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionMarkerAssetRegistryTest {
    private val active = MarkerSemantic.ENCOUNTER_ACTIVE.key

    @Test
    fun sameSemanticKeyCanExistInDayAndDarkWithoutCollision() {
        val index = MarkerProductionAssetIndex(
            listOf(
                ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "day/day-encounter-active.v1.svg"),
                ProductionMarkerAssetRecord(MarkerFamily.DARK, active, "dark/dark-encounter-active.v1.svg"),
            ),
        )

        assertEquals(2, index.records().size)
        assertTrue(index.contains(MarkerFamily.DAY, active))
        assertTrue(index.contains(MarkerFamily.DARK, active))
        assertEquals("day/day-encounter-active.v1.svg", index.require(MarkerFamily.DAY, active).assetPath)
        assertEquals("dark/dark-encounter-active.v1.svg", index.require(MarkerFamily.DARK, active).assetPath)
    }

    @Test
    fun duplicateFamilySemanticPairIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            MarkerProductionAssetIndex(
                listOf(
                    ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "day/one.svg"),
                    ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "day/two.svg"),
                ),
            )
        }
    }

    @Test
    fun unsafeOrCrossFamilyPathsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            MarkerProductionAssetIndex(
                listOf(ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "../day/active.svg")),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MarkerProductionAssetIndex(
                listOf(ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "dark/active.svg")),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MarkerProductionAssetIndex(
                listOf(ProductionMarkerAssetRecord(MarkerFamily.DAY, active, "day/active.png")),
            )
        }
    }

    @Test
    fun productionSingletonRemainsEmptyUntilExplicitPromotion() {
        assertEquals(0, ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT)
        assertTrue(ProductionMarkerAssetRegistry.records().isEmpty())
        assertFalse(ProductionMarkerAssetRegistry.contains(MarkerFamily.DAY, active))
        assertFalse(ProductionMarkerAssetRegistry.contains(MarkerFamily.DARK, active))
    }
}
