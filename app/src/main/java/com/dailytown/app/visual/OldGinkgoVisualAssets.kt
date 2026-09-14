package com.dailytown.app.visual

enum class ScenarioAssetUsage {
    CLUE_ART,
    DISCOVERY_CARD,
    RECORDS_HEADER,
    MEMORY_THUMBNAIL,
    RECORDS_CARD,
    COMPANION_RECENT_MEMORY,
}

/** Approved semantic keys for the Old Ginkgo first UX-validation scenario. */
object OldGinkgoVisualAssets {
    val FoldedNote = SemanticAssetKey("clue.old_ginkgo.folded_note")
    val GinkgoLeaf = SemanticAssetKey("clue.old_ginkgo.ginkgo_leaf")
    val PlaceMain = SemanticAssetKey("place.old_ginkgo.main")
    val Keepsake = SemanticAssetKey("memory.old_ginkgo.keepsake")
}
