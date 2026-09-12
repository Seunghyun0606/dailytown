package com.dailytown.app.ui.visual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailytown.app.visual.A3ClueState
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.A3AssetResolver
import com.dailytown.app.visual.SemanticAssetKey

typealias SemanticAssetRenderer = @Composable (SemanticAssetKey, Modifier) -> Unit

/** Compose layout knows semantic keys only; Android resources/file names stay behind the renderer adapter. */
@Composable
fun A3PaperSurface(
    screen: A3Screen,
    assetRenderer: SemanticAssetRenderer,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val key = when (screen) {
        A3Screen.COLLECTION_GRID -> SemanticAssetKey("surface.collection.paper")
        A3Screen.MEMORY_DETAIL -> SemanticAssetKey("surface.memory.paper")
        else -> SemanticAssetKey("surface.journal.paper")
    }
    Box(modifier) {
        assetRenderer(key, Modifier.fillMaxSize())
        content()
    }
}

@Composable
fun A3CompanionStamp(
    assetRenderer: SemanticAssetRenderer,
    sizeDp: Int = 48,
    modifier: Modifier = Modifier,
) {
    require(A3AssetResolver.supportedStampSizeDp(sizeDp))
    assetRenderer(SemanticAssetKey("stamp.companion.default"), modifier.size(sizeDp.dp))
}

@Composable
fun A3ClueCard(
    state: A3ClueState,
    assetRenderer: SemanticAssetRenderer,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val key = if (state == A3ClueState.RESOLVED) "card.clue.resolved" else "card.clue.unresolved"
    Box(modifier) {
        assetRenderer(SemanticAssetKey(key), Modifier.fillMaxSize())
        content()
    }
}

@Composable
fun A3CollectionGrid(
    widthDp: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(columns: Int) -> Unit,
) {
    val columns = A3AssetResolver.resolve(A3Screen.COLLECTION_GRID, widthDp).columns
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) { content(columns) }
}

@Composable
fun A3FixedWidthQaFrame(
    screen: A3Screen,
    widthDp: Int,
    assetRenderer: SemanticAssetRenderer,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    A3PaperSurface(screen, assetRenderer, modifier.width(widthDp.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}
