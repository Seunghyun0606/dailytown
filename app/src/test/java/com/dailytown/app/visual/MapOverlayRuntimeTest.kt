package com.dailytown.app.visual

import com.dailytown.app.mystery.EncounterPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapOverlayRuntimeTest {
    @Test
    fun hiddenEncounterDoesNotExposeMarker() {
        val state = EncounterMapVisualResolver.resolve(EncounterPhase.HIDDEN, isRevisit = false)
        assertNull(state.markerSemantic)
        assertFalse(state.selected)
        assertEquals(MapHaloVisualState.IDLE, state.overlays.haloState)
        assertEquals(MapRouteVisualState.IDLE, state.overlays.routeState)
    }

    @Test
    fun encounterPhasesMapToStableSemanticMarkers() {
        val hinted = EncounterMapVisualResolver.resolve(EncounterPhase.HINTED, isRevisit = false)
        val discovered = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, isRevisit = false)
        val resolved = EncounterMapVisualResolver.resolve(EncounterPhase.RESOLVED, isRevisit = false)

        assertEquals(MarkerSemantic.ENCOUNTER_HINTED, hinted.markerSemantic)
        assertEquals(MarkerSemantic.ENCOUNTER_ACTIVE, discovered.markerSemantic)
        assertTrue(discovered.selected)
        assertEquals(MapHaloVisualState.ACTIVE, discovered.overlays.haloState)
        assertEquals(MarkerSemantic.ENCOUNTER_SOLVED, resolved.markerSemantic)
        assertFalse(resolved.selected)
    }

    @Test
    fun revisitRemainsNonColorSemanticBeforeResolution() {
        val hinted = EncounterMapVisualResolver.resolve(EncounterPhase.HINTED, isRevisit = true)
        val discovered = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, isRevisit = true)

        assertEquals(MarkerSemantic.ENCOUNTER_REVISIT, hinted.markerSemantic)
        assertEquals(MarkerSemantic.ENCOUNTER_REVISIT, discovered.markerSemantic)
        assertTrue(discovered.selected)
    }

    @Test
    fun resolverNeverFabricatesNavigationRouteOrDiscoveryIntensity() {
        EncounterPhase.values().forEach { phase ->
            val state = EncounterMapVisualResolver.resolve(phase, isRevisit = false)
            assertEquals(MapRouteVisualState.IDLE, state.overlays.routeState)
            assertNull(state.overlays.discoveryIntensity)
        }
    }

    @Test
    fun reducedMotionIsPropagatedWithoutChangingSemanticMarker() {
        val normal = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, false, reducedMotion = false)
        val reduced = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, false, reducedMotion = true)

        assertEquals(normal.markerSemantic, reduced.markerSemantic)
        assertEquals(normal.selected, reduced.selected)
        assertEquals(normal.overlays.haloState, reduced.overlays.haloState)
        assertFalse(normal.overlays.reducedMotion)
        assertTrue(reduced.overlays.reducedMotion)
    }

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
