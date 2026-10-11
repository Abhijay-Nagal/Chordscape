package com.pianoquest.app.data

import com.pianoquest.app.theory.Pitch

enum class Genre(val displayName: String, val blurb: String) {
    BEGINNER("Beginner Favourites", "Nursery and folk tunes perfect for your first weeks."),
    FOLK("Folk & Traditional", "Timeless melodies from around the world."),
    HOLIDAY("Holiday", "Seasonal classics to play for family and friends."),
    BAROQUE("Baroque", "Bach, Pachelbel and friends (1600-1750)."),
    CLASSICAL("Classical", "Mozart, Beethoven and the Classical era (1750-1820)."),
    ROMANTIC("Romantic", "Expressive music of the 19th century."),
    MODERN_CLASSICAL("Impressionist & Modern", "Satie, Debussy and the 20th century."),
    RAGTIME("Ragtime & Stride", "Syncopated American piano from the 1900s."),
    JAZZ("Jazz", "Swing, ii-V-I's and jazz harmony studies."),
    BLUES("Blues & Boogie", "12-bar blues and boogie-woogie grooves."),
    POP("Pop Chord Studies", "The chord progressions behind famous pop songs."),
    CONTEMPORARY("Contemporary & Film", "Original pieces in modern cinematic style."),
    LATIN("Latin", "Bossa nova and Latin grooves."),
    GOSPEL("Gospel & Soul", "Church chords, walk-ups and soulful progressions."),
    EXERCISE("Technique Exercises", "Scales, arpeggios and finger exercises."),
    IMPORTED("My Imported Songs", "MIDI files you imported from your device."),
}

/**
 * A playable piece. Durations are in quarter-note beats. [chords] generates the left hand with [style] when
 * [lh] is not given; when [rh] is blank and [rhChordRhythm] is set, the right hand plays the chords too
 * (used for chord-progression studies of copyrighted pop songs - only the chords, never the melody).
 */
data class Song(
    val id: String,
    val title: String,
    val composer: String,
    val genre: Genre,
    val difficulty: Int,
    val bpm: Int,
    val meterNum: Int = 4,
    val meterDen: Int = 4,
    val key: String = "C",
    val rh: String = "",
    val lh: String? = null,
    val chords: String? = null,
    val style: AccStyle = AccStyle.BLOCK,
    val rhChordRhythm: String? = null,
    val extraRh: List<String> = emptyList(),
    val extraLh: List<String> = emptyList(),
    val pickup: Double = 0.0,
    val about: String = "",
    val tips: List<String> = emptyList(),
    val source: String = "Public domain",
    /** Bundled complete score: composeResources/files/midi/<name>.mid */
    val midiResource: String? = null,
    /** A MIDI file imported by the user (stored in app storage). */
    val importedFile: String? = null,
    /** Length in seconds for MIDI songs (known before loading). */
    val durationHint: Double? = null,
    val sourceUrl: String? = null,
) {
    val isMidi: Boolean get() = midiResource != null || importedFile != null

    /** Bar length in quarter-note beats (3/8 = 1.5, 6/8 = 3). */
    val barBeats: Double get() = meterNum * 4.0 / meterDen

    val meter: String get() = "$meterNum/$meterDen"

    val events: List<NoteEvent> by lazy { build() }

    val lengthBeats: Double by lazy { events.maxOfOrNull { it.end } ?: 0.0 }

    val durationSeconds: Double get() = durationHint ?: (lengthBeats * 60.0 / bpm)

    val chordSpans: List<ChordSpan> by lazy { chords?.let { ChordTrack.parse(it) } ?: emptyList() }

    val difficultyLabel: String get() = DIFFICULTY_LABELS[(difficulty - 1).coerceIn(0, 4)]

    val hasLeftHand: Boolean get() = isMidi || events.any { it.hand == Hand.LEFT }

    private fun build(): List<NoteEvent> {
        if (isMidi) return emptyList()
        val out = ArrayList<NoteEvent>()
        if (rh.isNotBlank()) out += Notation.parse(rh, Hand.RIGHT)
        for (v in extraRh) out += Notation.parse(v, Hand.RIGHT, velocity = 70)
        for (v in extraLh) out += Notation.parse(v, Hand.LEFT, velocity = 68)
        if (lh != null) {
            out += Notation.parse(lh, Hand.LEFT, velocity = 72)
        } else if (chords != null) {
            out += Accompaniment.generate(chordSpans, style, barBeats.toInt().coerceAtLeast(1))
        }
        if (rh.isBlank() && rhChordRhythm != null) out += chordRightHand(rhChordRhythm)
        out.sortWith(compareBy({ it.start }, { it.midi }))
        return out
    }

    /**
     * Right hand generated from the chord track. [pattern] is either a list of durations ("1.5 1.5 1", rests as "R1"),
     * "arp" (broken chord up and down in eighths) or "desc" (top-down triad in eighths, Clocks-style).
     */
    private fun chordRightHand(pattern: String): List<NoteEvent> {
        val out = ArrayList<NoteEvent>()
        for (span in chordSpans) {
            val chord = span.chord ?: continue
            val voicing = chord.compVoicing(center = 64)
            val flats = chord.root.accidental < 0 || key.contains('b')
            fun note(m: Int, t: Double, d: Double, v: Int = 80) =
                NoteEvent(Pitch.fromMidi(m, flats), span.start + t, d, Hand.RIGHT, v)
            when (pattern.trim()) {
                "arp" -> {
                    val seq = voicing + (voicing.last() + 12).let { top -> listOf(top) } + voicing.drop(1).reversed()
                    var t = 0.0; var i = 0
                    while (t < span.duration - 1e-6) { out += note(seq[i % seq.size], t, 0.5); t += 0.5; i++ }
                }
                "desc" -> {
                    val seq = voicing.take(3).reversed().map { it + 12 }
                    var t = 0.0; var i = 0
                    while (t < span.duration - 1e-6) {
                        if (i % 8 == 0) i = 0
                        out += note(seq[i % 3], t, 0.5, if (i % 3 == 0) 88 else 74)
                        t += 0.5; i++
                    }
                }
                else -> {
                    val tokens = pattern.trim().split(Regex("\\s+"))
                    var t = 0.0; var i = 0
                    while (t < span.duration - 1e-6) {
                        val tok = tokens[i % tokens.size]
                        val rest = tok.startsWith("R")
                        val d = minOf(Notation.parseDuration(tok.removePrefix("R")), span.duration - t)
                        if (!rest) for (m in voicing) out += note(m, t, d * 0.9)
                        t += d; i++
                    }
                }
            }
        }
        return out
    }

    companion object {
        val DIFFICULTY_LABELS = listOf("Very easy", "Easy", "Intermediate", "Hard", "Advanced")
    }
}
