package com.pianoquest.app.data.courses

import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.ComposerCard
import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.composers
import com.pianoquest.app.data.image
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Visual
import com.pianoquest.app.data.Illustration
import com.pianoquest.app.data.picture
import com.pianoquest.app.data.course
import com.pianoquest.app.data.kb
import com.pianoquest.app.data.staff
import com.pianoquest.app.data.symbols
import com.pianoquest.app.data.table

internal fun foundationsCourse() = course(
    id = "foundations", title = "Piano Foundations", subtitle = "Start here - from your very first key",
    description = """
        Everything a complete beginner needs: how the keyboard is organised, octaves, finger numbers, rhythm,
        reading the treble and bass clef, and your first two-handed pieces. No experience required.
    """,
    level = "Beginner", color = 0xFF6B3C21, icon = CourseIcon.FOUNDATIONS,
) {
    unit("keyboard", "Meet the Keyboard", "Posture, finger numbers, white & black keys, octaves and note names.") {
        lesson("welcome", "Welcome to the Piano", "How to sit, how to hold your hands, and your first sound.") {
            read("Welcome to the piano", """
                This course takes you from your very first note all the way to playing Bach, Chopin, jazz and your favourite songs.

                Every lesson mixes short explanations with things to **do**: tap keys, play melodies, listen and answer questions.
                You can use the on-screen keyboard, your computer keyboard, or a real **MIDI keyboard** (see Settings → MIDI).

                > Little and often beats long and rare. Ten focused minutes a day will take you further than two hours once a week.
            """, mood = MascotMood.WAVING)
            read("Your instrument", """
                The piano is a **percussion and string instrument**: each key throws a felt hammer at tightly stretched strings. It has 88 keys covering more than seven octaves - the widest range of any acoustic instrument.

                Digital pianos and this app imitate that sound; the techniques you learn here work on any of them.
            """, visual = image("grand_piano", "Steinway & Sons grand piano, New York, 1857 (Metropolitan Museum of Art)"))
            read("Sitting at the piano", """
                Good posture prevents tension and lets your fingers move freely.

                - Sit on the **front half** of the bench, facing middle C.
                - Your forearms should be roughly **level with the keys**; adjust the bench height.
                - Sit far enough back that your elbows are slightly in front of your body.
                - Feet flat on the floor (the right foot near the sustain pedal).
                - Shoulders relaxed, back tall but not stiff.
            """, visual = picture(Illustration.POSTURE), mood = MascotMood.TEACHING)
            read("Hand shape", """
                Let your arms hang by your sides, relaxed. Notice the natural curve of your fingers? That's the shape we want.

                - Imagine holding a **small bubble** or orange under your palm.
                - Play on the **pads** of your fingertips, close to the nail (the thumb plays on its side).
                - Keep your wrist level - not drooping, not lifted high.
            """, visual = picture(Illustration.HAND_SHAPE))
            read("Finger numbers", """
                Pianists number their fingers so music can tell you which finger to use.

                **Both hands** use the same numbers: thumb = **1**, index = **2**, middle = **3**, ring = **4**, pinky = **5**.

                So the left-hand thumb is 1, just like the right-hand thumb, even though they point in opposite directions.
            """, visual = Visual.Hands())
            quiz("Which finger is number 1?", "The thumb", "The pinky", "The index finger", "It depends on the hand",
                explain = "On both hands, the thumb is finger 1 and the pinky is finger 5.", visual = Visual.Hands())
            quiz("Your right-hand middle finger is…", "3", "2", "4", "5", visual = Visual.Hands(3))
            quiz("In the left hand, which finger is number 5?", "The pinky", "The thumb", "The ring finger", "The middle finger",
                explain = "Numbers don't flip between hands: 5 is always the pinky.")
            tap("Make your first sound! Press any white key.", "C4", "D4", "E4", "F4", "G4", "A4", "B4",
                anyOctave = true, any = true, explain = "Any key counts. Notice: further right = higher sound, further left = lower.")
        }
        lesson("black-keys", "Black Keys: Groups of 2 and 3", "The secret map that helps you find any note.") {
            read("The pattern", """
                Look at the black keys. They come in alternating **groups of two** and **groups of three**.

                This pattern repeats across the whole keyboard - a full piano has **88 keys**: 52 white and 36 black.
                Once you can see the groups, you can find any note instantly.
            """, visual = kb("C3", "B5", labels = KeyLabels.NONE, caption = "2 black keys, then 3 black keys, repeating"))
            tap("Tap both black keys in a group of TWO.", "C#4", "D#4", from = "C4", to = "B4", hint = true)
            tap("Now tap all three black keys in a group of THREE.", "F#4", "G#4", "A#4", from = "C4", to = "B4", hint = true)
            read("High and low", """
                Moving **right** on the keyboard, the sound gets **higher** (higher *pitch*). Moving **left**, it gets **lower**.

                Listen: here's a sound going up, then coming back down.
            """, visual = picture(Illustration.HIGH_LOW), play = "C3:e E3 G3 C4 E4 G4 C5 E5 G5 C6:q G5:e E5 C5 G4 E4 C4 G3 E3 C3:q", bpm = 120)
            ear("Listen. Did the notes go up or down?", "C4:q E4 G4 C5", "Up (higher)", "Down (lower)", "Stayed the same")
            ear("And this time?", "G5:q E5 C5 G4", "Down (lower)", "Up (higher)", "Stayed the same")
        }
        lesson("note-names", "The Musical Alphabet", "Seven letters: A B C D E F G - and where they live.") {
            read("Only seven letters", """
                Music uses just **seven letter names**: `A B C D E F G`. After G, we start again at A.

                The white keys are named with these letters. The easiest one to find is **C**: it's the white key just to the **left of a group of two black keys**.
            """, visual = kb("C4", "B4", "C4", labels = KeyLabels.HIGHLIGHTED, caption = "C sits just left of the two black keys"))
            tap("Find a C (any C!).", "C4", anyOctave = true, from = "C3", to = "B5")
            read("C D E around the two black keys", """
                - **C** - left of the two black keys
                - **D** - *between* the two black keys (like a dog in its kennel 🐶)
                - **E** - right of the two black keys
            """, visual = kb("C4", "B4", "C4", "D4", "E4"))
            tap("Find D - between the two black keys.", "D4", anyOctave = true, from = "C3", to = "B5")
            tap("Find E.", "E4", anyOctave = true, from = "C3", to = "B5")
            read("F G A B around the three black keys", """
                - **F** - left of the three black keys
                - **G** and **A** - the two keys *inside* the group of three
                - **B** - right of the three black keys
            """, visual = kb("C4", "B4", "F4", "G4", "A4", "B4"))
            tap("Find F.", "F4", anyOctave = true, from = "C3", to = "B5")
            tap("Find G.", "G4", anyOctave = true, from = "C3", to = "B5")
            tap("Find A.", "A4", anyOctave = true, from = "C3", to = "B5")
            tap("Find B.", "B4", anyOctave = true, from = "C3", to = "B5")
            play("Play the white keys from C up to the next C, naming each one aloud.", "C4:q D4 E4 F4 G4 A4 B4 C5", from = "C4", to = "C5")
            drill(DrillKind.NAME_KEY, count = 6)
            drill(DrillKind.FIND_KEY, count = 6)
        }
        lesson("octaves", "Octaves & Middle C", "Why the same letter appears again and again.") {
            read("What is an octave?", """
                From one C to the next C is an **octave** (8 white keys: C D E F G A B C).

                Two notes an octave apart share a name because they sound like the "same note", just higher or lower. Their frequencies are in the ratio **2 : 1** - the higher C vibrates exactly twice as fast.
            """, visual = kb("C3", "C5", "C3", "C4", "C5", caption = "Three Cs, each an octave apart"),
                play = "C3:h C4 C5 C4+C5:w", bpm = 100)
            read("Numbering the octaves", """
                To say *which* C we mean, we add a number. This is **scientific pitch notation**:

                - The lowest C on a piano is **C1** (the lowest key of all is A0).
                - The C closest to the middle of the keyboard is **C4**, called **Middle C**.
                - Numbers change at **C**: the B just below middle C is **B3**, and the D above it is **D4**.
                - The highest key is **C8**.
            """, visual = kb("C2", "C6", "C2", "C3", "C4", "C5", "C6", labels = KeyLabels.C_ONLY, caption = "C2 · C3 · C4 (middle C) · C5 · C6"))
            quiz("Which note is Middle C?", "C4", "C3", "C5", "C1")
            quiz("What is the note just below C4 called?", "B3", "B4", "C3", "A4",
                explain = "Octave numbers increase at C, so the B just below middle C is still in octave 3.")
            quiz("How many white keys make up one octave, counting both Cs?", "8", "7", "12", "5",
                explain = "C D E F G A B C = 8 notes. (There are 7 different letters, plus the repeated C.)")
            tap("Play middle C.", "C4", from = "C3", to = "B5")
            tap("Play the C an octave ABOVE middle C.", "C5", from = "C3", to = "B5")
            tap("Play the G just above middle C.", "G4", from = "C3", to = "B5")
            tap("Play A3 (the A just below middle C).", "A3", from = "C3", to = "B5")
            ear("Do these two notes form an octave?", "C4:h C5:h", "Yes - same name, one higher", "No")
        }
        lesson("sharps-flats", "Sharps, Flats & Half Steps", "Naming the black keys and measuring distance.") {
            read("Half steps and whole steps", """
                The smallest distance on the piano is a **half step** (also called a *semitone*): from one key to the very next key, black or white.

                - C → C♯ is a half step.
                - E → F is a half step (no black key between!).
                - B → C is a half step too.

                Two half steps make a **whole step** (*tone*): C → D, E → F♯, etc.
            """, visual = kb("C4", "C5", "E4", "F4", "B4", "C5", caption = "E-F and B-C are natural half steps"))
            read("Sharp ♯ and flat ♭", """
                - A **sharp (♯)** raises a note by a half step. C♯ is the key just right of C.
                - A **flat (♭)** lowers a note by a half step. D♭ is the key just left of D.
                - A **natural (♮)** cancels a sharp or flat.

                So each black key has **two names**: C♯ = D♭, F♯ = G♭ and so on. Notes that sound the same but are spelled differently are called **enharmonic**.
            """, visual = Visual.Keyboard("C4", "B4", listOf("C#4", "D#4", "F#4", "G#4", "A#4"), KeyLabels.HIGHLIGHTED,
                customLabels = mapOf("C#4" to "C♯\nD♭", "D#4" to "D♯\nE♭", "F#4" to "F♯\nG♭", "G#4" to "G♯\nA♭", "A#4" to "A♯\nB♭")))
            tap("Play F♯.", "F#4", anyOctave = true, from = "C4", to = "B5")
            tap("Play B♭.", "Bb4", anyOctave = true, from = "C4", to = "B5")
            tap("Play E♭.", "Eb4", anyOctave = true, from = "C4", to = "B5")
            quiz("C♯ and D♭ are…", "Enharmonic - the same key", "A whole step apart", "An octave apart", "Not on the piano")
            quiz("What is a half step above E?", "F", "F♯", "E♯ only", "D♯",
                explain = "There's no black key between E and F, so F is a half step above E. (E♯ is an enharmonic spelling of F.)")
            quiz("C to D is a…", "Whole step", "Half step", "Octave", "Unison")
            quiz("B to C is a…", "Half step", "Whole step", "Octave", "Third")
            tap("Sharps and flats are also white keys sometimes! Play C♭ (a half step below C).", "B4", anyOctave = true, from = "C4", to = "B5",
                explain = "C♭ sounds the same as B. Spelling depends on the key you're in.")
        }
        lesson("five-finger", "Your First Hand Position", "C position: one finger per key.") {
            read("C position, right hand", """
                Place your right thumb (1) on **middle C**. Let fingers 2, 3, 4, 5 rest on **D, E, F, G**.

                This is called **C five-finger position**. Each finger owns one key - no looking needed!
            """, visual = Visual.Keyboard("C4", "B4", listOf("C4", "D4", "E4", "F4", "G4"), fingers = mapOf("C4" to 1, "D4" to 2, "E4" to 3, "F4" to 4, "G4" to 5)))
            play("Play up and down: C D E F G F E D C. Use fingers 1-2-3-4-5-4-3-2-1.", "C4:q D4 E4 F4 G4 F4 E4 D4 C4:h",
                fingers = listOf(1, 2, 3, 4, 5, 4, 3, 2, 1))
            read("Left hand C position", """
                Now the left hand: put your **pinky (5)** on the C *below* middle C (C3). Fingers 4, 3, 2, 1 rest on D, E, F, G.

                Notice the left hand plays the same notes with *reversed* finger numbers.
            """, visual = Visual.Keyboard("C3", "B3", listOf("C3", "D3", "E3", "F3", "G3"), fingers = mapOf("C3" to 5, "D3" to 4, "E3" to 3, "F3" to 2, "G3" to 1)))
            play("Left hand: C D E F G F E D C (fingers 5-4-3-2-1-2-3-4-5).", "C3:q D3 E3 F3 G3 F3 E3 D3 C3:h", from = "C3", to = "B3",
                fingers = listOf(5, 4, 3, 2, 1, 2, 3, 4, 5))
            play("Your first tune: Hot Cross Buns!", "E4:q D4 C4:h E4:q D4 C4:h C4:e C4 C4 C4 D4 D4 D4 D4 E4:q D4 C4:h", fingers = listOf(3, 2, 1, 3, 2, 1))
            song("hot-cross-buns", "Play Hot Cross Buns in the song player - it adds a left hand for you.")
        }
        test {
            quiz("A full-size piano has how many keys?", "88", "76", "61", "100")
            quiz("C is the white key just to the left of…", "The group of two black keys", "The group of three black keys", "Any black key", "E")
            quiz("Which note lies between the two black keys?", "D", "C", "E", "G")
            drill(DrillKind.NAME_KEY, count = 4)
            drill(DrillKind.FIND_KEY, count = 4)
            tap("Play middle C.", "C4", from = "C3", to = "B5")
            tap("Play B♭3.", "Bb3", from = "C3", to = "B5")
            quiz("E to F is a…", "Half step", "Whole step", "Octave", "Minor third")
            quiz("Which finger is 4?", "Ring finger", "Middle finger", "Pinky", "Index finger")
            quiz("F♯ and G♭ are…", "Enharmonic equivalents", "A whole step apart", "Different keys", "An octave apart")
        }
    }

    unit("rhythm", "Rhythm & Beat", "Pulse, note values, rests, time signatures, dots and ties.") {
        lesson("pulse", "Feel the Beat", "Pulse, tempo and counting out loud.") {
            read("The beat", """
                Almost all music has a steady **pulse** called the **beat** - what you tap your foot to.

                How fast the beat goes is the **tempo**, measured in **BPM** (beats per minute). 60 BPM = one beat per second.

                A **metronome** clicks the beat for you. You'll find one in **Tools → Metronome**.
            """, visual = picture(Illustration.BEAT), play = "C5:q C5 C5 C5 C5 C5 C5 C5", bpm = 80)
            rhythm("Tap along with the metronome: one tap on every beat.", "q q q q q q q q", bpm = 72)
            read("Tempo words", "Composers often write tempo in Italian:", visual = table(listOf("Term", "Meaning", "Approx. BPM"),
                listOf("Largo", "Very slow, broad", "40-60"), listOf("Adagio", "Slow, at ease", "66-76"), listOf("Andante", "Walking pace", "76-108"),
                listOf("Moderato", "Moderate", "108-120"), listOf("Allegro", "Fast, lively", "120-156"), listOf("Presto", "Very fast", "168-200"),
                listOf("Accelerando", "Gradually faster", "-"), listOf("Ritardando (rit.)", "Gradually slower", "-"), listOf("A tempo", "Back to the original speed", "-")))
            quiz("Which tempo is fastest?", "Presto", "Andante", "Adagio", "Largo")
            quiz("'Andante' means…", "At a walking pace", "Very fast", "Gradually slower", "Very slow")
        }
        lesson("note-values", "Note Values", "Whole, half, quarter, eighth and sixteenth notes.") {
            read("How long is a note?", """
                The **shape** of a note tells you how long to hold it. In most music the **quarter note** gets one beat:
            """, visual = table(listOf("Note", "Name (US / UK)", "Beats in 4/4"),
                listOf("Whole", "Whole / semibreve", "4"), listOf("Half", "Half / minim", "2"), listOf("Quarter", "Quarter / crotchet", "1"),
                listOf("Eighth", "Eighth / quaver", "½"), listOf("Sixteenth", "Sixteenth / semiquaver", "¼")))
            read("See them on the staff", "Each note value is half the one before it.",
                visual = staff("C5:w | C5:h C5:h | C5:q C5 C5 C5 | C5:e C5 C5 C5 C5 C5 C5 C5", time = "4/4"),
                play = "C5:w C5:h C5:h C5:q C5 C5 C5 C5:e C5 C5 C5 C5 C5 C5 C5", bpm = 80)
            read("Fun fact", "In Britain the eighth note is called a **quaver**, the sixteenth a **semiquaver** and the quarter a **crotchet** - names that go back to medieval music theory.",
                mood = MascotMood.THINKING)
            quiz("How many beats does a half note get in 4/4?", "2", "1", "4", "½")
            quiz("How many eighth notes fit in one quarter note?", "2", "4", "1", "8")
            quiz("How many sixteenth notes equal a half note?", "8", "4", "16", "2")
            drill(DrillKind.NOTE_VALUE, count = 4)
            rhythm("Tap this rhythm: quarter, quarter, half.", "q q h", bpm = 80)
            rhythm("Tap: eighth eighth quarter, eighth eighth quarter.", "e e q e e q", bpm = 72)
        }
        lesson("rests", "Rests: Musical Silence", "Silence is part of the music.") {
            read("Rests", """
                Every note value has a matching **rest** - a symbol for silence of the same length.

                - **Whole rest** hangs *below* the 4th line (think: it's heavy, like a hole 🕳️).
                - **Half rest** sits *on* the 3rd line (like a hat 🎩).
                - **Quarter rest** looks like a squiggly zig-zag.
                - **Eighth rest** looks like a small 7 with a dot.
            """, visual = staff("R:w | R:h R:h | R:q R R R | R:e R R R R R R R", time = "4/4"))
            rhythm("Tap the notes, stay silent on the rests: quarter, rest, quarter, rest.", "q Rq q Rq", bpm = 76)
            rhythm("Now: half, quarter, rest.", "h q Rq", bpm = 76)
            quiz("A rest means…", "Silence for that duration", "Play louder", "Hold the previous note", "Repeat the bar")
        }
        lesson("time-signatures", "Time Signatures & Bars", "4/4, 3/4, 2/4 and how bars organise beats.") {
            read("Bars (measures)", """
                Beats are grouped into **bars** (also called *measures*), separated by **bar lines**. The first beat of each bar is usually a little stronger.

                The **time signature** at the start tells you how:
                - **Top number**: how many beats per bar.
                - **Bottom number**: which note gets one beat (4 = quarter, 8 = eighth, 2 = half).
            """, visual = staff("C5:q C5 C5 C5 | C5:h C5:h | C5:w", time = "4/4"))
            read("Common time signatures", "", visual = table(listOf("Signature", "Feel", "Examples"),
                listOf("4/4 (common time, C)", "ONE two three four", "Most pop, rock, Ode to Joy"),
                listOf("3/4", "ONE two three - waltz", "Waltzes, Happy Birthday, Minuet in G"),
                listOf("2/4", "ONE two - march", "Marches, polkas, Rondo alla Turca"),
                listOf("6/8", "ONE-and-a TWO-and-a (two big beats)", "Row Your Boat, Silent Night, ballads"),
                listOf("2/2 (cut time, ¢)", "Two half-note beats", "Fast marches, ragtime")))
            ear("Is this in 3/4 (waltz) or 4/4?", "C3:q E4+G4 E4+G4 G2 D4+F4 D4+F4 C3 E4+G4 E4+G4 C3:h.", "3/4", "4/4", bpm = 120)
            ear("And this one?", "C3:q E4+G4 G2 E4+G4 C3 E4+G4 G2 E4+G4", "4/4", "3/4", bpm = 120)
            quiz("In 3/4 time, how many quarter-note beats are in each bar?", "3", "4", "2", "6")
            quiz("The bottom number '4' in a time signature means…", "The quarter note gets one beat", "There are 4 beats", "Play 4 times", "Four sharps")
            rhythm("Tap a waltz pattern in 3/4: half, quarter.", "h q h q", bpm = 90, beatsPerBar = 3)
        }
        lesson("dots-ties", "Dots, Ties & Triplets", "Making notes longer and dividing beats in three.") {
            read("The dot", """
                A **dot** after a note adds **half of its value**.

                - Dotted half = 2 + 1 = **3 beats**
                - Dotted quarter = 1 + ½ = **1½ beats** (often followed by an eighth: "long-short")
            """, visual = staff("C5:h. C5:q | C5:q. C5:e C5:h", time = "4/4"), play = "C5:h. C5:q C5:q. C5:e C5:h", bpm = 90)
            read("The tie", """
                A **tie** is a curved line joining two notes of the **same pitch**: play the first and hold through the second.

                Ties let notes cross bar lines. (A curved line over *different* pitches is a **slur**, which means play smoothly - more on that later.)
            """, visual = picture(Illustration.TIE))
            read("Triplets", """
                A **triplet** squeezes **three** notes into the time of two. Eighth-note triplets fit three notes into one beat: "trip-o-let".

                Beethoven's Moonlight Sonata is made of continuous triplets.
            """, visual = Visual.Rhythm("t t t t t t q q", counts = "1 trip let 2 trip let 3 4"), play = "C5:t E5 G5 C5 E5 G5 C5 E5 G5 C5 E5 G5", bpm = 70)
            quiz("How many beats is a dotted half note (in 4/4)?", "3", "2", "2½", "4")
            quiz("How many beats is a dotted quarter note?", "1½", "1", "2", "¾")
            quiz("A tie connects…", "Two notes of the same pitch", "Two different pitches", "Two bars of rest", "Two hands")
            rhythm("Tap the long-short pattern: dotted quarter, eighth, half.", "q. e h", bpm = 72)
        }
        test {
            drill(DrillKind.NOTE_VALUE, count = 5)
            quiz("Which time signature is a waltz?", "3/4", "4/4", "2/4", "5/4")
            quiz("A dotted half note lasts…", "3 beats", "2 beats", "4 beats", "1½ beats")
            quiz("'Allegro' means…", "Fast and lively", "Slow", "At walking pace", "Gradually slower")
            quiz("Which rest hangs below the line?", "Whole rest", "Half rest", "Quarter rest", "Eighth rest")
            rhythm("Tap: quarter, quarter, half.", "q q h", bpm = 80)
            rhythm("Tap: dotted quarter, eighth, quarter, quarter.", "q. e q q", bpm = 72)
            quiz("How many eighth notes fill a 4/4 bar?", "8", "4", "16", "6")
        }
    }

    unit("reading", "Reading Music", "The staff, treble and bass clef, ledger lines, grand staff.") {
        lesson("staff", "The Staff", "Five lines, four spaces - a map of pitch.") {
            read("Lines and spaces", """
                Music is written on a **staff** (or *stave*): **5 lines** and **4 spaces**. Lines are counted from the **bottom** up.

                Notes sit either **on a line** (the line goes through the middle) or **in a space**. Higher on the staff = higher pitch. Each step from a line to the next space is one letter of the alphabet.
            """, visual = staff("E4:q F4 G4 A4 B4 C5 D5 E5 F5", names = true, caption = "Each step up the staff is the next letter"),
                play = "E4:q F4 G4 A4 B4 C5 D5 E5 F5", bpm = 110)
            quiz("How many lines does a staff have?", "5", "4", "6", "7")
            quiz("Moving from a line to the space just above it raises the note by…", "One letter (a step)", "An octave", "Two letters", "Nothing")
        }
        lesson("treble", "The Treble Clef", "Reading the right-hand notes.") {
            read("The treble clef", """
                The **treble clef** (or *G clef*) curls around the **2nd line**, marking it as **G4** (the G above middle C).

                It's used for higher notes - usually the right hand.

                ## Memory tricks
                - **Lines** (bottom to top): E G B D F - "**E**very **G**ood **B**oy **D**eserves **F**ruit"
                - **Spaces** spell **F A C E** - "FACE in the space"
            """, visual = staff("E4:q G4 B4 D5 F5 | F4 A4 C5 E5", names = true, caption = "Lines: E G B D F · Spaces: F A C E"))
            drill(DrillKind.NAME_TREBLE, count = 8, level = 1)
            tap("This note is on the 2nd line of the treble clef. Play it.", "G4", from = "C4", to = "B5", staff = Clef.TREBLE,
                explain = "The treble clef wraps around G - so the 2nd line is G4.")
            drill(DrillKind.FIND_STAFF_NOTE, count = 5, level = 1)
        }
        lesson("bass", "The Bass Clef", "Reading the left-hand notes.") {
            read("The bass clef", """
                The **bass clef** (or *F clef*) has two dots around the **4th line**, marking it as **F3** (the F below middle C).

                ## Memory tricks
                - **Lines** (bottom to top): G B D F A - "**G**ood **B**oys **D**o **F**ine **A**lways"
                - **Spaces**: A C E G - "**A**ll **C**ows **E**at **G**rass"
            """, visual = staff("G2:q B2 D3 F3 A3 | A2 C3 E3 G3", clef = Clef.BASS, names = true, caption = "Lines: G B D F A · Spaces: A C E G"))
            drill(DrillKind.NAME_BASS, count = 8, level = 1)
            drill(DrillKind.FIND_STAFF_NOTE, count = 4, level = 2)
        }
        lesson("grand-staff", "The Grand Staff & Ledger Lines", "Both hands together, and notes beyond the lines.") {
            read("The grand staff", """
                Piano music uses two staves joined by a brace: treble on top, bass below. Together they form the **grand staff**.

                **Middle C** sits right between them, on its own little line called a **ledger line**.
                - Written just below the treble staff, it's usually played by the right hand.
                - Written just above the bass staff, usually by the left hand.
            """, visual = staff("C4:w", clef = Clef.GRAND, names = true), play = "C4:w", bpm = 80)
            read("Ledger lines", """
                Notes too high or low for the staff get short extra lines: **ledger lines**. Count up or down from the staff, alternating line - space - line, just like inside the staff.
            """, visual = staff("C4:q D4 A5 B5 C6", names = true))
            read("Landmark notes", """
                Don't read every note from scratch - learn a few **landmarks** and count from them:
                - **Middle C** (C4) - between the staves
                - **Treble G** (G4) - 2nd line of the treble clef
                - **Bass F** (F3) - 4th line of the bass clef
                - **High C** (C5) - 3rd space of treble; **Low C** (C3) - 2nd space of bass
            """, visual = staff("C3:q F3 C4 G4 C5", clef = Clef.GRAND, names = true))
            drill(DrillKind.NAME_GRAND, count = 8, level = 1)
            quiz("Middle C on the grand staff is written…", "On a ledger line between the staves", "On the top line of the treble staff", "In the 3rd space of the bass staff", "It can't be written")
        }
        lesson("steps-skips", "Steps, Skips & Leaps", "Read by direction and distance, not letter by letter.") {
            read("Reading by interval", """
                Good readers look at the **shape**:
                - **Step**: line → next space (or space → next line). Next letter.
                - **Skip**: line → next line, or space → next space. Skips one letter (a *third*).
                - **Leap**: anything bigger.
            """, visual = staff("C4:q D4 E4 | C4 E4 G4 | C4 G4 C5", names = true, caption = "steps · skips · leaps"))
            ear("Steps or skips?", "C4:q D4 E4 F4 G4", "Steps", "Skips", "Leaps")
            ear("Steps or skips?", "C4:q E4 G4 B4", "Skips", "Steps", "Repeated notes")
            play("Read and play: steps up, then a skip down.", "C4:q D4 E4 C4:h", staff = Clef.TREBLE, hints = false)
            play("Read and play this melody.", "E4:q G4 E4 C4 D4 F4 D4 B3 C4:w", from = "A3", to = "B4", staff = Clef.TREBLE, hints = false)
        }
        test {
            drill(DrillKind.NAME_TREBLE, count = 5, level = 1)
            drill(DrillKind.NAME_BASS, count = 5, level = 1)
            quiz("The treble clef is also called the…", "G clef", "F clef", "C clef", "Bass clef")
            quiz("The spaces of the treble staff spell…", "FACE", "ACEG", "EGBDF", "GBDFA")
            quiz("The lines of the bass staff are…", "G B D F A", "E G B D F", "F A C E", "A C E G")
            drill(DrillKind.FIND_STAFF_NOTE, count = 3)
        }
    }

    unit("first-pieces", "First Pieces", "Hands together, dynamics, articulation and your first songs.") {
        lesson("melodies", "Melodies in C Position", "Reading and playing real tunes.") {
            play("Mary Had a Little Lamb (start).", "E4:q D4 C4 D4 E4 E4 E4:h D4:q D4 D4:h E4:q G4 G4:h", staff = Clef.TREBLE)
            play("Ode to Joy (start).", "E4:q E4 F4 G4 G4 F4 E4 D4 C4 C4 D4 E4 E4:q. D4:e D4:h", staff = Clef.TREBLE)
            song("ode-to-joy", "Now play the full Ode to Joy with accompaniment.")
            song("mary-lamb", "Try Mary Had a Little Lamb in the song player.")
        }
        lesson("hands-together", "Hands Together", "Coordinating left and right.") {
            read("Two hands, one brain", """
                Playing with both hands is a new skill - be patient!

                1. Learn each hand **separately** until it's easy.
                2. Put them together **very slowly**.
                3. Start with the left hand playing just **one long note** while the right plays the melody.
            """, visual = Visual.Keyboard(from = "C3", to = "B4", highlights = listOf("C4", "D4", "E4", "F4", "G4"),
                secondary = listOf("C3", "D3", "E3", "F3", "G3"),
                fingers = mapOf("C3" to 5, "D3" to 4, "E3" to 3, "F3" to 2, "G3" to 1, "C4" to 1, "D4" to 2, "E4" to 3, "F4" to 4, "G4" to 5),
                caption = "Both hands on C: left hand 5-4-3-2-1, right hand 1-2-3-4-5"))
            play("Left hand C and right hand C together, then left G with right G.", "C3+C4:h G3+G4:h C3+C4:w", from = "C3", to = "B4")
            play("Right hand melody over a held left-hand C.", "C3+E4:q D4 C4 D4 E4 E4 E4:h", from = "C3", to = "B4")
            play("Contrary motion: hands move apart from C.", "C3+C4:q B2+D4 A2+E4 G2+F4 F2+G4:w", from = "F2", to = "G4")
            song("twinkle", "Twinkle Twinkle in the song player: try 'Left hand' mode first, then 'Both'.")
        }
        lesson("dynamics", "Dynamics: Loud & Soft", "Piano and forte - the instrument's very name.") {
            read("Why it's called the piano", """
                The full name is *pianoforte* - "soft-loud" - because it was the first keyboard that could play both, depending on how hard you press.

                Dynamics are written in Italian:
            """, visual = symbols("pp" to "**pianissimo** - very soft", "p" to "**piano** - soft", "mp" to "**mezzo-piano** - medium soft",
                "mf" to "**mezzo-forte** - medium loud", "f" to "**forte** - loud", "ff" to "**fortissimo** - very loud",
                "<" to "**crescendo** (cresc.) - gradually louder", ">" to "**diminuendo / decrescendo** - gradually softer",
                "sfz" to "**sforzando** - sudden strong accent"))
            read("Hear the difference", "Here's the same phrase played softly, then loudly.", play = "C4:q E4 G4 C5:h R:2 C4:q E4 G4 C5:h", bpm = 100)
            quiz("'p' stands for…", "Piano - soft", "Pause", "Pedal", "Presto")
            quiz("Which is loudest?", "ff", "mf", "f", "mp")
            quiz("A crescendo means…", "Gradually louder", "Gradually softer", "Gradually faster", "Suddenly soft")
            read("Touch-sensitive keys", """
                On a real or MIDI piano, speed of key press = loudness. On the touch screen, Keyscape makes notes **louder when you touch lower on the key** - try it in Free Play!
            """, visual = picture(Illustration.TOUCH), mood = MascotMood.THINKING)
        }
        lesson("articulation", "Articulation: Legato & Staccato", "How notes connect.") {
            read("Articulation", "How each note starts and ends changes the music's character.", visual = symbols(
                "⌒" to "**Slur / legato** - smooth and connected; don't lift until the next note sounds",
                "•" to "**Staccato** - short and detached (about half the written length)",
                ">" to "**Accent** - emphasise this note",
                "—" to "**Tenuto** - hold for full value, slightly weighted",
                "fermata" to "**Fermata** - hold longer than written, at the performer's choice"))
            read("Hear legato vs staccato", "First smooth (legato), then short (staccato):",
                play = "C4:q D4 E4 F4 G4:h R:2 C4:e R:e D4:e R:e E4:e R:e F4:e R:e G4:q", bpm = 100)
            quiz("Dots above or below notes mean…", "Staccato - short and detached", "Play louder", "Hold longer", "Use the pedal")
            quiz("Legato means…", "Smooth and connected", "Short and bouncy", "Very loud", "Fast")
        }
        lesson("next-steps", "Where Next?", "Choose your path.") {
            read("You've built the foundation! 🎉", """
                You can now find any note, read both clefs, count rhythms and play simple tunes with two hands.

                From here, choose any path - they all build on this course:
                - **Music Theory A-Z** - understand *why* music works (strongly recommended next)
                - **Classical**, **Romantic**, **Jazz**, **Pop & Modern**, **Blues** and more
                - **Technique Lab** and **Sight-Reading** to train your hands and eyes
                - **Ear Training** to hear what you play
            """, mood = MascotMood.EXCITED)
            song("london-bridge", "Celebrate with London Bridge - your first dotted rhythm!")
        }
        test {
            quiz("Which dynamic is softest?", "pp", "p", "mp", "mf")
            quiz("Staccato notes are…", "Short and detached", "Smooth and connected", "Loud", "Held longer")
            quiz("Pianoforte means…", "Soft-loud", "Fast-slow", "High-low", "Keyboard-strings")
            play("Play this melody from the staff.", "C4:q E4 G4 E4 F4 D4 B3 C4", from = "A3", to = "B4", staff = Clef.TREBLE, hints = false)
            drill(DrillKind.NAME_GRAND, count = 4)
            quiz("When learning hands together, you should first…", "Practise each hand separately", "Play as fast as possible", "Skip the left hand", "Only use the pedal")
        }
    }
}
