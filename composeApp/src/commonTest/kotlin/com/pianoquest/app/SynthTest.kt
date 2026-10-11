package com.pianoquest.app

import com.pianoquest.app.audio.Instrument
import com.pianoquest.app.audio.SynthEngine
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class SynthTest {
    private fun peakAfterNotes(inst: Instrument, notes: List<Int>): Float {
        val e = SynthEngine(44100)
        e.setInstrument(inst)
        notes.forEach { e.noteOn(it, 110) }
        val buf = FloatArray(512)
        var peak = 0f
        repeat(80) {
            e.render(buf, buf.size)
            for (v in buf) {
                assertTrue(!v.isNaN(), "NaN from $inst")
                peak = maxOf(peak, abs(v))
            }
        }
        return peak
    }

    @Test
    fun everyInstrumentMakesSoundWithoutClipping() {
        for (inst in Instrument.entries) {
            val single = peakAfterNotes(inst, listOf(60))
            assertTrue(single > 0.05f, "$inst too quiet: $single")
            val chord = peakAfterNotes(inst, listOf(36, 48, 60, 64, 67, 72, 76, 79, 84))
            assertTrue(chord < 1.0f, "$inst clips on big chords: $chord")
        }
    }

    @Test
    fun noteOffReleasesSound() {
        val e = SynthEngine(44100)
        e.noteOn(60, 100)
        val buf = FloatArray(512)
        repeat(10) { e.render(buf, buf.size) }
        e.noteOff(60)
        repeat(200) { e.render(buf, buf.size) }
        // After ~2.3 s and a released damper only the reverb tail can remain.
        assertTrue(buf.maxOf { abs(it) } < 0.02f)
    }
}
