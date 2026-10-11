package com.pianoquest.app.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pianoquest.app.audio.AudioOutput
import com.pianoquest.app.audio.AudioRenderer
import com.pianoquest.app.input.MidiService
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDate
import java.util.concurrent.locks.ReentrantLock

/** Holds the application context and current activity, set from MainActivity. */
@SuppressLint("StaticFieldLeak")
object AndroidPlatform {
    lateinit var appContext: Context
    var activity: WeakReference<Activity>? = null
    var midiPicker: ActivityResultLauncher<Array<String>>? = null
    var pendingPick: ((String, ByteArray) -> Unit)? = null
    /** Interface scale applied by MainActivity for this screen, and the device class. */
    var uiScale: Pair<Float, com.pianoquest.app.ui.FormFactor>? = null

    /** Called by MainActivity with the document the user picked. */
    fun onPicked(uri: Uri?) {
        val cb = pendingPick ?: return
        pendingPick = null
        if (uri == null) return
        val resolver = appContext.contentResolver
        var name = "Imported song.mid"
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) name = c.getString(0) ?: name
            }
        }
        val bytes = runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return
        cb(name, bytes)
    }
}

actual class PlatformLock actual constructor() {
    private val l = ReentrantLock()
    actual fun lock() = l.lock()
    actual fun unlock() = l.unlock()
}

actual fun createAudioOutput(): AudioOutput = AndroidAudioOutput(AndroidPlatform.appContext)

actual fun createMidiService(): MidiService = AndroidMidiService(AndroidPlatform.appContext)

actual object Storage {
    private fun file(name: String) = File(AndroidPlatform.appContext.filesDir, name)
    actual fun read(name: String): String? = runCatching { file(name).takeIf { it.exists() }?.readText() }.getOrNull()
    actual fun write(name: String, text: String) = writeBytes(name, text.encodeToByteArray())
    actual fun readBytes(name: String): ByteArray? = runCatching { file(name).takeIf { it.exists() }?.readBytes() }.getOrNull()
    actual fun writeBytes(name: String, bytes: ByteArray) {
        runCatching {
            val tmp = file("$name.tmp")
            tmp.parentFile?.mkdirs()
            tmp.writeBytes(bytes)
            val target = file(name)
            if (!tmp.renameTo(target)) { target.delete(); tmp.renameTo(target) }
        }
    }
    actual fun delete(name: String) { runCatching { file(name).delete() } }
}

actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

actual fun nowMillis(): Long = System.nanoTime() / 1_000_000

actual fun nowWallMillis(): Long = System.currentTimeMillis()

actual val isDesktop: Boolean = false

actual val platformName: String = "Android"

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

private var allowPortrait = false
private var forceLandscape = false

private fun applyOrientation() {
    AndroidPlatform.activity?.get()?.requestedOrientation =
        if (forceLandscape || !allowPortrait) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_FULL_USER
}

actual fun requestLandscape(landscape: Boolean) {
    forceLandscape = landscape
    applyOrientation()
}

actual fun setAllowPortrait(allow: Boolean) {
    allowPortrait = allow
    applyOrientation()
}

actual fun setImmersive(immersive: Boolean) {
    val activity = AndroidPlatform.activity?.get() ?: return
    activity.runOnUiThread {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (immersive) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

actual fun pickMidiFile(onPicked: (name: String, bytes: ByteArray) -> Unit) {
    val launcher = AndroidPlatform.midiPicker ?: return
    AndroidPlatform.pendingPick = onPicked
    launcher.launch(arrayOf("audio/midi", "audio/x-midi", "audio/mid", "application/x-midi", "application/octet-stream"))
}

actual val canImportFiles: Boolean = true

actual fun platformAutoScale(): Pair<Float, com.pianoquest.app.ui.FormFactor>? = AndroidPlatform.uiScale

actual fun setKeepScreenOn(on: Boolean) {
    val activity = AndroidPlatform.activity?.get() ?: return
    activity.runOnUiThread {
        if (on) activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

actual fun hapticTick() {
    AndroidPlatform.activity?.get()?.window?.decorView?.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
}

/**
 * Streams the synth to a low-latency float AudioTrack on an audio-priority thread.
 *
 * Latency: the track's buffer is trimmed to two hardware bursts (a few milliseconds) instead of the larger default,
 * and a tuner adds one burst at a time only if this device actually underruns - the same approach as Google's Oboe
 * library. Key presses from a MIDI keyboard reach the synth directly on the MIDI thread and are heard on the next block.
 * The track is rebuilt automatically if it dies or the audio route changes (for example when a digital piano that is
 * also a USB audio device is plugged in or unplugged) - previously this could silence the app until restart.
 */
class AndroidAudioOutput(private val context: Context) : AudioOutput {
    override val sampleRate: Int
    private val framesPerBlock: Int
    @Volatile private var running = false
    @Volatile private var rebuild = false
    private var thread: Thread? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) { rebuild = true }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) { rebuild = true }
    }

    init {
        sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 48000
        val burst = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toIntOrNull() ?: 256
        framesPerBlock = burst.coerceIn(64, 512)
    }

    private fun buildTrack(): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, framesPerBlock * 4 * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
    }

    override fun start(renderer: AudioRenderer) {
        if (running) return
        running = true
        audioManager.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        thread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val buf = FloatArray(framesPerBlock)
            var track: AudioTrack? = null
            var failures = 0
            var bufferFrames = framesPerBlock * 2
            var lastUnderruns = 0
            var blocks = 0
            while (running) {
                try {
                    if (track == null || rebuild || track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        rebuild = false
                        track?.let { runCatching { it.pause(); it.flush(); it.release() } }
                        track = buildTrack().also { t ->
                            bufferFrames = framesPerBlock * 2
                            runCatching { t.setBufferSizeInFrames(bufferFrames) }
                            t.play()
                            lastUnderruns = runCatching { t.underrunCount }.getOrDefault(0)
                        }
                    }
                    // Latency tuner: grow the buffer by one burst whenever the device underruns (glitches).
                    if (++blocks % 32 == 0) {
                        val u = runCatching { track.underrunCount }.getOrDefault(lastUnderruns)
                        if (u > lastUnderruns && bufferFrames + framesPerBlock <= track.bufferCapacityInFrames) {
                            bufferFrames += framesPerBlock
                            runCatching { track.setBufferSizeInFrames(bufferFrames) }
                        }
                        lastUnderruns = u
                    }
                    renderer.render(buf, framesPerBlock)
                    val written = track.write(buf, 0, framesPerBlock, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) {
                        Log.w("Keyscape", "AudioTrack write error $written - rebuilding")
                        rebuild = true
                        failures++
                        if (failures > 3) Thread.sleep(50)
                    } else failures = 0
                } catch (t: Throwable) {
                    Log.w("Keyscape", "Audio thread error - rebuilding", t)
                    runCatching { track?.release() }
                    track = null
                    Thread.sleep(100)
                }
            }
            runCatching { track?.stop() }
            runCatching { track?.release() }
        }, "Keyscape-Audio")
        thread?.start()
    }

    override fun stop() {
        running = false
        runCatching { audioManager.unregisterAudioDeviceCallback(deviceCallback) }
        thread?.join(500)
        thread = null
    }
}
