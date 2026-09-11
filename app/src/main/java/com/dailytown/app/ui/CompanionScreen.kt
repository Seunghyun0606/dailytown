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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.ui.visual.LocalDailyTownCompanionLighting
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest

private enum class CompanionPanel { NONE, TALK, MEMORIES, GIFTS }

@Composable
internal fun CompanionScreen(progress: ExplorationProgress?) {
    val bond = progress?.companionBond?.takeIf { it > 0 } ?: 12
    val lighting = LocalDailyTownCompanionLighting.current
    var panel by remember { mutableStateOf(CompanionPanel.NONE) }
    val recentRememberedPlace = progress?.recentPoiIds.orEmpty()
        .mapIndexedNotNull { index, poiId ->
            if ("poi:$poiId" !in progress?.companionMemoryKeys.orEmpty()) null
            else progress?.recentPoiTitles?.getOrNull(index)?.takeIf { it.isNotBlank() }
        }
        .firstOrNull()
    val hasTodayDiscovery = (progress?.daily?.discoveredPoiIds?.size ?: 0) > 0
    val mood = if (hasTodayDiscovery) "호기심 가득" else "산책 준비 중"
    val contextualLine = when {
        recentRememberedPlace != null -> "$recentRememberedPlace 이야기가 아직 기억나. 다음에는 뭐가 달라졌는지 보고 싶어."
        !progress?.companionMemoryKeys.isNullOrEmpty() -> "우리 둘만 아는 산책 기억이 조금씩 쌓이고 있어."
        else -> "오늘은 가까운 골목부터 천천히 걸어볼까?"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp)
            .testTag("companion-product-screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("오늘의 동행", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Moru", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("같이 걸었던 장소와 해결한 이야기가 관계의 맥락이 됩니다.")
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProductionCompanionVisual(
                    request = CompanionVisualRequest(
                        companionId = "moru",
                        expression = if (hasTodayDiscovery) CompanionExpression.CURIOUS else CompanionExpression.NEUTRAL,
                        lightingFamily = lighting,
                        appearanceProfile = AppearanceProfile.BASE,
                        usageContext = CompanionUsageContext.RESULT_LARGE,
                        reducedMotion = true,
                    ),
                    modifier = Modifier.size(248.dp),
                    contentDescription = "동행 캐릭터 Moru",
                    rasterTargetPx = 384,
                )
                Text(mood, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("“$contextualLine”", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().testTag("companion-relationship"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("우리의 관계", style = MaterialTheme.typography.titleMedium)
                Text("관계 단계 · ${relationshipTierLabel(bond)}")
                Text("호감도 $bond", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().testTag("companion-recent-memory"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("최근 함께한 기억", style = MaterialTheme.typography.titleMedium)
                Text(recentRememberedPlace ?: "아직 이름이 남은 장소 기억은 없어요.")
                Text("함께 남긴 기억 ${progress?.companionMemoryKeys?.size ?: 0}개", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { panel = if (panel == CompanionPanel.TALK) CompanionPanel.NONE else CompanionPanel.TALK },
                modifier = Modifier.weight(1f).testTag("companion-talk"),
            ) { Text("대화") }
            OutlinedButton(
                onClick = { panel = if (panel == CompanionPanel.MEMORIES) CompanionPanel.NONE else CompanionPanel.MEMORIES },
                modifier = Modifier.weight(1f).testTag("companion-memories"),
            ) { Text("기억") }
            OutlinedButton(
                onClick = { panel = if (panel == CompanionPanel.GIFTS) CompanionPanel.NONE else CompanionPanel.GIFTS },
                modifier = Modifier.weight(1f).testTag("companion-gifts"),
            ) { Text("선물") }
        }

        when (panel) {
            CompanionPanel.NONE -> Unit
            CompanionPanel.TALK -> PanelSurface("Moru의 한마디") {
                Text(contextualLine)
                Text("현재 탐험과 기억에 맞춘 짧은 반응이에요.", style = MaterialTheme.typography.bodySmall)
            }
            CompanionPanel.MEMORIES -> PanelSurface("함께한 기억") {
                val rememberedTitles = progress?.recentPoiIds.orEmpty().mapIndexedNotNull { index, poiId ->
                    if ("poi:$poiId" !in progress?.companionMemoryKeys.orEmpty()) null
                    else progress?.recentPoiTitles?.getOrNull(index)?.takeIf { it.isNotBlank() }
                }
                if (rememberedTitles.isEmpty()) Text("아직 장소 이름이 남은 기억은 없어요.")
                else rememberedTitles.take(5).forEach { Text("• $it") }
            }
            CompanionPanel.GIFTS -> PanelSurface("선물 · 기념품") {
                Text("아직 건넬 수 있는 기념품은 없어요.")
                Text("지금은 함께한 장소와 기억을 쌓아가요.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().testTag("companion-long-history"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("함께 걸어온 기록", style = MaterialTheme.typography.titleMedium)
                StatRow("발견한 장소", "${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                StatRow("해결한 미스터리", "${progress?.resolvedEncounterIds?.size ?: 0}건")
                StatRow("수집한 단서", "${progress?.inventoryClueIds?.size ?: 0}개")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PanelSurface(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

private fun relationshipTierLabel(bond: Int): String = when {
    bond >= 50 -> "가까운 사이"
    bond >= 20 -> "익숙한 사이"
    else -> "새로운 사이"
}
