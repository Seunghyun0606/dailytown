package com.dailytown.app.visual

data class MotionPlaybackRequest(
    val companionId: String,
    val motion: CompanionMotion,
    val currentExpression: CompanionExpression,
    val reducedMotion: Boolean,
)

data class ResolvedMotionPlayback(
    val animationAsset: SemanticAssetKey?,
    val staticExpressionAsset: SemanticAssetKey,
    val experimental: Boolean,
    val requiresHumanTimingApproval: Boolean,
)

/**
 * Runtime consumes an explicitly authored atlas/manifest only. sprite-gen is never an Android dependency.
 * No product timing/easing values are defined here because M-B timing/intensity is human-gated.
 */
class MotionAssetResolver(
    private val availability: SemanticAssetAvailability,
) {
    fun resolve(request: MotionPlaybackRequest): ResolvedMotionPlayback {
        val staticAsset = CompanionAssetResolver.expressionKey(request.companionId, request.currentExpression)
        val animation = CompanionAssetResolver.animationKey(request.companionId, request.motion)
        val experimental = request.motion == CompanionMotion.WALK
        return ResolvedMotionPlayback(
            animationAsset = animation.takeIf { !request.reducedMotion && !experimental && availability.contains(it) },
            staticExpressionAsset = staticAsset,
            experimental = experimental,
            requiresHumanTimingApproval = !request.reducedMotion && !experimental && availability.contains(animation),
        )
    }
}
