package com.dailytown.app.companion

import com.dailytown.app.domain.Companion

enum class CompanionMoment { HINT_APPEARED, SPOT_DISCOVERED, CLUE_FOUND, MYSTERY_RESOLVED }

data class CompanionReaction(
    val reactionKey: String,
    val bondDelta: Int,
)

/**
 * Returns semantic reaction keys rather than final authored dialogue.
 * Final tone/copy remains a human content-review TODO.
 */
interface CompanionReactionPolicy {
    fun react(companion: Companion, moment: CompanionMoment): CompanionReaction
}

class DefaultCompanionReactionPolicy : CompanionReactionPolicy {
    override fun react(companion: Companion, moment: CompanionMoment): CompanionReaction {
        val tier = when {
            companion.bond >= 50 -> "close"
            companion.bond >= 20 -> "familiar"
            else -> "new"
        }
        val delta = when (moment) {
            CompanionMoment.HINT_APPEARED -> 0
            CompanionMoment.SPOT_DISCOVERED -> 1
            CompanionMoment.CLUE_FOUND -> 1
            CompanionMoment.MYSTERY_RESOLVED -> 2
        }
        return CompanionReaction(
            reactionKey = "companion.$tier.${moment.name.lowercase()}",
            bondDelta = delta,
        )
    }
}
