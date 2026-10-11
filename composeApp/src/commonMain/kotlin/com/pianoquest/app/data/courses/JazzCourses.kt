package com.pianoquest.app.data.courses

import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.ComposerCard
import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.composers
import com.pianoquest.app.data.image
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Visual
import com.pianoquest.app.data.chords
import com.pianoquest.app.data.course
import com.pianoquest.app.data.kb
import com.pianoquest.app.data.progression
import com.pianoquest.app.data.staff
import com.pianoquest.app.data.table

internal fun jazzCourse() = course(
    id = "jazz", title = "Jazz Piano", subtitle = "Swing, voicings, comping and improvisation",
    description = """
        From ragtime and stride to bebop and modal jazz. Learn to read lead sheets, voice seventh chords like a pro
        (shells, guide tones, rootless voicings), comp with swing, walk a bass line, and improvise over the ii-V-I.
    """,
    level = "Intermediate → Advanced", color = 0xFF2A170D, icon = CourseIcon.JAZZ, recommendedAfter = "theory",
) {
    unit("foundations", "Jazz Foundations", "History, swing feel and lead sheets.") {
        lesson("history", "A Short History of Jazz Piano", "From New Orleans to today.") {
            read("Jazz piano timeline", "", visual = table(listOf("Era", "Style", "Pianists"),
                listOf("1890s-1910s", "Ragtime", "Scott Joplin"),
                listOf("1920s", "Stride, early jazz", "James P. Johnson, Fats Waller, Jelly Roll Morton"),
                listOf("1930s", "Swing", "Art Tatum, Teddy Wilson, Count Basie"),
                listOf("1940s", "Bebop", "Bud Powell, Thelonious Monk"),
                listOf("1950s", "Cool & hard bop", "Bill Evans, Horace Silver, Red Garland"),
                listOf("1959+", "Modal jazz", "Bill Evans, McCoy Tyner"),
                listOf("1960s-70s", "Post-bop, fusion", "Herbie Hancock, Chick Corea, Keith Jarrett"),
                listOf("Today", "Everything!", "Brad Mehldau, Robert Glasper, Hiromi")))
            read("What makes it jazz?", """
                - **Swing** rhythm and syncopation
                - **Improvisation** - creating music on the spot
                - **Extended harmony** - 7ths, 9ths, 13ths, alterations
                - **Interaction** - musicians listening and responding
                - The **blues** influence everywhere
            """, mood = MascotMood.EXCITED)
            read("Pioneers of jazz piano", "From ragtime to bebop:", visual = composers(
                ComposerCard("joplin", "Scott Joplin", "c.1868-1917", "The King of Ragtime", "maple-leaf"),
                ComposerCard("art_tatum", "Art Tatum", "1909-1956", "Breathtaking virtuoso of swing-era piano (photo: W. P. Gottlieb)", null),
                ComposerCard("thelonious_monk", "Thelonious Monk", "1917-1982", "Bebop's great original (photo: W. P. Gottlieb)", null),
            ))
            quiz("Which style came first?", "Ragtime", "Bebop", "Modal jazz", "Fusion")
            quiz("Bill Evans is closely associated with…", "Modal jazz & rootless voicings", "Ragtime", "Boogie-woogie", "Baroque")
        }
        lesson("swing", "Swing Feel", "Long-short eighths and the backbeat.") {
            read("Swinging eighths", """
                Jazz eighth notes are usually **swung**: the first of each pair is longer, the second shorter and lighter - close to a **triplet** feel: "**doo**-ba **doo**-ba".

                Accent the **off-beats** ("&s") slightly, and feel beats **2 and 4** (where the drummer's hi-hat closes and listeners snap their fingers).
            """, play = "C5:qt D5:t E5:qt F5:t G5:qt A5:t G5:qt E5:t", bpm = 120)
            ear("Straight or swung?", "C5:qt D5:t E5:qt F5:t G5:qt F5:t E5:qt D5:t", "Swung", "Straight", bpm = 120)
            ear("Straight or swung?", "C5:e D5 E5 F5 G5 F5 E5 D5", "Straight", "Swung", bpm = 120)
            rhythm("Snap on 2 and 4: tap only on beats 2 and 4.", "Rq q Rq q Rq q Rq q", bpm = 96)
            quiz("In swing, accents are usually felt on beats…", "2 and 4", "1 and 3", "1 only", "All equally")
        }
        lesson("lead-sheets", "Reading Lead Sheets", "Melody + chord symbols = freedom.") {
            read("The jazz lead sheet", """
                A lead sheet shows only the **melody** and **chord symbols**. You decide the voicings, rhythm and bass line.

                A collection of lead sheets is a "**fake book**" (e.g., The Real Book). Most standards follow **32-bar AABA** or **ABAC** form.

                Common jazz symbols: **Δ** = major 7, **-** = minor, **ø** = half-diminished, **°** = diminished, **alt** = altered dominant.
            """, visual = chords("Dm7", "G7", "Cmaj7", "A7"))
            drill(DrillKind.CHORD_NAME, count = 5, level = 2)
        }
        test {
            quiz("Swung eighths are played…", "Long-short", "Short-long", "Evenly", "Staccato only")
            quiz("A 'fake book' contains…", "Lead sheets", "Full orchestral scores", "Only chord charts with no melody", "Exercises")
            quiz("'CΔ' means…", "C major 7", "C diminished", "C augmented", "C minor")
        }
    }

    unit("voicings", "Jazz Voicings", "Shells, guide tones and rootless voicings.") {
        lesson("ii-v-i", "The ii-V-I", "Jazz's most important progression.") {
            read("ii - V - I", """
                Most jazz standards are chains of **ii-V-I** progressions in various keys. In C: **Dm7 - G7 - Cmaj7**.

                In minor: **iiø7 - V7(♭9) - i** → **Dm7♭5 - G7♭9 - Cm7** (or Cm6).
            """, visual = progression("ii7 V7 Imaj7", "C"))
            read("Learn it in all 12 keys", "Follow the circle of fifths: C, F, B♭, E♭, A♭, D♭, G♭, B, E, A, D, G.", visual = Visual.CircleOfFifths("C"))
            quiz("ii-V-I in F major is…", "Gm7 - C7 - Fmaj7", "Dm7 - G7 - Cmaj7", "Cm7 - F7 - B♭maj7", "Am7 - D7 - Gmaj7")
            quiz("ii-V-I in B♭ major is…", "Cm7 - F7 - B♭maj7", "Gm7 - C7 - Fmaj7", "Fm7 - B♭7 - E♭maj7", "Dm7 - G7 - Cmaj7")
            drill(DrillKind.DIATONIC_CHORD, count = 3)
        }
        lesson("shells", "Shell Voicings", "Root, 3rd and 7th - the essentials.") {
            read("Shells", """
                The **3rd and 7th** define a chord's quality; the 5th adds little. A **shell voicing** plays just **root + 3rd + 7th** (or root + 7th + 3rd).

                Bud Powell made this his signature left-hand sound, freeing the right hand for bebop lines.
            """, visual = chords("Dm7", "G7", "Cmaj7"))
            read("Two shapes", """
                - **Shape A**: Root - 7th (left hand), 3rd above
                - **Shape B**: Root - 3rd, 7th above
                Alternate them through a ii-V-I so your hand barely moves:
                Dm7 (D-C-F) → G7 (G-F-B) → Cmaj7 (C-B-E)
            """, play = "D3+C4+F4:w G2+F3+B3:w C3+B3+E4:w", visual = staff("D3+C4:w G2+F3 C3+B3", clef = Clef.BASS))
            tap("Play a shell for G7: G + B + F.", "G3", "B3", "F4", from = "C3", to = "B4")
            tap("Play a shell for Dm7: D + F + C.", "D3", "F3", "C4", from = "C3", to = "B4")
        }
        lesson("guide-tones", "Guide-Tone Lines", "The smoothest path through the changes.") {
            read("Guide tones", """
                The **3rds and 7ths** of a ii-V-I form two smooth lines:
                - The **7th of Dm7 (C)** falls a half step to the **3rd of G7 (B)**.
                - The **7th of G7 (F)** falls to the **3rd of Cmaj7 (E)**.

                Every ii-V-I works this way. Playing these lines is the secret to sounding "inside" the harmony.
            """, play = "F4+C5:w F4+B4:w E4+B4:w", visual = staff("F4+C5:w F4+B4 E4+B4", names = true))
            song("guide-tones", "Practise guide tones through ii-V-Is in three keys.")
        }
        lesson("rootless", "Rootless Voicings", "The modern jazz piano sound.") {
            read("Let the bassist play the root", """
                In a band, the bass plays roots - so pianists drop them and add colour (9ths, 13ths). **Bill Evans** and **Wynton Kelly** popularised these four-note **rootless voicings**:

                - **Type A**: 3 - 5 - 7 - 9
                - **Type B**: 7 - 9 - 3 - 5
            """, visual = table(listOf("Chord", "Type A (3-5-7-9)", "Type B (7-9-3-5)"),
                listOf("Dm9", "F A C E", "C E F A"), listOf("G13", "F A B E (7-9-3-13)", "B E F A (3-13-7-9)"),
                listOf("Cmaj9", "E G B D", "B D E G")))
            read("ii-V-I with rootless voicings", "Type A on Dm9 → Type B on G13 → Type A on Cmaj9: almost no hand movement!",
                play = "D2+F3+A3+C4+E4:w G1+F3+A3+B3+E4:w C2+E3+G3+B3+D4:w", bpm = 70)
            tap("Play the Dm9 Type A voicing: F A C E.", "F3", "A3", "C4", "E4", from = "C3", to = "B4")
            tap("Play G13 (rootless): F A B E.", "F3", "A3", "B3", "E4", from = "C3", to = "B4")
            quiz("Type A rootless voicing is built…", "3 - 5 - 7 - 9", "1 - 3 - 5 - 7", "7 - 9 - 3 - 5", "1 - 5 - 9")
        }
        lesson("quartal", "Quartal Voicings", "Stacked fourths: the modal sound.") {
            read("So What chords", """
                **McCoy Tyner** and **Bill Evans** used chords built in **4ths** instead of 3rds. The famous **'So What' voicing**: three perfect 4ths + a major 3rd on top: **E A D G B** over Dm (Dorian).

                Quartal voicings are ambiguous and open - perfect for modal tunes.
            """, play = "E3+A3+D4+G4+B4:w D3+G3+C4+F4+A4:w", bpm = 60, visual = staff("E3+A3+D4+G4+B4:w", clef = Clef.GRAND))
            quiz("Quartal chords are built in…", "4ths", "3rds", "2nds", "Octaves")
        }
        test {
            quiz("Which two notes define a chord's quality?", "3rd and 7th", "Root and 5th", "5th and 9th", "Root and octave")
            quiz("Rootless voicings omit the root because…", "The bassist plays it", "It sounds bad", "It's too low", "Jazz has no roots")
            quiz("In a ii-V-I, the 7th of the ii chord moves to the…", "3rd of the V chord", "Root of the V chord", "5th of the I chord", "9th of the V chord")
            tap("Play a G7 shell (G B F).", "G3", "B3", "F4", from = "C3", to = "B4")
        }
    }

    unit("comping", "Comping & Bass Lines", "Accompanying with rhythm and walking bass.") {
        lesson("comp-rhythm", "Comping Rhythms", "Accompaniment that swings.") {
            read("Comping = complementing", """
                **Comping** means accompanying a soloist with short, rhythmic chords. Less is more: leave space!

                Classic rhythms:
                - **Charleston**: hit on beat 1 and the "and" of 2 ("dotted quarter + eighth").
                - **Reverse Charleston**: hits on the "and" of 1 and beat 3.
                - **On 2 and 4**: Freddie Green-style four-to-the-bar lightness.
                - **Anticipation**: hit the next chord an eighth early (the "and" of 4).
            """, play = "D4+F4+A4+C5:q. D4+F4+A4+C5:e R:h G3+B3+F4:q. G3+B3+F4:e R:h", bpm = 110)
            rhythm("Tap the Charleston rhythm: dotted quarter, eighth, rest, rest.", "q. e Rq Rq", bpm = 100)
            rhythm("Reverse Charleston: rest-eighth, eighth, quarter… (rest, hit on '&1', hit on 3)", "Re e Rq q Rq", bpm = 100)
        }
        lesson("walking", "Walking Bass", "Quarter notes that drive the swing.") {
            read("Walking bass lines", """
                The bassist "walks" in steady quarter notes. Pianists play walking bass when there's no bass player.

                Simple recipe for each bar of a chord:
                1. **Beat 1**: the root
                2. **Beats 2-3**: chord tones or scale steps
                3. **Beat 4**: an **approach note** a half step above or below the next root
            """, visual = staff("D3:q F3 A3 Ab3 | G2 B2 D3 Db3 | C3 E3 G3 B2", clef = Clef.BASS, names = true),
                play = "D3:q F3 A3 Ab3 G2 B2 D3 Db3 C3 E3 G3 B2 C3:w", bpm = 120)
            play("Walk a bar of Dm7 into G7: D F A A♭ → G", "D3:q F3 A3 Ab3 G2:w", from = "G2", to = "B3")
            song("walkin-f", "Hear walking bass with comping in a jazz blues.")
        }
        lesson("stride", "Stride Piano", "The one-person orchestra.") {
            read("Stride", """
                Born in Harlem in the 1920s (James P. Johnson, Fats Waller, Willie 'The Lion' Smith, Art Tatum). The left hand leaps between:
                - **Beats 1 and 3**: a low bass note or **10th** (root + 3rd an octave up)
                - **Beats 2 and 4**: a mid-range chord

                It descends from **ragtime**'s "oom-pah" bass but swings harder.
            """, play = "C2:q E3+G3+C4 G1 E3+G3+C4 F2 F3+A3+C4 C2 F3+A3+C4 G1 F3+G3+B3 D2 F3+G3+B3 C2:h E3+G3+C4:h", bpm = 110)
            song("stride-steps", "Play Stride Steps.")
            song("entertainer", "Ragtime: Joplin's The Entertainer.")
        }
        test {
            quiz("The Charleston rhythm hits…", "Beat 1 and the '&' of 2", "Beats 2 and 4", "Every eighth note", "Beat 4 only")
            quiz("In a walking bass, beat 4 is often…", "A chromatic approach note", "The root", "A rest", "An octave jump")
            quiz("Stride piano's left hand alternates…", "Bass notes and mid-range chords", "Trills and scales", "Both hands' melody", "Octave runs")
        }
    }

    unit("improv", "Jazz Improvisation", "Chord tones, approach notes, licks and the blues.") {
        lesson("chord-tones", "Start with Chord Tones", "Target notes that always work.") {
            read("Arpeggiate the changes", """
                The simplest way to sound "right": play **chord tones** (1-3-5-7) on the strong beats. Then connect them with scale steps.

                Over Dm7 - G7 - Cmaj7, try: D F A C | B D F A | G E C B…
            """, play = "D4:e F4 A4 C5 B4 G4 F4 D4 E4 G4 B4 D5 C5:h", bpm = 120)
            play("Arpeggiate Dm7 → G7 → Cmaj7.", "D4:q F4 A4 C5 B4 G4 F4 D4 E4 G4 B4 D5 C5:w", from = "C4", to = "E5")
        }
        lesson("approach", "Approach Notes & Enclosures", "The bebop language.") {
            read("Chromatic approaches", """
                - **Chromatic approach**: arrive at a target note from a half step below or above.
                - **Enclosure**: surround the target - one note above, one below, then the target (e.g., F - D♯ - **E**).
                - **Bebop scale**: Mixolydian plus a passing major 7th, so chord tones fall on the beats in eighth-note lines.
            """, play = "F5:e D#5 E5:q C5:e B4 C5:q", bpm = 110)
            play("Play an enclosure onto E: F → D♯ → E.", "F4:e D#4 E4:h", from = "C4", to = "B4")
        }
        lesson("blues-improv", "Blues Improvisation", "The foundation of all jazz soloing.") {
            read("Blues scale soloing", """
                Over a 12-bar blues in C, the **C blues scale** (C E♭ F G♭ G B♭) works over every chord. Add the major 3rd (E) by sliding from E♭ - the classic **"crushed" grace note**.

                Tips:
                - Use **repetition** and **call and response**.
                - Leave **space** - the rests make the phrases speak.
                - Develop one **motif** instead of running up and down the scale.
            """, play = "Eb4:s E4:e. G4:q C5:q R:q Bb4:e G4 Gb4 F4 Eb4 C4:q", bpm = 100)
            song("blue-monday", "Solo over Blue Monday Shuffle - or study its call-and-response licks.")
            read("Try it in Free Play", "Open **Free Play**, turn on the metronome, and improvise with the C blues scale. The black keys + C and F work wonderfully!", mood = MascotMood.EXCITED)
        }
        test {
            quiz("An enclosure surrounds a target note with…", "Notes above and below it", "Two octaves", "Rests", "A trill")
            quiz("The bebop dominant scale adds a passing…", "Major 7th", "Minor 2nd", "Sharp 4th", "Flat 3rd")
            quiz("A 'crushed' grace note in blues slides…", "From the ♭3 to the 3", "From the 5 to the 6", "From the root to the 9", "Down an octave")
        }
    }

    unit("standards", "Standards & Reharmonisation", "Forms, turnarounds, substitutions and modal jazz.") {
        lesson("forms", "Jazz Forms", "AABA, blues, rhythm changes.") {
            read("The standard forms", "", visual = table(listOf("Form", "Structure", "Examples (style)"),
                listOf("32-bar AABA", "A(8) A(8) B(8 'bridge') A(8)", "Many Tin Pan Alley standards"),
                listOf("32-bar ABAC", "A B A C", "Ballads and show tunes"),
                listOf("12-bar blues", "I IV I / IV I / V IV I", "Jazz blues"),
                listOf("Rhythm changes", "AABA, A = I-vi-ii-V turnarounds, bridge = III7-VI7-II7-V7", "Based on Gershwin's 'I Got Rhythm'"),
                listOf("Modal", "Long stretches of one mode", "Dorian vamps")))
            quiz("In AABA form, the B section is called the…", "Bridge", "Chorus", "Coda", "Turnaround")
        }
        lesson("turnarounds", "Turnarounds", "Circling back to the top.") {
            read("Turnarounds", """
                A **turnaround** is a short progression at the end of a section that leads back to the start:
                - **I - vi - ii - V** (C - Am7 - Dm7 - G7)
                - **iii - VI - ii - V** (Em7 - A7 - Dm7 - G7)
                - **I - ♭III - ♭VI - ♭II** (tritone-sub turnaround: C - E♭7 - A♭7 - D♭7)
            """, visual = progression("I vi7 ii7 V7", "C"))
            read("Hear the tritone-sub turnaround", "", play = "C3+E3+G3+B3:h Eb3+G3+Db4:h Ab2+C4+Gb4:h Db3+F3+B3:h C3+E3+G3+B3:w", bpm = 90)
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 3)
        }
        lesson("reharm", "Reharmonisation", "New chords under old melodies.") {
            read("Reharmonisation techniques", """
                - **Tritone substitution**: replace V7 with ♭II7 (G7 → D♭7).
                - **Add a ii before every V**: G7 → Dm7 - G7.
                - **Backdoor ii-V**: iv7 - ♭VII7 - I (Fm7 - B♭7 - C).
                - **Diminished passing chords**: C - C♯°7 - Dm7.
                - **Chromatic planing**: slide a voicing by half steps into the target.
                - **Coltrane changes**: key centres a major 3rd apart (advanced!).
            """, visual = chords("Fm7", "Bb7", "Cmaj7"))
            quiz("A 'backdoor' ii-V into C uses…", "Fm7 - B♭7", "Dm7 - G7", "Em7 - A7", "D♭7 - G♭7")
            quiz("A diminished passing chord between C and Dm7 is…", "C♯°7", "D♯°7", "B°7", "F♯°7")
        }
        lesson("modal-jazz", "Modal Jazz", "Kind of Blue and beyond.") {
            read("Modal jazz", """
                In 1959, Miles Davis's *Kind of Blue* (with Bill Evans) replaced fast chord changes with long stretches of a single **mode**. "So What" is 16 bars of D Dorian, 8 of E♭ Dorian, 8 of D Dorian.

                Improvise with the mode's colour notes, use **quartal voicings** and **pentatonics** - and let melodies breathe.
            """, play = "D3+G3+C4+F4+A4:w E3+A3+D4+G4+B4:w", bpm = 70)
            quiz("'So What' is mostly in which mode?", "Dorian", "Lydian", "Phrygian", "Locrian")
        }
        test {
            drill(DrillKind.PROGRESSION_EAR, count = 3, level = 3)
            quiz("I - vi - ii - V in C is…", "C - Am - Dm - G", "C - F - G - C", "C - Em - Am - G", "Am - Dm - G - C")
            quiz("Rhythm changes are based on…", "'I Got Rhythm'", "'So What'", "12-bar blues", "Pachelbel's Canon")
            quiz("Tritone sub for G7 is…", "D♭7", "C♯m7", "F♯7", "A♭7")
        }
    }
}

internal fun bluesCourse() = course(
    id = "blues", title = "Blues & Boogie-Woogie", subtitle = "12-bar blues, shuffles, boogie bass and licks",
    description = """
        The blues is the root of jazz, rock, R&B and pop. Master the 12-bar form, the blues scale, shuffle feel,
        boogie-woogie left hands, turnarounds, intros and classic licks.
    """,
    level = "Beginner → Intermediate", color = 0xFF34465A, icon = CourseIcon.BLUES, recommendedAfter = "foundations",
) {
    unit("form", "The 12-Bar Blues", "The most important form in popular music.") {
        lesson("twelve-bar", "The 12-Bar Form", "Three chords, twelve bars.") {
            read("The blues progression", """
                The **12-bar blues** uses the **I, IV and V** chords (usually all dominant 7ths) in a fixed pattern:

                | I | I | I | I |
                | IV | IV | I | I |
                | V | IV | I | V |

                In C: C7 C7 C7 C7 | F7 F7 C7 C7 | G7 F7 C7 G7.
                A common variation, the **quick change**, uses IV in bar 2.
            """, visual = chords("C7", "F7", "G7").copy(numerals = listOf("I7", "IV7", "V7")))
            read("Lyrics follow the form", """
                Classic blues lyrics use **AAB**: a line (bars 1-4), the same line repeated (bars 5-8), then an answer that rhymes (bars 9-12).
            """)
            quiz("How many bars in a standard blues?", "12", "8", "16", "32")
            quiz("Bar 5 of a 12-bar blues is the…", "IV chord", "I chord", "V chord", "vi chord")
            quiz("Bar 9 of a 12-bar blues is the…", "V chord", "IV chord", "I chord", "ii chord")
            song("blue-monday", "Hear the 12-bar form in Blue Monday Shuffle.")
        }
        lesson("blues-scale", "The Blues Scale", "Minor pentatonic + the blue note.") {
            read("C blues scale", "**C E♭ F G♭ G B♭ C** - six notes, works over the whole 12-bar blues in C.",
                visual = kb("C4", "C5", "C4", "Eb4", "F4", "Gb4", "G4", "Bb4", "C5"), play = "C4:e Eb4 F4 Gb4 G4 Bb4 C5 Bb4 G4 Gb4 F4 Eb4 C4:h")
            play("Play the C blues scale up and down.", "C4:q Eb4 F4 Gb4 G4 Bb4 C5 Bb4 G4 Gb4 F4 Eb4 C4", from = "C4", to = "C5")
            read("Blue notes", """
                The "**blue notes**" are the ♭3, ♭5 and ♭7 - pitches that bend between major and minor, inherited from African-American vocal traditions. On piano we imitate the bend by **crushing** ♭3 into 3 (a quick grace note).
            """, play = "Eb4:s E4:e. C4:q Eb4:s E4:e. G4:q", bpm = 90)
        }
        lesson("shuffle", "Shuffle & Swing Feel", "The blues groove.") {
            read("The shuffle", "Like jazz swing: long-short eighths in a triplet feel - 'DOO-ba DOO-ba'. Many blues tunes are slow and heavy; others are bouncy and fast.",
                play = "C3:qt E3:t G3:qt A3:t Bb3:qt A3:t G3:qt E3:t", bpm = 110)
            rhythm("Tap a shuffle: long-short, long-short.", "qt t qt t qt t qt t", bpm = 90)
        }
        test {
            quiz("Which chords make up a basic 12-bar blues?", "I, IV, V", "I, ii, V", "I, vi, IV, V", "ii, V, I")
            quiz("The blue note in the blues scale is the…", "♭5", "♯4 only", "Major 7", "♭2")
            play("Play the C blues scale ascending.", "C4:q Eb4 F4 Gb4 G4 Bb4 C5", from = "C4", to = "C5", hints = false)
        }
    }

    unit("boogie", "Boogie-Woogie Left Hand", "Driving bass patterns.") {
        lesson("patterns", "Boogie Bass Patterns", "The patterns that launched rock 'n' roll.") {
            read("Walking boogie", "The most famous pattern climbs root - 3 - 5 - 6 - ♭7 - 6 - 5 - 3 in eighths:",
                visual = staff("C3:e E3 G3 A3 Bb3 A3 G3 E3", clef = Clef.BASS, names = true), play = "C3:e E3 G3 A3 Bb3 A3 G3 E3 C3 E3 G3 A3 Bb3 A3 G3 E3", bpm = 130)
            play("Play the walking boogie on C.", "C3:e E3 G3 A3 Bb3 A3 G3 E3", from = "C3", to = "C4")
            read("Other patterns", """
                - **Octave boogie**: bouncing octaves C2-C3 on every eighth.
                - **5-6 rock 'n' roll**: root+5, root+6 alternating (Chuck Berry guitar style).
                - **Broken octave + 5-6-♭7**: the Pete Johnson / Albert Ammons sound.
            """, play = "C3+G3:e C3+G3 C3+A3 C3+A3 C3+G3 C3+G3 C3+A3 C3+A3", bpm = 140)
            song("boogie-train", "Play Boogie Train - left-hand boogie with right-hand stabs.")
        }
        lesson("turnarounds", "Intros, Turnarounds & Endings", "Classic blues clichés.") {
            read("The blues turnaround", "Bars 11-12 lead back to the top. A classic descending turnaround over C - G7:",
                play = "C4+C5:e Bb4 C4+A4 Ab4 C4+G4:q G3+B3+F4:h", bpm = 90)
            read("Endings", "The classic blues ending: a chromatic climb into a C7(♯9) or C9 chord - with a dramatic tremolo.",
                play = "G3:e Ab3 A3 Bb3 B3 C4:q C3+E3+Bb3+D#4:w", bpm = 100)
        }
        lesson("licks", "Blues Licks", "Vocabulary for your right hand.") {
            read("Essential licks", """
                - **Crushed 3rd**: E♭→E grace note into a chord.
                - **Tremolo**: shake between two notes (e.g., G and C) rapidly.
                - **Repeated-note lick**: hammer the same note over changing chords.
                - **Call and response**: a phrase, then an answer.
            """, play = "Eb4:s E4:e. G4+C5:q G4:s C5 G4 C5 G4 C5 G4 C5 Bb4:e G4 Eb4 C4:h", bpm = 100)
            song("minor-blues", "Hear licks over a minor blues.")
        }
        test {
            quiz("The walking boogie pattern on C is…", "C E G A B♭ A G E", "C D E F G A B C", "C G C G C G C G", "C E♭ F G♭ G B♭")
            quiz("A turnaround leads…", "Back to the beginning of the form", "To a new key", "To the bridge", "To the end only")
            play("Play the walking boogie.", "C3:e E3 G3 A3 Bb3 A3 G3 E3", from = "C3", to = "C4", hints = false)
        }
    }

    unit("variations", "Blues Variations", "Minor blues, 8-bar and 16-bar blues, jazz blues.") {
        lesson("minor", "Minor Blues", "The darker side.") {
            read("Minor blues", "i - iv - i - ♭VI7 - V7 - i. In A minor: Am7 Am7 Am7 Am7 | Dm7 Dm7 Am7 Am7 | F7 E7 Am7 E7",
                visual = progression("i7 iv7 bVI7 V7", "Am"))
            song("minor-blues", "Play Midnight Minor Blues.")
        }
        lesson("other-forms", "8-Bar, 16-Bar & Jazz Blues", "More blues shapes.") {
            read("Other forms", """
                - **8-bar blues**: I - V - IV - IV - I - V - I - V ("Key to the Highway" style)
                - **16-bar blues**: extends the I or adds a repeated section
                - **Jazz blues**: adds ii-V's, a quick IV, secondary dominants and a I-VI-ii-V turnaround
            """, visual = progression("I7 IV7 I7 ii7 V7", "F"))
            song("walkin-f", "Hear a jazz blues: Walkin' the F Blues.")
        }
        test {
            quiz("In a minor blues in A, bar 9 is often…", "F7 (♭VI7)", "G7", "Dm7", "C7")
            quiz("A jazz blues adds…", "ii-V's and passing chords", "Fewer chords", "Only minor chords", "No dominant chords")
        }
    }
}

internal fun gospelCourse() = course(
    id = "gospel", title = "Gospel & Soul", subtitle = "Church chords, passing chords and soulful runs",
    description = """
        Gospel piano is the source of the rich chords in soul, R&B and neo-soul. Learn the 'Amen' cadence,
        walk-ups, diminished passing chords, 2-5-1s, 7-3-6 progressions, gospel runs and neo-soul voicings.
    """,
    level = "Intermediate", color = 0xFF5E3A5A, icon = CourseIcon.GOSPEL, recommendedAfter = "theory",
) {
    unit("gospel-harmony", "Gospel Harmony", "The church sound.") {
        lesson("amen", "The 1-4-1 and Amen Cadence", "Where gospel begins.") {
            read("Numbers, not letters", """
                Gospel musicians talk in **numbers**: "the 1", "the 4", "a 2-5-1". (Like Roman numerals - it works in every key.)

                The foundation: **1 - 4 - 1**, and the **Amen (plagal) cadence** 4 → 1. Add colour: **4 → 4m → 1** for extra emotion.
            """, visual = progression("I IV iv I", "C"), play = "C3+E4+G4+C5:h F3+F4+A4+C5:h F3+F4+Ab4+C5:h C3+E4+G4+C5:w")
            tap("Play the 4 chord in C (F A C).", "F4", "A4", "C5", from = "C4", to = "C5")
        }
        lesson("passing", "Passing Chords & Walk-Ups", "Movement between the main chords.") {
            read("Diminished passing chords", """
                Connect 1 to 2 with a **♯1 diminished 7th**: C → C♯°7 → Dm7. Or 4 to 5 with ♯4°7: F → F♯°7 → C/G.

                The classic gospel **walk-up**: 1 - 1/3 - 4 - ♯4°7 - 1/5 - 6(7) - 2m7 - 5(7) - 1
            """, visual = chords("C", "C/E", "F", "F#dim7", "C/G", "A7", "Dm7", "G7", "C"))
            song("gospel-sunday", "Hear the gospel walk-up in Sunday Morning.")
        }
        lesson("736", "7-3-6 and Dominant Chains", "Circle progressions, gospel style.") {
            read("7-3-6-2-5-1", """
                Gospel loves chains of dominants walking around the circle: **7 - 3 - 6 - 2 - 5 - 1**. In C: **Bø7 - E7 - Am7 - D7 - Dm7/G7 - C**. Often each chord gets altered tensions (♯9, ♭9, ♭13).
            """, visual = chords("Bm7b5", "E7", "Am7", "D7", "G7", "C"))
            quiz("In gospel numbers, 'the 5' in C is…", "G", "F", "A", "E")
        }
        lesson("runs", "Gospel Runs & Fills", "Pentatonic flourishes.") {
            read("Runs", """
                Gospel pianists fill gaps with fast **runs**, usually from the **major or minor pentatonic** (or blues) scale, often in groups of 3 or 4 descending:
            """, play = "C6:s A5 G5 E5 D5 C5 A4 G4 E4 D4 C4:q", bpm = 90)
            play("Play a descending pentatonic run.", "C6:e A5 G5 E5 D5 C5 A4 G4 E4 D4 C4:h", from = "C4", to = "C6")
        }
        lesson("neosoul", "Neo-Soul Voicings", "Modern R&B colours.") {
            read("Neo-soul", """
                Artists like D'Angelo, Erykah Badu and Robert Glasper use: **minor 9ths and 11ths**, **major 9ths**, **cluster voicings**, **chromatic slides** into chords, and laid-back, behind-the-beat rhythm.

                Try **Fmaj9 - Em7 - Dm9 - Cmaj9** with lush voicings.
            """, play = "F2+E3+A3+C4+G4:w E2+D3+G3+B3:w D2+C3+F3+A3+E4:w C2+B2+E3+G3+D4:w", bpm = 60)
        }
        test {
            quiz("The 'Amen' cadence is…", "4 → 1", "5 → 1", "2 → 5", "1 → 6")
            quiz("A diminished passing chord between C and Dm7 is…", "C♯°7", "D♯°7", "E°7", "B°7")
            quiz("Gospel runs are often built from the…", "Pentatonic scale", "Chromatic scale only", "Whole-tone scale", "Locrian mode")
        }
    }
}

internal fun latinCourse() = course(
    id = "latin", title = "Latin Piano", subtitle = "Clave, bossa nova, montuno, tango",
    description = """
        The rhythms of Latin America: the clave, Afro-Cuban montunos, Brazilian bossa nova and samba,
        Argentine tango and the habanera - plus the tresillo that powers today's reggaeton and pop.
    """,
    level = "Intermediate", color = 0xFFA0522D, icon = CourseIcon.LATIN, recommendedAfter = "theory",
) {
    unit("rhythm", "Latin Rhythm", "Clave, tresillo and syncopation.") {
        lesson("clave", "The Clave", "The key to Afro-Cuban music.") {
            read("Clave", """
                The **clave** ("key") is a two-bar rhythmic pattern that organises Afro-Cuban music. **Son clave 3-2**: three hits in bar 1, two in bar 2.

                Bar 1: beats 1, the "&" of 2, and 4. Bar 2: beats 2 and 3.
            """, play = "C6:q. C6:q. C6:q R:q C6:q C6:q R:q", bpm = 100)
            rhythm("Tap son clave (3-2).", "q. q. q Rq q q Rq", bpm = 90)
            read("Tresillo", "The first half of the clave - **3 + 3 + 2** eighths - is the **tresillo**. It's everywhere: habanera, tango, reggaeton, and pop hits like 'Shape of You'.",
                play = "C6:q. C6:q. C6:q", bpm = 100)
            rhythm("Tap the tresillo: 3 + 3 + 2.", "q. q. q", bpm = 90)
        }
        lesson("montuno", "Piano Montuno", "Salsa's repeating piano pattern.") {
            read("Montuno / guajeo", """
                In salsa, the piano plays a syncopated, repeating **montuno** in **octaves**, outlining a short progression like **I - IV - V - IV** and locking with the clave.
            """, play = "C4+C5:e E4+E5 G4+G5 C4+C5 R:e A4+A5 F4+F5 C4+C5 B3+B4 D4+D5 G4+G5 B3+B4 R:e A4+A5 F4+F5 C4+C5", bpm = 120)
        }
        test {
            quiz("Son clave 3-2 has how many hits in the first bar?", "3", "2", "4", "5")
            quiz("The tresillo groups eighths as…", "3 + 3 + 2", "2 + 2 + 2 + 2", "4 + 4", "3 + 2 + 3")
        }
    }

    unit("brazil", "Brazil & Argentina", "Bossa nova, samba and tango.") {
        lesson("bossa", "Bossa Nova", "Cool Brazilian jazz.") {
            read("Bossa nova", """
                Born in Rio around 1958 (João Gilberto, Antônio Carlos Jobim). It blends samba rhythm with cool jazz harmony.
                - Bass: root on 1, fifth on the "&" of 2 (a gentle dotted feel)
                - Chords: syncopated, soft, lots of **maj7, m7, 9ths**
            """, play = "C3:q. G2:e G2:h D3:q. A2:e A2:h", bpm = 120)
            song("bossa-breeze", "Play Bossa Breeze.")
        }
        lesson("tango", "Tango & Habanera", "Passion and drama.") {
            read("Tango", """
                Argentine tango has a strong, marcato beat, dramatic minor harmony and the **habanera** rhythm (dotted eighth - sixteenth - eighth - eighth). The **Andalusian cadence** (i - ♭VII - ♭VI - V) is a favourite.
            """, play = "D3:e. D3:s A3:e D4:e D3:e. D3:s A3:e D4:e", bpm = 90, visual = progression("i bVII bVI V", "Dm"))
            song("tango-nights", "Play Tango Nights.")
        }
        test {
            quiz("Bossa nova comes from…", "Brazil", "Cuba", "Argentina", "Spain")
            quiz("The habanera rhythm begins with a…", "Dotted eighth + sixteenth", "Whole note", "Triplet", "Rest")
        }
    }
}

