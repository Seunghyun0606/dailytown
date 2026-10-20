package com.dailytown.app.visualqa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.graphics.writeToTestStorage
import com.dailytown.app.ui.visual.LocalCompanionRuntimeProfile
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.CompanionRuntimeProfile
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import org.junit.Rule
import org.junit.Test

/** Emulator artifact gate. Physical-device outdoor readability remains a separate Human Gate. */
class MoruV2RuntimeVisualQaTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mapAndHudDpMatrixPlusAllUsageContextsProduceReviewableArtifacts() {
        var lighting by mutableStateOf(CompanionLightingFamily.LIGHT)
        composeRule.setContent {
            CompositionLocalProvider(LocalCompanionRuntimeProfile provides CompanionRuntimeProfile.MORU_CANONICAL_V2) {
                MaterialTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5EEDC))
                            .padding(8.dp)
                            .testTag("moru-v2-qa-root"),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Moru v2 · ${lighting.name} · cream / dark / map-heavy")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(48, 56, 64).forEach { size ->
                                QaMoru(
                                    usage = CompanionUsageContext.MAP_AVATAR,
                                    expression = CompanionExpression.CURIOUS,
                                    lighting = lighting,
                                    affinity = AppearanceProfile.BASE,
                                    sizeDp = size,
                                    tag = "moru-map-${size}dp",
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(56, 64, 72).forEach { size ->
                                QaMoru(
                                    usage = CompanionUsageContext.HUD_PORTRAIT,
                                    expression = CompanionExpression.CLUE_FOUND,
                                    lighting = lighting,
                                    affinity = AppearanceProfile.FAMILIAR,
                                    sizeDp = size,
                                    tag = "moru-hud-${size}dp",
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QaContext("Encounter", Color(0xFFF1E8D5)) {
                                QaMoru(CompanionUsageContext.ENCOUNTER_HALFBODY, CompanionExpression.SURPRISED, lighting, AppearanceProfile.BASE, 88, "moru-qa-encounter")
                            }
                            QaContext("Result", Color(0xFF202832)) {
                                QaMoru(CompanionUsageContext.RESULT_LARGE, CompanionExpression.RESOLVED, lighting, AppearanceProfile.TRUSTED, 88, "moru-qa-result")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QaContext("Companion", Color(0xFFECE1C8)) {
                                QaMoru(CompanionUsageContext.COMPANION_PORTRAIT, CompanionExpression.HAPPY, lighting, AppearanceProfile.BEST_FRIEND, 88, "moru-qa-companion")
                            }
                            QaContext("Journal", Color(0xFFBAC8B0)) {
                                QaMoru(CompanionUsageContext.JOURNAL_CROP, CompanionExpression.NEUTRAL, lighting, AppearanceProfile.BASE, 88, "moru-qa-journal")
                            }
                        }
                    }
                }
            }
        }

        CompanionLightingFamily.entries.forEach { target ->
            composeRule.runOnIdle { lighting = target }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("moru-v2-qa-root").captureToImage().asAndroidBitmap()
                .writeToTestStorage("visual/moru-v2/context-matrix-${target.name.lowercase()}")
        }
    }
}

@Composable
private fun QaContext(label: String, background: Color, content: @Composable () -> Unit) {
    Surface(color = background, modifier = Modifier.size(width = 190.dp, height = 112.dp)) {
        Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = if (qaLuminance(background) < 0.4f) Color.White else Color.Black)
            content()
        }
    }
}

@Composable
private fun QaMoru(
    usage: CompanionUsageContext,
    expression: CompanionExpression,
    lighting: CompanionLightingFamily,
    affinity: AppearanceProfile,
    sizeDp: Int,
    tag: String,
) {
    Box(Modifier.size(sizeDp.dp)) {
        ProductionCompanionVisual(
            request = CompanionVisualRequest(
                companionId = "moru",
                expression = expression,
                lightingFamily = lighting,
                appearanceProfile = affinity,
                usageContext = usage,
                reducedMotion = true,
            ),
            modifier = Modifier.fillMaxSize().testTag(tag),
            contentDescription = tag,
            rasterTargetPx = 256,
        )
    }
}

private fun qaLuminance(color: Color): Float =
    0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
