package com.pianoquest.app.theory

import com.pianoquest.app.theory.Interval.Companion.A2
import com.pianoquest.app.theory.Interval.Companion.A4
import com.pianoquest.app.theory.Interval.Companion.A5
import com.pianoquest.app.theory.Interval.Companion.M2
import com.pianoquest.app.theory.Interval.Companion.M3
import com.pianoquest.app.theory.Interval.Companion.M6
import com.pianoquest.app.theory.Interval.Companion.M7
import com.pianoquest.app.theory.Interval.Companion.P1
import com.pianoquest.app.theory.Interval.Companion.P4
import com.pianoquest.app.theory.Interval.Companion.P5
import com.pianoquest.app.theory.Interval.Companion.d5
import com.pianoquest.app.theory.Interval.Companion.m2
import com.pianoquest.app.theory.Interval.Companion.m3
import com.pianoquest.app.theory.Interval.Companion.m6
import com.pianoquest.app.theory.Interval.Companion.m7

enum class ScaleType(
    val displayName: String,
    val intervals: List<Interval>,
    val family: String,
    val description: String,
) {
    MAJOR("Major (Ionian)", listOf(P1, M2, M3, P4, P5, M6, M7), "Diatonic",
        "The bright 'do-re-mi' scale. Pattern: W W H W W W H."),
    NATURAL_MINOR("Natural minor (Aeolian)", listOf(P1, M2, m3, P4, P5, m6, m7), "Diatonic",
        "The sad-sounding relative of the major scale. Pattern: W H W W H W W."),
    HARMONIC_MINOR("Harmonic minor", listOf(P1, M2, m3, P4, P5, m6, M7), "Minor",
        "Natural minor with a raised 7th, giving a strong leading tone and an exotic augmented 2nd."),
    MELODIC_MINOR("Melodic minor (ascending)", listOf(P1, M2, m3, P4, P5, M6, M7), "Minor",
        "Raised 6th and 7th going up; classical players return to natural minor coming down. Jazz uses it both ways."),
    DORIAN("Dorian", listOf(P1, M2, m3, P4, P5, M6, m7), "Mode",
        "Minor with a bright raised 6th. Think 'Scarborough Fair', 'So What', funk and soul vamps."),
    PHRYGIAN("Phrygian", listOf(P1, m2, m3, P4, P5, m6, m7), "Mode",
        "Minor with a dark flat 2nd. Spanish, flamenco and metal flavour."),
    LYDIAN("Lydian", listOf(P1, M2, M3, A4, P5, M6, M7), "Mode",
        "Major with a dreamy raised 4th. Film scores love it."),
    MIXOLYDIAN("Mixolydian", listOf(P1, M2, M3, P4, P5, M6, m7), "Mode",
        "Major with a flat 7th. Rock, blues and dominant-7th chords."),
    LOCRIAN("Locrian", listOf(P1, m2, m3, P4, d5, m6, m7), "Mode",
        "The unstable mode with a flat 2nd and flat 5th; fits the half-diminished chord."),
    MAJOR_PENTATONIC("Major pentatonic", listOf(P1, M2, M3, P5, M6), "Pentatonic",
        "Five notes, no half steps: impossible to sound wrong. Folk, pop, country."),
    MINOR_PENTATONIC("Minor pentatonic", listOf(P1, m3, P4, P5, m7), "Pentatonic",
        "The backbone of rock and blues soloing."),
    BLUES("Blues scale", listOf(P1, m3, P4, d5, P5, m7), "Blues",
        "Minor pentatonic plus the 'blue note' (flat 5)."),
    MAJOR_BLUES("Major blues", listOf(P1, M2, m3, M3, P5, M6), "Blues",
        "Major pentatonic plus the flat 3rd that slides into the major 3rd. Gospel and country piano."),
    WHOLE_TONE("Whole tone", listOf(P1, M2, M3, A4, A5, Interval(5, 10, "A6", "Augmented 6th")), "Symmetric",
        "Only whole steps. Dreamy, floating - Debussy's favourite."),
    DIMINISHED_HW("Diminished (half-whole)", listOf(P1, m2, m3, M3, A4, P5, M6, m7), "Symmetric",
        "Alternating half and whole steps. Fits dominant 7(b9) chords in jazz."),
    DIMINISHED_WH("Diminished (whole-half)", listOf(P1, M2, m3, P4, d5, m6, M6, M7), "Symmetric",
        "Alternating whole and half steps. Fits diminished 7th chords."),
    CHROMATIC("Chromatic", listOf(P1, m2, M2, m3, M3, P4, A4, P5, m6, M6, m7, M7), "Symmetric",
        "All twelve notes, each a half step apart."),
    BEBOP_DOMINANT("Bebop dominant", listOf(P1, M2, M3, P4, P5, M6, m7, M7), "Jazz",
        "Mixolydian with an added major 7th passing tone so chord tones land on the beat."),
    PHRYGIAN_DOMINANT("Phrygian dominant", listOf(P1, m2, M3, P4, P5, m6, m7), "Exotic",
        "5th mode of harmonic minor. Middle-Eastern and flamenco sound."),
    LYDIAN_DOMINANT("Lydian dominant", listOf(P1, M2, M3, A4, P5, M6, m7), "Jazz",
        "4th mode of melodic minor. Fits 7(#11) chords."),
    ALTERED("Altered (super-Locrian)", listOf(P1, m2, A2, M3, A4, A5, m7), "Jazz",
        "7th mode of melodic minor. Every tension altered: b9, #9, #11, b13. For 7alt chords."),
    HUNGARIAN_MINOR("Hungarian minor", listOf(P1, M2, m3, A4, P5, m6, M7), "Exotic",
        "Harmonic minor with a raised 4th: two augmented 2nds. Liszt's Hungarian Rhapsodies."),
    JAPANESE_IN("Japanese In (Miyako-bushi)", listOf(P1, m2, P4, P5, m6), "Exotic",
        "A pentatonic scale with half steps, used in traditional Japanese music."),
    ;

    val semitones: List<Int> get() = intervals.map { it.semitones }

    fun pitches(tonic: Pitch, ascendingOctave: Boolean = true): List<Pitch> {
        val base = intervals.map { tonic.transpose(it) }
        return if (ascendingOctave) base + tonic.withOctave(tonic.octave + 1) else base
    }

    companion object {
        val MODES = listOf(MAJOR, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, NATURAL_MINOR, LOCRIAN)
    }
}

