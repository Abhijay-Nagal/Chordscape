package com.pianoquest.app.input

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Plays the piano from a computer keyboard (desktop, Chromebooks, tablets with keyboards).
 * Bottom row A S D F G H J K L ; = white keys, W E T Y U O P = black keys. Z / X shift octave. Space = sustain.
 */
object ComputerKeyboard {
    val baseOctave = MutableStateFlow(4)

    private val map: Map<Key, Int> = mapOf(
        Key.A to 0, Key.W to 1, Key.S to 2, Key.E to 3, Key.D to 4, Key.F to 5, Key.T to 6, Key.G to 7,
        Key.Y to 8, Key.H to 9, Key.U to 10, Key.J to 11, Key.K to 12, Key.O to 13, Key.L to 14, Key.P to 15,
        Key.Semicolon to 16, Key.Apostrophe to 17,
    )
    private val down = HashMap<Key, Int>()

    /** Returns true if the event was consumed. */
    fun handle(event: KeyEvent): Boolean {
        val k = event.key
        if (k == Key.Spacebar) {
            when (event.type) {
                KeyEventType.KeyDown -> if (!PianoInput.sustain.value) PianoInput.setSustain(true)
                KeyEventType.KeyUp -> PianoInput.setSustain(false)
            }
            return true
        }
        if (event.type == KeyEventType.KeyDown && (k == Key.Z || k == Key.X)) {
            baseOctave.value = (baseOctave.value + if (k == Key.Z) -1 else 1).coerceIn(1, 7)
            return true
        }
        val offset = map[k] ?: return false
        when (event.type) {
            KeyEventType.KeyDown -> if (k !in down) {
                val midi = (baseOctave.value + 1) * 12 + offset
                down[k] = midi
                PianoInput.noteOn(midi, 96, InputSource.COMPUTER_KEYBOARD)
            }
            KeyEventType.KeyUp -> down.remove(k)?.let { PianoInput.noteOff(it, InputSource.COMPUTER_KEYBOARD) }
        }
        return true
    }
}
