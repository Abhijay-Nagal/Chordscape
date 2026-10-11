package com.pianoquest.app.input

import com.pianoquest.app.audio.Audio
import com.pianoquest.app.platform.PlatformLock
import com.pianoquest.app.platform.createMidiService
import com.pianoquest.app.platform.nowMillis
import com.pianoquest.app.platform.withLock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

enum class InputSource { TOUCH, MIDI, COMPUTER_KEYBOARD, DEMO }

data class KeyEvent(val midi: Int, val down: Boolean, val velocity: Int, val source: InputSource, val timeMillis: Long)

/**
 * The single place where every key press arrives - on-screen keys, a MIDI keyboard, the computer keyboard
 * or the app's own demo playback. It plays the sound and broadcasts events to lessons and games.
 */
object PianoInput {
    private val lock = PlatformLock()
    private val counts = HashMap<Int, Int>()
    private val demoCounts = HashMap<Int, Int>()

    private val _held = MutableStateFlow<Set<Int>>(emptySet())
    /** Keys currently held by the player (not demo playback). */
    val held: StateFlow<Set<Int>> = _held.asStateFlow()

    private val _demo = MutableStateFlow<Set<Int>>(emptySet())
    /** Keys currently sounding from the app's demo / accompaniment playback. */
    val demo: StateFlow<Set<Int>> = _demo.asStateFlow()

    private val _events = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 512)
    val events: SharedFlow<KeyEvent> = _events

    private val _sustain = MutableStateFlow(false)
    val sustain: StateFlow<Boolean> = _sustain.asStateFlow()

    /** When false, notes from a MIDI keyboard are not voiced by the app (the keyboard makes its own sound). */
    var playMidiThroughApp = true
    /** Touch sensitivity for the player's own notes: OFF, LIGHT, NORMAL or HEAVY. */
    var touchCurve = "NORMAL"

    private fun shape(velocity: Int): Int {
        val v = velocity.coerceIn(1, 127) / 127.0
        return when (touchCurve) {
            "OFF" -> 92
            "LIGHT" -> (127 * v.pow(0.6)).toInt()
            "HEAVY" -> (127 * v.pow(1.6)).toInt()
            else -> velocity
        }.coerceIn(1, 127)
    }

    /** Mutes every sound the app makes when false. */
    var soundEnabled = true

    val midi: MidiService by lazy {
        createMidiService().also { svc ->
            svc.setListener { msg ->
                when (msg) {
                    is MidiMessage.NoteOn -> noteOn(msg.note, msg.velocity, InputSource.MIDI)
                    is MidiMessage.NoteOff -> noteOff(msg.note, InputSource.MIDI)
                    is MidiMessage.Sustain -> setSustain(msg.on)
                    MidiMessage.AllNotesOff -> { setSustain(false); releasePlayer() }
                }
            }
        }
    }

    fun noteOn(midi: Int, velocityIn: Int = 96, source: InputSource = InputSource.TOUCH) {
        if (midi !in 0..127) return
        val velocity = if (source == InputSource.DEMO) velocityIn else shape(velocityIn)
        val map = if (source == InputSource.DEMO) demoCounts else counts
        lock.withLock {
            map[midi] = (map[midi] ?: 0) + 1
            publish(source)
        }
        if (soundEnabled && (source != InputSource.MIDI || playMidiThroughApp)) Audio.noteOn(midi, velocity)
        _events.tryEmit(KeyEvent(midi, true, velocity, source, nowMillis()))
    }

    fun noteOff(midi: Int, source: InputSource = InputSource.TOUCH) {
        if (midi !in 0..127) return
        val map = if (source == InputSource.DEMO) demoCounts else counts
        val released = lock.withLock {
            val c = (map[midi] ?: 0) - 1
            if (c <= 0) map.remove(midi) else map[midi] = c
            publish(source)
            c <= 0
        }
        if (released && (source != InputSource.MIDI || playMidiThroughApp)) Audio.noteOff(midi)
        _events.tryEmit(KeyEvent(midi, false, 0, source, nowMillis()))
    }

    fun setSustain(on: Boolean) {
        _sustain.value = on
        Audio.sustain(on)
    }

    /** Releases every demo note (used when stopping playback). */
    fun releaseDemo() {
        val notes = lock.withLock { demoCounts.keys.toList() }
        for (n in notes) {
            lock.withLock { demoCounts.remove(n); publish(InputSource.DEMO) }
            Audio.noteOff(n)
        }
    }

    /** Releases every key held by the player (touch, MIDI, computer keyboard). */
    fun releasePlayer() {
        val notes = lock.withLock { counts.keys.toList().also { counts.clear(); publish(InputSource.TOUCH) } }
        notes.forEach { Audio.noteOff(it) }
    }

    fun releaseAll() {
        val notes = lock.withLock { counts.keys.toList() }
        lock.withLock { counts.clear(); publish(InputSource.TOUCH) }
        notes.forEach { Audio.noteOff(it) }
        releaseDemo()
    }

    private fun publish(source: InputSource) {
        if (source == InputSource.DEMO) _demo.value = demoCounts.keys.toSet()
        else _held.value = counts.keys.toSet()
    }
}
