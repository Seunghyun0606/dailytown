package com.dailytown.app.ui.visual

import com.dailytown.app.companion.CompanionMoment
import com.dailytown.app.visual.CompanionExpression

/**
 * Maps gameplay moments to already-approved companion expression semantics for the compact
 * exploration HUD. This layer deliberately does not choose asset files or invent new expressions.
 */
object CompanionHudVisualResolver {
    fun expression(moment: CompanionMoment?): CompanionExpression = when (moment) {
        null -> CompanionExpression.NEUTRAL
        CompanionMoment.HINT_APPEARED -> CompanionExpression.CURIOUS
        CompanionMoment.SPOT_DISCOVERED -> CompanionExpression.SURPRISED
        CompanionMoment.CLUE_FOUND -> CompanionExpression.CLUE_FOUND
        CompanionMoment.MYSTERY_RESOLVED -> CompanionExpression.RESOLVED
    }
}
