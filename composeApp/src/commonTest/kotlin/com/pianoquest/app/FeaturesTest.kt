package com.pianoquest.app

import com.pianoquest.app.data.AutoPedal
import com.pianoquest.app.data.Hand
import com.pianoquest.app.data.NoteEvent
import com.pianoquest.app.data.SongData
import com.pianoquest.app.data.Today
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.ui.FormFactor
import com.pianoquest.app.ui.UiScale
import com.pianoquest.app.ui.screens.HandChoice
import com.pianoquest.app.ui.screens.PlayMode
import com.pianoquest.app.ui.screens.SongRun
import com.pianoquest.app.ui.screens.octaveMatters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeaturesTest {
    private fun midi(vararg names: String) = names.map { Pitch.parse(it).midi }

    @Test
    fun octaveIsOnlyRequiredWhenTheExerciseIsAboutRegister() {
        assertFalse(octaveMatters(midi("C4", "E4", "G4"), "Play a C major chord"))
        assertFalse(octaveMatters(midi("C4", "D4", "E4", "F4", "G4"), "Play up: C D E F G"))
        assertTrue(octaveMatters(midi("C4"), "Find middle C"))
        assertTrue(octaveMatters(midi("C3", "C4", "C5"), "Play every C"))
        assertTrue(octaveMatters(midi("E4", "G4", "C5"), "Play C major in 1st inversion (E G C)"))
        assertFalse(octaveMatters(midi("C3", "C4"), "Any C", forceAny = true))
    }

    @Test
    fun songOfTheDayChangesEveryDay() {
        val days = (20000L until 20120L).map { Today.songOfTheDay(it).id }
        for (i in 1 until days.size) assertNotEquals(days[i - 1], days[i], "same song on consecutive days")
        // A full rotation shows every piece once before repeating.
        assertEquals(days.size, days.distinct().size)
    }

    @Test
    fun automaticPedalAlternatesAndEndsUp() {
        // A broken-chord accompaniment with a new bass note every bar.
        val ev = ArrayList<NoteEvent>()
        for (bar in 0 until 8) {
            val bass = listOf("C3", "A2", "F2", "G2")[bar % 4]
            ev += NoteEvent(Pitch.parse(bass), bar * 4.0, 1.0, Hand.LEFT)
            for (k in 1..3) ev += NoteEvent(Pitch.parse(listOf("E3", "G3", "C4")[k - 1]), bar * 4.0 + k, 1.0, Hand.LEFT)
            ev += NoteEvent(Pitch.parse("E5"), bar * 4.0, 4.0, Hand.RIGHT)
        }
        val pedal = AutoPedal.generate(ev, 4.0)
        assertTrue(pedal.size >= 8, "expected pedal changes, got ${pedal.size}")
        for (i in 1 until pedal.size) assertNotEquals(pedal[i - 1].down, pedal[i].down, "pedal must alternate")
        assertFalse(pedal.last().down, "pedal must be released at the end")
        // Changes happen with the bass notes, not on the broken-chord notes in between.
        val downs = pedal.filter { it.down }.map { it.beat - 0.12 }
        assertTrue(downs.all { kotlin.math.abs(it - kotlin.math.round(it / 4.0) * 4.0) < 1e-6 }, "pedal changed off the bass notes: $downs")
    }

    @Test
    fun tabletsScaleUpPhonesDoNot() {
        assertEquals(1f, UiScale.auto(892f, 412f))
        assertEquals(UiScale.MAX_AUTO, UiScale.auto(1280f, 800f))
        assertEquals(FormFactor.PHONE, UiScale.formFactor(892f, 412f))
        assertEquals(FormFactor.TABLET, UiScale.formFactor(1280f, 800f))
    }

    @Test
    fun perHandScores() {
        val ev = listOf(
            NoteEvent(Pitch.parse("C5"), 0.0, 1.0, Hand.RIGHT), NoteEvent(Pitch.parse("D5"), 1.0, 1.0, Hand.RIGHT),
            NoteEvent(Pitch.parse("C3"), 0.0, 1.0, Hand.LEFT), NoteEvent(Pitch.parse("G2"), 1.0, 1.0, Hand.LEFT),
        )
        val run = SongRun(SongData(ev, 60.0, 4.0, listOf(0.0)), PlayMode.PERFORM, HandChoice.RIGHT, 4, 4)
        run.handPerfect[0] = 2
        assertEquals(100, run.handScore(0))
        assertNull(run.handScore(1), "the left hand was not played")
        // Listen mode scores only the notes that went by while their hand was shown.
        val listen = SongRun(SongData(ev, 60.0, 4.0, listOf(0.0)), PlayMode.LISTEN, HandChoice.BOTH, 4, 4)
        listen.shown = HandChoice.RIGHT
        assertFalse(listen.playable(2))
        listen.counted[0] = true; listen.counted[1] = true
        listen.handPerfect[0] = 1
        assertEquals(50, listen.handScore(0))
        assertNull(listen.handScore(1))
    }
}
