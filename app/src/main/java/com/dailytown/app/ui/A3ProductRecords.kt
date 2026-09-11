package com.dailytown.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.poi.defaultFixturePois

private enum class RecordsRoute { HOME, PLACE_DETAIL, CLUE_DETAIL, MEMORY_DETAIL }

@Composable
fun DailyTownRecordsScreen(progress: ExplorationProgress?) {
    var route by remember { mutableStateOf(RecordsRoute.HOME) }
    var selectedPoiIndex by remember { mutableIntStateOf(0) }
    val recentPoiIds = progress?.recentPoiIds.orEmpty()
    val recentPoiTitles = progress?.recentPoiTitles.orEmpty()
    val fixtureNames = remember { defaultFixturePois().associate { it.id to it.name } }
    val selectedPoiId = recentPoiIds.getOrNull(selectedPoiIndex)
    val selectedTitle = selectedPoiId?.let {
        recentPoiTitle(selectedPoiIndex, it, recentPoiTitles, fixtureNames)
    } ?: "아직 발견하지 않은 장소"

    when (route) {
        RecordsRoute.HOME -> RecordsHome(
            progress = progress,
            recentPoiIds = recentPoiIds,
            recentPoiTitles = recentPoiTitles,
            fixtureNames = fixtureNames,
            onOpenPlace = { index ->
                selectedPoiIndex = index
                route = RecordsRoute.PLACE_DETAIL
            },
            onOpenClues = { route = RecordsRoute.CLUE_DETAIL },
            onOpenMemories = { route = RecordsRoute.MEMORY_DETAIL },
        )
        RecordsRoute.PLACE_DETAIL -> PlaceRecordDetail(
            progress = progress,
            poiId = selectedPoiId,
            title = selectedTitle,
            onBack = { route = RecordsRoute.HOME },
            onOpenClues = { route = RecordsRoute.CLUE_DETAIL },
            onOpenMemories = { route = RecordsRoute.MEMORY_DETAIL },
        )
        RecordsRoute.CLUE_DETAIL -> ClueRecordDetail(
            progress = progress,
            onBack = { route = RecordsRoute.HOME },
        )
        RecordsRoute.MEMORY_DETAIL -> MemoryRecordDetail(
            progress = progress,
            recentPoiIds = recentPoiIds,
            recentPoiTitles = recentPoiTitles,
            fixtureNames = fixtureNames,
            onBack = { route = RecordsRoute.HOME },
        )
    }
}

@Composable
private fun RecordsHome(
    progress: ExplorationProgress?,
    recentPoiIds: List<String>,
    recentPoiTitles: List<String>,
    fixtureNames: Map<String, String>,
    onOpenPlace: (Int) -> Unit,
    onOpenClues: () -> Unit,
    onOpenMemories: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("record-journal-home"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("탐험 기록", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("산책에서 실제로 남은 장소, 미스터리, 단서와 모루의 기억을 모아봅니다.")
        }

        RecordSection(
            title = "오늘의 기록",
            modifier = Modifier.testTag("records-today"),
        ) {
            val daily = progress?.daily
            StatLine("발견", "${daily?.discoveredPoiIds?.size ?: 0}곳")
            StatLine("단서", "${daily?.clueIds?.size ?: 0}개")
            StatLine("해결", "${daily?.resolvedEncounterIds?.size ?: 0}건")
        }

        RecordSection(
            title = "장소",
            modifier = Modifier.testTag("records-places"),
        ) {
            if (recentPoiIds.isEmpty()) {
                Text("아직 기록된 장소가 없어요.", style = MaterialTheme.typography.bodyMedium)
            } else {
                recentPoiIds.take(6).forEachIndexed { index, poiId ->
                    val title = recentPoiTitle(index, poiId, recentPoiTitles, fixtureNames)
                    val remembered = "poi:$poiId" in progress?.companionMemoryKeys.orEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPlace(index) }
                            .padding(vertical = 6.dp)
                            .testTag("journal-entry-$index"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (remembered) "모루와 공유한 기억 있음" else "발견 기록",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text("보기 ›", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        RecordSection(
            title = "미스터리",
            modifier = Modifier.testTag("records-mysteries"),
        ) {
            Text("해결한 동네 이야기 ${progress?.resolvedEncounterIds?.size ?: 0}건")
            Text(
                "여러 조각을 1/4→4/4로 모으는 장기 미스터리는 아직 만들지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RecordSection(
            title = "단서",
            modifier = Modifier
                .clickable(onClick = onOpenClues)
                .testTag("records-clues"),
        ) {
            Text("관찰한 단서 ${progress?.inventoryClueIds?.size ?: 0}개")
            Text("단서 기록 보기 ›", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        RecordSection(
            title = "Moru와의 기억",
            modifier = Modifier
                .clickable(onClick = onOpenMemories)
                .testTag("records-memories"),
        ) {
            Text("함께 남긴 기억 ${progress?.companionMemoryKeys?.size ?: 0}개")
            Text("함께한 기억 보기 ›", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PlaceRecordDetail(
    progress: ExplorationProgress?,
    poiId: String?,
    title: String,
    onBack: () -> Unit,
    onOpenClues: () -> Unit,
    onOpenMemories: () -> Unit,
) {
    val remembered = poiId != null && "poi:$poiId" in progress?.companionMemoryKeys.orEmpty()
    DetailPage(tag = "record-discovery-detail", title = "장소 기록", onBack = onBack, backTag = "discovery-back") {
        RecordSection(title = title) {
            Text("이 장소에서 탐험 신호를 발견해 기록했습니다.")
            Text(
                if (remembered) "모루와 공유한 장소 기억이 저장되어 있어요."
                else "현재 저장된 공유 기억은 아직 없어요.",
            )
        }
        RecordSection(title = "연결된 진행") {
            Text("전체 단서 ${progress?.inventoryClueIds?.size ?: 0}개 · 해결 ${progress?.resolvedEncounterIds?.size ?: 0}건")
            Text(
                "현재 persistence는 장소와 각 단서/해결 결과의 세부 1:1 문장을 별도 필드로 저장하지 않으므로 없는 연결을 만들어 표시하지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onOpenClues, modifier = Modifier.testTag("discovery-open-clue")) { Text("단서 기록 보기") }
        }
        if (remembered) {
            RecordSection(title = "Moru") {
                Text("이 장소는 모루의 semantic memory에 남아 있습니다.")
                TextButton(onClick = onOpenMemories, modifier = Modifier.testTag("discovery-open-memory")) { Text("Moru와의 기억 보기") }
            }
        }
        Text(
            "재방문 시 기존 방문·memory 이력은 encounter rotation/weighting에 활용될 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ClueRecordDetail(
    progress: ExplorationProgress?,
    onBack: () -> Unit,
) {
    DetailPage(tag = "record-clue-note", title = "단서", onBack = onBack, backTag = "clue-back") {
        RecordSection(title = "관찰한 단서") {
            val count = progress?.inventoryClueIds?.size ?: 0
            Text("지금까지 실제 encounter에서 수집한 단서 $count개가 저장되어 있어요.")
            if (count > 0) {
                Text("첫 UX validation에서는 ‘오래된 가로수의 메모’의 표시를 authored copy로 보여주되, 저장 ID는 기존 encounter clue contract를 그대로 사용합니다.", style = MaterialTheme.typography.bodySmall)
            }
        }
        RecordSection(title = "해결 상태") {
            Text("해결한 미스터리 ${progress?.resolvedEncounterIds?.size ?: 0}건")
        }
    }
}

@Composable
private fun MemoryRecordDetail(
    progress: ExplorationProgress?,
    recentPoiIds: List<String>,
    recentPoiTitles: List<String>,
    fixtureNames: Map<String, String>,
    onBack: () -> Unit,
) {
    DetailPage(tag = "record-memory-detail", title = "Moru와의 기억", onBack = onBack, backTag = "memory-back") {
        val memories = progress?.companionMemoryKeys.orEmpty()
        if (memories.isEmpty()) {
            RecordSection(title = "아직 빈 페이지") {
                Text("함께 탐험을 해결하면 장소와 행동의 semantic memory가 남아요.")
            }
        } else {
            val rememberedPlaces = recentPoiIds.mapIndexedNotNull { index, poiId ->
                if ("poi:$poiId" !in memories) null
                else recentPoiTitle(index, poiId, recentPoiTitles, fixtureNames)
            }
            RecordSection(title = "최근 함께 기억한 장소") {
                if (rememberedPlaces.isEmpty()) Text("장소 이름이 남은 최근 기억은 아직 없어요.")
                else rememberedPlaces.take(5).forEach { Text("• $it") }
            }
            val mechanicMemoryCount = memories.count { it.startsWith("mechanic:") }
            RecordSection(title = "함께 해결한 방식") {
                Text("미스터리 해결에서 남은 semantic memory $mechanicMemoryCount개")
            }
        }
    }
}

@Composable
private fun DetailPage(
    tag: String,
    title: String,
    onBack: () -> Unit,
    backTag: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onBack, modifier = Modifier.testTag(backTag)) { Text("뒤로") }
        }
        content()
    }
}

@Composable
private fun RecordSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

private fun recentPoiTitle(
    index: Int,
    poiId: String,
    recentPoiTitles: List<String>,
    fixtureNames: Map<String, String>,
): String = recentPoiTitles.getOrNull(index)
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: fixtureNames[poiId]
    ?: "최근 발견 ${index + 1}"
