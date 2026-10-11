package com.pianoquest.app.data

enum class MascotMood { HAPPY, TEACHING, THINKING, EXCITED, SAD, WAVING }

enum class Clef { TREBLE, BASS, GRAND }

enum class KeyLabels { NONE, HIGHLIGHTED, C_ONLY, WHITE, ALL, COLORED }

/** Illustrations attached to lesson steps. Note names use scientific pitch notation ("C4" = middle C). */
sealed interface Visual {
    data class Keyboard(
        val from: String = "C4",
        val to: String = "B4",
        val highlights: List<String> = emptyList(),
        val labels: KeyLabels = KeyLabels.HIGHLIGHTED,
        val fingers: Map<String, Int> = emptyMap(),
        val secondary: List<String> = emptyList(),
        val customLabels: Map<String, String> = emptyMap(),
        val caption: String? = null,
    ) : Visual

    data class Staff(
        val clef: Clef = Clef.TREBLE,
        val notes: String = "",
        val keySig: Int = 0,
        val timeSig: String? = null,
        val showNames: Boolean = false,
        val labels: List<String> = emptyList(),
        val caption: String? = null,
    ) : Visual

    data class CircleOfFifths(val highlight: String? = null) : Visual

    data class Rhythm(val pattern: String, val timeSig: String = "4/4", val counts: String? = null) : Visual

    data class Hands(val highlightFinger: Int? = null) : Visual

    data class Chords(val symbols: List<String>, val numerals: List<String> = emptyList()) : Visual

    data class Progression(val numerals: String, val key: String) : Visual

    data class Table(val header: List<String>, val rows: List<List<String>>) : Visual

    data class Symbols(val items: List<Pair<String, String>>) : Visual

    /** A picture (composer portrait or historical instrument), by key in LessonImages. */
    data class Image(val key: String, val caption: String) : Visual

    /** Interactive composer cards: portrait, dates and a button that opens one of their pieces. */
    data class Composers(val items: List<ComposerCard>) : Visual

    /** A drawn teaching picture (posture, hand shape, pedals ...). */
    data class Picture(val kind: Illustration, val caption: String? = null) : Visual
}

/** The drawn teaching pictures available to lessons. */
enum class Illustration { POSTURE, HAND_SHAPE, ARM_WEIGHT, HIGH_LOW, BEAT, TIE, TOUCH, PEDALS }

fun picture(kind: Illustration, caption: String? = null) = Visual.Picture(kind, caption)

data class ComposerCard(val image: String, val name: String, val years: String, val note: String = "", val songId: String? = null)

fun image(key: String, caption: String) = Visual.Image(key, caption)

fun composers(vararg items: ComposerCard) = Visual.Composers(items.toList())

/** One screen inside a lesson or test. */
sealed interface Step {
    /** Explanation with optional illustration and listen button. */
    data class Read(
        val title: String,
        val body: String,
        val visual: Visual? = null,
        val play: String? = null,
        val playBpm: Int = 90,
        val mood: MascotMood = MascotMood.TEACHING,
    ) : Step

    /** Multiple choice; the first option is correct (shuffled at runtime). Optional sound for ear training. */
    data class Quiz(
        val question: String,
        val options: List<String>,
        val explanation: String = "",
        val visual: Visual? = null,
        val play: String? = null,
        val playBpm: Int = 90,
    ) : Step

    /** Press the given keys (any order). With [anyOctave], the pitch class is enough. */
    data class TapKeys(
        val prompt: String,
        val targets: List<String>,
        val anyOctave: Boolean = false,
        val from: String = "C4",
        val to: String = "B4",
        val showHint: Boolean = false,
        val explanation: String = "",
        val staff: Clef? = null,
        /** When true, pressing any ONE of the targets completes the step. */
        val matchAny: Boolean = false,
    ) : Step

    /** Play a sequence on the keyboard (or MIDI keyboard). The app waits for each correct note (wait mode). */
    data class PlayAlong(
        val prompt: String,
        val notes: String,
        val from: String = "C4",
        val to: String = "B4",
        val hints: Boolean = true,
        val staff: Clef? = null,
        val fingers: List<Int> = emptyList(),
        val bpm: Int = 80,
    ) : Step

    /** Tap the rhythm on a big pad (or any key) in time with the metronome. */
    data class RhythmTap(val prompt: String, val pattern: String, val bpm: Int = 80, val beatsPerBar: Int = 4) : Step

    /** Auto-generated questions (note reading, ear training ...). Expanded into concrete steps at runtime. */
    data class Drill(val kind: DrillKind, val count: Int = 5, val level: Int = 1, val keys: List<String> = emptyList()) : Step

    /** Points the learner to a song in the library. */
    data class SongPractice(val songId: String, val text: String) : Step
}

val Step.isGraded: Boolean get() = when (this) {
    is Step.Quiz, is Step.TapKeys, is Step.PlayAlong, is Step.RhythmTap -> true
    else -> false
}

enum class DrillKind(val title: String) {
    NAME_TREBLE("Name the treble-clef note"),
    NAME_BASS("Name the bass-clef note"),
    NAME_GRAND("Name the note (grand staff)"),
    FIND_KEY("Find the key"),
    NAME_KEY("Name the highlighted key"),
    FIND_STAFF_NOTE("Play the note on the staff"),
    INTERVAL_STAFF("Name the interval"),
    INTERVAL_EAR("Hear the interval"),
    CHORD_QUALITY_EAR("Major, minor, diminished or augmented?"),
    SEVENTH_EAR("Which seventh chord?"),
    CHORD_NAME("Name the chord"),
    PLAY_CHORD("Play the chord"),
    PLAY_SCALE("Play the scale"),
    KEY_SIGNATURE("Name the key signature"),
    RELATIVE_KEY("Relative major / minor"),
    NOTE_VALUE("Note values"),
    DIATONIC_CHORD("Chords in a key"),
    CIRCLE("Circle of fifths"),
    CADENCE_EAR("Hear the cadence"),
    PROGRESSION_EAR("Hear the progression"),
    MODE_ID("Identify the mode"),
    SCALE_DEGREE("Scale degrees"),
    TERMS("Musical terms"),
}

data class Lesson(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<Step>,
    val minutes: Int,
)

data class CourseUnit(
    val id: String,
    val title: String,
    val summary: String,
    val lessons: List<Lesson>,
    val test: List<Step>,
) {
    val testId: String get() = "$id.test"
}

enum class CourseIcon { FOUNDATIONS, THEORY, CLASSICAL, ROMANTIC, JAZZ, POP, BLUES, GOSPEL, LATIN, FILM, IMPROV, EAR, SIGHT, TECHNIQUE, BAROQUE, RAGTIME }

data class Course(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val level: String,
    val color: Long,
    val icon: CourseIcon,
    val units: List<CourseUnit>,
    val recommendedAfter: String? = null,
) {
    val lessonCount: Int get() = units.sumOf { it.lessons.size }
    val allLessons: List<Lesson> get() = units.flatMap { it.lessons }
}

// ---------------------------------------------------------------- DSL ------------------------------------------------

@DslMarker
annotation class CourseDsl

@CourseDsl
class CourseBuilder(private val courseId: String) {
    val units = ArrayList<CourseUnit>()
    fun unit(id: String, title: String, summary: String, block: UnitBuilder.() -> Unit) {
        val b = UnitBuilder("$courseId.$id").apply(block)
        units += CourseUnit("$courseId.$id", title, summary, b.lessons, b.testSteps)
    }
}

@CourseDsl
class UnitBuilder(private val unitId: String) {
    val lessons = ArrayList<Lesson>()
    var testSteps: List<Step> = emptyList()
    fun lesson(id: String, title: String, summary: String, block: StepsBuilder.() -> Unit) {
        val steps = StepsBuilder().apply(block).steps
        val minutes = (steps.size * 0.8).toInt().coerceIn(3, 15)
        lessons += Lesson("$unitId.$id", title, summary, steps, minutes)
    }
    fun test(block: StepsBuilder.() -> Unit) {
        testSteps = StepsBuilder().apply(block).steps
    }
}

@CourseDsl
class StepsBuilder {
    val steps = ArrayList<Step>()

    fun read(title: String, body: String, visual: Visual? = null, play: String? = null, bpm: Int = 90, mood: MascotMood = MascotMood.TEACHING) {
        steps += Step.Read(title, body.trimIndent(), visual, play, bpm, mood)
    }

    fun quiz(question: String, correct: String, vararg wrong: String, explain: String = "", visual: Visual? = null, play: String? = null, bpm: Int = 90) {
        steps += Step.Quiz(question, listOf(correct) + wrong, explain, visual, play, bpm)
    }

    fun ear(question: String, play: String, correct: String, vararg wrong: String, explain: String = "", bpm: Int = 90) {
        steps += Step.Quiz(question, listOf(correct) + wrong, explain, null, play, bpm)
    }

    fun tap(prompt: String, vararg targets: String, anyOctave: Boolean = false, from: String = "C4", to: String = "B4",
            hint: Boolean = false, explain: String = "", staff: Clef? = null, any: Boolean = false) {
        steps += Step.TapKeys(prompt, targets.toList(), anyOctave, from, to, hint, explain, staff, any)
    }

    fun play(prompt: String, notes: String, from: String = "C4", to: String = "B4", hints: Boolean = true, staff: Clef? = null,
             fingers: List<Int> = emptyList(), bpm: Int = 80) {
        steps += Step.PlayAlong(prompt, notes, from, to, hints, staff, fingers, bpm)
    }

    fun rhythm(prompt: String, pattern: String, bpm: Int = 80, beatsPerBar: Int = 4) {
        steps += Step.RhythmTap(prompt, pattern, bpm, beatsPerBar)
    }

    fun drill(kind: DrillKind, count: Int = 5, level: Int = 1, keys: List<String> = emptyList()) {
        steps += Step.Drill(kind, count, level, keys)
    }

    fun song(songId: String, text: String) {
        steps += Step.SongPractice(songId, text)
    }
}

fun course(
    id: String, title: String, subtitle: String, description: String, level: String, color: Long, icon: CourseIcon,
    recommendedAfter: String? = null, block: CourseBuilder.() -> Unit,
): Course {
    val b = CourseBuilder(id).apply(block)
    // Source lines are wrapped for readability; join them so the text flows (blank lines stay paragraph breaks).
    val text = description.trimIndent().split(Regex("\\n\\s*\\n")).joinToString("\n\n") { it.lines().joinToString(" ") { l -> l.trim() } }
    return Course(id, title, subtitle, text, level, color, icon, b.units, recommendedAfter)
}

// Visual helpers for concise content authoring.
fun kb(from: String, to: String, vararg highlights: String, labels: KeyLabels = KeyLabels.HIGHLIGHTED, caption: String? = null) =
    Visual.Keyboard(from, to, highlights.toList(), labels, caption = caption)

fun staff(notes: String, clef: Clef = Clef.TREBLE, keySig: Int = 0, time: String? = null, names: Boolean = false, caption: String? = null) =
    Visual.Staff(clef, notes, keySig, time, names, caption = caption)

fun chords(vararg symbols: String) = Visual.Chords(symbols.toList())

fun progression(numerals: String, key: String) = Visual.Progression(numerals, key)

fun table(header: List<String>, vararg rows: List<String>) = Visual.Table(header, rows.toList())

fun symbols(vararg items: Pair<String, String>) = Visual.Symbols(items.toList())
