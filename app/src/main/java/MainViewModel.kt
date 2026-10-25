package sbgplay.binaural.`in`

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.PowerManager
import android.os.RemoteException
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class RecentFile(
    val title: String,
    val uriString: String
)

data class ScriptOptionGroup(
    val optionName: String,
    val choices: List<String>,
    val selectedIndex: Int = 0
)

data class MainUiState(
    val selectedTab: Int = 0,
    val sequenceTitle: String = "Untitled",
    val sequenceText: String = "",
    val playingSequence: String? = null,
    val totalDurationMs: Int = 0,
    val elapsedTimeMs: Int = 0,
    val isPaused: Boolean = false,
    val errorMessage: String? = null,
    val showAboutDialog: Boolean = false,
    val currentDirectory: File = File("/"),
    val fileList: List<String> = emptyList(),
    val scriptFile: File? = null,
    val scriptOptions: List<ScriptOptionGroup> = emptyList(),
    val isIgnoringBatteryOptimizations: Boolean = true,
    val recentFiles: List<RecentFile> = emptyList(),
    val isRecentFilesExpanded: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application),
    Handler.Callback, ServiceConnection {

    private val _uiState = MutableStateFlow(
        MainUiState(
            sequenceTitle = Presets.items[0].title,
            sequenceText = Presets.items[0].sequence
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Tone generator form inputs
    var editDuration by mutableStateOf("30")
    var editPink by mutableStateOf("15")
    var editF1Carrier by mutableStateOf("440")
    var editF1Beat by mutableStateOf("10")
    var editF1Vol by mutableStateOf("60")
    var editF2Carrier by mutableStateOf("0")
    var editF2Beat by mutableStateOf("0")
    var editF2Vol by mutableStateOf("0")

    private var playerService: Messenger? = null
    private var isConnecting = false
    private val incomingMessenger = Messenger(Handler(Looper.getMainLooper(), this))
    private val prefs = application.getSharedPreferences("binaural_prefs", Context.MODE_PRIVATE)

    init {
        connectService()
        _uiState.value = _uiState.value.copy(recentFiles = loadRecentFiles())
    }

    private fun loadRecentFiles(): List<RecentFile> {
        val raw = prefs.getString("recent_files_v1", null) ?: return emptyList()
        return raw.split(";;;").mapNotNull { line ->
            val parts = line.split("|||")
            if (parts.size >= 2) RecentFile(parts[0], parts[1]) else null
        }
    }

    private fun addRecentFile(title: String, uriString: String) {
        val current = loadRecentFiles().toMutableList()
        current.removeAll { it.uriString == uriString || it.title == title }
        current.add(0, RecentFile(title, uriString))
        val trimmed = current.take(5)
        val encoded = trimmed.joinToString(";;;") { "${it.title}|||${it.uriString}" }
        prefs.edit().putString("recent_files_v1", encoded).apply()
        _uiState.value = _uiState.value.copy(recentFiles = trimmed)
    }

    fun toggleRecentFilesExpanded() {
        _uiState.value = _uiState.value.copy(
            isRecentFilesExpanded = !_uiState.value.isRecentFilesExpanded
        )
    }

    fun clearRecentFiles() {
        prefs.edit().remove("recent_files_v1").apply()
        _uiState.value = _uiState.value.copy(recentFiles = emptyList())
    }

    fun loadRecentFile(recentFile: RecentFile) {
        try {
            val uri = Uri.parse(recentFile.uriString)
            loadUri(uri)
        } catch (e: Exception) {
            showError("Failed to open recent file: ${e.message}")
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun loadPreset(preset: Preset) {
        _uiState.value = _uiState.value.copy(
            sequenceTitle = preset.title,
            sequenceText = preset.sequence,
            selectedTab = 1
        )
    }

    fun loadUri(uri: Uri) {
        try {
            val contentResolver = getApplication<Application>().contentResolver
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val inputStream = contentResolver.openInputStream(uri) ?: return
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Sequence"
            addRecentFile(fileName, uri.toString())
            _uiState.value = _uiState.value.copy(
                sequenceTitle = fileName,
                sequenceText = sb.toString(),
                selectedTab = 1
            )
        } catch (e: Exception) {
            showError("Failed to read selected file: ${e.message}")
        }
    }

    fun updateSequenceText(text: String) {
        _uiState.value = _uiState.value.copy(sequenceText = text)
    }

    fun generateEditSequence(): String? {
        return try {
            val dur = editDuration.toInt()
            if (dur < 1) return null
            val decl = StringBuilder("beat:")
            if (editPink.isNotBlank() && editPink != "0") {
                decl.append(" pink/").append(editPink)
            }
            val tones = listOf(
                Triple(editF1Carrier, editF1Beat, editF1Vol),
                Triple(editF2Carrier, editF2Beat, editF2Vol)
            )
            for ((carrier, rawBeat, vol) in tones) {
                var beat = rawBeat.trim()
                if (beat.isNotEmpty() && !beat.startsWith("-") && !beat.startsWith("+")) {
                    beat = "+$beat"
                }
                if (vol.isNotBlank() && vol != "0" && carrier.isNotBlank() && carrier != "0") {
                    decl.append(" ").append(carrier).append(beat).append("/").append(vol)
                }
            }
            val end = String.format("%02d:%02d:00", dur / 60, dur % 60)
            "$decl\noff: -\nNOW beat\n+$end off\n"
        } catch (e: Exception) {
            showError("Error generating sequence: ${e.message}")
            null
        }
    }

    fun onGenerateEdit() {
        val seq = generateEditSequence() ?: return
        _uiState.value = _uiState.value.copy(
            sequenceTitle = "Custom Tone ($editDuration min)",
            sequenceText = seq,
            selectedTab = 1
        )
    }

    fun onEditEditClicked() {
        onGenerateEdit()
    }

    fun onEditPlayClicked() {
        val seq = generateEditSequence() ?: return
        _uiState.value = _uiState.value.copy(
            sequenceTitle = "Custom Tone ($editDuration min)",
            sequenceText = seq
        )
        playSequence(seq)
    }

    fun onPlayCurrentSequence() {
        playSequence(_uiState.value.sequenceText)
    }

    fun playSequence(sequence: String?) {
        if (sequence.isNullOrBlank()) {
            showError("Sequence is empty.")
            return
        }
        val intent = Intent(getApplication(), Binaural_player::class.java)
        getApplication<Application>().startService(intent)

        val msg = Message.obtain(null, 'R'.code)
        val b = Bundle(1).apply { putString("seq", sequence) }
        msg.data = b
        sendMessage(msg)
    }

    fun onPauseClicked() {
        val msg = Message.obtain(null, 'C'.code)
        msg.arg1 = if (_uiState.value.isPaused) 'R'.code else 'P'.code
        sendMessage(msg)
    }

    fun onStopClicked() {
        val msg = Message.obtain(null, 'C'.code)
        msg.arg1 = 'S'.code
        sendMessage(msg)
    }

    fun showAboutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAboutDialog = show)
    }

    fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isIgnoring = pm?.isIgnoringBatteryOptimizations(getApplication<Application>().packageName) ?: true
            _uiState.value = _uiState.value.copy(isIgnoringBatteryOptimizations = isIgnoring)
        }
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm?.isIgnoringBatteryOptimizations(packageName) == false) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(fallbackIntent)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun showError(msg: String) {
        _uiState.value = _uiState.value.copy(errorMessage = msg)
    }

    private fun connectService() {
        val intent = Intent(getApplication(), Binaural_player::class.java)
        isConnecting = true
        getApplication<Application>().bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun sendMessage(msg: Message) {
        val service = playerService ?: return
        msg.replyTo = incomingMessenger
        try {
            service.send(msg)
        } catch (_: RemoteException) {}
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        playerService = Messenger(binder)
        val msg = Message.obtain(null, 'I'.code)
        sendMessage(msg)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        playerService = null
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            'S'.code -> {
                val b = msg.data
                val seq = b?.getString("seq")
                val duration = b?.getInt("duration") ?: 0
                _uiState.value = _uiState.value.copy(
                    playingSequence = seq,
                    totalDurationMs = duration,
                    selectedTab = if (seq != null) 3 else _uiState.value.selectedTab
                )
                true
            }
            'T'.code -> {
                _uiState.value = _uiState.value.copy(elapsedTimeMs = msg.arg1)
                true
            }
            'P'.code -> {
                _uiState.value = _uiState.value.copy(isPaused = msg.arg1 != 0)
                true
            }
            'E'.code -> {
                val b = msg.data
                showError(b?.getString("message") ?: "Audio error")
                true
            }
            else -> false
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (playerService != null) {
            val msg = Message.obtain(null, 'J'.code)
            sendMessage(msg)
            getApplication<Application>().unbindService(this)
            playerService = null
        }
    }
}