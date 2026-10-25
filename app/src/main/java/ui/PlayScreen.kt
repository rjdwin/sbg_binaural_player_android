package org.cigaes.binaural_player.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.cigaes.binaural_player.MainUiState

@Composable
fun PlayScreen(
    uiState: MainUiState,
    onPauseClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onRequestIgnoreBatteryOptimization: (() -> Unit)? = null
) {
    val isPlaying = uiState.playingSequence != null && !uiState.isPaused

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!uiState.isIgnoringBatteryOptimizations && onRequestIgnoreBatteryOptimization != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ Longer Background Run",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ignore battery optimization to keep playback active when device sleeps.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestIgnoreBatteryOptimization,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Disable Battery Optimization")
                    }
                }
            }
        }
        // Waveform / Equalizer Animation
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(80.dp)
        ) {
            val bars = 9
            for (i in 0 until bars) {
                val infiniteTransition = rememberInfiniteTransition(label = "bar_$i")
                val heightAnim by infiniteTransition.animateFloat(
                    initialValue = 12f,
                    targetValue = if (isPlaying) 20f + ((i * 17) % 55) else 12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + i * 80, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "height_$i"
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(heightAnim.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val elapsed = formatTime(uiState.elapsedTimeMs)
        val total = if (uiState.totalDurationMs > 0) " / " + formatTime(uiState.totalDurationMs) else ""

        Text(
            text = "$elapsed$total",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        val progress = if (uiState.totalDurationMs > 0) {
            (uiState.elapsedTimeMs.toFloat() / uiState.totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onPauseClicked,
                enabled = uiState.playingSequence != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.isPaused) "Resume ▶" else "Pause ❚❚")
            }
            OutlinedButton(
                onClick = onStopClicked,
                enabled = uiState.playingSequence != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop ■")
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val sec = (ms / 1000) % 60
    val min = (ms / 1000) / 60
    return String.format("%d:%02d", min, sec)
}