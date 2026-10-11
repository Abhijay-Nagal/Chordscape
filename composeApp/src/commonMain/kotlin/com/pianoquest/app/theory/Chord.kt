package com.pianoquest.app.theory

import com.pianoquest.app.theory.Interval.Companion.A11
import com.pianoquest.app.theory.Interval.Companion.A5
import com.pianoquest.app.theory.Interval.Companion.A9
import com.pianoquest.app.theory.Interval.Companion.M13
import com.pianoquest.app.theory.Interval.Companion.M2
import com.pianoquest.app.theory.Interval.Companion.M3
import com.pianoquest.app.theory.Interval.Companion.M6
import com.pianoquest.app.theory.Interval.Companion.M7
import com.pianoquest.app.theory.Interval.Companion.M9
import com.pianoquest.app.theory.Interval.Companion.P1
import com.pianoquest.app.theory.Interval.Companion.P11
import com.pianoquest.app.theory.Interval.Companion.P4
import com.pianoquest.app.theory.Interval.Companion.P5
import com.pianoquest.app.theory.Interval.Companion.d5
import com.pianoquest.app.theory.Interval.Companion.d7
import com.pianoquest.app.theory.Interval.Companion.m13
import com.pianoquest.app.theory.Interval.Companion.m3
import com.pianoquest.app.theory.Interval.Companion.m7
import com.pianoquest.app.theory.Interval.Companion.m9

enum class ChordType(
    val symbol: String,
    val displayName: String,
    val intervals: List<Interval>,
    val formula: String,
    val category: String,
    val aliases: List<String> = emptyList(),
    val sound: String = "",
) {
    MAJOR("", "Major triad", listOf(P1, M3, P5), "1 3 5", "Triad", listOf("maj", "M"), "Bright, stable, happy."),
    MINOR("m", "Minor triad", listOf(P1, m3, P5), "1 b3 5", "Triad", listOf("min", "-"), "Darker, sad or serious."),
    DIMINISHED("dim", "Diminished triad", listOf(P1, m3, d5), "1 b3 b5", "Triad", listOf("°", "o"), "Tense and unstable; wants to resolve."),
    AUGMENTED("aug", "Augmented triad", listOf(P1, M3, A5), "1 3 #5", "Triad", listOf("+", "#5"), "Dreamy, suspended, mysterious."),
    SUS2("sus2", "Suspended 2nd", listOf(P1, M2, P5), "1 2 5", "Triad", sound = "Open and airy; neither major nor minor."),
    SUS4("sus4", "Suspended 4th", listOf(P1, P4, P5), "1 4 5", "Triad", listOf("sus"), "Yearning; the 4th wants to fall to the 3rd."),
    POWER("5", "Power chord", listOf(P1, P5), "1 5", "Triad", sound = "Hollow and strong. Rock staple."),
    SIX("6", "Major 6th", listOf(P1, M3, P5, M6), "1 3 5 6", "Sixth", sound = "Sweet, vintage, swing-era ending chord."),
    MINOR_SIX("m6", "Minor 6th", listOf(P1, m3, P5, M6), "1 b3 5 6", "Sixth", sound = "Film-noir, bittersweet."),
    SIX_NINE("6/9", "6/9", listOf(P1, M3, P5, M6, M9), "1 3 5 6 9", "Sixth", listOf("69"), "Lush, modern jazz/bossa resting chord."),
    DOM7("7", "Dominant 7th", listOf(P1, M3, P5, m7), "1 3 5 b7", "Seventh", listOf("dom7"), "Bluesy tension that pulls home."),
    MAJ7("maj7", "Major 7th", listOf(P1, M3, P5, M7), "1 3 5 7", "Seventh", listOf("M7", "Δ", "Δ7", "ma7"), "Soft, jazzy, romantic."),
    MIN7("m7", "Minor 7th", listOf(P1, m3, P5, m7), "1 b3 5 b7", "Seventh", listOf("min7", "-7"), "Mellow, smooth, soulful."),
    MIN7B5("m7b5", "Half-diminished 7th", listOf(P1, m3, d5, m7), "1 b3 b5 b7", "Seventh", listOf("ø", "ø7"), "Moody; the ii chord in minor keys."),
    DIM7("dim7", "Diminished 7th", listOf(P1, m3, d5, d7), "1 b3 b5 bb7", "Seventh", listOf("°7", "o7"), "Dramatic, suspenseful (silent-movie villain!)."),
    MIN_MAJ7("mMaj7", "Minor-major 7th", listOf(P1, m3, P5, M7), "1 b3 5 7", "Seventh", listOf("m(maj7)", "mM7", "-Δ7"), "Spy-movie mystery."),
    AUG7("7#5", "Augmented 7th", listOf(P1, M3, A5, m7), "1 3 #5 b7", "Seventh", listOf("aug7", "+7"), "Restless dominant that pushes upward."),
    SEVEN_SUS4("7sus4", "Dominant 7 sus4", listOf(P1, P4, P5, m7), "1 4 5 b7", "Seventh", listOf("7sus"), "Modern, open dominant."),
    ADD9("add9", "Add 9", listOf(P1, M3, P5, M9), "1 3 5 9", "Added", listOf("add2"), "Shimmering pop sound."),
    MIN_ADD9("madd9", "Minor add 9", listOf(P1, m3, P5, M9), "1 b3 5 9", "Added", sound = "Wistful, cinematic."),
    NINE("9", "Dominant 9th", listOf(P1, M3, P5, m7, M9), "1 3 5 b7 9", "Extended", sound = "Funky, soulful dominant."),
    MAJ9("maj9", "Major 9th", listOf(P1, M3, P5, M7, M9), "1 3 5 7 9", "Extended", listOf("M9"), "Lush and dreamy."),
    MIN9("m9", "Minor 9th", listOf(P1, m3, P5, m7, M9), "1 b3 5 b7 9", "Extended", listOf("min9", "-9"), "Neo-soul warmth."),
    ELEVEN("11", "Dominant 11th", listOf(P1, P5, m7, M9, P11), "1 (3) 5 b7 9 11", "Extended", sound = "Gospel and fusion; the 3rd is usually left out."),
    MIN11("m11", "Minor 11th", listOf(P1, m3, P5, m7, M9, P11), "1 b3 5 b7 9 11", "Extended", sound = "Open, modal, quartal."),
    THIRTEEN("13", "Dominant 13th", listOf(P1, M3, m7, M9, M13), "1 3 b7 9 13", "Extended", sound = "Big-band brass richness."),
    MAJ13("maj13", "Major 13th", listOf(P1, M3, M7, M9, M13), "1 3 7 9 13", "Extended", sound = "Expansive, sophisticated."),
    MIN13("m13", "Minor 13th", listOf(P1, m3, m7, M9, M13), "1 b3 b7 9 13", "Extended", sound = "Smooth jazz minor."),
    SEVEN_FLAT9("7b9", "Dominant 7 flat 9", listOf(P1, M3, P5, m7, m9), "1 3 5 b7 b9", "Altered", sound = "Dark tension resolving to minor."),
    SEVEN_SHARP9("7#9", "Dominant 7 sharp 9", listOf(P1, M3, P5, m7, A9), "1 3 5 b7 #9", "Altered", sound = "The 'Hendrix chord': gritty and bluesy."),
    SEVEN_SHARP11("7#11", "Dominant 7 sharp 11", listOf(P1, M3, P5, m7, A11), "1 3 5 b7 #11", "Altered", sound = "Lydian-dominant shimmer."),
    SEVEN_FLAT5("7b5", "Dominant 7 flat 5", listOf(P1, M3, d5, m7), "1 3 b5 b7", "Altered", sound = "Whole-tone tension, French-sounding."),
    SEVEN_FLAT13("7b13", "Dominant 7 flat 13", listOf(P1, M3, m7, m13), "1 3 b7 b13", "Altered", sound = "Bittersweet dominant toward minor."),
    ALT("7alt", "Altered dominant", listOf(P1, M3, m7, A9, m13), "1 3 b7 #9 b13", "Altered", listOf("alt"), "Maximum tension from the altered scale."),
    ;

    val semitones: List<Int> get() = intervals.map { it.semitones }

    companion object {
        /** Lookup by any accepted spelling of the suffix, longest first so "m7b5" wins over "m7". */
        val bySuffix: List<Pair<String, ChordType>> = entries
            .flatMap { t -> (listOf(t.symbol) + t.aliases).map { it to t } }
            .sortedByDescending { it.first.length }

        val COMMON = listOf(MAJOR, MINOR, DIMINISHED, AUGMENTED, SUS2, SUS4, DOM7, MAJ7, MIN7, MIN7B5, DIM7)
    }
}

/** A chord: root + type, plus an optional different bass note (slash chord, e.g. C/E). */
data class Chord(val root: Pitch, val type: ChordType, val bass: Pitch? = null) {

    val symbol: String get() = root.ascii + type.symbol + (bass?.let { "/${it.ascii}" } ?: "")
    val prettySymbol: String get() = root.pretty + type.symbol.replace("b", "♭").replace("#", "♯") + (bass?.let { "/${it.pretty}" } ?: "")

    /** Chord tones in close position starting from the root in [octave]. */
    fun pitches(octave: Int = 4, inversion: Int = 0): List<Pitch> {
        val base = type.intervals.map { root.withOctave(octave).transpose(it) }
        var voiced = base
        repeat(inversion.coerceIn(0, base.size - 1)) {
            voiced = voiced.drop(1) + voiced.first().withOctave(voiced.first().octave + 1)
        }
        return voiced
    }

    fun midis(octave: Int = 4, inversion: Int = 0): List<Int> = pitches(octave, inversion).map { it.midi }

    /** A comfortable left-hand bass note (octave 2/3) for accompaniment. */
    fun bassMidi(): Int {
        val b = bass ?: root
        var midi = b.withOctave(3).midi
        while (midi > 52) midi -= 12
        while (midi < 40) midi += 12
        return midi
    }

    /** Close-position right-hand voicing kept near middle C, used by accompaniment generators. */
    fun compVoicing(center: Int = 62): List<Int> {
        val tones = type.intervals.filter { it.semitones < 12 || type.intervals.size <= 3 }.map { root.transpose(it).pitchClass }.distinct()
        // Choose the inversion whose average is closest to the center.
        var best: List<Int> = emptyList()
        var bestDist = Int.MAX_VALUE
        for (start in 48..72) {
            if (start.mod(12) !in tones) continue
            val voicing = mutableListOf(start)
            for (pc in tones.drop(tones.indexOf(start.mod(12)) + 1) + tones.take(tones.indexOf(start.mod(12)))) {
                var n = voicing.last() + 1
                while (n.mod(12) != pc) n++
                voicing += n
            }
            val dist = kotlin.math.abs(voicing.sum() / voicing.size - center)
            if (dist < bestDist) { bestDist = dist; best = voicing }
        }
        return best
    }

    /** Spells [midi] as the chord tone it is (B♭4 in C9, not A♯4); notes outside the chord get the default spelling. */
    fun spell(midi: Int): Pitch {
        val tone = (listOf(root) + type.intervals.map { root.transpose(it) }).firstOrNull { it.midi.mod(12) == midi.mod(12) }
            ?.let { if (kotlin.math.abs(it.accidental) > 1) it.simplified() else it }
            ?: return Pitch.fromMidi(midi)
        return tone.copy(octave = tone.octave + (midi - tone.midi).floorDiv(12))
    }

    override fun toString() = symbol

    companion object {
        private val rootRegex = Regex("^([A-Ga-g])(#|b|♯|♭)?")

        fun parse(text: String): Chord {
            val t = text.trim()
            val slash = t.lastIndexOf('/')
            val (main, bassText) = if (slash > 0 && t.substring(slash + 1).let { it.isNotEmpty() && it[0].uppercaseChar() in "ABCDEFG" })
                t.substring(0, slash) to t.substring(slash + 1) else t to null
            val rm = rootRegex.find(main) ?: throw IllegalArgumentException("Bad chord '$text'")
            val root = Pitch.parse(rm.value.replaceFirstChar { it.uppercase() } + "4")
            val suffix = main.substring(rm.value.length)
            val type = if (suffix.isEmpty()) ChordType.MAJOR else
                ChordType.bySuffix.firstOrNull { it.first == suffix }?.second
                    ?: throw IllegalArgumentException("Unknown chord suffix '$suffix' in '$text'")
            val bass = bassText?.let { Pitch.parse(it + "3") }
            return Chord(root, type, bass)
        }

        fun parseOrNull(text: String) = runCatching { parse(text) }.getOrNull()

        /** Identifies a chord from a set of midi notes (any voicing). Returns the best match or null. */
        fun identify(midis: Collection<Int>): Chord? {
            if (midis.size < 2) return null
            val pcs = midis.map { it.mod(12) }.toSet()
            val lowest = midis.min().mod(12)
            val candidates = mutableListOf<Pair<Chord, Int>>()
            for (rootPc in pcs) {
                for (type in ChordType.entries) {
                    val chordPcs = type.semitones.map { (rootPc + it).mod(12) }.toSet()
                    if (chordPcs == pcs) {
                        val root = Pitch.fromMidi(60 + rootPc, preferFlats = rootPc in listOf(1, 3, 8, 10))
                        val bass = if (lowest != rootPc) Pitch.fromMidi(48 + lowest, preferFlats = lowest in listOf(1, 3, 8, 10)) else null
                        val score = (if (bass == null) 0 else 10) + type.ordinal
                        candidates += Chord(root, type, bass) to score
                    }
                }
            }
            return candidates.minByOrNull { it.second }?.first
        }
    }
}
