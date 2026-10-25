package org.cigaes.binaural_player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cigaes.binaural_player.Preset
import org.cigaes.binaural_player.Presets

@Composable
fun PresetsScreen(
    onPresetSelected: (Preset) -> Unit,
    onOpenFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedButton(
            onClick = onOpenFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📂 Import .sbg / .txt File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Built-in Presets",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(Presets.items) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPresetSelected(preset) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}