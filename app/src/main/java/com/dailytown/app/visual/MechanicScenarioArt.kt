package com.dailytown.app.visual

import com.dailytown.app.mystery.MysteryMechanic

/**
 * Semantic art keys per mystery mechanic. TRACE_CHAIN keeps the approved Old Ginkgo first scenario;
 * the other mechanics use their own place scene, clue item, keepsake item and shared-memory crop.
 * Raw paths stay in ProductionScenarioAssetRegistry; a missing binding falls back to text layout.
 */
data class MechanicScenarioArt(
    val place: SemanticAssetKey,
    val clueMain: SemanticAssetKey,
    val clueKeepsake: SemanticAssetKey,
    val memory: SemanticAssetKey,
    val placeDescription: String,
    val clueDescription: String,
    val keepsakeDescription: String,
)

object MechanicScenarioArtCatalog {
    private fun generated(mechanic: MysteryMechanic, place: String, clue: String, keepsake: String): MechanicScenarioArt {
        val id = mechanic.name.lowercase()
        return MechanicScenarioArt(
            place = SemanticAssetKey("place.$id.main"),
            clueMain = SemanticAssetKey("clue.$id.main"),
            clueKeepsake = SemanticAssetKey("clue.$id.keepsake"),
            memory = SemanticAssetKey("memory.$id.keepsake"),
            placeDescription = place,
            clueDescription = clue,
            keepsakeDescription = keepsake,
        )
    }

    private val byMechanic: Map<MysteryMechanic, MechanicScenarioArt> = mapOf(
        MysteryMechanic.TRACE_CHAIN to MechanicScenarioArt(
            place = OldGinkgoVisualAssets.PlaceMain,
            clueMain = OldGinkgoVisualAssets.FoldedNote,
            clueKeepsake = OldGinkgoVisualAssets.GinkgoLeaf,
            memory = OldGinkgoVisualAssets.Keepsake,
            placeDescription = "오래된 가로수 아래 돌계단",
            clueDescription = "접힌 메모 단서",
            keepsakeDescription = "은행잎 단서",
        ),
        MysteryMechanic.SOUND_PATTERN to generated(MysteryMechanic.SOUND_PATTERN, "풍경이 걸린 처마 골목", "작은 풍경 종", "작은 깃털"),
        MysteryMechanic.TIME_LAYER to generated(MysteryMechanic.TIME_LAYER, "담장 모퉁이의 오래된 경계석", "닳은 돌 표식", "낡은 열쇠"),
        MysteryMechanic.SYMBOL_MATCH to generated(MysteryMechanic.SYMBOL_MATCH, "잎 무늬가 이어진 계단 골목", "잎 무늬 타일", "은행잎 책갈피"),
        MysteryMechanic.LOST_OBJECT to generated(MysteryMechanic.LOST_OBJECT, "작은 주머니가 놓인 공원 벤치", "뜨개 주머니", "도토리"),
        MysteryMechanic.PHOTO_ANGLE to generated(MysteryMechanic.PHOTO_ANGLE, "지붕들이 내려다보이는 계단 위", "작은 돋보기", "작은 지도 조각"),
        MysteryMechanic.LOCAL_MEMORY to generated(MysteryMechanic.LOCAL_MEMORY, "쪽지가 가득한 동네 게시판", "말린 꽃 카드", "말린 꽃 다발"),
        MysteryMechanic.COMPANION_SENSE to generated(MysteryMechanic.COMPANION_SENSE, "새싹이 자라는 동네 텃밭 길", "작은 새싹 화분", "새싹 리본"),
    )

    init {
        check(byMechanic.keys == MysteryMechanic.entries.toSet()) { "every mechanic needs scenario art keys" }
    }

    fun forMechanic(mechanic: MysteryMechanic): MechanicScenarioArt = byMechanic.getValue(mechanic)

    fun all(): List<MechanicScenarioArt> = MysteryMechanic.entries.map(::forMechanic)
}

/** Art for the most recent encounter recorded in progress (recent template ids are newest first). */
fun recentScenarioArt(recentTemplateIds: List<String>): MechanicScenarioArt {
    val templates = com.dailytown.app.mystery.MysteryTemplateCatalog.defaults().associateBy { it.id }
    val mechanic = recentTemplateIds.firstNotNullOfOrNull { templates[it]?.mechanic } ?: MysteryMechanic.TRACE_CHAIN
    return MechanicScenarioArtCatalog.forMechanic(mechanic)
}

/** Art for the encounter last met at [poiId], from `poiId:templateId` recent pair keys. */
fun scenarioArtForPlace(poiId: String, recentPairKeys: List<String>): MechanicScenarioArt? {
    val templates = com.dailytown.app.mystery.MysteryTemplateCatalog.defaults().associateBy { it.id }
    val templateId = recentPairKeys.firstOrNull { it.startsWith("$poiId:") }?.substringAfterLast(':') ?: return null
    return templates[templateId]?.let { MechanicScenarioArtCatalog.forMechanic(it.mechanic) }
}
