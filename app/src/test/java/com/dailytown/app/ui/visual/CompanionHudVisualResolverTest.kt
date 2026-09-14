package com.dailytown.app.ui.visual

import com.dailytown.app.companion.CompanionMoment
import com.dailytown.app.visual.CompanionExpression
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionHudVisualResolverTest {
    @Test
    fun mapsGameplayMomentsToExistingExpressionSemantics() {
        assertEquals(CompanionExpression.NEUTRAL, CompanionHudVisualResolver.expression(null))
        assertEquals(CompanionExpression.CURIOUS, CompanionHudVisualResolver.expression(CompanionMoment.HINT_APPEARED))
        assertEquals(CompanionExpression.SURPRISED, CompanionHudVisualResolver.expression(CompanionMoment.SPOT_DISCOVERED))
        assertEquals(CompanionExpression.CLUE_FOUND, CompanionHudVisualResolver.expression(CompanionMoment.CLUE_FOUND))
        assertEquals(CompanionExpression.RESOLVED, CompanionHudVisualResolver.expression(CompanionMoment.MYSTERY_RESOLVED))
    }
}
