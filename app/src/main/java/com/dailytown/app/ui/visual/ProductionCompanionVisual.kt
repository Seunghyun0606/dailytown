package com.dailytown.app.ui.visual

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionVisualRequest

/**
 * Compose entry point for promoted companion visuals.
 * Callers supply semantic state only; file paths and SVG details remain in the Android adapter.
 */
@Composable
fun ProductionCompanionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    rasterTargetPx: Int = 256,
) {
    val applicationContext = LocalContext.current.applicationContext
    val resolver = remember { CompanionAssetResolver(ProductionVisualAssetRegistry) }
    val renderer = remember(applicationContext) {
        ProductionCompanionCanvasRenderer(
            AndroidProductionVisualAssetCatalog(applicationContext.assets),
        )
    }
    val resolved = remember(request) { resolver.resolve(request) }
    val bitmap = remember(resolved, rasterTargetPx) { renderer.render(resolved, rasterTargetPx) }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
