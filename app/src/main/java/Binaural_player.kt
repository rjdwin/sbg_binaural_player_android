package org.cigaes.binaural_player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

class Binaural_player : Service(), Handler.Callback {

    private val channelId = "binaural_player_channel"
    private val notificationId = 1001

    private var playingTime = -1
    private var playingPaused = false
    private var playingSequence: String? = null
    private var playingTotalTime = 0

    private val clients = ArrayList<Messenger>()
    private val incomingMessenger = Messenger(Handler(Looper.getMainLooper(), this))

    private var decoder: Binaural_decoder? = null
    private var decoderThread: Thread? = null
    private var playingNext: String? = null

    override fun onBind(intent: Intent?): IBinder = incomingMessenger.binder

    override fun onUnbind(intent: Intent?): Boolean {
        exitIfFinished()
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PAUSE -> decoderPause(!playingPaused)
                ACTION_STOP -> decoderStop()
            }
        }
        return START_NOT_STICKY
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            'I'.code -> {
                msg.replyTo?.let {
                    if (!clients.contains(it)) clients.add(it)
                    clientSendStatus(it)
                    clientSendTime(it)
                    clientSendPause(it)
                }
                return true
            }
            'J'.code -> {
                msg.replyTo?.let { clients.remove(it) }
                exitIfFinished()
                return true
            }
            'R'.code -> {
                val b = msg.data
                decoderStart(b?.getString("seq") ?: "")
                return true
            }
            'C'.code -> {
                handleClientControl(msg.arg1.toChar())
                return true
            }
            't'.code -> {
                playingPaused = false
                if (msg.arg1 >= 0) {
                    playingTime = msg.arg1
                    clientSendTime(null)
                    updateNotification()
                } else {
                    decoderReap()
                }
                return true
            }
            'e'.code -> {
                val b = msg.data
                clientSendError(null, b?.getString("message") ?: "Error")
                decoderReap()
                return true
            }
        }
        return false
    }

    private fun handleClientControl(cmd: Char) {
        when (cmd) {
            'S' -> decoderStop()
            'P' -> decoderPause(true)
            'R' -> decoderPause(false)
        }
    }

    private fun clientSendStatus(client: Messenger?) {
        val b = Bundle(2).apply {
            putString("seq", playingSequence)
            putInt("duration", playingTotalTime)
        }
        clientSendMessage(client, 'S', 0, b)
    }

    private fun clientSendTime(client: Messenger?) {
        clientSendMessage(client, 'T', playingTime, null)
    }

    private fun clientSendPause(client: Messenger?) {
        clientSendMessage(client, 'P', if (playingPaused) 1 else 0, null)
    }

    private fun clientSendError(client: Messenger?, error: String) {
        val b = Bundle(1).apply { putString("message", error) }
        clientSendMessage(client, 'E', 0, b)
    }

    private fun clientSendMessage(client: Messenger?, code: Char, arg1: Int, b: Bundle?) {
        val msg = Message.obtain(null, code.code).apply {
            this.arg1 = arg1
            data = b
        }
        if (client == null) {
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val c = iterator.next()
                try {
                    c.send(msg)
                } catch (_: RemoteException) {
                    iterator.remove()
                }
            }
        } else {
            try {
                client.send(msg)
            } catch (_: RemoteException) {
                clients.remove(client)
            }
        }
    }

    private fun decoderStart(seq: String) {
        if (decoder != null) {
            decoderStop()
            playingNext = seq
            return
        }
        playingSequence = seq
        playingTotalTime = parseTotalTime(seq)
        playingTime = 0
        playingPaused = false

        val dec = Binaural_decoder(incomingMessenger, seq)
        decoder = dec
        val t = Thread(dec, "BinauralDecoderThread")
        decoderThread = t
        t.start()

        clientSendStatus(null)
        setForegroundNotification()
    }

    private fun decoderStop() {
        decoder?.set_command('S')
    }

    private fun decoderPause(pause: Boolean) {
        decoder?.set_command(if (pause) 'P' else 'R')
        playingPaused = pause
        clientSendPause(null)
        updateNotification()
    }

    private fun decoderReap() {
        playingSequence = null
        clientSendStatus(null)
        if (decoder == null) return

        @Suppress("DEPRECATION")
        stopForeground(true)
        val t = decoderThread
        while (t != null && t.isAlive) {
            try {
                t.join(500)
                break
            } catch (_: InterruptedException) {}
        }
        decoder = null
        decoderThread = null

        if (playingNext != null) {
            val next = playingNext!!
            playingNext = null
            decoderStart(next)
        } else {
            exitIfFinished()
        }
    }

    private fun exitIfFinished() {
        if (clients.isEmpty() && decoder == null) {
            stopSelf()
        }
    }

    private fun setForegroundNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Binaural Beat Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows controls for ongoing binaural beat playback"
            }
            nm.createNotificationChannel(channel)
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pOpen = PendingIntent.getActivity(this, 0, openIntent, flags)

        val pauseIntent = Intent(this, Binaural_player::class.java).apply { action = ACTION_PAUSE }
        val pPause = PendingIntent.getService(this, 1, pauseIntent, flags)

        val stopIntent = Intent(this, Binaural_player::class.java).apply { action = ACTION_STOP }
        val pStop = PendingIntent.getService(this, 2, stopIntent, flags)

        val min = (playingTime / 1000) / 60
        val sec = (playingTime / 1000) % 60
        val timeStr = String.format("%d:%02d", min, sec)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(if (playingPaused) "Paused" else "Playing Binaural Beat")
            .setContentText("Elapsed: $timeStr")
            .setContentIntent(pOpen)
            .setOngoing(!playingPaused)
            .addAction(0, if (playingPaused) "Resume" else "Pause", pPause)
            .addAction(0, "Stop", pStop)
            .build()
    }

    private val timeRegex = Pattern.compile("\\s*\\+(\\d+(?::\\d+)*)\\s+.*")

    private fun parseTotalTime(seq: String): Int {
        val lines = seq.split("\n")
        for (i in lines.indices.reversed()) {
            val line = lines[i].trim()
            if (line.isNotEmpty()) {
                val m = timeRegex.matcher(line)
                if (m.matches()) {
                    val parts = m.group(1)?.split(":") ?: emptyList()
                    var total = 0
                    for (p in parts) {
                        total = total * 60 + (p.toIntOrNull() ?: 0)
                    }
                    return total * 1000
                }
            }
        }
        return -1
    }

    companion object {
        const val ACTION_PAUSE = "org.cigaes.binaural_player.ACTION_PAUSE"
        const val ACTION_STOP = "org.cigaes.binaural_player.ACTION_STOP"
    }
}