package com.pianoquest.app.audio

import com.pianoquest.app.platform.PlatformLock
import com.pianoquest.app.platform.withLock
import com.pianoquest.app.theory.midiToFrequency
import kotlin.concurrent.Volatile
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/** Pulls rendered audio from the engine; implemented per platform (AudioTrack / Java Sound). */
fun interface AudioRenderer {
    fun render(buffer: FloatArray, frames: Int)
}

interface AudioOutput {
    val sampleRate: Int
    fun start(renderer: AudioRenderer)
    fun stop()
}

/** Sound-engineering settings applied to everything the engine plays. */
data class SoundFx(
    val transpose: Int = 0,
    val tuneCents: Float = 0f,
    val bassDb: Float = 0f,
    val trebleDb: Float = 0f,
    val drive: Float = 0f,
    val chorus: Float = 0f,
    val tremoloDepth: Float = 0f,
    val tremoloRate: Float = 5f,
    /** Multiplies every instrument's release time (how long notes ring after the key is let go). */
    val releaseScale: Float = 1f,
    /** How long notes ring while the sustain pedal is held: 1 = naturally, lower = damped sooner. */
    val sustainLevel: Float = 0.5f,
    /** Reverb room size 0 (small room) .. 1 (cathedral). */
    val roomSize: Float = 0.5f,
)

/**
 * Polyphonic synthesizer. UI / MIDI threads post commands; the audio thread drains them and renders.
 */
class SynthEngine(val sampleRate: Int) : AudioRenderer {

    private sealed interface Cmd {
        data class On(val key: Int, val midi: Int, val velocity: Int, val sample: FloatArray?, val hit: PianoHit? = null,
                      val detune: Double = 1.0, val gainMul: Float = 1f) : Cmd
        data class Off(val key: Int) : Cmd
        data class Sustain(val on: Boolean) : Cmd
        data class Click(val accent: Boolean) : Cmd
        data object AllOff : Cmd
    }

    private val lock = PlatformLock()
    private var pending = ArrayList<Cmd>()
    private var draining = ArrayList<Cmd>()

    private val sampleLock = PlatformLock()
    private val samples = HashMap<Int, FloatArray>()

    @Volatile var instrument: Instrument = Instrument.GRAND_PIANO
        private set
    @Volatile var masterVolume: Float = 0.8f
    @Volatile var reverbAmount: Float = 0.18f
    @Volatile var fx: SoundFx = SoundFx()
    /** Pitch-bend wheel in semitones (-2..2); springs back to 0 in the UI. */
    @Volatile var pitchBend: Float = 0f

    private var wavetable: FloatArray = SampleBank.wavetable(Instrument.ORGAN)
    private var wavetableRatio = 1.0

    private val voices = Array(MAX_VOICES) { Voice() }
    private var sustainOn = false
    private var voiceCounter = 0L

    private val clickAccent = SampleBank.click(sampleRate, true)
    private val clickNormal = SampleBank.click(sampleRate, false)
    private var clickData: FloatArray? = null
    private var clickPos = 0

    private val reverb = Reverb(sampleRate)

    // Effect state
    private val chorusBuf = FloatArray((0.05 * sampleRate).toInt())
    private var chorusIdx = 0
    private var lfoPhase = 0.0
    private var tremPhase = 0.0
    private var lowLp = 0f
    private var highLp = 0f

    private class Voice {
        var active = false
        /** The key that was pressed (what noteOff refers to), and the pitch that sounds (after transpose). */
        var key = -1
        var midi = -1
        var sample: FloatArray? = null
        var spos = 0.0
        var pcm: ShortArray? = null
        var fpos = 0.0
        var rate = 1.0
        var gain = 0f
        var held = false
        var sustained = false
        var releasing = false
        var age = 0L
        var phase = DoubleArray(3)
        var inc = 0.0
        var env = 0.0
        var attackStep = 0.0
        var t = 0
    }

    fun setInstrument(newInstrument: Instrument) {
        if (newInstrument == instrument) return
        allNotesOff()
        sampleLock.withLock { samples.clear() }
        if (newInstrument.sustained) {
            wavetable = SampleBank.wavetable(newInstrument)
            wavetableRatio = SampleBank.wavetableRatio(newInstrument)
        }
        instrument = newInstrument
    }

    private val usesPianoSamples get() = instrument == Instrument.GRAND_PIANO || instrument == Instrument.BRIGHT_PIANO ||
        instrument == Instrument.HONKY_TONK

    /** True when the instrument plays the recorded grand-piano samples (no rendering needed). */
    val isSampledPiano: Boolean get() = usesPianoSamples

    /** Returns (rendering on demand if needed) the cached buffer for a note of the current instrument. */
    fun sampleFor(midi: Int): FloatArray? {
        val inst = instrument
        if (inst.sustained) return null
        sampleLock.withLock { samples[midi] }?.let { return it }
        val data = SampleBank.render(inst, midi, sampleRate)
        sampleLock.withLock { if (instrument == inst) samples[midi] = data }
        return data
    }

    fun isCached(midi: Int) = sampleLock.withLock { samples.containsKey(midi) }

    fun noteOn(key: Int, velocity: Int = 96) {
        val midi = key + fx.transpose
        if (midi !in 21..108) return
        val vel = velocity.coerceIn(1, 127)
        if (usesPianoSamples) {
            val hit = PianoSamples.pick(midi, vel)
            if (hit != null) {
                if (instrument == Instrument.HONKY_TONK) {
                    post(Cmd.On(key, midi, vel, null, hit, 1.0045, 0.62f))
                    post(Cmd.On(key, midi, vel, null, hit, 0.9955, 0.62f))
                } else post(Cmd.On(key, midi, vel, null, hit))
                return
            }
        }
        post(Cmd.On(key, midi, vel, sampleFor(midi)))
    }

    fun noteOff(key: Int) = post(Cmd.Off(key))
    fun setSustain(on: Boolean) = post(Cmd.Sustain(on))
    fun click(accent: Boolean) = post(Cmd.Click(accent))
    fun allNotesOff() = post(Cmd.AllOff)

    private fun post(cmd: Cmd) = lock.withLock { pending.add(cmd) }

    override fun render(buffer: FloatArray, frames: Int) {
        lock.withLock {
            val tmp = pending; pending = draining; draining = tmp
        }
        for (cmd in draining) handle(cmd)
        draining.clear()

        for (i in 0 until frames) buffer[i] = 0f
        val inst = instrument
        val f = fx
        val releaseMul = exp(-1.0 / (inst.releaseSeconds * f.releaseScale.coerceIn(0.1f, 6f) * sampleRate)).toFloat()
        // Damping of notes that ring only because the pedal is down.
        val sustainMul = if (f.sustainLevel >= 0.999f) 1f
            else exp(-1.0 / ((0.35 + 8.0 * f.sustainLevel * f.sustainLevel) * sampleRate)).toFloat()
        val pitch = 2.0.pow((f.tuneCents / 100.0 + pitchBend) / 12.0)
        for (v in voices) {
            if (!v.active) continue
            when {
                v.pcm != null -> renderPcmVoice(v, buffer, frames, releaseMul, sustainMul, pitch)
                v.sample != null -> renderSampleVoice(v, buffer, frames, releaseMul, sustainMul, pitch)
                else -> renderOscVoice(v, buffer, frames, inst, releaseMul, pitch)
            }
        }
        clickData?.let { c ->
            var i = 0
            while (i < frames && clickPos < c.size) { buffer[i] += c[clickPos++]; i++ }
            if (clickPos >= c.size) clickData = null
        }
        applyEffects(buffer, frames, f, inst)
    }

    private fun applyEffects(buffer: FloatArray, frames: Int, f: SoundFx, inst: Instrument) {
        val vol = masterVolume
        val wet = reverbAmount
        reverb.roomSize = f.roomSize
        val bassG = 10f.pow(f.bassDb / 20f) - 1f
        val trebleG = 10f.pow((f.trebleDb + inst.trebleBoost) / 20f) - 1f
        val aLow = (1 - exp(-2 * PI * 250.0 / sampleRate)).toFloat()
        val aHigh = (1 - exp(-2 * PI * 3500.0 / sampleRate)).toFloat()
        val drive = f.drive
        val driveGain = 1f + drive * 14f
        val driveNorm = tanh(driveGain * 0.5f).let { if (it < 1e-3f) 1f else it } * 2f
        val chorus = f.chorus
        val trem = f.tremoloDepth
        val tremInc = 2 * PI * f.tremoloRate / sampleRate
        val lfoInc = 2 * PI * 0.7 / sampleRate
        val cb = chorusBuf
        val n = cb.size
        val eqOn = bassG != 0f || trebleG != 0f
        for (i in 0 until frames) {
            var x = buffer[i]
            if (trem > 0f) {
                x *= 1f - trem * 0.5f * (1f + sin(tremPhase).toFloat())
                tremPhase += tremInc
                if (tremPhase > 2 * PI) tremPhase -= 2 * PI
            }
            if (drive > 0f) x = x * (1 - drive) + tanh(x * driveGain) / driveNorm * drive * 1.6f
            if (chorus > 0f) {
                cb[chorusIdx] = x
                val s = sin(lfoPhase)
                val d1 = (0.012 + 0.005 * s) * sampleRate
                val d2 = (0.017 - 0.006 * s) * sampleRate
                x = x * (1 - chorus * 0.35f) + chorus * 0.45f * (tap(cb, n, d1) + tap(cb, n, d2))
                if (++chorusIdx >= n) chorusIdx = 0
                lfoPhase += lfoInc
                if (lfoPhase > 2 * PI) lfoPhase -= 2 * PI
            }
            if (eqOn) {
                lowLp += aLow * (x - lowLp)
                highLp += aHigh * (x - highLp)
                x += bassG * lowLp + trebleG * (x - highLp)
            }
            val r = reverb.process(x)
            val y = (x + r * wet) * vol
            // Soft limiter: near-linear for small signals, smooth saturation for loud chords.
            buffer[i] = y * (27f + y * y) / (27f + 9f * y * y)
        }
    }

    private fun tap(buf: FloatArray, n: Int, delay: Double): Float {
        var p = chorusIdx - delay
        while (p < 0) p += n
        val i0 = p.toInt()
        val frac = (p - i0).toFloat()
        val a = buf[i0 % n]
        val b = buf[(i0 + 1) % n]
        return a + (b - a) * frac
    }

    private fun renderPcmVoice(v: Voice, buffer: FloatArray, frames: Int, releaseMul: Float, sustainMul: Float, pitch: Double) {
        val data = v.pcm ?: return
        var pos = v.fpos
        var g = v.gain
        val rate = v.rate * pitch
        val last = data.size - 1
        val scale = 1f / 32768f
        val damp = if (v.sustained && !v.releasing) sustainMul else 1f
        for (i in 0 until frames) {
            val idx = pos.toInt()
            if (idx >= last) { v.active = false; break }
            val frac = (pos - idx).toFloat()
            val a = data[idx].toFloat()
            val b = data[idx + 1].toFloat()
            buffer[i] += (a + (b - a) * frac) * scale * g
            pos += rate
            if (v.releasing) {
                g *= releaseMul
                if (g < 0.0005f) { v.active = false; break }
            } else if (damp < 1f) {
                g *= damp
                if (g < 0.0005f) { v.active = false; break }
            }
        }
        v.fpos = pos
        v.gain = g
    }

    private fun renderSampleVoice(v: Voice, buffer: FloatArray, frames: Int, releaseMul: Float, sustainMul: Float, pitch: Double) {
        val data = v.sample ?: return
        var pos = v.spos
        var g = v.gain
        val last = data.size - 1
        val damp = if (v.sustained && !v.releasing) sustainMul else 1f
        for (i in 0 until frames) {
            val idx = pos.toInt()
            if (idx >= last) { v.active = false; break }
            val frac = (pos - idx).toFloat()
            buffer[i] += (data[idx] + (data[idx + 1] - data[idx]) * frac) * g
            pos += pitch
            if (v.releasing) {
                g *= releaseMul
                if (g < 0.0005f) { v.active = false; break }
            } else if (damp < 1f) {
                g *= damp
                if (g < 0.0005f) { v.active = false; break }
            }
        }
        v.spos = pos
        v.gain = g
    }

    private fun renderOscVoice(v: Voice, buffer: FloatArray, frames: Int, inst: Instrument, releaseMul: Float, pitch: Double) {
        val table = wavetable
        val size = table.size
        val detunes = inst.detunes
        val voicesN = detunes.size
        val norm = 0.22 / kotlin.math.sqrt(voicesN.toDouble())
        val vibDepth = inst.vibratoDepth
        val vibW = 2 * PI * inst.vibratoRate / sampleRate
        val vibDelay = sampleRate / 4
        for (i in 0 until frames) {
            if (v.releasing) {
                v.env *= releaseMul
                if (v.env < 0.0005) { v.active = false; return }
            } else if (v.env < 1.0) {
                v.env = (v.env + v.attackStep).coerceAtMost(1.0)
            }
            val vib = if (vibDepth > 0 && v.t > vibDelay) 1.0 + vibDepth * sin(vibW * v.t) else 1.0
            var s = 0.0
            for (k in 0 until voicesN) {
                var ph = v.phase[k]
                val idx = ph.toInt()
                val frac = ph - idx
                val a = table[idx % size]
                val b = table[(idx + 1) % size]
                s += a + (b - a) * frac
                ph += v.inc * detunes[k] * vib * pitch
                if (ph >= size) ph -= size
                v.phase[k] = ph
            }
            buffer[i] += (s * norm * v.env * v.gain).toFloat()
            v.t++
        }
    }

    private fun handle(cmd: Cmd) {
        when (cmd) {
            is Cmd.On -> startVoice(cmd)
            is Cmd.Off -> for (v in voices) if (v.active && v.key == cmd.key && v.held) {
                v.held = false
                if (sustainOn) v.sustained = true else v.releasing = true
            }
            is Cmd.Sustain -> {
                sustainOn = cmd.on
                if (!cmd.on) for (v in voices) if (v.active && v.sustained && !v.held) { v.sustained = false; v.releasing = true }
            }
            is Cmd.Click -> { clickData = if (cmd.accent) clickAccent else clickNormal; clickPos = 0 }
            Cmd.AllOff -> for (v in voices) { v.active = false }
        }
    }

    private fun startVoice(cmd: Cmd.On) {
        // Re-striking a key: release the previous voice of the same key quickly (but not the twin honky-tonk voice).
        if (cmd.detune <= 1.0) for (v in voices) if (v.active && v.key == cmd.key && !v.releasing) { v.releasing = true; v.held = false }
        val v = voices.firstOrNull { !it.active }
            ?: voices.filter { it.releasing }.minByOrNull { it.age }
            ?: voices.minBy { it.age }
        val vel = cmd.velocity / 127.0
        v.active = true
        v.key = cmd.key
        v.midi = cmd.midi
        v.held = true
        v.sustained = false
        v.releasing = false
        v.age = voiceCounter++
        v.gain = ((0.18 + 0.82 * vel * vel) * cmd.gainMul).toFloat()
        v.sample = cmd.sample
        v.spos = 0.0
        v.pcm = cmd.hit?.pcm
        if (cmd.hit != null) {
            v.fpos = 0.0
            v.rate = cmd.hit.pitchRatio * PianoSamples.RATE / sampleRate * cmd.detune
            v.gain = cmd.hit.gain * cmd.gainMul
        }
        if (cmd.sample == null && cmd.hit == null) {
            val freq = midiToFrequency(cmd.midi.toDouble()) * wavetableRatio
            v.inc = freq * wavetable.size / sampleRate
            v.env = 0.0
            v.attackStep = 1.0 / (instrument.attack * sampleRate)
            v.t = 0
            for (k in v.phase.indices) v.phase[k] = (k * 517.0) % wavetable.size
        }
    }

    companion object {
        const val MAX_VOICES = 64
    }
}

/** Small Schroeder/Freeverb-style mono reverb with an adjustable room size. */
class Reverb(sampleRate: Int) {
    private val scale = sampleRate / 44100.0
    private val combs = intArrayOf(1116, 1188, 1277, 1356).map { Comb((it * scale).toInt()) }
    private val allpasses = intArrayOf(556, 441).map { AllPass((it * scale).toInt()) }
    var roomSize: Float = 0.5f

    fun process(input: Float): Float {
        val x = input * 0.25f
        val fb = 0.70f + roomSize.coerceIn(0f, 1f) * 0.24f
        var out = 0f
        for (c in combs) out += c.process(x, fb)
        for (a in allpasses) out = a.process(out)
        return out
    }

    private class Comb(size: Int) {
        private val buf = FloatArray(size)
        private var idx = 0
        private var store = 0f
        fun process(x: Float, feedback: Float): Float {
            val y = buf[idx]
            store = y * 0.7f + store * 0.3f
            buf[idx] = x + store * feedback
            if (++idx >= buf.size) idx = 0
            return y
        }
    }

    private class AllPass(size: Int) {
        private val buf = FloatArray(size)
        private var idx = 0
        fun process(x: Float): Float {
            val b = buf[idx]
            val y = -x + b
            buf[idx] = x + b * 0.5f
            if (++idx >= buf.size) idx = 0
            return y
        }
    }
}
