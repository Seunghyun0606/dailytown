package com.dailytown.app.ui.visual

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dailytown.app.visual.OldGinkgoVisualAssets
import com.dailytown.app.visual.ScenarioAssetUsage
import com.dailytown.app.visual.SemanticAssetKey
import java.io.InputStream

/**
 * Runtime-only binding for approved raster scenario art.
 *
 * Design/reference files never enter the APK through this registry. Only explicitly promoted exact
 * bytes under app/src/main/assets are visible here. Missing records or decode failures deliberately
 * fall back to the existing text/layout presentation instead of crashing the encounter flow.
 */
object ProductionScenarioAssetRegistry {
    data class Record(
        val semanticKey: SemanticAssetKey,
        val usage: ScenarioAssetUsage,
        val assetPath: String,
        val expectedSha256: String,
        val widthPx: Int,
        val heightPx: Int,
        val hasAlpha: Boolean,
    )

    private val records = listOf(
        Record(
            semanticKey = OldGinkgoVisualAssets.FoldedNote,
            usage = ScenarioAssetUsage.CLUE_ART,
            assetPath = "old-ginkgo/clue-folded-note.v4.png",
            expectedSha256 = "2adc914f6ca2e5648339943bc02afe62059948d52a672e382fad69b5e6c08869",
            widthPx = 768,
            heightPx = 768,
            hasAlpha = true,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.GinkgoLeaf,
            usage = ScenarioAssetUsage.CLUE_ART,
            assetPath = "old-ginkgo/clue-ginkgo-leaf.v5.png",
            expectedSha256 = "775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366",
            widthPx = 512,
            heightPx = 512,
            hasAlpha = true,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.PlaceMain,
            usage = ScenarioAssetUsage.DISCOVERY_CARD,
            assetPath = "old-ginkgo/place-main-discovery-card.v3.webp",
            expectedSha256 = "fde285964928825dc77018efe00ae43b7f243fcb5682ce359bf752a8277bfe41",
            widthPx = 1600,
            heightPx = 900,
            hasAlpha = false,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.PlaceMain,
            usage = ScenarioAssetUsage.RECORDS_HEADER,
            assetPath = "old-ginkgo/place-main-records-header.v3.webp",
            expectedSha256 = "97f8d772e62bc6da9d08023c5daec6db903c7eeab0d02b27427cde65938a0920",
            widthPx = 1600,
            heightPx = 600,
            hasAlpha = false,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.PlaceMain,
            usage = ScenarioAssetUsage.MEMORY_THUMBNAIL,
            assetPath = "old-ginkgo/place-main-memory-thumbnail.v3.webp",
            expectedSha256 = "7dda466c3e1182897a8e09eee63978f4a16ad82bb2942e4d2e71c6582561fdd2",
            widthPx = 640,
            heightPx = 480,
            hasAlpha = false,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.Keepsake,
            usage = ScenarioAssetUsage.RECORDS_CARD,
            assetPath = "old-ginkgo/memory-keepsake-records-card.v3.webp",
            expectedSha256 = "4ee1dab4fa9de7f3edfa34f424afcb575d399b0354644c52411e39418733e146",
            widthPx = 1024,
            heightPx = 768,
            hasAlpha = false,
        ),
        Record(
            semanticKey = OldGinkgoVisualAssets.Keepsake,
            usage = ScenarioAssetUsage.COMPANION_RECENT_MEMORY,
            assetPath = "old-ginkgo/memory-keepsake-companion-recent.v3.webp",
            expectedSha256 = "01d9dd9679b66ec66ae6f79991a54bd5863096afafbf893d07693e03d120b73a",
            widthPx = 1280,
            heightPx = 720,
            hasAlpha = false,
        ),
    )

    private val recordsByIdentity = records.associateBy { it.semanticKey.value to it.usage }.also { indexed ->
        check(indexed.size == records.size) { "Production scenario asset bindings must be unique" }
    }

    fun resolve(key: SemanticAssetKey, usage: ScenarioAssetUsage): Record? =
        recordsByIdentity[key.value to usage]

    fun records(): List<Record> = records.sortedWith(
        compareBy<Record> { it.semanticKey.value }.thenBy { it.usage.name },
    )

    const val PROMOTED_OLD_GINKGO_ASSET_COUNT = 7
}

class AndroidProductionScenarioAssetCatalog(
    private val assets: AssetManager,
) {
    fun open(record: ProductionScenarioAssetRegistry.Record): InputStream = assets.open(record.assetPath)

    fun open(key: SemanticAssetKey, usage: ScenarioAssetUsage): InputStream {
        val record = ProductionScenarioAssetRegistry.resolve(key, usage)
            ?: error("No production scenario asset for ${key.value}/${usage.name}")
        return open(record)
    }
}

@Composable
fun ProductionScenarioRasterAsset(
    semanticKey: SemanticAssetKey,
    usage: ScenarioAssetUsage,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    fallback: @Composable () -> Unit = {},
) {
    val assets = LocalContext.current.assets
    val record = remember(semanticKey, usage) {
        ProductionScenarioAssetRegistry.resolve(semanticKey, usage)
    }
    val bitmap = remember(assets, record) {
        record?.let { candidate ->
            runCatching {
                assets.open(candidate.assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    if (bitmap == null) {
        fallback()
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}
