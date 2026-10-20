package com.dailytown.app.ui.visual

import com.dailytown.app.map.MapBrightnessFamily
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.visual.DayPhase
import com.dailytown.app.visual.DayPhaseResolver
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.VisualDebugOverride
import com.dailytown.app.visual.VisualThemeProfile
import com.dailytown.app.visual.VisualThemeProfiles
import java.time.LocalTime

data class ResolvedMapRuntimeTheme(
    val profile: VisualThemeProfile,
    val mapTheme: MapThemeSpec,
)

/**
 * Application-layer bridge for the approved visual architecture:
 * local clock/debug override -> DayPhaseResolver -> VisualThemeProfile -> provider-neutral MapThemeSpec.
 *
 * The schedule remains injectable in DayPhaseResolver and is still an engineering fallback rather
 * than product truth. Screenshot/QA callers can force phase/EV-1 through VisualDebugOverride.
 */
class MapRuntimeThemeResolver(
    private val dayPhaseResolver: DayPhaseResolver = DayPhaseResolver(),
) {
    fun resolve(
        time: LocalTime,
        override: VisualDebugOverride = VisualDebugOverride(),
        mapBackgroundLuminance: Float? = null,
    ): ResolvedMapRuntimeTheme {
        val phase = dayPhaseResolver.resolve(time, override)
        val eveningProgress = if (phase == DayPhase.EVENING) {
            dayPhaseResolver.eveningProgress(time, override)
        } else {
            .5f
        }
        val profile = VisualThemeProfiles.forPhase(
            phase = phase,
            eveningProgress = eveningProgress,
            mapBackgroundLuminance = mapBackgroundLuminance,
        )
        return ResolvedMapRuntimeTheme(
            profile = profile,
            mapTheme = MapThemeSpec(
                preferredBrightness = if (profile.markerFamily == MarkerFamily.DARK) {
                    MapBrightnessFamily.DARK
                } else {
                    MapBrightnessFamily.LIGHT
                },
                markerFamily = profile.markerFamily,
                routeColor = profile.route,
            ),
        )
    }
}
