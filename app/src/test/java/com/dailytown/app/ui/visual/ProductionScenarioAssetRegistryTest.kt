package com.dailytown.app.ui.visual

import com.dailytown.app.visual.OldGinkgoVisualAssets
import com.dailytown.app.visual.ScenarioAssetUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionScenarioAssetRegistryTest {
    @Test
    fun approvedOldGinkgoPackHasSevenUsageSpecificBindings() {
        val records = ProductionScenarioAssetRegistry.oldGinkgoBindings()

        assertEquals(ProductionScenarioAssetRegistry.PROMOTED_OLD_GINKGO_ASSET_COUNT, records.size)
        assertEquals(2, records.count { it.usage == ScenarioAssetUsage.CLUE_ART })
        assertEquals(3, records.count { it.semanticKey == OldGinkgoVisualAssets.PlaceMain })
        assertEquals(2, records.count { it.semanticKey == OldGinkgoVisualAssets.Keepsake })
        assertTrue(records.all { it.expectedSha256.length == 64 })
    }

    @Test
    fun resolverIsUsageSpecificAndFailsClosedForUnsupportedFallback() {
        assertEquals(
            "old-ginkgo/place-main-discovery-card.v3.webp",
            ProductionScenarioAssetRegistry.resolve(
                OldGinkgoVisualAssets.PlaceMain,
                ScenarioAssetUsage.DISCOVERY_CARD,
            )?.assetPath,
        )
        assertEquals(
            "old-ginkgo/memory-keepsake-companion-recent.v3.webp",
            ProductionScenarioAssetRegistry.resolve(
                OldGinkgoVisualAssets.Keepsake,
                ScenarioAssetUsage.COMPANION_RECENT_MEMORY,
            )?.assetPath,
        )
        assertNull(
            ProductionScenarioAssetRegistry.resolve(
                OldGinkgoVisualAssets.FoldedNote,
                ScenarioAssetUsage.RECORDS_HEADER,
            ),
        )
        assertNull(
            ProductionScenarioAssetRegistry.resolve(
                OldGinkgoVisualAssets.GinkgoLeaf,
                ScenarioAssetUsage.COMPANION_RECENT_MEMORY,
            ),
        )
    }

    @Test
    fun everyMechanicHasAllSevenUsageBindingsWithUniquePaths() {
        val all = ProductionScenarioAssetRegistry.records()
        assertEquals(
            ProductionScenarioAssetRegistry.PROMOTED_OLD_GINKGO_ASSET_COUNT + ProductionScenarioAssetRegistry.PROMOTED_MECHANIC_ASSET_COUNT,
            all.size,
        )
        assertEquals(all.size, all.map { it.assetPath }.toSet().size)
        com.dailytown.app.visual.MechanicScenarioArtCatalog.all().forEach { art ->
            listOf(
                art.place to ScenarioAssetUsage.DISCOVERY_CARD,
                art.place to ScenarioAssetUsage.RECORDS_HEADER,
                art.place to ScenarioAssetUsage.MEMORY_THUMBNAIL,
                art.memory to ScenarioAssetUsage.RECORDS_CARD,
                art.memory to ScenarioAssetUsage.COMPANION_RECENT_MEMORY,
                art.clueMain to ScenarioAssetUsage.CLUE_ART,
                art.clueKeepsake to ScenarioAssetUsage.CLUE_ART,
            ).forEach { (key, usage) ->
                assertTrue("${key.value}/$usage must be bound", ProductionScenarioAssetRegistry.resolve(key, usage) != null)
            }
        }
    }
}
