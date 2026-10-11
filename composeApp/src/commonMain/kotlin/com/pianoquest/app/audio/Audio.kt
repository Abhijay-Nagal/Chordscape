package com.pianoquest.app.audio

import com.pianoquest.app.platform.createAudioOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Process-wide audio: owns the platform output and the synth engine. */
object Audio {
    private var output: AudioOutput? = null
    private var engineRef: SynthEngine? = null
    private var running = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var warmJob: Job? = null

    val engine: SynthEngine
        get() = engineRef ?: error("Audio not started")

    val isReady get() = engineRef != null

    fun start() {
        if (output == null) {
            val o = runCatching { createAudioOutput() }.getOrNull() ?: return
            output = o
            engineRef = SynthEngine(o.sampleRate).also { e ->
                runCatching {
                    val st = com.pianoquest.app.data.ProgressStore.current.settings
                    e.masterVolume = st.volume
                    e.reverbAmount = st.reverb
                    e.setInstrument(Instrument.valueOf(st.instrument))
                }
            }
            prewarm()
        }
        if (!running) {
            runCatching { output?.start(engine) }
            running = true
        }
    }

    fun pause() {
        if (running) {
            engineRef?.allNotesOff()
            runCatching { output?.stop() }
            running = false
        }
    }

    fun setInstrument(instrument: Instrument) {
        val e = engineRef ?: return
        e.setInstrument(instrument)
        prewarm()
    }

    /** Loads the recorded grand piano and renders synthesized notes in the background so the first key press is instant. */
    private fun prewarm() {
        scope.launch { runCatching { PianoSamples.load() } }
        val e = engineRef ?: return
        if (e.instrument.sustained || e.isSampledPiano) return
        warmJob?.cancel()
        warmJob = scope.launch {
            val order = (48..84) + (36..47).reversed() + (85..96) + (21..35).reversed() + (97..108)
            for (midi in order) {
                if (!isActive) break
                if (!e.isCached(midi)) e.sampleFor(midi)
            }
        }
    }

    fun noteOn(midi: Int, velocity: Int = 96) { engineRef?.noteOn(midi, velocity) }
    fun setFx(fx: SoundFx) { engineRef?.fx = fx }
    fun setPitchBend(semitones: Float) { engineRef?.pitchBend = semitones }
    fun noteOff(midi: Int) { engineRef?.noteOff(midi) }
    fun sustain(on: Boolean) { engineRef?.setSustain(on) }
    fun click(accent: Boolean) { engineRef?.click(accent) }
    fun allOff() { engineRef?.allNotesOff() }
}
