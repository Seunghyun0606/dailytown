package com.dailytown.app.ui.visual

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dailytown.app.BuildConfig
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionRuntimeProfile
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import com.dailytown.app.visual.MoruV2Resolution
import com.dailytown.app.visual.MoruV2SemanticResolver

/** Default remains the current v1 profile until v2 Android + physical-device gates are complete. */
val LocalCompanionRuntimeProfile = staticCompositionLocalOf { CompanionRuntimeProfile.LEGACY_V1 }

/**
 * Versioned Compose entry point for production companion visuals.
 *
 * v1 is preserved byte/behavior-compatible as the terminal rollback path. v2 consumes only raster
 * authorities and never routes through the SVG/canvas character renderer. The M-B motion prototype
 * remains v1/debug-only because motion timing/intensity is still a Human Gate.
 */
@Composable
fun ProductionCompanionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    rasterTargetPx: Int = 256,
) {
    val profile = LocalCompanionRuntimeProfile.current
    if (profile == CompanionRuntimeProfile.MORU_CANONICAL_V2 && request.companionId == "moru") {
        MoruV2ProductionCompanionVisual(
            request = request,
            modifier = modifier,
            contentDescription = contentDescription,
            rasterTargetPx = rasterTargetPx,
        )
        return
    }

    LegacyProductionCompanionVisual(
        request = request,
        modifier = modifier,
        contentDescription = contentDescription,
        rasterTargetPx = rasterTargetPx,
    )
}

@Composable
private fun LegacyProductionCompanionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier,
    contentDescription: String?,
    rasterTargetPx: Int,
) {
    val spriteEligible = BuildConfig.DEBUG &&
        request.companionId == "moru" &&
        request.usageContext != CompanionUsageContext.JOURNAL_CROP &&
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
private fun MoruV2ProductionCompanionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier,
    contentDescription: String?,
    rasterTargetPx: Int,
) {
    val applicationContext = LocalContext.current.applicationContext
    val runtime = remember(applicationContext) {
        runCatching {
            val catalog = AndroidMoruV2RuntimeAssetCatalog(applicationContext.assets)
            Triple(catalog, MoruV2SemanticResolver(catalog), MoruV2RasterRenderer(catalog))
        }.getOrNull()
    }
    if (runtime == null) {
        StaticProductionCompanionVisual(request, modifier, contentDescription, rasterTargetPx)
        return
    }

    val resolved = remember(request, runtime) { runtime.second.resolve(request) }
    if (resolved !is MoruV2Resolution.Raster) {
        StaticProductionCompanionVisual(request, modifier, contentDescription, rasterTargetPx)
        return
    }

    val bitmap = remember(resolved.key, rasterTargetPx, runtime) {
        runCatching { runtime.third.render(resolved.key, rasterTargetPx) }.getOrNull()
    }
    if (bitmap == null) {
        StaticProductionCompanionVisual(request, modifier, contentDescription, rasterTargetPx)
        return
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
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
