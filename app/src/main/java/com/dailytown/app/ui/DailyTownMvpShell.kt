package com.dailytown.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.persistence.ProgressStore
import com.dailytown.app.poi.PoiRepository
import com.dailytown.app.progress.ProgressRuntimeCoordinator
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.visual.DailyTownTheme
import com.dailytown.app.ui.visual.DailyTownTokens

private enum class MvpSection(val label: String, val symbol: String, val testTag: String) {
    EXPLORE("탐험", "⌖", "nav-explore"),
    COMPANION("동행", "●", "nav-companion"),
    RECORDS("기록", "▦", "nav-collection"),
    GOALS("목표", "✓", "nav-goals"),
    SETTINGS("설정", "⚙", "nav-settings"),
}

@Composable
fun DailyTownMvpShell(
    mapAdapter: MapViewAdapter,
    progressStore: ProgressStore,
    poiRepository: PoiRepository,
    reminderManager: LocalReminderManager,
) {
    var selectedSection by rememberSaveable { mutableStateOf(MvpSection.EXPLORE) }
    var qaMode by rememberSaveable { mutableStateOf(false) }
    val progressCoordinator = remember(progressStore) { ProgressRuntimeCoordinator(progressStore) }
    val progressRuntime by progressCoordinator.state.collectAsState()
    val progress = progressRuntime.progress.takeIf { progressRuntime.ready }
    val dailyGoals = progressRuntime.dailyGoals
    val weeklyGoals = progressRuntime.weeklyGoals
    val poiSources = remember(poiRepository) { poiRepository.sourceMetadata() }

    fun openExplore() {
        qaMode = false
        selectedSection = MvpSection.EXPLORE
    }

    BackHandler(enabled = selectedSection != MvpSection.EXPLORE) {
        openExplore()
    }

    DailyTownTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    MvpSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = selectedSection == section,
                            onClick = {
                                selectedSection = section
                                if (section == MvpSection.EXPLORE) qaMode = false
                            },
                            icon = {
                                Text(
                                    section.symbol,
                                    fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            label = { Text(section.label) },
                            modifier = Modifier.testTag(section.testTag),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = DailyTownTokens.LeafSecondary.copy(alpha = 0.42f),
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Keep Explore composed while another tab is visible so location/gameplay runtime survives.
                SectionLayer(active = selectedSection == MvpSection.EXPLORE) {
                    DailyTownApp(
                        mapAdapter = mapAdapter,
                        progressCoordinator = progressCoordinator,
                        poiRepository = poiRepository,
                        showQaTools = qaMode,
                        onOpenRecords = {
                            qaMode = false
                            selectedSection = MvpSection.RECORDS
                        },
                    )
                }
                SectionLayer(active = selectedSection == MvpSection.COMPANION) {
                    CompanionScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.RECORDS) {
                    DailyTownRecordsScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.GOALS) {
                    GoalsScreen(progress, dailyGoals, weeklyGoals, onOpenExplore = ::openExplore)
                }
                SectionLayer(active = selectedSection == MvpSection.SETTINGS) {
                    SettingsScreen(
                        reminderManager = reminderManager,
                        poiSources = poiSources,
                        onOpenQa = {
                            qaMode = true
                            selectedSection = MvpSection.EXPLORE
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SectionLayer(
    active: Boolean,
    content: @Composable () -> Unit,
) {
    val visibilityModifier = if (active) Modifier else Modifier.clearAndSetSemantics { }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (active) 1f else 0f)
            .alpha(if (active) 1f else 0f)
            .then(visibilityModifier),
    ) {
        content()
    }
}
