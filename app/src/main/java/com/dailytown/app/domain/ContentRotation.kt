package com.dailytown.app.domain

data class ContentCandidate(
    val id: String,
    val districtKey: String,
    val novelty: Double,
    val distanceMeters: Double,
    val companionAffinity: Double,
)

class ContentRotation {
    fun rank(candidates: List<ContentCandidate>, recentlySeenIds: Set<String>): List<ContentCandidate> =
        candidates
            .filterNot { it.id in recentlySeenIds }
            .sortedByDescending {
                (it.novelty * 0.5) +
                    (it.companionAffinity * 0.3) +
                    (proximityScore(it.distanceMeters) * 0.2)
            }

    private fun proximityScore(distanceMeters: Double): Double =
        (1.0 - (distanceMeters / 2_000.0)).coerceIn(0.0, 1.0)
}
