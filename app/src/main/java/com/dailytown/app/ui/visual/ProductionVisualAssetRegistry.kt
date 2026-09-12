package com.dailytown.app.ui.visual

import android.content.res.AssetManager
import com.dailytown.app.visual.SemanticAssetAvailability
import com.dailytown.app.visual.SemanticAssetKey
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Android adapter boundary for visual files that have completed development integration QA.
 * Domain/Compose state resolves semantic keys; raw asset paths stay centralized here.
 *
 * Marker candidates are deliberately absent until credentialed NAVER base-map QA passes.
 */
object ProductionVisualAssetRegistry : SemanticAssetAvailability {
    data class Record(
        val semanticKey: SemanticAssetKey,
        val assetPath: String,
        val family: Family,
    )

    enum class Family { COMPANION, A3 }

    private val recordsByKey: Map<String, Record> = buildList {
        listOf("base", "familiar", "trusted", "best_friend").forEach { profile ->
            add(companion("appearance.moru.$profile", "moru/appearance-$profile.v1.svg"))
        }

        listOf("moru", "luca").forEach { companionId ->
            listOf("front", "three_quarter", "side", "back", "silhouette").forEach { view ->
                add(companion("companion.$companionId.canonical.$view", "$companionId/canonical-$view.v1.svg"))
            }
            listOf("light", "warm_dusk", "dark").forEach { lighting ->
                add(companion("lighting.$companionId.$lighting", "$companionId/lighting-$lighting.v1.svg"))
            }
            listOf("neutral", "happy", "curious", "surprised", "clue_found", "resolved").forEach { expression ->
                add(companion("companion.$companionId.expression.$expression", "$companionId/expression-$expression.v1.svg"))
            }
        }

        add(a3("surface.journal.paper", "surface-journal-paper.v1.svg"))
        add(a3("surface.collection.paper", "surface-collection-paper.v1.svg"))
        add(a3("surface.memory.paper", "surface-memory-paper.v1.svg"))
        add(a3("sticker.discovery.default", "sticker-discovery-default.v1.svg"))
        add(a3("card.clue.unresolved", "card-clue-unresolved.v1.svg"))
        add(a3("card.clue.resolved", "card-clue-resolved.v1.svg"))
        add(a3("stamp.companion.default", "stamp-companion-default.v1.svg"))
        add(a3("stamp.memory.resolved", "stamp-memory-resolved.v1.svg"))
        add(a3("collection.locked.pattern", "collection-locked-pattern.v1.svg"))
    }.associateBy { it.semanticKey.value }.also { values ->
        check(values.size == PROMOTED_ASSET_COUNT) { "Production visual semantic keys must be unique" }
    }

    override fun contains(key: SemanticAssetKey): Boolean = key.value in recordsByKey

    fun resolve(key: SemanticAssetKey): Record? = recordsByKey[key.value]

    fun require(key: SemanticAssetKey): Record =
        resolve(key) ?: error("No production visual asset for semantic key: ${key.value}")

    fun records(): List<Record> = recordsByKey.values.sortedBy { it.semanticKey.value }

    private fun companion(key: String, path: String) = Record(SemanticAssetKey(key), path, Family.COMPANION)
    private fun a3(key: String, path: String) = Record(SemanticAssetKey(key), path, Family.A3)

    const val PROMOTED_COMPANION_COUNT = 32
    const val PROMOTED_A3_COUNT = 9
    const val PROMOTED_ASSET_COUNT = PROMOTED_COMPANION_COUNT + PROMOTED_A3_COUNT
}

/** File loading remains Android-specific and is kept behind the semantic registry. */
class AndroidProductionVisualAssetCatalog(
    private val assets: AssetManager,
) {
    fun open(key: SemanticAssetKey): InputStream =
        assets.open(ProductionVisualAssetRegistry.require(key).assetPath)

    fun readSvg(key: SemanticAssetKey): String =
        open(key).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}
