package sbgplay.binaural.`in`.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sbgplay.binaural.`in`.MainViewModel

@Composable
fun EditScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.editDuration,
            onValueChange = { viewModel.editDuration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.editPink,
            onValueChange = { viewModel.editPink = it },
            label = { Text("Pink Noise") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Tone 1")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.editF1Carrier,
                onValueChange = { viewModel.editF1Carrier = it },
                label = { Text("Carrier") },
                modifier = Modifier.weight(1f)
            )
            Text("+")
            OutlinedTextField(
                value = viewModel.editF1Beat,
                onValueChange = { viewModel.editF1Beat = it },
                label = { Text("Beat") },
                modifier = Modifier.weight(1f)
            )
            Text("/")
            OutlinedTextField(
                value = viewModel.editF1Vol,
                onValueChange = { viewModel.editF1Vol = it },
                label = { Text("Vol") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Tone 2")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.editF2Carrier,
                onValueChange = { viewModel.editF2Carrier = it },
                label = { Text("Carrier") },
                modifier = Modifier.weight(1f)
            )
            Text("+")
            OutlinedTextField(
                value = viewModel.editF2Beat,
                onValueChange = { viewModel.editF2Beat = it },
                label = { Text("Beat") },
                modifier = Modifier.weight(1f)
            )
            Text("/")
            OutlinedTextField(
                value = viewModel.editF2Vol,
                onValueChange = { viewModel.editF2Vol = it },
                label = { Text("Vol") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onEditEditClicked() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate Edit")
            }
            Button(
                onClick = { viewModel.onEditPlayClicked() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Play ▶")
            }
        }
    }
}
