package com.pianoquest.app.theory

import kotlin.math.pow

/** Letter indices 0..6 = C D E F G A B. */
val LETTERS = charArrayOf('C', 'D', 'E', 'F', 'G', 'A', 'B')
val LETTER_SEMITONES = intArrayOf(0, 2, 4, 5, 7, 9, 11)

/** A spelled pitch: a letter, an accidental (-2..2) and an octave in scientific pitch notation (C4 = middle C). */
data class Pitch(val letter: Int, val accidental: Int, val octave: Int) {

    val midi: Int get() = (octave + 1) * 12 + LETTER_SEMITONES[letter] + accidental

    /** Steps from C0 counted on white keys only; used to place notes on the staff. */
    val diatonicIndex: Int get() = octave * 7 + letter

    val pitchClass: Int get() = ((midi % 12) + 12) % 12

    /** Name without octave using ASCII accidentals, e.g. "F#", "Bb". */
    val ascii: String get() = "${LETTERS[letter]}${accidentalAscii(accidental)}"

    /** Name without octave using music symbols, e.g. "F♯". */
    val pretty: String get() = "${LETTERS[letter]}${accidentalPretty(accidental)}"

    /** Full name including octave, e.g. "C4", "F#5". */
    val name: String get() = "$ascii$octave"

    val prettyName: String get() = "$pretty$octave"

    fun withOctave(o: Int) = copy(octave = o)

    /** Transposes by an interval, keeping correct letter spelling (C + M3 = E, not F♭). */
    fun transpose(interval: Interval, down: Boolean = false): Pitch {
        val steps = if (down) -interval.steps else interval.steps
        val semis = if (down) -interval.semitones else interval.semitones
        val newDiatonic = diatonicIndex + steps
        val newLetter = newDiatonic.mod(7)
        val newOctave = newDiatonic.floorDiv(7)
        val natural = (newOctave + 1) * 12 + LETTER_SEMITONES[newLetter]
        return Pitch(newLetter, midi + semis - natural, newOctave)
    }

    /** Enharmonic respelling to the simplest name (used when spelling gets ugly, e.g. double flats). */
    fun simplified(preferFlats: Boolean = accidental < 0): Pitch =
        if (accidental in -1..1) this else fromMidi(midi, preferFlats)

    override fun toString() = name

    companion object {
        private val regex = Regex("^([A-Ga-g])(##|bb|x|#|b|♯|♭|𝄪|𝄫|n)?(-?\\d+)?$")

        /** Parses names like "C4", "F#3", "Bb5", "Ebb2". Octave defaults to 4. */
        fun parse(text: String): Pitch {
            val m = regex.matchEntire(text.trim()) ?: throw IllegalArgumentException("Bad pitch: '$text'")
            val letter = LETTERS.indexOf(m.groupValues[1].uppercase()[0])
            val acc = when (m.groupValues[2]) {
                "#", "♯" -> 1
                "##", "x", "𝄪" -> 2
                "b", "♭" -> -1
                "bb", "𝄫" -> -2
                else -> 0
            }
            val oct = m.groupValues[3].ifEmpty { "4" }.toInt()
            return Pitch(letter, acc, oct)
        }

        fun parseOrNull(text: String): Pitch? = runCatching { parse(text) }.getOrNull()

        private val SHARP_SPELL = intArrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)
        private val FLAT_SPELL = intArrayOf(0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6)

        fun fromMidi(midi: Int, preferFlats: Boolean = false): Pitch {
            val pc = midi.mod(12)
            val octave = midi.floorDiv(12) - 1
            val letter = if (preferFlats) FLAT_SPELL[pc] else SHARP_SPELL[pc]
            return Pitch(letter, pc - LETTER_SEMITONES[letter], octave)
        }

        /** Spells a midi note following a key signature: sharps keys spell with sharps, flat keys with flats. */
        fun fromMidiInKey(midi: Int, key: Key?): Pitch {
            if (key == null) return fromMidi(midi)
            val pc = midi.mod(12)
            key.scalePitches().firstOrNull { it.pitchClass == pc }?.let { sp ->
                val oct = (midi - (LETTER_SEMITONES[sp.letter] + sp.accidental)) / 12 - 1
                return Pitch(sp.letter, sp.accidental, oct)
            }
            return fromMidi(midi, key.signature < 0)
        }
    }
}

fun accidentalAscii(a: Int) = when (a) {
    -2 -> "bb"; -1 -> "b"; 1 -> "#"; 2 -> "x"; else -> ""
}

fun accidentalPretty(a: Int) = when (a) {
    -2 -> "𝄫"; -1 -> "♭"; 1 -> "♯"; 2 -> "𝄪"; else -> ""
}

fun midiToFrequency(midi: Double): Double = 440.0 * 2.0.pow((midi - 69.0) / 12.0)

fun isBlackKey(midi: Int): Boolean = when (midi.mod(12)) {
    1, 3, 6, 8, 10 -> true
    else -> false
}

fun midiName(midi: Int, preferFlats: Boolean = false) = Pitch.fromMidi(midi, preferFlats).prettyName

/** Pitch-class names for display ("C", "C♯/D♭" ...). */
fun pitchClassLabel(pc: Int, style: NoteLabelStyle = NoteLabelStyle.LETTERS): String {
    val p = Pitch.fromMidi(60 + pc)
    return when (style) {
        NoteLabelStyle.LETTERS -> p.pretty
        NoteLabelStyle.SOLFEGE -> SOLFEGE[p.letter] + accidentalPretty(p.accidental)
    }
}

val SOLFEGE = arrayOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Ti")

enum class NoteLabelStyle { LETTERS, SOLFEGE }

/** Helpers for content authoring. */
fun p(name: String) = Pitch.parse(name)
fun m(name: String) = Pitch.parse(name).midi
fun notes(spaceSeparated: String): List<Pitch> =
    spaceSeparated.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { Pitch.parse(it) }
