package com.dailytown.app.ui.visual

import com.dailytown.app.map.MapBrightnessFamily
import com.dailytown.app.visual.DayPhase
import com.dailytown.app.visual.EveningDebugState
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.VisualDebugOverride
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRuntimeThemeResolverTest {
    private val resolver = MapRuntimeThemeResolver()

    @Test
    fun forcedMorningUsesApprovedDayFamilyAndLightMap() {
        val resolved = resolver.resolve(
            time = LocalTime.NOON,
            override = VisualDebugOverride(forcedPhase = DayPhase.MORNING),
        )

        assertEquals(DayPhase.MORNING, resolved.profile.phase)
        assertEquals(MarkerFamily.DAY, resolved.profile.markerFamily)
        assertEquals(MapBrightnessFamily.LIGHT, resolved.mapTheme.preferredBrightness)
        assertEquals(resolved.profile.route, resolved.mapTheme.routeColor)
    }

    @Test
    fun forcedNightUsesApprovedDarkFamilyAndNightMap() {
        val resolved = resolver.resolve(
            time = LocalTime.NOON,
            override = VisualDebugOverride(forcedPhase = DayPhase.NIGHT),
        )

        assertEquals(DayPhase.NIGHT, resolved.profile.phase)
        assertEquals(MarkerFamily.DARK, resolved.profile.markerFamily)
        assertEquals(MapBrightnessFamily.DARK, resolved.mapTheme.preferredBrightness)
    }

    @Test
    fun forcedEveningCheckpointsNeverCreateEveningMarkerFamily() {
        EveningDebugState.values().forEach { checkpoint ->
            val resolved = resolver.resolve(
                time = LocalTime.NOON,
                override = VisualDebugOverride(forcedEveningState = checkpoint),
                mapBackgroundLuminance = .5f,
            )
            assertEquals(DayPhase.EVENING, resolved.profile.phase)
            assertEquals(resolved.profile.markerFamily, resolved.mapTheme.markerFamily)
            assertTrue(
                resolved.profile.markerFamily == MarkerFamily.DAY ||
                    resolved.profile.markerFamily == MarkerFamily.DARK,
            )
        }
    }

    @Test
    fun e2UsesMeasuredMapLuminanceForFamilyChoice() {
        val override = VisualDebugOverride(forcedEveningState = EveningDebugState.E2)
        val bright = resolver.resolve(LocalTime.NOON, override, mapBackgroundLuminance = .8f)
        val dark = resolver.resolve(LocalTime.NOON, override, mapBackgroundLuminance = .2f)

        assertEquals(MarkerFamily.DARK, bright.profile.markerFamily)
        assertEquals(MarkerFamily.DAY, dark.profile.markerFamily)
    }
}
