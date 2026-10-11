package com.pianoquest.app.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tiny feedback jingles played straight through the synth (they don't light up keys). */
object Sfx {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun jingle(notes: List<Int>, stepMs: Long, holdMs: Long, velocity: Int) {
        scope.launch {
            for (n in notes) { Audio.noteOn(n, velocity); delay(stepMs) }
            delay(holdMs)
            notes.forEach { Audio.noteOff(it) }
        }
    }

    fun correct() = jingle(listOf(84, 88, 91), 55, 260, 70)
    fun wrong() = jingle(listOf(52, 51), 90, 200, 55)
    fun fanfare() = jingle(listOf(72, 76, 79, 84, 88, 91, 96), 75, 700, 80)
}
