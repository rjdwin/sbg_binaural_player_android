package org.cigaes.binaural_player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cigaes.binaural_player.MainUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionsScreen(
    uiState: MainUiState,
    onOptionSelected: (groupIndex: Int, choiceIndex: Int) -> Unit,
    onPlayClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Script Options: ${uiState.scriptFile?.name ?: ""}",
            style = MaterialTheme.typography.titleMedium
        )

        for ((groupIdx, group) in uiState.scriptOptions.withIndex()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = group.optionName,
                    style = MaterialTheme.typography.bodyLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((choiceIdx, choiceText) in group.choices.withIndex()) {
                        FilterChip(
                            selected = (group.selectedIndex == choiceIdx),
                            onClick = { onOptionSelected(groupIdx, choiceIdx) },
                            label = { Text(choiceText) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCloseClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Close ×")
            }
            Button(
                onClick = onPlayClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Play ▶")
            }
        }
    }
}
