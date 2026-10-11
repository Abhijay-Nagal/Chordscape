package com.pianoquest.app

import com.pianoquest.app.data.Arranger
import com.pianoquest.app.data.Difficulty
import com.pianoquest.app.data.Hand
import com.pianoquest.app.data.MidiFileReader
import com.pianoquest.app.data.SongLibrary
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/** Parses every bundled complete score and checks the Easy/Medium/Hard arrangements. */
class MidiScoresTest {
    private val dir = File("src/commonMain/composeResources/files/midi")

    @Test
    fun everyBundledScoreParses() {
        val midiSongs = SongLibrary.all.filter { it.midiResource != null }
        assertTrue(midiSongs.size >= 60, "expected 60+ complete scores, got ${midiSongs.size}")
        val problems = mutableListOf<String>()
        for (song in midiSongs) {
            val f = File(dir, "${song.midiResource}.mid")
            if (!f.exists()) { problems += "${song.id}: missing ${f.path}"; continue }
            try {
                val parsed = MidiFileReader.parse(f.readBytes())
                val (events, bars) = MidiFileReader.toEvents(parsed)
                if (events.size < 40) problems += "${song.id}: only ${events.size} notes"
                if (events.none { it.hand == Hand.RIGHT }) problems += "${song.id}: no right-hand notes"
                if (events.none { it.hand == Hand.LEFT }) problems += "${song.id}: no left-hand notes"
                if (bars.size < 4) problems += "${song.id}: only ${bars.size} bars"
                if (kotlin.math.abs(parsed.lengthSec - (song.durationHint ?: 0.0)) > 2.0) problems += "${song.id}: length ${parsed.lengthSec} vs hint ${song.durationHint}"
                val easy = Arranger.arrange(events, Difficulty.EASY)
                val medium = Arranger.arrange(events, Difficulty.MEDIUM)
                if (!(easy.size < medium.size && medium.size <= events.size)) problems += "${song.id}: arrangement sizes ${easy.size}/${medium.size}/${events.size}"
            } catch (e: Exception) {
                problems += "${song.id}: ${e::class.simpleName} ${e.message}"
            }
        }
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }

    /** Classical pieces without pedal marks get pedal on chords and broken chords, but not on fast scales and turns. */
    @Test
    fun automaticPedalForClassicalPieces() {
        fun plan(id: String): Pair<List<com.pianoquest.app.data.PedalChange>, Double> {
            val song = SongLibrary.get(id)!!
            val parsed = MidiFileReader.parse(File(dir, "${song.midiResource}.mid").readBytes())
            val (events, bars) = MidiFileReader.toEvents(parsed)
            assertTrue(com.pianoquest.app.data.AutoPedal.wanted(song), "$id should get automatic pedal")
            val pedal = com.pianoquest.app.data.AutoPedal.generate(events, parsed.meterNum * 4.0 / parsed.meterDen, bars,
                com.pianoquest.app.data.AutoPedal.liftForRuns(song))
            val length = events.maxOf { it.end }
            val down = pedal.zipWithNext().filter { (a, _) -> a.down }.sumOf { (a, b) -> b.beat - a.beat }
            println("$id: ${pedal.count { it.down }} pedal presses, pedal down ${(100 * down / length).toInt()}% of the piece; first presses at beats " + pedal.filter { it.down }.take(14).joinToString { ((it.beat * 4).toInt() / 4.0).toString() })
            return pedal to down / length
        }
        val (turkish, share) = plan("turkish-march")
        assertTrue(turkish.count { it.down } >= 10, "Turkish March needs pedal in its chordal sections")
        assertTrue(share in 0.05..0.85, "Turkish March pedal share ${share}")
        // The opening theme is a fast turn (B A G# A C): no pedal there.
        assertTrue(turkish.none { it.down && it.beat < 3.5 }, "no pedal on the opening turn: ${turkish.take(4)}")
        for (id in listOf("fur-elise", "sonata-facile", "mozart-k331-menuetto")) {
            val (p, s) = plan(id)
            for (i in 1 until p.size) assertTrue(p[i - 1].down != p[i].down && p[i].beat >= p[i - 1].beat, "$id pedal order at $i")
            assertTrue(s < 0.9, "$id pedal down almost all the time")
        }
    }

    @Test
    fun famousOpeningsAreExact() {
        fun firstNotes(id: String, n: Int): List<Int> {
            val p = MidiFileReader.parse(File(dir, "$id.mid").readBytes())
            return p.notes.filter { it.midi >= 60 }.take(n).map { it.midi }
        }
        // Für Elise: E5 D#5 E5 D#5 E5 B4 D5 C5 A4
        assertTrue(firstNotes("fur-elise", 9) == listOf(76, 75, 76, 75, 76, 71, 74, 72, 69), "Für Elise opening: ${firstNotes("fur-elise", 9)}")
        // Rondo alla Turca: B4 A4 G#4 A4 C5
        assertTrue(firstNotes("turkish-march", 5) == listOf(71, 69, 68, 69, 72), "Turkish March opening: ${firstNotes("turkish-march", 5)}")
    }
}
