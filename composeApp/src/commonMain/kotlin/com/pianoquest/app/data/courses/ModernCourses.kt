package com.pianoquest.app.data.courses

import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.chords
import com.pianoquest.app.data.course
import com.pianoquest.app.data.kb
import com.pianoquest.app.data.progression
import com.pianoquest.app.data.staff
import com.pianoquest.app.data.table

internal fun popCourse() = course(
    id = "pop", title = "Pop & Modern Songs", subtitle = "Play any song: chords, patterns and playing by ear",
    description = """
        Learn how modern songs really work and how to play them yourself. Chord charts and the Nashville number system,
        the famous four-chord loops, a step-by-step method to figure out any song's chords by ear, and full breakdowns
        of hits like Shape of You and A Thousand Years - plus accompaniment patterns to make them sound great.
    """,
    level = "Beginner → Intermediate", color = 0xFF9A5A2A, icon = CourseIcon.POP, recommendedAfter = "foundations",
) {
    unit("basics", "Pop Piano Basics", "Chord charts, numbers and the left-right split.") {
        lesson("chord-charts", "Chord Charts", "How pop musicians read music.") {
            read("Most pop isn't written in notes", """
                Pop musicians usually read **chord charts** or **lead sheets**: lyrics or melody with **chord symbols** above.
                The basic piano approach:
                - **Left hand**: the **root** (bass note) of each chord, low on the keyboard.
                - **Right hand**: the **chord** (triad) in the middle of the keyboard.

                That's it - with just this you can accompany thousands of songs.
            """, visual = chords("C", "G", "Am", "F"))
            play("Play C - G - Am - F: left hand roots, right hand triads.", "C3+C4+E4+G4:w G2+B3+D4+G4:w A2+A3+C4+E4:w F2+A3+C4+F4:w",
                from = "F2", to = "G4", bpm = 70)
        }
        lesson("inversions", "Smooth Chords with Inversions", "Stop jumping around the keyboard.") {
            read("Keep your hand still", """
                Jumping from root position C to root position G is clunky. Instead, pick the **inversion closest** to where your hand already is:

                **C (C E G) → G/B (B D G) → Am (A C E) → F/A (A C F)**

                Your right hand barely moves, and it sounds smoother - this is how pros play pop.
            """, visual = staff("C4+E4+G4:w B3+D4+G4 A3+C4+E4 A3+C4+F4", names = false), play = "C4+E4+G4:w B3+D4+G4:w A3+C4+E4:w A3+C4+F4:w")
            play("Play the smooth version (right hand only).", "C4+E4+G4:w B3+D4+G4:w A3+C4+E4:w A3+C4+F4:w", from = "A3", to = "G4", bpm = 70)
            quiz("Why use inversions in pop accompaniment?", "Smoother, closer chord changes", "They're louder", "Songs require them", "To change the key")
        }
        lesson("nashville", "The Nashville Number System", "Think in numbers, play in any key.") {
            read("Numbers instead of letters", """
                Session musicians in Nashville write chords as **numbers** of the scale: **1 5 6- 4** instead of C G Am F.
                (Same idea as Roman numerals: a minus sign or 'm' means minor.)

                Why? A singer asks to move the song up a tone - you just play **1 5 6- 4** in D: D A Bm G. No rewriting!
            """, visual = table(listOf("Number", "In C", "In G", "In D", "In A"),
                listOf("1", "C", "G", "D", "A"), listOf("2-", "Dm", "Am", "Em", "Bm"), listOf("3-", "Em", "Bm", "F♯m", "C♯m"),
                listOf("4", "F", "C", "G", "D"), listOf("5", "G", "D", "A", "E"), listOf("6-", "Am", "Em", "Bm", "F♯m")))
            quiz("'1 4 5' in G major is…", "G C D", "C F G", "G D Em", "D G A")
            quiz("'6- 4 1 5' in D major is…", "Bm G D A", "Am F C G", "Em C G D", "F♯m D A E")
            drill(DrillKind.DIATONIC_CHORD, count = 4)
        }
        test {
            quiz("In basic pop accompaniment, the left hand usually plays…", "The root of the chord", "The melody", "Nothing", "The 7th")
            quiz("C to G/B is smoother than C to G because…", "The hand moves less", "It's a different chord", "It's louder", "It's in minor")
            quiz("'2- 5 1' in F major is…", "Gm C F", "Dm G C", "Gm D F", "Am D G")
        }
    }

    unit("four-chords", "The Four-Chord Songs", "Why so many hits share the same chords.") {
        lesson("axis", "The Axis Progression", "I - V - vi - IV.") {
            read("One progression, hundreds of hits", """
                **I - V - vi - IV** (C - G - Am - F) appears in countless songs across decades: "Let It Be", "Someone Like You",
                "Don't Stop Believin'", "With or Without You", "No Woman No Cry", "Apologize", "I'm Yours"...

                Why does it work? It contains the three primary chords (I, IV, V) plus the relative minor (vi) - a perfect balance of home, tension and emotion.
            """, visual = progression("I V vi IV", "C"))
            song("canon-d", "Pachelbel's Canon - the 300-year-old ancestor of I - V - vi - IV, played complete.")
        }
        lesson("rotations", "Rotations & Relatives", "Same chords, different mood.") {
            read("Start somewhere else", """
                Rotate the four chords and the feeling changes:
                - **vi - IV - I - V** (Am F C G): more emotional, minor-feeling. "Zombie", "All of Me", "Despacito", "Counting Stars", "Faded".
                - **I - vi - IV - V** (C Am F G): the 50s doo-wop sound. "Stand By Me", "Perfect", "Every Breath You Take".
                - **IV - V - vi - I**: always pushing forward. "Viva la Vida" uses IV - V - I - vi.
            """, visual = progression("vi IV I V", "C"))
            ear("Which starts on the minor chord?", "A2+A3+C4+E4:h F2+A3+C4+F4:h C3+G3+C4+E4:h G2+G3+B3+D4:h", "vi - IV - I - V", "I - V - vi - IV", bpm = 80)
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 2)
        }
        lesson("minor-loops", "Minor-Key Loops", "The modern dark-pop sound.") {
            read("Minor loops", """
                Many modern hits live in a minor key with a 4-chord loop:
                - **i - VI - III - VII** (Am F C G - the same chords as the axis, felt from the minor side)
                - **i - iv - VI - VII** (Am Dm F G) - "Shape of You" uses this shape in C♯ minor
                - **i - v - ♭VII - IV** (Fm Cm E♭ B♭) - "Blinding Lights"
                - **i - ♭VII - ♭VI - V** - the Andalusian descent ("Hit the Road Jack", "Stray Cat Strut")
            """, visual = progression("i iv VI VII", "Am"))
        }
        test {
            quiz("I - V - vi - IV in G major is…", "G D Em C", "G C D Em", "C G Am F", "G Em C D")
            quiz("vi - IV - I - V in C is…", "Am F C G", "Am Dm G C", "C Am F G", "F G Am C")
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 2)
        }
    }

    unit("by-ear", "Figuring Out Songs by Ear", "A step-by-step method for any song.") {
        lesson("method", "The 6-Step Method", "How to work out the chords of any song.") {
            read("Step 1 - Find the key (the 'home' note)", """
                Play the song and hum the note that feels like **home** - often the last note of the chorus. Find it on the piano by trial: play notes until one matches your hum. That's likely the **tonic**.
            """, mood = MascotMood.TEACHING)
            read("Step 2 - Major or minor?", """
                Play the tonic's **major** triad, then its **minor** triad, while the song plays. Which fits the overall mood? Happy/bright → major. Sad/dark → minor.
            """)
            read("Step 3 - Write out the 'chord family'", """
                Once you know the key, the song will mostly use its **seven diatonic chords** - and mostly I, IV, V and vi (or i, iv, VI, VII in minor). Write them down: in C major → C Dm Em F G Am B°.
            """, visual = chords("C", "Dm", "Em", "F", "G", "Am"))
            read("Step 4 - Follow the bass", """
                Listen for the **lowest note** - the bass line. Hum it, find it on the keyboard. The bass note is usually the **root** of the chord. Write down the bass notes for each chord change.
            """)
            read("Step 5 - Match chords to bass notes", """
                For each bass note, try the diatonic chord built on it. Bass A in C major → probably **Am**. Check by ear: does it fit? If not, try the chord that has that note as its 3rd or 5th (a slash chord, e.g. C/E).
            """)
            read("Step 6 - Check for surprises", """
                If something doesn't fit, it might be a **borrowed chord** (♭VII, ♭VI, iv), a **secondary dominant** (a major chord where you expected minor, like E in C major), or a **sus** chord.

                > Tip: use the **Speed** control in the song player, or apps that slow audio down, and loop short sections.
            """)
            quiz("What's the first step in figuring out a song?", "Find the home note (key)", "Play the melody exactly", "Look for 7th chords", "Transpose it")
            quiz("The bass note is usually the chord's…", "Root", "7th", "9th", "Melody note")
            drill(DrillKind.PROGRESSION_EAR, count = 4, level = 1)
        }
        lesson("shape-of-you", "Song Breakdown: Shape of You", "Ed Sheeran's loop, step by step.") {
            read("Shape of You (Ed Sheeran, 2017)", """
                Let's apply the method:
                1. **Home note**: the song sits around **C♯**.
                2. **Major or minor?** It's dark and moody: **C♯ minor**.
                3. **Chord family** of C♯ minor: C♯m, D♯°, E, F♯m, G♯m, A, B.
                4. **Bass line**: C♯ → F♯ → A → B, repeating forever.
                5. **Matching chords**: **C♯m - F♯m - A - B** = **i - iv - VI - VII**.
                6. **Surprises**: none - the whole song is one loop!
            """, visual = chords("C#m", "F#m", "A", "B").copy(numerals = listOf("i", "iv", "VI", "VII")))
            read("The secret is the rhythm", """
                The famous marimba groove uses the **3-3-2 'tresillo'** rhythm: hits on **1**, the **'&' of 2**, and **4**.
                Count eight eighth notes: **1** 2 3 **4** 5 6 **7** 8.
            """, play = "C#3+C#4+E4+G#4:q. C#3+C#4+E4+G#4:q. C#3+C#4+E4+G#4:q F#2+C#4+F#4+A4:q. F#2+C#4+F#4+A4:q. F#2+C#4+F#4+A4:q", bpm = 96)
            rhythm("Tap the 3-3-2 rhythm.", "q. q. q q. q. q", bpm = 96)
            read("Make it easier", """
                Hard key? **Transpose** to A minor: **Am - Dm - F - G**. All white keys, same sound, just lower. (Or use a transpose button on a digital piano.)
            """, visual = chords("Am", "Dm", "F", "G").copy(numerals = listOf("i", "iv", "VI", "VII")))
            play("Play the loop in A minor (right hand triads).", "A3+C4+E4:w A3+D4+F4:w A3+C4+F4:w B3+D4+G4:w", from = "A3", to = "G4", bpm = 80)
            quiz("Shape of You's progression in C♯ minor is…", "C♯m - F♯m - A - B", "C♯m - A - E - B", "A - E - F♯m - D", "C♯m - G♯m - A - E")
            quiz("In Roman numerals that's…", "i - iv - VI - VII", "i - v - VI - IV", "vi - IV - I - V", "I - IV - V - I")
        }
        lesson("thousand-years", "Song Breakdown: A Thousand Years", "A waltz-time ballad.") {
            read("A Thousand Years (Christina Perri, 2011)", """
                1. **Home note**: B♭.
                2. **Major or minor?** Tender and warm: **B♭ major**.
                3. **Chord family**: B♭, Cm, Dm, E♭, F, Gm, A°.
                4. **Bass line**: B♭ → G → F → E♭ (falling).
                5. **Chords**: **B♭ - Gm - F - E♭** = **I - vi - V - IV**.
                6. **Feel**: a gentle triple meter (counted in 3 or 6/8) - like a slow waltz.
            """, visual = chords("Bb", "Gm", "F", "Eb").copy(numerals = listOf("I", "vi", "V", "IV")))
            read("How to play it", """
                - Left hand: the bass note on beat 1, held.
                - Right hand: the chord on beats 1-2-3, or a rolled **arpeggio** for a music-box feel.
                - Easier key: **C major** → C - Am - G - F.
            """, play = "Bb2+D4+F4+Bb4:q D4+F4+Bb4 D4+F4+Bb4 G2+D4+G4+Bb4:q D4+G4+Bb4 D4+G4+Bb4 F2+C4+F4+A4:q C4+F4+A4 C4+F4+A4 Eb2+Eb4+G4+Bb4:q Eb4+G4+Bb4 Eb4+G4+Bb4", bpm = 132)
            play("Play the progression in C (right hand).", "C4+E4+G4:h. C4+E4+A4:h. B3+D4+G4:h. A3+C4+F4:h.", from = "A3", to = "A4", bpm = 90)
            quiz("The progression of A Thousand Years is…", "I - vi - V - IV", "I - IV - V - I", "vi - IV - I - V", "ii - V - I")
        }
        lesson("more-breakdowns", "More Breakdowns", "Clocks, Hallelujah, Hotel California.") {
            read("Clocks (Coldplay)", """
                **E♭ - B♭m - Fm** (I - v - ii). The minor **v** (B♭m instead of B♭) is a borrowed Mixolydian colour.
                The riff plays each triad **top-down** in eighths, grouped **3+3+2** across the bar.
            """, visual = chords("Eb", "Bbm", "Fm"))
            read("Hallelujah (Leonard Cohen)", """
                The lyrics describe the chords! "It goes like this: the **fourth** (F), the **fifth** (G), the **minor fall** (Am), the **major lift** (F)…"
                Main progression: **C - Am - C - Am - F - G - C - G**.
            """, visual = progression("I vi I vi IV V I V", "C"))
            read("Hotel California (Eagles)", """
                **Bm - F♯ - A - E - G - D - Em - F♯** - in B minor, with the **major V (F♯)** from harmonic minor. A long descending circle.
            """, visual = chords("Bm", "F#", "A", "E", "G", "D", "Em", "F#"))
        }
        test {
            quiz("To find a song's key, you first…", "Find the home note by humming", "Count the bars", "Look up the tempo", "Play the highest note")
            quiz("A Thousand Years uses which feel?", "Triple (waltz-like)", "Straight 4/4 rock", "12-bar blues", "Bossa nova")
            quiz("Shape of You's rhythm pattern is…", "3-3-2", "4-4", "2-2-2-2", "Swing")
            quiz("If a bass note doesn't match a diatonic chord's root, try…", "A slash chord or borrowed chord", "Giving up", "Changing the key", "A different tempo")
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 2)
        }
    }

    unit("patterns", "Accompaniment Patterns", "Make simple chords sound like a record.") {
        lesson("lh-patterns", "Left-Hand Patterns", "From bass notes to flowing arpeggios.") {
            read("Level up your left hand", "", visual = table(listOf("Pattern", "What to play (on C)", "Good for"),
                listOf("Whole note root", "C (held)", "Beginners, ballads"),
                listOf("Octaves", "C2 + C3", "Big, powerful sound"),
                listOf("Root-5th", "C - G", "Folk, country"),
                listOf("1-5-8", "C G C (eighths)", "Pop ballads"),
                listOf("1-5-8-10", "C G C E (eighths)", "Big piano ballads"),
                listOf("Pulsing 8ths", "C C C C C C C C", "Rock, pop drive")))
            read("1-5-8-10 ballad arpeggio", "", play = "C2:e G2 C3 E3 C3 G2 C3 E3 A1 E2 A2 C3 A2 E2 A2 C3 F1 C2 F2 A2 F2 C2 F2 A2 G1 D2 G2 B2 G2 D2 G2 B2", bpm = 80)
            play("Play the 1-5-8 pattern on C.", "C3:e G3 C4 G3 C3 G3 C4 G3", from = "C3", to = "C4")
            song("rainfall", "Rainfall uses flowing left-hand arpeggios.")
        }
        lesson("rh-patterns", "Right-Hand Rhythm Patterns", "Groove with your chords.") {
            read("Rhythm makes the style", """
                Same chords, different style:
                - **Ballad**: hold chords, or arpeggiate gently.
                - **Pop/rock**: steady **eighth-note** chords.
                - **Syncopated pop**: hits on 1, the "&" of 2, and 4 (3-3-2).
                - **Reggae**: chords on the off-beats (the "&s").
                - **Funk**: short, choppy 16th-note stabs.
            """, play = "C4+E4+G4:e C4+E4+G4 C4+E4+G4 C4+E4+G4 C4+E4+G4 C4+E4+G4 C4+E4+G4 C4+E4+G4 R:e C4+E4+G4:e R:e C4+E4+G4:e R:e C4+E4+G4:e R:e C4+E4+G4:e", bpm = 110)
            rhythm("Tap reggae off-beats: rest on the beat, tap on '&'.", "Re e Re e Re e Re e", bpm = 80)
            song("neon-horizon", "Play Neon Horizon with a 3-3-2 groove.")
        }
        lesson("intros", "Intros, Endings & Arranging", "Turn a chord chart into a performance.") {
            read("Arranging tips", """
                - **Intro**: play the chorus or verse chords once with just the right hand, or a riff.
                - **Verse**: sparse - fewer notes, softer, lower register.
                - **Chorus**: fuller - octaves in the left hand, bigger right-hand voicings, louder.
                - **Bridge**: change texture (e.g., only arpeggios).
                - **Ending**: slow down (ritardando), end on a rolled chord or an **add9** for a shimmering finish.
            """, mood = MascotMood.TEACHING)
            read("Add colour", "Replace plain triads with **sus2, add9, maj7** and slash chords for a modern sound:", visual = chords("Cadd9", "G/B", "Am7", "Fmaj7"))
        }
        test {
            quiz("1-5-8 in C is…", "C G C", "C E G", "C F C", "C D E")
            quiz("Reggae rhythm puts chords…", "On the off-beats", "On beat 1 only", "On every 16th", "Never")
            quiz("In a typical arrangement, the chorus should be…", "Fuller and louder than the verse", "Softer than the verse", "Identical to the intro", "Played without chords")
        }
    }
}

internal fun filmCourse() = course(
    id = "film", title = "Film, Ambient & Neo-Classical", subtitle = "Cinematic piano: Einaudi, Yiruma, Zimmer-style",
    description = """
        The modern solo-piano sound: flowing arpeggios, minimalist repetition, sus and add9 colours, ostinatos,
        epic chord movements and how to create emotional, cinematic textures.
    """,
    level = "Beginner → Intermediate", color = 0xFF4E6A5E, icon = CourseIcon.FILM, recommendedAfter = "foundations",
) {
    unit("neo", "Neo-Classical Piano", "Simple ideas, big emotions.") {
        lesson("minimalism", "Minimalism & Repetition", "Less is more.") {
            read("Minimalism", """
                Composers like **Philip Glass**, **Steve Reich**, **Arvo Pärt** and **Ludovico Einaudi** use **repetition** of short patterns that change slowly. The listener settles into a trance-like state, so small changes feel huge.

                Techniques: **ostinato** (repeated pattern), **additive** patterns, slow harmonic change, simple diatonic harmony.
            """, play = "A3:e E4 A4 E4 A3 E4 A4 E4 F3 C4 F4 C4 F3 C4 F4 C4 C4 G4 C5 G4 C4 G4 C5 G4 G3 D4 G4 D4 G3 D4 G4 D4", bpm = 100)
            song("rainfall", "Rainfall: a minimalist neo-classical piece.")
        }
        lesson("colours", "Cinematic Colours", "Sus, add9 and open voicings.") {
            read("The modern piano palette", """
                - **add9** and **sus2** chords: shimmering, ambiguous.
                - **Open voicings**: root + 5th low, 3rd and 9th high.
                - **Pedal tones**: one bass note under changing chords.
                - **Register**: the very high register is delicate and fragile; the low register is ominous and epic.
            """, visual = chords("Cadd9", "Gsus4", "Am7", "Fmaj7"), play = "C2+G2+E4+D5:w G1+D2+C4+D5:w A1+E2+C4+G4:w F1+C2+A3+E4:w", bpm = 60)
        }
        lesson("epic", "Epic Film Harmony", "Chromatic mediants and the minor iv.") {
            read("Hollywood harmony", """
                - **Chromatic mediants** (C → A♭ → E): wonder and awe.
                - **i - VI - III - VII** in minor: heroic struggle.
                - **I - ♭VI - ♭VII - I**: triumph (the 'Mario cadence').
                - **Ostinato** strings + low octave pedals = tension (think Inception, Interstellar).
            """, play = "C2+C3+E4+G4:w Ab1+Ab2+Eb4+C5:w E2+E3+B3+G#4:w C2+C3+E4+G4:w", bpm = 60)
            song("morning-light", "Morning Light: descending bass and slash chords.")
        }
        test {
            quiz("An ostinato is…", "A repeated musical pattern", "A long rest", "A trill", "A key change")
            quiz("C → A♭ major is a…", "Chromatic mediant", "Plagal cadence", "Tritone sub", "Pedal point")
            quiz("Which composer is associated with neo-classical piano?", "Ludovico Einaudi", "J.S. Bach", "Scott Joplin", "Bud Powell")
        }
    }
}

internal fun improvCourse() = course(
    id = "improv", title = "Improvisation & Composition", subtitle = "Create your own music",
    description = """
        Stop only playing other people's music. Learn to improvise with pentatonics and modes, develop motifs,
        write melodies over progressions, harmonise a melody, and structure your own songs.
    """,
    level = "Beginner → Advanced", color = 0xFFA07A2E, icon = CourseIcon.IMPROV, recommendedAfter = "theory",
) {
    unit("improv", "Improvising", "Freedom with a few rules.") {
        lesson("black-keys", "Black-Key Improvisation", "You can't play a wrong note!") {
            read("The pentatonic playground", """
                The five black keys form the **G♭ major pentatonic** (or E♭ minor pentatonic) scale. With no half steps, *every* combination sounds good.

                Try it: left hand holds G♭ + D♭ (a 5th), right hand plays any black keys. Make up short phrases, repeat them, vary them.
            """, visual = kb("C4", "B5", "C#4", "D#4", "F#4", "G#4", "A#4", "C#5", "D#5", "F#5", "G#5", "A#5"))
            read("Go to Free Play!", "Open **Play** and improvise on the black keys for 5 minutes. Record yourself with the ⏺ button and listen back.", mood = MascotMood.EXCITED)
        }
        lesson("motifs", "Motifs & Development", "Say something, then develop it.") {
            read("Think like a storyteller", """
                Great improvisers develop a **motif** (a short idea) rather than playing random notes:
                - **Repeat** it
                - **Sequence** it (same shape, higher or lower)
                - **Vary the rhythm**
                - **Invert** it (upside down)
                - **Extend** or **fragment** it
                - Use **call and response**: a phrase, then an answer.
            """, play = "C4:e D4 E4:q G4:h R:2 D4:e E4 F4:q A4:h R:2 E4:e F4 G4:q C5:h", bpm = 100)
            quiz("Repeating a motif at a different pitch is called…", "Sequence", "Cadence", "Inversion", "Modulation")
        }
        lesson("over-chords", "Improvising Over Chords", "Target chord tones.") {
            read("Chord tones on strong beats", """
                Over a progression, land on **chord tones** (1, 3, 5) on strong beats; use scale notes in between. Over C - Am - F - G, use the C major scale and aim for E or G over C, C or E over Am, A or C over F, B or D over G.
            """, visual = progression("I vi IV V", "C"))
            song("gymnopedie", "Listen to Satie's Gymnopédie: two chords rock back and forth - then improvise your own melody over G - D in the same calm mood.")
        }
        test {
            quiz("Why do black-key improvisations always sound good?", "They form a pentatonic scale with no half steps", "They're louder", "They're higher", "They're in C major")
            quiz("Call and response means…", "A phrase followed by an answering phrase", "Playing loudly then softly", "Using two pianos", "Changing key")
        }
    }

    unit("composition", "Composition", "Write your own pieces.") {
        lesson("melody", "Writing Melodies", "Shape, steps and leaps.") {
            read("Melody writing tips", """
                - Have a clear **shape** (arch, wave, rise-to-climax).
                - Mostly **steps**, with occasional **leaps** - and after a leap, move by step in the other direction.
                - One **high point** (climax) per phrase.
                - Use **repetition** so listeners can follow - but vary it.
                - End phrases on stable notes (1, 3, 5) at cadences.
            """, visual = staff("C4:q D4 E4 G4 A4:h G4 | F4:q E4 D4 E4 C4:w", names = false))
        }
        lesson("harmonise", "Harmonising a Melody", "Choosing chords for a tune.") {
            read("Pick chords that contain the melody note", """
                For each strong beat, find which of **I, IV, V** (or ii, vi) contains the melody note:
                - Melody C, E or G → **I** (C)
                - F, A, or C → **IV** (F)
                - G, B or D → **V** (G)

                Change chords about once per bar, and end with **V → I**.
            """, visual = table(listOf("Melody note", "Possible chords (C major)"),
                listOf("C", "C, F, Am"), listOf("D", "G, Dm, Bdim"), listOf("E", "C, Em, Am"), listOf("F", "F, Dm, G7"),
                listOf("G", "C, G, Em"), listOf("A", "F, Am, Dm"), listOf("B", "G, Em, G7")))
            quiz("Melody note B in C major - which primary chord fits?", "G", "C", "F", "Am")
            quiz("Melody note A - which primary chord fits?", "F", "G", "C", "E")
        }
        lesson("song-form", "Writing a Song", "Verse, chorus, bridge.") {
            read("A simple songwriting recipe", """
                1. Choose a **key** and **tempo** that fit the mood.
                2. Write a **chorus** progression (e.g., I - V - vi - IV) and a memorable **hook** melody.
                3. Write a **verse** with a different progression or the same chords in a different rhythm, lower and calmer.
                4. Add a **bridge**: something new - e.g. start on IV or vi, or modulate.
                5. Form: Intro - V - C - V - C - Bridge - C - C - Outro.
            """, mood = MascotMood.EXCITED)
            read("Your turn", "Use **Tools → Progression Player** to audition progressions, then compose a melody over it in **Play** and record it!")
        }
        test {
            quiz("After a large leap, a melody often moves…", "By step in the opposite direction", "By another leap the same way", "Nowhere", "Up an octave")
            quiz("Melody note E in C major - which chord does NOT contain it?", "G", "C", "Am", "Em")
        }
    }
}

