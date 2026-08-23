package com.dailytown.app.mystery

data class EncounterCandidate(
    val poiId: String,
    val templateId: String,
    val districtKey: String,
    val novelty: Double,
    val companionAffinity: Double,
    val distanceMeters: Double,
)

data class EncounterHistory(
    val recentPoiIds: Set<String> = emptySet(),
    val recentTemplateIds: Set<String> = emptySet(),
    val recentPairKeys: Set<String> = emptySet(),
)

/**
 * Soft anti-repeat ranking: recent content is penalized instead of removed, so a familiar
 * neighborhood never becomes empty just because the user has already visited every POI.
 */
class EncounterPlanner {
    fun rank(candidates: List<EncounterCandidate>, history: EncounterHistory): List<EncounterCandidate> =
        candidates.sortedByDescending { candidate -> score(candidate, history) }

    private fun score(candidate: EncounterCandidate, history: EncounterHistory): Double {
        val pairKey = "${candidate.poiId}:${candidate.templateId}"
        val poiPenalty = if (candidate.poiId in history.recentPoiIds) 0.22 else 0.0
        val templatePenalty = if (candidate.templateId in history.recentTemplateIds) 0.18 else 0.0
        val pairPenalty = if (pairKey in history.recentPairKeys) 0.35 else 0.0
        val proximity = (1.0 - candidate.distanceMeters / 2_000.0).coerceIn(0.0, 1.0)
        return (candidate.novelty * 0.45) +
            (candidate.companionAffinity * 0.25) +
            (proximity * 0.30) - poiPenalty - templatePenalty - pairPenalty
    }
}
