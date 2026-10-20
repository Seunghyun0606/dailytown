package com.dailytown.app.visual

import java.time.LocalTime
import kotlin.math.roundToInt

/**
 * Clock boundaries stay injectable: they are an engineering fallback, not a product/design truth.
 * Screenshot/replay tests should force [DayPhase] or [EveningDebugState] instead of reading device time.
 */
data class DayPhaseSchedule(
    val dawnStartMinute: Int,
    val morningStartMinute: Int,
    val middayStartMinute: Int,
    val afternoonStartMinute: Int,
    val sunsetStartMinute: Int,
    val eveningStartMinute: Int,
    val nightStartMinute: Int,
) {
    init {
        val values = listOf(dawnStartMinute, morningStartMinute, middayStartMinute, afternoonStartMinute, sunsetStartMinute, eveningStartMinute, nightStartMinute)
        require(values.all { it in 0 until 24 * 60 })
        require(values.zipWithNext().all { (a, b) -> a < b })
    }

    companion object {
        /** TODO(product/design): replace these configurable fallback boundaries if solar/product windows are approved. */
        fun initialEngineeringFallback() = DayPhaseSchedule(
            dawnStartMinute = 5 * 60,
            morningStartMinute = 8 * 60,
            middayStartMinute = 11 * 60,
            afternoonStartMinute = 14 * 60,
            sunsetStartMinute = 17 * 60,
            eveningStartMinute = 19 * 60,
            nightStartMinute = 21 * 60,
        )
    }
}

enum class EveningDebugState(val progress: Float) {
    E0(0f), E1(.25f), E2(.5f), E3(.75f), E4(1f),
}

data class VisualDebugOverride(
    val forcedPhase: DayPhase? = null,
    val forcedEveningState: EveningDebugState? = null,
    val forcedEveningProgress: Float? = null,
) {
    init { require(forcedEveningProgress == null || forcedEveningProgress in 0f..1f) }
}

class DayPhaseResolver(
    private val schedule: DayPhaseSchedule = DayPhaseSchedule.initialEngineeringFallback(),
) {
    fun resolve(time: LocalTime, override: VisualDebugOverride = VisualDebugOverride()): DayPhase {
        if (override.forcedEveningState != null || override.forcedEveningProgress != null) return DayPhase.EVENING
        override.forcedPhase?.let { return it }
        val minute = time.hour * 60 + time.minute
        return when {
            minute < schedule.dawnStartMinute -> DayPhase.NIGHT
            minute < schedule.morningStartMinute -> DayPhase.DAWN
            minute < schedule.middayStartMinute -> DayPhase.MORNING
            minute < schedule.afternoonStartMinute -> DayPhase.MIDDAY
            minute < schedule.sunsetStartMinute -> DayPhase.AFTERNOON
            minute < schedule.eveningStartMinute -> DayPhase.SUNSET
            minute < schedule.nightStartMinute -> DayPhase.EVENING
            else -> DayPhase.NIGHT
        }
    }

    fun eveningProgress(time: LocalTime, override: VisualDebugOverride = VisualDebugOverride()): Float {
        override.forcedEveningState?.let { return it.progress }
        override.forcedEveningProgress?.let { return it }
        val minute = time.hour * 60 + time.minute
        val span = schedule.nightStartMinute - schedule.eveningStartMinute
        if (span <= 0) return 0f
        return ((minute - schedule.eveningStartMinute).toFloat() / span).coerceIn(0f, 1f)
    }
}

/** Approved EV-1 SUNSET -> NIGHT interpolation. It never creates an EVENING asset family. */
object EveningVisualInterpolator {
    private data class Stop(val progress: Float, val color: VisualArgb)
    private val routeStops = listOf(
        Stop(0f, VisualArgb.rgb("#E8794F")),
        Stop(.25f, VisualArgb.rgb("#CC8479")),
        Stop(.5f, VisualArgb.rgb("#B090A2")),
        Stop(.75f, VisualArgb.rgb("#949CCC")),
        Stop(1f, VisualArgb.rgb("#78A7F6")),
    )

    const val MAP_LUMINANCE_THRESHOLD = .42f

    fun profile(progress: Float, mapBackgroundLuminance: Float? = null): VisualThemeProfile {
        val p = progress.coerceIn(0f, 1f)
        val markerFamily = when {
            p < .5f -> MarkerFamily.DAY
            p > .5f -> MarkerFamily.DARK
            mapBackgroundLuminance == null -> MarkerFamily.DAY // E2 manifest fallback is AUTO; deterministic until measured.
            mapBackgroundLuminance >= MAP_LUMINANCE_THRESHOLD -> MarkerFamily.DARK
            else -> MarkerFamily.DAY
        }
        val lighting = if (p <= .5f) CompanionLightingFamily.WARM_DUSK else CompanionLightingFamily.DARK
        return VisualThemeProfile(
            phase = DayPhase.EVENING,
            markerFamily = markerFamily,
            companionLighting = lighting,
            route = interpolateRoute(p),
            warmLocalPointWeight = lerpPiecewise(p, listOf(0f to 1f, .25f to .85f, .5f to .70f, .75f to .55f, 1f to .40f)),
            coolAmbientWeight = lerpPiecewise(p, listOf(0f to 0f, .25f to .25f, .5f to .50f, .75f to .75f, 1f to 1f)),
            eveningProgress = p,
        )
    }

    private fun interpolateRoute(progress: Float): VisualArgb {
        val upperIndex = routeStops.indexOfFirst { it.progress >= progress }.takeIf { it >= 0 } ?: routeStops.lastIndex
        if (upperIndex == 0) return routeStops.first().color
        val lower = routeStops[upperIndex - 1]
        val upper = routeStops[upperIndex]
        val local = (progress - lower.progress) / (upper.progress - lower.progress)
        return lerpColor(lower.color, upper.color, local)
    }

    private fun lerpColor(a: VisualArgb, b: VisualArgb, t: Float): VisualArgb {
        fun channel(value: Long, shift: Int) = ((value shr shift) and 0xFF).toInt()
        fun mix(x: Int, y: Int) = (x + (y - x) * t).roundToInt().coerceIn(0, 255)
        val av = a.value
        val bv = b.value
        val r = mix(channel(av, 16), channel(bv, 16))
        val g = mix(channel(av, 8), channel(bv, 8))
        val bl = mix(channel(av, 0), channel(bv, 0))
        return VisualArgb(0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or bl.toLong())
    }

    private fun lerpPiecewise(progress: Float, stops: List<Pair<Float, Float>>): Float {
        val upperIndex = stops.indexOfFirst { it.first >= progress }.takeIf { it >= 0 } ?: stops.lastIndex
        if (upperIndex == 0) return stops.first().second
        val lower = stops[upperIndex - 1]
        val upper = stops[upperIndex]
        val local = (progress - lower.first) / (upper.first - lower.first)
        return lower.second + (upper.second - lower.second) * local
    }
}

object VisualThemeProfiles {
    fun forPhase(phase: DayPhase, eveningProgress: Float = .5f, mapBackgroundLuminance: Float? = null): VisualThemeProfile = when (phase) {
        DayPhase.DAWN -> VisualThemeProfile(phase, MarkerFamily.DAY, CompanionLightingFamily.LIGHT, VisualArgb.rgb("#6F9F71"))
        DayPhase.MORNING -> VisualThemeProfile(phase, MarkerFamily.DAY, CompanionLightingFamily.LIGHT, VisualArgb.rgb("#6F9F71"))
        DayPhase.MIDDAY -> VisualThemeProfile(phase, MarkerFamily.DAY, CompanionLightingFamily.LIGHT, VisualArgb.rgb("#6F9F71"))
        DayPhase.AFTERNOON -> VisualThemeProfile(phase, MarkerFamily.DAY, CompanionLightingFamily.LIGHT, VisualArgb.rgb("#6F9F71"))
        DayPhase.SUNSET -> VisualThemeProfile(phase, MarkerFamily.DAY, CompanionLightingFamily.WARM_DUSK, VisualArgb.rgb("#E8794F"), warmLocalPointWeight = 1f)
        DayPhase.EVENING -> EveningVisualInterpolator.profile(eveningProgress, mapBackgroundLuminance)
        DayPhase.NIGHT -> VisualThemeProfile(phase, MarkerFamily.DARK, CompanionLightingFamily.DARK, VisualArgb.rgb("#78A7F6"), warmLocalPointWeight = .4f, coolAmbientWeight = 1f)
    }
}
