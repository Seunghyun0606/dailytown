package com.dailytown.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.poi.defaultFixturePois
import com.dailytown.app.ui.visual.A3ClueCard
import com.dailytown.app.ui.visual.A3CollectionGrid
import com.dailytown.app.ui.visual.A3CompanionStamp
import com.dailytown.app.ui.visual.A3PaperSurface
import com.dailytown.app.ui.visual.SemanticAssetRenderer
import com.dailytown.app.ui.visual.rememberProductionA3AssetRenderer
import com.dailytown.app.visual.A3ClueState
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.SemanticAssetKey
import kotlin.math.roundToInt

private enum class RecordRoute { JOURNAL_HOME, DISCOVERY_DETAIL, CLUE_NOTE, COLLECTION_GRID, MEMORY_DETAIL }

@Composable
fun DailyTownRecordsScreen(progress: ExplorationProgress?) {
    var route by remember { mutableStateOf(RecordRoute.JOURNAL_HOME) }
    var selectedPoiIndex by remember { mutableIntStateOf(0) }
    val assetRenderer = rememberProductionA3AssetRenderer()
    val recentPoiIds = progress?.recentPoiIds.orEmpty()
    val fixtureNames = remember { defaultFixturePois().associate { it.id to it.name } }
    val selectedPoiId = recentPoiIds.getOrNull(selectedPoiIndex)
    val selectedTitle = selectedPoiId?.let { fixtureNames[it] }
        ?: selectedPoiId?.let { "최근 발견 ${selectedPoiIndex + 1}" }
        ?: "아직 발견하지 않은 장소"

    when (route) {
        RecordRoute.JOURNAL_HOME -> JournalHome(progress, recentPoiIds, fixtureNames, assetRenderer, { index -> selectedPoiIndex = index; route = RecordRoute.DISCOVERY_DETAIL }, { route = RecordRoute.COLLECTION_GRID }, { route = RecordRoute.MEMORY_DETAIL })
        RecordRoute.DISCOVERY_DETAIL -> DiscoveryDetail(progress, selectedTitle, assetRenderer, { route = RecordRoute.JOURNAL_HOME }, { route = RecordRoute.CLUE_NOTE }, { route = RecordRoute.MEMORY_DETAIL })
        RecordRoute.CLUE_NOTE -> ClueNote(progress, selectedTitle, assetRenderer) { route = RecordRoute.DISCOVERY_DETAIL }
        RecordRoute.COLLECTION_GRID -> CollectionGrid(progress, recentPoiIds, fixtureNames, assetRenderer, { route = RecordRoute.JOURNAL_HOME }, { route = RecordRoute.MEMORY_DETAIL }) { index -> selectedPoiIndex = index; route = RecordRoute.DISCOVERY_DETAIL }
        RecordRoute.MEMORY_DETAIL -> MemoryDetail(progress, selectedTitle, assetRenderer, { route = RecordRoute.JOURNAL_HOME }) { route = RecordRoute.COLLECTION_GRID }
    }
}

@Composable
private fun JournalHome(
    progress: ExplorationProgress?, recentPoiIds: List<String>, fixtureNames: Map<String, String>,
    assetRenderer: SemanticAssetRenderer, onOpenDiscovery: (Int) -> Unit, onOpenCollection: () -> Unit, onOpenMemory: () -> Unit,
) {
    A3Page(A3Screen.JOURNAL_HOME, "record-journal-home", assetRenderer) {
        A3Title("탐험 일지", "걸었던 동네와 발견한 이야기가 차곡차곡 남아요.")
        RecordTabs(RecordRoute.JOURNAL_HOME, {}, onOpenCollection, onOpenMemory)
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("이번까지의 기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatLine("발견", "${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                StatLine("단서", "${progress?.inventoryClueIds?.size ?: 0}개")
                StatLine("해결", "${progress?.resolvedEncounterIds?.size ?: 0}건")
            }
        }
        Text("최근 기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (recentPoiIds.isEmpty()) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    A3Asset(assetRenderer, "sticker.discovery.default", Modifier.size(84.dp))
                    Text("아직 기록된 발견이 없어요.")
                    Text("탐험을 시작하면 첫 페이지가 만들어집니다.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            recentPoiIds.take(8).forEachIndexed { index, poiId ->
                val title = fixtureNames[poiId] ?: "최근 발견 ${index + 1}"
                val remembered = "poi:$poiId" in progress?.companionMemoryKeys.orEmpty()
                ElevatedCard(Modifier.fillMaxWidth().clickable { onOpenDiscovery(index) }.testTag("journal-entry-$index")) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        A3Asset(assetRenderer, "sticker.discovery.default", Modifier.size(58.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            Text("발견됨${if (remembered) " · 모루와 기억함" else ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        A3CompanionStamp(assetRenderer, sizeDp = 40)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryDetail(progress: ExplorationProgress?, title: String, assetRenderer: SemanticAssetRenderer, onBack: () -> Unit, onOpenClue: () -> Unit, onOpenMemory: () -> Unit) {
    A3Page(A3Screen.DISCOVERY_DETAIL, "record-discovery-detail", assetRenderer) {
        A3BackHeader("발견 기록", "discovery-back", onBack)
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                A3Asset(assetRenderer, "sticker.discovery.default", Modifier.fillMaxWidth(0.58f).aspectRatio(1f))
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text("최근 탐험에서 발견한 장소", style = MaterialTheme.typography.bodySmall)
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("발견 메모", style = MaterialTheme.typography.titleMedium)
                Text("이 장소에서 모루와 함께 주변 신호를 확인했어요. 세부 이야기와 날짜 정보는 실제 콘텐츠 데이터가 저장되는 범위 안에서만 표시합니다.")
                Text("누적 이동 ${progress?.distanceWalkedMeters?.roundToInt() ?: 0}m", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (!progress?.inventoryClueIds.isNullOrEmpty()) {
            ElevatedCard(Modifier.fillMaxWidth().clickable(onClick = onOpenClue).testTag("discovery-open-clue")) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("단서 노트", style = MaterialTheme.typography.titleMedium)
                        Text("수집한 단서 ${progress?.inventoryClueIds?.size ?: 0}개", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("열기 ›")
                }
            }
        }
        if (!progress?.companionMemoryKeys.isNullOrEmpty()) {
            TextButton(onClick = onOpenMemory, modifier = Modifier.testTag("discovery-open-memory")) { Text("모루와 남긴 기억 보기") }
        }
    }
}

@Composable
private fun ClueNote(progress: ExplorationProgress?, sourceTitle: String, assetRenderer: SemanticAssetRenderer, onBack: () -> Unit) {
    val resolved = !progress?.resolvedEncounterIds.isNullOrEmpty()
    A3Page(A3Screen.CLUE_NOTE, "record-clue-note", assetRenderer) {
        A3BackHeader("단서 노트", "clue-back", onBack)
        Text(if (resolved) "해결된 단서" else "아직 이어지는 단서", style = MaterialTheme.typography.headlineSmall)
        A3ClueCard(if (resolved) A3ClueState.RESOLVED else A3ClueState.UNRESOLVED, assetRenderer, Modifier.fillMaxWidth().height(210.dp)) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(if (resolved) "✓ SOLVED" else "? UNRESOLVED", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("관찰한 작은 흔적", style = MaterialTheme.typography.titleLarge)
                    Text("탐험 중 실제로 수집된 단서 ${progress?.inventoryClueIds?.size ?: 0}개가 이 노트에 연결되어 있어요.")
                }
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("발견 위치", style = MaterialTheme.typography.labelLarge)
                Text(sourceTitle)
                Text("세부 단서 문장은 authored scenario pack이 연결되는 범위에서 표시합니다.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            A3CompanionStamp(assetRenderer, sizeDp = 48)
            Text(if (resolved) "모루도 이 단서를 기억하고 있어요." else "모루가 다음 흔적을 기다리고 있어요.")
        }
    }
}

@Composable
private fun CollectionGrid(
    progress: ExplorationProgress?, recentPoiIds: List<String>, fixtureNames: Map<String, String>, assetRenderer: SemanticAssetRenderer,
    onOpenJournal: () -> Unit, onOpenMemory: () -> Unit, onOpenDiscovery: (Int) -> Unit,
) {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val filled = recentPoiIds.take(8)
    val slots = maxOf(6, filled.size)
    A3Page(A3Screen.COLLECTION_GRID, "record-collection-grid", assetRenderer) {
        A3Title("동네 컬렉션", "발견한 장소와 아직 만나지 못한 자리를 한눈에 봅니다.")
        RecordTabs(RecordRoute.COLLECTION_GRID, onOpenJournal, {}, onOpenMemory)
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("수집 ${progress?.encounterVisitedPoiIds?.size ?: 0}")
                Text("해결 ${progress?.resolvedEncounterIds?.size ?: 0}")
                Text("단서 ${progress?.inventoryClueIds?.size ?: 0}")
            }
        }
        A3CollectionGrid(widthDp = widthDp, modifier = Modifier.fillMaxWidth()) { columns ->
            val rows = (slots + columns - 1) / columns
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(columns) { column ->
                        val slot = row * columns + column
                        if (slot >= slots) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            val poiId = filled.getOrNull(slot)
                            val unlocked = poiId != null
                            Column(
                                Modifier.weight(1f).then(if (unlocked) Modifier.clickable { onOpenDiscovery(slot) } else Modifier)
                                    .testTag(if (unlocked) "collection-entry-$slot" else "collection-locked-$slot"),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                A3Asset(assetRenderer, if (unlocked) "sticker.discovery.default" else "collection.locked.pattern", Modifier.fillMaxWidth().aspectRatio(1f))
                                Text(if (poiId != null) fixtureNames[poiId] ?: "발견 ${slot + 1}" else "아직 미발견", style = MaterialTheme.typography.bodySmall)
                                Text(if (unlocked) "✓ 수집됨" else "◇ LOCKED", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryDetail(progress: ExplorationProgress?, title: String, assetRenderer: SemanticAssetRenderer, onBack: () -> Unit, onOpenCollection: () -> Unit) {
    A3Page(A3Screen.MEMORY_DETAIL, "record-memory-detail", assetRenderer) {
        A3BackHeader("기억", "memory-back", onBack)
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                A3Asset(assetRenderer, "sticker.discovery.default", Modifier.fillMaxWidth(0.72f).aspectRatio(1f))
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(if (progress?.companionMemoryKeys.isNullOrEmpty()) "아직 모루와 남긴 기억이 없어요." else "함께 걸으며 남긴 기억 ${progress?.companionMemoryKeys?.size ?: 0}개 중 한 장면")
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    A3Asset(assetRenderer, "stamp.memory.resolved", Modifier.size(58.dp))
                    A3CompanionStamp(assetRenderer, sizeDp = 64)
                }
            }
        }
        Text("기억은 실제 탐험 기록에서 파생되며, 저장되지 않은 시간·장소 설명을 임의로 만들어내지 않습니다.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = onOpenCollection, modifier = Modifier.testTag("memory-open-collection")) { Text("컬렉션으로 이동") }
    }
}

@Composable
private fun A3Page(screen: A3Screen, testTag: String, assetRenderer: SemanticAssetRenderer, content: @Composable () -> Unit) {
    A3PaperSurface(screen, assetRenderer, Modifier.fillMaxSize().testTag(testTag)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            content()
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun A3Title(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun A3BackHeader(title: String, testTag: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.testTag(testTag)) { Text("‹ 뒤로") }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RecordTabs(selected: RecordRoute, onJournal: () -> Unit, onCollection: () -> Unit, onMemory: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected == RecordRoute.JOURNAL_HOME, onJournal, { Text("일지") }, modifier = Modifier.testTag("record-tab-journal"))
        FilterChip(selected == RecordRoute.COLLECTION_GRID, onCollection, { Text("컬렉션") }, modifier = Modifier.testTag("record-tab-collection"))
        FilterChip(selected == RecordRoute.MEMORY_DETAIL, onMemory, { Text("기억") }, modifier = Modifier.testTag("record-tab-memory"))
    }
}

@Composable
private fun A3Asset(assetRenderer: SemanticAssetRenderer, key: String, modifier: Modifier) {
    Box(modifier) { assetRenderer(SemanticAssetKey(key), Modifier.fillMaxSize()) }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
