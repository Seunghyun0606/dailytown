package com.dailytown.app.visual

interface SemanticAssetAvailability {
    fun contains(key: SemanticAssetKey): Boolean
}

class SetSemanticAssetAvailability(keys: Iterable<SemanticAssetKey>) : SemanticAssetAvailability {
    private val values = keys.mapTo(hashSetOf()) { it.value }
    override fun contains(key: SemanticAssetKey): Boolean = key.value in values
}

enum class CompanionVisualFallback {
    EXPRESSION_TO_NEUTRAL,
    LIGHTING_TO_LIGHT,
    APPEARANCE_TO_BASE,
    ANIMATION_TO_STATIC_EXPRESSION,
}

data class CompanionVisualRequest(
    val companionId: String,
    val expression: CompanionExpression,
    val lightingFamily: CompanionLightingFamily,
    val appearanceProfile: AppearanceProfile = AppearanceProfile.BASE,
    val usageContext: CompanionUsageContext,
    val motion: CompanionMotion? = null,
    val reducedMotion: Boolean = false,
)

data class ResolvedCompanionVisual(
    val companionId: String,
    val expression: CompanionExpression,
    val lightingFamily: CompanionLightingFamily,
    val appearanceProfile: AppearanceProfile,
    val usageContext: CompanionUsageContext,
    val expressionAsset: SemanticAssetKey,
    val lightingAsset: SemanticAssetKey,
    val appearanceAsset: SemanticAssetKey?,
    val animationAsset: SemanticAssetKey?,
    val staticFallbackExpressionAsset: SemanticAssetKey,
    val fallbacks: Set<CompanionVisualFallback>,
)

/**
 * Resolves conceptual companion state to semantic layers only. File names and Android resources are adapter concerns.
 * Expression, lighting, affinity appearance and motion remain orthogonal dimensions.
 */
class CompanionAssetResolver(
    private val availability: SemanticAssetAvailability,
) {
    fun resolve(request: CompanionVisualRequest): ResolvedCompanionVisual {
        require(request.companionId.isNotBlank())
        val fallbacks = linkedSetOf<CompanionVisualFallback>()

        val requestedExpression = expressionKey(request.companionId, request.expression)
        val expression = if (availability.contains(requestedExpression)) {
            request.expression
        } else {
            fallbacks += CompanionVisualFallback.EXPRESSION_TO_NEUTRAL
            CompanionExpression.NEUTRAL
        }
        val expressionAsset = expressionKey(request.companionId, expression)

        val requestedLighting = lightingKey(request.companionId, request.lightingFamily)
        val lighting = if (availability.contains(requestedLighting)) {
            request.lightingFamily
        } else {
            fallbacks += CompanionVisualFallback.LIGHTING_TO_LIGHT
            CompanionLightingFamily.LIGHT
        }
        val lightingAsset = lightingKey(request.companionId, lighting)

        val requestedAppearance = appearanceKey(request.companionId, request.appearanceProfile)
        val baseAppearance = appearanceKey(request.companionId, AppearanceProfile.BASE)
        val appearanceAsset = when {
            availability.contains(requestedAppearance) -> requestedAppearance
            availability.contains(baseAppearance) -> {
                if (request.appearanceProfile != AppearanceProfile.BASE) fallbacks += CompanionVisualFallback.APPEARANCE_TO_BASE
                baseAppearance
            }
            else -> null // Luca currently has no separate appearance overlay; lighting/body remains authoritative.
        }
        val appearance = when {
            appearanceAsset == requestedAppearance -> request.appearanceProfile
            appearanceAsset == baseAppearance -> AppearanceProfile.BASE
            else -> AppearanceProfile.BASE
        }

        val requestedAnimation = request.motion?.let { animationKey(request.companionId, it) }
        val animationAsset = when {
            request.reducedMotion -> null
            requestedAnimation == null -> null
            request.motion == CompanionMotion.WALK -> null // experimental_only in M-B v1
            availability.contains(requestedAnimation) -> requestedAnimation
            else -> null
        }
        if (requestedAnimation != null && animationAsset == null) {
            fallbacks += CompanionVisualFallback.ANIMATION_TO_STATIC_EXPRESSION
        }

        return ResolvedCompanionVisual(
            companionId = request.companionId,
            expression = expression,
            lightingFamily = lighting,
            appearanceProfile = appearance,
            usageContext = request.usageContext,
            expressionAsset = expressionAsset,
            lightingAsset = lightingAsset,
            appearanceAsset = appearanceAsset,
            animationAsset = animationAsset,
            staticFallbackExpressionAsset = expressionAsset,
            fallbacks = fallbacks,
        )
    }

    companion object {
        fun expressionKey(companionId: String, expression: CompanionExpression) =
            SemanticAssetKey("companion.$companionId.expression.${expression.semantic}")

        fun lightingKey(companionId: String, lighting: CompanionLightingFamily) =
            SemanticAssetKey("lighting.$companionId.${lighting.name.lowercase()}")

        fun appearanceKey(companionId: String, profile: AppearanceProfile) =
            SemanticAssetKey("appearance.$companionId.${profile.semantic}")

        fun animationKey(companionId: String, motion: CompanionMotion) =
            SemanticAssetKey("animation.companion.$companionId.${motion.semantic}")
    }
}
