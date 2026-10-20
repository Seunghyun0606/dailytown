package com.dailytown.app.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapOverlayRuntimeTest {
    @Test
    fun decorativeDegradationNeverMutatesCriticalSemanticState() {
        val semantic = MapOverlaySemanticState(
            routeState = MapRouteVisualState.FOLLOWING,
            haloState = MapHaloVisualState.ACTIVE,
            discoveryIntensity = MapDiscoveryVisualIntensity.MEDIUM,
        )
        val plan = MapOverlayReadabilityPlanner.plan(
            semanticState = semantic,
            evidence = RbReadabilityEvidence(
                degradedDecorativeLayers = setOf(
                    RbDecorativeLayer.ATMOSPHERE,
                    RbDecorativeLayer.ACTIVE_GLOW,
                ),
            ),
        )

        assertEquals(RbReadabilityResult.PASS_WITH_DECORATIVE_DEGRADATION, plan.readabilityResult)
        assertEquals(semantic, plan.semanticState)
        assertFalse(plan.decorativeVisibility.atmosphere)
        assertFalse(plan.decorativeVisibility.activeGlow)
        assertTrue(plan.decorativeVisibility.discoveryDecoration)
    }

    @Test
    fun criticalFailureRemainsFailAndRemovesOptionalDecoration() {
        val semantic = MapOverlaySemanticState(
            routeState = MapRouteVisualState.FOLLOWING,
            haloState = MapHaloVisualState.STRONG,
        )
        val plan = MapOverlayReadabilityPlanner.plan(
            semanticState = semantic,
            evidence = RbReadabilityEvidence(
                unreadableCriticalLayers = setOf(RbCriticalLayer.PROVIDER_MAP_INFORMATION),
            ),
        )

        assertEquals(RbReadabilityResult.FAIL, plan.readabilityResult)
        assertEquals(semantic, plan.semanticState)
        assertFalse(plan.decorativeVisibility.atmosphere)
        assertFalse(plan.decorativeVisibility.activeGlow)
        assertFalse(plan.decorativeVisibility.discoveryDecoration)
    }
}
