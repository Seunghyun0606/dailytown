package com.dailytown.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.poi.defaultFixturePois
import com.dailytown.app.ui.visual.LocalCompanionRuntimeProfile
import com.dailytown.app.ui.visual.LocalDailyTownCompanionLighting
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.ui.visual.ProductionScenarioRasterAsset
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionRuntimeProfile
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import com.dailytown.app.visual.recentScenarioArt
import com.dailytown.app.visual.scenarioArtForPlace
import com.dailytown.app.visual.ScenarioAssetUsage

private enum class RecordsRoute { HOME, PLACE_DETAIL, CLUE_DETAIL, MEMORY_DETAIL }

@Composable
fun DailyTownRecordsScreen(progress: ExplorationProgress?, isActive: Boolean = true) {
    var route by remember { mutableStateOf(RecordsRoute.HOME) }
    var selectedPoiIndex by remember { mutableIntStateOf(0) }
    val recentPoiIds = progress?.recentPoiIds.orEmpty()
    val recentPoiTitles = progress?.recentPoiTitles.orEmpty()
    val fixtureNames = remember { defaultFixturePois().associate { it.id to it.name } }
    val selectedPoiId = recentPoiIds.getOrNull(selectedPoiIndex)
    val selectedTitle = selectedPoiId?.let {
        recentPoiTitle(selectedPoiIndex, it, recentPoiTitles, fixtureNames)
    } ?: "아직 발견하지 않은 장소"

    // System Back on a Records detail returns to Records home before the shell sends the player to Explore.
    BackHandler(enabled = isActive && route != RecordsRoute.HOME) { route = RecordsRoute.HOME }

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

        RecordSection(title = "오늘의 기록", modifier = Modifier.testTag("records-today")) {
            val daily = progress?.daily
            StatLine("발견", "${daily?.discoveredPoiIds?.size ?: 0}곳")
            StatLine("단서", "${daily?.clueIds?.size ?: 0}개")
            StatLine("해결", "${daily?.resolvedEncounterIds?.size ?: 0}건")
        }

        RecordSection(title = "장소", modifier = Modifier.testTag("records-places")) {
            if (recentPoiIds.isEmpty()) {
                Text("아직 기록된 장소가 없어요.")
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
                            Text(if (remembered) "모루와 공유한 기억 있음" else "발견 기록", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("보기 ›", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        RecordSection(title = "미스터리", modifier = Modifier.testTag("records-mysteries")) {
            Text("해결한 동네 이야기 ${progress?.resolvedEncounterIds?.size ?: 0}건")
            Text(
                "하나하나 풀어 간 이야기가 이곳에 쌓여요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RecordSection(
            title = "단서",
            modifier = Modifier.clickable(onClick = onOpenClues).testTag("records-clues"),
        ) {
            Text("관찰한 단서 ${progress?.inventoryClueIds?.size ?: 0}개")
            Text("단서 기록 보기 ›", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        RecordSection(
            title = "Moru와의 기억",
            modifier = Modifier.clickable(onClick = onOpenMemories).testTag("records-memories"),
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
    val placeArt = poiId?.let { scenarioArtForPlace(it, progress?.recentPairKeys.orEmpty()) }
    val hasPlaceArt = placeArt != null
    DetailPage(tag = "record-discovery-detail", title = "장소 기록", onBack = onBack, backTag = "discovery-back") {
        if (hasPlaceArt) {
            ProductionScenarioRasterAsset(
                semanticKey = requireNotNull(placeArt).place,
                usage = ScenarioAssetUsage.RECORDS_HEADER,
                contentDescription = "${placeArt.placeDescription} 장소 기록",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(8f / 3f)
                    .testTag("scenario-place-records"),
                contentScale = ContentScale.Crop,
            )
        }
        RecordSection(title = title) {
            Text("이 장소에서 탐험 신호를 발견해 기록했습니다.")
            Text(if (remembered) "모루와 공유한 장소 기억이 저장되어 있어요." else "현재 저장된 공유 기억은 아직 없어요.")
        }
        RecordSection(title = "연결된 진행") {
            Text("전체 단서 ${progress?.inventoryClueIds?.size ?: 0}개 · 해결 ${progress?.resolvedEncounterIds?.size ?: 0}건")
            TextButton(onClick = onOpenClues, modifier = Modifier.testTag("discovery-open-clue")) { Text("단서 기록 보기") }
        }
        if (remembered) {
            RecordSection(title = "Moru") {
                Text("모루가 이 장소를 기억하고 있어요.")
                TextButton(onClick = onOpenMemories, modifier = Modifier.testTag("discovery-open-memory")) { Text("Moru와의 기억 보기") }
            }
        }
        Text("다시 찾아오면 전과는 다른 이야기가 기다리고 있을지도 몰라요.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ClueRecordDetail(progress: ExplorationProgress?, onBack: () -> Unit) {
    DetailPage(tag = "record-clue-note", title = "단서", onBack = onBack, backTag = "clue-back") {
        RecordSection(title = "관찰한 단서") {
            val count = progress?.inventoryClueIds?.size ?: 0
            val oldGinkgoClueCount = progress?.daily?.clueIds?.size ?: 0
            Text("지금까지 동네에서 살펴본 단서 ${count}개가 남아 있어요.")
            if (oldGinkgoClueCount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductionScenarioRasterAsset(
                        semanticKey = recentScenarioArt(progress?.recentTemplateIds.orEmpty()).clueMain,
                        usage = ScenarioAssetUsage.CLUE_ART,
                        contentDescription = "접힌 메모 단서",
                        modifier = Modifier
                            .size(112.dp)
                            .testTag("scenario-note-records"),
                    )
                    if (oldGinkgoClueCount > 1) {
                        ProductionScenarioRasterAsset(
                            semanticKey = recentScenarioArt(progress?.recentTemplateIds.orEmpty()).clueKeepsake,
                            usage = ScenarioAssetUsage.CLUE_ART,
                            contentDescription = "은행잎 단서",
                            modifier = Modifier
                                .size(112.dp)
                                .testTag("scenario-leaf-records"),
                        )
                    }
                }
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
        if (LocalCompanionRuntimeProfile.current == CompanionRuntimeProfile.MORU_CANONICAL_V2) {
            ProductionCompanionVisual(
                request = CompanionVisualRequest(
                    companionId = "moru",
                    expression = if (memories.isEmpty()) CompanionExpression.NEUTRAL else CompanionExpression.RESOLVED,
                    lightingFamily = LocalDailyTownCompanionLighting.current,
                    appearanceProfile = AppearanceProfile.BASE,
                    usageContext = CompanionUsageContext.JOURNAL_CROP,
                    reducedMotion = true,
                ),
                modifier = Modifier.size(112.dp).testTag("moru-v2-journal-crop"),
                contentDescription = "기록 속 동행 캐릭터 Moru",
                rasterTargetPx = 256,
            )
        }
        if (memories.isEmpty()) {
            RecordSection(title = "아직 빈 페이지") {
                Text("모루와 함께 동네 이야기를 풀면 그 장소와 순간이 여기에 남아요.")
            }
        } else {
            val hasSessionArt = !progress?.daily?.resolvedEncounterIds.isNullOrEmpty()
            if (hasSessionArt) {
                ProductionScenarioRasterAsset(
                    semanticKey = recentScenarioArt(progress?.recentTemplateIds.orEmpty()).memory,
                    usage = ScenarioAssetUsage.RECORDS_CARD,
                    contentDescription = "${recentScenarioArt(progress?.recentTemplateIds.orEmpty()).placeDescription}에서 모루와 함께한 기억",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .testTag("scenario-memory-records"),
                    contentScale = ContentScale.Crop,
                )
            }
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
                Text("함께 풀어 본 이야기의 종류 ${mechanicMemoryCount}가지")
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
private fun RecordSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
