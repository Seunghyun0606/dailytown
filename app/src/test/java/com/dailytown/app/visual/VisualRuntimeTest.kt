package com.dailytown.app.visual

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualRuntimeTest {
    @Test
    fun forcedPhaseAndEveningStatesDoNotDependOnDeviceClock() {
        val resolver = DayPhaseResolver()
        assertEquals(DayPhase.NIGHT, resolver.resolve(LocalTime.NOON, VisualDebugOverride(forcedPhase = DayPhase.NIGHT)))
        EveningDebugState.entries.forEach { state ->
            val override = VisualDebugOverride(forcedEveningState = state)
            assertEquals(DayPhase.EVENING, resolver.resolve(LocalTime.MIDNIGHT, override))
            assertEquals(state.progress, resolver.eveningProgress(LocalTime.MIDNIGHT, override), 0f)
        }
    }

    @Test
    fun ev1UsesOnlyDayDarkAndWarmDuskDarkFamilies() {
        EveningDebugState.entries.forEach { state ->
            val profile = EveningVisualInterpolator.profile(state.progress, mapBackgroundLuminance = .5f)
            assertTrue(profile.markerFamily in setOf(MarkerFamily.DAY, MarkerFamily.DARK))
            assertTrue(profile.companionLighting in setOf(CompanionLightingFamily.WARM_DUSK, CompanionLightingFamily.DARK))
        }
        assertEquals(MarkerFamily.DARK, EveningVisualInterpolator.profile(.5f, .8f).markerFamily)
        assertEquals(MarkerFamily.DAY, EveningVisualInterpolator.profile(.5f, .2f).markerFamily)
        assertEquals(VisualArgb.rgb("#B090A2"), EveningVisualInterpolator.profile(.5f, .2f).route)
    }

    @Test
    fun companionFallbacksAreSemanticAndDoNotMutateIdentity() {
        val keys = mutableListOf<SemanticAssetKey>()
        listOf("moru", "luca").forEach { id ->
            keys += CompanionAssetResolver.expressionKey(id, CompanionExpression.NEUTRAL)
            keys += CompanionAssetResolver.lightingKey(id, CompanionLightingFamily.LIGHT)
        }
        keys += CompanionAssetResolver.appearanceKey("moru", AppearanceProfile.BASE)
        val resolver = CompanionAssetResolver(SetSemanticAssetAvailability(keys))
        val resolved = resolver.resolve(
            CompanionVisualRequest(
                companionId = "moru",
                expression = CompanionExpression.CLUE_FOUND,
                lightingFamily = CompanionLightingFamily.DARK,
                appearanceProfile = AppearanceProfile.BEST_FRIEND,
                usageContext = CompanionUsageContext.HUD_PORTRAIT,
                motion = CompanionMotion.CLUE_REACT,
            ),
        )
        assertEquals("moru", resolved.companionId)
        assertEquals(CompanionExpression.NEUTRAL, resolved.expression)
        assertEquals(CompanionLightingFamily.LIGHT, resolved.lightingFamily)
        assertEquals(AppearanceProfile.BASE, resolved.appearanceProfile)
        assertNull(resolved.animationAsset)
        assertTrue(CompanionVisualFallback.EXPRESSION_TO_NEUTRAL in resolved.fallbacks)
        assertTrue(CompanionVisualFallback.LIGHTING_TO_LIGHT in resolved.fallbacks)
        assertTrue(CompanionVisualFallback.APPEARANCE_TO_BASE in resolved.fallbacks)
        assertTrue(CompanionVisualFallback.ANIMATION_TO_STATIC_EXPRESSION in resolved.fallbacks)
    }

    @Test
    fun affinityAndCompanionSwapPreserveRequestedSemantics() {
        val keys = buildList {
            listOf("moru", "luca").forEach { id ->
                CompanionExpression.entries.forEach { add(CompanionAssetResolver.expressionKey(id, it)) }
                CompanionLightingFamily.entries.forEach { add(CompanionAssetResolver.lightingKey(id, it)) }
            }
            AppearanceProfile.entries.forEach { add(CompanionAssetResolver.appearanceKey("moru", it)) }
        }
        val resolver = CompanionAssetResolver(SetSemanticAssetAvailability(keys))
        AppearanceProfile.entries.forEach { profile ->
            val value = resolver.resolve(
                CompanionVisualRequest("moru", CompanionExpression.RESOLVED, CompanionLightingFamily.DARK, profile, CompanionUsageContext.RESULT_LARGE),
            )
            assertEquals("moru", value.companionId)
            assertEquals(CompanionExpression.RESOLVED, value.expression)
            assertEquals(CompanionLightingFamily.DARK, value.lightingFamily)
            assertEquals(profile, value.appearanceProfile)
        }
        val luca = resolver.resolve(
            CompanionVisualRequest("luca", CompanionExpression.RESOLVED, CompanionLightingFamily.DARK, AppearanceProfile.BASE, CompanionUsageContext.RESULT_LARGE),
        )
        assertEquals("luca", luca.companionId)
        assertEquals(CompanionExpression.RESOLVED, luca.expression)
        assertEquals(CompanionLightingFamily.DARK, luca.lightingFamily)
        assertFalse(luca.expressionAsset.value.contains("moru"))
    }

    @Test
    fun markerAnchorAndA3PaperStayStableAcrossVariants() {
        val markerEntries = buildList {
            MarkerFamily.entries.forEach { family -> MarkerSemantic.entries.forEach { semantic -> add(family to semantic.key) } }
        }
        val resolver = MarkerAssetResolver(SetMarkerAssetAvailability(markerEntries))
        val base = resolver.resolve(MarkerSemantic.ENCOUNTER_ACTIVE, MarkerFamily.DAY, selected = false)
        val selected = resolver.resolve(MarkerSemantic.ENCOUNTER_ACTIVE, MarkerFamily.DARK, selected = true)
        assertEquals(base.anchorX, selected.anchorX, 0f)
        assertEquals(base.anchorY, selected.anchorY, 0f)

        DayPhase.entries.forEach {
            val spec = A3AssetResolver.resolve(A3Screen.JOURNAL_HOME, 360, reducedMotion = true)
            assertTrue(SemanticAssetKey("surface.journal.paper") in spec.assets)
            assertEquals(A3MotionTreatment.STATIC_REDUCED, spec.motionTreatment)
        }
        assertEquals(2, A3AssetResolver.resolve(A3Screen.COLLECTION_GRID, 360).columns)
        assertEquals(2, A3AssetResolver.resolve(A3Screen.COLLECTION_GRID, 412).columns)
        assertEquals(3, A3AssetResolver.resolve(A3Screen.COLLECTION_GRID, 600).columns)
        assertTrue(A3AssetResolver.supportedStampSizeDp(32))
        assertTrue(A3AssetResolver.supportedStampSizeDp(64))
    }
}
