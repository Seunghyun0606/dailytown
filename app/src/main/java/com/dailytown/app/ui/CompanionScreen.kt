package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.BuildConfig
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.ui.visual.A3CompanionStamp
import com.dailytown.app.ui.visual.A3PaperSurface
import com.dailytown.app.ui.visual.LocalDailyTownCompanionLighting
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.ui.visual.rememberProductionA3AssetRenderer
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest

@Composable
internal fun CompanionScreen(progress: ExplorationProgress?) {
    val bond = progress?.companionBond?.takeIf { it > 0 } ?: 12
    val lighting = LocalDailyTownCompanionLighting.current
    val a3AssetRenderer = rememberProductionA3AssetRenderer()
    A3PaperSurface(
        screen = A3Screen.MEMORY_DETAIL,
        assetRenderer = a3AssetRenderer,
        modifier = Modifier.fillMaxSize().testTag("companion-product-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("오늘의 동행", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("모루", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("같이 걸었던 시간이 모루의 기억이 됩니다.")
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProductionCompanionVisual(
                        request = CompanionVisualRequest(
                            companionId = "moru",
                            expression = CompanionExpression.NEUTRAL,
                            lightingFamily = lighting,
                            appearanceProfile = AppearanceProfile.BASE,
                            usageContext = CompanionUsageContext.RESULT_LARGE,
                        ),
                        modifier = Modifier.size(248.dp),
                        contentDescription = "동행 캐릭터 모루",
                        rasterTargetPx = 384,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        A3CompanionStamp(assetRenderer = a3AssetRenderer, sizeDp = 48)
                        Column {
                            Text("호감도 $bond", style = MaterialTheme.typography.titleMedium)
                            Text("함께 발견한 기억 ${progress?.companionMemoryKeys?.size ?: 0}개", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("우리의 산책", style = MaterialTheme.typography.titleMedium)
                    StatRow("발견한 장소", "${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                    StatRow("해결한 미스터리", "${progress?.resolvedEncounterIds?.size ?: 0}건")
                    StatRow("수집한 단서", "${progress?.inventoryClueIds?.size ?: 0}개")
                }
            }
            Text(
                if (BuildConfig.DEBUG) "M-B motion prototype · final timing pending" else "M-B static fallback",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(value)
    }
}
