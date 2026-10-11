package com.pianoquest.app

import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.Drills
import com.pianoquest.app.data.Notation
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.data.Step
import com.pianoquest.app.data.Visual
import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.RomanNumerals
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class ContentTest {

    private fun checkVisual(v: Visual?, where: String, problems: MutableList<String>) {
        try {
            when (v) {
                null -> {}
                is Visual.Keyboard -> (listOf(v.from, v.to) + v.highlights + v.secondary + v.fingers.keys + v.customLabels.keys).forEach { Pitch.parse(it) }
                is Visual.Staff -> if (v.notes.isNotBlank()) Notation.parse(v.notes)
                is Visual.Chords -> v.symbols.forEach { Chord.parse(it) }
                is Visual.Progression -> RomanNumerals.progression(v.numerals, Key.parse(v.key))
                is Visual.Image -> if (com.pianoquest.app.ui.components.LessonImages.resource(v.key) == null) problems += "$where: unknown image ${v.key}"
                is Visual.Composers -> v.items.forEach { c ->
                    if (com.pianoquest.app.ui.components.LessonImages.resource(c.image) == null) problems += "$where: unknown image ${c.image}"
                    if (c.songId != null && SongLibrary.get(c.songId) == null) problems += "$where: unknown song ${c.songId}"
                }
                is Visual.Rhythm -> v.pattern.split(" ").filter { it.isNotBlank() }.forEach { Notation.parseDuration(it.removePrefix("R").ifEmpty { "q" }) }
                else -> {}
            }
        } catch (e: Exception) {
            problems += "$where visual: ${e.message}"
        }
    }

    private fun checkStep(s: Step, where: String, problems: MutableList<String>) {
        try {
            when (s) {
                is Step.Read -> { checkVisual(s.visual, where, problems); s.play?.let { Notation.parse(it) } }
                is Step.Quiz -> {
                    checkVisual(s.visual, where, problems); s.play?.let { Notation.parse(it) }
                    if (s.options.size < 2) problems += "$where: quiz needs 2+ options"
                    if (s.options.toSet().size != s.options.size) problems += "$where: duplicate options ${s.options}"
                }
                is Step.TapKeys -> { s.targets.forEach { Pitch.parse(it) }; Pitch.parse(s.from); Pitch.parse(s.to)
                    if (!s.anyOctave) s.targets.forEach { t ->
                        if (Pitch.parse(t).midi !in Pitch.parse(s.from).midi - 1..Pitch.parse(s.to).midi + 1) problems += "$where: target $t outside ${s.from}-${s.to}"
                    }
                }
                is Step.PlayAlong -> {
                    val ev = Notation.parse(s.notes)
                    val lo = Pitch.parse(s.from).midi - 1
                    val hi = Pitch.parse(s.to).midi + 1
                    ev.forEach { if (it.midi !in lo..hi) problems += "$where: note ${it.pitch} outside ${s.from}-${s.to}" }
                }
                is Step.RhythmTap -> s.pattern.split(" ").filter { it.isNotBlank() }.forEach { Notation.parseDuration(it.removePrefix("R").ifEmpty { "q" }) }
                is Step.Drill -> repeat(30) { i -> Drills.expand(s, Random(i)).forEach { checkStep(it, "$where[drill ${s.kind}]", problems) } }
                is Step.SongPractice -> if (SongLibrary.get(s.songId) == null) problems += "$where: unknown song ${s.songId}"
            }
        } catch (e: Exception) {
            problems += "$where: ${e::class.simpleName} ${e.message}"
        }
    }

    @Test
    fun allCourseContentIsValid() {
        val problems = mutableListOf<String>()
        val ids = HashSet<String>()
        for (c in CourseCatalog.courses) {
            for (u in c.units) {
                if (u.test.isEmpty()) problems += "${u.id}: no test"
                for (l in u.lessons) {
                    if (!ids.add(l.id)) problems += "duplicate lesson id ${l.id}"
                    if (l.steps.isEmpty()) problems += "${l.id}: empty"
                    l.steps.forEachIndexed { i, s -> checkStep(s, "${l.id}#$i", problems) }
                }
                u.test.forEachIndexed { i, s -> checkStep(s, "${u.testId}#$i", problems) }
            }
        }
        if (problems.isNotEmpty()) fail(problems.distinct().joinToString("\n"))
        val lessons = CourseCatalog.courses.sumOf { it.lessonCount }
        println("Courses: ${CourseCatalog.courses.size}, lessons: $lessons, songs: ${SongLibrary.all.size}")
        assertTrue(lessons > 100)
    }
}
