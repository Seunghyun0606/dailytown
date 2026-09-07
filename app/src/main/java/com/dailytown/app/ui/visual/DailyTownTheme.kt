package com.dailytown.app.ui.visual

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dailytown.app.visual.DayPhase
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
    val phase = remember {
        MapRuntimeThemeResolver().resolve(LocalTime.now()).profile.phase
    }
    val dark = phase == DayPhase.NIGHT
    MaterialTheme(
        colorScheme = if (dark) DarkDailyTownScheme else LightDailyTownScheme,
        shapes = DailyTownShapes,
        content = content,
    )
}
