package com.pianoquest.app.data.songs

import com.pianoquest.app.data.AccStyle
import com.pianoquest.app.data.Genre
import com.pianoquest.app.data.Song

private const val ORIGINAL = "Keyscape original composition"

internal fun jazzBluesSongs(): List<Song> {
    val cLick = "C5:e Eb5 E5 G5 A5 G5 Eb5 C5"
    val cResp = "Bb4+E5:q. Bb4+E5:e R:h"
    val fLick = "F4:e Ab4 A4 C5 D5 C5 Ab4 F4"
    val fResp = "A4+Eb5:q. A4+Eb5:e R:h"
    val gLick = "G4:e Bb4 B4 D5 E5 D5 Bb4 G4"

    val stabC = "G4+C5+E5:e. R:s G4+C5+E5:e R:e Bb4+C5+E5:q R:q"
    val stabF = "A4+C5+F5:e. R:s A4+C5+F5:e R:e A4+C5+Eb5:q R:q"
    val stabG = "B4+D5+G5:e. R:s B4+D5+G5:e R:e B4+D5+F5:q R:q"

    val aLick = "A4:e C5 D5 Eb5 E5 G5 E5 C5"
    val aResp = "A4+C5+E5:q. A4+C5+E5:e R:h"
    val dLick = "D5:e F5 G5 Ab5 A5 C6 A5 F5"

    return listOf(
        Song(
            id = "blue-monday", title = "Blue Monday Shuffle", composer = ORIGINAL, genre = Genre.BLUES,
            difficulty = 3, bpm = 126, key = "C", source = ORIGINAL,
            rh = "$cLick | $cResp | $cLick | $cResp | $fLick | $fResp | $cLick | $cResp | $gLick | $fLick | " +
                "C5:q Eb5:e E5:e G5:q. C5:e | G4+B4+F5:h R:h | E4+G4+Bb4+C5:w",
            chords = "C7:4 | C7:4 | C7:4 | C7:4 | F7:4 | F7:4 | C7:4 | C7:4 | G7:4 | F7:4 | C7:4 | G7:4 | C7:4",
            style = AccStyle.BOOGIE,
            about = "A 12-bar blues in C. Right hand: call-and-response licks from the C blues scale (C E♭ F G♭ G B♭) plus the major 3rd. " +
                "Left hand: the classic boogie-woogie walking pattern (root-3-5-6-♭7-6-5-3).",
            tips = listOf("Bars 1-4 = I, 5-6 = IV, 7-8 = I, 9 = V, 10 = IV, 11 = I, 12 = V (turnaround).",
                "Real blues is swung: lengthen the first eighth of each pair (long-short)."),
        ),
        Song(
            id = "boogie-train", title = "Boogie Train", composer = ORIGINAL, genre = Genre.BLUES,
            difficulty = 3, bpm = 140, key = "C", source = ORIGINAL,
            rh = "$stabC | $stabC | $stabC | $stabC | $stabF | $stabF | $stabC | $stabC | $stabG | $stabF | $stabC | $stabG | G4+C5+E5:w",
            chords = "C:4 | C:4 | C:4 | C:4 | F:4 | F:4 | C:4 | C:4 | G:4 | F:4 | C:4 | G:4 | C:4",
            style = AccStyle.BOOGIE,
            about = "Boogie-woogie: a driving eighth-note left hand that never stops, with syncopated right-hand chord 'stabs'. " +
                "Born in the 1920s-30s, it led straight to rock 'n' roll.",
            tips = listOf("Master the left hand alone until it's automatic.", "Accent the stabs - they push against the beat."),
        ),
        Song(
            id = "minor-blues", title = "Midnight Minor Blues", composer = ORIGINAL, genre = Genre.BLUES,
            difficulty = 3, bpm = 104, key = "Am", source = ORIGINAL,
            rh = "$aLick | $aResp | $aLick | $aResp | $dLick | D5+F5+A5:q. D5+F5+A5:e R:h | $aLick | $aResp | " +
                "C5:e Eb5 F5 A5 Eb5:h | D5:e B4 G#4 E4 D5:h | A4:e C5 E5 A5 C6:h | G#4+B4+D5+E5:h R:h | A3+C4+E4+A4:w",
            chords = "Am7:4 | Am7:4 | Am7:4 | Am7:4 | Dm7:4 | Dm7:4 | Am7:4 | Am7:4 | F7:4 | E7:4 | Am7:4 | E7:4 | Am:4",
            style = AccStyle.BOOGIE,
            about = "A minor blues: i-iv-i with a ♭VI7-V7 ending (F7-E7) instead of V-IV. Darker and moodier than major blues.",
        ),
        Song(
            id = "guide-tones", title = "ii-V-I Guide Tones (down a whole step)", composer = ORIGINAL, genre = Genre.JAZZ,
            difficulty = 2, bpm = 80, key = "C", source = ORIGINAL,
            rh = "F4+C5:w | F4+B4:w | E4+B4:w | E4+B4:w | Eb4+Bb4:w | Eb4+A4:w | D4+A4:w | D4+A4:w | Db4+Ab4:w | Db4+G4:w | C4+G4:w | C4+G4:w",
            lh = "D3:w | G2:w | C3:w | C3:w | C3:w | F2:w | Bb2:w | Bb2:w | Bb2:w | Eb2:w | Ab2:w | Ab2:w",
            about = "The ii-V-I is the most important progression in jazz. Here the right hand plays only the 3rd and 7th of each chord " +
                "(the 'guide tones'). Notice how each moves by a half step or stays put - that smooth motion is the sound of jazz harmony.",
            tips = listOf("Dm7 → G7: C (7th of Dm7) falls to B (3rd of G7).", "G7 → Cmaj7: F (7th of G7) falls to E (3rd of C).",
                "Then the whole thing moves down a whole step: C, B♭, A♭."),
        ),
        Song(
            id = "walkin-f", title = "Walkin' the F Blues", composer = ORIGINAL, genre = Genre.JAZZ,
            difficulty = 4, bpm = 132, key = "F", source = ORIGINAL,
            chords = "F7:4 | Bb7:4 | F7:4 | Cm7:2 F7:2 | Bb7:4 | Bdim7:4 | F7:4 | D7:4 | Gm7:4 | C7:4 | F7:2 D7:2 | Gm7:2 C7:2 | F7:4",
            style = AccStyle.WALKING, rhChordRhythm = "R1 0.5 R1 1.5",
            about = "Jazz blues: the 12-bar form with jazz substitutions - a quick IV in bar 2, a ii-V into bar 5, a diminished passing chord, " +
                "and a I-VI-ii-V turnaround. Left hand walks quarter notes; right hand 'comps' on the off-beats.",
            tips = listOf("Walking bass: chord tone on beat 1, approach the next root by half step on beat 4.",
                "Comping = accompanying with short, rhythmic chords. Leave space!"),
        ),
        Song(
            id = "stride-steps", title = "Stride Steps", composer = ORIGINAL, genre = Genre.RAGTIME,
            difficulty = 3, bpm = 112, key = "C", source = ORIGINAL,
            rh = "E5:q. D5:e C5:q G4:q | A4:q C5:q E5:h | D5:q. C5:e A4:q F4:q | G4:w | E5:q. D5:e C5:q G4:q | A4:q C5:q E5:q G5:q | " +
                "F5:q E5:q D5:q B4:q | C5:w",
            chords = "C:4 | F:4 | Dm7:2 G7:2 | G7:4 | C:2 C7:2 | F:2 Fm:2 | C:2 G7:2 | C:4",
            style = AccStyle.STRIDE,
            about = "Stride piano (1920s Harlem: James P. Johnson, Fats Waller) - the left hand 'strides' between a low bass note and a mid-range chord.",
            tips = listOf("Practise the left-hand leap with your eyes closed to build spatial memory.", "Bar 6 uses the minor iv (Fm) - a lovely borrowed chord."),
        ),
        Song(
            id = "bossa-breeze", title = "Bossa Breeze", composer = ORIGINAL, genre = Genre.LATIN,
            difficulty = 3, bpm = 120, key = "C", source = ORIGINAL,
            rh = "E5:h. D5:e E5:e | C5:w | F5:q. E5:e F5:q A5:q | G5:h F5:q D5:q | E5:h. D5:e E5:e | C#5:w | D5:q. E5:e F5:q A5:q | " +
                "G5:q F5:q D5:q B4:q | C5:w | E4+G4+B4+D5:w",
            chords = "Cmaj7:4 | Am7:4 | Dm7:4 | G7:4 | Em7:4 | A7:4 | Dm7:4 | G7:4 | Cmaj7:4 | Cmaj7:4",
            style = AccStyle.BOSSA,
            about = "Bossa nova ('new trend') came from Rio de Janeiro around 1958. Seventh chords, a gentle syncopated bass and a relaxed melody.",
            tips = listOf("Em7 → A7 → Dm7 is a 'secondary dominant': A7 is the V of Dm.", "Keep the touch light and laid-back."),
        ),
        Song(
            id = "gospel-sunday", title = "Sunday Morning (gospel walk-up)", composer = ORIGINAL, genre = Genre.GOSPEL,
            difficulty = 3, bpm = 72, key = "C", source = ORIGINAL,
            chords = "C:2 C/E:2 F:2 F#dim7:2 C/G:2 A7:2 Dm7:2 G7:2 | C:2 C7/E:2 F:2 Fm:2 C/G:2 A7:2 Dm7:2 G7:2 | C:4 F:2 Fm6:2 C:4",
            style = AccStyle.BASS, rhChordRhythm = "1 1",
            about = "The classic gospel walk-up: the bass climbs C - E - F - F♯ - G - A while diminished and secondary-dominant chords connect " +
                "the main harmonies. Ends with the emotional 'Amen' cadence: IV - iv - I.",
            tips = listOf("F♯°7 is a passing chord between F and C/G.", "A7 is V7/ii - it pulls to Dm7."),
        ),
        Song(
            id = "tango-nights", title = "Tango Nights", composer = ORIGINAL, genre = Genre.LATIN,
            difficulty = 3, bpm = 104, key = "Dm", source = ORIGINAL,
            rh = "A4:q. A4:e D5:q F5:q | E5:q. D5:e C5:h | D5:q. C5:e Bb4:q A4:q | A4:h. R:q | F5:q. E5:e D5:q A5:q | G5:q. F5:e E5:h | " +
                "F5:q. E5:e D5:q C#5:q | D5:w",
            chords = "Dm:4 | C:4 | Bb:4 | A:4 | Dm:4 | C:4 | Bb:2 A:2 | Dm:4",
            style = AccStyle.ROOT_CHORD,
            about = "Built on the Andalusian cadence - a descending bass i-♭VII-♭VI-V (D-C-B♭-A) - heard in flamenco, tango and countless film scores.",
        ),
    )
}

internal fun contemporarySongs(): List<Song> = listOf(
    Song(
        id = "rainfall", title = "Rainfall", composer = ORIGINAL, genre = Genre.CONTEMPORARY,
        difficulty = 2, bpm = 76, key = "Am", source = ORIGINAL,
        rh = "E5:w | C5:w | E5:h G5:h | D5:w | E5:h. D5:q | C5:h A4:h | G4:h C5:h | B4:w | A4:w",
        chords = "Am:4 | F:4 | C:4 | G:4 | Am:4 | F:4 | C:4 | G:4 | Am:4",
        style = AccStyle.ARPEGGIO,
        about = "Modern 'neo-classical' piano (think Einaudi or Yiruma): a simple melody over flowing left-hand arpeggios and the vi-IV-I-V progression.",
        tips = listOf("Hold the sustain pedal through each bar, change on the next chord.", "Let the melody notes ring - no rush."),
    ),
    Song(
        id = "morning-light", title = "Morning Light", composer = ORIGINAL, genre = Genre.CONTEMPORARY,
        difficulty = 2, bpm = 84, key = "C", source = ORIGINAL,
        rh = "G4:q C5 D5 E5 | D5:h. B4:q | C5:q A4 B4 C5 | A4:w | G4:q C5 D5 E5 | G5:h. E5:q | F5:q E5 D5 C5 | C5:w",
        chords = "C:4 | G/B:4 | Am:4 | F:4 | C:4 | Em:4 | F:2 G:2 | C:4",
        style = AccStyle.ARPEGGIO,
        about = "A descending bass line (C-B-A) under a rising melody. G/B is a slash chord: G major with B in the bass.",
    ),
    Song(
        id = "neon-horizon", title = "Neon Horizon", composer = ORIGINAL, genre = Genre.CONTEMPORARY,
        difficulty = 2, bpm = 100, key = "Am", source = ORIGINAL,
        rh = "E5:q. E5:q. D5:q | C5:q. C5:q. A4:q | G4:q. C5:q. E5:q | D5:w | E5:q. E5:q. G5:q | A5:q. G5:q. E5:q | D5:q. C5:q. D5:q | C5:w",
        chords = "Am:4 | F:4 | C:4 | G:4 | Am:4 | F:4 | G:4 | C:4",
        style = AccStyle.BASS_332,
        about = "Modern pop rhythm: the 3-3-2 'tresillo' pattern (hits on 1, the 'and' of 2, and 4) drives countless chart hits.",
    ),
)

/** The chord progressions behind famous songs - for learning how pop harmony works. */
internal fun exerciseSongs(): List<Song> {
    /** Hanon No. 1: the 8-note figure climbs one white key at a time for an octave, then mirrors back down. */
    fun hanon(): String {
        val white = listOf("C", "D", "E", "F", "G", "A", "B")
        fun w(i: Int) = white[i % 7] + (4 + i / 7)
        val up = (0..6).flatMap { s -> listOf(s, s + 2, s + 3, s + 4, s + 5, s + 4, s + 3, s + 2) }
        val down = (11 downTo 5).flatMap { t -> listOf(t, t - 2, t - 3, t - 4, t - 5, t - 4, t - 3, t - 2) }
        return (up + down).mapIndexed { i, n -> if (i == 0) "${w(n)}:s" else w(n) }.joinToString(" ") + " C4:h"
    }
    return listOf(
        Song(
            id = "ex-five-finger-c", title = "Five-Finger Pattern in C", composer = "Exercise", genre = Genre.EXERCISE,
            difficulty = 1, bpm = 90, key = "C", source = "Exercise",
            rh = "{ C4:q D4 E4 F4 | G4 F4 E4 D4 | }*2 C4:w",
            lh = "{ C3:q D3 E3 F3 | G3 F3 E3 D3 | }*2 C3:w",
            about = "Both hands play the same five notes, one octave apart. Right thumb on C4, left pinky on C3.",
            tips = listOf("Right hand fingers 1-2-3-4-5; left hand fingers 5-4-3-2-1.", "Keep every finger curved and touching its key."),
        ),
        Song(
            id = "ex-c-scale", title = "C Major Scale - 2 octaves", composer = "Exercise", genre = Genre.EXERCISE,
            difficulty = 2, bpm = 80, key = "C", source = "Exercise",
            rh = "C4:e D4 E4 F4 G4 A4 B4 C5 D5 E5 F5 G5 A5 B5 C6 B5 A5 G5 F5 E5 D5 C5 B4 A4 G4 F4 E4 D4 C4:h.",
            lh = "C3:e D3 E3 F3 G3 A3 B3 C4 D4 E4 F4 G4 A4 B4 C5 B4 A4 G4 F4 E4 D4 C4 B3 A3 G3 F3 E3 D3 C3:h.",
            about = "Right hand fingering: 1-2-3 (thumb under) 1-2-3-4 (thumb under) 1-2-3 (thumb under) 1-2-3-4-5. Left hand: 5-4-3-2-1 (3 over) 3-2-1 (4 over)...",
        ),
        Song(
            id = "ex-hanon-1", title = "Hanon Exercise No. 1", composer = "Charles-Louis Hanon (1873)", genre = Genre.EXERCISE,
            difficulty = 2, bpm = 72, key = "C", source = "Public domain",
            rh = hanon(),
            about = "From 'The Virtuoso Pianist'. The same 8-note shape climbs one step at a time then comes back down - building finger independence and evenness.",
            tips = listOf("Lift each finger and play with equal strength.", "Start at 60 bpm and increase by 4 bpm each day."),
        ),
        Song(
            id = "ex-chromatic", title = "Chromatic Scale", composer = "Exercise", genre = Genre.EXERCISE,
            difficulty = 2, bpm = 72, key = "C", source = "Exercise",
            rh = "C4:e C#4 D4 D#4 E4 F4 F#4 G4 G#4 A4 A#4 B4 C5 B4 Bb4 A4 Ab4 G4 Gb4 F4 E4 Eb4 D4 Db4 C4:h.",
            about = "Every key, white and black. Fingering: 3 on every black key, 1 on white keys, except 2 on F and C... (E-F and B-C use 1-2).",
        ),
        Song(
            id = "ex-arpeggio-c", title = "C Major Arpeggio", composer = "Exercise", genre = Genre.EXERCISE,
            difficulty = 2, bpm = 80, key = "C", source = "Exercise",
            rh = "C4:e E4 G4 C5 E5 G5 C6 G5 E5 C5 G4 E4 C4:h",
            lh = "C3:e E3 G3 C4 E4 G4 C5 G4 E4 C4 G3 E3 C3:h",
            about = "Arpeggio = a chord played one note at a time. Fingering RH: 1-2-3-1-2-3-5. The thumb passes under smoothly.",
        ),
        Song(
            id = "ex-chord-inversions", title = "Triad Inversions Up the Scale", composer = "Exercise", genre = Genre.EXERCISE,
            difficulty = 2, bpm = 76, key = "C", source = "Exercise",
            rh = "C4+E4+G4:q E4+G4+C5 G4+C5+E5 C5+E5+G5 | G4+C5+E5 E4+G4+C5 C4+E4+G4:h | F4+A4+C5:q A4+C5+F5 C5+F5+A5 F5+A5+C6 | " +
                "C5+F5+A5 A4+C5+F5 F4+A4+C5:h | G4+B4+D5:q B4+D5+G5 D5+G5+B5 G5+B5+D6 | D5+G5+B5 B4+D5+G5 G4+B4+D5:h | C4+E4+G4:w",
            lh = "C3:w | C3:w | F2:w | F2:w | G2:w | G2:w | C3:w",
            about = "Root position, 1st inversion, 2nd inversion, root position - up and back - for the three primary chords C, F and G.",
        ),
    )
}
