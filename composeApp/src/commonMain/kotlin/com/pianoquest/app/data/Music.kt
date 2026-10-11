package com.pianoquest.app.data

import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.Pitch

enum class Hand(val label: String) { RIGHT("Right hand"), LEFT("Left hand") }

/** A note in a piece, timed in beats (quarter note = 1 beat unless the meter says otherwise). */
data class NoteEvent(
    val pitch: Pitch,
    val start: Double,
    val duration: Double,
    val hand: Hand,
    val velocity: Int = 90,
) {
    val midi: Int get() = pitch.midi
    val end: Double get() = start + duration
}

/**
 * Compact text notation used for every song and exercise in the app.
 *
 *  - Notes: `C4`, `F#5`, `Bb3`; chords join notes with `+`: `C4+E4+G4`
 *  - Duration after a colon, in beats or letters: `:1`, `:0.5`, `:q` `:h` `:w` `:e` `:s`, dotted `:q.` `:h.`, triplet eighth `:t`
 *  - Durations are sticky: a note without one reuses the previous duration.
 *  - Rests: `R` or `R:2`. Bar lines `|` are ignored (they're for readability).
 *  - Repeats: `{ C4 E4 G4 E4 }*4`
 */
object Notation {

    fun parse(text: String, hand: Hand = Hand.RIGHT, startBeat: Double = 0.0, velocity: Int = 90): List<NoteEvent> {
        val tokens = expandRepeats(text).split(Regex("\\s+")).filter { it.isNotEmpty() && it != "|" }
        val out = ArrayList<NoteEvent>()
        var time = startBeat
        var dur = 1.0
        for (raw in tokens) {
            val tok = raw.trim('|')
            if (tok.isEmpty()) continue
            val colon = tok.indexOf(':')
            val body = if (colon >= 0) tok.substring(0, colon) else tok
            if (colon >= 0) dur = parseDuration(tok.substring(colon + 1))
            if (body.equals("R", ignoreCase = false) || body == "-") {
                time += dur
                continue
            }
            for (part in body.split('+')) {
                out += NoteEvent(Pitch.parse(part), time, dur, hand, velocity)
            }
            time += dur
        }
        return out
    }

    /** Total length in beats of a notation string. */
    fun length(text: String): Double {
        val tokens = expandRepeats(text).split(Regex("\\s+")).filter { it.isNotEmpty() && it != "|" }
        var time = 0.0
        var dur = 1.0
        for (tok in tokens) {
            val colon = tok.indexOf(':')
            if (colon >= 0) dur = parseDuration(tok.substring(colon + 1))
            time += dur
        }
        return time
    }

    fun parseDuration(s: String): Double {
        val t = s.trim()
        val dotted = t.endsWith(".")
        val core = t.removeSuffix(".")
        val base = when (core) {
            "w" -> 4.0
            "h" -> 2.0
            "q" -> 1.0
            "e" -> 0.5
            "s" -> 0.25
            "t" -> 1.0 / 3.0
            "ts" -> 1.0 / 6.0
            "qt" -> 2.0 / 3.0
            else -> if (core.contains('/')) {
                val (a, b) = core.split('/')
                a.toDouble() / b.toDouble()
            } else core.toDouble()
        }
        return if (dotted) base * 1.5 else base
    }

    private val repeatRegex = Regex("\\{([^{}]*)\\}\\*(\\d+)")

    fun expandRepeats(text: String): String {
        var t = text
        while (true) {
            val m = repeatRegex.find(t) ?: break
            val body = m.groupValues[1]
            val times = m.groupValues[2].toInt()
            t = t.replaceRange(m.range, List(times) { body }.joinToString(" "))
        }
        return t
    }
}

/** Chord-symbol track: "C:4 G:4 Am:2 F:2" (duration in beats, sticky). "N.C." or "R" = no chord. */
data class ChordSpan(val chord: Chord?, val start: Double, val duration: Double)

object ChordTrack {
    fun parse(text: String, startBeat: Double = 0.0): List<ChordSpan> {
        val out = ArrayList<ChordSpan>()
        var time = startBeat
        var dur = 4.0
        for (tok in Notation.expandRepeats(text).split(Regex("\\s+")).filter { it.isNotEmpty() && it != "|" }) {
            val colon = tok.lastIndexOf(':')
            val name = if (colon >= 0) tok.substring(0, colon) else tok
            if (colon >= 0) dur = Notation.parseDuration(tok.substring(colon + 1))
            val chord = if (name == "R" || name == "N.C.") null else Chord.parse(name)
            out += ChordSpan(chord, time, dur)
            time += dur
        }
        return out
    }
}

/** Left-hand accompaniment patterns generated from chord symbols. */
enum class AccStyle(val displayName: String, val description: String) {
    NONE("None", "No accompaniment"),
    BASS("Bass notes", "Just the root of each chord, held."),
    BLOCK("Block chords", "The whole chord held, in close position."),
    ROOT_CHORD("Root + chord", "Bass note on the beat, chord on the off-beats (oom-pah)."),
    WALTZ("Waltz", "Bass on beat 1, chord on beats 2 and 3."),
    BROKEN("Broken 1-5-8", "Root, fifth and octave rolling in eighths."),
    ALBERTI("Alberti bass", "Low-high-middle-high: the Classical-era favourite."),
    ARPEGGIO("Pop ballad arpeggio", "Root-5th-octave-10th flowing eighths."),
    STRIDE("Stride", "Low bass note then mid-range chord, alternating every beat."),
    BOOGIE("Boogie-woogie", "Root-5-6-b7 walking eighths."),
    WALKING("Walking bass", "Quarter-note jazz bass walking between chord roots."),
    BOSSA("Bossa nova", "Root and fifth in a gentle dotted pattern."),
    POP_332("3-3-2 groove", "Syncopated chord hits on 1, the 'and' of 2, and 4 (tresillo)."),
    BASS_332("3-3-2 bass", "Bass note only, in the syncopated 3-3-2 rhythm."),
    BASS_PULSE("Pulsing bass", "Root repeated in steady eighth notes (pop/rock)."),
    SUSTAINED_OCTAVES("Low octaves", "Root in octaves, held: big cinematic sound."),
}

object Accompaniment {

    fun generate(spans: List<ChordSpan>, style: AccStyle, beatsPerBar: Int, velocity: Int = 70): List<NoteEvent> {
        if (style == AccStyle.NONE) return emptyList()
        val out = ArrayList<NoteEvent>()
        for ((idx, span) in spans.withIndex()) {
            val chord = span.chord ?: continue
            val next = spans.getOrNull(idx + 1)?.chord
            out += pattern(chord, next, span.start, span.duration, style, beatsPerBar, velocity)
        }
        return out
    }

    private fun ev(midi: Int, start: Double, dur: Double, vel: Int) =
        NoteEvent(Pitch.fromMidi(midi), start, dur, Hand.LEFT, vel)

    /** Triad/7th tones in close position just below middle C, starting from the root. */
    private fun lhChord(chord: Chord): List<Int> {
        val pcs = chord.type.intervals.filter { it.semitones < 12 }.map { chord.root.transpose(it).pitchClass }.distinct().take(4)
        var root = 48 + pcs[0]
        if (root > 55) root -= 12
        val res = mutableListOf(root)
        for (pc in pcs.drop(1)) {
            var n = res.last() + 1
            while (n.mod(12) != pc) n++
            res += n
        }
        return res
    }

    private fun fifthAbove(chord: Chord, bass: Int): Int {
        val fifth = chord.type.intervals.firstOrNull { it.steps == 4 }?.semitones ?: 7
        return bass + fifth
    }

    private fun pattern(chord: Chord, next: Chord?, start: Double, dur: Double, style: AccStyle, bpb: Int, vel: Int): List<NoteEvent> {
        val bass = chord.bassMidi()
        val tones = lhChord(chord)
        val out = ArrayList<NoteEvent>()
        fun steps(step: Double, notes: (Int) -> List<Int>, noteDur: Double = step) {
            var t = 0.0
            var i = 0
            while (t < dur - 1e-6) {
                val d = minOf(noteDur, dur - t)
                for (n in notes(i)) out += ev(n, start + t, d, vel)
                t += step
                i++
            }
        }
        when (style) {
            AccStyle.NONE -> {}
            AccStyle.BASS -> out += ev(bass, start, dur, vel)
            AccStyle.SUSTAINED_OCTAVES -> { out += ev(bass - 12, start, dur, vel); out += ev(bass, start, dur, vel) }
            AccStyle.BLOCK -> tones.forEach { out += ev(it, start, dur, vel) }
            AccStyle.ROOT_CHORD -> steps(1.0, { i -> if (i % 2 == 0) listOf(bass) else tones.drop(1).ifEmpty { tones } })
            AccStyle.WALTZ -> steps(1.0, { i -> if (i % bpb == 0) listOf(bass) else tones.drop(1).map { if (it < bass + 5) it + 12 else it } })
            AccStyle.BROKEN -> steps(0.5, { i -> listOf(listOf(bass, fifthAbove(chord, bass), bass + 12, fifthAbove(chord, bass))[i % 4]) })
            AccStyle.ALBERTI -> {
                val lo = tones[0]; val mid = tones.getOrElse(1) { lo + 4 }; val hi = tones.getOrElse(2) { lo + 7 }
                steps(0.5, { i -> listOf(listOf(lo, hi, mid, hi)[i % 4]) })
            }
            AccStyle.ARPEGGIO -> {
                val third = chord.root.transpose(chord.type.intervals[1]).pitchClass
                var tenth = bass + 12
                while (tenth.mod(12) != third) tenth++
                steps(0.5, { i -> listOf(listOf(bass, fifthAbove(chord, bass), bass + 12, tenth, bass + 12, fifthAbove(chord, bass), bass + 12, tenth)[i % 8]) },
                    noteDur = 0.5)
            }
            AccStyle.STRIDE -> {
                val high = tones.map { if (it < 53) it + 12 else it }
                steps(1.0, { i -> if (i % 2 == 0) listOf(if (i % 4 == 0) bass - 12 else fifthAbove(chord, bass) - 12) else high })
            }
            AccStyle.BOOGIE -> {
                val third = if (chord.type.intervals[1].semitones == 3) 3 else 4
                val seq = listOf(0, third, 7, 9, 10, 9, 7, third)
                val b = bass.let { if (it > 45) it - 12 else it }
                steps(0.5, { i -> listOf(b + seq[i % seq.size]) })
            }
            AccStyle.WALKING -> {
                val target = next?.bassMidi() ?: bass
                val beats = dur.toInt().coerceAtLeast(1)
                val third = bass + chord.type.intervals[1].semitones
                val fifth = fifthAbove(chord, bass)
                val approach = if (target > bass) target - 1 else target + 1
                val line = when (beats) {
                    1 -> listOf(bass)
                    2 -> listOf(bass, approach)
                    3 -> listOf(bass, third, approach)
                    else -> listOf(bass, third, fifth, approach) + List(beats - 4) { fifth }
                }
                line.forEachIndexed { i, n -> out += ev(n, start + i, 1.0, vel) }
            }
            AccStyle.BOSSA -> {
                var t = 0.0
                while (t < dur - 1e-6) {
                    out += ev(bass, start + t, 1.5, vel)
                    if (t + 1.5 < dur) out += ev(fifthAbove(chord, bass) - 12, start + t + 1.5, 0.5, vel)
                    if (t + 2 < dur) out += ev(fifthAbove(chord, bass) - 12, start + t + 2, 1.5, vel)
                    if (t + 3.5 < dur) out += ev(bass, start + t + 3.5, 0.5, vel)
                    t += 4.0
                }
                tones.forEach { out += ev(it + 12, start + 1.0, minOf(dur - 1.0, 2.0).coerceAtLeast(0.5), vel - 10) }
            }
            AccStyle.BASS_332 -> {
                var t = 0.0
                while (t < dur - 1e-6) {
                    for ((off, d) in listOf(0.0 to 1.5, 1.5 to 1.5, 3.0 to 1.0)) if (t + off < dur) out += ev(bass, start + t + off, minOf(d, dur - t - off), vel)
                    t += 4.0
                }
            }
            AccStyle.BASS_PULSE -> steps(0.5, { i -> listOf(if (i % 8 == 0) bass - 12 else bass) }, noteDur = 0.45)
            AccStyle.POP_332 -> {
                var t = 0.0
                while (t < dur - 1e-6) {
                    for ((off, d) in listOf(0.0 to 1.5, 1.5 to 1.5, 3.0 to 1.0)) {
                        if (t + off < dur) {
                            out += ev(bass, start + t + off, minOf(d, dur - t - off), vel)
                            tones.forEach { out += ev(it + if (it < 52) 12 else 0, start + t + off, minOf(d, dur - t - off) * 0.8, vel - 8) }
                        }
                    }
                    t += 4.0
                }
            }
        }
        return out
    }
}
