package com.dailytown.app.visual

import com.dailytown.app.mystery.EncounterPhase

/**
 * Provider-neutral semantic state for live-map overlays.
 *
 * This deliberately contains no px sizes, animation timing, provider coordinates, or raw assets.
 * A provider adapter may render only the dimensions for which an approved visual implementation
 * exists. Missing visual implementation must not mutate gameplay semantics.
 */
enum class MapRouteVisualState {
    IDLE,
    FOLLOWING,
    COMPLETED,
}

enum class MapHaloVisualState {
    IDLE,
    ACTIVE,
    STRONG,
}

enum class MapDiscoveryVisualIntensity {
    SMALL,
    MEDIUM,
    BIG,
}

data class MapOverlaySemanticState(
    val routeState: MapRouteVisualState = MapRouteVisualState.IDLE,
    val haloState: MapHaloVisualState = MapHaloVisualState.IDLE,
    val discoveryIntensity: MapDiscoveryVisualIntensity? = null,
    val reducedMotion: Boolean = false,
)

data class MapDecorativeVisibility(
    val atmosphere: Boolean = true,
    val activeGlow: Boolean = true,
    val discoveryDecoration: Boolean = true,
)

data class MapOverlayRenderPlan(
    val semanticState: MapOverlaySemanticState,
    val decorativeVisibility: MapDecorativeVisibility,
    val readabilityResult: RbReadabilityResult,
)

/**
 * Applies the approved R-B hierarchy without changing route/encounter/marker semantics.
 *
 * If critical readability has already failed, all optional decoration is removed but the result
 * remains FAIL. This is a safety fallback, not a way to turn a critical failure into PASS.
 */
object MapOverlayReadabilityPlanner {
    fun plan(
        semanticState: MapOverlaySemanticState,
        evidence: RbReadabilityEvidence = RbReadabilityEvidence(),
    ): MapOverlayRenderPlan {
        val result = RbReadabilityClassifier.classify(evidence)
        val degraded = evidence.degradedDecorativeLayers
        val failClosed = result == RbReadabilityResult.FAIL
        return MapOverlayRenderPlan(
            semanticState = semanticState,
            decorativeVisibility = MapDecorativeVisibility(
                atmosphere = !failClosed && RbDecorativeLayer.ATMOSPHERE !in degraded,
                activeGlow = !failClosed && RbDecorativeLayer.ACTIVE_GLOW !in degraded,
                discoveryDecoration = !failClosed && RbDecorativeLayer.DISCOVERY_DECORATION !in degraded,
            ),
            readabilityResult = result,
        )
    }
}

data class EncounterMapVisualState(
    val markerSemantic: MarkerSemantic?,
    val selected: Boolean,
    val overlays: MapOverlaySemanticState,
)

/**
 * Maps gameplay state to visual semantics only.
 *
 * HIDDEN encounters remain absent from the map. The current product does not yet own a route
 * geometry/navigation source, so this resolver never fabricates a FOLLOWING route. Discovery
 * animation intensity is also left unset until an authored gameplay mapping is approved.
 */
object EncounterMapVisualResolver {
    fun resolve(
        phase: EncounterPhase,
        isRevisit: Boolean,
        reducedMotion: Boolean = false,
    ): EncounterMapVisualState = when (phase) {
        EncounterPhase.HIDDEN -> EncounterMapVisualState(
            markerSemantic = null,
            selected = false,
            overlays = MapOverlaySemanticState(reducedMotion = reducedMotion),
        )
        EncounterPhase.HINTED -> EncounterMapVisualState(
            markerSemantic = if (isRevisit) MarkerSemantic.ENCOUNTER_REVISIT else MarkerSemantic.ENCOUNTER_HINTED,
            selected = false,
            overlays = MapOverlaySemanticState(
                haloState = MapHaloVisualState.IDLE,
                reducedMotion = reducedMotion,
            ),
        )
        EncounterPhase.DISCOVERED -> EncounterMapVisualState(
            markerSemantic = if (isRevisit) MarkerSemantic.ENCOUNTER_REVISIT else MarkerSemantic.ENCOUNTER_ACTIVE,
            selected = true,
            overlays = MapOverlaySemanticState(
                haloState = MapHaloVisualState.ACTIVE,
                reducedMotion = reducedMotion,
            ),
        )
        EncounterPhase.RESOLVED -> EncounterMapVisualState(
            markerSemantic = MarkerSemantic.ENCOUNTER_SOLVED,
            selected = false,
            overlays = MapOverlaySemanticState(reducedMotion = reducedMotion),
        )
    }
}
