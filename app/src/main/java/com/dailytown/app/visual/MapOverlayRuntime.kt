package com.dailytown.app.visual

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
