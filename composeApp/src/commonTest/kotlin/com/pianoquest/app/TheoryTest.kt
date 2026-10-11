package com.pianoquest.app

import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.ChordType
import com.pianoquest.app.theory.Interval
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.RomanNumerals
import com.pianoquest.app.theory.ScaleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TheoryTest {
    @Test
    fun pitchParsingAndMidi() {
        assertEquals(60, Pitch.parse("C4").midi)
        assertEquals(69, Pitch.parse("A4").midi)
        assertEquals(61, Pitch.parse("C#4").midi)
        assertEquals(70, Pitch.parse("Bb4").midi)
        assertEquals(60, Pitch.parse("B#3").midi)
        assertEquals(21, Pitch.parse("A0").midi)
        assertEquals("C#4", Pitch.fromMidi(61).name)
        assertEquals("Db4", Pitch.fromMidi(61, preferFlats = true).name)
    }

    @Test
    fun intervalSpelling() {
        assertEquals("E4", Pitch.parse("C4").transpose(Interval.M3).name)
        assertEquals("Eb4", Pitch.parse("C4").transpose(Interval.m3).name)
        assertEquals("F#4", Pitch.parse("B3").transpose(Interval.P5).name)
        assertEquals("A3", Pitch.parse("C4").transpose(Interval.m3, down = true).name)
        assertEquals(Interval.P5, Interval.between(Pitch.parse("C4"), Pitch.parse("G4")))
    }

    @Test
    fun scales() {
        assertEquals("D E F# G A B C# D", ScaleType.MAJOR.pitches(Pitch.parse("D4")).joinToString(" ") { it.ascii })
        assertEquals("F G Ab Bb C Db Eb F", ScaleType.NATURAL_MINOR.pitches(Pitch.parse("F4")).joinToString(" ") { it.ascii })
        assertEquals("A B C D E F G# A", ScaleType.HARMONIC_MINOR.pitches(Pitch.parse("A4")).joinToString(" ") { it.ascii })
    }

    @Test
    fun keys() {
        assertEquals(2, Key.major("D").signature)
        assertEquals(-3, Key.minor("C").signature)
        assertEquals("A", Key.major("C").relative.tonic.ascii)
        assertEquals(listOf("I", "ii", "iii", "IV", "V", "vi", "vii°"), Key.major("C").diatonicTriads().map { it.first })
        assertEquals("F#m", Key.major("D").diatonicTriads()[2].second.symbol)
        assertEquals(listOf("i", "ii°", "III", "iv", "v", "VI", "VII"), Key.minor("A").diatonicTriads().map { it.first })
    }

    @Test
    fun chords() {
        val c = Chord.parse("Bbmaj7")
        assertEquals("Bb D F A", c.pitches().joinToString(" ") { it.ascii })
        assertEquals(ChordType.MIN7B5, Chord.parse("Dm7b5").type)
        assertEquals("E", Chord.parse("C/E").bass?.ascii)
        assertEquals("C#m", Chord.parse("C#m").symbol)
        assertEquals("F#dim7", Chord.parse("F#dim7").symbol)
        assertEquals("Am", Chord.identify(listOf(57, 60, 64))?.symbol)
        assertEquals("C/E", Chord.identify(listOf(52, 55, 60))?.symbol)
        assertEquals("G7", Chord.identify(listOf(55, 59, 62, 65))?.symbol)
        // Notes are spelled as chord tones: the 7th of C9 is B-flat, the 3rd of E is G-sharp.
        val c9 = Chord.identify(listOf(60, 64, 67, 70, 74))!!
        assertEquals(listOf("C4", "E4", "G4", "Bb4", "D5"), listOf(60, 64, 67, 70, 74).map { c9.spell(it).name })
        assertEquals("G#3", Chord.identify(listOf(52, 56, 59))!!.spell(56).name)
    }

    @Test
    fun romanNumerals() {
        assertEquals(listOf("C", "G", "Am", "F"), RomanNumerals.progression("I V vi IV", Key.major("C")).map { it.symbol })
        assertEquals(listOf("Dm7", "G7", "Cmaj7"), RomanNumerals.progression("ii7 V7 Imaj7", Key.major("C")).map { it.symbol })
        assertEquals("D", RomanNumerals.chord("V/V", Key.major("C")).symbol)
        assertEquals("Bb", RomanNumerals.chord("bVII", Key.major("C")).symbol)
        assertEquals("E7", RomanNumerals.chord("V7", Key.minor("A")).symbol)
        assertNotNull(RomanNumerals.chord("vii°", Key.major("C")))
    }
}
