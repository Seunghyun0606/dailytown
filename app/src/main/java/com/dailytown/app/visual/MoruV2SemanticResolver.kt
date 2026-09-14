package com.dailytown.app.visual

/**
 * Versioned Moru runtime profiles. The legacy profile remains the default and terminal rollback path
 * until Android QA plus the physical-device Human Gate are complete.
 */
enum class CompanionRuntimeProfile(val semantic: String) {
    LEGACY_V1("legacy/current-v1"),
    MORU_CANONICAL_V2("companion.moru.canonical.v2"),
    ;

    companion object {
        fun fromSemantic(value: String?): CompanionRuntimeProfile? =
            entries.singleOrNull { it.semantic == value }
    }
}

object CompanionRuntimeProfileSelector {
    const val DEBUG_INTENT_EXTRA = "com.dailytown.app.extra.COMPANION_RUNTIME_PROFILE"
    val defaultProfile: CompanionRuntimeProfile = CompanionRuntimeProfile.LEGACY_V1

    /** Release builds fail closed to v1. Debug builds may explicitly opt into v2 for emulator QA. */
    fun select(debugBuild: Boolean, requestedSemantic: String?): CompanionRuntimeProfile {
        if (!debugBuild) return defaultProfile
        return CompanionRuntimeProfile.fromSemantic(requestedSemantic) ?: defaultProfile
    }
}

data class MoruV2SemanticKey(
    val usage: CompanionUsageContext,
    val expression: CompanionExpression,
    val lighting: CompanionLightingFamily,
    val affinity: AppearanceProfile,
) {
    val value: String =
        "companion/moru/v2/${usage.semantic}/${expression.semantic}/${lighting.name}/${affinity.semantic}"

    override fun toString(): String = value
}

fun interface MoruV2SemanticAvailability {
    fun contains(key: MoruV2SemanticKey): Boolean
}

object CompleteMoruV2SemanticAvailability : MoruV2SemanticAvailability {
    override fun contains(key: MoruV2SemanticKey): Boolean = true
}

enum class MoruV2Fallback {
    SAME_CONTEXT_EXPRESSION_LIGHT_BASE,
    SAME_CONTEXT_NEUTRAL_LIGHT_BASE,
    MOTION_TO_STATIC,
    LEGACY_V1_TERMINAL,
}

sealed interface MoruV2Resolution {
    data class Raster(
        val key: MoruV2SemanticKey,
        val fallbacks: List<MoruV2Fallback>,
    ) : MoruV2Resolution

    data class LegacyV1(
        val fallbacks: List<MoruV2Fallback>,
    ) : MoruV2Resolution

    data object Invalid : MoruV2Resolution
}

/**
 * Mirrors the design-side MORU-03 resolver contract:
 * exact -> same-context expression/LIGHT/base -> same-context neutral/LIGHT/base -> legacy v1.
 * Motion is intentionally not a semantic axis in the 432-key v2 raster family and resolves static.
 */
class MoruV2SemanticResolver(
    private val availability: MoruV2SemanticAvailability,
) {
    fun resolve(request: CompanionVisualRequest): MoruV2Resolution {
        if (request.companionId != "moru") return MoruV2Resolution.Invalid
        return resolve(
            usage = request.usageContext,
            expression = request.expression,
            lighting = request.lightingFamily,
            affinity = request.appearanceProfile,
            requestedMotion = request.motion,
        )
    }

    fun resolve(
        usage: CompanionUsageContext,
        expression: CompanionExpression,
        lighting: CompanionLightingFamily,
        affinity: AppearanceProfile,
        requestedMotion: CompanionMotion? = null,
    ): MoruV2Resolution {
        val motionFallback = if (requestedMotion != null) listOf(MoruV2Fallback.MOTION_TO_STATIC) else emptyList()
        val exact = MoruV2SemanticKey(usage, expression, lighting, affinity)
        if (availability.contains(exact)) return MoruV2Resolution.Raster(exact, motionFallback)

        val expressionLightBase = MoruV2SemanticKey(
            usage = usage,
            expression = expression,
            lighting = CompanionLightingFamily.LIGHT,
            affinity = AppearanceProfile.BASE,
        )
        if (availability.contains(expressionLightBase)) {
            return MoruV2Resolution.Raster(
                expressionLightBase,
                motionFallback + MoruV2Fallback.SAME_CONTEXT_EXPRESSION_LIGHT_BASE,
            )
        }

        val neutralLightBase = MoruV2SemanticKey(
            usage = usage,
            expression = CompanionExpression.NEUTRAL,
            lighting = CompanionLightingFamily.LIGHT,
            affinity = AppearanceProfile.BASE,
        )
        if (availability.contains(neutralLightBase)) {
            return MoruV2Resolution.Raster(
                neutralLightBase,
                motionFallback +
                    MoruV2Fallback.SAME_CONTEXT_EXPRESSION_LIGHT_BASE +
                    MoruV2Fallback.SAME_CONTEXT_NEUTRAL_LIGHT_BASE,
            )
        }

        return MoruV2Resolution.LegacyV1(
            motionFallback +
                MoruV2Fallback.SAME_CONTEXT_EXPRESSION_LIGHT_BASE +
                MoruV2Fallback.SAME_CONTEXT_NEUTRAL_LIGHT_BASE +
                MoruV2Fallback.LEGACY_V1_TERMINAL,
        )
    }

    /** String boundary used by manifests/debug tooling. Unknown values never fall back silently. */
    fun resolveExternal(
        usage: String,
        expression: String,
        lighting: String,
        affinity: String,
    ): MoruV2Resolution {
        val parsedUsage = CompanionUsageContext.entries.singleOrNull { it.semantic == usage }
            ?: return MoruV2Resolution.Invalid
        val parsedExpression = CompanionExpression.entries.singleOrNull { it.semantic == expression }
            ?: return MoruV2Resolution.Invalid
        val parsedLighting = CompanionLightingFamily.entries.singleOrNull { it.name == lighting }
            ?: return MoruV2Resolution.Invalid
        val parsedAffinity = AppearanceProfile.entries.singleOrNull { it.semantic == affinity }
            ?: return MoruV2Resolution.Invalid
        return resolve(parsedUsage, parsedExpression, parsedLighting, parsedAffinity)
    }
}

object MoruV2SemanticContract {
    val allKeys: List<MoruV2SemanticKey> = buildList {
        CompanionUsageContext.entries.forEach { usage ->
            CompanionExpression.entries.forEach { expression ->
                CompanionLightingFamily.entries.forEach { lighting ->
                    AppearanceProfile.entries.forEach { affinity ->
                        add(MoruV2SemanticKey(usage, expression, lighting, affinity))
                    }
                }
            }
        }
    }

    const val EXPECTED_KEY_COUNT = 432
}
