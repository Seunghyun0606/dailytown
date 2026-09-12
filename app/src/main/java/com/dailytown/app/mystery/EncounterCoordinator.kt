package com.dailytown.app.mystery

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.poi.Poi
import java.time.LocalDate
import java.time.LocalTime

/**
 * Provider- and UI-neutral inputs needed to rank the next encounter.
 * Keeping this independent from persistence models makes encounter behavior easy to unit test
 * and lets the UI decide how/when progress is stored.
 */
data class EncounterRuntimeContext(
    val visitedPoiIds: Set<String> = emptySet(),
    val recentPoiIds: Set<String> = emptySet(),
    val recentTemplateIds: Set<String> = emptySet(),
    val recentPairKeys: Set<String> = emptySet(),
    val companionBond: Int = 0,
    val memoryKeys: Set<String> = emptySet(),
)

enum class EncounterTransition {
    NONE,
    HINTED,
    DISCOVERED,
}

data class EncounterStep(
    val selection: EncounterSelection?,
    val transition: EncounterTransition = EncounterTransition.NONE,
)

/**
 * Owns short-lived encounter sequencing and proximity transitions.
 * Compose/UI code only supplies location, nearby POIs, and derived history, then reacts to the
 * returned transition. No Android, map-provider, or persistence APIs are used here.
 */
class EncounterCoordinator(
    templates: List<MysteryTemplate> = MysteryTemplateCatalog.defaults(),
    private val generator: EncounterGenerator = EncounterGenerator(templates = templates),
    private val proximityController: EncounterProximityController = EncounterProximityController(
        reducer = MysteryReducer(templates.associateBy { it.id }),
    ),
) {
    private var sequence: Long = 0L

    fun advance(
        current: EncounterSelection?,
        user: GeoPoint,
        nearbyPois: List<Poi>,
        runtime: EncounterRuntimeContext,
        date: LocalDate,
        time: LocalTime,
    ): EncounterStep {
        var selection = current
        if (selection == null) {
            selection = generator.choose(
                encounterKey = "enc-${sequence++}",
                center = user,
                pois = nearbyPois,
                context = EncounterContextFactory.create(
                    date = date,
                    time = time,
                    companionBond = runtime.companionBond,
                    memoryKeys = runtime.memoryKeys,
                ),
                visitedPoiIds = runtime.visitedPoiIds,
                history = EncounterHistory(
                    recentPoiIds = runtime.recentPoiIds,
                    recentTemplateIds = runtime.recentTemplateIds,
                    recentPairKeys = runtime.recentPairKeys,
                ),
            )
        }

        val selected = selection ?: return EncounterStep(selection = null)
        if (selected.encounter.phase == EncounterPhase.RESOLVED) {
            return EncounterStep(selection = selected)
        }

        val previousPhase = selected.encounter.phase
        val advanced = proximityController.advance(
            encounter = selected.encounter,
            user = user,
            poi = selected.poi.position,
        )
        val transition = when {
            previousPhase != EncounterPhase.DISCOVERED && advanced.phase == EncounterPhase.DISCOVERED -> {
                EncounterTransition.DISCOVERED
            }
            previousPhase == EncounterPhase.HIDDEN && advanced.phase == EncounterPhase.HINTED -> {
                EncounterTransition.HINTED
            }
            else -> EncounterTransition.NONE
        }
        return EncounterStep(
            selection = if (advanced == selected.encounter) selected else selected.copy(encounter = advanced),
            transition = transition,
        )
    }

    fun distanceTo(user: GeoPoint, selection: EncounterSelection): Double =
        proximityController.distanceTo(user, selection.poi.position)

    /** A new tracking/replay session starts a fresh deterministic encounter-id sequence. */
    fun reset() {
        sequence = 0L
    }
}
