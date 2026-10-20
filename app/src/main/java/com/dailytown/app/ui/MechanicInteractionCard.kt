package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dailytown.app.mystery.InteractionKind
import com.dailytown.app.mystery.InteractionProgress
import com.dailytown.app.mystery.MechanicInteraction
import com.dailytown.app.mystery.MechanicInteractions

/**
 * One mechanic-specific investigation step. CHOICE = pick one; SEQUENCE = tap options in order
 * (options may repeat, e.g. a rhythm). Wrong input only resets the attempt with Moru's hint.
 */
@Composable
internal fun MechanicInteractionCard(
    interaction: MechanicInteraction,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var picks by remember(interaction) { mutableStateOf(emptyList<Int>()) }
    var feedback by remember(interaction) { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mechanic-interaction")
            .semantics { contentDescription = "조사: ${interaction.prompt}" },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (interaction.kind == InteractionKind.SEQUENCE) "순서대로 눌러 보세요" else "하나를 골라 보세요",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(
                    if (interaction.kind == InteractionKind.SEQUENCE) "mechanic-type-sequence" else "mechanic-type-choice",
                ),
            )
            Text(interaction.prompt, style = MaterialTheme.typography.bodyLarge)
            if (interaction.kind == InteractionKind.SEQUENCE) {
                Text(
                    "진행 ${picks.size}/${interaction.answer.size}" +
                        if (picks.isEmpty()) "" else " · " + picks.joinToString(" → ") { interaction.options[it] },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("mechanic-progress"),
                )
            }
            val optionRows = if (interaction.options.size <= 2) listOf(interaction.options.indices.toList())
            else interaction.options.indices.map { listOf(it) }
            optionRows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { index ->
                        OutlinedButton(
                            onClick = {
                                when (val result = MechanicInteractions.pick(interaction, picks, index)) {
                                    InteractionProgress.Solved -> {
                                        picks = emptyList()
                                        feedback = interaction.successLine
                                        onSolved()
                                    }
                                    InteractionProgress.Wrong -> {
                                        picks = emptyList()
                                        feedback = interaction.retryLine
                                    }
                                    is InteractionProgress.Partial -> {
                                        picks = result.picks
                                        feedback = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("mechanic-option-$index"),
                        ) { Text(interaction.options[index]) }
                    }
                }
            }
            feedback?.let {
                Text("모루 · $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("mechanic-feedback"))
            }
        }
    }
}
