package com.dailytown.app.visual

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** CSS-compatible cubic-bezier easing evaluated without Android/Compose so the contract is JVM-testable. */
class CubicBezierCurve(
    private val x1: Double,
    private val y1: Double,
    private val x2: Double,
    private val y2: Double,
) {
    init {
        require(x1 in 0.0..1.0 && x2 in 0.0..1.0) { "cubic-bezier x control points must be within 0..1" }
    }

    fun ease(t: Double): Double {
        if (t <= 0.0) return 0.0
        if (t >= 1.0) return 1.0
        return sample(y1, y2, solveX(t))
    }

    private fun sample(a: Double, b: Double, s: Double): Double {
        val inv = 1.0 - s
        return 3.0 * inv * inv * s * a + 3.0 * inv * s * s * b + s * s * s
    }

    private fun solveX(x: Double): Double {
        var s = x
        repeat(8) {
            val error = sample(x1, x2, s) - x
            if (abs(error) < 1e-7) return s
            val inv = 1.0 - s
            val slope = 3.0 * inv * inv * x1 + 6.0 * inv * s * (x2 - x1) + 3.0 * s * s * (1.0 - x2)
            if (abs(slope) < 1e-6) return@repeat
            s -= error / slope
        }
        var lo = 0.0
        var hi = 1.0
        s = x
        repeat(40) {
            val value = sample(x1, x2, s)
            if (abs(value - x) < 1e-7) return s
            if (value < x) lo = s else hi = s
            s = (lo + hi) / 2.0
        }
        return s
    }
}

/**
 * One presentation-transform sample. Translation is a fraction of the rendered art height
 * (negative = up); the caller converts it to pixels and applies [idleCapDp] for idle only.
 */
data class MotionTransformSample(
    val translateYFraction: Float,
    val scale: Float,
    val rotationDegrees: Float,
    val finished: Boolean,
) {
    companion object {
        val REST = MotionTransformSample(0f, 1f, 0f, finished = true)
    }
}

/**
 * Locked M-B2 "Calm Reactive" contract (design/final-human-gates-v3, FINAL_HUMAN_GATES_V3.md §2,
 * human decision 2026-09-15). It only transforms the existing semantic Moru art; it never selects
 * new poses. Keyframe shapes follow the approved review preview (motion-review.html, mb2):
 * idle rises from rest and returns; reactions dip slightly (anticipation), rise (accent), then settle.
 */
object MbCalmReactiveMotion {
    const val APPROVAL_REFERENCE = "FINAL_HUMAN_GATES_V3 M-B2 Calm Reactive (2026-09-15)"

    /** Transform origin used by the approved preview: horizontal centre, lower-body anchor. */
    const val PIVOT_X = 0.5f
    const val PIVOT_Y = 0.86f

    const val IDLE_PERIOD_MS = 2_600L
    private const val IDLE_AMPLITUDE = 0.014f
    private const val IDLE_SCALE = 0.008f
    private const val IDLE_ROTATION = 0.4f

    data class Reaction(
        val anticipationMs: Long,
        val accentMs: Long,
        val settleMs: Long,
        val anticipationTranslateY: Float,
        val anticipationScale: Float,
        val accentTranslateY: Float,
        val accentScale: Float,
        val accentRotation: Float,
        val settleOvershoot: Float,
    ) {
        val totalMs: Long get() = anticipationMs + accentMs + settleMs
    }

    val CLUE_FOUND = Reaction(
        anticipationMs = 90L,
        accentMs = 210L,
        settleMs = 420L,
        anticipationTranslateY = 0.006f,
        anticipationScale = 0.990f,
        accentTranslateY = -0.035f,
        accentScale = 1.025f,
        accentRotation = 1.8f,
        // "tiny soft overshoot <= 0.5%": a single half-sine dip past rest, never a repeated bounce.
        settleOvershoot = 0.004f,
    )

    val RESOLVED = Reaction(
        anticipationMs = 100L,
        accentMs = 240L,
        settleMs = 560L,
        anticipationTranslateY = 0.006f,
        anticipationScale = 1f,
        accentTranslateY = -0.020f,
        accentScale = 1f,
        accentRotation = 1.2f,
        settleOvershoot = 0f,
    )

    val idleEasing = CubicBezierCurve(.4, 0.0, .2, 1.0)
    val anticipationEasing = CubicBezierCurve(.4, 0.0, .6, 1.0)
    val reactionEasing = CubicBezierCurve(.16, 1.0, .3, 1.0)
    val settleEasing = CubicBezierCurve(.22, .72, .24, 1.0)

    /** Idle vertical cap in dp: 1.0 dp on the map, 1.2 dp in HUD/other surfaces. */
    fun idleCapDp(usage: CompanionUsageContext): Float =
        if (usage == CompanionUsageContext.MAP_AVATAR) 1.0f else 1.2f

    /** Which motion a static semantic expression implies; clue/resolved are one-shot, everything else idles. */
    fun motionFor(expression: CompanionExpression): CompanionMotion = when (expression) {
        CompanionExpression.CLUE_FOUND -> CompanionMotion.CLUE_REACT
        CompanionExpression.RESOLVED -> CompanionMotion.RESOLVED_SETTLE
        else -> CompanionMotion.IDLE_BREATHE
    }

    /** Pure sampling; callers inject elapsed time. WALK stays experimental and therefore static. */
    fun sample(motion: CompanionMotion, elapsedMs: Long): MotionTransformSample {
        require(elapsedMs >= 0L)
        return when (motion) {
            CompanionMotion.IDLE_BREATHE -> idle(elapsedMs)
            CompanionMotion.CLUE_REACT -> reaction(CLUE_FOUND, elapsedMs)
            CompanionMotion.RESOLVED_SETTLE -> reaction(RESOLVED, elapsedMs)
            CompanionMotion.WALK -> MotionTransformSample.REST
        }
    }

    private fun idle(elapsedMs: Long): MotionTransformSample {
        val half = IDLE_PERIOD_MS / 2
        val position = elapsedMs % IDLE_PERIOD_MS
        val rising = position < half
        val local = (if (rising) position else position - half).toDouble() / half
        val eased = idleEasing.ease(local).toFloat()
        val lift = if (rising) eased else 1f - eased
        return MotionTransformSample(
            translateYFraction = -IDLE_AMPLITUDE * lift,
            scale = 1f + IDLE_SCALE * lift,
            rotationDegrees = IDLE_ROTATION * lift,
            finished = false,
        )
    }

    private fun reaction(spec: Reaction, elapsedMs: Long): MotionTransformSample {
        if (elapsedMs >= spec.totalMs) return MotionTransformSample.REST
        return when {
            elapsedMs < spec.anticipationMs -> {
                val e = anticipationEasing.ease(elapsedMs.toDouble() / spec.anticipationMs).toFloat()
                MotionTransformSample(
                    translateYFraction = spec.anticipationTranslateY * e,
                    scale = 1f + (spec.anticipationScale - 1f) * e,
                    rotationDegrees = 0f,
                    finished = false,
                )
            }
            elapsedMs < spec.anticipationMs + spec.accentMs -> {
                val e = reactionEasing.ease((elapsedMs - spec.anticipationMs).toDouble() / spec.accentMs).toFloat()
                MotionTransformSample(
                    translateYFraction = lerp(spec.anticipationTranslateY, spec.accentTranslateY, e),
                    scale = lerp(spec.anticipationScale, spec.accentScale, e),
                    rotationDegrees = spec.accentRotation * e,
                    finished = false,
                )
            }
            else -> {
                val t = (elapsedMs - spec.anticipationMs - spec.accentMs).toDouble() / spec.settleMs
                val e = settleEasing.ease(t).toFloat()
                val overshoot = spec.settleOvershoot * sin(PI * e).toFloat()
                MotionTransformSample(
                    translateYFraction = spec.accentTranslateY * (1f - e) + overshoot,
                    scale = lerp(spec.accentScale, 1f, e),
                    rotationDegrees = spec.accentRotation * (1f - e),
                    finished = false,
                )
            }
        }
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
