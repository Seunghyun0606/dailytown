package com.dailytown.app.ui.visual

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dailytown.app.BuildConfig
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest

/**
 * Compose entry point for promoted companion visuals.
 * Callers supply semantic state only; file paths and SVG details remain in the Android adapter.
 *
 * Internal/debug builds expose the visible M-B prototype through a real bitmap sprite atlas for Moru.
 * Release keeps the static production visual until the final human motion-tuning gate is approved.
 */
@Composable
fun ProductionCompanionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    rasterTargetPx: Int = 256,
) {
    val spriteEligible = BuildConfig.DEBUG &&
        request.companionId == "moru" &&
        request.usageContext != CompanionUsageContext.JOURNAL_STAMP &&
        !request.reducedMotion

    if (spriteEligible) {
        ProductionMoruSpriteMotionVisual(
            request = request,
            modifier = modifier,
            contentDescription = contentDescription,
            rasterTargetPx = rasterTargetPx,
        )
    } else {
        StaticProductionCompanionVisual(
            request = request,
            modifier = modifier,
            contentDescription = contentDescription,
            rasterTargetPx = rasterTargetPx,
        )
    }
}

@Composable
internal fun StaticProductionCompanionVisual(
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
