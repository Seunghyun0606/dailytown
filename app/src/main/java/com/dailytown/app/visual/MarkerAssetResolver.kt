package com.dailytown.app.visual

interface MarkerAssetAvailability {
    fun contains(family: MarkerFamily, key: SemanticAssetKey): Boolean
}

class SetMarkerAssetAvailability(entries: Iterable<Pair<MarkerFamily, SemanticAssetKey>>) : MarkerAssetAvailability {
    private val values = entries.mapTo(hashSetOf()) { it.first to it.second.value }
    override fun contains(family: MarkerFamily, key: SemanticAssetKey): Boolean = family to key.value in values
}

enum class MarkerVisualFallback { POI_TO_OTHER, DARK_TO_DAY }

data class ResolvedMarkerAsset(
    val requestedSemantic: MarkerSemantic,
    val semanticKey: SemanticAssetKey,
    val family: MarkerFamily,
    val anchorX: Float = GEO_ANCHOR_X,
    val anchorY: Float = GEO_ANCHOR_Y,
    val selected: Boolean = false,
    val fallbacks: Set<MarkerVisualFallback> = emptySet(),
) {
    companion object {
        const val GEO_ANCHOR_X = .5f
        const val GEO_ANCHOR_Y = .96875f
    }
}

/** Selected/active treatment never changes the canonical geographic anchor. */
class MarkerAssetResolver(
    private val availability: MarkerAssetAvailability,
) {
    fun resolve(semantic: MarkerSemantic, family: MarkerFamily, selected: Boolean = false): ResolvedMarkerAsset {
        val fallbacks = linkedSetOf<MarkerVisualFallback>()
        var key = semantic.key
        if (!availability.contains(family, key) && semantic.name.startsWith("POI_") && semantic != MarkerSemantic.POI_OTHER) {
            key = MarkerSemantic.POI_OTHER.key
            fallbacks += MarkerVisualFallback.POI_TO_OTHER
        }
        var resolvedFamily = family
        if (!availability.contains(resolvedFamily, key) && resolvedFamily == MarkerFamily.DARK && availability.contains(MarkerFamily.DAY, key)) {
            resolvedFamily = MarkerFamily.DAY
            fallbacks += MarkerVisualFallback.DARK_TO_DAY
        }
        return ResolvedMarkerAsset(
            requestedSemantic = semantic,
            semanticKey = key,
            family = resolvedFamily,
            selected = selected,
            fallbacks = fallbacks,
        )
    }
}
