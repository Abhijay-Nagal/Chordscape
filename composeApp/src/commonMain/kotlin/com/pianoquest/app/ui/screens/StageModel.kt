package com.pianoquest.app.ui.screens

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pianoquest.app.data.Hand
import com.pianoquest.app.data.NoteEvent
import com.pianoquest.app.data.SongData

enum class PlayMode(val label: String, val help: String) {
    LISTEN("Listen", "Watch and listen to the whole piece"),
    PRACTICE("Note Wait", "The music waits until you play the right notes"),
    PERFORM("Practice", "Play along in real time - scored on timing"),
}

enum class HandChoice(val label: String) { RIGHT("Right"), LEFT("Left"), BOTH("Both") }

/**
 * The fast-changing playback state. Only the views that draw it (falling notes, seek bar, status line, keyboard
 * highlights) read these values, so the rest of the screen does not redraw on every frame.
 */
@Stable
internal class Playback {
    var time by mutableDoubleStateOf(-4.0)
    var frame by mutableIntStateOf(0)
    var playing by mutableStateOf(false)
    var waiting by mutableStateOf(false)
    var finished by mutableStateOf(false)
    var loopA by mutableStateOf<Double?>(null)
    var loopB by mutableStateOf<Double?>(null)
    /** Increases on every jump (seek, loop restart) so the playback loop can re-sync its note and click positions. */
    var seeks = 0

    /** True when [start] lies outside the active A-B loop (such notes are hidden while looping). */
    fun outsideLoop(start: Double): Boolean {
        val a = loopA ?: return false
        if (start < a - 1e-6) return true
        val b = loopB ?: return false
        return start >= b - 1e-6
    }
}

/** Mutable state of one play-through; all times are in beats of the piece's opening tempo. */
internal class SongRun(val data: SongData, val mode: PlayMode, val hands: HandChoice, meterNum: Int, meterDen: Int) {
    val events: List<NoteEvent> = data.events
    val isUser = BooleanArray(events.size) { i ->
        mode != PlayMode.LISTEN && when (hands) {
            HandChoice.BOTH -> true
            HandChoice.RIGHT -> events[i].hand == Hand.RIGHT || !data.hasLeftHand
            HandChoice.LEFT -> events[i].hand == Hand.LEFT || !data.hasRightHand
        }
    }
    val started = BooleanArray(events.size)
    val ended = BooleanArray(events.size)
    val hit = BooleanArray(events.size)
    val missed = BooleanArray(events.size)

    /**
     * The player's chords. Notes of one chord in a score file often start a few milliseconds apart; anything starting
     * within [CHORD_WINDOW] beats belongs to the same chord, so each one counts when pressed together.
     */
    val userGroups: List<List<Int>> = run {
        val idx = events.indices.filter { isUser[it] }.sortedBy { events[it].start }
        val out = ArrayList<List<Int>>()
        var cur = ArrayList<Int>()
        var first = 0.0
        for (i in idx) {
            if (cur.isNotEmpty() && events[i].start - first > CHORD_WINDOW) { out += cur; cur = ArrayList() }
            if (cur.isEmpty()) first = events[i].start
            cur += i
        }
        if (cur.isNotEmpty()) out += cur
        out
    }
    fun groupStart(g: List<Int>) = events[g.first()].start
    var groupPtr = 0

    // ---- Per-hand results (index 0 = right hand, 1 = left hand)
    val handPerfect = IntArray(2)
    val handGood = IntArray(2)
    val handWrong = IntArray(2)
    /** Listen mode: notes that went by while their hand was shown - the ones the player could have played along with. */
    val counted = BooleanArray(events.size)
    /** Listen mode: which hands' falling notes are shown (and scored). */
    var shown: HandChoice by mutableStateOf(HandChoice.BOTH)
    /** Number of [counted] notes (kept up to date by the playback loop). */
    var countedTotal = 0

    fun shows(h: Hand) = when (shown) { HandChoice.BOTH -> true; HandChoice.RIGHT -> h == Hand.RIGHT; HandChoice.LEFT -> h == Hand.LEFT }

    /** Whether note [i] is one the player is meant to play (and is scored on) right now. */
    fun playable(i: Int) = if (mode == PlayMode.LISTEN) shows(events[i].hand) else isUser[i]

    fun handTotal(h: Int): Int = events.indices.count { i ->
        (if (h == 0) events[i].hand == Hand.RIGHT else events[i].hand == Hand.LEFT) && (if (mode == PlayMode.LISTEN) counted[i] else isUser[i])
    }

    /** Accuracy for one hand in percent, or null when that hand was not played at all. */
    fun handScore(h: Int): Int? {
        val total = handTotal(h)
        return when (mode) {
            PlayMode.PRACTICE -> if (total == 0) null else (100 * handPerfect[h] / (handPerfect[h] + handWrong[h]).coerceAtLeast(1)).coerceAtMost(100)
            else -> if (total == 0) null else ((handPerfect[h] + handGood[h] * 0.7) * 100 / (total + handWrong[h] * 0.5)).toInt().coerceIn(0, 100)
        }
    }

    /** The hand a wrong key most likely belongs to: the nearest expected note in pitch, else by side of middle C. */
    fun handForWrong(midi: Int, candidates: List<Int>): Int {
        val near = candidates.minByOrNull { kotlin.math.abs(events[it].midi - midi) }
        if (near != null) return if (events[near].hand == Hand.RIGHT) 0 else 1
        return when {
            mode == PlayMode.LISTEN && shown == HandChoice.RIGHT -> 0
            mode == PlayMode.LISTEN && shown == HandChoice.LEFT -> 1
            mode != PlayMode.LISTEN && hands == HandChoice.RIGHT -> 0
            mode != PlayMode.LISTEN && hands == HandChoice.LEFT -> 1
            else -> if (midi >= 60) 0 else 1
        }
    }

    var wrong = 0
    var perfect = 0
    var good = 0
    var combo = 0
    var bestCombo = 0
    val userTotal = isUser.count { it }
    val length = data.length

    /** Metronome clicks: (beat position, accent) for every beat of every bar, following the real bar lines. */
    val clicks: List<Pair<Double, Boolean>> = run {
        val perBar = (if (meterDen == 8 && meterNum % 3 == 0 && meterNum > 3) meterNum / 3 else meterNum).coerceIn(1, 12)
        val bars = data.barLines.ifEmpty { listOf(0.0) }
        val out = ArrayList<Pair<Double, Boolean>>()
        // Count-in before the music starts.
        val step0 = data.barBeats / perBar
        var t = -data.barBeats.coerceAtLeast(2.0)
        var k = 0
        while (t < bars.first() - 1e-6) { out += t to (k % perBar == 0); t += step0; k++ }
        for (b in bars.indices) {
            val s = bars[b]
            val len = (bars.getOrNull(b + 1) ?: (s + data.barBeats)) - s
            val step = len / perBar
            for (j in 0 until perBar) out += (s + j * step) to (j == 0)
        }
        out
    }

    /** Length in beats of the metronome beat around [t] (follows changing meters). */
    fun clickLength(t: Double): Double {
        val i = clicks.indexOfFirst { it.first > t }.let { if (it <= 0) 1 else it }
        return (clicks.getOrNull(i)?.first ?: (t + 1)) - (clicks.getOrNull(i - 1)?.first ?: t)
    }

    /** Jumps to beat [t]: everything before it counts as already played (neither hit nor missed). */
    fun seek(t: Double) {
        for (i in events.indices) {
            val before = events[i].start < t - 1e-6
            started[i] = before
            ended[i] = before
            if (before) { hit[i] = false; missed[i] = false; counted[i] = false }
        }
        countedTotal = counted.count { it }
        groupPtr = userGroups.indexOfFirst { groupStart(it) >= t - 1e-6 }.let { if (it < 0) userGroups.size else it }
    }

    companion object {
        const val CHORD_WINDOW = 0.08
    }
}
