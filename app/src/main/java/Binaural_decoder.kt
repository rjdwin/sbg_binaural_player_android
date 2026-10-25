package sbgplay.binaural.`in`

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log

class Binaural_decoder(
    private val service: Messenger,
    private val sequence: String
) : Runnable {

    private val rate = 44100
    private var track: AudioTrack? = null
    private var playPos = 0
    private var playPosNotify = 0
    @Volatile
    private var command: Char = '\u0000'

    override fun run() {
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBufSize = AudioTrack.getMinBufferSize(rate, channelConfig, encoding)
        val bufferSize = minBufSize * 8

        track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(rate)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } else {
            @Suppress("DEPRECATION")
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                rate,
                channelConfig,
                encoding,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
        }

        try {
            sbagen_init()
            sbagen_set_parameters(rate, 0, 0, null)
            sbagen_parse_seq(sequence)
            track?.play()
            sbagen_run()
            sendStatus(-1, null)
        } catch (_: InterruptedException) {
            sendStatus(-1, null)
        } catch (e: Exception) {
            sendStatus(-1, e.message ?: "Unknown playback error")
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
            track = null
            sbagen_free_seq()
            sbagen_exit()
        }
    }

    private fun sendStatus(t: Int, e: String?) {
        val msg = Message.obtain(null, 0)
        if (e == null) {
            msg.what = 't'.code
            msg.arg1 = t
        } else {
            msg.what = 'e'.code
            val b = Bundle(2)
            b.putString("message", e)
            msg.data = b
        }
        try {
            service.send(msg)
        } catch (_: RemoteException) {}
    }

    @Synchronized
    @Throws(InterruptedException::class)
    fun obey_command() {
        while (true) {
            val cmd = command
            command = '\u0000'
            when (cmd) {
                'S' -> throw InterruptedException()
                'P' -> waitNewCommand()
                'R' -> {}
                '\u0000' -> return
                else -> Log.w("Binaural_decoder", "Unknown command: $cmd")
            }
        }
    }

    @Synchronized
    private fun waitNewCommand() {
        try {
            track?.pause()
        } catch (_: Exception) {}
        while (command == '\u0000') {
            try {
                (this as java.lang.Object).wait()
            } catch (_: InterruptedException) {}
        }
        try {
            track?.play()
        } catch (_: Exception) {}
    }

    /**
     * Called directly from C via JNI in `sbagen.c`
     */
    @Synchronized
    @Throws(InterruptedException::class)
    fun out(data: ShortArray) {
        obey_command()
        track?.write(data, 0, data.size)
        playPos += data.size / 2
        if (playPos >= playPosNotify) {
            sendStatus((1000.0 * playPos / rate).toInt(), null)
            playPosNotify = playPos + rate - 1
            playPosNotify -= playPosNotify % rate
        }
    }

    @Synchronized
    fun set_command(c: Char) {
        command = c
        (this as java.lang.Object).notifyAll()
    }

    companion object {
        init {
            System.loadLibrary("sbagen")
        }
    }

    private external fun sbagen_init()
    private external fun sbagen_set_parameters(rate: Int, prate: Int, fade: Int, roll: String?)
    private external fun sbagen_exit()
    private external fun sbagen_parse_seq(seq: String)
    private external fun sbagen_free_seq()
    private external fun sbagen_run()
}