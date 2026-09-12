package com.dailytown.app.visual

/**
 * Provider-neutral contract for the approved map-overlay QA matrix.
 *
 * This file contains semantic QA dimensions only. It deliberately does not define production
 * pixel sizes, animation timing, provider coordinates, or raw asset names.
 */
enum class MapQaTimeAnchor(val phase: DayPhase, val semantic: String) {
    MORNING(DayPhase.MORNING, "MORNING"),
    SUNSET(DayPhase.SUNSET, "SUNSET"),
    NIGHT(DayPhase.NIGHT, "NIGHT"),
}

enum class MapQaComplexity(val semantic: String) {
    SIMPLE_RESIDENTIAL("simple_residential"),
    DENSE_URBAN("dense_urban"),
    MIXED_POI("mixed_poi"),
}

enum class MapQaMotionMode(val semantic: String, val reducedMotion: Boolean) {
    NORMAL("normal", false),
    REDUCED_MOTION("reduced_motion", true),
}

data class MapOverlayQaCase(
    val timeAnchor: MapQaTimeAnchor,
    val mapComplexity: MapQaComplexity,
    val motionMode: MapQaMotionMode,
) {
    val id: String = listOf(timeAnchor.semantic, mapComplexity.semantic, motionMode.semantic)
        .joinToString(".")
        .lowercase()
}

object MapOverlayQaMatrix {
    const val EXPECTED_BASELINE_CAPTURE_COUNT = 18

    val baselineCases: List<MapOverlayQaCase> = buildList {
        MapQaTimeAnchor.values().forEach { timeAnchor ->
            MapQaComplexity.values().forEach { mapComplexity ->
                MapQaMotionMode.values().forEach { motionMode ->
                    add(MapOverlayQaCase(timeAnchor, mapComplexity, motionMode))
                }
            }
        }
    }.also { cases ->
        check(cases.size == EXPECTED_BASELINE_CAPTURE_COUNT)
        check(cases.map { it.id }.toSet().size == EXPECTED_BASELINE_CAPTURE_COUNT)
    }
}

/** R-B result vocabulary from the approved design handoff. */
enum class RbReadabilityResult {
    PASS,
    PASS_WITH_DECORATIVE_DEGRADATION,
    FAIL,
}

/**
 * Layers that must remain readable. A failure here cannot be hidden by reducing decoration.
 * Order follows the approved map-overlay priority hierarchy.
 */
enum class RbCriticalLayer {
    USER_LOCATION_NAVIGATION,
    ACTIVE_ROUTE,
    ACTIVE_ENCOUNTER,
    PROVIDER_MAP_INFORMATION,
    COMPANION_PRESENCE,
    SECONDARY_GAMEPLAY_MARKERS,
}

/** Decorative layers are the only layers that may be sacrificed to preserve critical semantics. */
enum class RbDecorativeLayer {
    ATMOSPHERE,
    ACTIVE_GLOW,
    DISCOVERY_DECORATION,
}

data class RbReadabilityEvidence(
    val unreadableCriticalLayers: Set<RbCriticalLayer> = emptySet(),
    val degradedDecorativeLayers: Set<RbDecorativeLayer> = emptySet(),
)

object RbReadabilityClassifier {
    /** Reduce decoration in this order before touching gameplay semantics or provider information. */
    val decorativeDegradationOrder: List<RbDecorativeLayer> = listOf(
        RbDecorativeLayer.ATMOSPHERE,
        RbDecorativeLayer.ACTIVE_GLOW,
        RbDecorativeLayer.DISCOVERY_DECORATION,
    )

    fun classify(evidence: RbReadabilityEvidence): RbReadabilityResult = when {
        evidence.unreadableCriticalLayers.isNotEmpty() -> RbReadabilityResult.FAIL
        evidence.degradedDecorativeLayers.isNotEmpty() -> RbReadabilityResult.PASS_WITH_DECORATIVE_DEGRADATION
        else -> RbReadabilityResult.PASS
    }
}
