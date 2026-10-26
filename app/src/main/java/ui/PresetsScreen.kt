package sbgplay.binaural.`in`.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sbgplay.binaural.`in`.MainUiState
import sbgplay.binaural.`in`.RecentFile

@Composable
fun PresetsScreen(
    uiState: MainUiState,
    onOpenFile: () -> Unit,
    onRecentFileClicked: (RecentFile) -> Unit,
    onToggleRecentExpanded: () -> Unit,
    onClearRecents: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP: Import Button
        item {
            OutlinedButton(
                onClick = onOpenFile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📂 Import .sbg / .txt File")
            }
        }

        // 2. TOP SUB-SECTION: Recent Files Toggle & Quick Access List
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleRecentExpanded() }
                    ) {
                        Text(
                            text = "🕒 Recent Files",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.recentFiles.isNotEmpty()) {
                                TextButton(onClick = onClearRecents) {
                                    Text("Clear")
                                }
                            }
                            IconButton(onClick = onToggleRecentExpanded) {
                                Icon(
                                    imageVector = if (uiState.isRecentFilesExpanded)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Recent Files"
                                )
                            }
                        }
                    }

                    if (uiState.isRecentFilesExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.recentFiles.isEmpty()) {
                            Text(
                                text = "No recent files opened yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                uiState.recentFiles.forEach { recent ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onRecentFileClicked(recent) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🎵 ${recent.title}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. MIDDLE/BOTTOM: Brief About Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ About Binaural Player",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Real-time DSP synthesis engine by SBaGen (Jim Peters)\n" +
                               "• Modernized Android & AudioTrack integration (Nicolas George)\n" +
                               "• Clean Jetpack Compose Material 3 UI by Johnson Dcunha & AI Assistant\n" +
                               "• Released under GNU GPL v2",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
