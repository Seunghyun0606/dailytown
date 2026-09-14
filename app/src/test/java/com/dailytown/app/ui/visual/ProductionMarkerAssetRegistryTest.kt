package com.dailytown.app.ui.visual

import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertEquals
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
    fun productionSingletonContainsExactDayDarkMarkerMatrixAfterPromotion() {
        val records = ProductionMarkerAssetRegistry.records()
        assertEquals(24, ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT)
        assertEquals(24, records.size)
        assertEquals(12, records.count { it.family == MarkerFamily.DAY })
        assertEquals(12, records.count { it.family == MarkerFamily.DARK })

        MarkerFamily.entries.forEach { family ->
            MarkerSemantic.entries.forEach { semantic ->
                assertTrue(
                    "Missing production marker for $family/${semantic.key.value}",
                    ProductionMarkerAssetRegistry.contains(family, semantic.key),
                )
            }
        }
    }
}
