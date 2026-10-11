package com.pianoquest.app.data.courses

import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Visual
import com.pianoquest.app.data.course
import com.pianoquest.app.data.table

internal fun earCourse() = course(
    id = "ear", title = "Ear Training", subtitle = "Hear it, name it, play it",
    description = """
        Train your ears to recognise intervals, chord qualities, cadences, progressions and modes - the skill behind
        playing by ear, improvising and transcribing. Every drill is generated fresh, so you can repeat lessons forever.
    """,
    level = "All levels", color = 0xFF55693A, icon = CourseIcon.EAR,
) {
    unit("pitch", "Pitch & Intervals", "From high/low to every interval.") {
        lesson("high-low", "High, Low & Direction", "The first ear skills.") {
            ear("Which note is higher: the first or the second?", "C4:h G4:h", "Second", "First", "Same")
            ear("Which note is higher?", "E5:h A4:h", "First", "Second", "Same")
            ear("Up, down or the same?", "D4:q D4 D4 D4", "The same", "Up", "Down")
            ear("Does the melody go up or down?", "G4:q F4 E4 D4 C4", "Down", "Up", "Same")
            ear("Steps or leaps?", "C4:q G4 C5 E5", "Leaps", "Steps")
        }
        lesson("intervals-1", "Intervals I: 2nds, 3rds, 5ths, Octaves", "The most common sounds.") {
            read("Train with reference songs", "Remember: M2 = Happy Birthday, M3 = When the Saints, P5 = Twinkle, P8 = Somewhere Over the Rainbow.", mood = MascotMood.THINKING)
            drill(DrillKind.INTERVAL_EAR, count = 10, level = 1)
        }
        lesson("intervals-2", "Intervals II: 4ths, 6ths, minor 3rds", "Expanding the palette.") {
            drill(DrillKind.INTERVAL_EAR, count = 10, level = 2)
        }
        lesson("intervals-3", "Intervals III: All 12", "Including 7ths and the tritone.") {
            drill(DrillKind.INTERVAL_EAR, count = 12, level = 3)
        }
        test {
            drill(DrillKind.INTERVAL_EAR, count = 10, level = 3)
        }
    }
    unit("chords", "Chord Qualities", "Major, minor and beyond.") {
        lesson("triads", "Major vs Minor", "Happy or sad?") {
            read("Listen for mood", "Major chords sound bright; minor chords sound darker. Diminished = tense and small; augmented = stretched and dreamy.")
            drill(DrillKind.CHORD_QUALITY_EAR, count = 10, level = 1)
        }
        lesson("all-triads", "All Four Triads", "Add diminished and augmented.") {
            drill(DrillKind.CHORD_QUALITY_EAR, count = 10, level = 2)
        }
        lesson("sevenths", "Seventh Chords", "Maj7, 7, m7, m7♭5, dim7.") {
            read("Seventh chord sounds", "maj7 = dreamy; dominant 7 = bluesy tension; m7 = mellow; m7♭5 = moody; dim7 = suspense.")
            drill(DrillKind.SEVENTH_EAR, count = 10, level = 2)
        }
        test {
            drill(DrillKind.CHORD_QUALITY_EAR, count = 6, level = 2)
            drill(DrillKind.SEVENTH_EAR, count = 6, level = 2)
        }
    }
    unit("harmony", "Cadences, Progressions & Modes", "Hearing harmony in context.") {
        lesson("cadences", "Cadences", "Perfect, plagal, half, deceptive.") {
            drill(DrillKind.CADENCE_EAR, count = 8)
        }
        lesson("progressions", "Progressions", "The famous patterns.") {
            drill(DrillKind.PROGRESSION_EAR, count = 8, level = 2)
        }
        lesson("modes", "Modes by Ear", "Hear the characteristic note.") {
            read("Characteristic notes", "Dorian = bright 6th in minor; Phrygian = dark ♭2; Lydian = floating ♯4; Mixolydian = bluesy ♭7.")
            drill(DrillKind.MODE_ID, count = 6)
        }
        test {
            drill(DrillKind.CADENCE_EAR, count = 4)
            drill(DrillKind.PROGRESSION_EAR, count = 4, level = 3)
        }
    }
}

internal fun sightCourse() = course(
    id = "sight", title = "Sight-Reading", subtitle = "Read faster, play more fluently",
    description = """
        Systematic training for reading music at sight: fast note recognition in both clefs, landmark notes,
        reading by interval, key signatures, rhythm reading, and play-it-now exercises.
    """,
    level = "All levels", color = 0xFF5B4A3A, icon = CourseIcon.SIGHT, recommendedAfter = "foundations",
) {
    unit("notes", "Fast Note Reading", "Instant recognition.") {
        lesson("landmarks", "Landmark Notes", "Anchors on the grand staff.") {
            read("Use landmarks", "Learn these instantly, and read everything else as a step or skip from them: **C2, F3 (bass F), C4 (middle C), G4 (treble G), C5, C6**.",
                visual = Visual.Staff(Clef.GRAND, "C3:q F3 C4 G4 C5", showNames = true))
            drill(DrillKind.NAME_GRAND, count = 10, level = 1)
        }
        lesson("treble-speed", "Treble Clef Speed Drill", "Ledger lines included.") { drill(DrillKind.NAME_TREBLE, count = 12, level = 2) }
        lesson("bass-speed", "Bass Clef Speed Drill", "Ledger lines included.") { drill(DrillKind.NAME_BASS, count = 12, level = 2) }
        lesson("play-it", "See It, Play It", "From staff to keys without thinking.") { drill(DrillKind.FIND_STAFF_NOTE, count = 10, level = 2) }
        test {
            drill(DrillKind.NAME_GRAND, count = 8, level = 2)
            drill(DrillKind.FIND_STAFF_NOTE, count = 4, level = 2)
        }
    }
    unit("reading", "Reading Music in Context", "Keys, intervals and phrases.") {
        lesson("intervals", "Reading by Interval", "See the shape.") {
            drill(DrillKind.INTERVAL_STAFF, count = 10, level = 1)
        }
        lesson("keys", "Key Signatures at a Glance", "Know the key before you play.") {
            read("Before you play", "Always check: **key signature**, **time signature**, **tempo**, the **lowest/highest notes**, and any **repeats** - the 'pre-flight check'.", mood = MascotMood.TEACHING)
            drill(DrillKind.KEY_SIGNATURE, count = 8, level = 2)
        }
        lesson("phrases", "Sight-Reading Phrases", "Keep going, don't stop!") {
            read("The golden rule", "When sight-reading, **never stop** to fix a mistake. Keep the pulse going - like a musician in a band. Look **ahead**, not at your hands.")
            play("Read and play.", "C4:q E4 D4 F4 E4 G4 F4 D4 C4:w", from = "B3", to = "A4", staff = Clef.TREBLE, hints = false)
            play("Read and play.", "G4:q F4 E4 D4 E4 C4 D4 B3 C4:w", from = "A3", to = "A4", staff = Clef.TREBLE, hints = false)
            play("Bass clef: read and play.", "C3:q E3 G3 E3 F3 D3 B2 G2 C3:w", from = "G2", to = "B3", staff = Clef.BASS, hints = false)
            play("Skips and steps.", "E4:q G4 B4 G4 A4 F4 D4 F4 E4:w", from = "C4", to = "C5", staff = Clef.TREBLE, hints = false)
            play("With a sharp.", "G4:q A4 B4 C5 D5 C5 B4 A4 G4 F#4 G4:h", from = "E4", to = "E5", staff = Clef.TREBLE, hints = false)
        }
        test {
            drill(DrillKind.INTERVAL_STAFF, count = 4, level = 2)
            play("Read and play.", "E4:q C4 D4 F4 E4 G4 C5 G4 E4:w", from = "B3", to = "C5", staff = Clef.TREBLE, hints = false)
            play("Read and play (bass).", "G2:q B2 D3 G3 F3 D3 B2 D3 C3:w", from = "G2", to = "B3", staff = Clef.BASS, hints = false)
        }
    }
}

internal fun techniqueCourse() = course(
    id = "technique", title = "Technique Lab", subtitle = "Scales, arpeggios, chords in all 12 keys",
    description = """
        Daily technical work that builds speed, evenness, strength and freedom: five-finger patterns, all major and minor
        scales, arpeggios, chord inversions, Hanon exercises, octaves and trills - with the right fingerings.
    """,
    level = "All levels", color = 0xFF6B4E36, icon = CourseIcon.TECHNIQUE, recommendedAfter = "foundations",
) {
    unit("warmups", "Warm-ups & Five-Finger Patterns", "Start every session here.") {
        lesson("five-finger", "Five-Finger Patterns in Every Key", "Major and minor shapes.") {
            read("The pattern", "Major five-finger pattern: W - W - H - W (e.g. C D E F G). Minor: W - H - W - W (C D E♭ F G). Play in every key, around the circle of fifths.")
            play("C major five-finger.", "C4:q D4 E4 F4 G4 F4 E4 D4 C4:h", fingers = listOf(1, 2, 3, 4, 5, 4, 3, 2, 1))
            play("C minor five-finger.", "C4:q D4 Eb4 F4 G4 F4 Eb4 D4 C4:h", fingers = listOf(1, 2, 3, 4, 5, 4, 3, 2, 1))
            play("G major five-finger.", "G4:q A4 B4 C5 D5 C5 B4 A4 G4:h", from = "G4", to = "D5")
            play("D major five-finger (F♯!).", "D4:q E4 F#4 G4 A4 G4 F#4 E4 D4:h", from = "C4", to = "B4")
            play("A minor five-finger.", "A3:q B3 C4 D4 E4 D4 C4 B3 A3:h", from = "A3", to = "E4")
        }
        lesson("hanon", "Hanon Exercises", "Finger independence.") {
            read("Charles-Louis Hanon (1873)", """
                *The Virtuoso Pianist* contains 60 exercises. Practise them slowly, evenly, with lifted fingers, and **musically**: vary dynamics (pp to ff), articulation (legato/staccato) and rhythms.
            """)
            song("ex-hanon-1", "Hanon No. 1 in the song player - start at 50% speed.")
            song("ex-five-finger-c", "Five-finger pattern, both hands.")
        }
        test {
            play("E major five-finger pattern.", "E4:q F#4 G#4 A4 B4 A4 G#4 F#4 E4:h", from = "D4", to = "C5", hints = false)
            play("D minor five-finger pattern.", "D4:q E4 F4 G4 A4 G4 F4 E4 D4:h", from = "C4", to = "B4", hints = false)
        }
    }
    unit("scales", "Scales in All Keys", "Major and minor scales with correct fingering.") {
        lesson("sharp-keys", "Sharp-Key Major Scales", "G, D, A, E, B.") {
            read("Fingering", "G, D, A, E major use the same fingering as C (RH 123 1234 5). Watch the sharps!",
                visual = table(listOf("Key", "Sharps"), listOf("G", "F♯"), listOf("D", "F♯ C♯"), listOf("A", "F♯ C♯ G♯"), listOf("E", "F♯ C♯ G♯ D♯"), listOf("B", "F♯ C♯ G♯ D♯ A♯")))
            drill(DrillKind.PLAY_SCALE, count = 4, level = 2, keys = listOf("G", "D", "A", "E"))
        }
        lesson("flat-keys", "Flat-Key Major Scales", "F, B♭, E♭, A♭, D♭.") {
            read("Fingering", "F major RH: 1234 1234. For black-key scales, the **thumb plays only white keys** and finger 4 lands on B♭ (in RH).",
                visual = table(listOf("Key", "Flats"), listOf("F", "B♭"), listOf("B♭", "B♭ E♭"), listOf("E♭", "B♭ E♭ A♭"), listOf("A♭", "B♭ E♭ A♭ D♭"), listOf("D♭", "B♭ E♭ A♭ D♭ G♭")))
            drill(DrillKind.PLAY_SCALE, count = 4, level = 2, keys = listOf("F", "Bb", "Eb", "Ab"))
        }
        lesson("minor-scales", "Harmonic Minor Scales", "A, E, D, B, G minor.") {
            play("A harmonic minor.", "A3:q B3 C4 D4 E4 F4 G#4 A4", from = "A3", to = "A4", fingers = listOf(1, 2, 3, 1, 2, 3, 4, 5))
            play("E harmonic minor.", "E4:q F#4 G4 A4 B4 C5 D#5 E5", from = "E4", to = "E5", fingers = listOf(1, 2, 3, 1, 2, 3, 4, 5))
            play("D harmonic minor.", "D4:q E4 F4 G4 A4 Bb4 C#5 D5", from = "D4", to = "D5", fingers = listOf(1, 2, 3, 1, 2, 3, 4, 5))
            drill(DrillKind.PLAY_SCALE, count = 3, level = 3)
        }
        test { drill(DrillKind.PLAY_SCALE, count = 4, level = 3) }
    }
    unit("chords", "Chords & Arpeggios", "Inversions, arpeggios, broken chords.") {
        lesson("inversions", "Triad Inversions", "Up and down the keyboard.") {
            song("ex-chord-inversions", "Triad inversions in C, F and G.")
            drill(DrillKind.PLAY_CHORD, count = 6, level = 1)
        }
        lesson("arpeggios", "Arpeggios", "Flowing across the keys.") {
            song("ex-arpeggio-c", "C major arpeggio, 2 octaves, hands together.")
            play("A minor arpeggio.", "A3:q C4 E4 A4 C5 A4 E4 C4 A3", from = "A3", to = "C5", fingers = listOf(1, 2, 3, 1, 2, 1, 3, 2, 1))
            play("G major arpeggio.", "G3:q B3 D4 G4 B4 G4 D4 B3 G3", from = "G3", to = "B4")
        }
        lesson("advanced", "Octaves, Trills & Chromatics", "Advanced technique.") {
            read("Advanced techniques", """
                - **Octaves**: thumb and 5th finger (4th on black keys), with a loose wrist "bounce". Practise slowly, from the wrist.
                - **Trills**: use fingers 2-3 or 1-3; keep fingers close to the keys; start slow and even.
                - **Chromatic scale**: RH fingers 3 on black keys, 1 on white keys (2 on F and C going up).
                - **Repeated notes**: change fingers (4-3-2-1) on the same key.
            """)
            song("ex-chromatic", "Chromatic scale, one octave up and down.")
            play("Trill practice (fingers 2-3).", "E4:s F4 E4 F4 E4 F4 E4 F4 E4 F4 E4 F4 E4 F4 E4 F4 E4:h", from = "C4", to = "B4", bpm = 60)
        }
        test {
            drill(DrillKind.PLAY_CHORD, count = 5, level = 2)
            play("C major arpeggio.", "C4:q E4 G4 C5 G4 E4 C4", from = "C4", to = "C5", hints = false)
        }
    }
}

