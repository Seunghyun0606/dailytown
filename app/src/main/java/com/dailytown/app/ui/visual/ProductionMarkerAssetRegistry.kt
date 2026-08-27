package com.dailytown.app.ui.visual

import android.content.res.AssetManager
import com.dailytown.app.visual.MarkerAssetAvailability
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.SemanticAssetKey
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * One production marker asset. Marker identity is the pair of family + semantic key;
 * DAY and DARK intentionally reuse the same semantic keys and must never collide.
 */
data class ProductionMarkerAssetRecord(
    val family: MarkerFamily,
    val semanticKey: SemanticAssetKey,
    val assetPath: String,
)

interface ProductionMarkerAssetLookup : MarkerAssetAvailability {
    fun resolve(family: MarkerFamily, key: SemanticAssetKey): ProductionMarkerAssetRecord?
    fun records(): List<ProductionMarkerAssetRecord>
}

/**
 * Validated family-aware index used by the Android marker adapter and tests.
 * Raw file paths remain below this boundary and are never exposed to gameplay/domain code.
 */
class MarkerProductionAssetIndex(
    records: Iterable<ProductionMarkerAssetRecord>,
) : ProductionMarkerAssetLookup {
    private val sourceRecords = records.toList()

    init {
        sourceRecords.forEach(::validateRecord)
        require(sourceRecords.map { it.family to it.semanticKey.value }.toSet().size == sourceRecords.size) {
            "Production marker family/semantic pairs must be unique"
        }
    }

    private val recordsByPair = sourceRecords.associateBy { it.family to it.semanticKey.value }

    override fun contains(family: MarkerFamily, key: SemanticAssetKey): Boolean =
        family to key.value in recordsByPair

    override fun resolve(family: MarkerFamily, key: SemanticAssetKey): ProductionMarkerAssetRecord? =
        recordsByPair[family to key.value]

    fun require(family: MarkerFamily, key: SemanticAssetKey): ProductionMarkerAssetRecord =
        resolve(family, key)
            ?: error("No production marker asset for family=$family semantic=${key.value}")

    override fun records(): List<ProductionMarkerAssetRecord> =
        recordsByPair.values.sortedWith(compareBy({ it.family.name }, { it.semanticKey.value }))

    private fun validateRecord(record: ProductionMarkerAssetRecord) {
        require(record.semanticKey.value.startsWith("marker.")) {
            "Production marker semantic key must start with marker.: ${record.semanticKey.value}"
        }
        val path = record.assetPath
        require(path.isNotBlank() && !path.startsWith('/') && '\\' !in path) {
            "Production marker asset path must be a safe relative path: $path"
        }
        val segments = path.split('/')
        require(segments.size >= 2 && segments.none { it.isBlank() || it == "." || it == ".." }) {
            "Production marker asset path contains unsafe segments: $path"
        }
        require(segments.first() == record.family.name.lowercase()) {
            "Production marker asset path must stay inside ${record.family.name.lowercase()}/: $path"
        }
        require(path.endsWith(".svg")) {
            "Production marker asset must be SVG: $path"
        }
    }
}

/**
 * Deliberately empty until the fingerprint-bound physical + human readiness gate passes.
 * Wiring this singleton into the app is safe: an absent record returns null and NAVER keeps
 * its provider default marker instead of consuming a production_export_candidate asset.
 */
object ProductionMarkerAssetRegistry : ProductionMarkerAssetLookup {
    private val index = MarkerProductionAssetIndex(emptyList())

    override fun contains(family: MarkerFamily, key: SemanticAssetKey): Boolean = index.contains(family, key)
    override fun resolve(family: MarkerFamily, key: SemanticAssetKey): ProductionMarkerAssetRecord? =
        index.resolve(family, key)
    override fun records(): List<ProductionMarkerAssetRecord> = index.records()

    const val PROMOTED_MARKER_COUNT = 0

    init {
        check(records().size == PROMOTED_MARKER_COUNT) {
            "Production marker registry count must match explicit promotion state"
        }
    }
}

fun interface ProductionMarkerSvgCatalog {
    fun readSvg(record: ProductionMarkerAssetRecord): String
}

/** Android-specific file loading; the family-aware registry remains provider-neutral. */
class AndroidProductionMarkerAssetCatalog(
    private val assets: AssetManager,
) : ProductionMarkerSvgCatalog {
    fun open(record: ProductionMarkerAssetRecord): InputStream = assets.open(record.assetPath)

    override fun readSvg(record: ProductionMarkerAssetRecord): String =
        open(record).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}
