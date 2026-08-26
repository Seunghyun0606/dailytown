package com.dailytown.app.visualqa

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.ui.visual.A3ClueCard
import com.dailytown.app.ui.visual.A3CollectionGrid
import com.dailytown.app.ui.visual.A3CompanionStamp
import com.dailytown.app.ui.visual.A3FixedWidthQaFrame
import com.dailytown.app.visual.A3ClueState
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.SemanticAssetKey
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class A3VisualQaTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val catalog by lazy {
        CandidateAssetCatalog(InstrumentationRegistry.getInstrumentation().context.assets)
    }

    @Test
    fun fiveScreensRenderAt360_412_600AndReducedMotionFallback() {
        val screenWidthDp = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.screenWidthDp
        assumeTrue("A3 exact-width capture requires the tablet visual-QA managed device", screenWidthDp >= 600)

        val keys = listOf(
            "surface.journal.paper", "surface.collection.paper", "surface.memory.paper",
            "sticker.discovery.default", "card.clue.unresolved", "card.clue.resolved",
            "stamp.companion.default", "stamp.memory.resolved", "collection.locked.pattern",
        )
        val bitmaps = keys.associateWith { key ->
            CandidateSvgRenderer.renderAsset(catalog, key, widthPx = 440, heightPx = 560).asImageBitmap()
        }
        var screen by mutableStateOf(A3Screen.JOURNAL_HOME)
        var widthDp by mutableStateOf(360)
        var reducedMotion by mutableStateOf(false)

        composeRule.setContent {
            MaterialTheme {
                val renderer: @androidx.compose.runtime.Composable (SemanticAssetKey, Modifier) -> Unit = { key, modifier ->
                    Image(
                        bitmap = bitmaps.getValue(key.value),
                        contentDescription = key.value,
                        modifier = modifier,
                        contentScale = ContentScale.FillBounds,
                    )
                }
                A3FixedWidthQaFrame(
                    screen = screen,
                    widthDp = widthDp,
                    assetRenderer = renderer,
                    modifier = Modifier.testTag("a3-root"),
                ) {
                    Text("QA · ${screen.name} · ${widthDp}dp · ${if (reducedMotion) "reduced" else "normal"}")
                    when (screen) {
                        A3Screen.JOURNAL_HOME -> {
                            repeat(3) { index ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Record ${index + 1} · semantic state label")
                                    A3CompanionStamp(renderer, 48)
                                }
                            }
                            Image(bitmaps.getValue("sticker.discovery.default"), null, Modifier.size(64.dp))
                        }
                        A3Screen.DISCOVERY_DETAIL -> {
                            Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                                Image(bitmaps.getValue("sticker.discovery.default"), null, Modifier.fillMaxWidth())
                            }
                            Text("Discovery title may wrap to three lines without colliding with the companion stamp")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("state: discovered")
                                A3CompanionStamp(renderer, 48)
                            }
                        }
                        A3Screen.CLUE_NOTE -> {
                            A3ClueCard(A3ClueState.UNRESOLVED, renderer, Modifier.fillMaxWidth().height(180.dp)) {
                                Text("UNRESOLVED · icon + label", Modifier.padding(20.dp))
                            }
                            A3ClueCard(A3ClueState.RESOLVED, renderer, Modifier.fillMaxWidth().height(180.dp)) {
                                Text("RESOLVED · icon + label", Modifier.padding(20.dp))
                            }
                            A3CompanionStamp(renderer, 32)
                        }
                        A3Screen.COLLECTION_GRID -> {
                            A3CollectionGrid(widthDp, Modifier.fillMaxWidth()) { columns ->
                                repeat(2) { rowIndex ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        repeat(columns) { columnIndex ->
                                            val locked = (rowIndex + columnIndex) % 2 == 0
                                            Column(Modifier.weight(1f)) {
                                                Image(
                                                    bitmaps.getValue(if (locked) "collection.locked.pattern" else "sticker.discovery.default"),
                                                    null,
                                                    Modifier.fillMaxWidth().aspectRatio(1f / 1.15f),
                                                )
                                                Text(if (locked) "LOCKED · pattern" else "COMPLETED · mark")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        A3Screen.MEMORY_DETAIL -> {
                            Image(bitmaps.getValue("sticker.discovery.default"), null, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Image(bitmaps.getValue("stamp.memory.resolved"), null, Modifier.size(56.dp))
                                A3CompanionStamp(renderer, 64)
                            }
                            Text("Resolved memory · place/date metadata · reflective copy remains readable on stable paper.")
                        }
                    }
                    if (reducedMotion) Text("reduced-motion: static placement / semantic state unchanged")
                }
            }
        }

        listOf(360, 412, 600).forEach { width ->
            A3Screen.entries.forEach { targetScreen ->
                composeRule.runOnIdle { widthDp = width; screen = targetScreen; reducedMotion = false }
                composeRule.waitForIdle()
                composeRule.onNodeWithTag("a3-root").captureToImage().asAndroidBitmap()
                    .writeToTestStorage("visual/a3/${targetScreen.name.lowercase()}.$width.dp.normal")
            }
        }
        A3Screen.entries.forEach { targetScreen ->
            composeRule.runOnIdle { widthDp = 412; screen = targetScreen; reducedMotion = true }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("a3-root").captureToImage().asAndroidBitmap()
                .writeToTestStorage("visual/a3/${targetScreen.name.lowercase()}.412.dp.reduced")
        }
    }
}
