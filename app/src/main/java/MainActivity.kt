package sbgplay.binaural.`in`

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import sbgplay.binaural.`in`.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BinauralApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinauralApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadUri(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        viewModel.checkBatteryOptimization()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val tabs = listOf("Home", "Sequence", "Generator", "Playing")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Binaural Player") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.showAboutDialog(true) }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTab) {
                    0 -> PresetsScreen(
                        uiState = uiState,
                        onOpenFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onRecentFileClicked = { viewModel.loadRecentFile(it) },
                        onToggleRecentExpanded = { viewModel.toggleRecentFilesExpanded() },
                        onClearRecents = { viewModel.clearRecentFiles() }
                    )
                    1 -> SequenceScreen(
                        uiState = uiState,
                        onSequenceTextChanged = { viewModel.updateSequenceText(it) },
                        onPlay = { viewModel.onPlayCurrentSequence() }
                    )
                    2 -> GeneratorScreen(
                        viewModel = viewModel
                    )
                    3 -> PlayScreen(
                        uiState = uiState,
                        onPauseClicked = { viewModel.onPauseClicked() },
                        onStopClicked = { viewModel.onStopClicked() },
                        onRequestIgnoreBatteryOptimization = {
                            viewModel.requestIgnoreBatteryOptimization(context)
                        }
                    )
                }
            }

            if (uiState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Notice") },
                    text = { Text(uiState.errorMessage ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (uiState.showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.showAboutDialog(false) },
                    title = { Text("About Binaural Player") },
                    text = {
                        Text(
                            "Binaural Beat Generator for Android\n\n" +
                            "• Real-time DSP synthesis engine by SBaGen (Jim Peters)\n" +
                            "• Modernized Android & AudioTrack integration (Nicolas George)\n" +
                            "• Modern Jetpack Compose Material 3 UI & updates by Johnson Dcunha and Android Studio AI Assistant\n" +
                            "• Released under GNU GPL v2"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.showAboutDialog(false) }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}