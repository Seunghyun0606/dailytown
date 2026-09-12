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
        val records = ProductionScenarioAssetRegistry.records()

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
}
