package com.dailytown.app.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoruV2SemanticResolverTest {
    @Test
    fun semanticContractContainsAll432UniqueKeys() {
        val keys = MoruV2SemanticContract.allKeys
        assertEquals(MoruV2SemanticContract.EXPECTED_KEY_COUNT, keys.size)
        assertEquals(MoruV2SemanticContract.EXPECTED_KEY_COUNT, keys.map { it.value }.toSet().size)
        assertEquals(6, CompanionExpression.entries.size)
        assertEquals(3, CompanionLightingFamily.entries.size)
        assertEquals(4, AppearanceProfile.entries.size)
        assertEquals(6, CompanionUsageContext.entries.size)
    }

    @Test
    fun exactResolutionCoversAll432Combinations() {
        val resolver = MoruV2SemanticResolver(CompleteMoruV2SemanticAvailability)
        MoruV2SemanticContract.allKeys.forEach { key ->
            val resolved = resolver.resolve(key.usage, key.expression, key.lighting, key.affinity)
            assertEquals(MoruV2Resolution.Raster(key, emptyList()), resolved)
        }
    }

    @Test
    fun singleMissingExactUsesDesignFallbackOrderForAll432Requests() {
        MoruV2SemanticContract.allKeys.forEach { requested ->
            val availability = MoruV2SemanticAvailability { it != requested }
            val resolved = MoruV2SemanticResolver(availability).resolve(
                requested.usage,
                requested.expression,
                requested.lighting,
                requested.affinity,
            )
            val expressionLightBase = requested.copy(
                lighting = CompanionLightingFamily.LIGHT,
                affinity = AppearanceProfile.BASE,
            )
            val neutralLightBase = MoruV2SemanticKey(
                requested.usage,
                CompanionExpression.NEUTRAL,
                CompanionLightingFamily.LIGHT,
                AppearanceProfile.BASE,
            )
            when {
                expressionLightBase != requested -> {
                    val raster = resolved as MoruV2Resolution.Raster
                    assertEquals(expressionLightBase, raster.key)
                    assertTrue(MoruV2Fallback.SAME_CONTEXT_EXPRESSION_LIGHT_BASE in raster.fallbacks)
                }
                neutralLightBase != requested -> {
                    val raster = resolved as MoruV2Resolution.Raster
                    assertEquals(neutralLightBase, raster.key)
                    assertTrue(MoruV2Fallback.SAME_CONTEXT_NEUTRAL_LIGHT_BASE in raster.fallbacks)
                }
                else -> {
                    assertTrue(resolved is MoruV2Resolution.LegacyV1)
                }
            }
        }
    }

    @Test
    fun cascadeFallbackReachesNeutralThenLegacy() {
        val requested = MoruV2SemanticKey(
            CompanionUsageContext.HUD_PORTRAIT,
            CompanionExpression.HAPPY,
            CompanionLightingFamily.DARK,
            AppearanceProfile.TRUSTED,
        )
        val expressionLightBase = requested.copy(
            lighting = CompanionLightingFamily.LIGHT,
            affinity = AppearanceProfile.BASE,
        )
        val neutralLightBase = MoruV2SemanticKey(
            requested.usage,
            CompanionExpression.NEUTRAL,
            CompanionLightingFamily.LIGHT,
            AppearanceProfile.BASE,
        )

        val neutralResult = MoruV2SemanticResolver(
            MoruV2SemanticAvailability { it != requested && it != expressionLightBase },
        ).resolve(requested.usage, requested.expression, requested.lighting, requested.affinity)
        val neutralRaster = neutralResult as MoruV2Resolution.Raster
        assertEquals(neutralLightBase, neutralRaster.key)
        assertEquals(
            listOf(
                MoruV2Fallback.SAME_CONTEXT_EXPRESSION_LIGHT_BASE,
                MoruV2Fallback.SAME_CONTEXT_NEUTRAL_LIGHT_BASE,
            ),
            neutralRaster.fallbacks,
        )

        val legacyResult = MoruV2SemanticResolver(
            MoruV2SemanticAvailability {
                it != requested && it != expressionLightBase && it != neutralLightBase
            },
        ).resolve(requested.usage, requested.expression, requested.lighting, requested.affinity)
        val legacy = legacyResult as MoruV2Resolution.LegacyV1
        assertEquals(MoruV2Fallback.LEGACY_V1_TERMINAL, legacy.fallbacks.last())
    }

    @Test
    fun motionAlwaysFailsBackToStaticSemanticRaster() {
        val resolver = MoruV2SemanticResolver(CompleteMoruV2SemanticAvailability)
        val result = resolver.resolve(
            usage = CompanionUsageContext.MAP_AVATAR,
            expression = CompanionExpression.CLUE_FOUND,
            lighting = CompanionLightingFamily.WARM_DUSK,
            affinity = AppearanceProfile.BEST_FRIEND,
            requestedMotion = CompanionMotion.CLUE_REACT,
        ) as MoruV2Resolution.Raster
        assertEquals(listOf(MoruV2Fallback.MOTION_TO_STATIC), result.fallbacks)
    }

    @Test
    fun invalidExternalSemanticInputFailsClosed() {
        val resolver = MoruV2SemanticResolver(CompleteMoruV2SemanticAvailability)
        assertEquals(MoruV2Resolution.Invalid, resolver.resolveExternal("bad", "neutral", "LIGHT", "base"))
        assertEquals(MoruV2Resolution.Invalid, resolver.resolveExternal("map_avatar", "bad", "LIGHT", "base"))
        assertEquals(MoruV2Resolution.Invalid, resolver.resolveExternal("map_avatar", "neutral", "bad", "base"))
        assertEquals(MoruV2Resolution.Invalid, resolver.resolveExternal("map_avatar", "neutral", "LIGHT", "bad"))
    }

    @Test
    fun profileSelectionKeepsV1AsDefaultAndRollback() {
        assertEquals(CompanionRuntimeProfile.LEGACY_V1, CompanionRuntimeProfileSelector.defaultProfile)
        assertEquals(
            CompanionRuntimeProfile.MORU_CANONICAL_V2,
            CompanionRuntimeProfileSelector.select(true, CompanionRuntimeProfile.MORU_CANONICAL_V2.semantic),
        )
        assertEquals(
            CompanionRuntimeProfile.LEGACY_V1,
            CompanionRuntimeProfileSelector.select(true, CompanionRuntimeProfile.LEGACY_V1.semantic),
        )
        assertEquals(
            CompanionRuntimeProfile.LEGACY_V1,
            CompanionRuntimeProfileSelector.select(true, "unknown-profile"),
        )
        assertEquals(
            CompanionRuntimeProfile.LEGACY_V1,
            CompanionRuntimeProfileSelector.select(false, CompanionRuntimeProfile.MORU_CANONICAL_V2.semantic),
        )
        assertFalse(CompanionRuntimeProfileSelector.defaultProfile == CompanionRuntimeProfile.MORU_CANONICAL_V2)
        assertNotEquals(CompanionRuntimeProfile.LEGACY_V1.semantic, CompanionRuntimeProfile.MORU_CANONICAL_V2.semantic)
    }
}
