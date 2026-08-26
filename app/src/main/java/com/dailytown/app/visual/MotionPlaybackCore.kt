package com.dailytown.app.visual

/** Loop behavior is already fixed by the approved M-B source manifest; frame timing is not. */
enum class MotionLoopMode { LOOP, ONCE }

data class SpriteAtlasFrame(
    val index: Int,
    val xPx: Int,
    val yPx: Int,
    val widthPx: Int,
    val heightPx: Int,
) {
    init {
        require(index >= 0)
        require(xPx >= 0 && yPx >= 0)
        require(widthPx > 0 && heightPx > 0)
    }
}

/** Provider-neutral descriptor for an authored runtime sprite atlas. */
data class SpriteAtlasDescriptor(
    val asset: SemanticAssetKey,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val frames: List<SpriteAtlasFrame>,
) {
    init {
        require(pixelWidth > 0 && pixelHeight > 0)
        require(frames.isNotEmpty())
        require(frames.map { it.index } == frames.indices.toList()) {
            "Sprite atlas frame indexes must be ordered and contiguous from zero"
        }
        require(frames.all { frame ->
            frame.xPx + frame.widthPx <= pixelWidth && frame.yPx + frame.heightPx <= pixelHeight
        }) { "Sprite atlas frame exceeds atlas bounds" }
    }
}

interface MotionAtlasRepository {
    fun descriptor(asset: SemanticAssetKey): SpriteAtlasDescriptor?
}

class SetMotionAtlasRepository(atlases: Iterable<SpriteAtlasDescriptor>) : MotionAtlasRepository {
    private val byKey = atlases.associateBy { it.asset.value }.also { values ->
        require(values.size == atlases.count()) { "Duplicate sprite atlas semantic key" }
    }

    override fun descriptor(asset: SemanticAssetKey): SpriteAtlasDescriptor? = byKey[asset.value]
}

/**
 * Human-approved runtime tuning only. There are intentionally no default duration/easing/intensity values.
 * The opaque easing/intensity ids let authoring data cross the runtime boundary without this core inventing
 * visual behavior. A provider should return null until the visible M-B prototype receives approval.
 */
data class MotionPlaybackTuning(
    val frameDurationsMs: List<Long>,
    val easingId: String,
    val intensityId: String,
    val approvalReference: String,
) {
    init {
        require(frameDurationsMs.isNotEmpty() && frameDurationsMs.all { it > 0L })
        require(easingId.isNotBlank())
        require(intensityId.isNotBlank())
        require(approvalReference.isNotBlank())
    }
}

interface MotionPlaybackTuningProvider {
    fun tuning(
        companionId: String,
        motion: CompanionMotion,
        atlas: SpriteAtlasDescriptor,
    ): MotionPlaybackTuning?
}

object NoApprovedMotionPlaybackTuning : MotionPlaybackTuningProvider {
    override fun tuning(
        companionId: String,
        motion: CompanionMotion,
        atlas: SpriteAtlasDescriptor,
    ): MotionPlaybackTuning? = null
}

enum class MotionPlaybackMode {
    SPRITE_ATLAS,
    STATIC_REDUCED_MOTION,
    STATIC_EXPERIMENTAL,
    STATIC_ASSET_UNAVAILABLE,
    STATIC_ATLAS_UNAVAILABLE,
    STATIC_TIMING_UNAPPROVED,
    STATIC_INVALID_TUNING,
}

data class MotionPlaybackPlan(
    val mode: MotionPlaybackMode,
    val staticExpressionAsset: SemanticAssetKey,
    val atlas: SpriteAtlasDescriptor? = null,
    val tuning: MotionPlaybackTuning? = null,
    val loopMode: MotionLoopMode? = null,
    val requiresHumanTimingApproval: Boolean = false,
) {
    val canAnimate: Boolean get() = mode == MotionPlaybackMode.SPRITE_ATLAS
}

/** Approved source semantics that are not part of the still-open timing/easing/intensity gate. */
object MbMotionContract {
    fun loopMode(motion: CompanionMotion): MotionLoopMode? = when (motion) {
        CompanionMotion.IDLE_BREATHE -> MotionLoopMode.LOOP
        CompanionMotion.CLUE_REACT -> MotionLoopMode.ONCE
        CompanionMotion.RESOLVED_SETTLE -> MotionLoopMode.ONCE
        CompanionMotion.WALK -> null // experimental_only in M-B v1
    }
}

/**
 * Converts semantic motion resolution into a fail-closed playback plan.
 * No runtime atlas, no approved tuning, reduced motion, or experimental WALK all stay static.
 */
class MotionPlaybackPlanner(
    private val atlasRepository: MotionAtlasRepository,
    private val tuningProvider: MotionPlaybackTuningProvider = NoApprovedMotionPlaybackTuning,
) {
    fun plan(request: MotionPlaybackRequest, resolved: ResolvedMotionPlayback): MotionPlaybackPlan {
        val staticPlan: (MotionPlaybackMode, Boolean) -> MotionPlaybackPlan = { mode, approvalRequired ->
            MotionPlaybackPlan(
                mode = mode,
                staticExpressionAsset = resolved.staticExpressionAsset,
                requiresHumanTimingApproval = approvalRequired,
            )
        }

        if (request.reducedMotion) return staticPlan(MotionPlaybackMode.STATIC_REDUCED_MOTION, false)
        if (resolved.experimental || request.motion == CompanionMotion.WALK) {
            return staticPlan(MotionPlaybackMode.STATIC_EXPERIMENTAL, false)
        }

        val animationAsset = resolved.animationAsset
            ?: return staticPlan(MotionPlaybackMode.STATIC_ASSET_UNAVAILABLE, false)
        val atlas = atlasRepository.descriptor(animationAsset)
            ?: return staticPlan(MotionPlaybackMode.STATIC_ATLAS_UNAVAILABLE, false)
        val loopMode = MbMotionContract.loopMode(request.motion)
            ?: return staticPlan(MotionPlaybackMode.STATIC_EXPERIMENTAL, false)
        val tuning = tuningProvider.tuning(request.companionId, request.motion, atlas)
            ?: return staticPlan(MotionPlaybackMode.STATIC_TIMING_UNAPPROVED, true)

        if (tuning.frameDurationsMs.size != atlas.frames.size) {
            return staticPlan(MotionPlaybackMode.STATIC_INVALID_TUNING, true)
        }

        return MotionPlaybackPlan(
            mode = MotionPlaybackMode.SPRITE_ATLAS,
            staticExpressionAsset = resolved.staticExpressionAsset,
            atlas = atlas,
            tuning = tuning,
            loopMode = loopMode,
            requiresHumanTimingApproval = false,
        )
    }
}

data class SpriteFrameSelection(
    val frame: SpriteAtlasFrame,
    val elapsedInFrameMs: Long,
    val finished: Boolean,
)

/** Pure deterministic frame selection; callers inject elapsed time instead of reading a device clock. */
object SpriteAtlasFrameSelector {
    fun select(
        elapsedMs: Long,
        atlas: SpriteAtlasDescriptor,
        tuning: MotionPlaybackTuning,
        loopMode: MotionLoopMode,
    ): SpriteFrameSelection {
        require(elapsedMs >= 0L)
        require(tuning.frameDurationsMs.size == atlas.frames.size)
        val totalDuration = tuning.frameDurationsMs.fold(0L) { total, duration ->
            Math.addExact(total, duration)
        }
        require(totalDuration > 0L)

        val finished = loopMode == MotionLoopMode.ONCE && elapsedMs >= totalDuration
        val position = when (loopMode) {
            MotionLoopMode.LOOP -> elapsedMs % totalDuration
            MotionLoopMode.ONCE -> if (finished) totalDuration - 1L else elapsedMs
        }

        var start = 0L
        tuning.frameDurationsMs.forEachIndexed { index, duration ->
            val end = Math.addExact(start, duration)
            if (position < end) {
                return SpriteFrameSelection(
                    frame = atlas.frames[index],
                    elapsedInFrameMs = position - start,
                    finished = finished,
                )
            }
            start = end
        }
        error("Frame selection exceeded validated sprite timing")
    }
}
