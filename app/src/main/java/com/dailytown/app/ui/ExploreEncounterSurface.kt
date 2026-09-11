package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailytown.app.mystery.EncounterEvent
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.mystery.MysteryEncounter
import com.dailytown.app.mystery.MysteryReducer
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.ui.presentation.ExploreEncounterPresentation
import com.dailytown.app.ui.presentation.ExploreExperienceStep

@Composable
internal fun ExploreEncounterSurface(
    selection: EncounterSelection?,
    presentation: ExploreEncounterPresentation,
    reducer: MysteryReducer,
    progress: ExplorationProgress,
    companionBond: Int,
    showQaTools: Boolean,
    onCollectClue: (String, MysteryEncounter) -> Unit,
    onResolve: (MysteryEncounter) -> Unit,
    onOpenRecords: () -> Unit,
    onContinue: () -> Unit,
) {
    val stateTag = when (presentation.step) {
        ExploreExperienceStep.PREPARE -> "explore-state-prepare"
        ExploreExperienceStep.DETECT -> "explore-state-detect"
        ExploreExperienceStep.APPROACH -> "explore-state-approach"
        ExploreExperienceStep.DISCOVER -> "explore-state-discover"
        ExploreExperienceStep.INVESTIGATE -> "explore-state-investigate"
        ExploreExperienceStep.RECORD -> "explore-state-record"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(stateTag),
        shape = MaterialTheme.shapes.large,
        color = when (presentation.step) {
            ExploreExperienceStep.DETECT,
            ExploreExperienceStep.APPROACH -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
            ExploreExperienceStep.DISCOVER,
            ExploreExperienceStep.INVESTIGATE -> MaterialTheme.colorScheme.surfaceContainerHigh
            ExploreExperienceStep.RECORD -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
            ExploreExperienceStep.PREPARE -> MaterialTheme.colorScheme.surfaceContainer
        },
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (presentation.step) {
                ExploreExperienceStep.PREPARE -> PrepareContent()
                ExploreExperienceStep.DETECT -> SignalContent(presentation, near = false)
                ExploreExperienceStep.APPROACH -> SignalContent(presentation, near = true)
                ExploreExperienceStep.DISCOVER -> DiscoveryContent(
                    selection = requireNotNull(selection),
                    presentation = presentation,
                    reducer = reducer,
                    onCollectClue = onCollectClue,
                )
                ExploreExperienceStep.INVESTIGATE -> InvestigationContent(
                    selection = requireNotNull(selection),
                    presentation = presentation,
                    reducer = reducer,
                    onCollectClue = onCollectClue,
                    onResolve = onResolve,
                )
                ExploreExperienceStep.RECORD -> CompletionContent(
                    presentation = presentation,
                    progress = progress,
                    companionBond = companionBond,
                    onOpenRecords = onOpenRecords,
                    onContinue = onContinue,
                )
            }

            if (showQaTools && selection != null) {
                Text(
                    "QA · domain=${selection.encounter.phase.name} · clues=${selection.encounter.clueIds.size}/${selection.template.requiredClues}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("explore-domain-state"),
                )
            }
        }
    }
}

@Composable
private fun PrepareContent() {
    Text("오늘의 탐험", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Text("걸음을 시작하면 모루가 가까운 곳의 작은 신호를 알려줄 거예요.")
    Text("지도에서 주변을 확인하고 천천히 걸어보세요.", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun SignalContent(presentation: ExploreEncounterPresentation, near: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (near) "신호가 가까워지고 있어요" else presentation.signalLabel ?: "알 수 없는 신호",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        presentation.distanceMeters?.let {
            Text("약 ${it}m", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
    presentation.hint?.let { Text(it) }
    presentation.moruLine?.let { MoruLine(it) }
    if (near) {
        Text("조금만 더 가까이 가면 무엇인지 확인할 수 있어요.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DiscoveryContent(
    selection: EncounterSelection,
    presentation: ExploreEncounterPresentation,
    reducer: MysteryReducer,
    onCollectClue: (String, MysteryEncounter) -> Unit,
) {
    val encounter = selection.encounter
    Text("발견", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(presentation.title.orEmpty(), style = MaterialTheme.typography.headlineSmall)
    presentation.premise?.let { Text(it) }
    presentation.moruLine?.let { MoruLine(it) }
    Button(
        onClick = {
            val clueId = "${encounter.id}:clue-${encounter.clueIds.size + 1}"
            val updated = reducer.reduce(encounter, EncounterEvent.CollectClue(clueId))
            if (updated != encounter) onCollectClue(clueId, updated)
        },
        modifier = Modifier.testTag("encounter-start-investigation"),
    ) {
        Text("메모 살펴보기")
    }
}

@Composable
private fun InvestigationContent(
    selection: EncounterSelection,
    presentation: ExploreEncounterPresentation,
    reducer: MysteryReducer,
    onCollectClue: (String, MysteryEncounter) -> Unit,
    onResolve: (MysteryEncounter) -> Unit,
) {
    val encounter = selection.encounter
    Text(presentation.title.orEmpty(), style = MaterialTheme.typography.titleLarge)
    Text("살펴보기", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    presentation.clueLabel?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
    presentation.moruLine?.let { MoruLine(it) }
    Text(
        "확인한 단서 ${encounter.clueIds.size}/${selection.template.requiredClues}",
        style = MaterialTheme.typography.bodySmall,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (encounter.clueIds.size < selection.template.requiredClues) {
            Button(
                onClick = {
                    val clueId = "${encounter.id}:clue-${encounter.clueIds.size + 1}"
                    val updated = reducer.reduce(encounter, EncounterEvent.CollectClue(clueId))
                    if (updated != encounter) onCollectClue(clueId, updated)
                },
                modifier = Modifier.testTag("encounter-collect-clue"),
            ) {
                Text("단서 더 살펴보기")
            }
        }
        Button(
            enabled = encounter.clueIds.size >= selection.template.requiredClues,
            onClick = {
                val resolved = reducer.reduce(encounter, EncounterEvent.Resolve)
                if (resolved != encounter) onResolve(resolved)
            },
            modifier = Modifier.testTag("encounter-resolve"),
        ) {
            Text("이야기 해결")
        }
    }
}

@Composable
private fun CompletionContent(
    presentation: ExploreEncounterPresentation,
    progress: ExplorationProgress,
    companionBond: Int,
    onOpenRecords: () -> Unit,
    onContinue: () -> Unit,
) {
    Text("오늘의 기록이 생겼어요", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(presentation.title.orEmpty(), style = MaterialTheme.typography.headlineSmall)
    presentation.premise?.let { Text(it) }
    presentation.clueLabel?.let { Text("단서 · $it", style = MaterialTheme.typography.bodyMedium) }
    presentation.moruLine?.let { MoruLine(it) }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("모루와의 기억 ${progress.companionMemoryKeys.size}개 · 관계 $companionBond", style = MaterialTheme.typography.bodySmall)
        Text(
            "오늘 발견 ${progress.daily.discoveredPoiIds.size} · 단서 ${progress.daily.clueIds.size} · 해결 ${progress.daily.resolvedEncounterIds.size}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onOpenRecords, modifier = Modifier.testTag("encounter-open-records")) {
            Text("기록 보기")
        }
        OutlinedButton(onClick = onContinue, modifier = Modifier.testTag("encounter-continue")) {
            Text("계속 탐험")
        }
    }
}

@Composable
private fun MoruLine(line: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
    ) {
        Text(
            "모루 · $line",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
