package com.dailytown.app.mystery

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.poi.Poi

data class EncounterSelection(
    val poi: Poi,
    val template: MysteryTemplate,
    val encounter: MysteryEncounter,
    val context: EncounterContext,
    val isRevisit: Boolean,
) {
    val rarity: EncounterRarity get() = template.rarity
}

class EncounterGenerator(
    private val templates: List<MysteryTemplate> = MysteryTemplateCatalog.defaults(),
    private val planner: EncounterPlanner = EncounterPlanner(),
    private val rarePolicy: RareEncounterPolicy = RareEncounterPolicy(),
    private val distance: ExplorationEngine = ExplorationEngine(),
) {
    fun choose(
        encounterKey: String,
        center: GeoPoint,
        pois: List<Poi>,
        context: EncounterContext,
        visitedPoiIds: Set<String>,
        history: EncounterHistory,
    ): EncounterSelection? {
        if (pois.isEmpty() || templates.isEmpty()) return null

        val candidates = pois.flatMap { poi ->
            val isRevisit = poi.id in visitedPoiIds
            templates
                .asSequence()
                .filter { template -> rarePolicy.isEligible(template, poi.id, context) }
                .map { template ->
                    EncounterCandidate(
                        poiId = poi.id,
                        templateId = template.id,
                        districtKey = poi.districtKey,
                        novelty = novelty(template, isRevisit),
                        companionAffinity = affinity(template, poi.id, isRevisit, context),
                        distanceMeters = distance.distanceMeters(center, poi.position),
                    )
                }
                .toList()
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
            context = context,
            isRevisit = poi.id in visitedPoiIds,
        )
    }

    private fun novelty(template: MysteryTemplate, isRevisit: Boolean): Double {
        if (!isRevisit) return 1.0
        return when (template.mechanic) {
            MysteryMechanic.LOCAL_MEMORY -> 0.82
            MysteryMechanic.TIME_LAYER -> 0.68
            MysteryMechanic.COMPANION_SENSE -> 0.58
            else -> 0.35
        }
    }

    private fun affinity(
        template: MysteryTemplate,
        poiId: String,
        isRevisit: Boolean,
        context: EncounterContext,
    ): Double {
        val normalizedBond = (context.companionBond / 100.0).coerceIn(0.0, 1.0)
        var score = when (template.mechanic) {
            MysteryMechanic.COMPANION_SENSE -> 0.65 + normalizedBond * 0.35
            else -> 0.55 + normalizedBond * 0.15
        }

        if (isRevisit) {
            score += when (template.mechanic) {
                MysteryMechanic.LOCAL_MEMORY -> 0.25
                MysteryMechanic.TIME_LAYER -> 0.12
                MysteryMechanic.COMPANION_SENSE -> 0.08
                else -> 0.0
            }
        }

        score += when (context.timeBand) {
            TimeBand.DAWN -> if (template.mechanic == MysteryMechanic.TRACE_CHAIN) 0.10 else 0.0
            TimeBand.DAY -> if (template.mechanic == MysteryMechanic.PHOTO_ANGLE) 0.12 else 0.0
            TimeBand.EVENING -> if (template.mechanic in setOf(MysteryMechanic.SOUND_PATTERN, MysteryMechanic.TIME_LAYER)) 0.12 else 0.0
            TimeBand.NIGHT -> if (template.mechanic in setOf(MysteryMechanic.SOUND_PATTERN, MysteryMechanic.COMPANION_SENSE)) 0.18 else 0.0
        }

        if ("poi:$poiId" in context.memoryKeys) score += 0.08
        if ("mechanic:${template.mechanic.name}" in context.memoryKeys) score += 0.08
        return score.coerceIn(0.0, 1.0)
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
