package sbgplay.binaural.`in`.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import sbgplay.binaural.`in`.MainUiState

@Composable
fun SequenceScreen(
    uiState: MainUiState,
    onSequenceTextChanged: (String) -> Unit,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = uiState.sequenceTitle,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.sequenceText,
            onValueChange = onSequenceTextChanged,
            label = { Text("SBaGen Script") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Sequence ▶")
        }
    }
}