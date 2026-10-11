package com.pianoquest.app.data.songs

import com.pianoquest.app.data.AccStyle
import com.pianoquest.app.data.Genre
import com.pianoquest.app.data.Song

internal fun folkSongs(): List<Song> = listOf(
    Song(
        id = "hot-cross-buns", title = "Hot Cross Buns", composer = "Traditional", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 96, key = "C",
        rh = "E4:q D4 C4:h | E4:q D4 C4:h | C4:e C4 C4 C4 D4 D4 D4 D4 | E4:q D4 C4:h",
        chords = "C:4 | C:4 | C:2 G:2 | C:4", style = AccStyle.BASS,
        about = "Three notes - E, D and C - played by the right hand's fingers 3, 2 and 1. The perfect very first song.",
        tips = listOf("Put your thumb (finger 1) on middle C.", "Keep your fingers curved like holding a bubble."),
    ),
    Song(
        id = "mary-lamb", title = "Mary Had a Little Lamb", composer = "Traditional (Sarah J. Hale, 1830)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 108, key = "C",
        rh = "E4:q D4 C4 D4 | E4 E4 E4:h | D4:q D4 D4:h | E4:q G4 G4:h | E4:q D4 C4 D4 | E4 E4 E4 E4 | D4 D4 E4 D4 | C4:w",
        chords = "C:4 | C:4 | G:4 | C:4 | C:4 | C:4 | G:4 | C:4", style = AccStyle.BASS,
        about = "Uses only C-D-E-G. Notice how the melody repeats - spotting repetition makes learning faster.",
        tips = listOf("Fingers: C=1, D=2, E=3, G=5.", "Count '1-2-3-4' out loud for the long notes."),
    ),
    Song(
        id = "twinkle", title = "Twinkle, Twinkle, Little Star", composer = "Traditional (French melody, 1761)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 100, key = "C",
        rh = "C4:q C4 G4 G4 | A4 A4 G4:h | F4:q F4 E4 E4 | D4 D4 C4:h | G4:q G4 F4 F4 | E4 E4 D4:h | " +
            "G4:q G4 F4 F4 | E4 E4 D4:h | C4:q C4 G4 G4 | A4 A4 G4:h | F4:q F4 E4 E4 | D4 D4 C4:h",
        chords = "C:4 | F:2 C:2 | F:2 C:2 | G:2 C:2 | C:2 F:2 | C:2 G:2 | C:2 F:2 | C:2 G:2 | C:4 | F:2 C:2 | F:2 C:2 | G:2 C:2",
        style = AccStyle.BLOCK,
        about = "Mozart wrote twelve variations on this melody! It starts with a leap of a perfect 5th (C up to G).",
        tips = listOf("Shift your hand: thumb on C, then stretch finger 5 to G and 6th note A with a small hand shift.",
            "Left hand plays just three chords: C, F and G - the I, IV and V chords."),
    ),
    Song(
        id = "frere-jacques", title = "Frère Jacques", composer = "Traditional (French)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 104, key = "C",
        rh = "C4:q D4 E4 C4 | C4 D4 E4 C4 | E4 F4 G4:h | E4:q F4 G4:h | G4:e A4 G4 F4 E4:q C4 | G4:e A4 G4 F4 E4:q C4 | C4 G3 C4:h | C4:q G3 C4:h",
        chords = "C:4 | C:4 | C:4 | C:4 | C:4 | C:4 | C:2 G:1 C:1 | C:2 G:1 C:1", style = AccStyle.BASS,
        about = "A round: one person starts, and a second person starts the same tune two bars later. Try it with a friend!",
    ),
    Song(
        id = "london-bridge", title = "London Bridge Is Falling Down", composer = "Traditional (English)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 110, key = "C",
        rh = "G4:q. A4:e G4:q F4 | E4 F4 G4:h | D4:q E4 F4:h | E4:q F4 G4:h | G4:q. A4:e G4:q F4 | E4 F4 G4:h | D4:h G4:h | E4:q C4:h.",
        chords = "C:4 | C:4 | G:4 | C:4 | C:4 | C:4 | G:4 | C:4", style = AccStyle.ROOT_CHORD,
        about = "Your first dotted rhythm: a dotted quarter (1½ beats) followed by an eighth (½ beat).",
    ),
    Song(
        id = "au-clair", title = "Au Clair de la Lune", composer = "Traditional (French, 18th c.)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 100, key = "C",
        rh = "{ C4:q C4 C4 D4 | E4:h D4:h | C4:q E4 D4 D4 | C4:w | }*2 D4:q D4 D4 D4 | A3:h A3:h | D4:q C4 B3 A3 | G3:w | " +
            "C4:q C4 C4 D4 | E4:h D4:h | C4:q E4 D4 D4 | C4:w",
        chords = "{ C:4 | C:2 G:2 | C:2 G:2 | C:4 | }*2 G:4 | Dm:4 | G:2 D:2 | G:4 | C:4 | C:2 G:2 | C:2 G:2 | C:4",
        style = AccStyle.BLOCK,
        about = "AABA form: the opening phrase appears three times with a contrasting B section in the middle.",
    ),
    Song(
        id = "row-boat", title = "Row, Row, Row Your Boat", composer = "Traditional", genre = Genre.BEGINNER,
        difficulty = 2, bpm = 96, meterNum = 6, meterDen = 8, key = "C",
        rh = "C4:q. C4:q. | C4:q D4:e E4:q. | E4:q D4:e E4:q F4:e | G4:h. | C5:e C5 C5 G4 G4 G4 | E4 E4 E4 C4 C4 C4 | G4:q F4:e E4:q D4:e | C4:h.",
        chords = "C:3 | C:3 | C:3 | C:3 | C:3 | C:3 | G:3 | C:3", style = AccStyle.BASS,
        about = "Written in 6/8: two big beats per bar, each divided into three. Feel it as 'ONE-and-a TWO-and-a'.",
    ),
    Song(
        id = "old-macdonald", title = "Old MacDonald Had a Farm", composer = "Traditional", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 116, key = "G",
        rh = "G4:q G4 G4 D4 | E4 E4 D4:h | B4:q B4 A4 A4 | G4:h. D4:q | G4 G4 G4 D4 | E4 E4 D4:h | B4:q B4 A4 A4 | G4:w",
        chords = "G:4 | C:2 G:2 | G:2 D:2 | G:4 | G:4 | C:2 G:2 | G:2 D:2 | G:4", style = AccStyle.ROOT_CHORD,
        about = "In G major - the key with one sharp (F♯) - though this melody never uses the F♯!",
    ),
    Song(
        id = "yankee-doodle", title = "Yankee Doodle", composer = "Traditional (American)", genre = Genre.FOLK,
        difficulty = 2, bpm = 120, key = "C",
        rh = "C4:q C4 D4 E4 | C4 E4 D4 G3 | C4 C4 D4 E4 | C4:h B3:h | C4:q C4 D4 E4 | F4 E4 D4 C4 | B3 G3 A3 B3 | C4:h C4:h",
        chords = "C:4 | C:2 G:2 | C:4 | C:2 G:2 | C:4 | F:2 C:2 | G:4 | C:4", style = AccStyle.ROOT_CHORD,
        about = "Notice the melody dips below middle C to G3 and B3 - your thumb has to cross under or your hand shifts.",
    ),
    Song(
        id = "ode-to-joy", title = "Ode to Joy", composer = "Ludwig van Beethoven (Symphony No. 9, 1824)", genre = Genre.BEGINNER,
        difficulty = 1, bpm = 108, key = "C",
        rh = "E4:q E4 F4 G4 | G4 F4 E4 D4 | C4 C4 D4 E4 | E4:q. D4:e D4:h | E4:q E4 F4 G4 | G4 F4 E4 D4 | C4 C4 D4 E4 | D4:q. C4:e C4:h | " +
            "D4:q D4 E4 C4 | D4 E4:e F4:e E4:q C4 | D4 E4:e F4:e E4:q D4 | C4 D4 G3:h | E4:q E4 F4 G4 | G4 F4 E4 D4 | C4 C4 D4 E4 | D4:q. C4:e C4:h",
        chords = "C:4 | G:4 | C:4 | C:2 G:2 | C:4 | G:4 | C:4 | G:2 C:2 | G:2 C:2 | G:2 C:2 | G:2 C:2 | C:2 G:2 | C:4 | G:4 | C:4 | G:2 C:2",
        style = AccStyle.BLOCK,
        about = "Beethoven's melody moves almost entirely by step - great for learning smooth five-finger playing.",
        tips = listOf("Watch the dotted rhythm at the end of each phrase.", "Bar 12 drops to G3: stretch your thumb down."),
    ),
    Song(
        id = "saints", title = "When the Saints Go Marching In", composer = "Traditional (Spiritual)", genre = Genre.FOLK,
        difficulty = 2, bpm = 126, key = "C",
        rh = "R:1 C4:q E4 F4 | G4:w | R:1 C4:q E4 F4 | G4:w | R:1 C4:q E4 F4 | G4:h E4:h | C4:h E4:h | D4:w | " +
            "R:1 E4:q E4 D4 | C4:h. C4:q | E4:h G4:q G4:q | F4:w | R:1 E4:q F4 G4 | E4:h C4:h | D4:w | C4:w",
        chords = "C:4 | C:4 | C:4 | C:4 | C:4 | C:4 | C:4 | G:4 | G:4 | C:4 | C7:4 | F:4 | C:4 | C:4 | G:4 | C:4",
        style = AccStyle.STRIDE,
        about = "A New Orleans jazz standard. The left hand plays a 'stride' pattern: bass note, chord, bass note, chord.",
    ),
    Song(
        id = "greensleeves", title = "Greensleeves", composer = "Traditional (English, 16th c.)", genre = Genre.FOLK,
        difficulty = 3, bpm = 112, meterNum = 3, key = "Am", pickup = 1.0,
        rh = "A4:q | C5:h D5:q | E5:q. F5:e E5:q | D5:h B4:q | G4:q. A4:e B4:q | C5:h A4:q | A4:q. G#4:e A4:q | B4:h G#4:q | E4:h A4:q | " +
            "C5:h D5:q | E5:q. F5:e E5:q | D5:h B4:q | G4:q. A4:e B4:q | C5:q. B4:e A4:q | G#4:q. F#4:e G#4:q | A4:h. | " +
            "G5:h. | G5:q. F#5:e E5:q | D5:h B4:q | G4:q. A4:e B4:q | C5:h A4:q | A4:q. G#4:e A4:q | B4:h G#4:q | E4:h. | " +
            "G5:h. | G5:q. F#5:e E5:q | D5:h B4:q | G4:q. A4:e B4:q | C5:q. B4:e A4:q | G#4:q. F#4:e G#4:q | A4:h.",
        chords = "R:1 Am:3 C:3 G:3 Em:3 Am:3 E:3 E:3 E:3 Am:3 C:3 G:3 Em:3 Am:3 E:3 Am:3 " +
            "C:3 C:3 G:3 Em:3 Am:3 E:3 E:3 E:3 C:3 C:3 G:3 Em:3 Am:3 E:3 Am:3",
        style = AccStyle.WALTZ,
        about = "A minor melody using the raised 7th (G♯) from the harmonic minor scale - listen for its bittersweet pull to A.",
        tips = listOf("Feel the lilting long-short rhythm (dotted quarter + eighth).", "The G♯ is the 'leading tone' that leans into A."),
    ),
    Song(
        id = "auld-lang-syne", title = "Auld Lang Syne", composer = "Traditional (Scottish)", genre = Genre.FOLK,
        difficulty = 2, bpm = 92, key = "F", pickup = 1.0,
        rh = "C4:q | F4:q. F4:e F4:q A4:q | G4:q. F4:e G4:q A4:e G4:e | F4:q. F4:e A4:q C5:q | D5:h. D5:q | " +
            "C5:q. A4:e A4:q F4:q | G4:q. F4:e G4:q A4:e G4:e | F4:q. D4:e D4:q C4:q | F4:h.",
        chords = "R:1 F:4 | C7:4 | F:4 | Bb:4 | F:4 | C7:4 | Dm:2 C:2 | F:4",
        style = AccStyle.ROOT_CHORD,
        about = "This melody uses the major pentatonic scale (F G A C D) - no B♭ or E appear in the tune at all.",
    ),
    Song(
        id = "happy-birthday", title = "Happy Birthday", composer = "Mildred & Patty Hill (public domain)", genre = Genre.BEGINNER,
        difficulty = 2, bpm = 100, meterNum = 3, key = "C", pickup = 1.0,
        rh = "G4:e. G4:s | A4:q G4 C5 | B4:h G4:e. G4:s | A4:q G4 D5 | C5:h G4:e. G4:s | G5:q E5 C5 | B4 A4 F5:e. F5:s | E5:q C5 D5 | C5:h.",
        chords = "R:1 C:3 | G:3 | G7:3 | C:3 | C:3 | F:3 | C:2 G:1 | C:3", style = AccStyle.WALTZ,
        about = "Starts with a pickup (anacrusis): the first two notes come before the first full bar.",
        tips = listOf("The big leap to G5 in bar 5 is an octave - prepare your hand early."),
    ),
)

internal fun holidaySongs(): List<Song> = listOf(
    Song(
        id = "jingle-bells", title = "Jingle Bells (chorus)", composer = "James Lord Pierpont (1857)", genre = Genre.HOLIDAY,
        difficulty = 1, bpm = 120, key = "C",
        rh = "E4:q E4 E4:h | E4:q E4 E4:h | E4:q G4 C4:q. D4:e | E4:w | F4:q F4 F4:q. F4:e | F4:q E4 E4 E4:e E4:e | E4:q D4 D4 E4 | D4:h G4:h | " +
            "E4:q E4 E4:h | E4:q E4 E4:h | E4:q G4 C4:q. D4:e | E4:w | F4:q F4 F4:q. F4:e | F4:q E4 E4 E4:e E4:e | G4:q G4 F4 D4 | C4:w",
        chords = "C:4 | C:4 | C:4 | C:4 | F:4 | C:4 | D7:4 | G:4 | C:4 | C:4 | C:4 | C:4 | F:4 | C:4 | G7:4 | C:4",
        style = AccStyle.ROOT_CHORD,
    ),
    Song(
        id = "silent-night", title = "Silent Night", composer = "Franz Xaver Gruber (1818)", genre = Genre.HOLIDAY,
        difficulty = 2, bpm = 84, meterNum = 6, meterDen = 8, key = "C",
        rh = "G4:q. A4:e G4:q | E4:h. | G4:q. A4:e G4:q | E4:h. | D5:h D5:q | B4:h. | C5:h C5:q | G4:h. | " +
            "A4:h A4:q | C5:q. B4:e A4:q | G4:q. A4:e G4:q | E4:h. | A4:h A4:q | C5:q. B4:e A4:q | G4:q. A4:e G4:q | E4:h. | " +
            "D5:h D5:q | F5:q. D5:e B4:q | C5:h. | E5:h. | C5:q. G4:e E4:q | G4:q. F4:e D4:q | C4:h. | C4:h.",
        chords = "C:3 C:3 C:3 C:3 G:3 G:3 C:3 C:3 F:3 F:3 C:3 C:3 F:3 F:3 C:3 C:3 G:3 G7:3 C:3 C:3 C:3 G7:3 C:3 C:3",
        style = AccStyle.WALTZ,
        about = "A gentle lullaby in compound time (6/8). Keep the left hand soft so the melody sings.",
    ),
    Song(
        id = "deck-the-halls", title = "Deck the Halls", composer = "Traditional (Welsh)", genre = Genre.HOLIDAY,
        difficulty = 2, bpm = 116, key = "C",
        rh = "{ G4:q. F4:e E4:q D4:q | C4 D4 E4 C4 | D4:e E4:e F4:e D4:e E4:q. D4:e | C4:q B3:q C4:h | }*2 " +
            "D4:q. E4:e F4:q D4:q | E4:q. F4:e G4:q D4:q | E4:e F#4:e G4:q A4:e B4:e C5:q | B4:q A4:q G4:h | " +
            "G4:q. F4:e E4:q D4:q | C4 D4 E4 C4 | A4:e A4:e A4:e A4:e G4:q. F4:e | E4:q D4:q C4:h",
        chords = "{ C:4 | C:2 G:2 | G:2 C:2 | G:2 C:2 | }*2 G:4 | C:2 G:2 | C:2 D:2 | D:2 G:2 | C:4 | C:2 F:2 | F:2 C:2 | G:2 C:2",
        style = AccStyle.ROOT_CHORD,
    ),
    Song(
        id = "merry-christmas", title = "We Wish You a Merry Christmas", composer = "Traditional (English)", genre = Genre.HOLIDAY,
        difficulty = 2, bpm = 120, meterNum = 3, key = "C", pickup = 1.0,
        rh = "G4:q | C5 C5:e D5:e C5:e B4:e | A4:q A4 A4 | D5 D5:e E5:e D5:e C5:e | B4:q G4 G4 | E5 E5:e F5:e E5:e D5:e | C5:q A4 G4:e G4:e | A4:q D5 B4 | C5:h.",
        chords = "R:1 C:3 | F:3 | D7:3 | G:3 | E7:3 | Am:3 | F:1 G:2 | C:3", style = AccStyle.WALTZ,
    ),
)
