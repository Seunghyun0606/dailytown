package com.dailytown.app.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapReadabilityQaTest {
    @Test
    fun baselineMatrixHasApprovedEighteenUniqueCases() {
        val cases = MapOverlayQaMatrix.baselineCases

        assertEquals(MapOverlayQaMatrix.EXPECTED_BASELINE_CAPTURE_COUNT, cases.size)
        assertEquals(cases.size, cases.map { it.id }.toSet().size)
        assertEquals(MapQaTimeAnchor.values().toSet(), cases.map { it.timeAnchor }.toSet())
        assertEquals(MapQaComplexity.values().toSet(), cases.map { it.mapComplexity }.toSet())
        assertEquals(MapQaMotionMode.values().toSet(), cases.map { it.motionMode }.toSet())
    }

    @Test
    fun rbClassifierPassesOnlyWhenCriticalLayersRemainReadable() {
        assertEquals(
            RbReadabilityResult.PASS,
            RbReadabilityClassifier.classify(RbReadabilityEvidence()),
        )

        assertEquals(
            RbReadabilityResult.PASS_WITH_DECORATIVE_DEGRADATION,
            RbReadabilityClassifier.classify(
                RbReadabilityEvidence(
                    degradedDecorativeLayers = setOf(RbDecorativeLayer.ATMOSPHERE),
                ),
            ),
        )

        RbCriticalLayer.values().forEach { critical ->
            assertEquals(
                "critical layer $critical must fail closed",
                RbReadabilityResult.FAIL,
                RbReadabilityClassifier.classify(
                    RbReadabilityEvidence(unreadableCriticalLayers = setOf(critical)),
                ),
            )
        }
    }

    @Test
    fun rbDegradationOrderSacrificesDecorationOnly() {
        assertEquals(
            listOf(
                RbDecorativeLayer.ATMOSPHERE,
                RbDecorativeLayer.ACTIVE_GLOW,
                RbDecorativeLayer.DISCOVERY_DECORATION,
            ),
            RbReadabilityClassifier.decorativeDegradationOrder,
        )
        assertTrue(RbReadabilityClassifier.decorativeDegradationOrder.isNotEmpty())
    }
}
