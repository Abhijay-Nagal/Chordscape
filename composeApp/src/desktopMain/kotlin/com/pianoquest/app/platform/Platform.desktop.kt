package com.pianoquest.app.platform

import androidx.compose.runtime.Composable
import com.pianoquest.app.audio.AudioOutput
import com.pianoquest.app.audio.AudioRenderer
import com.pianoquest.app.input.MidiByteParser
import com.pianoquest.app.input.MidiDevice
import com.pianoquest.app.input.MidiMessage
import com.pianoquest.app.input.MidiService
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.time.LocalDate
import java.util.concurrent.locks.ReentrantLock
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.Sequencer
import javax.sound.midi.ShortMessage
import javax.sound.midi.Synthesizer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

actual class PlatformLock actual constructor() {
    private val l = ReentrantLock()
    actual fun lock() = l.lock()
    actual fun unlock() = l.unlock()
}

actual fun createAudioOutput(): AudioOutput = DesktopAudioOutput()

actual fun createMidiService(): MidiService = DesktopMidiService()

actual object Storage {
    private val dir: File by lazy {
        val override = System.getProperty("pianoquest.dataDir")
        val base = override?.let { File(it) } ?: File(System.getProperty("user.home"), ".pianoquest")
        base.apply { mkdirs() }
    }

    actual fun read(name: String): String? = readBytes(name)?.decodeToString()
    actual fun write(name: String, text: String) = writeBytes(name, text.encodeToByteArray())
    actual fun readBytes(name: String): ByteArray? = runCatching { File(dir, name).takeIf { it.exists() }?.readBytes() }.getOrNull()
    actual fun writeBytes(name: String, bytes: ByteArray) {
        runCatching {
            val target = File(dir, name)
            target.parentFile?.mkdirs()
            val tmp = File(dir, "$name.tmp")
            tmp.writeBytes(bytes)
            target.delete()
            tmp.renameTo(target)
        }
    }
    actual fun delete(name: String) { runCatching { File(dir, name).delete() } }
}

actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

actual fun nowMillis(): Long = System.nanoTime() / 1_000_000

actual fun nowWallMillis(): Long = System.currentTimeMillis()

actual val isDesktop: Boolean = true

actual val platformName: String = "Desktop"

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun requestLandscape(landscape: Boolean) {}

actual fun setImmersive(immersive: Boolean) {}

actual fun pickMidiFile(onPicked: (name: String, bytes: ByteArray) -> Unit) {
    java.awt.EventQueue.invokeLater {
        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Import a MIDI file", java.awt.FileDialog.LOAD)
        dialog.setFilenameFilter { _, n -> n.lowercase().endsWith(".mid") || n.lowercase().endsWith(".midi") }
        dialog.file = "*.mid;*.midi"
        dialog.isVisible = true
        val f = dialog.files.firstOrNull() ?: return@invokeLater
        val bytes = runCatching { f.readBytes() }.getOrNull() ?: return@invokeLater
        onPicked(f.name, bytes)
    }
}
actual fun setAllowPortrait(allow: Boolean) {}


actual val canImportFiles: Boolean = true

class DesktopAudioOutput : AudioOutput {
    override val sampleRate = 44100
    private val frames = 256
    @Volatile private var running = false
    private var thread: Thread? = null

    override fun start(renderer: AudioRenderer) {
        if (running) return
        running = true
        thread = Thread({
            val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
            val line: SourceDataLine = runCatching { AudioSystem.getSourceDataLine(format) }.getOrNull() ?: run {
                running = false
                return@Thread
            }
            line.open(format, frames * 2 * 6)
            line.start()
            val buf = FloatArray(frames)
            val bytes = ByteArray(frames * 2)
            try {
                while (running) {
                    renderer.render(buf, frames)
                    for (i in 0 until frames) {
                        val s = (buf[i].coerceIn(-1f, 1f) * 32767f).toInt()
                        bytes[2 * i] = (s and 0xFF).toByte()
                        bytes[2 * i + 1] = ((s shr 8) and 0xFF).toByte()
                    }
                    line.write(bytes, 0, bytes.size)
                }
            } finally {
                line.drain()
                line.close()
            }
        }, "Keyscape-Audio").apply {
            isDaemon = true
            priority = Thread.MAX_PRIORITY
        }
        thread?.start()
    }

    override fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }
}

/** MIDI keyboards on Windows/macOS/Linux through javax.sound.midi. */
class DesktopMidiService : MidiService {
    override val supported = true
    override val devices = MutableStateFlow<List<MidiDevice>>(emptyList())
    override val connected = MutableStateFlow<MidiDevice?>(null)
    override val status = MutableStateFlow("No MIDI keyboard connected")
    override val lastActivity = MutableStateFlow(0L)
    private var wanted: MidiDevice? = null

    private var listener: (MidiMessage) -> Unit = {}
    private val parser = MidiByteParser { listener(it) }
    private var open: javax.sound.midi.MidiDevice? = null

    init {
        refresh()
        devices.value.firstOrNull()?.let { connect(it) }
    }

    override fun setListener(listener: (MidiMessage) -> Unit) { this.listener = listener }

    private fun inputDevices(): List<Pair<MidiDevice, javax.sound.midi.MidiDevice>> =
        MidiSystem.getMidiDeviceInfo().mapIndexedNotNull { idx, info ->
            val dev = runCatching { MidiSystem.getMidiDevice(info) }.getOrNull() ?: return@mapIndexedNotNull null
            if (dev is Sequencer || dev is Synthesizer || dev.maxTransmitters == 0) return@mapIndexedNotNull null
            MidiDevice("$idx:${info.name}", info.name, listOf(info.vendor, info.description).filter { it.isNotBlank() && it != "Unknown vendor" }.joinToString(" • ")) to dev
        }

    override fun refresh() {
        val list = runCatching { inputDevices().map { it.first } }.getOrDefault(emptyList())
        devices.value = list
        if (connected.value == null) status.value = if (list.isEmpty()) "No MIDI keyboard found. Plug one in via USB and press Refresh." else "Keyboard found - click Connect"
    }

    override fun reconnect() {
        val w = wanted ?: devices.value.firstOrNull() ?: return
        refresh()
        devices.value.firstOrNull { it.name == w.name }?.let { connect(it) } ?: run { status.value = "${w.name} not found" }
    }

    override fun connect(device: MidiDevice) {
        disconnect()
        wanted = device
        val dev = inputDevices().firstOrNull { it.first.id == device.id }?.second ?: run {
            status.value = "Device not found"
            return
        }
        runCatching {
            dev.open()
            dev.transmitter.receiver = object : Receiver {
                override fun send(message: javax.sound.midi.MidiMessage, timeStamp: Long) {
                    lastActivity.value = System.currentTimeMillis()
                    if (message is ShortMessage || message.length <= 3) {
                        synchronized(parser) { parser.feed(message.message, 0, message.length) }
                    }
                }
                override fun close() {}
            }
            open = dev
            connected.value = device
            status.value = "Connected: ${device.name}"
        }.onFailure {
            status.value = "Could not open ${device.name}: ${it.message}"
        }
    }

    override fun disconnect() {
        runCatching { open?.close() }
        if (open != null) listener(MidiMessage.AllNotesOff)
        open = null
        connected.value = null
        status.value = "Disconnected"
    }
}

actual fun platformAutoScale(): Pair<Float, com.pianoquest.app.ui.FormFactor>? = null

actual fun setKeepScreenOn(on: Boolean) {}

actual fun hapticTick() {}
