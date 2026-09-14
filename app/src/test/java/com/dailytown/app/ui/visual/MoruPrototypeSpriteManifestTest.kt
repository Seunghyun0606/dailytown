package com.dailytown.app.ui.visual

import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionMotion
import com.dailytown.app.visual.MotionLoopMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoruPrototypeSpriteManifestTest {
    @Test
    fun approvedPilotMotionsHaveMultiFramePrototypeSequences() {
        listOf(
            CompanionMotion.IDLE_BREATHE,
            CompanionMotion.CLUE_REACT,
            CompanionMotion.RESOLVED_SETTLE,
        ).forEach { motion ->
            val sequence = requireNotNull(MoruPrototypeSpriteManifest.sequence(motion))
            assertTrue(sequence.frames.size >= 4)
            assertTrue(sequence.frames.all { it.durationMs > 0L })
        }
    }

    @Test
    fun idleLoopsAndOneShotStatesDoNotLoop() {
        assertEquals(
            MotionLoopMode.LOOP,
            MoruPrototypeSpriteManifest.sequence(CompanionMotion.IDLE_BREATHE)?.loopMode,
        )
        assertEquals(
            MotionLoopMode.ONCE,
            MoruPrototypeSpriteManifest.sequence(CompanionMotion.CLUE_REACT)?.loopMode,
        )
        assertEquals(
            MotionLoopMode.ONCE,
            MoruPrototypeSpriteManifest.sequence(CompanionMotion.RESOLVED_SETTLE)?.loopMode,
        )
    }

    @Test
    fun clueAndResolvedSequencesEndOnTheirSemanticExpressions() {
        assertEquals(
            CompanionExpression.CLUE_FOUND,
            MoruPrototypeSpriteManifest.sequence(CompanionMotion.CLUE_REACT)?.frames?.last()?.expression,
        )
        assertEquals(
            CompanionExpression.RESOLVED,
            MoruPrototypeSpriteManifest.sequence(CompanionMotion.RESOLVED_SETTLE)?.frames?.last()?.expression,
        )
    }

    @Test
    fun walkRemainsExperimentalAndPrototypeIsNotMarkedApproved() {
        assertNull(MoruPrototypeSpriteManifest.sequence(CompanionMotion.WALK))
        assertEquals("prototype_pending_human_tuning", MoruPrototypeSpriteManifest.approvalState)
    }
}
