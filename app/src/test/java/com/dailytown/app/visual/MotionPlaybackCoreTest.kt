package com.dailytown.app.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionPlaybackCoreTest {
    private val idleAsset = CompanionAssetResolver.animationKey("moru", CompanionMotion.IDLE_BREATHE)
    private val clueAsset = CompanionAssetResolver.animationKey("moru", CompanionMotion.CLUE_REACT)
    private val walkAsset = CompanionAssetResolver.animationKey("moru", CompanionMotion.WALK)

    private fun atlas(asset: SemanticAssetKey, frames: Int = 3): SpriteAtlasDescriptor = SpriteAtlasDescriptor(
        asset = asset,
        pixelWidth = frames * 64,
        pixelHeight = 64,
        frames = List(frames) { index ->
            SpriteAtlasFrame(index = index, xPx = index * 64, yPx = 0, widthPx = 64, heightPx = 64)
        },
    )

    private fun request(
        motion: CompanionMotion,
        reducedMotion: Boolean = false,
        expression: CompanionExpression = CompanionExpression.NEUTRAL,
    ) = MotionPlaybackRequest(
        companionId = "moru",
        motion = motion,
        currentExpression = expression,
        reducedMotion = reducedMotion,
    )

    private fun resolve(request: MotionPlaybackRequest, availableAnimation: SemanticAssetKey?): ResolvedMotionPlayback {
        val keys = buildList {
            add(CompanionAssetResolver.expressionKey("moru", request.currentExpression))
            availableAnimation?.let(::add)
        }
        return MotionAssetResolver(SetSemanticAssetAvailability(keys)).resolve(request)
    }

    @Test
    fun noHumanTuningMeansStaticEvenWhenAtlasIsAvailable() {
        val request = request(CompanionMotion.IDLE_BREATHE)
        val resolved = resolve(request, idleAsset)
        val plan = MotionPlaybackPlanner(SetMotionAtlasRepository(listOf(atlas(idleAsset)))).plan(request, resolved)

        assertEquals(MotionPlaybackMode.STATIC_TIMING_UNAPPROVED, plan.mode)
        assertTrue(plan.requiresHumanTimingApproval)
        assertFalse(plan.canAnimate)
        assertNull(plan.tuning)
    }

    @Test
    fun reducedMotionSkipsAtlasAndHumanTimingGate() {
        val request = request(CompanionMotion.CLUE_REACT, reducedMotion = true, expression = CompanionExpression.CLUE_FOUND)
        val resolved = resolve(request, clueAsset)
        val plan = MotionPlaybackPlanner(SetMotionAtlasRepository(listOf(atlas(clueAsset)))).plan(request, resolved)

        assertEquals(MotionPlaybackMode.STATIC_REDUCED_MOTION, plan.mode)
        assertEquals(CompanionAssetResolver.expressionKey("moru", CompanionExpression.CLUE_FOUND), plan.staticExpressionAsset)
        assertFalse(plan.requiresHumanTimingApproval)
        assertFalse(plan.canAnimate)
    }

    @Test
    fun walkRemainsExperimentalAndNeverAnimates() {
        val request = request(CompanionMotion.WALK)
        val resolved = resolve(request, walkAsset)
        val tuning = approvedTuningProvider(listOf(10L, 10L, 10L))
        val plan = MotionPlaybackPlanner(SetMotionAtlasRepository(listOf(atlas(walkAsset))), tuning).plan(request, resolved)

        assertEquals(MotionPlaybackMode.STATIC_EXPERIMENTAL, plan.mode)
        assertFalse(plan.canAnimate)
        assertNull(plan.atlas)
    }

    @Test
    fun approvedInjectedTuningEnablesOnlyAuthoredAtlas() {
        val request = request(CompanionMotion.CLUE_REACT, expression = CompanionExpression.CLUE_FOUND)
        val resolved = resolve(request, clueAsset)
        val atlas = atlas(clueAsset)
        val tuning = approvedTuningProvider(listOf(40L, 60L, 80L))
        val plan = MotionPlaybackPlanner(SetMotionAtlasRepository(listOf(atlas)), tuning).plan(request, resolved)

        assertEquals(MotionPlaybackMode.SPRITE_ATLAS, plan.mode)
        assertTrue(plan.canAnimate)
        assertEquals(MotionLoopMode.ONCE, plan.loopMode)
        assertEquals(atlas, plan.atlas)
        assertEquals(listOf(40L, 60L, 80L), plan.tuning?.frameDurationsMs)
        assertFalse(plan.requiresHumanTimingApproval)
    }

    @Test
    fun mismatchedHumanTuningFailsClosedToStatic() {
        val request = request(CompanionMotion.IDLE_BREATHE)
        val resolved = resolve(request, idleAsset)
        val atlas = atlas(idleAsset, frames = 3)
        val plan = MotionPlaybackPlanner(
            SetMotionAtlasRepository(listOf(atlas)),
            approvedTuningProvider(listOf(100L, 100L)),
        ).plan(request, resolved)

        assertEquals(MotionPlaybackMode.STATIC_INVALID_TUNING, plan.mode)
        assertTrue(plan.requiresHumanTimingApproval)
        assertFalse(plan.canAnimate)
    }

    @Test
    fun frameSelectorLoopsIdleAndClampsOneShotAtFinalFrame() {
        val atlas = atlas(idleAsset, frames = 3)
        val tuning = tuning(listOf(20L, 30L, 50L))

        val looped = SpriteAtlasFrameSelector.select(125L, atlas, tuning, MotionLoopMode.LOOP)
        assertEquals(1, looped.frame.index)
        assertEquals(5L, looped.elapsedInFrameMs)
        assertFalse(looped.finished)

        val finished = SpriteAtlasFrameSelector.select(100L, atlas, tuning, MotionLoopMode.ONCE)
        assertEquals(2, finished.frame.index)
        assertEquals(49L, finished.elapsedInFrameMs)
        assertTrue(finished.finished)
    }

    @Test
    fun approvedMbLoopContractMatchesSourceManifest() {
        assertEquals(MotionLoopMode.LOOP, MbMotionContract.loopMode(CompanionMotion.IDLE_BREATHE))
        assertEquals(MotionLoopMode.ONCE, MbMotionContract.loopMode(CompanionMotion.CLUE_REACT))
        assertEquals(MotionLoopMode.ONCE, MbMotionContract.loopMode(CompanionMotion.RESOLVED_SETTLE))
        assertNull(MbMotionContract.loopMode(CompanionMotion.WALK))
    }

    private fun tuning(durations: List<Long>) = MotionPlaybackTuning(
        frameDurationsMs = durations,
        easingId = "human-approved-easing",
        intensityId = "human-approved-intensity",
        approvalReference = "test-approval",
    )

    private fun approvedTuningProvider(durations: List<Long>) = object : MotionPlaybackTuningProvider {
        override fun tuning(
            companionId: String,
            motion: CompanionMotion,
            atlas: SpriteAtlasDescriptor,
        ): MotionPlaybackTuning = tuning(durations)
    }
}
