package com.pianoquest.app.theory

enum class Mode(val label: String) { MAJOR("major"), MINOR("minor") }

/** A key such as G major or E minor. */
data class Key(val tonic: Pitch, val mode: Mode) {

    val name: String get() = "${tonic.pretty} ${mode.label}"
    val shortName: String get() = tonic.ascii + if (mode == Mode.MINOR) "m" else ""

    /** Number of sharps (positive) or flats (negative) in the key signature. */
    val signature: Int get() {
        val major = if (mode == Mode.MAJOR) tonic else tonic.transpose(Interval.m3)
        return MAJOR_SIGNATURES[major.ascii] ?: 0
    }

    val scale: ScaleType get() = if (mode == Mode.MAJOR) ScaleType.MAJOR else ScaleType.NATURAL_MINOR

    fun scalePitches(): List<Pitch> = scale.pitches(tonic.withOctave(4), ascendingOctave = false)

    val relative: Key get() = if (mode == Mode.MAJOR) Key(tonic.transpose(Interval.m3, down = true).withOctave(4), Mode.MINOR)
        else Key(tonic.transpose(Interval.m3).withOctave(4), Mode.MAJOR)

    val parallel: Key get() = Key(tonic, if (mode == Mode.MAJOR) Mode.MINOR else Mode.MAJOR)

    /** The seven diatonic triads with Roman numerals. */
    fun diatonicTriads(): List<Pair<String, Chord>> = diatonicChords(sevenths = false)

    fun diatonicChords(sevenths: Boolean): List<Pair<String, Chord>> {
        val sp = scalePitches()
        return (0 until 7).map { i ->
            val root = sp[i]
            val third = sp[(i + 2) % 7]
            val fifth = sp[(i + 4) % 7]
            val seventh = sp[(i + 6) % 7]
            val t3 = (third.pitchClass - root.pitchClass).mod(12)
            val t5 = (fifth.pitchClass - root.pitchClass).mod(12)
            val t7 = (seventh.pitchClass - root.pitchClass).mod(12)
            val triad = when {
                t3 == 4 && t5 == 7 -> ChordType.MAJOR
                t3 == 3 && t5 == 7 -> ChordType.MINOR
                t3 == 3 && t5 == 6 -> ChordType.DIMINISHED
                else -> ChordType.AUGMENTED
            }
            val type = if (!sevenths) triad else when (triad) {
                ChordType.MAJOR -> if (t7 == 11) ChordType.MAJ7 else ChordType.DOM7
                ChordType.MINOR -> if (t7 == 10) ChordType.MIN7 else ChordType.MIN_MAJ7
                ChordType.DIMINISHED -> if (t7 == 10) ChordType.MIN7B5 else ChordType.DIM7
                else -> ChordType.AUG7
            }
            val numeral = ROMAN[i].let { if (triad == ChordType.MAJOR || triad == ChordType.AUGMENTED) it else it.lowercase() } +
                when (type) {
                    ChordType.DIMINISHED -> "°"
                    ChordType.MAJ7 -> "maj7"
                    ChordType.DOM7, ChordType.MIN7 -> "7"
                    ChordType.MIN7B5 -> "ø7"
                    else -> ""
                }
            numeral to Chord(root, type)
        }
    }

    /** Accidentals shown in the key signature, in order (e.g. F#, C# for D major). */
    fun signatureAccidentals(): List<Pitch> = signatureAccidentals(signature)

    companion object {
        val ROMAN = listOf("I", "II", "III", "IV", "V", "VI", "VII")

        val MAJOR_SIGNATURES = mapOf(
            "C" to 0, "G" to 1, "D" to 2, "A" to 3, "E" to 4, "B" to 5, "F#" to 6, "C#" to 7,
            "F" to -1, "Bb" to -2, "Eb" to -3, "Ab" to -4, "Db" to -5, "Gb" to -6, "Cb" to -7,
        )

        /** Circle of fifths order (clockwise from C) for major keys and their relative minors. */
        val CIRCLE_MAJOR = listOf("C", "G", "D", "A", "E", "B", "F#", "Db", "Ab", "Eb", "Bb", "F")
        val CIRCLE_MINOR = listOf("A", "E", "B", "F#", "C#", "G#", "D#", "Bb", "F", "C", "G", "D")

        private val SHARP_ORDER = listOf("F", "C", "G", "D", "A", "E", "B")
        private val FLAT_ORDER = listOf("B", "E", "A", "D", "G", "C", "F")

        fun signatureAccidentals(sig: Int): List<Pitch> = when {
            sig > 0 -> SHARP_ORDER.take(sig).map { Pitch.parse("$it#4") }
            sig < 0 -> FLAT_ORDER.take(-sig).map { Pitch.parse("${it}b4") }
            else -> emptyList()
        }

        /** Parses "C", "Am", "F# major", "Bb minor", "Ebm". */
        fun parse(text: String): Key {
            val t = text.trim()
            val minor = t.endsWith("m") || t.contains("minor", ignoreCase = true)
            val tonicText = t.removeSuffix("m").replace(Regex("(?i)\\s*(major|minor)"), "").trim()
            return Key(Pitch.parse(tonicText + "4"), if (minor) Mode.MINOR else Mode.MAJOR)
        }

        fun major(name: String) = Key(Pitch.parse(name + "4"), Mode.MAJOR)
        fun minor(name: String) = Key(Pitch.parse(name + "4"), Mode.MINOR)

        val ALL_MAJOR: List<Key> get() = CIRCLE_MAJOR.map { major(it) }
        val ALL_MINOR: List<Key> get() = CIRCLE_MINOR.map { minor(it) }
    }
}

/**
 * Roman-numeral progressions such as "I V vi IV" or "ii7 V7 Imaj7".
 * Upper case = major quality, lower case = minor; suffixes ° ø + 7 maj7 sus4 etc.
 * Accidental prefixes (bVII, #iv°) are relative to the key's scale. Secondary chords: "V/V", "V7/ii".
 */
object RomanNumerals {
    private val regex = Regex("^(b|#|♭|♯)?(VII|VI|IV|V|III|II|I|vii|vi|iv|v|iii|ii|i)(.*)$")

    fun chord(numeral: String, key: Key): Chord {
        val parts = numeral.split("/")
        if (parts.size == 2) {
            val target = chord(parts[1], key)
            val targetKey = Key(target.root, if (target.type == ChordType.MINOR || target.type == ChordType.MIN7) Mode.MINOR else Mode.MAJOR)
            // Secondary chords are built on the target's major-scale degrees (V/x = the dominant of x).
            return chord(parts[0], Key(targetKey.tonic, Mode.MAJOR))
        }
        val m = regex.matchEntire(numeral.trim()) ?: throw IllegalArgumentException("Bad numeral '$numeral'")
        val acc = when (m.groupValues[1]) { "b", "♭" -> -1; "#", "♯" -> 1; else -> 0 }
        val rn = m.groupValues[2]
        val upper = rn[0].isUpperCase()
        val degree = Key.ROMAN.indexOf(rn.uppercase())
        var root = key.scalePitches()[degree]
        if (acc != 0) root = Pitch(root.letter, root.accidental + acc, root.octave)
        root = root.simplified()
        val suffix = m.groupValues[3]
        val type = when (suffix) {
            "" -> if (upper) ChordType.MAJOR else ChordType.MINOR
            "°", "o", "dim" -> ChordType.DIMINISHED
            "°7", "o7", "dim7" -> ChordType.DIM7
            "ø", "ø7", "m7b5" -> ChordType.MIN7B5
            "+", "aug" -> ChordType.AUGMENTED
            "7" -> if (upper) ChordType.DOM7 else ChordType.MIN7
            "maj7", "M7", "Δ7" -> if (upper) ChordType.MAJ7 else ChordType.MIN_MAJ7
            "9" -> if (upper) ChordType.NINE else ChordType.MIN9
            "maj9" -> ChordType.MAJ9
            "6" -> if (upper) ChordType.SIX else ChordType.MINOR_SIX
            "sus4", "sus" -> ChordType.SUS4
            "sus2" -> ChordType.SUS2
            "7sus4" -> ChordType.SEVEN_SUS4
            "add9" -> if (upper) ChordType.ADD9 else ChordType.MIN_ADD9
            "13" -> ChordType.THIRTEEN
            "7b9" -> ChordType.SEVEN_FLAT9
            "7alt" -> ChordType.ALT
            else -> ChordType.bySuffix.firstOrNull { it.first == suffix }?.second
                ?: throw IllegalArgumentException("Unknown numeral suffix '$suffix'")
        }
        return Chord(root, type)
    }

    fun progression(numerals: String, key: Key): List<Chord> =
        numerals.trim().split(Regex("[\\s\\-–]+")).filter { it.isNotEmpty() }.map { chord(it, key) }
}
