package com.dailytown.app.mystery

enum class EncounterPhase { HIDDEN, HINTED, DISCOVERED, RESOLVED }
enum class EncounterRarity { COMMON, UNCOMMON, RARE }

enum class MysteryMechanic {
    TRACE_CHAIN,
    SOUND_PATTERN,
    TIME_LAYER,
    SYMBOL_MATCH,
    LOST_OBJECT,
    PHOTO_ANGLE,
    LOCAL_MEMORY,
    COMPANION_SENSE,
}

data class MysteryTemplate(
    val id: String,
    val mechanic: MysteryMechanic,
    val requiredClues: Int,
    val tags: Set<String> = emptySet(),
    val rarity: EncounterRarity = EncounterRarity.COMMON,
)

data class MysteryEncounter(
    val id: String,
    val templateId: String,
    val poiId: String,
    val phase: EncounterPhase = EncounterPhase.HIDDEN,
    val clueIds: Set<String> = emptySet(),
)

sealed interface EncounterEvent {
    data object EnterHintRadius : EncounterEvent
    data object ReachSpot : EncounterEvent
    data class CollectClue(val clueId: String) : EncounterEvent
    data object Resolve : EncounterEvent
}

class MysteryReducer(private val templates: Map<String, MysteryTemplate>) {
    fun reduce(state: MysteryEncounter, event: EncounterEvent): MysteryEncounter {
        val template = templates[state.templateId] ?: return state
        return when (event) {
            EncounterEvent.EnterHintRadius -> if (state.phase == EncounterPhase.HIDDEN) state.copy(phase = EncounterPhase.HINTED) else state
            EncounterEvent.ReachSpot -> if (state.phase == EncounterPhase.HINTED) state.copy(phase = EncounterPhase.DISCOVERED) else state
            is EncounterEvent.CollectClue -> if (state.phase == EncounterPhase.DISCOVERED) {
                state.copy(clueIds = state.clueIds + event.clueId)
            } else state
            EncounterEvent.Resolve -> if (
                state.phase == EncounterPhase.DISCOVERED && state.clueIds.size >= template.requiredClues
            ) state.copy(phase = EncounterPhase.RESOLVED) else state
        }
    }
}

data class ClueInventory(val clueIds: Set<String> = emptySet()) {
    fun add(clueId: String): ClueInventory = copy(clueIds = clueIds + clueId)
    fun contains(clueId: String): Boolean = clueId in clueIds
}

object MysteryTemplateCatalog {
    fun defaults(): List<MysteryTemplate> = listOf(
        MysteryTemplate("trace-chain", MysteryMechanic.TRACE_CHAIN, 2, setOf("walk", "observe")),
        MysteryTemplate("sound-pattern", MysteryMechanic.SOUND_PATTERN, 2, setOf("listen", "pattern")),
        MysteryTemplate("time-layer", MysteryMechanic.TIME_LAYER, 2, setOf("history", "compare")),
        MysteryTemplate("symbol-match", MysteryMechanic.SYMBOL_MATCH, 3, setOf("observe", "match")),
        MysteryTemplate("lost-object", MysteryMechanic.LOST_OBJECT, 2, setOf("search", "route")),
        MysteryTemplate("photo-angle", MysteryMechanic.PHOTO_ANGLE, 2, setOf("viewpoint", "compare")),
        MysteryTemplate(
            "local-memory",
            MysteryMechanic.LOCAL_MEMORY,
            2,
            setOf("place", "story", "revisit"),
            rarity = EncounterRarity.UNCOMMON,
        ),
        MysteryTemplate(
            "companion-sense",
            MysteryMechanic.COMPANION_SENSE,
            2,
            setOf("companion", "choice", "rare"),
            rarity = EncounterRarity.RARE,
        ),
    )
}
