package com.pianoquest.app

import com.pianoquest.app.data.Hand
import com.pianoquest.app.data.Notation
import com.pianoquest.app.data.SongLibrary
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SongsTest {
    @Test
    fun notationBasics() {
        val ev = Notation.parse("C4:q D4 E4:h | R:1 C4+E4+G4:w")
        assertEquals(6, ev.size)
        assertEquals(5.0, ev[3].start)
        assertEquals(4.0, ev[5].duration)
        assertEquals(9.0, Notation.length("C4:q D4 E4:h | R:1 C4+E4+G4:w"))
        assertEquals(1.5, Notation.parseDuration("q."))
        assertEquals(8, Notation.parse("{ C4:e D4 }*4").size)
    }

    @Test
    fun allSongsParseAndHaveNotes() {
        val ids = HashSet<String>()
        for (song in SongLibrary.all) {
            assertTrue(ids.add(song.id), "duplicate id ${song.id}")
            if (song.isMidi) continue // bundled scores are checked in MidiScoresTest
            val events = try { song.events } catch (e: Exception) { fail("${song.id}: ${e.message}") }
            assertTrue(events.isNotEmpty(), "${song.id} has no notes")
            assertTrue(events.all { it.midi in 21..108 }, "${song.id} has notes outside the piano")
        }
        assertTrue(SongLibrary.all.size >= 100, "expected a big library, got ${SongLibrary.all.size}")
    }

    /** Every bar written with '|' must add up to the time signature (except pickup and final bar). */
    @Test
    fun barsAddUp() {
        val problems = mutableListOf<String>()
        for (song in SongLibrary.all) {
            val sources = buildList {
                if (song.rh.isNotBlank()) add("rh" to song.rh)
                song.lh?.let { add("lh" to it) }
            }
            for ((name, text) in sources) {
                val bars = Notation.expandRepeats(text).split("|").map { it.trim() }.filter { it.isNotEmpty() }
                var sticky = 1.0
                bars.forEachIndexed { i, bar ->
                    var len = 0.0
                    for (tok in bar.split(Regex("\\s+")).filter { it.isNotEmpty() }) {
                        val c = tok.indexOf(':')
                        if (c >= 0) sticky = Notation.parseDuration(tok.substring(c + 1))
                        len += sticky
                    }
                    val isPickup = i == 0 && song.pickup > 0 && abs(len - song.pickup) < 1e-6
                    val isLast = i == bars.lastIndex
                    val ok = abs(len - song.barBeats) < 1e-6 || abs(len % song.barBeats) < 1e-6
                    if (!ok && !isPickup && !isLast) problems += "${song.id}.$name bar ${i + 1}: $len beats (expected ${song.barBeats}) -> '$bar'"
                }
            }
        }
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }

    @Test
    fun handsAreAssigned() {
        val elise = SongLibrary.get("twinkle")!!
        assertTrue(elise.events.any { it.hand == Hand.LEFT })
        assertTrue(elise.events.any { it.hand == Hand.RIGHT })
    }
}
