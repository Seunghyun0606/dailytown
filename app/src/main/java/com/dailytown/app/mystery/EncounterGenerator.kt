package com.dailytown.app.mystery

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.poi.Poi

data class EncounterSelection(
    val poi: Poi,
    val template: MysteryTemplate,
    val encounter: MysteryEncounter,
)

class EncounterGenerator(
    private val templates: List<MysteryTemplate> = MysteryTemplateCatalog.defaults(),
    private val planner: EncounterPlanner = EncounterPlanner(),
    private val distance: ExplorationEngine = ExplorationEngine(),
) {
    fun choose(
        encounterKey: String,
        center: GeoPoint,
        pois: List<Poi>,
        companionBond: Int,
        visitedPoiIds: Set<String>,
        history: EncounterHistory,
    ): EncounterSelection? {
        if (pois.isEmpty() || templates.isEmpty()) return null

        val candidates = pois.flatMap { poi ->
            templates.map { template ->
                EncounterCandidate(
                    poiId = poi.id,
                    templateId = template.id,
                    districtKey = poi.districtKey,
                    novelty = if (poi.id in visitedPoiIds) 0.35 else 1.0,
                    companionAffinity = affinity(template, companionBond),
                    distanceMeters = distance.distanceMeters(center, poi.position),
                )
            }
        }
        val selected = planner.rank(candidates, history).firstOrNull() ?: return null
        val poi = pois.first { it.id == selected.poiId }
        val template = templates.first { it.id == selected.templateId }
        return EncounterSelection(
            poi = poi,
            template = template,
            encounter = MysteryEncounter(
                id = "$encounterKey:${poi.id}:${template.id}",
                templateId = template.id,
                poiId = poi.id,
            ),
        )
    }

    private fun affinity(template: MysteryTemplate, bond: Int): Double {
        val normalizedBond = (bond / 100.0).coerceIn(0.0, 1.0)
        return when (template.mechanic) {
            MysteryMechanic.COMPANION_SENSE -> 0.65 + normalizedBond * 0.35
            else -> 0.55 + normalizedBond * 0.15
        }
    }
}

class EncounterProximityController(
    private val reducer: MysteryReducer,
    private val distance: ExplorationEngine = ExplorationEngine(),
    private val hintRadiusMeters: Double = 180.0,
    private val discoveryRadiusMeters: Double = 60.0,
) {
    fun advance(encounter: MysteryEncounter, user: GeoPoint, poi: GeoPoint): MysteryEncounter {
        val meters = distance.distanceMeters(user, poi)
        var next = encounter
        if (meters <= hintRadiusMeters && next.phase == EncounterPhase.HIDDEN) {
            next = reducer.reduce(next, EncounterEvent.EnterHintRadius)
        }
        if (meters <= discoveryRadiusMeters && next.phase == EncounterPhase.HINTED) {
            next = reducer.reduce(next, EncounterEvent.ReachSpot)
        }
        return next
    }

    fun distanceTo(user: GeoPoint, poi: GeoPoint): Double = distance.distanceMeters(user, poi)
}
