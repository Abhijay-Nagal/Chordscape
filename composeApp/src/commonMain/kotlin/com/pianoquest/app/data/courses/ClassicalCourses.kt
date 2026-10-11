package com.pianoquest.app.data.courses

import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.ComposerCard
import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.composers
import com.pianoquest.app.data.image
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Visual
import com.pianoquest.app.data.Illustration
import com.pianoquest.app.data.picture
import com.pianoquest.app.data.chords
import com.pianoquest.app.data.course
import com.pianoquest.app.data.progression
import com.pianoquest.app.data.staff
import com.pianoquest.app.data.symbols
import com.pianoquest.app.data.table

internal fun classicalCourse() = course(
    id = "classical", title = "Classical Piano", subtitle = "Baroque & Classical eras: Bach, Mozart, Beethoven",
    description = """
        A structured path through the classical tradition: technique foundations, the Baroque style of Bach,
        the elegance of Mozart and the drama of Beethoven. Learn the history, the performance practice, and
        play real pieces: Minuet in G, Prelude in C, Für Elise, the Sonata facile and more.
    """,
    level = "Beginner → Advanced", color = 0xFF3A4A5E, icon = CourseIcon.CLASSICAL, recommendedAfter = "foundations",
) {
    unit("technique", "Classical Technique", "Touch, scales, arpeggios and how to practise.") {
        lesson("touch", "Tone & Touch", "Using arm weight, not finger force.") {
            read("Where tone comes from", """
                A beautiful piano sound comes from **arm weight** transferred through relaxed fingers - not from pushing hard with the fingers.

                - Drop into the key from a small height with a loose wrist; the finger stays firm at the tip.
                - **Speed of the key** = volume. A faster key descent makes a louder note.
                - Listen to the *end* of each note as much as the beginning.
            """, visual = picture(Illustration.ARM_WEIGHT))
            read("Finger independence", """
                Classical music demands even, independent fingers. Daily exercises (Hanon, Czerny, Schmitt) help - but always **musically**: shape them, vary dynamics and rhythms.
            """, visual = Visual.Hands())
            song("ex-five-finger-c", "Warm up with the five-finger pattern, listening for perfectly even notes.")
            song("ex-hanon-1", "Hanon No. 1: slow, even, relaxed. Increase speed only when it's perfectly even.")
        }
        lesson("scales", "Scale Fingering", "Thumb-under technique and the standard fingerings.") {
            read("Thumb under", """
                Scales longer than five notes need the **thumb to pass under** the hand (going up in the right hand) or **fingers to cross over** the thumb (coming down).

                **C major, right hand**: 1 2 3 - (thumb under) - 1 2 3 4 5
                **C major, left hand**: 5 4 3 2 1 - (3 crosses over) - 3 2 1
            """, visual = Visual.Keyboard("C4", "C5", listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5"),
                fingers = mapOf("C4" to 1, "D4" to 2, "E4" to 3, "F4" to 1, "G4" to 2, "A4" to 3, "B4" to 4, "C5" to 5)))
            read("Fingering groups", "Most major scales use one of these patterns:", visual = table(listOf("Scales", "Right hand", "Left hand"),
                listOf("C G D A E (white-key start)", "123 1234 5", "54321 321"),
                listOf("F major", "1234 123 4", "54321 321"),
                listOf("B major", "123 1234 5", "4321 4321"),
                listOf("Black-key scales (D♭ G♭ B♭ E♭ A♭)", "Thumb on white keys only, 4 on B♭", "Thumb on white keys only")))
            play("C major scale, right hand, with thumb-under fingering.", "C4:q D4 E4 F4 G4 A4 B4 C5 B4 A4 G4 F4 E4 D4 C4:h",
                from = "C4", to = "C5", fingers = listOf(1, 2, 3, 1, 2, 3, 4, 5, 4, 3, 2, 1, 3, 2, 1))
            play("G major scale, right hand (F♯!).", "G4:q A4 B4 C5 D5 E5 F#5 G5", from = "G4", to = "G5", fingers = listOf(1, 2, 3, 1, 2, 3, 4, 5))
            song("ex-c-scale", "Two octaves, both hands, in the song player. Start at 60%.")
            drill(DrillKind.PLAY_SCALE, count = 2, level = 1)
        }
        lesson("arpeggios", "Arpeggios & Broken Chords", "Chords one note at a time.") {
            read("Arpeggios", """
                An **arpeggio** plays a chord's notes one after another across octaves. C major RH: **1 2 3 1 2 3 5** (C E G C E G C).

                Keep the wrist level and let the arm guide the hand sideways; the thumb passes under smoothly without twisting the wrist.
            """, visual = Visual.Keyboard(from = "C4", to = "C6", highlights = listOf("C4", "E4", "G4", "C5", "E5", "G5", "C6"),
                fingers = mapOf("C4" to 1, "E4" to 2, "G4" to 3, "C5" to 1, "E5" to 2, "G5" to 3, "C6" to 5),
                caption = "C major arpeggio, right hand: the thumb passes under to the next C"), play = "C4:e E4 G4 C5 E5 G5 C6 G5 E5 C5 G4 E4 C4:h", bpm = 100)
            play("C major arpeggio, one octave and back.", "C4:q E4 G4 C5 G4 E4 C4", from = "C4", to = "C5", fingers = listOf(1, 2, 3, 5, 3, 2, 1))
            song("ex-arpeggio-c", "Two-octave arpeggios with both hands.")
        }
        lesson("practice", "How to Practise", "The methods professional pianists use.") {
            read("Deliberate practice", """
                1. **Slow is fast.** Practise at a tempo where you make *zero* mistakes. Mistakes you repeat become habits.
                2. **Hands separately**, then together.
                3. **Chunking**: work in small sections (1-2 bars), then link them. Start practising from the *end* sometimes.
                4. **Fixed fingering**: decide fingering early and keep it.
                5. **Metronome ladder**: increase 4 bpm at a time once a passage is perfect three times in a row.
                6. **Rhythmic variants**: practise even runs as long-short and short-long to strengthen control.
                7. **Listen and record** yourself.
            """, mood = MascotMood.TEACHING)
            quiz("When should you speed up a passage?", "When it's perfect several times in a row at the current tempo", "As soon as you can play it once", "Every day automatically", "Never")
            quiz("'Chunking' means…", "Practising small sections and linking them", "Playing very loudly", "Only playing hands together", "Skipping hard parts")
        }
        test {
            quiz("In the C major RH scale, the thumb passes under after which finger?", "3", "4", "2", "5")
            quiz("Volume on the piano is controlled mainly by…", "The speed of the key's descent", "How long you hold the key", "Pressing harder after the note sounds", "The pedal")
            drill(DrillKind.PLAY_SCALE, count = 2, level = 1)
            quiz("C major arpeggio RH fingering (one octave + top C)?", "1 2 3 1 2 3 5", "1 2 3 4 5", "1 3 5 1 3 5", "5 3 1")
        }
    }

    unit("baroque", "The Baroque Era (1600-1750)", "Bach, Handel, Scarlatti - counterpoint and dance.") {
        lesson("era", "The Baroque World", "Harpsichords, churches and courts.") {
            read("The Baroque era", """
                **1600-1750.** Ornate art, grand palaces and the birth of opera. Keyboard music was played on the **harpsichord**, **clavichord** and **organ** - the piano was only invented around 1700 by Bartolomeo Cristofori.

                Key composers: **J.S. Bach**, **Handel**, **Domenico Scarlatti**, **Vivaldi**, **Couperin**, **Rameau**, **Purcell**.

                Characteristics:
                - **Counterpoint**: several independent melodic lines (fugues, inventions).
                - **Continuous motion** and a steady pulse ("motor rhythm").
                - **Terraced dynamics**: sudden changes between loud and soft rather than gradual crescendos (harpsichords couldn't do gradual!).
                - **Ornaments**: trills, mordents, turns.
                - **Basso continuo / figured bass**: a bass line with numbers showing the harmony to improvise.
                - Dance forms: minuet, gavotte, bourrée, sarabande, gigue - often in **binary form**.
            """, visual = image("harpsichord", "A Baroque harpsichord by Jan Couchet, Antwerp (Metropolitan Museum of Art)"), play = "D5:q G4:e A4 B4 C5 D5:q G4 G4", bpm = 112)
            read("Meet the Baroque masters", "Tap a card to hear one of their pieces in full.", visual = composers(
                ComposerCard("bach", "Johann Sebastian Bach", "1685-1750", "Well-Tempered Clavier, Inventions, Goldberg Variations", "prelude-c"),
                ComposerCard("handel", "George Frideric Handel", "1685-1759", "Messiah, harpsichord suites", null),
            ))
            read("The harpsichord", """
                The harpsichord **plucks** its strings with small quills when you press a key. However hard you press, the volume barely changes - so composers created contrast by switching between two keyboards (manuals) or adding more strings.

                That's why Baroque music uses **terraced dynamics** and so many ornaments: a trill keeps a note alive on an instrument that can't sustain.
            """, visual = image("harpsichord", "Double-manual harpsichord by Joannes Couchet, Antwerp, c. 1650 (Metropolitan Museum of Art)"))
            read("The clavichord", """
                The clavichord's strings are struck by small brass blades (tangents). It's very quiet, but it can play soft and loud and even add vibrato - Bach's favourite instrument for practising at home.
            """, visual = image("clavichord", "Clavichord (Metropolitan Museum of Art)"))
            quiz("Which instrument did Bach mostly write keyboard music for?", "Harpsichord, clavichord & organ", "Modern grand piano", "Synthesizer", "Celesta")
            quiz("Terraced dynamics means…", "Sudden shifts between loud and soft", "Gradual crescendos", "Always loud", "Using the pedal")
            quiz("Baroque dances are usually in…", "Binary form", "Sonata form", "12-bar blues", "Verse-chorus")
        }
        lesson("articulation", "Baroque Articulation & Ornaments", "Playing Bach stylishly on a modern piano.") {
            read("Stylish Bach", """
                Modern pianists usually follow these guidelines (there's healthy debate!):
                - **Quarter notes slightly detached**, **eighths and sixteenths legato** in steady passagework.
                - **Little or no sustain pedal** - clarity of lines comes first.
                - Shape lines with subtle dynamics; bring out the **theme** wherever it appears.
                - Trills in Baroque music usually **start on the upper note** and on the beat.
            """, visual = symbols("tr" to "Trill - start on the note above", "~" to "Mordent - main, upper, main", "(  )" to "Ornaments are decorations - the melody must stay clear"))
            read("Hear a Baroque trill ending", "", play = "D5:s C5 D5 C5 D5 C5 B4 C5 B4:h", bpm = 70)
            quiz("Baroque trills usually start on…", "The upper note", "The lower note", "The main note, after the beat", "An octave above")
            quiz("In Bach on a modern piano, the sustain pedal is usually…", "Used very sparingly", "Held throughout", "Used on every beat", "Forbidden by law")
        }
        lesson("minuet", "Study: Minuet in G", "The most famous Baroque teaching piece.") {
            read("About the piece", """
                From the **Notebook for Anna Magdalena Bach** (1725) - long credited to J.S. Bach, now attributed to **Christian Petzold**. It's in **G major**, **3/4** and **binary form**.

                The right hand dances in eighths; the left hand provides a second melodic line - real two-part counterpoint.
            """, visual = staff("D5:q G4:e A4 B4 C5 | D5:q G4 G4", keySig = 1, time = "3/4"), play = "D5:q G4:e A4 B4 C5 D5:q G4 G4", bpm = 112)
            play("Learn the opening: right hand bars 1-2.", "D5:q G4:e A4 B4 C5 D5:q G4 G4", from = "G4", to = "D5", staff = Clef.TREBLE)
            play("Bars 3-4.", "E5:q C5:e D5 E5 F#5 G5:q G4 G4", from = "G4", to = "G5", staff = Clef.TREBLE)
            quiz("This minuet is in which time signature?", "3/4", "4/4", "2/4", "6/8")
            song("minuet-g", "Play the complete Minuet in the song player. Try Easy first, then Hard - the full score.")
        }
        lesson("prelude", "Study: Prelude in C (BWV 846)", "One pattern, a world of harmony.") {
            read("The Well-Tempered Clavier", """
                Bach wrote a prelude and fugue in **every major and minor key** - 24 of them, twice (1722 and 1742). It showcased **well-tempered tuning**, which made all keys usable.

                The C major Prelude is a sequence of broken chords. Each bar = one harmony. The art lies in the **voice leading**: notes move by step from chord to chord.
            """, visual = chords("C", "Dm7/C", "G7/B", "C", "Am/C", "D7/C", "G/B", "Cmaj7/B"))
            read("The pattern", "Each half-bar: two low notes then three upper notes repeated.", visual = staff("C4:s E4 G4 C5 E5 G4 C5 E5", names = true),
                play = "C4:s E4 G4 C5 E5 G4 C5 E5 C4 E4 G4 C5 E5 G4 C5 E5", bpm = 66)
            quiz("How many keys does the Well-Tempered Clavier cover?", "All 24 major and minor keys", "Only C major", "12 major keys", "7 modes")
            song("prelude-c", "Play the complete Prelude. Name each bar's chord as you go!")
        }
        lesson("canon", "Ground Bass: Pachelbel's Canon", "A Baroque loop progression.") {
            read("Variations over a ground", """
                Pachelbel's Canon repeats an 8-chord bass pattern - a **ground bass** - while melodies above become more elaborate. Three violins play the same tune in **canon** (like a round), each entering two bars apart.

                **D - A - Bm - F♯m - G - D - G - A** = I - V - vi - iii - IV - I - IV - V
            """, visual = progression("I V vi iii IV I IV V", "D"))
            song("canon-d", "Play the complete Canon - all three upper voices in the right hand over the ground bass.")
        }
        test {
            quiz("The Baroque era spans roughly…", "1600-1750", "1750-1820", "1820-1900", "1900-2000")
            quiz("A ground bass is…", "A repeating bass pattern", "A low drum", "The lowest piano key", "A type of pedal")
            quiz("Who wrote the Well-Tempered Clavier?", "J.S. Bach", "Mozart", "Chopin", "Handel")
            quiz("Two independent melodic lines at once is…", "Counterpoint", "Homophony", "Alberti bass", "Rubato")
            play("Play the opening of the Minuet in G.", "D5:q G4:e A4 B4 C5 D5:q G4 G4", from = "G4", to = "D5", staff = Clef.TREBLE, hints = false)
        }
    }

    unit("classical-era", "The Classical Era (1750-1820)", "Haydn, Mozart, Beethoven - balance, clarity, drama.") {
        lesson("era", "Elegance & Balance", "The age of the sonata.") {
            read("The Classical style", """
                **1750-1820.** The early **fortepiano** replaced the harpsichord, allowing **gradual dynamics** (crescendo, diminuendo).

                Composers: **Haydn**, **Mozart**, **Beethoven** (who bridges into Romanticism), **Clementi**, **Kuhlau**, **Diabelli**.

                Characteristics:
                - **Homophony**: clear melody with accompaniment.
                - **Balanced phrases** (4 + 4 bars, question and answer).
                - **Alberti bass** and other broken-chord accompaniments.
                - Clear **cadences** and simple, functional harmony.
                - **Sonata form** and the multi-movement **sonata** (fast - slow - fast).
            """, visual = image("fortepiano", "A Viennese fortepiano by Anton Walter - the instrument Mozart knew"))
            read("The birth of the piano", """
                Around **1700**, Bartolomeo Cristofori in Florence built a harpsichord-shaped instrument whose strings were struck by **hammers**. He called it a *gravicembalo col piano e forte* - a "harpsichord with soft and loud". The name stuck: **pianoforte**, then simply **piano**.
            """, visual = image("cristofori_piano", "Piano by Bartolomeo Cristofori, Florence, 1720 - the oldest surviving piano (Metropolitan Museum of Art)"))
            read("The fortepiano", """
                By Mozart's time, Viennese builders such as Anton Walter made light, fast, bright-sounding **fortepianos** with a narrower range (about 5 octaves) and knee levers instead of pedals.

                Mozart owned a Walter piano. Its quick, clear tone explains why his music needs a light, crisp touch.
            """, visual = image("fortepiano", "Fortepiano by Anton Walter, Vienna (Musical Instrument Museum)"))
            read("The great Classical composers", "Tap a card to hear one of their pieces in full.", visual = composers(
                ComposerCard("haydn", "Joseph Haydn", "1732-1809", "'Father of the symphony', 60+ piano sonatas", null),
                ComposerCard("mozart", "Wolfgang Amadeus Mozart", "1756-1791", "Child prodigy; over 600 works", "turkish-march"),
                ComposerCard("beethoven", "Ludwig van Beethoven", "1770-1827", "32 piano sonatas, 9 symphonies", "fur-elise"),
            ))
            quiz("What new possibility did the fortepiano offer?", "Gradual dynamics (crescendo/diminuendo)", "Sustained organ tone", "Electric sound", "Quarter tones")
            quiz("Which composer bridges the Classical and Romantic eras?", "Beethoven", "Bach", "Chopin", "Debussy")
        }
        lesson("alberti", "The Alberti Bass", "The Classical accompaniment pattern.") {
            read("Low - high - middle - high", """
                Named after Domenico Alberti, it breaks a chord into the pattern **bottom - top - middle - top**:
                C major → **C G E G**. It keeps harmony moving softly under a singing melody.
            """, visual = staff("C3:e G3 E3 G3 C3 G3 E3 G3", clef = Clef.BASS, names = true), play = "C3:e G3 E3 G3 C3 G3 E3 G3 B2 G3 D3 G3 C3:h", bpm = 110)
            play("Play an Alberti bass on C (left hand, fingers 5-1-3-1).", "C3:e G3 E3 G3 C3 G3 E3 G3", from = "C3", to = "B3", fingers = listOf(5, 1, 3, 1, 5, 1, 3, 1))
            play("Now on G7 (B-F-D-F) and back to C.", "B2:e F3 D3 F3 B2 F3 D3 F3 C3 G3 E3 G3 C3:h", from = "A2", to = "B3")
            read("The key to Alberti", "Keep it **quiet** and **even** - the left hand whispers so the right hand sings. Rotate the wrist slightly rather than lifting fingers.", mood = MascotMood.THINKING)
            song("sonata-facile", "Mozart's Sonata facile uses an Alberti bass throughout.")
        }
        lesson("sonata-form", "Sonatinas & Sonata Form", "The architecture of Classical music.") {
            read("Sonata vs sonata form", """
                - A **sonata** is a multi-movement work (usually 3: fast - slow - fast).
                - **Sonata form** is the structure typically used for the *first movement*:
                  **Exposition** (Theme 1 in tonic, Theme 2 in dominant) → **Development** → **Recapitulation** (both themes in tonic).
                - A **sonatina** is a small, simpler sonata - perfect for intermediate players (Clementi Op. 36, Kuhlau, Diabelli).
            """, visual = table(listOf("Section", "Key (major piece)", "What happens"),
                listOf("Exposition - Theme 1", "Tonic (I)", "Main idea"), listOf("Transition", "Moving", "Modulates"),
                listOf("Exposition - Theme 2", "Dominant (V)", "Contrasting idea"), listOf("Development", "Many keys", "Themes broken up, tension"),
                listOf("Recapitulation", "Tonic (I)", "Both themes return home"), listOf("Coda", "Tonic", "Ending")))
            quiz("In a major-key sonata exposition, Theme 2 is usually in the…", "Dominant key", "Tonic key", "Relative minor", "Subdominant key")
            quiz("A sonatina is…", "A small, simpler sonata", "A song", "A dance", "An étude")
        }
        lesson("mozart", "Mozart's Style", "Clarity, song and opera on the keyboard.") {
            read("Wolfgang Amadeus Mozart (1756-1791)", """
                A child prodigy who wrote over 600 works. His piano music sings like opera: clear melodies, graceful ornaments, crisp articulation.

                Playing Mozart:
                - **Light, even** fingerwork; scales like strings of pearls.
                - **Slurs** usually mean a slight emphasis on the first note and lightness on the last.
                - Minimal pedal, with clear textures.
            """, visual = image("mozart", "Wolfgang Amadeus Mozart"), play = "C5:h E5:q G5:q B4:q. C5:s D5:s C5:h", bpm = 112)
            song("sonata-facile", "Sonata in C, K. 545: keep the Alberti bass soft.")
            song("turkish-march", "Rondo alla Turca: light, crisp sixteenth-note turns.")
        }
        lesson("beethoven", "Beethoven's Drama", "Storms, contrasts and the human spirit.") {
            read("Ludwig van Beethoven (1770-1827)", """
                Beethoven expanded everything: dynamics from **pp to ff**, sudden **sforzandos**, long developments, and personal emotion. He kept composing after becoming completely **deaf**.

                His 32 piano sonatas are called the "New Testament" of piano literature (Bach's WTC being the "Old Testament").
            """, visual = image("beethoven", "Beethoven, by Joseph Karl Stieler (1820)"))
            read("Ludwig van Beethoven", "", visual = composers(
                ComposerCard("beethoven", "Ludwig van Beethoven", "1770-1827", "Portrait by Joseph Karl Stieler, 1820, painted while Beethoven was composing the Missa Solemnis", "moonlight"),
            ))
            read("Für Elise", """
                A bagatelle (short, light piece) in **A minor**, **rondo form** (A B A C A). The famous opening alternates E and D♯ - a chromatic **neighbour-tone** motif - over broken chords passed between the hands.
            """, visual = staff("E5:s D#5 E5 D#5 E5 B4 D5 C5 A4:e", names = true), play = "E5:s D#5 E5 D#5 E5 B4 D5 C5 A4:e", bpm = 66)
            play("Play the Für Elise motif (right hand).", "E5:s D#5 E5 D#5 E5 B4 D5 C5 A4:q", from = "A4", to = "E5")
            song("fur-elise", "Learn the complete Für Elise - start on Easy, then Medium, then the full score on Hard.")
            read("Moonlight Sonata", """
                Op. 27 No. 2 in **C♯ minor** - Beethoven called it "quasi una fantasia". The famous 1st movement: slow **triplets**, deep bass octaves, a sad melody, and the instruction to use the pedal throughout.
            """, visual = image("beethoven_hornemann", "Beethoven in 1803, around the time of the Moonlight Sonata (Christian Horneman)"), play = "G#3:t C#4 E4 G#3 C#4 E4 G#3 C#4 E4 G#3 C#4 E4", bpm = 54)
            song("moonlight", "Play the complete first movement of the Moonlight Sonata.")
        }
        test {
            quiz("The Alberti bass pattern is…", "Low - high - middle - high", "Low - middle - high - middle", "All notes together", "Octaves")
            quiz("Für Elise is in which key?", "A minor", "C major", "E minor", "C♯ minor")
            quiz("The Moonlight Sonata's first movement is built on…", "Triplets", "Alberti bass", "Boogie-woogie", "Waltz rhythm")
            quiz("Sonata form's three main sections are…", "Exposition, Development, Recapitulation", "Verse, Chorus, Bridge", "A, B, A", "Prelude, Fugue, Coda")
            play("Play the Alberti bass on C.", "C3:e G3 E3 G3 C3 G3 E3 G3", from = "C3", to = "B3", hints = false)
        }
    }

    unit("advanced", "Toward Advanced Repertoire", "Interpretation, memorisation and your repertoire roadmap.") {
        lesson("interpretation", "Interpretation", "Making the notes into music.") {
            read("Beyond the notes", """
                - **Phrasing**: every phrase has a shape - usually growing toward a high point, then relaxing.
                - **Voicing**: in chords, bring out the melody note (usually the top) by giving that finger more weight.
                - **Rhythm**: steady but alive; Classical music avoids heavy rubato.
                - **Character**: decide the mood of each section before you play.
                - **Listen** to several recordings by great pianists (Schiff, Uchida, Brendel, Gould, Argerich) and compare.
            """)
            quiz("Voicing a chord means…", "Making one note (often the melody) stand out", "Singing along", "Playing all notes equally", "Using the soft pedal")
        }
        lesson("memorisation", "Memorising Music", "Four kinds of memory.") {
            read("Memory is layered", """
                1. **Muscle (kinaesthetic) memory** - fastest to form, but fragile under nerves.
                2. **Aural memory** - knowing how it sounds; you can sing it.
                3. **Visual memory** - seeing the page or the hand shapes.
                4. **Analytical memory** - knowing the harmony, form and structure. The strongest safety net!

                Memorise in sections, from different starting points, and practise away from the piano (mentally).
            """)
            quiz("The most reliable memory under performance pressure is…", "Analytical (knowing the structure)", "Muscle memory alone", "Visual only", "None")
        }
        lesson("roadmap", "Your Repertoire Roadmap", "What to learn next, grade by grade.") {
            read("A suggested path", "", visual = table(listOf("Level", "Repertoire ideas"),
                listOf("Early", "Anna Magdalena Notebook, Türk, Gurlitt, Bartók Mikrokosmos I-II"),
                listOf("Elementary", "Clementi Sonatinas Op. 36, Burgmüller Op. 100, Schumann Album for the Young"),
                listOf("Intermediate", "Bach Inventions, Mozart K.545, Beethoven Für Elise & Sonatinas, Kuhlau"),
                listOf("Late intermediate", "Bach Sinfonias, Mozart K.331, Beethoven 'Pathétique' 2nd mvt, Chopin Preludes"),
                listOf("Advanced", "Bach Preludes & Fugues, Beethoven sonatas, Chopin Nocturnes & Études, Debussy"),
                listOf("Virtuoso", "Liszt, Rachmaninoff, Chopin Ballades, Beethoven late sonatas, Ravel")))
            read("Keep going!", "You're on your way to playing the masterpieces of 300 years of keyboard music. Pick one piece above your level to dream about, and one at your level to master. 🎹",
                mood = MascotMood.EXCITED)
        }
        test {
            quiz("Analytical memory means…", "Understanding harmony and structure", "Remembering finger motions", "Seeing the score", "Hearing the music")
            quiz("Bach's Inventions are a good step after…", "Anna Magdalena Notebook pieces", "Liszt études", "Rachmaninoff concertos", "Nothing - start there")
            quiz("Bringing out a melody note in a chord is called…", "Voicing", "Phrasing", "Pedalling", "Rubato")
        }
    }
}

internal fun romanticCourse() = course(
    id = "romantic", title = "Romantic Period", subtitle = "Chopin, Schumann, Liszt, Brahms - music of feeling",
    description = """
        The 19th century turned the piano into the voice of the soul. Learn the expressive tools of Romantic piano -
        rubato, singing tone, pedalling, wide arpeggios and rich chromatic harmony - and the character pieces of
        Chopin, Schumann, Liszt, Brahms, Grieg and Tchaikovsky, through to Satie and Debussy.
    """,
    level = "Intermediate → Advanced", color = 0xFF7A2E2A, icon = CourseIcon.ROMANTIC, recommendedAfter = "classical",
) {
    unit("era", "The Romantic Era", "1820-1900: the piano becomes a superstar.") {
        lesson("context", "Music of Feeling", "Why Romantic music sounds the way it does.") {
            read("The Romantic era (≈1820-1900)", """
                Romantic artists prized **emotion, nature, the individual and the supernatural**. The piano grew to its modern form - iron frame, 88 keys, powerful sound - and became the centre of middle-class homes.

                Key composers: **Chopin**, **Schumann**, **Clara Schumann**, **Liszt**, **Mendelssohn**, **Schubert**, **Brahms**, **Grieg**, **Tchaikovsky**, **Rachmaninoff** (late).

                Characteristics:
                - **Lyrical, singing melodies**
                - **Rich chromatic harmony**, extended chords, distant modulations
                - **Rubato** and flexible tempo
                - **Wide dynamic range** and full use of the pedal
                - **Character pieces** (short, evocative works) and **virtuoso** showpieces
                - **Programme music** - music that tells a story or paints a picture
                - **Nationalism** - folk music of Poland, Norway, Hungary, Russia
            """, visual = image("liszt_piano", "Liszt at the piano, by Josef Danhauser (1840)"), mood = MascotMood.TEACHING)
            quiz("Which is NOT a Romantic composer?", "J.S. Bach", "Chopin", "Liszt", "Brahms")
            quiz("Music that tells a story or paints a scene is called…", "Programme music", "Absolute music", "Counterpoint", "Figured bass")
            quiz("Chopin's mazurkas and polonaises reflect which nation's music?", "Poland", "Norway", "Hungary", "Russia")
        }
        lesson("composers", "The Great Romantic Pianists", "Who's who.") {
            read("Meet the composers", "", visual = table(listOf("Composer", "Known for"),
                listOf("Frédéric Chopin (1810-49)", "Nocturnes, Études, Ballades, Preludes, Waltzes - the 'poet of the piano'"),
                listOf("Robert Schumann (1810-56)", "Kinderszenen (incl. Träumerei), Carnaval - imaginative character pieces"),
                listOf("Clara Schumann (1819-96)", "Virtuoso pianist and composer; premiered many works by Brahms"),
                listOf("Franz Liszt (1811-86)", "Hungarian Rhapsodies, Liebesträume - the first rock-star virtuoso"),
                listOf("Felix Mendelssohn (1809-47)", "Songs Without Words"),
                listOf("Johannes Brahms (1833-97)", "Intermezzi, Hungarian Dances, the famous Lullaby"),
                listOf("Edvard Grieg (1843-1907)", "Lyric Pieces, Peer Gynt - Norwegian nationalism"),
                listOf("Pyotr Tchaikovsky (1840-93)", "The Seasons, The Nutcracker, Swan Lake"),
                listOf("Sergei Rachmaninoff (1873-1943)", "Preludes, concertos - late Romantic grandeur")))
            read("Portraits of the Romantics", "Tap a card to hear a complete piece by each composer.", visual = composers(
                ComposerCard("chopin", "Frédéric Chopin", "1810-1849", "Photograph, 1849", "nocturne-9-2"),
                ComposerCard("schumann", "Robert Schumann", "1810-1856", "Kinderszenen, Carnaval", "traumerei"),
                ComposerCard("clara_schumann", "Clara Schumann", "1819-1896", "Leading concert pianist of her age", null),
                ComposerCard("liszt", "Franz Liszt", "1811-1886", "The first piano superstar", "consolation-3"),
                ComposerCard("brahms", "Johannes Brahms", "1833-1897", "Intermezzi, waltzes, the Lullaby", "brahms-intermezzo"),
                ComposerCard("grieg", "Edvard Grieg", "1843-1907", "Peer Gynt, Lyric Pieces", "mountain-king"),
                ComposerCard("tchaikovsky", "Pyotr Tchaikovsky", "1840-1893", "The Seasons, Album for the Young", "january"),
                ComposerCard("dvorak", "Antonín Dvořák", "1841-1904", "New World Symphony", null),
                ComposerCard("rachmaninoff", "Sergei Rachmaninoff", "1873-1943", "The last great Romantic", "rach-prelude"),
            ))
            read("The modern grand piano", """
                The Romantic piano grew bigger and louder: a cast-iron frame to hold enormous string tension, 88 keys, felt hammers and a repetition action so notes could be repeated very fast. Steinway's 1850s grands like this one are close to the instruments we play today.
            """, visual = image("grand_piano", "Steinway & Sons grand piano, New York, 1857 (Metropolitan Museum of Art)"))
            song("brahms-lullaby", "Play Brahms's Lullaby - a gentle Romantic waltz.")
        }
        test {
            quiz("Which era is roughly 1820-1900?", "Romantic", "Baroque", "Classical", "Modern")
            quiz("'Songs Without Words' were written by…", "Mendelssohn", "Bach", "Liszt", "Grieg")
            quiz("Which composer is called the 'poet of the piano'?", "Chopin", "Brahms", "Beethoven", "Satie")
        }
    }

    unit("expression", "Expressive Tools", "Rubato, singing tone, pedalling and voicing.") {
        lesson("rubato", "Rubato", "Stolen time.") {
            read("Tempo rubato = 'stolen time'", """
                Rubato means flexing the tempo for expression: lingering on an important note, pressing forward toward a climax, then **giving back** the time.

                Chopin's advice (as reported by his students): the **left hand keeps time like a conductor** while the **right hand sings freely** above it.

                Good rubato sounds natural, like speech. Too much sounds seasick!
            """, play = "G5:q. F5:e G5:e F5:e Eb5:q. Bb4:q.", bpm = 60)
            quiz("Rubato means…", "Flexible, 'stolen' time", "Very loud", "Detached notes", "Repeated chords")
            quiz("In Chopin's rubato, which hand traditionally keeps steadier time?", "Left hand", "Right hand", "Neither", "Both equally rigid")
        }
        lesson("singing", "Singing Tone & Voicing", "Making the piano sing.") {
            read("Cantabile", """
                The piano is a percussion instrument - its notes only fade. To make a melody **sing** (cantabile):
                - Play melody notes with arm weight and a slightly flatter finger.
                - **Listen** to the decay of each note and match the next note to it.
                - **Voice** the melody above the accompaniment: melody **mf**, accompaniment **p**.
                - Shape phrases toward their peak.
            """)
            play("Voicing practice: play the top note louder than the others.", "C4+E4+G4+C5:h F4+A4+C5+F5:h E4+G4+C5+G5:h E4+G4+C5:w", from = "C4", to = "G5")
            quiz("To voice a melody, you…", "Play it louder than the accompaniment", "Play everything equally", "Use the soft pedal", "Play staccato")
        }
        lesson("pedalling", "Romantic Pedalling", "The soul of the piano.") {
            read("The sustain pedal is essential", """
                Anton Rubinstein called the pedal "the soul of the piano". Romantic textures - wide arpeggios, rich chords - need it.

                **Legato (syncopated) pedalling**: play the new chord, *then* quickly lift and re-press the pedal. This connects chords without blurring them.

                Change pedal with **every change of harmony**. Use **half-pedals** and **flutter pedal** for subtle colours, and listen - your ears are the judge.
            """, symbols("Ped." to "Press the sustain pedal", "✱" to "Release the pedal", "└──┘" to "Bracket-style pedal marking"))
            quiz("When should you change the pedal?", "With each change of harmony", "Every bar regardless", "Never", "Only at the end")
        }
        lesson("arpeggios", "Wide Left-Hand Arpeggios", "The nocturne texture.") {
            read("Spanning more than an octave", """
                Romantic accompaniment often spreads a chord across 1½ octaves or more: **root - 5th - 10th - 5th** etc. Combined with the pedal, it creates a rippling harp-like wash under a melody.

                Technique: keep the wrist flexible and let the hand **rotate** and shift, rather than stretching rigidly.
            """, visual = staff("Eb2:e Bb2 G3 Bb3 G3 Bb2", clef = Clef.BASS, names = true), play = "Eb2:e Bb2 G3 Bb3 G3 Bb2 Eb2 Bb2 G3 Bb3 G3 Bb2", bpm = 80)
            play("Play the E♭ nocturne arpeggio (left hand).", "Eb2:e Bb2 G3 Bb3 G3 Bb2", from = "C2", to = "C4", bpm = 70)
            song("nocturne-study", "Put it together: the Nocturne Study in E♭.")
        }
        test {
            quiz("Cantabile means…", "In a singing style", "Very fast", "Short and detached", "In strict time")
            quiz("Legato pedalling changes the pedal…", "Just after the new chord sounds", "Before the new chord", "Never", "Only on beat 1")
            quiz("Who called the pedal 'the soul of the piano'?", "Anton Rubinstein", "J.S. Bach", "Mozart", "Satie")
        }
    }

    unit("character", "Character Pieces", "Nocturnes, waltzes, mazurkas, études and preludes.") {
        lesson("forms", "Romantic Piano Genres", "Short pieces, big emotions.") {
            read("Character pieces", "", visual = table(listOf("Genre", "Character", "Famous examples"),
                listOf("Nocturne", "Night piece - singing melody over arpeggios", "Chopin Op. 9 No. 2, Field"),
                listOf("Waltz", "Elegant dance in 3/4", "Chopin 'Minute' Waltz"),
                listOf("Mazurka", "Polish dance in 3/4 with accents on beat 2 or 3", "Chopin's 59 mazurkas"),
                listOf("Polonaise", "Proud Polish dance in 3/4", "Chopin 'Heroic' Polonaise"),
                listOf("Étude", "Study focused on one technical problem - but real music", "Chopin, Liszt Études"),
                listOf("Prelude", "Short, free piece", "Chopin 24 Preludes Op. 28"),
                listOf("Song Without Words", "Piano 'song'", "Mendelssohn"),
                listOf("Intermezzo", "Intimate interlude", "Brahms Op. 117, 118"),
                listOf("Rhapsody", "Free, epic, often folk-inspired", "Liszt Hungarian Rhapsodies"),
                listOf("Ballade", "Narrative, story-like", "Chopin's four Ballades")))
            quiz("A nocturne is…", "A night piece with a singing melody", "A fast study", "A march", "A fugue")
            quiz("An étude is…", "A study focused on a technical skill", "A dance", "A lullaby", "A slow movement")
        }
        lesson("waltz", "The Romantic Waltz", "Oom-pah-pah with elegance.") {
            read("Waltz accompaniment", """
                Bass note on beat 1, chord on beats 2 and 3. Make beat 1 slightly heavier and beats 2-3 **light** - the dancers float!

                Viennese waltzes often slightly **anticipate beat 2** - a lilting, flexible feel.
            """, visual = staff("C3:q E3+G3 E3+G3 G2 D3+F3 D3+F3", clef = Clef.BASS, time = "3/4", caption = "Oom - pah - pah: bass note, then two light chords"), play = "C3:q E4+G4 E4+G4 G2 D4+F4 D4+F4 C3 E4+G4 E4+G4 C3:h.", bpm = 132)
            play("Play a waltz left hand: C - chord - chord, G - chord - chord.", "C3:q E3+G3 E3+G3 G2 D3+F3 D3+F3 C3:h.", from = "G2", to = "G3")
            song("brahms-lullaby", "Brahms' Lullaby uses a gentle waltz accompaniment.")
        }
        lesson("nationalism", "Nationalism & Folk", "Grieg, Dvořák, Chopin, Liszt.") {
            read("Music of the homeland", """
                Romantic composers drew on **folk music** of their countries: Chopin (Polish mazurkas), Liszt (Hungarian), Grieg (Norwegian), Dvořák (Czech, plus American spirituals), Mussorgsky and Tchaikovsky (Russian).

                Folk influence shows up as **pentatonic** melodies, **modal** colours, drones and distinctive dance rhythms.
            """)
            song("ases-death", "Grieg's Åse's Death from Peer Gynt - a grieving chorale.")
            song("mountain-king", "In the Hall of the Mountain King - an ostinato that accelerates.")
            song("troldhaugen", "Wedding Day at Troldhaugen - Grieg's festive Norwegian celebration.")
        }
        test {
            quiz("A mazurka comes from…", "Poland", "Austria", "Norway", "Italy")
            quiz("Brahms's Intermezzi are…", "Intimate character pieces", "Fast études", "Operas", "Fugues")
            quiz("Grieg's Morning Mood melody is built on the…", "Pentatonic scale", "Chromatic scale", "Whole-tone scale", "Blues scale")
        }
    }

    unit("harmony", "Romantic Harmony", "Chromaticism, colour and distant keys.") {
        lesson("chromatic", "Chromatic Colour", "Beyond the diatonic.") {
            read("Richer harmony", """
                Romantic composers used:
                - **Secondary dominants** and **diminished 7ths** for drama
                - **Borrowed chords** (minor iv, ♭VI in major)
                - **Chromatic mediants** (C → A♭ → E)
                - **Augmented sixths** and the **Neapolitan**
                - **Enharmonic modulation** to distant keys
                - Long **dominant pedals** and delayed resolutions (Wagner's 'Tristan chord' famously never resolves for hours!)
            """, visual = progression("I bVI IV iv I", "C"))
            read("The diminished 7th - Romantic drama", "Symmetric (all minor 3rds) - it can resolve to 8 different chords.", visual = chords("Bdim7", "C", "Cm"),
                play = "B3+D4+F4+Ab4:h C4+Eb4+G4+C5:w")
            quiz("Which chord is perfectly symmetrical (all minor 3rds)?", "Diminished 7th", "Major 7th", "Dominant 7th", "Augmented triad")
            ear("Major or borrowed minor iv? (listen to the 2nd chord)", "C3+E4+G4:h F3+Ab3+C4:h C3+E4+G4:w", "Minor iv (borrowed)", "Major IV")
        }
        lesson("impressionism", "Into the 20th Century: Impressionism", "Debussy, Ravel, Satie.") {
            read("Impressionism (≈1890-1920)", """
                Like Monet's paintings, **Debussy** and **Ravel** painted moods with sound: water, moonlight, mist.
                - **Whole-tone** and **pentatonic** scales, **modes**
                - **Parallel chords** ("planing") - chords slide together without functional pull
                - **Extended chords** (9ths, 11ths) as pure colour
                - Blurred pedal, soft dynamics, floating rhythm

                **Satie** was simpler and quirkier - his Gymnopédies use gently alternating major-7th chords.
            """, visual = image("impression_sunrise", "Claude Monet: Impression, Sunrise (1872) - the painting that gave the style its name"), play = "C4:e D4 E4 F#4 G#4 A#4 C5 A#4 G#4 F#4 E4 D4 C4:h", bpm = 90)
            read("Planing", "Parallel chords sliding in the same shape:", play = "C4+E4+G4+B4:q D4+F#4+A4+C#5 E4+G#4+B4+D#5 D4+F#4+A4+C#5 C4+E4+G4+B4:w", bpm = 70)
            read("Debussy and Satie", "", visual = composers(
                ComposerCard("debussy", "Claude Debussy", "1862-1918", "Clair de Lune, Préludes", "clair-de-lune"),
                ComposerCard("satie", "Erik Satie", "1866-1925", "Gymnopédies, Gnossiennes", "gymnopedie"),
            ))
            song("gymnopedie", "Play Satie's complete Gymnopédie No. 1.")
            quiz("Planing means…", "Moving chords in parallel", "Using no pedal", "Playing a fugue", "Changing tempo")
        }
        test {
            quiz("Which scale is a hallmark of Debussy?", "Whole-tone", "Blues", "Harmonic minor", "Bebop")
            quiz("Satie's Gymnopédie No. 1 alternates two…", "Major-7th chords", "Power chords", "Diminished chords", "Clusters")
            quiz("A chord on ♭VI in a major key is a…", "Borrowed chord", "Secondary dominant", "Neapolitan", "Tonic substitute")
        }
    }
}

