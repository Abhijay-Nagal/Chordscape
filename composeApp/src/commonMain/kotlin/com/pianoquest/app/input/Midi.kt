package com.pianoquest.app.input

import kotlinx.coroutines.flow.StateFlow

data class MidiDevice(val id: String, val name: String, val detail: String = "")

sealed interface MidiMessage {
    data class NoteOn(val note: Int, val velocity: Int) : MidiMessage
    data class NoteOff(val note: Int) : MidiMessage
    data class Sustain(val on: Boolean) : MidiMessage
    /** Release everything (device disconnected, or the keyboard sent All Notes Off). */
    data object AllNotesOff : MidiMessage
}

/** Platform MIDI input (Android MidiManager / javax.sound.midi). */
interface MidiService {
    val supported: Boolean
    val devices: StateFlow<List<MidiDevice>>
    val connected: StateFlow<MidiDevice?>
    val status: StateFlow<String>
    /** Wall-clock millis of the last MIDI message received (0 = never) - drives the activity light in Settings. */
    val lastActivity: StateFlow<Long>
    fun refresh()
    fun connect(device: MidiDevice)
    /** Close and re-open the current (or last) keyboard. */
    fun reconnect()
    fun disconnect()
    fun setListener(listener: (MidiMessage) -> Unit)
}

/** Parses a raw MIDI byte stream (with running status) into messages. */
class MidiByteParser(private val emit: (MidiMessage) -> Unit) {
    private var status = 0
    private val data = IntArray(2)
    private var count = 0
    private var inSysex = false

    fun feed(bytes: ByteArray, offset: Int, length: Int) {
        for (i in offset until offset + length) feed(bytes[i].toInt() and 0xFF)
    }

    fun feed(b: Int) {
        if (b >= 0xF8) return // real-time messages (clock, active sensing) - ignore
        if (b == 0xF0) { inSysex = true; return }
        if (b == 0xF7) { inSysex = false; return }
        if (inSysex) return
        if (b >= 0x80) {
            status = b
            count = 0
            if (b >= 0xF0) status = 0
            return
        }
        if (status == 0) return
        data[count++] = b
        val needed = when (status and 0xF0) { 0xC0, 0xD0 -> 1; else -> 2 }
        if (count < needed) return
        count = 0
        when (status and 0xF0) {
            0x90 -> if (data[1] == 0) emit(MidiMessage.NoteOff(data[0])) else emit(MidiMessage.NoteOn(data[0], data[1]))
            0x80 -> emit(MidiMessage.NoteOff(data[0]))
            0xB0 -> when (data[0]) {
                64 -> emit(MidiMessage.Sustain(data[1] >= 64))
                120, 123 -> emit(MidiMessage.AllNotesOff)
            }
        }
    }
}
