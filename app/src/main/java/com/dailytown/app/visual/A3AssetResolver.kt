package com.dailytown.app.visual

enum class A3Screen { JOURNAL_HOME, DISCOVERY_DETAIL, CLUE_NOTE, COLLECTION_GRID, MEMORY_DETAIL }
enum class A3ClueState { UNRESOLVED, RESOLVED }
enum class A3MotionTreatment { NATIVE_SOFT, STATIC_REDUCED }

data class A3ScreenVisualSpec(
    val screen: A3Screen,
    val assets: List<SemanticAssetKey>,
    val columns: Int = 1,
    val horizontalPaddingDp: Int = 16,
    val componentGapDp: Int = 12,
    val companionStampDp: Int = 48,
    val motionTreatment: A3MotionTreatment,
)

/** A-3 paper is deliberately independent from [DayPhase]. */
object A3AssetResolver {
    fun resolve(
        screen: A3Screen,
        widthDp: Int,
        clueState: A3ClueState = A3ClueState.UNRESOLVED,
        reducedMotion: Boolean = false,
    ): A3ScreenVisualSpec {
        require(widthDp > 0)
        val motion = if (reducedMotion) A3MotionTreatment.STATIC_REDUCED else A3MotionTreatment.NATIVE_SOFT
        val assets = when (screen) {
            A3Screen.JOURNAL_HOME -> listOf(
                "surface.journal.paper", "sticker.discovery.default", "stamp.companion.default",
            )
            A3Screen.DISCOVERY_DETAIL -> listOf(
                "surface.journal.paper", "sticker.discovery.default", "stamp.companion.default",
            )
            A3Screen.CLUE_NOTE -> listOf(
                "surface.journal.paper",
                if (clueState == A3ClueState.RESOLVED) "card.clue.resolved" else "card.clue.unresolved",
                "stamp.companion.default",
            )
            A3Screen.COLLECTION_GRID -> listOf(
                "surface.collection.paper", "collection.locked.pattern", "sticker.discovery.default",
            )
            A3Screen.MEMORY_DETAIL -> listOf(
                "surface.memory.paper", "stamp.memory.resolved", "stamp.companion.default",
            )
        }.map(::SemanticAssetKey)
        val columns = if (screen == A3Screen.COLLECTION_GRID) {
            when {
                widthDp >= 600 -> 3
                else -> 2
            }
        } else 1
        return A3ScreenVisualSpec(
            screen = screen,
            assets = assets,
            columns = columns,
            motionTreatment = motion,
        )
    }

    fun supportedStampSizeDp(sizeDp: Int): Boolean = sizeDp in 32..64
}
