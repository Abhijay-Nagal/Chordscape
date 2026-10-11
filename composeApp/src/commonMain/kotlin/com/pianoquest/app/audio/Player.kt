package com.pianoquest.app.audio

import com.pianoquest.app.data.NoteEvent
import com.pianoquest.app.data.Notation
import com.pianoquest.app.input.InputSource
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.nowMillis
import com.pianoquest.app.theory.Chord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Plays sequences of notes through the synth, lighting up the on-screen keys as "demo" notes. */
object Player {

    suspend fun playNotation(text: String, bpm: Int = 90, onIndex: (Int) -> Unit = {}) =
        play(Notation.parse(text), bpm.toDouble(), onIndex)

    suspend fun playChord(chord: Chord, octave: Int = 4, arpeggiate: Boolean = false, seconds: Double = 1.6) {
        val midis = chord.midis(octave)
        val bass = chord.bass?.let { listOf(chord.bassMidi()) } ?: emptyList()
        val all = bass + midis
        try {
            for ((i, m) in all.withIndex()) {
                PianoInput.noteOn(m, 88, InputSource.DEMO)
                if (arpeggiate && i < all.lastIndex) delay(110)
            }
            delay((seconds * 1000).toLong())
        } finally {
            for (m in all) PianoInput.noteOff(m, InputSource.DEMO)
        }
    }

    /** Plays events in real time. [onIndex] reports the index of the onset group being played. */
    suspend fun play(events: List<NoteEvent>, bpm: Double, onIndex: (Int) -> Unit = {}) {
        if (events.isEmpty()) return
        val beatMs = 60000.0 / bpm
        data class Action(val timeMs: Double, val on: Boolean, val midi: Int, val velocity: Int, val group: Int)
        val starts = events.map { it.start }.distinct().sorted()
        val actions = ArrayList<Action>()
        for (e in events) {
            val g = starts.indexOf(e.start)
            actions += Action(e.start * beatMs, true, e.midi, e.velocity, g)
            actions += Action((e.start + e.duration) * beatMs - 12, false, e.midi, 0, g)
        }
        actions.sortWith(compareBy({ it.timeMs }, { it.on }))
        val sounding = HashMap<Int, Int>()
        val t0 = nowMillis()
        try {
            var lastGroup = -1
            for (a in actions) {
                val wait = (t0 + a.timeMs - nowMillis()).toLong()
                if (wait > 0) delay(wait)
                if (a.on) {
                    PianoInput.noteOn(a.midi, a.velocity, InputSource.DEMO)
                    sounding[a.midi] = (sounding[a.midi] ?: 0) + 1
                    if (a.group != lastGroup) { lastGroup = a.group; onIndex(a.group) }
                } else if ((sounding[a.midi] ?: 0) > 0) {
                    PianoInput.noteOff(a.midi, InputSource.DEMO)
                    sounding[a.midi] = sounding[a.midi]!! - 1
                }
            }
        } catch (e: CancellationException) {
            throw e
        } finally {
            for ((m, c) in sounding) repeat(c) { PianoInput.noteOff(m, InputSource.DEMO) }
            onIndex(-1)
        }
    }
}
