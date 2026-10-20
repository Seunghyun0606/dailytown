package com.dailytown.app.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MbCalmReactiveMotionTest {
    private val eps = 1e-4f

    @Test
    fun cubicBezierMatchesCssEndpointsAndIsMonotonicForLockedCurves() {
        listOf(
            MbCalmReactiveMotion.idleEasing,
            MbCalmReactiveMotion.anticipationEasing,
            MbCalmReactiveMotion.reactionEasing,
            MbCalmReactiveMotion.settleEasing,
        ).forEach { curve ->
            assertEquals(0.0, curve.ease(0.0), 1e-9)
            assertEquals(1.0, curve.ease(1.0), 1e-9)
            var previous = 0.0
            for (step in 1..200) {
                val value = curve.ease(step / 200.0)
                assertTrue("locked M-B2 curves never overshoot or reverse", value >= previous - 1e-6 && value <= 1.0 + 1e-6)
                previous = value
            }
        }
        // CSS ease-out-like reaction curve: most of the change happens early.
        assertTrue(MbCalmReactiveMotion.reactionEasing.ease(0.25) > 0.6)
    }

    @Test
    fun lockedTimingTotalsMatchTheHumanGate() {
        assertEquals(2_600L, MbCalmReactiveMotion.IDLE_PERIOD_MS)
        assertEquals(720L, MbCalmReactiveMotion.CLUE_FOUND.totalMs)
        assertEquals(listOf(90L, 210L, 420L), MbCalmReactiveMotion.CLUE_FOUND.let { listOf(it.anticipationMs, it.accentMs, it.settleMs) })
        assertEquals(900L, MbCalmReactiveMotion.RESOLVED.totalMs)
        assertEquals(listOf(100L, 240L, 560L), MbCalmReactiveMotion.RESOLVED.let { listOf(it.anticipationMs, it.accentMs, it.settleMs) })
    }

    @Test
    fun idleRisesToLockedPeakAtHalfPeriodAndLoopsBackToRest() {
        val rest = MbCalmReactiveMotion.sample(CompanionMotion.IDLE_BREATHE, 0L)
        val peak = MbCalmReactiveMotion.sample(CompanionMotion.IDLE_BREATHE, 1_300L)
        val nextLoop = MbCalmReactiveMotion.sample(CompanionMotion.IDLE_BREATHE, 2_600L)
        assertEquals(0f, rest.translateYFraction, eps)
        assertEquals(-0.014f, peak.translateYFraction, eps)
        assertEquals(1.008f, peak.scale, eps)
        assertEquals(0.4f, peak.rotationDegrees, eps)
        assertEquals(rest, nextLoop)
        assertFalse("idle loops forever", peak.finished)
        (0L..2_600L step 50L).forEach { t ->
            val s = MbCalmReactiveMotion.sample(CompanionMotion.IDLE_BREATHE, t)
            assertTrue(s.translateYFraction in -0.014f - eps..eps)
            assertTrue(s.scale in 1f - eps..1.008f + eps)
            assertTrue(abs(s.rotationDegrees) <= 0.4f + eps)
        }
    }

    @Test
    fun idleCapIsTighterOnTheMapThanInTheHud() {
        assertEquals(1.0f, MbCalmReactiveMotion.idleCapDp(CompanionUsageContext.MAP_AVATAR), eps)
        assertEquals(1.2f, MbCalmReactiveMotion.idleCapDp(CompanionUsageContext.HUD_PORTRAIT), eps)
    }

    @Test
    fun clueFoundHitsAccentThenSettlesWithAtMostHalfPercentOvershootAndHolds() {
        val accent = MbCalmReactiveMotion.sample(CompanionMotion.CLUE_REACT, 300L)
        assertEquals(-0.035f, accent.translateYFraction, 2e-3f)
        assertEquals(1.025f, accent.scale, 1e-3f)
        assertEquals(1.8f, accent.rotationDegrees, 0.05f)

        var maxBelowRest = 0f
        (300L until 720L step 5L).forEach { t ->
            maxBelowRest = maxOf(maxBelowRest, MbCalmReactiveMotion.sample(CompanionMotion.CLUE_REACT, t).translateYFraction)
        }
        assertTrue("settle overshoot must stay within 0.5%", maxBelowRest <= 0.005f)

        val done = MbCalmReactiveMotion.sample(CompanionMotion.CLUE_REACT, 720L)
        assertTrue(done.finished)
        assertEquals(MotionTransformSample.REST, done)
        assertEquals("one-shot then static hold", done, MbCalmReactiveMotion.sample(CompanionMotion.CLUE_REACT, 60_000L))
    }

    @Test
    fun resolvedIsANodWithoutScaleAndEndsAtRest() {
        val accent = MbCalmReactiveMotion.sample(CompanionMotion.RESOLVED_SETTLE, 340L)
        assertEquals(-0.020f, accent.translateYFraction, 2e-3f)
        assertEquals(1.2f, accent.rotationDegrees, 0.05f)
        (0L..900L step 10L).forEach { t ->
            assertEquals(1f, MbCalmReactiveMotion.sample(CompanionMotion.RESOLVED_SETTLE, t).scale, eps)
        }
        assertTrue(MbCalmReactiveMotion.sample(CompanionMotion.RESOLVED_SETTLE, 900L).finished)
    }

    @Test
    fun anticipationDipsBeforeTheAccentLikeTheApprovedPreview() {
        val clue = MbCalmReactiveMotion.sample(CompanionMotion.CLUE_REACT, 89L)
        assertTrue("anticipation moves down (positive Y) first", clue.translateYFraction > 0f)
        assertTrue(clue.scale < 1f)
    }

    @Test
    fun semanticExpressionsSelectOnlyApprovedMotionsAndWalkStaysStatic() {
        assertEquals(CompanionMotion.CLUE_REACT, MbCalmReactiveMotion.motionFor(CompanionExpression.CLUE_FOUND))
        assertEquals(CompanionMotion.RESOLVED_SETTLE, MbCalmReactiveMotion.motionFor(CompanionExpression.RESOLVED))
        listOf(CompanionExpression.NEUTRAL, CompanionExpression.HAPPY, CompanionExpression.CURIOUS, CompanionExpression.SURPRISED)
            .forEach { assertEquals(CompanionMotion.IDLE_BREATHE, MbCalmReactiveMotion.motionFor(it)) }
        assertEquals(MotionTransformSample.REST, MbCalmReactiveMotion.sample(CompanionMotion.WALK, 500L))
    }
}
