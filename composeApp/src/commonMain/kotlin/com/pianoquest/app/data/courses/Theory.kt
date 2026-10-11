package com.pianoquest.app.data.courses

import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Visual
import com.pianoquest.app.data.Illustration
import com.pianoquest.app.data.picture
import com.pianoquest.app.data.chords
import com.pianoquest.app.data.course
import com.pianoquest.app.data.kb
import com.pianoquest.app.data.progression
import com.pianoquest.app.data.staff
import com.pianoquest.app.data.symbols
import com.pianoquest.app.data.table

internal fun theoryCourse() = course(
    id = "theory", title = "Music Theory A-Z", subtitle = "Intervals to modulation - every concept, on the keyboard",
    description = """
        The complete theory toolkit for pianists: intervals, scales, keys and the circle of fifths, every chord type,
        chord progressions (circle, loop, linear, pedal...), cadences, musical form, advanced harmony, modes and notation.
        Every idea is shown on the keyboard and staff, and you can hear all of it.
    """,
    level = "Beginner → Advanced", color = 0xFF4A2416, icon = CourseIcon.THEORY, recommendedAfter = "foundations",
) {
    unit("intervals", "Intervals", "The building blocks of melody and harmony.") {
        lesson("what", "What Is an Interval?", "Measuring the distance between two notes.") {
            read("Interval = distance", """
                An **interval** is the distance between two notes.
                - **Melodic** interval: the notes are played one after the other.
                - **Harmonic** interval: played together.

                Intervals have a **number** and a **quality**. The number counts letter names, *including both ends*: C up to E is C-D-E = a **3rd**. C up to G = C-D-E-F-G = a **5th**.
            """, visual = staff("C4:h E4 | C4+E4:w", names = true, caption = "Melodic 3rd, then harmonic 3rd"), play = "C4:h E4 C4+E4:w")
            quiz("C up to F is what number?", "4th", "3rd", "5th", "6th", explain = "C-D-E-F = four letters.")
            quiz("D up to A is a…", "5th", "4th", "6th", "3rd", explain = "D-E-F-G-A = five letters.")
            quiz("E up to C is a…", "6th", "5th", "7th", "4th", explain = "E-F-G-A-B-C = six letters.")
            quiz("Two notes played at the same time form a…", "Harmonic interval", "Melodic interval", "Scale", "Rhythm")
        }
        lesson("quality", "Major, Minor & Perfect", "Interval quality is all about half steps.") {
            read("Counting half steps", """
                Two 3rds can sound very different: C-E is bright, C-E♭ is dark. Quality tells them apart, by counting **half steps**:
            """, visual = table(listOf("Interval", "Half steps", "Example from C", "Sound"),
                listOf("Unison (P1)", "0", "C-C", "Same note"),
                listOf("Minor 2nd (m2)", "1", "C-D♭", "Tense, crunchy"),
                listOf("Major 2nd (M2)", "2", "C-D", "Step, mild"),
                listOf("Minor 3rd (m3)", "3", "C-E♭", "Sad, dark"),
                listOf("Major 3rd (M3)", "4", "C-E", "Happy, bright"),
                listOf("Perfect 4th (P4)", "5", "C-F", "Open, hymn-like"),
                listOf("Tritone (A4/d5)", "6", "C-F♯ / C-G♭", "Restless, 'devil's interval'"),
                listOf("Perfect 5th (P5)", "7", "C-G", "Strong, hollow"),
                listOf("Minor 6th (m6)", "8", "C-A♭", "Bittersweet"),
                listOf("Major 6th (M6)", "9", "C-A", "Warm, sweet"),
                listOf("Minor 7th (m7)", "10", "C-B♭", "Bluesy, unresolved"),
                listOf("Major 7th (M7)", "11", "C-B", "Dreamy, yearning"),
                listOf("Octave (P8)", "12", "C-C", "Same note, higher")))
            read("The rules", """
                - **Unisons, 4ths, 5ths and octaves** are **perfect** (P). Make them a half step bigger → *augmented*; smaller → *diminished*.
                - **2nds, 3rds, 6ths and 7ths** are **major** (M) or **minor** (m). A minor interval is one half step smaller than major.

                > Shortcut: in a **major scale**, every interval up from the tonic is major or perfect.
            """)
            read("Hear the difference", "Major 3rd, then minor 3rd:", visual = kb("C4", "B4", "C4", "E4", "Eb4"), play = "C4+E4:h R:1 C4+Eb4:h")
            quiz("How many half steps in a perfect 5th?", "7", "5", "6", "8")
            quiz("C to A is a…", "Major 6th", "Minor 6th", "Perfect 5th", "Major 7th")
            quiz("D to F is a…", "Minor 3rd", "Major 3rd", "Perfect 4th", "Major 2nd", explain = "D-E♭-E-F = 3 half steps → minor 3rd.")
            quiz("Which interval is 6 half steps?", "Tritone", "Perfect 5th", "Minor 6th", "Perfect 4th")
            drill(DrillKind.INTERVAL_STAFF, count = 6, level = 1)
        }
        lesson("by-ear", "Intervals by Ear", "Use famous songs to recognise intervals.") {
            read("Reference songs", "Singing the start of a familiar song is the fastest way to recognise an interval:", visual = table(
                listOf("Interval", "Ascending reference"),
                listOf("Minor 2nd", "Jaws theme"), listOf("Major 2nd", "Happy Birthday"), listOf("Minor 3rd", "Greensleeves"),
                listOf("Major 3rd", "When the Saints"), listOf("Perfect 4th", "Here Comes the Bride / Amazing Grace"),
                listOf("Tritone", "The Simpsons / Maria"), listOf("Perfect 5th", "Twinkle Twinkle"),
                listOf("Minor 6th", "The Entertainer"), listOf("Major 6th", "My Bonnie"),
                listOf("Minor 7th", "Star Trek (original theme)"), listOf("Major 7th", "Take On Me (chorus)"),
                listOf("Octave", "Somewhere Over the Rainbow")))
            ear("Which interval is this?", "C4:h G4:h", "Perfect 5th", "Perfect 4th", "Major 3rd", "Octave")
            ear("Which interval?", "C4:h E4:h", "Major 3rd", "Minor 3rd", "Perfect 4th", "Major 2nd")
            ear("Which interval?", "C4:h C5:h", "Octave", "Perfect 5th", "Major 7th", "Major 6th")
            drill(DrillKind.INTERVAL_EAR, count = 6, level = 1)
        }
        lesson("inversions", "Inverting & Compound Intervals", "Flip an interval; stretch past the octave.") {
            read("Inverting intervals", """
                Move the bottom note up an octave and the interval **inverts**. Two rules:
                - The numbers add up to **9**: a 3rd becomes a 6th, a 2nd becomes a 7th, a 4th becomes a 5th.
                - Major ↔ minor, augmented ↔ diminished, **perfect stays perfect**.

                So C-E (M3) inverts to E-C (m6).
            """, visual = staff("C4+E4:h E4+C5:h", names = true), play = "C4+E4:h E4+C5:h")
            read("Compound intervals", """
                Intervals bigger than an octave are **compound**: a 9th = octave + 2nd, a 10th = octave + 3rd, 11th = octave + 4th, 13th = octave + 6th.

                These names matter for jazz chords like C9, C11 and C13.
            """, visual = staff("C4+D5:w", names = true))
            quiz("A major 3rd inverts to a…", "Minor 6th", "Major 6th", "Minor 3rd", "Perfect 5th")
            quiz("A perfect 4th inverts to a…", "Perfect 5th", "Diminished 5th", "Major 5th", "Perfect 4th")
            quiz("A 10th is an octave plus a…", "3rd", "2nd", "4th", "5th")
        }
        lesson("consonance", "Consonance & Dissonance", "Why some intervals feel restful and others tense.") {
            read("Stable vs tense", """
                - **Perfect consonances**: unison, octave, perfect 5th (and the 4th, in some contexts) - very stable, hollow.
                - **Imperfect consonances**: 3rds and 6ths - sweet and full; the sound of harmony.
                - **Dissonances**: 2nds, 7ths, the tritone - tense, wanting to **resolve** to a consonance.

                Dissonance isn't "bad" - it creates movement. Music breathes by building tension and releasing it.
            """, play = "B4+F5:h C5+E5:h", bpm = 70)
            quiz("Which is a dissonance?", "Major 7th", "Perfect 5th", "Major 3rd", "Octave")
            quiz("3rds and 6ths are called…", "Imperfect consonances", "Perfect consonances", "Dissonances", "Tritones")
        }
        test {
            drill(DrillKind.INTERVAL_STAFF, count = 5, level = 2)
            drill(DrillKind.INTERVAL_EAR, count = 4, level = 1)
            quiz("A minor 7th inverts to a…", "Major 2nd", "Minor 2nd", "Major 7th", "Perfect 5th")
            quiz("How many half steps in a major 6th?", "9", "8", "10", "7")
            quiz("Which intervals can be 'perfect'?", "Unison, 4th, 5th, octave", "2nd, 3rd, 6th, 7th", "Only the octave", "All of them")
        }
    }

    unit("scales", "Scales & Keys", "Major and minor scales, key signatures, the circle of fifths.") {
        lesson("major-scale", "The Major Scale", "The W-W-H-W-W-W-H formula.") {
            read("Do-Re-Mi", """
                A **scale** is a set of notes in order. The **major scale** is the most important one - it's "do re mi fa sol la ti do".

                Its secret is the pattern of whole (W) and half (H) steps:

                **W - W - H - W - W - W - H**

                On C, this gives only white keys: C D E F G A B C. Half steps fall between E-F and B-C.
            """, visual = kb("C4", "C5", "C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5"), play = "C4:e D4 E4 F4 G4 A4 B4 C5:q")
            read("Building G major", """
                Start on G and follow the formula: G (W) A (W) B (H) C (W) D (W) E (W) **F♯** (H) G.

                We need **F♯** to keep the pattern - so G major has one sharp.
            """, visual = kb("G3", "G4", "G3", "A3", "B3", "C4", "D4", "E4", "F#4", "G4"), play = "G3:e A3 B3 C4 D4 E4 F#4 G4:q")
            read("Scale degrees", "Each note of the scale has a number and a name:", visual = table(listOf("Degree", "Name", "In C"),
                listOf("1 (^1)", "Tonic - home", "C"), listOf("2", "Supertonic", "D"), listOf("3", "Mediant", "E"),
                listOf("4", "Subdominant", "F"), listOf("5", "Dominant", "G"), listOf("6", "Submediant", "A"),
                listOf("7", "Leading tone (pulls up to the tonic)", "B")))
            play("Play the F major scale (it needs one flat: B♭). Fingering RH: 1-2-3-4, then thumb under on C: 1-2-3-4.", "F4:q G4 A4 Bb4 C5 D5 E5 F5",
                from = "F4", to = "F5", fingers = listOf(1, 2, 3, 4, 1, 2, 3, 4))
            quiz("The major scale pattern is…", "W W H W W W H", "W H W W H W W", "W W W H W W H", "H W W W H W W")
            quiz("The 5th degree of a scale is called the…", "Dominant", "Subdominant", "Mediant", "Tonic")
            quiz("Which note is the leading tone in G major?", "F♯", "F", "G", "D")
            drill(DrillKind.PLAY_SCALE, count = 2, level = 1)
            drill(DrillKind.SCALE_DEGREE, count = 4)
        }
        lesson("key-signatures", "Key Signatures", "Why sharps and flats appear at the start of every line.") {
            read("Key signatures", """
                Instead of writing F♯ every time in G major, we put a ♯ on the F line at the start of each staff: the **key signature**. It means "every F is F♯ unless told otherwise".

                **Order of sharps**: F C G D A E B - "**F**ather **C**harles **G**oes **D**own **A**nd **E**nds **B**attle"
                **Order of flats**: B E A D G C F - the reverse ("BEAD + Greatest Common Factor")
            """, visual = staff("", keySig = 4, caption = "E major: F♯ C♯ G♯ D♯"))
            read("Finding the key quickly", """
                - **Sharp keys**: the last sharp is the leading tone - go up a half step. Last sharp D♯ → **E major**.
                - **Flat keys**: the second-to-last flat *is* the key. Flats B♭ E♭ A♭ → **E♭ major**. (One flat = F major - memorise it.)
            """, visual = staff("", keySig = -3, caption = "Three flats: E♭ major (or C minor)"))
            quiz("Two sharps (F♯ C♯) is the key of…", "D major", "G major", "A major", "C♯ major")
            quiz("One flat is the key of…", "F major", "B♭ major", "C major", "G major")
            quiz("Which is the third sharp?", "G♯", "D♯", "C♯", "A♯")
            drill(DrillKind.KEY_SIGNATURE, count = 6, level = 1)
        }
        lesson("circle", "The Circle of Fifths", "The map of all 24 keys.") {
            read("Twelve keys in a circle", """
                Arrange the keys so each is a **perfect 5th** above the last: C → G → D → A → E → B → F♯…

                Going **clockwise**, each key adds one **sharp**. Going **counter-clockwise** (up in 4ths), each adds one **flat**: C → F → B♭ → E♭…

                At the bottom, F♯ major (6♯) and G♭ major (6♭) are the same keys spelled differently.
                The inner ring shows each key's **relative minor**.
            """, visual = Visual.CircleOfFifths("C"))
            read("Why it's useful", """
                - Find any key signature instantly.
                - **Neighbouring keys** share 6 of 7 notes - modulation between them sounds smooth.
                - Chords next to each other on the circle sound natural together: the I, IV and V of any key sit side by side.
                - **Circle progressions** (moving counter-clockwise, root down a 5th) are the strongest chord movements in music. More later!
            """, visual = Visual.CircleOfFifths("G"))
            quiz("Going clockwise around the circle, each key adds a…", "Sharp", "Flat", "Natural", "Octave")
            quiz("Which key is a 5th above D?", "A", "G", "E", "B")
            quiz("Which key has 4 flats?", "A♭ major", "E♭ major", "D♭ major", "B♭ major")
            drill(DrillKind.CIRCLE, count = 5)
        }
        lesson("minor", "Minor Scales", "Natural, harmonic and melodic minor.") {
            read("Natural minor", """
                The **natural minor** scale has the pattern **W H W W H W W**. A natural minor uses only white keys: A B C D E F G A.

                Compared with major, it has a **flat 3rd, 6th and 7th** (♭3 ♭6 ♭7).
            """, visual = kb("A3", "A4", "A3", "B3", "C4", "D4", "E4", "F4", "G4", "A4"), play = "A3:e B3 C4 D4 E4 F4 G4 A4:q")
            read("Harmonic minor", """
                Raise the **7th** degree by a half step. In A minor: G becomes **G♯**.

                This creates a strong **leading tone** (G♯ → A) and a major V chord (E major), but leaves an exotic gap of 3 half steps (an augmented 2nd) between F and G♯.
            """, visual = kb("A3", "A4", "A3", "B3", "C4", "D4", "E4", "F4", "G#4", "A4"), play = "A3:e B3 C4 D4 E4 F4 G#4 A4:q")
            read("Melodic minor", """
                To smooth that gap, raise **both the 6th and 7th** going up: A B C D E **F♯ G♯** A. Classically, it descends as natural minor.

                In jazz, melodic minor is used the same way up and down and generates many important modes (like the altered scale).
            """, visual = kb("A3", "A4", "A3", "B3", "C4", "D4", "E4", "F#4", "G#4", "A4"), play = "A3:e B3 C4 D4 E4 F#4 G#4 A4 G4 F4 E4 D4 C4 B3 A3:q")
            ear("Major or minor scale?", "C4:e D4 Eb4 F4 G4 Ab4 Bb4 C5:q", "Minor", "Major")
            ear("Which minor scale? (listen to the top)", "A3:e B3 C4 D4 E4 F4 G#4 A4:q", "Harmonic minor", "Natural minor", "Melodic minor")
            quiz("Harmonic minor raises which degree?", "7th", "6th", "3rd", "6th and 7th")
            play("Play D harmonic minor: D E F G A B♭ C♯ D.", "D4:q E4 F4 G4 A4 Bb4 C#5 D5", from = "C4", to = "E5")
        }
        lesson("relative", "Relative & Parallel Keys", "Two kinds of major/minor relationships.") {
            read("Relative keys", """
                Every major key shares its key signature with a **relative minor**, found **3 half steps (a minor 3rd) below** the tonic - or on the 6th degree.

                - C major ↔ A minor (no sharps or flats)
                - G major ↔ E minor (1♯)
                - F major ↔ D minor (1♭)
            """, visual = Visual.CircleOfFifths("C"))
            read("Parallel keys", """
                **Parallel** keys share the same **tonic** but different signatures: C major and **C minor** (3♭).

                Switching between parallel keys is a powerful emotional effect - a sudden darkening or brightening. Borrowing chords from the parallel key is called **modal mixture** (coming later).
            """, play = "C4+E4+G4:h C4+Eb4+G4:h")
            quiz("What is the relative minor of G major?", "E minor", "G minor", "B minor", "D minor")
            quiz("What is the relative major of D minor?", "F major", "D major", "B♭ major", "A major")
            quiz("C major and C minor are…", "Parallel keys", "Relative keys", "Enharmonic keys", "Unrelated")
            drill(DrillKind.RELATIVE_KEY, count = 5)
        }
        lesson("other-scales", "Pentatonic, Blues & Symmetric Scales", "Beyond major and minor.") {
            read("Pentatonic", """
                **Penta** = five. The **major pentatonic** drops the 4th and 7th: C D E G A. No half steps means no clashes - it's used in folk music worldwide.

                The black keys alone form a pentatonic scale (G♭ A♭ B♭ D♭ E♭) - you can improvise on them and it always sounds good!

                **Minor pentatonic**: A C D E G - the core of rock and blues soloing.
            """, visual = kb("C4", "B4", "C4", "D4", "E4", "G4", "A4"), play = "C4:e D4 E4 G4 A4 C5 A4 G4 E4 D4 C4:q")
            read("Blues scale", """
                Add the **♭5 "blue note"** to the minor pentatonic: C E♭ F **G♭** G B♭ C.
            """, visual = kb("C4", "C5", "C4", "Eb4", "F4", "Gb4", "G4", "Bb4", "C5"), play = "C4:e Eb4 F4 Gb4 G4 Bb4 C5 Bb4 G4 Gb4 F4 Eb4 C4:q")
            read("Symmetric scales", """
                - **Chromatic**: all 12 half steps.
                - **Whole-tone**: only whole steps (C D E F♯ G♯ A♯) - dreamy and floating (Debussy).
                - **Diminished / octatonic**: alternating half & whole steps (8 notes) - tense, used in jazz and film music.
            """, play = "C4:e D4 E4 F#4 G#4 A#4 C5:q")
            quiz("How many notes in a pentatonic scale?", "5", "6", "7", "8")
            quiz("The blues scale adds which note to the minor pentatonic?", "♭5 (blue note)", "Major 7th", "♭2", "♯4 only in major")
            quiz("A whole-tone scale has…", "Only whole steps", "Only half steps", "Alternating half and whole", "Five notes")
        }
        test {
            drill(DrillKind.KEY_SIGNATURE, count = 4, level = 2)
            drill(DrillKind.RELATIVE_KEY, count = 3)
            drill(DrillKind.CIRCLE, count = 3)
            quiz("Natural minor pattern?", "W H W W H W W", "W W H W W W H", "W H W W W H W", "H W W H W W W")
            quiz("The relative minor is found…", "A minor 3rd below the major tonic", "A 5th above", "A whole step below", "On the same tonic")
            drill(DrillKind.PLAY_SCALE, count = 1, level = 2)
        }
    }

    unit("chords", "Chords", "Triads, inversions, sevenths, extensions and chord symbols.") {
        lesson("triads", "Triads", "Three notes stacked in thirds.") {
            read("Stacking thirds", """
                A **chord** is three or more notes played together. The basic chord is the **triad**: a root, a **3rd** and a **5th** - notes stacked "every other key" on the white keys.

                The four triad types:
            """, visual = chords("C", "Cm", "Cdim", "Caug"))
            read("Triad formulas", "", visual = table(listOf("Triad", "Formula", "Thirds", "Symbol"),
                listOf("Major", "1 3 5", "M3 + m3", "C"), listOf("Minor", "1 ♭3 5", "m3 + M3", "Cm"),
                listOf("Diminished", "1 ♭3 ♭5", "m3 + m3", "C°, Cdim"), listOf("Augmented", "1 3 ♯5", "M3 + M3", "C+, Caug")))
            ear("Major or minor?", "C4+E4+G4:w", "Major", "Minor")
            ear("Major or minor?", "A3+C4+E4:w", "Minor", "Major")
            tap("Play a C major triad.", "C4", "E4", "G4", hint = true)
            tap("Play F major (F A C).", "F4", "A4", "C5", from = "C4", to = "C5")
            tap("Play D minor (D F A).", "D4", "F4", "A4")
            tap("Play E major - remember the major 3rd! (E G♯ B)", "E4", "G#4", "B4", explain = "E to G♯ is 4 half steps.")
            drill(DrillKind.CHORD_QUALITY_EAR, count = 6, level = 1)
            drill(DrillKind.PLAY_CHORD, count = 4, level = 1)
        }
        lesson("inversions", "Inversions & Voicing", "Same chord, different order.") {
            read("Inversions", """
                You can rearrange a chord's notes:
                - **Root position**: root on the bottom (C E G)
                - **1st inversion**: 3rd on the bottom (E G C) - written C/E
                - **2nd inversion**: 5th on the bottom (G C E) - written C/G

                Inversions let your hand move **smoothly** between chords instead of jumping.
            """, visual = staff("C4+E4+G4:h E4+G4+C5:h G4+C5+E5:w", names = true), play = "C4+E4+G4:h E4+G4+C5 G4+C5+E5:w")
            read("Smooth voice leading", "Compare C → F → G jumping in root position vs using inversions:",
                play = "C4+E4+G4:h F4+A4+C5:h G4+B4+D5:h C4+E4+G4:h R:1 C4+E4+G4:h C4+F4+A4:h B3+D4+G4:h C4+E4+G4:h", bpm = 90,
                visual = staff("C4+E4+G4:h C4+F4+A4 B3+D4+G4 C4+E4+G4", names = false, caption = "C - F/C - G/B - C: barely any movement!"))
            read("Close vs open voicing", """
                - **Close voicing**: notes packed within an octave.
                - **Open voicing**: notes spread out (e.g., left hand root + 5th, right hand 3rd + root). Fuller, more pianistic.
            """, play = "C4+E4+G4:w C3+G3+E4+C5:w")
            tap("Play C major in 1st inversion (E G C).", "E4", "G4", "C5", from = "C4", to = "C5")
            tap("Play G major in 2nd inversion (D G B).", "D4", "G4", "B4")
            quiz("C/E means…", "C major with E in the bass", "C or E", "E minor", "C diminished")
            quiz("In 2nd inversion, the lowest note is the…", "5th", "3rd", "Root", "7th")
        }
        lesson("sevenths", "Seventh Chords", "Add another third: the sound of jazz, soul and classical tension.") {
            read("Five seventh chords", "Stack one more third on top of a triad:", visual = chords("Cmaj7", "C7", "Cm7", "Cm7b5", "Cdim7"))
            read("Formulas", "", visual = table(listOf("Chord", "Formula", "Symbol", "Character"),
                listOf("Major 7th", "1 3 5 7", "Cmaj7, CΔ7", "Dreamy, soft"),
                listOf("Dominant 7th", "1 3 5 ♭7", "C7", "Bluesy, wants to resolve"),
                listOf("Minor 7th", "1 ♭3 5 ♭7", "Cm7, C-7", "Mellow, smooth"),
                listOf("Half-diminished", "1 ♭3 ♭5 ♭7", "Cm7♭5, Cø", "Moody"),
                listOf("Diminished 7th", "1 ♭3 ♭5 ♭♭7", "C°7", "Dramatic, suspenseful"),
                listOf("Minor-major 7th", "1 ♭3 5 7", "Cm(maj7)", "Mysterious, noir")))
            read("Why the dominant 7th matters", """
                The dominant 7th (V7) contains a **tritone** between its 3rd and 7th (in G7: B and F). That tension pulls hard toward the tonic:
                B rises to C, F falls to E. This is the engine of tonal music.
            """, visual = staff("G3+B3+D4+F4:h C4+E4+G4:h", names = true), play = "G3+B3+D4+F4:h C4+E4+G4:w")
            drill(DrillKind.SEVENTH_EAR, count = 5, level = 1)
            tap("Play G7: G B D F.", "G4", "B4", "D5", "F5", from = "C4", to = "B5")
            tap("Play Dm7: D F A C.", "D4", "F4", "A4", "C5", from = "C4", to = "C5")
            quiz("Which seventh chord has a ♭7 and a major 3rd?", "Dominant 7th", "Major 7th", "Minor 7th", "Diminished 7th")
        }
        lesson("color", "Sus, Add, Extended & Altered Chords", "Colour chords from pop to jazz.") {
            read("Suspended chords", """
                **Sus** chords replace the 3rd with a 2nd (**sus2**) or 4th (**sus4**). Without a 3rd, they're neither major nor minor - open and unresolved. Often the sus4 resolves to the 3rd.
            """, visual = chords("Csus2", "Csus4", "C"), play = "C4+F4+G4:h C4+E4+G4:h")
            read("Add chords", """
                **Add9** (or add2) adds the 9th to a triad *without* the 7th: Cadd9 = C E G D. A shimmering pop and worship staple.
                **6 chords** add the 6th: C6 = C E G A.
            """, visual = chords("Cadd9", "C6", "Cm6"))
            read("Extended chords: 9, 11, 13", """
                Keep stacking thirds past the 7th: **9th, 11th, 13th** (= 2nd, 4th, 6th an octave up). C13 contains (in theory) C E G B♭ D F A - all seven notes!

                On piano we **omit** notes: the 5th is usually dropped, the 11th clashes with a major 3rd so it's left out of dominant 13ths, and the root is often played by the bass.
            """, visual = chords("C9", "Cmaj9", "Cm9", "C13"))
            read("Altered chords", """
                In jazz, dominant chords get **altered** tensions: ♭9, ♯9, ♯11 (♭5), ♭13 (♯5). They add bite before resolving. Symbols like **G7♭9**, **G7♯9**, **G7alt**.
            """, visual = chords("G7b9", "G7#9", "G7alt"))
            read("Power chords & slash chords", """
                - **Power chord (C5)**: root + 5th only. Rock's bread and butter.
                - **Slash chord (C/G, D/F♯)**: chord over a specific bass note - either an inversion or a non-chord bass (e.g. F/G).
            """, visual = chords("C5", "D/F#", "F/G"))
            quiz("Csus4 contains…", "C F G", "C E G", "C D G", "C E F")
            quiz("A 9th is the same note as the…", "2nd, an octave up", "7th", "4th, an octave up", "6th")
            quiz("Which note is usually left out of a dominant 13th chord?", "The 11th", "The 3rd", "The 7th", "The 13th")
            ear("Which sounds more 'open/unresolved'?", "C4+F4+G4:w", "Suspended chord", "Major triad", "Minor triad")
        }
        lesson("symbols", "Reading Chord Symbols", "The language of lead sheets.") {
            read("Chord symbols", "Lead sheets show a melody plus chord symbols above. Learn to decode them:", visual = table(
                listOf("Symbol", "Meaning", "Notes (on C)"),
                listOf("C", "Major triad", "C E G"), listOf("Cm, C-, Cmin", "Minor triad", "C E♭ G"),
                listOf("C7", "Dominant 7th", "C E G B♭"), listOf("Cmaj7, CΔ, CM7", "Major 7th", "C E G B"),
                listOf("Cm7, C-7", "Minor 7th", "C E♭ G B♭"), listOf("Cø, Cm7♭5", "Half-diminished", "C E♭ G♭ B♭"),
                listOf("C°, Cdim", "Diminished triad", "C E♭ G♭"), listOf("C°7, Cdim7", "Diminished 7th", "C E♭ G♭ A"),
                listOf("C+, Caug", "Augmented", "C E G♯"), listOf("Csus4", "Suspended 4th", "C F G"),
                listOf("C6/9", "Six-nine", "C E G A D"), listOf("C/E", "C over E bass", "E C E G")))
            drill(DrillKind.CHORD_NAME, count = 6, level = 2)
            drill(DrillKind.PLAY_CHORD, count = 4, level = 2)
        }
        test {
            drill(DrillKind.CHORD_QUALITY_EAR, count = 4, level = 2)
            drill(DrillKind.CHORD_NAME, count = 4, level = 2)
            drill(DrillKind.PLAY_CHORD, count = 3, level = 2)
            quiz("A half-diminished chord is also written…", "m7♭5", "dim7", "maj7", "7♯9")
            quiz("The dominant 7th contains which unstable interval?", "Tritone", "Perfect 5th", "Octave", "Major 6th")
            quiz("G/B is…", "G major, 1st inversion", "G major, 2nd inversion", "B minor", "G with added B")
        }
    }

    unit("harmony", "Harmony & Chord Progressions", "Roman numerals, functions, and every type of progression.") {
        lesson("diatonic", "Chords in a Key", "Build a triad on every scale degree.") {
            read("Diatonic chords", """
                Build a triad on each note of a major scale using only notes from that scale. The pattern is always the same:

                **I - ii - iii - IV - V - vi - vii°**
                Major - minor - minor - Major - Major - minor - diminished

                Upper-case Roman numerals = major, lower-case = minor, ° = diminished.
            """, visual = chords("C", "Dm", "Em", "F", "G", "Am", "Bdim").copy(numerals = listOf("I", "ii", "iii", "IV", "V", "vi", "vii°")))
            read("In minor keys", """
                Natural minor gives: **i - ii° - III - iv - v - VI - VII**. Composers usually raise the 7th (harmonic minor) to get a **major V** (and V7) for a stronger pull home.
            """, visual = chords("Am", "Bdim", "C", "Dm", "E", "F", "G").copy(numerals = listOf("i", "ii°", "III", "iv", "V", "VI", "VII")))
            read("Diatonic seventh chords", "With sevenths: **Imaj7 - ii7 - iii7 - IVmaj7 - V7 - vi7 - viiø7**",
                visual = chords("Cmaj7", "Dm7", "Em7", "Fmaj7", "G7", "Am7", "Bm7b5").copy(numerals = listOf("Imaj7", "ii7", "iii7", "IVmaj7", "V7", "vi7", "viiø7")))
            quiz("In any major key, the vi chord is…", "Minor", "Major", "Diminished", "Augmented")
            quiz("In G major, what is the IV chord?", "C major", "D major", "E minor", "A minor")
            quiz("In D major, what is the V chord?", "A major", "G major", "B minor", "E minor")
            drill(DrillKind.DIATONIC_CHORD, count = 6)
        }
        lesson("function", "Tonic, Subdominant, Dominant", "The three jobs a chord can do.") {
            read("Harmonic function", """
                Chords play roles, like characters in a story:
                - **Tonic (T)** - home, rest: **I** (also vi, iii)
                - **Subdominant / Pre-dominant (S)** - moving away: **IV** and **ii**
                - **Dominant (D)** - tension, wants to go home: **V** and **vii°**

                The basic story of tonal music: **T → S → D → T**. In C: C → F → G → C.
            """, visual = progression("I IV V I", "C"))
            read("Primary chords", """
                **I, IV and V** are the *primary chords*. They contain every note of the scale between them, so they can harmonise almost any simple melody - from folk songs to rock 'n' roll.
            """, visual = progression("I IV I V I", "G"))
            quiz("Which chord has dominant function?", "V", "IV", "I", "vi")
            quiz("ii usually functions as…", "Pre-dominant (subdominant)", "Tonic", "Dominant", "Passing")
            ear("Does this end on the tonic (home) or the dominant (tension)?", "C3+C4+E4+G4:h F3+C4+F4+A4:h G3+B3+D4+G4:w", "Dominant - unfinished", "Tonic - finished", bpm = 80)
        }
        lesson("what-progression", "What Is a Chord Progression?", "Chords in motion.") {
            read("Chords in time", """
                A **chord progression** is a sequence of chords. It's the harmonic backbone of a song - the same melody feels completely different over different chords.

                We describe progressions with **Roman numerals** so they work in any key: "I-V-vi-IV" is C-G-Am-F in C, or G-D-Em-C in G.
            """, visual = progression("I V vi IV", "C"))
            read("Root movement", """
                What makes progressions feel strong or weak? Mostly how the **root** moves:
                - **Down a 5th / up a 4th** (G → C): the strongest, most "inevitable" motion.
                - **By step** (F → G): smooth, direct.
                - **By 3rd** (C → Am): gentle - the two chords share two notes.
            """)
            read("Types of progressions", """
                Musicians group progressions into a few families. We'll explore each:
                1. **Circle progressions** - roots fall by 5ths around the circle of fifths.
                2. **Loop (fixed / repeating) progressions** - a short pattern repeated, like a vamp or ostinato.
                3. **Linear progressions** - driven by a stepwise bass line (e.g., descending lament bass).
                4. **Pedal-point progressions** - chords change over one held bass note.
                5. **Cadential progressions** - formulas that end phrases.
                6. **Modal & chromatic progressions** - borrowed chords, ♭VII, chromatic mediants.
                7. **Form-based progressions** - 12-bar blues, rhythm changes.
            """, mood = MascotMood.TEACHING)
            quiz("Why use Roman numerals for progressions?", "They work in any key", "They're easier to play", "They show rhythm", "They're older")
            quiz("The strongest root movement is…", "Down a 5th", "Up a 2nd", "Down a 3rd", "Up a tritone")
        }
        lesson("circle-progressions", "Circle Progressions", "Riding the circle of fifths.") {
            read("Falling fifths", """
                A **circle progression** moves each root **down a 5th** (= up a 4th), following the circle of fifths counter-clockwise. Each chord feels like the "dominant" of the next.

                The full diatonic circle in C: **C - F - B° - E - A - D - G - C** ... but the most used part is the end:

                **vi - ii - V - I** (Am - Dm - G - C)
            """, visual = progression("vi ii V I", "C"))
            read("ii-V-I: the heart of jazz", """
                The last three links of the circle, **ii - V - I**, are the most common progression in jazz. With sevenths: **Dm7 - G7 - Cmaj7**.

                Notice the smooth voice leading: the 7th of each chord falls a half step to become the 3rd of the next.
            """, visual = progression("ii7 V7 Imaj7", "C"))
            read("Longer circles", """
                Baroque composers loved the full circle - Vivaldi, Handel and Bach chained them in sequences. In pop: "Fly Me to the Moon" and "Autumn Leaves" are built on circle motion; "I Will Survive" uses a circle progression in minor:

                **i - iv - VII - III - VI - ii° - V** (Am - Dm - G - C - F - B° - E)
            """, visual = progression("i iv VII III VI ii° V i", "Am"))
            read("Secondary dominant circles", "Make every chord a dominant 7th and you get the ragtime/jazz 'circle of dominants': **E7 - A7 - D7 - G7 - C**",
                visual = chords("E7", "A7", "D7", "G7", "C"))
            ear("Is this ii-V-I or I-IV-V?", "D3+F3+A3+C4:w G2+F3+B3+D4:w C3+E3+G3+B3:w", "ii-V-I", "I-IV-V", bpm = 70)
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 1)
        }
        lesson("loop-progressions", "Loop (Fixed) Progressions", "The repeating patterns behind modern music.") {
            read("Loops, vamps and ostinatos", """
                A **loop** or **fixed** progression is a short sequence - usually 2-4 chords - repeated throughout a section or a whole song. Jazz musicians call a repeated pattern a **vamp**; classical musicians call a repeating figure an **ostinato** or **ground bass**.

                Because the harmony doesn't "go anywhere", the melody, rhythm and production carry the song. Almost all modern pop, EDM and hip-hop is loop-based.
            """)
            read("The axis progression: I - V - vi - IV", """
                The most famous loop in pop. Hundreds of hits use it: "Let It Be", "Someone Like You", "Don't Stop Believin'", "With or Without You", "Despacito" (rotated)…

                **Rotations** of the same four chords sound different depending on where you start:
                - **I - V - vi - IV** - optimistic
                - **vi - IV - I - V** - wistful (the "sensitive female chord progression": "Zombie", "All of Me", "Despacito")
                - **IV - V - vi - I**, **V - vi - IV - I** …
            """, visual = progression("I V vi IV", "C"))
            read("The 50s / doo-wop progression: I - vi - IV - V", """
                "Stand By Me", "Every Breath You Take", "Perfect" (Ed Sheeran) … Also called the *ice cream changes*.
            """, visual = progression("I vi IV V", "C"))
            read("Pachelbel and two-chord vamps", """
                - **Pachelbel's Canon**: I - V - vi - iii - IV - I - IV - V (a ground bass repeated 28 times!).
                - **Two-chord vamps**: i - ♭VII (Dorian rock), I - IV (gospel/soul), i - iv (minor groove).
                - **Minor 4-chord loop**: i - VI - III - VII ("Shape of You" uses i - iv - VI - VII).
            """, visual = progression("I V vi iii IV I IV V", "D"))
            ear("Which loop is this?", "C3+C4+E4+G4:h G2+B3+D4+G4:h A2+A3+C4+E4:h F2+A3+C4+F4:h", "I-V-vi-IV", "I-IV-V-I", "ii-V-I", bpm = 80)
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 2)
        }
        lesson("linear-progressions", "Linear & Pedal Progressions", "When the bass line leads.") {
            read("Linear (stepwise) progressions", """
                In **linear progressions**, the chords are chosen to support a **bass line moving by step**. The chord roots may not follow strong patterns - the smooth line is the logic.

                **Descending bass**: C - C/B - Am - Am/G - F - C/E - Dm - G ("A Whiter Shade of Pale", "Let It Be" bridge, Bach's Air)
            """, visual = chords("C", "G/B", "Am", "C/G", "F", "C/E", "Dm", "G"))
            read("The lament bass", """
                A bass descending chromatically or diatonically from tonic to dominant in minor - used since the Baroque for sorrow (Purcell's "Dido's Lament"), and in songs like "Stairway to Heaven" (Am - Am/G♯ - Am/G - D/F♯).

                The **Andalusian cadence** **i - ♭VII - ♭VI - V** (Am - G - F - E) is a famous descending progression from flamenco.
            """, visual = progression("i bVII bVI V", "Am"))
            read("Pedal-point progressions", """
                A **pedal point** is a note (usually in the bass) held or repeated while the chords above change - first creating tension, then resolution.

                - **Tonic pedal**: C - F/C - G/C - C (organ endings, film music)
                - **Dominant pedal**: before the final cadence, a long G in the bass builds anticipation.
            """, visual = chords("C", "F/C", "G/C", "C"))
            read("Chromatic line clichés", """
                One note moves chromatically inside a held chord: **Am - Am(maj7) - Am7 - Am6** ("My Funny Valentine", "Stairway to Heaven", James Bond themes).
            """, play = "A2+A3+C4+E4:w A2+G#3+C4+E4:w A2+G3+C4+E4:w A2+F#3+C4+E4:w", bpm = 80)
            quiz("A pedal point is…", "A sustained note while chords change", "Using the sustain pedal", "A fast arpeggio", "A cadence")
            quiz("The Andalusian cadence (in A minor) is…", "Am - G - F - E", "C - F - G - C", "Dm - G - C", "Am - Dm - E")
        }
        lesson("voice-leading", "Voice Leading", "Moving smoothly between chords.") {
            read("Think in lines", """
                Each note of a chord is a **voice**. Good voice leading moves each voice as little as possible:
                1. **Keep common tones** - if both chords share a note, hold it.
                2. Move other voices by **step** where possible.
                3. Resolve tendency tones: the **leading tone rises** to the tonic, the **7th of a chord falls** by step.
                4. In classical style, avoid **parallel 5ths and octaves** between two voices - they make the voices lose independence.
            """, visual = staff("C4+E4+G4:h C4+F4+A4 B3+D4+G4 C4+E4+G4", names = false), play = "C4+E4+G4:h C4+F4+A4 B3+D4+G4 C4+E4+G4:w")
            quiz("In V7 → I, the 7th of the V7 chord usually…", "Falls by step", "Rises by step", "Leaps an octave", "Stays")
            quiz("Which should be avoided in classical voice leading?", "Parallel perfect 5ths", "Contrary motion", "Common tones", "Stepwise motion")
        }
        test {
            drill(DrillKind.DIATONIC_CHORD, count = 4)
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 2)
            quiz("vi - ii - V - I is an example of a…", "Circle progression", "Pedal progression", "Chromatic line", "Plagal cadence")
            quiz("A progression repeated over and over is called a…", "Loop / vamp / ostinato", "Cadence", "Modulation", "Sequence of fifths")
            quiz("In C major, I - V - vi - IV is…", "C G Am F", "C F G C", "C Am F G", "Am F C G")
            quiz("Which is the dominant chord in F major?", "C", "B♭", "G", "Dm")
        }
    }

    unit("cadences-form", "Cadences, Phrases & Form", "How music breathes and how pieces are built.") {
        lesson("cadences", "Cadences", "Musical punctuation.") {
            read("Musical punctuation", """
                A **cadence** is a chord progression that ends a phrase, like punctuation in language:
            """, visual = table(listOf("Cadence", "Chords", "Effect"),
                listOf("Perfect (authentic)", "V → I", "Full stop. Final."),
                listOf("Imperfect authentic", "V → I, but inverted or 3rd/5th on top", "Comma-like close"),
                listOf("Plagal ('Amen')", "IV → I", "Gentle, churchy"),
                listOf("Half", "any → V", "Question mark - unfinished"),
                listOf("Deceptive (interrupted)", "V → vi", "Surprise! Expected home, got elsewhere"),
                listOf("Phrygian half", "iv6 → V (minor)", "Baroque, dramatic pause")))
            read("Perfect cadence", "V7 - I", play = "G2+F3+B3+D4:h C3+E3+G3+C4:w", visual = progression("V7 I", "C"))
            read("Plagal cadence", "IV - I: the 'Amen' at the end of hymns.", play = "F2+F3+A3+C4:h C3+E3+G3+C4:w", visual = progression("IV I", "C"))
            read("Half cadence", "Ends on V - leaves you hanging.", play = "C3+E3+G3+C4:h F2+F3+A3+C4:h G2+G3+B3+D4:w", visual = progression("I IV V", "C"))
            read("Deceptive cadence", "V → vi: the ear expects I but gets the relative minor.", play = "G2+G3+B3+D4:h A2+A3+C4+E4:w", visual = progression("V vi", "C"))
            drill(DrillKind.CADENCE_EAR, count = 6)
        }
        lesson("phrases", "Motifs, Phrases & Periods", "The grammar of melody.") {
            read("From small to large", """
                - **Motif**: the smallest recognisable idea (Beethoven's 5th: da-da-da-DUM). Developed by repetition, sequence, inversion, augmentation.
                - **Phrase**: a musical sentence, often 4 bars, ending with a cadence.
                - **Period**: two phrases - an **antecedent** (question, ends with a half cadence) and **consequent** (answer, ends with a perfect cadence).
                - **Sequence**: a motif repeated at a higher or lower pitch.
            """, play = "G4:e G4 G4 Eb4:h R:1 F4:e F4 F4 D4:h", bpm = 100)
            quiz("A phrase that 'asks a question' often ends with a…", "Half cadence", "Perfect cadence", "Plagal cadence", "Key change")
            quiz("Repeating a motif at a different pitch is called a…", "Sequence", "Cadence", "Period", "Pedal")
        }
        lesson("forms", "Musical Forms", "Binary, ternary, rondo, sonata, song forms.") {
            read("Common forms", "We label sections with letters. Same letter = same (or similar) music.", visual = table(
                listOf("Form", "Pattern", "Where you'll find it"),
                listOf("Binary", "AB (often ||:A:||:B:||)", "Baroque dances, minuets"),
                listOf("Ternary", "ABA", "Nocturnes, arias, Für Elise sections"),
                listOf("Rondo", "ABACA(BA)", "Rondo alla Turca, Für Elise (overall)"),
                listOf("Theme & variations", "A A1 A2 A3…", "Mozart's Twinkle Variations, Goldberg Variations"),
                listOf("Sonata form", "Exposition - Development - Recapitulation", "First movements of sonatas & symphonies"),
                listOf("32-bar song (AABA)", "A A B A, 8 bars each", "Jazz standards, Tin Pan Alley"),
                listOf("12-bar blues", "I-I-I-I / IV-IV-I-I / V-IV-I-V", "Blues, rock 'n' roll"),
                listOf("Verse-chorus", "Intro V C V C Bridge C Outro", "Modern pop")))
            read("Sonata form in detail", """
                1. **Exposition** - Theme 1 in the home key; a transition; Theme 2 in a contrasting key (usually the dominant, or relative major in minor keys).
                2. **Development** - themes fragmented, combined and passed through many keys. Maximum tension.
                3. **Recapitulation** - both themes return, now **both in the home key**. Resolution.
                Often framed by an introduction and a coda.
            """)
            quiz("ABA is…", "Ternary form", "Binary form", "Rondo", "Sonata form")
            quiz("In sonata form, the section where themes travel through many keys is the…", "Development", "Exposition", "Recapitulation", "Coda")
            quiz("AABA 32-bar form is typical of…", "Jazz standards", "Baroque fugues", "12-bar blues", "Minuets")
        }
        lesson("texture", "Texture & Counterpoint", "How many lines, and how they interact.") {
            read("Texture", """
                - **Monophony**: a single melody line (Gregorian chant, someone whistling).
                - **Homophony**: melody + chordal accompaniment (most pop and Romantic piano music).
                - **Polyphony / counterpoint**: two or more independent melodies at once (Bach fugues, rounds like Frère Jacques).
                - **Heterophony**: variations of one melody simultaneously (folk traditions).
            """)
            read("Counterpoint basics", """
                **Counterpoint** = "note against note". Lines should be independent:
                - **Contrary motion** (one up, one down) gives the most independence.
                - **Oblique motion**: one voice holds while the other moves.
                - **Similar / parallel motion**: both move the same direction - use sparingly.

                A **fugue** starts with a theme (the *subject*) in one voice, then answered in another, building a web of imitation.
            """, play = "C4+C5:q D4+B4 E4+A4 F4+G4 G4+F4 A4+E4 B4+D4 C5+C5:w", bpm = 100)
            quiz("A Bach fugue is an example of…", "Polyphony", "Monophony", "Homophony", "Heterophony")
            quiz("Melody with chord accompaniment is…", "Homophony", "Polyphony", "Monophony", "Counterpoint")
        }
        test {
            drill(DrillKind.CADENCE_EAR, count = 4)
            quiz("V → vi is a…", "Deceptive cadence", "Plagal cadence", "Perfect cadence", "Half cadence")
            quiz("IV → I is a…", "Plagal cadence", "Perfect cadence", "Half cadence", "Deceptive cadence")
            quiz("Rondo form looks like…", "ABACA", "AB", "ABA", "AABA")
            quiz("An antecedent phrase usually ends with a…", "Half cadence", "Perfect cadence", "Fermata", "Key change")
        }
    }

    unit("advanced-harmony", "Advanced Harmony", "Secondary dominants, borrowed chords, modulation and more.") {
        lesson("secondary", "Secondary Dominants", "Borrowing V of other chords.") {
            read("Dominants of other chords", """
                Any major or minor diatonic chord can be preceded by **its own dominant**. This is a **secondary dominant**, written **V/x** ("five of x").

                In C major:
                - **V/V** = D (or D7) → G
                - **V/ii** = A7 → Dm
                - **V/vi** = E7 → Am
                - **V/IV** = C7 → F

                They add a chromatic note (F♯, C♯, G♯, B♭) and a strong pull to the target chord.
            """, visual = progression("I V/vi vi V/V V I", "C"))
            quiz("In C major, V/V is…", "D major", "G major", "A major", "E major")
            quiz("In G major, V7/vi is…", "B7", "E7", "D7", "F♯7", explain = "vi in G is Em; the dominant of E is B.")
            ear("Hear the secondary dominant (E7) pulling to Am?", "C3+C4+E4+G4:h E3+D4+G#4+B4:h A2+C4+E4+A4:w", "Yes - E7 resolves to Am", "No, it's plain C to Am")
        }
        lesson("borrowed", "Borrowed Chords (Modal Mixture)", "Mixing major and minor.") {
            read("Borrowing from the parallel minor", """
                In a major key you can borrow chords from the **parallel minor**. In C major, from C minor:
                - **iv** (Fm) - wistful (Beatles, Radiohead "Creep" uses I - III - IV - iv)
                - **♭VI** (A♭) and **♭VII** (B♭) - heroic, rock and film ("♭VI - ♭VII - I" = the Mario cadence!)
                - **♭III** (E♭)
                - **ii°** / **iiø7**
            """, visual = progression("I bVI bVII I", "C"))
            read("The minor iv", "The 'goosebumps chord': IV → iv → I", play = "F3+A3+C4+F4:h F3+Ab3+C4+F4:h C3+G3+C4+E4:w",
                visual = progression("IV iv I", "C"))
            quiz("In C major, the borrowed ♭VII chord is…", "B♭ major", "B diminished", "B major", "A♭ major")
            quiz("Borrowed chords come from the…", "Parallel key", "Relative key", "Dominant key", "Chromatic scale")
        }
        lesson("neapolitan", "Neapolitan & Augmented Sixths", "Classical chromatic colour chords.") {
            read("The Neapolitan sixth (N6)", """
                A **major chord on ♭2**, usually in first inversion. In C minor: **D♭/F**. It's a pre-dominant that moves to V. Dramatic and dark - used by Beethoven (Moonlight Sonata, bar 3 area) and Chopin.
            """, play = "F3+Ab3+Db4:h G3+B3+D4:h C3+Eb3+G3+C4:w", visual = chords("Db/F", "G", "Cm"))
            read("Augmented sixth chords", """
                Built around the interval of an **augmented 6th** between ♭6 in the bass and ♯4 above, which expand outward to the octave on V:
                - **Italian (It+6)**: ♭6 - 1 - ♯4 → in C: A♭ C F♯
                - **French (Fr+6)**: ♭6 - 1 - 2 - ♯4 → A♭ C D F♯
                - **German (Ger+6)**: ♭6 - 1 - ♭3 - ♯4 → A♭ C E♭ F♯ (sounds like A♭7!)
            """, play = "Ab2+C4+Eb4+F#4:h G2+B3+D4+G4:w", bpm = 70)
            quiz("The Neapolitan chord is built on…", "♭2", "♭7", "♯4", "♭6")
            quiz("The German augmented sixth sounds like a…", "Dominant 7th chord", "Minor triad", "Diminished triad", "Power chord")
        }
        lesson("modulation", "Modulation", "Changing key mid-piece.") {
            read("Changing key", """
                **Modulation** moves the music to a new tonic. Common techniques:
                - **Pivot chord**: a chord that belongs to both keys (in C → G, Am is vi in C and ii in G).
                - **Direct / truck-driver modulation**: jump up a half or whole step for the last chorus (pop ballads!).
                - **Via secondary dominant**: use V7 of the new key.
                - **Enharmonic**: respell a diminished 7th or German 6th to land in a distant key.

                Most common destinations: dominant key (V), relative minor/major, subdominant (IV).
            """, play = "C3+E4+G4:h A2+C4+E4:h D3+F#3+A3+D4:h G2+G3+B3+D4:w", bpm = 80)
            quiz("A chord common to two keys used to modulate is a…", "Pivot chord", "Pedal chord", "Neapolitan", "Passing chord")
            quiz("The 'truck driver' modulation usually goes…", "Up a half or whole step", "Down a 5th", "To the relative minor", "To the parallel minor")
        }
        lesson("non-chord", "Non-Chord Tones", "Notes that decorate the harmony.") {
            read("Decorations", "", visual = table(listOf("Type", "How it works"),
                listOf("Passing tone", "Steps between two chord tones (C-D-E over C)"),
                listOf("Neighbour tone", "Steps away and back (E-F-E)"),
                listOf("Suspension", "A chord tone held over into the next chord, then resolves down (4-3 suspension)"),
                listOf("Appoggiatura", "Leaps to a dissonance on the beat, then resolves by step - very expressive"),
                listOf("Anticipation", "Arrives at the next chord's note early"),
                listOf("Escape tone", "Steps away, then leaps the opposite way"),
                listOf("Pedal tone", "Held note against changing harmony")))
            read("Hear a 4-3 suspension", "The F clashes, then falls to E:", play = "G3+C4+F4:h G3+C4+E4:w", bpm = 70)
            quiz("A suspension usually resolves…", "Down by step", "Up by leap", "By staying", "Down an octave")
            quiz("A note that steps away from a chord tone and back is a…", "Neighbour tone", "Passing tone", "Anticipation", "Pedal")
        }
        lesson("jazz-harmony", "Tritone Substitution & Chromatic Mediants", "Modern harmonic colours.") {
            read("Tritone substitution", """
                Any dominant 7th can be replaced by the dominant 7th a **tritone away**: G7 → **D♭7**. They share the same tritone (B/C♭ and F), so the tension and resolution are the same - but the bass now slides by half step: **Dm7 - D♭7 - Cmaj7**.
            """, visual = chords("Dm7", "Db7", "Cmaj7"), play = "D3+F3+A3+C4:h Db3+Ab3+Cb4+F4:h C3+E3+G3+B3:w")
            read("Chromatic mediants", """
                Chords whose roots are a **3rd apart** but don't share a key: C → E, C → A♭, C → E♭. Common tones + unexpected colour = the "epic film score" sound (John Williams, Hans Zimmer).
            """, play = "C3+C4+E4+G4:h Ab2+C4+Eb4+Ab4:h C3+C4+E4+G4:h E3+B3+E4+G#4:w", bpm = 70)
            quiz("The tritone substitute for G7 is…", "D♭7", "D7", "C♯m7", "F7")
            quiz("C major to A♭ major is a…", "Chromatic mediant", "Secondary dominant", "Plagal cadence", "Pivot chord")
        }
        test {
            quiz("In C major, V/ii is…", "A (A7)", "D (D7)", "E (E7)", "G (G7)")
            quiz("Borrowing iv in a major key comes from…", "The parallel minor", "The relative minor", "The dominant key", "The Lydian mode")
            quiz("Which is a pre-dominant chord built on ♭2?", "Neapolitan", "Italian sixth", "Secondary dominant", "Tritone sub")
            quiz("Tritone substitution replaces G7 with…", "D♭7", "C♯7", "B7", "E♭7")
            quiz("A note held from the previous chord that then resolves down is a…", "Suspension", "Passing tone", "Neighbour", "Escape tone")
        }
    }

    unit("modes", "Modes & Exotic Scales", "Seven flavours of the diatonic scale and beyond.") {
        lesson("modes", "The Seven Modes", "Same notes, different home.") {
            read("Modes = rotations", """
                Play the white keys starting from different notes and you get the seven **modes**. Each has a unique mood because the half steps fall in different places.
            """, visual = table(listOf("Mode", "White keys from", "Formula", "Flavour"),
                listOf("Ionian", "C", "1 2 3 4 5 6 7", "Major - bright"),
                listOf("Dorian", "D", "1 2 ♭3 4 5 **6** ♭7", "Minor with bright 6 - soulful, jazzy"),
                listOf("Phrygian", "E", "1 **♭2** ♭3 4 5 ♭6 ♭7", "Dark, Spanish"),
                listOf("Lydian", "F", "1 2 3 **♯4** 5 6 7", "Dreamy, floating"),
                listOf("Mixolydian", "G", "1 2 3 4 5 6 **♭7**", "Major with bluesy ♭7 - rock"),
                listOf("Aeolian", "A", "1 2 ♭3 4 5 ♭6 ♭7", "Natural minor - sad"),
                listOf("Locrian", "B", "1 **♭2** ♭3 4 **♭5** ♭6 ♭7", "Unstable, rarely used")))
            read("Brightest to darkest", "**Lydian → Ionian → Mixolydian → Dorian → Aeolian → Phrygian → Locrian** - each step flattens one more note.",
                mood = MascotMood.THINKING)
            read("Hear Dorian", "D Dorian over a Dm - G vamp (think 'So What', 'Scarborough Fair', 'Oye Como Va'):",
                play = "D3+A3+C4+F4:h G3+B3+D4+F4:h D4:e E4 F4 G4 A4 B4 C5 D5:q", visual = kb("D4", "D5", "D4", "E4", "F4", "G4", "A4", "B4", "C5", "D5"))
            read("Hear Lydian", "F Lydian - the ♯4 (B natural) gives that magical film-score lift:",
                play = "F3+C4+E4+A4:w F4:e G4 A4 B4 C5 D5 E5 F5:q")
            quiz("Which mode has a raised 4th?", "Lydian", "Mixolydian", "Dorian", "Phrygian")
            quiz("Dorian is like natural minor but with a…", "Raised 6th", "Raised 7th", "Flat 2nd", "Flat 5th")
            quiz("Mixolydian is major with a…", "Flat 7th", "Sharp 4th", "Flat 3rd", "Flat 6th")
            drill(DrillKind.MODE_ID, count = 5)
        }
        lesson("using-modes", "Using Modes", "Modal vamps and modal interchange.") {
            read("Modal music", """
                In **modal** music, the harmony emphasises the mode's characteristic note, often with a **vamp**:
                - **Dorian**: i - IV (Dm - G) - funk, soul, "Get Lucky"-style grooves
                - **Mixolydian**: I - ♭VII - IV (G - F - C) - classic rock ("Sweet Child o' Mine", "Hey Jude" coda)
                - **Lydian**: I - II (F - G) - film and TV themes
                - **Phrygian**: i - ♭II (Em - F) - flamenco, metal
            """, visual = progression("I bVII IV I", "G"))
            ear("Which mode does this vamp suggest? (G - F - C)", "G2+G3+B3+D4:h F2+F3+A3+C4:h C3+C4+E4+G4:h G2+G3+B3+D4:h", "Mixolydian", "Lydian", "Phrygian")
        }
        lesson("exotic", "Exotic & Jazz Scales", "Harmonic minor modes, melodic minor modes and world scales.") {
            read("From melodic and harmonic minor", "", visual = table(listOf("Scale", "Notes (on C)", "Use"),
                listOf("Phrygian dominant", "C D♭ E F G A♭ B♭", "Flamenco, Middle-Eastern, metal"),
                listOf("Lydian dominant", "C D E F♯ G A B♭", "7♯11 chords, The Simpsons theme"),
                listOf("Altered (super-Locrian)", "C D♭ E♭ E F♯ G♯ B♭", "7alt chords in jazz"),
                listOf("Hungarian minor", "C D E♭ F♯ G A♭ B", "Liszt, gypsy jazz"),
                listOf("Bebop dominant", "C D E F G A B♭ B", "Jazz lines"),
                listOf("Japanese In", "C D♭ F G A♭", "Japanese traditional music")))
            read("Hear Phrygian dominant", "", play = "E4:e F4 G#4 A4 B4 C5 D5 E5 D5 C5 B4 A4 G#4 F4 E4:q", bpm = 110)
            quiz("Which scale fits a 7alt chord?", "Altered scale", "Major scale", "Dorian", "Pentatonic")
            quiz("Phrygian dominant is the 5th mode of…", "Harmonic minor", "Melodic minor", "Major", "Whole tone")
        }
        test {
            drill(DrillKind.MODE_ID, count = 5)
            quiz("Which mode is the natural minor?", "Aeolian", "Dorian", "Ionian", "Locrian")
            quiz("Which mode starts on G using white keys?", "Mixolydian", "Lydian", "Dorian", "Aeolian")
            quiz("The darkest mode is…", "Locrian", "Phrygian", "Aeolian", "Dorian")
        }
    }

    unit("notation", "Notation Deep Dive", "Repeats, ornaments, pedalling, meters and expression.") {
        lesson("navigation", "Repeats & Road Maps", "D.C., D.S., Coda, Fine, endings.") {
            read("Navigation symbols", "", visual = symbols(
                "|: :|" to "**Repeat signs** - play the section between them twice",
                "1. 2." to "**1st/2nd endings (volta)** - play ending 1 the first time, skip to ending 2 the second time",
                "D.C." to "**Da Capo** - go back to the beginning",
                "D.S." to "**Dal Segno** - go back to the segno sign",
                "Fine" to "**Fine** - the end (used with D.C./D.S. al Fine)",
                "Coda" to "**To Coda** - jump to the ending section marked with the coda sign",
                "8va" to "**Ottava** - play an octave higher (8vb = lower)"))
            quiz("D.C. al Fine means…", "Go back to the beginning and stop at Fine", "Go to the sign", "Play softly", "Skip to the coda")
            quiz("'8va' over notes means…", "Play an octave higher", "Play 8 times", "Play 8 beats", "Play slower")
        }
        lesson("ornaments", "Ornaments", "Trills, mordents, turns and grace notes.") {
            read("Decorating a note", "", visual = symbols(
                "tr" to "**Trill** - rapid alternation with the note above",
                "mordent" to "**Mordent** - main note, note above, main note (quick)",
                "lower mordent" to "**Lower mordent** - main, note below, main",
                "turn (~)" to "**Turn** - above, main, below, main",
                "grace" to "**Acciaccatura** (slashed small note) - crushed in as fast as possible",
                "appogg." to "**Appoggiatura** (small note, no slash) - leans on the beat, takes half the main note's time",
                "arpeggio ⌇" to "**Rolled chord** - spread the notes from bottom to top"))
            read("Hear them", "Trill, mordent, turn:", play = "E5:s F5 E5 F5 E5 F5 E5 F5 E5:q R:1 E5:s F5 E5:h R:1 F5:s E5 D5 E5:h", bpm = 80)
            quiz("A trill alternates the main note with…", "The note above", "The note below", "The octave", "A chord")
            quiz("Ornaments are especially common in which era?", "Baroque", "Romantic", "Modern pop", "Minimalism")
        }
        lesson("pedals", "Pedal Markings", "Sustain, soft and sostenuto pedals.") {
            read("The three pedals", """
                - **Right - sustain (damper) pedal**: lifts all dampers so notes ring. Marked **Ped.** … ✱ or with a bracket line.
                - **Left - soft (una corda) pedal**: softer, mellower tone. Marked *una corda*; release with *tre corde*.
                - **Middle - sostenuto**: sustains only the notes held down when you press it (not on all pianos).

                **Legato pedalling** (syncopated pedal): change the pedal *just after* the new chord sounds - lift as you play, press again immediately. This avoids both gaps and blur.
            """, visual = picture(Illustration.PEDALS))
            quiz("Which pedal makes notes keep ringing?", "Right (sustain)", "Left (una corda)", "Middle (sostenuto)", "None")
            quiz("In legato pedalling, you change the pedal…", "Just after playing the new chord", "Before playing the new chord", "Only at the end", "Every beat regardless")
        }
        lesson("meters", "Simple, Compound & Odd Meters", "6/8, 5/4, 7/8 and swing.") {
            read("Simple vs compound", """
                - **Simple meter**: beats divide into **2** (2/4, 3/4, 4/4).
                - **Compound meter**: beats divide into **3** (6/8, 9/8, 12/8). 6/8 has **two** dotted-quarter beats, not six!
                - **Irregular / odd meters**: 5/4 ("Take Five", grouped 3+2), 7/8 (2+2+3).
            """, visual = staff("C5:e C5 C5 C5 C5 C5", time = "6/8"))
            read("Syncopation & swing", """
                - **Syncopation**: accents on weak beats or off-beats - the heart of ragtime, jazz, funk and pop.
                - **Swing**: pairs of eighth notes are played long-short (roughly like a triplet quarter + eighth). Notated as straight eighths with "Swing" written at the top.
                - **Polyrhythm**: two rhythms at once, e.g. 3 against 2 (Chopin loves this).
            """, play = "C5:qt C5:t C5:qt C5:t C5:qt C5:t C5:qt C5:t", bpm = 100)
            quiz("6/8 time has how many main beats per bar?", "2", "6", "3", "8")
            quiz("Swing eighths are played…", "Long-short", "Short-long", "Exactly even", "As triplets of three notes")
            quiz("'Take Five' is famous for being in…", "5/4", "7/8", "6/8", "3/4")
        }
        lesson("terms", "Expression Terms", "The Italian vocabulary of musicians.") {
            read("Expression marks", "", visual = table(listOf("Term", "Meaning"),
                listOf("dolce", "Sweetly"), listOf("cantabile", "In a singing style"), listOf("espressivo", "Expressively"),
                listOf("legato", "Smoothly"), listOf("marcato", "Marked, accented"), listOf("rubato", "Flexible tempo - steal and give back time"),
                listOf("con brio", "With spirit"), listOf("sotto voce", "In an undertone, very quietly"), listOf("tenuto", "Held"),
                listOf("fermata", "Pause - hold"), listOf("poco a poco", "Little by little"), listOf("subito", "Suddenly"),
                listOf("molto", "Very"), listOf("rallentando", "Slowing down"), listOf("morendo", "Dying away")))
            drill(DrillKind.TERMS, count = 6)
        }
        test {
            drill(DrillKind.TERMS, count = 4)
            quiz("1st and 2nd endings tell you…", "Which bars to play on each repeat", "Which hand to use", "Two different tempos", "Two dynamics")
            quiz("Una corda refers to the…", "Soft pedal", "Sustain pedal", "A single string melody", "An ornament")
            quiz("An acciaccatura is…", "A crushed grace note", "A long held note", "A repeat sign", "A pedal mark")
            quiz("Rubato means…", "Flexible tempo", "Very loud", "Detached", "Repeat")
        }
    }
}

