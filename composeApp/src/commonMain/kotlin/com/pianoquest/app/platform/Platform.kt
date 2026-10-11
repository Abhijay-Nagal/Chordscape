package com.pianoquest.app.platform

import androidx.compose.runtime.Composable
import com.pianoquest.app.audio.AudioOutput
import com.pianoquest.app.input.MidiService

/** A simple mutual-exclusion lock (both targets are JVM based, so this maps to ReentrantLock). */
expect class PlatformLock() {
    fun lock()
    fun unlock()
}

inline fun <T> PlatformLock.withLock(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}

expect fun createAudioOutput(): AudioOutput

expect fun createMidiService(): MidiService

/** Tiny persistent text-file storage in the app's private data directory. */
expect object Storage {
    fun read(name: String): String?
    fun write(name: String, text: String)
    fun readBytes(name: String): ByteArray?
    fun writeBytes(name: String, bytes: ByteArray)
    fun delete(name: String)
}

expect fun currentEpochDay(): Long

expect fun nowMillis(): Long

/** Wall-clock time in milliseconds (comparable with MIDI activity timestamps). */
expect fun nowWallMillis(): Long

expect val isDesktop: Boolean

expect val platformName: String

/** Handles the system back gesture/button on Android. No-op on desktop (Esc is handled by the app). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Forces landscape (true, used by the piano stage) or returns to the app's orientation policy (false). */
expect fun requestLandscape(landscape: Boolean)

/** App-wide orientation policy: landscape only (default) or landscape and portrait. */
expect fun setAllowPortrait(allow: Boolean)

/** Hides (true) or restores (false) the system status and navigation bars - used on the piano screens. */
expect fun setImmersive(immersive: Boolean)

/** Lets the user choose a .mid file. The callback receives the file name and its bytes (on the main thread). */
expect fun pickMidiFile(onPicked: (name: String, bytes: ByteArray) -> Unit)

/** True when the platform can open a file picker. */
expect val canImportFiles: Boolean

/**
 * The interface scale the platform already applies for this screen size, and whether the device is a tablet;
 * null when the app should scale itself (desktop windows, screenshot tests).
 */
expect fun platformAutoScale(): Pair<Float, com.pianoquest.app.ui.FormFactor>?

/** Keeps the display awake while the app is open (on by default, so it never dims during practice). */
expect fun setKeepScreenOn(on: Boolean)

/** A short, light vibration for key presses (no-op where unsupported). */
expect fun hapticTick()
