package com.pianoquest.app.theory

import kotlin.jvm.JvmField

/** An interval measured both in letter steps (for spelling) and semitones (for sound). */
data class Interval(val steps: Int, val semitones: Int, val short: String, val long: String) {
    override fun toString() = short

    companion object {
        @JvmField val P1 = Interval(0, 0, "P1", "Perfect unison")
        @JvmField val m2 = Interval(1, 1, "m2", "Minor 2nd")
        @JvmField val M2 = Interval(1, 2, "M2", "Major 2nd")
        @JvmField val A2 = Interval(1, 3, "A2", "Augmented 2nd")
        @JvmField val m3 = Interval(2, 3, "m3", "Minor 3rd")
        @JvmField val M3 = Interval(2, 4, "M3", "Major 3rd")
        @JvmField val d4 = Interval(3, 4, "d4", "Diminished 4th")
        @JvmField val P4 = Interval(3, 5, "P4", "Perfect 4th")
        @JvmField val A4 = Interval(3, 6, "A4", "Augmented 4th (tritone)")
        @JvmField val d5 = Interval(4, 6, "d5", "Diminished 5th (tritone)")
        @JvmField val P5 = Interval(4, 7, "P5", "Perfect 5th")
        @JvmField val A5 = Interval(4, 8, "A5", "Augmented 5th")
        @JvmField val m6 = Interval(5, 8, "m6", "Minor 6th")
        @JvmField val M6 = Interval(5, 9, "M6", "Major 6th")
        @JvmField val d7 = Interval(6, 9, "d7", "Diminished 7th")
        @JvmField val m7 = Interval(6, 10, "m7", "Minor 7th")
        @JvmField val M7 = Interval(6, 11, "M7", "Major 7th")
        @JvmField val P8 = Interval(7, 12, "P8", "Perfect octave")
        @JvmField val m9 = Interval(8, 13, "m9", "Minor 9th")
        @JvmField val M9 = Interval(8, 14, "M9", "Major 9th")
        @JvmField val A9 = Interval(8, 15, "A9", "Augmented 9th")
        @JvmField val P11 = Interval(10, 17, "P11", "Perfect 11th")
        @JvmField val A11 = Interval(10, 18, "A11", "Augmented 11th")
        @JvmField val m13 = Interval(12, 20, "m13", "Minor 13th")
        @JvmField val M13 = Interval(12, 21, "M13", "Major 13th")

        /** The 13 simple intervals in order of size, one spelling each (used for ear training and quizzes). */
        val SIMPLE = listOf(P1, m2, M2, m3, M3, P4, A4, P5, m6, M6, m7, M7, P8)

        val ALL = listOf(P1, m2, M2, A2, m3, M3, d4, P4, A4, d5, P5, A5, m6, M6, d7, m7, M7, P8, m9, M9, A9, P11, A11, m13, M13)

        fun bySemitones(semis: Int): Interval = SIMPLE.first { it.semitones == semis.coerceIn(0, 12) }

        /** Describes the interval between two spelled pitches (lower first). */
        fun between(a: Pitch, b: Pitch): Interval? {
            val lo = if (a.midi <= b.midi) a else b
            val hi = if (lo === a) b else a
            val steps = hi.diatonicIndex - lo.diatonicIndex
            val semis = hi.midi - lo.midi
            return ALL.firstOrNull { it.steps == steps && it.semitones == semis }
        }
    }
}

/** Plain-language interval names for beginners, indexed by semitones 0..12. */
val INTERVAL_FRIENDLY = listOf(
    "Unison", "Minor 2nd (half step)", "Major 2nd (whole step)", "Minor 3rd", "Major 3rd", "Perfect 4th",
    "Tritone", "Perfect 5th", "Minor 6th", "Major 6th", "Minor 7th", "Major 7th", "Octave",
)

/** Well-known song openings that help you remember how intervals sound. */
val INTERVAL_SONG_HINTS = mapOf(
    1 to "Jaws theme",
    2 to "Happy Birthday (\"Hap-py\")",
    3 to "Greensleeves (first two notes)",
    4 to "When the Saints (\"Oh when\")",
    5 to "Here Comes the Bride / Amazing Grace",
    6 to "The Simpsons (\"The Simp-\")",
    7 to "Twinkle Twinkle (\"Twinkle, twinkle\")",
    8 to "The Entertainer (first leap)",
    9 to "My Bonnie Lies Over the Ocean",
    10 to "Star Trek theme (original series)",
    11 to "Take On Me (chorus leap)",
    12 to "Somewhere Over the Rainbow (\"Some-where\")",
)
