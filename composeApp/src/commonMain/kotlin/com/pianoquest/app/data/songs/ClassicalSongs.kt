package com.pianoquest.app.data.songs

import com.pianoquest.app.data.AccStyle
import com.pianoquest.app.data.Genre
import com.pianoquest.app.data.Song

/** Hand-arranged Romantic pieces (the complete classical scores live in MidiSongs.kt). */
internal fun romanticSongs(): List<Song> = listOf(
    Song(
        id = "brahms-lullaby", title = "Lullaby (Wiegenlied)", composer = "Johannes Brahms, Op. 49 No. 4 (1868)", genre = Genre.ROMANTIC,
        difficulty = 2, bpm = 92, meterNum = 3, key = "C", pickup = 1.0,
        rh = "E4:e E4 | G4:h E4:e E4 | G4:h E4:e G4 | C5:q B4:q. A4:e | A4:q G4 D4:e E4 | F4:q D4 D4:e E4 | F4:h D4:e F4 | " +
            "B4:e A4 G4:q B4 | C5:h C4:e C4 | C5:h A4:e F4 | G4:h E4:e C4 | F4:q G4 A4 | E4:h C4:e C4 | C5:h A4:e F4 | G4:h E4:e C4 | F4:q E4 D4 | C4:h.",
        chords = "R:1 C:3 C:3 G:3 G:3 G7:3 G7:3 G7:3 C:3 F:3 C:3 G7:3 C:3 F:3 C:3 G7:3 C:3",
        style = AccStyle.WALTZ,
        about = "Brahms wrote this cradle song for a friend's new baby. A gentle waltz: bass on beat 1, soft chords on 2 and 3.",
        tips = listOf("Play the left-hand chords very softly - 'pianissimo'.", "Shape each phrase like a breath: grow, then relax."),
    ),    Song(
        id = "nocturne-study", title = "Nocturne Study in E♭", composer = "Keyscape original (in the style of Chopin & Field)", genre = Genre.ROMANTIC,
        difficulty = 3, bpm = 60, meterNum = 12, meterDen = 8, key = "Eb", source = "Original composition",
        rh = "G5:q. F5:e G5:e F5:e Eb5:q. Bb4:q. | C5:q. D5:e Eb5:e F5:e G5:h. | Ab5:q. G5:e F5:e Eb5:e D5:q. Bb4:q. | " +
            "Eb5:q. F5:e G5:e Ab5:e G5:q. F5:q. | G5:q. F5:e G5:e Bb5:e Ab5:q. G5:q. | F5:q. Eb5:e F5:e G5:e C5:h. | " +
            "D5:q. Eb5:e F5:e Ab5:e G5:q. F5:q. | Eb5:w.",
        lh = "{ Eb2:e Bb2 G3 Bb3 G3 Bb2 }*2 | { Ab2:e Eb3 C4 Eb4 C4 Eb3 }*2 | { Bb1:e F2 D3 Ab3 D3 F2 }*2 | { Eb2:e Bb2 G3 Bb3 G3 Bb2 }*2 | " +
            "{ C2:e G2 Eb3 Bb3 Eb3 G2 }*2 | { F2:e C3 A3 Eb4 A3 C3 }*2 | { Bb1:e F2 D3 Ab3 D3 F2 }*2 | Eb2+Bb2+G3:w.",
        about = "Romantic nocturnes put a singing melody over wide, rippling left-hand arpeggios spanning more than an octave - " +
            "a texture Chopin made famous. This study lets you practise exactly that.",
        tips = listOf("Use the sustain pedal, changing it at each new bass note.", "Let the melody 'sing' above the quieter accompaniment (voicing).",
            "Small rubato is welcome: stretch the top of a phrase, catch up after."),
    ),
)
