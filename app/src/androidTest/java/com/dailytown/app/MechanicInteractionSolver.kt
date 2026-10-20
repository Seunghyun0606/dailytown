package com.dailytown.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

/**
 * Plays mechanic-specific investigation steps like a player who does not know the answer: wrong
 * picks are free, so it tries options (or option sequences) until the clue count goes up.
 */
class MechanicInteractionSolver(private val rule: ComposeTestRule) {
    private fun exists(tag: String) = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun textOf(tag: String): String =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
            ?.joinToString { it.text }.orEmpty()

    /** Solves interactions until the encounter has all required clues (resolve becomes possible). */
    fun collectAllClues(maxSteps: Int = 3) {
        repeat(maxSteps) {
            rule.waitForIdle()
            if (!exists("mechanic-interaction")) return
            solveOne()
        }
    }

    fun solveOne() {
        rule.waitUntil(8_000L) { exists("mechanic-interaction") }
        val before = textOf("encounter-clue-progress")
        val optionCount = (0..4).count { exists("mechanic-option-$it") }
        val sequence = exists("mechanic-type-sequence")
        val length = if (sequence) {
            Regex("/([0-9]+)").find(textOf("mechanic-progress"))?.groupValues?.get(1)?.toInt() ?: 3
        } else 1
        fun candidates(prefix: List<Int>): Sequence<List<Int>> =
            if (prefix.size == length) sequenceOf(prefix)
            else (0 until optionCount).asSequence().flatMap { candidates(prefix + it) }
        for (attempt in candidates(emptyList())) {
            for ((step, index) in attempt.withIndex()) {
                rule.onNodeWithTag("mechanic-option-$index").performScrollTo().performClick()
                rule.waitForIdle()
                if (textOf("encounter-clue-progress") != before) return
                if (sequence && textOf("mechanic-progress").startsWith("진행 0/") && step < attempt.lastIndex) break
            }
        }
        throw AssertionError("mechanic interaction could not be solved")
    }
}
