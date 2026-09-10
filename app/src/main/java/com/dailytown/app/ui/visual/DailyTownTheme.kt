package com.dailytown.app.ui.visual

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.DayPhase
import com.dailytown.app.visual.VisualThemeProfile
import java.time.LocalTime

/** Approved Option A / A-3 tokens expressed as Compose theme primitives. */
object DailyTownTokens {
    val LeafPrimary = Color(0xFF6B8F7A)
    val LeafSecondary = Color(0xFFA7C7A5)
    val LeafInk = Color(0xFF214F3B)
    val Butter = Color(0xFFFFD36B)
    val WarmPeach = Color(0xFFFFB184)
    val Sky = Color(0xFF9CC9FF)
    val DeepNavy = Color(0xFF1E2A44)
    val Ivory = Color(0xFFF7F3E8)
    val Paper = Color(0xFFF4F0E6)
    val CoolLight = Color(0xFFEEF3F5)
    val Ink = Color(0xFF1A1F1C)
    val OnDark = Color(0xFFF7FAF5)
    val SoftStroke = Color(0xFFD8D3C3)
}

/** Live semantic lighting supplied by the app theme to companion surfaces. */
internal val LocalDailyTownCompanionLighting = staticCompositionLocalOf {
    CompanionLightingFamily.LIGHT
}

private val DailyTownShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

private val LightDailyTownScheme = lightColorScheme(
    primary = DailyTownTokens.LeafInk,
    onPrimary = Color.White,
    primaryContainer = DailyTownTokens.LeafSecondary,
    onPrimaryContainer = DailyTownTokens.LeafInk,
    secondary = DailyTownTokens.LeafPrimary,
    secondaryContainer = DailyTownTokens.Butter.copy(alpha = 0.48f),
    onSecondaryContainer = DailyTownTokens.Ink,
    tertiary = DailyTownTokens.WarmPeach,
    background = DailyTownTokens.Ivory,
    onBackground = DailyTownTokens.Ink,
    surface = DailyTownTokens.Paper,
    onSurface = DailyTownTokens.Ink,
    surfaceVariant = DailyTownTokens.CoolLight,
    onSurfaceVariant = DailyTownTokens.LeafInk,
    outline = DailyTownTokens.SoftStroke,
)

private val DarkDailyTownScheme = darkColorScheme(
    primary = DailyTownTokens.LeafSecondary,
    onPrimary = DailyTownTokens.DeepNavy,
    primaryContainer = DailyTownTokens.LeafInk,
    onPrimaryContainer = DailyTownTokens.OnDark,
    secondary = DailyTownTokens.Butter,
    secondaryContainer = Color(0xFF3A4638),
    onSecondaryContainer = DailyTownTokens.OnDark,
    tertiary = DailyTownTokens.WarmPeach,
    background = DailyTownTokens.DeepNavy,
    onBackground = DailyTownTokens.OnDark,
    surface = Color(0xFF26324A),
    onSurface = DailyTownTokens.OnDark,
    surfaceVariant = Color(0xFF344158),
    onSurfaceVariant = DailyTownTokens.OnDark,
    outline = Color(0xFF6D788C),
)

@Composable
fun DailyTownTheme(content: @Composable () -> Unit) {
    val resolver = remember { MapRuntimeThemeResolver() }
    val lifecycleOwner = LocalLifecycleOwner.current
    var profile by remember {
        mutableStateOf(resolver.resolve(LocalTime.now()).profile)
    }

    // Refresh the full semantic profile rather than only DayPhase. EV-1 deliberately changes route,
    // marker family, ambient weights and companion lighting while remaining inside EVENING, so a
    // phase-only state would miss those minute-boundary transitions during long-running sessions.
    // Keep the minute clock lifecycle-bound as well: background UI does not need visual ticks, and
    // start() emits the latest profile immediately when the app returns to the foreground.
    DisposableEffect(lifecycleOwner, resolver) {
        val controller = DailyTownThemeRefreshController(
            onProfile = { profile = it },
            themeResolver = resolver,
        )
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> controller.start()
                Lifecycle.Event.ON_STOP -> controller.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            controller.start()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.stop()
        }
    }

    val dark = profile.phase == DayPhase.NIGHT
    MaterialTheme(
        colorScheme = if (dark) DarkDailyTownScheme else LightDailyTownScheme,
        shapes = DailyTownShapes,
    ) {
        CompositionLocalProvider(
            LocalDailyTownCompanionLighting provides profile.companionLighting,
            content = content,
        )
    }
}

/**
 * Minute-boundary clock bridge for the Compose app shell and semantic companion lighting.
 *
 * Kept separate from visual token values so the design session can replace palette/components
 * without reimplementing time-of-day lifecycle behavior.
 */
internal class DailyTownThemeRefreshController(
    private val onProfile: (VisualThemeProfile) -> Unit,
    private val themeResolver: MapRuntimeThemeResolver = MapRuntimeThemeResolver(),
    private val clock: () -> LocalTime = LocalTime::now,
    private val scheduler: DailyTownThemeRefreshScheduler = MainThreadDailyTownThemeRefreshScheduler(),
) {
    private var started = false

    private val tick = object : Runnable {
        override fun run() {
            if (!started) return
            val now = clock()
            onProfile(themeResolver.resolve(now).profile)
            scheduler.schedule(this, MapThemeRefreshCadence.millisUntilNextMinute(now))
        }
    }

    fun start() {
        if (started) return
        started = true
        tick.run()
    }

    fun stop() {
        if (!started) return
        started = false
        scheduler.cancel(tick)
    }

    fun close() = stop()
}

internal fun interface DailyTownThemeRefreshScheduler {
    fun schedule(task: Runnable, delayMillis: Long)

    fun cancel(task: Runnable) = Unit
}

private class MainThreadDailyTownThemeRefreshScheduler(
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : DailyTownThemeRefreshScheduler {
    override fun schedule(task: Runnable, delayMillis: Long) {
        handler.postDelayed(task, delayMillis)
    }

    override fun cancel(task: Runnable) {
        handler.removeCallbacks(task)
    }
}
