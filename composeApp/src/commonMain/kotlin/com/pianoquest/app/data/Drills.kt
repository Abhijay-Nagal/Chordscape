package com.pianoquest.app.data

import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.ChordType
import com.pianoquest.app.theory.INTERVAL_FRIENDLY
import com.pianoquest.app.theory.Interval
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Mode
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.RomanNumerals
import com.pianoquest.app.theory.ScaleType
import kotlin.random.Random

/** Expands [Step.Drill] into concrete, freshly randomised questions. */
object Drills {

    fun expand(d: Step.Drill, rnd: Random = Random.Default): List<Step> = List(d.count) { i -> one(d, rnd, i) }

    private fun <T> pickDistinct(rnd: Random, correct: T, pool: List<T>, n: Int = 4): List<T> =
        (listOf(correct) + pool.filter { it != correct }.distinct().shuffled(rnd).take(n - 1))

    private val naturalsTreble1 = "E4 F4 G4 A4 B4 C5 D5 E5 F5".split(" ")
    private val naturalsTreble2 = "A3 B3 C4 D4 E4 F4 G4 A4 B4 C5 D5 E5 F5 G5 A5 B5 C6".split(" ")
    private val naturalsBass1 = "G2 A2 B2 C3 D3 E3 F3 G3 A3".split(" ")
    private val naturalsBass2 = "C2 D2 E2 F2 G2 A2 B2 C3 D3 E3 F3 G3 A3 B3 C4 D4 E4".split(" ")
    private val letters = listOf("C", "D", "E", "F", "G", "A", "B")

    private fun one(d: Step.Drill, rnd: Random, index: Int): Step = when (d.kind) {
        DrillKind.NAME_TREBLE -> nameNote(rnd, Clef.TREBLE, if (d.level >= 2) naturalsTreble2 else naturalsTreble1, d.level >= 3)
        DrillKind.NAME_BASS -> nameNote(rnd, Clef.BASS, if (d.level >= 2) naturalsBass2 else naturalsBass1, d.level >= 3)
        DrillKind.NAME_GRAND -> if (rnd.nextBoolean()) nameNote(rnd, Clef.TREBLE, if (d.level >= 2) naturalsTreble2 else naturalsTreble1, false, grand = true)
            else nameNote(rnd, Clef.BASS, if (d.level >= 2) naturalsBass2 else naturalsBass1, false, grand = true)
        DrillKind.FIND_KEY -> {
            val names = if (d.level >= 2) listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B") else letters
            val n = names[rnd.nextInt(names.size)]
            val p = Pitch.parse(n + "4")
            Step.TapKeys("Find ${p.pretty} - any octave.", listOf(p.name), anyOctave = true, from = "C3", to = "B5")
        }
        DrillKind.NAME_KEY -> {
            val pool = if (d.level >= 2) (60..71).toList() else listOf(60, 62, 64, 65, 67, 69, 71)
            val m = pool[rnd.nextInt(pool.size)]
            val p = Pitch.fromMidi(m, preferFlats = rnd.nextBoolean())
            val label = p.pretty + if (p.accidental != 0) " / " + Pitch.fromMidi(m, preferFlats = p.accidental > 0).pretty else ""
            val wrong = pool.filter { it != m }.map { mm -> Pitch.fromMidi(mm).pretty }
            Step.Quiz("Which note is highlighted?", pickDistinct(rnd, label, wrong),
                visual = Visual.Keyboard("C4", "B4", listOf(p.name), labels = KeyLabels.NONE))
        }
        DrillKind.FIND_STAFF_NOTE -> {
            val clef = if (d.level >= 2 && rnd.nextBoolean()) Clef.BASS else Clef.TREBLE
            val pool = if (clef == Clef.TREBLE) "C4 D4 E4 F4 G4 A4 B4 C5 D5 E5".split(" ") else "C3 D3 E3 F3 G3 A3 B3 C4".split(" ")
            val n = pool[rnd.nextInt(pool.size)]
            val (from, to) = if (clef == Clef.TREBLE) "C4" to "E5" else "C3" to "C4"
            Step.TapKeys("Play the note shown on the staff (exact octave).", listOf(n), anyOctave = false, from = from, to = to, staff = clef,
                explanation = "That note is ${Pitch.parse(n).prettyName}.")
        }
        DrillKind.INTERVAL_STAFF -> {
            val pool = when (d.level) {
                1 -> listOf(Interval.M2, Interval.M3, Interval.P4, Interval.P5, Interval.P8)
                2 -> listOf(Interval.m2, Interval.M2, Interval.m3, Interval.M3, Interval.P4, Interval.P5, Interval.M6, Interval.m6, Interval.P8)
                else -> Interval.SIMPLE.drop(1)
            }
            val iv = pool[rnd.nextInt(pool.size)]
            val roots = listOf("C4", "D4", "E4", "F4", "G4", "A4")
            val lo = Pitch.parse(roots[rnd.nextInt(roots.size)])
            val hi = lo.transpose(iv).simplified()
            val harmonic = rnd.nextBoolean()
            val notes = if (harmonic) "${lo.name}+${hi.name}:w" else "${lo.name}:h ${hi.name}:h"
            Step.Quiz("Name this interval.", pickDistinct(rnd, iv.long, pool.map { it.long }),
                visual = Visual.Staff(Clef.TREBLE, notes), play = notes, explanation = "${lo.pretty} to ${hi.pretty}: ${iv.semitones} half steps.")
        }
        DrillKind.INTERVAL_EAR -> {
            val pool = when (d.level) {
                1 -> listOf(2, 4, 7, 12)
                2 -> listOf(2, 3, 4, 5, 7, 9, 12)
                else -> (1..12).toList()
            }
            val s = pool[rnd.nextInt(pool.size)]
            val root = 55 + rnd.nextInt(10)
            val a = Pitch.fromMidi(root)
            val b = Pitch.fromMidi(root + s)
            val play = "${a.name}:h ${b.name}:h ${a.name}+${b.name}:w"
            Step.Quiz("Listen - which interval is this?", pickDistinct(rnd, INTERVAL_FRIENDLY[s], pool.map { INTERVAL_FRIENDLY[it] }),
                play = play, explanation = com.pianoquest.app.theory.INTERVAL_SONG_HINTS[s]?.let { "Think: $it" } ?: "")
        }
        DrillKind.CHORD_QUALITY_EAR -> {
            val types = if (d.level <= 1) listOf(ChordType.MAJOR, ChordType.MINOR)
                else listOf(ChordType.MAJOR, ChordType.MINOR, ChordType.DIMINISHED, ChordType.AUGMENTED)
            val t = types[rnd.nextInt(types.size)]
            val root = Pitch.fromMidi(55 + rnd.nextInt(9))
            val ch = Chord(root, t)
            val notes = ch.pitches(root.octave).map { it.simplified() }
            val play = notes.joinToString(" ") { "${it.name}:e" } + " " + notes.joinToString("+") { it.name } + ":w"
            Step.Quiz("Listen - what quality is this chord?", pickDistinct(rnd, t.displayName.removeSuffix(" triad"), types.map { it.displayName.removeSuffix(" triad") }),
                play = play, explanation = t.sound)
        }
        DrillKind.SEVENTH_EAR -> {
            val types = listOf(ChordType.MAJ7, ChordType.DOM7, ChordType.MIN7, ChordType.MIN7B5, ChordType.DIM7)
            val t = types[rnd.nextInt(types.size)]
            val root = Pitch.fromMidi(50 + rnd.nextInt(8))
            val notes = Chord(root, t).pitches(root.octave).map { it.simplified() }
            val play = notes.joinToString("+") { it.name } + ":w"
            Step.Quiz("Listen - which seventh chord?", pickDistinct(rnd, t.displayName, types.map { it.displayName }), play = play, explanation = t.sound)
        }
        DrillKind.CHORD_NAME -> {
            val types = if (d.level <= 1) listOf(ChordType.MAJOR, ChordType.MINOR)
                else listOf(ChordType.MAJOR, ChordType.MINOR, ChordType.DIMINISHED, ChordType.DOM7, ChordType.MAJ7, ChordType.MIN7)
            val roots = listOf("C", "D", "E", "F", "G", "A", "Bb", "Eb")
            val t = types[rnd.nextInt(types.size)]
            val root = Pitch.parse(roots[rnd.nextInt(roots.size)] + "4")
            val ch = Chord(root, t)
            val wrongs = buildList {
                types.filter { it != t }.forEach { add(Chord(root, it).prettySymbol) }
                add(Chord(root.transpose(Interval.M2).simplified(), t).prettySymbol)
                add(Chord(root.transpose(Interval.m3, down = true).simplified(), t).prettySymbol)
            }
            Step.Quiz("Which chord is shown on the keyboard?", pickDistinct(rnd, ch.prettySymbol, wrongs),
                visual = Visual.Keyboard("C4", "B5", ch.pitches(4).map { it.name }, KeyLabels.HIGHLIGHTED),
                play = ch.pitches(4).joinToString("+") { it.name } + ":w")
        }
        DrillKind.PLAY_CHORD -> {
            val types = if (d.level <= 1) listOf(ChordType.MAJOR, ChordType.MINOR) else listOf(ChordType.MAJOR, ChordType.MINOR, ChordType.DIMINISHED, ChordType.DOM7, ChordType.MAJ7, ChordType.MIN7)
            val roots = if (d.level <= 1) listOf("C", "D", "E", "F", "G", "A") else listOf("C", "D", "E", "F", "G", "A", "B", "Bb", "Eb", "Ab", "F#", "Db")
            val t = types[rnd.nextInt(types.size)]
            val ch = Chord(Pitch.parse(roots[rnd.nextInt(roots.size)] + "4"), t)
            val ps = ch.pitches(4)
            Step.TapKeys("Play ${ch.prettySymbol} (${t.displayName}). Hold all the notes together - any octave.", ps.map { it.name }, anyOctave = true,
                from = "C3", to = "B5", explanation = "${ch.prettySymbol} = ${ps.joinToString(" ") { it.pretty }}")
        }
        DrillKind.PLAY_SCALE -> {
            val keys = d.keys.ifEmpty { if (d.level <= 1) listOf("C", "G", "F") else if (d.level == 2) listOf("G", "D", "A", "F", "Bb", "Eb") else listOf("Am", "Em", "Dm", "Gm") }
            val k = keys[(index + rnd.nextInt(keys.size)) % keys.size]
            val key = Key.parse(k)
            val minor = key.mode == Mode.MINOR
            val type = if (minor) ScaleType.HARMONIC_MINOR else ScaleType.MAJOR
            val tonic = key.tonic.withOctave(4).let { if (it.midi > 66) it.withOctave(3) else it }
            val ps = type.pitches(tonic)
            Step.PlayAlong("Play the ${key.tonic.pretty} ${if (minor) "harmonic minor" else "major"} scale, one octave up.",
                ps.joinToString(" ") { it.name + ":q" }, from = Pitch.fromMidi(ps.first().midi).name, to = Pitch.fromMidi(ps.last().midi).name,
                hints = d.level <= 1)
        }
        DrillKind.KEY_SIGNATURE -> {
            val max = if (d.level <= 1) 3 else 7
            val sig = rnd.nextInt(-max, max + 1)
            val major = Key.MAJOR_SIGNATURES.entries.first { it.value == sig }.key
            val majorKey = Key.major(major)
            val minorAsk = d.level >= 3 && rnd.nextBoolean()
            val answer = if (minorAsk) majorKey.relative.name else majorKey.name
            val near = Key.MAJOR_SIGNATURES.filter { kotlin.math.abs(it.value - sig) in 1..3 }.keys
                .map { Key.major(it).let { k -> if (minorAsk) k.relative.name else k.name } }
            Step.Quiz("Which ${if (minorAsk) "minor" else "major"} key has this key signature?", pickDistinct(rnd, answer, near),
                visual = Visual.Staff(Clef.TREBLE, "", keySig = sig),
                explanation = when {
                    sig == 0 -> "No sharps or flats: C major / A minor."
                    sig > 0 -> "$sig sharp(s): last sharp is ${Key.signatureAccidentals(sig).last().pretty}, a half step below ${majorKey.tonic.pretty}."
                    else -> "${-sig} flat(s)." + if (sig < -1) " The second-to-last flat names the key." else " One flat = F major."
                })
        }
        DrillKind.RELATIVE_KEY -> {
            val k = Key.ALL_MAJOR[rnd.nextInt(12)]
            if (rnd.nextBoolean()) {
                Step.Quiz("What is the relative minor of ${k.name}?", pickDistinct(rnd, k.relative.name, Key.ALL_MINOR.map { it.name }),
                    explanation = "Go down a minor 3rd (3 half steps) from ${k.tonic.pretty}.")
            } else {
                Step.Quiz("What is the relative major of ${k.relative.name}?", pickDistinct(rnd, k.name, Key.ALL_MAJOR.map { it.name }),
                    explanation = "Go up a minor 3rd from ${k.relative.tonic.pretty}.")
            }
        }
        DrillKind.NOTE_VALUE -> {
            val items = listOf("whole note" to "4", "half note" to "2", "quarter note" to "1", "eighth note" to "½", "sixteenth note" to "¼",
                "dotted half note" to "3", "dotted quarter note" to "1½", "dotted eighth note" to "¾", "whole rest" to "4", "half rest" to "2")
            val (name, beats) = items[rnd.nextInt(items.size)]
            Step.Quiz("In 4/4 time, how many beats does a $name last?", pickDistinct(rnd, beats, listOf("4", "2", "1", "½", "3", "1½", "¼", "6")))
        }
        DrillKind.DIATONIC_CHORD -> {
            val keys = listOf("C", "G", "D", "F", "Bb", "A", "E", "Eb")
            val key = Key.major(keys[rnd.nextInt(keys.size)])
            val triads = key.diatonicTriads()
            val idx = listOf(1, 2, 3, 4, 5, 6).let { it[rnd.nextInt(it.size)] }
            val (num, ch) = triads[idx]
            Step.Quiz("In ${key.name}, what is the $num chord?", pickDistinct(rnd, ch.prettySymbol, triads.map { it.second.prettySymbol }),
                explanation = "${key.name} chords: " + triads.joinToString(", ") { "${it.first}=${it.second.prettySymbol}" })
        }
        DrillKind.CIRCLE -> {
            val circle = Key.CIRCLE_MAJOR
            when (rnd.nextInt(3)) {
                0 -> {
                    val i = rnd.nextInt(12)
                    val ans = Key.major(circle[(i + 1) % 12]).name
                    Step.Quiz("Which major key is a perfect 5th above ${Key.major(circle[i]).name}?", pickDistinct(rnd, ans, circle.map { Key.major(it).name }),
                        visual = Visual.CircleOfFifths(circle[i]))
                }
                1 -> {
                    val keys = Key.MAJOR_SIGNATURES.entries.filter { it.value in -6..6 }
                    val e = keys[rnd.nextInt(keys.size)]
                    fun desc(v: Int) = when { v == 0 -> "No sharps or flats"; v > 0 -> "$v sharp${if (v > 1) "s" else ""}"; else -> "${-v} flat${if (v < -1) "s" else ""}" }
                    Step.Quiz("How many sharps or flats does ${Key.major(e.key).name} have?", pickDistinct(rnd, desc(e.value), (-6..6).map { desc(it) }))
                }
                else -> {
                    val n = rnd.nextInt(1, 7)
                    val flats = rnd.nextBoolean()
                    val sig = if (flats) -n else n
                    val k = Key.major(Key.MAJOR_SIGNATURES.entries.first { it.value == sig }.key)
                    Step.Quiz("Which major key has $n ${if (flats) "flat" else "sharp"}${if (n > 1) "s" else ""}?",
                        pickDistinct(rnd, k.name, Key.MAJOR_SIGNATURES.keys.map { Key.major(it).name }))
                }
            }
        }
        DrillKind.CADENCE_EAR -> {
            val cad = listOf(
                "Perfect (V → I)" to "I IV V7 I",
                "Plagal (IV → I)" to "I V IV I",
                "Half (ends on V)" to "I vi IV V",
                "Deceptive (V → vi)" to "I IV V vi",
            )
            val (name, numerals) = cad[rnd.nextInt(cad.size)]
            Step.Quiz("Listen to the ending - which cadence?", pickDistinct(rnd, name, cad.map { it.first }),
                play = progressionNotation(numerals, Key.major(listOf("C", "F", "G", "D")[rnd.nextInt(4)])), playBpm = 80)
        }
        DrillKind.PROGRESSION_EAR -> {
            val all = listOf("I IV V I", "I V vi IV", "vi IV I V", "I vi IV V", "ii V I I", "I IV I V", "i iv V i", "I bVII IV I")
            val pool = when (d.level) { 1 -> all.take(4); 2 -> all.take(6); else -> all }
            val prog = pool[rnd.nextInt(pool.size)]
            val key = if (prog.startsWith("i ")) Key.minor("A") else Key.major(listOf("C", "G", "F", "D")[rnd.nextInt(4)])
            Step.Quiz("Which progression do you hear?", pickDistinct(rnd, prog.replace(" ", " - "), pool.map { it.replace(" ", " - ") }),
                play = progressionNotation(prog, key), playBpm = 76,
                explanation = "In ${key.name}: " + RomanNumerals.progression(prog, key).joinToString(" - ") { it.prettySymbol })
        }
        DrillKind.MODE_ID -> {
            val modes = listOf(ScaleType.MAJOR to "Ionian (major)", ScaleType.DORIAN to "Dorian", ScaleType.PHRYGIAN to "Phrygian",
                ScaleType.LYDIAN to "Lydian", ScaleType.MIXOLYDIAN to "Mixolydian", ScaleType.NATURAL_MINOR to "Aeolian (natural minor)")
            val (type, name) = modes[rnd.nextInt(modes.size)]
            val tonic = Pitch.parse("D4")
            val ps = type.pitches(tonic)
            val third = ps[2]
            val play = "D3+${tonic.name}+${third.name}+${ps[4].name}:h " + ps.joinToString(" ") { it.name + ":e" } + " " + ps.reversed().drop(1).joinToString(" ") { it.name + ":e" }
            Step.Quiz("Which mode is this (starting on D)?", pickDistinct(rnd, name, modes.map { it.second }), play = play, playBpm = 120,
                explanation = type.description)
        }
        DrillKind.SCALE_DEGREE -> {
            val keys = listOf("C", "G", "D", "F", "Bb", "A")
            val key = Key.major(keys[rnd.nextInt(keys.size)])
            val names = listOf("tonic", "supertonic", "mediant", "subdominant", "dominant", "submediant", "leading tone")
            val deg = rnd.nextInt(7)
            val sp = key.scalePitches()
            Step.Quiz("What is the ${names[deg]} (degree ${deg + 1}) of ${key.name}?", pickDistinct(rnd, sp[deg].pretty, sp.map { it.pretty }))
        }
        DrillKind.TERMS -> {
            val terms = listOf(
                "Allegro" to "Fast and lively", "Adagio" to "Slow", "Andante" to "Walking pace", "Presto" to "Very fast",
                "Crescendo" to "Gradually louder", "Diminuendo" to "Gradually softer", "Legato" to "Smooth and connected",
                "Staccato" to "Short and detached", "Rubato" to "Flexible tempo", "Dolce" to "Sweetly", "Fermata" to "Hold / pause",
                "Ritardando" to "Gradually slower", "A tempo" to "Return to the original tempo", "Cantabile" to "In a singing style",
                "Forte" to "Loud", "Piano" to "Soft", "Sforzando" to "Sudden strong accent", "Subito" to "Suddenly",
                "Poco a poco" to "Little by little", "Fine" to "The end", "Da capo" to "From the beginning", "Accelerando" to "Gradually faster",
            )
            val (t, m) = terms[rnd.nextInt(terms.size)]
            Step.Quiz("What does '$t' mean?", pickDistinct(rnd, m, terms.map { it.second }))
        }
    }

    private fun nameNote(rnd: Random, clef: Clef, pool: List<String>, accidentals: Boolean, grand: Boolean = false): Step {
        var p = Pitch.parse(pool[rnd.nextInt(pool.size)])
        if (accidentals && rnd.nextInt(3) == 0) p = Pitch(p.letter, if (rnd.nextBoolean()) 1 else -1, p.octave)
        val answer = p.pretty
        val wrong = letters.map { it } + if (accidentals) listOf("${p.pretty.first()}♯", "${p.pretty.first()}♭", p.pretty.first().toString()) else emptyList()
        return Step.Quiz("Name this note.", pickDistinct(rnd, answer, wrong),
            visual = Visual.Staff(if (grand) Clef.GRAND else clef, "${p.name}:w"), play = "${p.name}:w",
            explanation = "It's ${p.prettyName}.")
    }

    /** Block-chord notation for a Roman-numeral progression, with a bass note and smooth upper voices. */
    fun progressionNotation(numerals: String, key: Key, beatsEach: Int = 2): String =
        RomanNumerals.progression(numerals, key).joinToString(" ") { ch ->
            val upper = ch.compVoicing(64)
            val bass = ch.bassMidi()
            (listOf(bass) + upper).joinToString("+") { Pitch.fromMidi(it, key.signature < 0).name } + ":$beatsEach"
        }
}
