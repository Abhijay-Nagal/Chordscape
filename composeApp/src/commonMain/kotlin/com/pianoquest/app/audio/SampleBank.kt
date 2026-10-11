package com.pianoquest.app.audio

import com.pianoquest.app.theory.midiToFrequency
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class InstrumentFamily(val label: String) {
    PIANO("Pianos & keys"), MALLETS("Bells & mallets"), PLUCKED("Plucked strings"), ORGAN("Organs"),
    ENSEMBLE("Strings, voices & winds"), SYNTH("Synths"),
}

/**
 * Every playable sound. Decaying sounds (pianos, mallets, plucked strings) are pre-rendered per key; sustained sounds
 * (organs, strings, voices, synths) are played from a single-cycle wavetable with an envelope.
 */
enum class Instrument(
    val displayName: String,
    val family: InstrumentFamily,
    val sustained: Boolean,
    val releaseSeconds: Double,
    /** Sustained sounds only: attack time, chorus voices (detune ratios), vibrato depth (fraction) and rate (Hz). */
    val attack: Double = 0.01,
    val detunes: DoubleArray = doubleArrayOf(1.0),
    val vibratoDepth: Double = 0.0,
    val vibratoRate: Double = 5.0,
    /** Extra treble (dB) applied when this sound is selected. */
    val trebleBoost: Float = 0f,
) {
    GRAND_PIANO("Grand Piano", InstrumentFamily.PIANO, false, 0.22),
    BRIGHT_PIANO("Bright Piano", InstrumentFamily.PIANO, false, 0.22, trebleBoost = 5f),
    HONKY_TONK("Honky-Tonk Piano", InstrumentFamily.PIANO, false, 0.22),
    ELECTRIC_PIANO("Electric Piano", InstrumentFamily.PIANO, false, 0.18),
    FM_PIANO("Digital E-Piano", InstrumentFamily.PIANO, false, 0.2),
    CLAVINET("Clavinet", InstrumentFamily.PIANO, false, 0.06),
    HARPSICHORD("Harpsichord", InstrumentFamily.PLUCKED, false, 0.09),
    HARP("Concert Harp", InstrumentFamily.PLUCKED, false, 0.6),
    NYLON_GUITAR("Nylon Guitar", InstrumentFamily.PLUCKED, false, 0.25),
    STEEL_GUITAR("Steel Guitar", InstrumentFamily.PLUCKED, false, 0.25),
    PIZZICATO("Pizzicato Strings", InstrumentFamily.PLUCKED, false, 0.15),
    MUSIC_BOX("Music Box", InstrumentFamily.MALLETS, false, 1.2),
    CELESTA("Celesta", InstrumentFamily.MALLETS, false, 0.5),
    GLOCKENSPIEL("Glockenspiel", InstrumentFamily.MALLETS, false, 0.8),
    VIBRAPHONE("Vibraphone", InstrumentFamily.MALLETS, false, 0.5),
    MARIMBA("Marimba", InstrumentFamily.MALLETS, false, 0.15),
    XYLOPHONE("Xylophone", InstrumentFamily.MALLETS, false, 0.1),
    KALIMBA("Kalimba", InstrumentFamily.MALLETS, false, 0.25),
    TUBULAR_BELLS("Tubular Bells", InstrumentFamily.MALLETS, false, 1.5),
    STEEL_DRUM("Steel Drum", InstrumentFamily.MALLETS, false, 0.3),
    ORGAN("Jazz Organ", InstrumentFamily.ORGAN, true, 0.05, attack = 0.006, vibratoDepth = 0.0012, vibratoRate = 6.5),
    CHURCH_ORGAN("Church Organ", InstrumentFamily.ORGAN, true, 0.35, attack = 0.05, detunes = doubleArrayOf(0.9985, 1.0015)),
    ACCORDION("Accordion", InstrumentFamily.ORGAN, true, 0.08, attack = 0.03, detunes = doubleArrayOf(0.996, 1.0, 1.004)),
    STRINGS("String Ensemble", InstrumentFamily.ENSEMBLE, true, 0.45, attack = 0.22, detunes = doubleArrayOf(0.9968, 1.0, 1.0033), vibratoDepth = 0.0025, vibratoRate = 5.2),
    VIOLIN("Solo Violin", InstrumentFamily.ENSEMBLE, true, 0.25, attack = 0.09, vibratoDepth = 0.004, vibratoRate = 5.6),
    CHOIR("Choir Aahs", InstrumentFamily.ENSEMBLE, true, 0.5, attack = 0.25, detunes = doubleArrayOf(0.995, 0.9995, 1.004), vibratoDepth = 0.002, vibratoRate = 4.8),
    FLUTE("Flute", InstrumentFamily.ENSEMBLE, true, 0.12, attack = 0.06, vibratoDepth = 0.003, vibratoRate = 5.0),
    WARM_PAD("Warm Pad", InstrumentFamily.SYNTH, true, 1.2, attack = 0.6, detunes = doubleArrayOf(0.993, 1.0, 1.007)),
    SAW_LEAD("Saw Lead", InstrumentFamily.SYNTH, true, 0.12, attack = 0.005, detunes = doubleArrayOf(0.997, 1.003), vibratoDepth = 0.002, vibratoRate = 5.5),
    SQUARE_LEAD("Square Lead", InstrumentFamily.SYNTH, true, 0.1, attack = 0.005, vibratoDepth = 0.0015, vibratoRate = 5.5),
}

/**
 * Pre-renders decaying instrument notes into float buffers. Rendering a note once and replaying the buffer keeps
 * the real-time audio thread cheap.
 */
object SampleBank {

    fun durationSeconds(instrument: Instrument, midi: Int): Double = when (instrument) {
        Instrument.GRAND_PIANO, Instrument.BRIGHT_PIANO, Instrument.HONKY_TONK -> (5.5 - (midi - 21) * 0.05).coerceIn(1.2, 5.5)
        Instrument.ELECTRIC_PIANO, Instrument.FM_PIANO -> (4.0 - (midi - 21) * 0.035).coerceIn(1.0, 4.0)
        Instrument.HARPSICHORD -> (3.0 - (midi - 21) * 0.025).coerceIn(0.8, 3.0)
        Instrument.MUSIC_BOX, Instrument.CELESTA -> (3.0 - (midi - 21) * 0.02).coerceIn(1.0, 3.0)
        Instrument.HARP -> (4.5 - (midi - 21) * 0.04).coerceIn(1.0, 4.5)
        Instrument.NYLON_GUITAR, Instrument.STEEL_GUITAR -> (3.5 - (midi - 21) * 0.03).coerceIn(0.8, 3.5)
        Instrument.CLAVINET -> 1.6
        Instrument.PIZZICATO -> 0.9
        Instrument.GLOCKENSPIEL -> 2.5
        Instrument.VIBRAPHONE -> 3.5
        Instrument.MARIMBA -> 1.2
        Instrument.XYLOPHONE -> 0.7
        Instrument.KALIMBA -> 1.8
        Instrument.TUBULAR_BELLS -> 5.0
        Instrument.STEEL_DRUM -> 1.6
        else -> 1.0
    }

    fun render(instrument: Instrument, midi: Int, sampleRate: Int): FloatArray {
        val f0 = midiToFrequency(midi.toDouble())
        val n = (durationSeconds(instrument, midi) * sampleRate).toInt()
        val out = DoubleArray(n)
        val sr = sampleRate
        when (instrument) {
            Instrument.GRAND_PIANO, Instrument.BRIGHT_PIANO, Instrument.HONKY_TONK -> renderPiano(out, f0, midi, sr)
            Instrument.ELECTRIC_PIANO -> renderEPiano(out, f0, midi, sr)
            Instrument.FM_PIANO -> renderFmPiano(out, f0, midi, sr)
            Instrument.HARPSICHORD -> renderHarpsichord(out, f0, midi, sr)
            Instrument.MUSIC_BOX -> renderModal(out, f0, sr, doubleArrayOf(1.0, 2.0, 2.76, 5.40, 8.93), doubleArrayOf(1.0, 0.25, 0.30, 0.12, 0.05),
                (2.4 - (midi - 21) * 0.015).coerceIn(0.7, 2.4), 1.2, 0.001)
            Instrument.CELESTA -> renderModal(out, f0, sr, doubleArrayOf(1.0, 3.0, 4.0, 6.27), doubleArrayOf(1.0, 0.12, 0.2, 0.05),
                (2.2 - (midi - 21) * 0.012).coerceIn(0.6, 2.2), 1.5, 0.001)
            Instrument.GLOCKENSPIEL -> renderModal(out, f0 * 2, sr, doubleArrayOf(1.0, 2.76, 5.40, 8.93), doubleArrayOf(1.0, 0.45, 0.2, 0.08),
                1.6, 1.4, 0.0005)
            Instrument.VIBRAPHONE -> renderModal(out, f0, sr, doubleArrayOf(1.0, 4.0, 10.0), doubleArrayOf(1.0, 0.22, 0.05), 2.6, 3.5, 0.002, tremolo = 5.5)
            Instrument.MARIMBA -> renderModal(out, f0, sr, doubleArrayOf(1.0, 3.93, 9.2), doubleArrayOf(1.0, 0.12, 0.03),
                (0.9 - (midi - 40) * 0.006).coerceIn(0.25, 0.9), 4.0, 0.002)
            Instrument.XYLOPHONE -> renderModal(out, f0 * 2, sr, doubleArrayOf(1.0, 3.0, 6.0), doubleArrayOf(1.0, 0.3, 0.08), 0.35, 4.0, 0.0008)
            Instrument.KALIMBA -> renderModal(out, f0, sr, doubleArrayOf(1.0, 5.4, 9.6), doubleArrayOf(1.0, 0.18, 0.06), 1.1, 6.0, 0.001)
            Instrument.TUBULAR_BELLS -> renderModal(out, f0, sr, doubleArrayOf(1.0, 2.0, 3.0, 4.2, 5.4, 6.8), doubleArrayOf(0.5, 1.0, 0.6, 0.45, 0.3, 0.15),
                3.8, 1.3, 0.001)
            Instrument.STEEL_DRUM -> renderModal(out, f0, sr, doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.08), doubleArrayOf(1.0, 0.6, 0.3, 0.25, 0.1),
                1.0, 2.2, 0.002)
            Instrument.HARP -> renderPluck(out, f0, sr, brightness = 0.45, decay = (4.0 - (midi - 21) * 0.035).coerceIn(0.8, 4.0), pluckPos = 0.5)
            Instrument.NYLON_GUITAR -> renderPluck(out, f0, sr, brightness = 0.35, decay = (3.0 - (midi - 21) * 0.03).coerceIn(0.6, 3.0), pluckPos = 0.2)
            Instrument.STEEL_GUITAR -> renderPluck(out, f0, sr, brightness = 0.8, decay = (3.2 - (midi - 21) * 0.03).coerceIn(0.6, 3.2), pluckPos = 0.13)
            Instrument.PIZZICATO -> renderPluck(out, f0, sr, brightness = 0.3, decay = 0.45, pluckPos = 0.3)
            Instrument.CLAVINET -> renderPluck(out, f0, sr, brightness = 0.95, decay = 0.9, pluckPos = 0.08)
            else -> {}
        }
        // Normalise, shape loudness across the range and fade the tail to avoid clicks.
        var peak = 1e-9
        for (v in out) peak = max(peak, abs(v))
        val loudness = when (instrument) {
            Instrument.GRAND_PIANO, Instrument.BRIGHT_PIANO, Instrument.HONKY_TONK -> 0.62 + 0.18 * ((108 - midi) / 87.0)
            Instrument.MUSIC_BOX, Instrument.GLOCKENSPIEL, Instrument.XYLOPHONE -> 0.42
            Instrument.TUBULAR_BELLS -> 0.45
            else -> 0.55
        }
        val gain = loudness / peak
        val fade = min(n / 4, (0.08 * sampleRate).toInt())
        return FloatArray(n) { i ->
            var v = out[i] * gain
            if (i > n - fade) v *= (n - i).toDouble() / fade
            v.toFloat()
        }
    }

    /** Additive piano: stretched (inharmonic) partials, hammer-position comb, two detuned strings, double decay. */
    private fun renderPiano(out: DoubleArray, f0: Double, midi: Int, sr: Int) {
        val nyquistLimit = min(sr * 0.45, 14000.0)
        val b = 0.00007 * exp((midi - 21) * 0.055)
        val baseDecay = (4.2 - (midi - 21) * 0.042).coerceIn(0.5, 4.2)
        val hammerPos = 0.12
        val brightnessCutoff = 1800.0 + f0 * 3.0
        val maxPartials = if (midi < 45) 28 else if (midi < 70) 18 else 10
        for (k in 1..maxPartials) {
            val fk = k * f0 * sqrt(1 + b * k * k)
            if (fk > nyquistLimit) break
            var amp = 1.0 / k.toDouble().pow(1.05) * abs(sin(PI * k * hammerPos)) + 0.02 / k
            amp *= 1.0 / (1.0 + (fk / brightnessCutoff).pow(2))
            if (k == 1 && midi < 40) amp *= 0.6
            val tLong = baseDecay / (1.0 + 0.22 * (k - 1))
            val tShort = tLong * 0.16
            val strings = if (k <= 10) 2 else 1
            for (s in 0 until strings) {
                val detune = if (s == 0) 1.0 else 1.0 + 0.0007 * (1 + k * 0.05)
                addDecayingPartial(out, fk * detune, amp / strings, tShort, tLong, 0.62, sr, phase = s * 0.7)
            }
        }
        val rnd = Random(midi)
        var lp = 0.0
        val noiseLen = (0.012 * sr).toInt().coerceAtMost(out.size)
        val noiseAmp = 0.06 + 0.04 * ((midi - 21) / 87.0)
        for (i in 0 until noiseLen) {
            lp += 0.25 * ((rnd.nextDouble() * 2 - 1) - lp)
            out[i] += lp * noiseAmp * exp(-i / (0.0035 * sr))
        }
        applyAttack(out, sr, 0.0015)
    }

    private fun renderEPiano(out: DoubleArray, f0: Double, midi: Int, sr: Int) {
        val decay = (2.8 - (midi - 21) * 0.025).coerceIn(0.6, 2.8)
        val w = 2 * PI * f0 / sr
        val bellW = 2 * PI * f0 * 7.0 / sr
        for (i in out.indices) {
            val t = i.toDouble() / sr
            val index = 1.6 * exp(-t / 0.22) + 0.22
            val mod = sin(w * i) * index
            val env = 0.75 * exp(-t / decay) + 0.25 * exp(-t / (decay * 0.2))
            var v = sin(w * i + mod) * env
            if (bellW < PI) v += sin(bellW * i) * 0.09 * exp(-t / 0.04)
            v += sin(w * 2 * i) * 0.12 * exp(-t / (decay * 0.5))
            out[i] = v
        }
        applyAttack(out, sr, 0.002)
    }

    /** Two-operator FM with a 1:14 bell ratio - the classic 1980s digital electric piano. */
    private fun renderFmPiano(out: DoubleArray, f0: Double, midi: Int, sr: Int) {
        val decay = (3.0 - (midi - 21) * 0.028).coerceIn(0.6, 3.0)
        val w = 2 * PI * f0 / sr
        val wTine = 2 * PI * f0 * 14.0 / sr
        for (i in out.indices) {
            val t = i.toDouble() / sr
            val body = sin(w * i + sin(w * i) * (1.1 * exp(-t / 0.5) + 0.15)) * exp(-t / decay)
            val tine = if (wTine < PI) sin(wTine * i + sin(w * i) * 0.8) * 0.22 * exp(-t / 0.12) else 0.0
            out[i] = body + tine
        }
        applyAttack(out, sr, 0.001)
    }

    private fun renderHarpsichord(out: DoubleArray, f0: Double, midi: Int, sr: Int) {
        val base = (2.2 - (midi - 21) * 0.018).coerceIn(0.5, 2.2)
        for (k in 1..36) {
            val fk = k * f0 * (1 + 0.00002 * k * k)
            if (fk > sr * 0.45) break
            val amp = 1.0 / k.toDouble().pow(0.75) * (0.15 + abs(sin(PI * k * 0.08)))
            val tLong = base / (1.0 + 0.06 * k)
            addDecayingPartial(out, fk, amp, tLong * 0.3, tLong, 0.5, sr, 0.0)
        }
        applyAttack(out, sr, 0.0008)
    }

    /** Modal synthesis: a few inharmonic partials, each decaying faster the higher it is ([spread]). */
    private fun renderModal(
        out: DoubleArray, f0: Double, sr: Int, ratios: DoubleArray, amps: DoubleArray, decay: Double, spread: Double,
        attack: Double, tremolo: Double = 0.0,
    ) {
        for (k in ratios.indices) {
            val f = f0 * ratios[k]
            if (f > sr * 0.45) continue
            val t = decay / (1 + k * spread * 0.4)
            addDecayingPartial(out, f, amps[k], t * 0.3, t, 0.35, sr, k * 0.9)
        }
        if (tremolo > 0) for (i in out.indices) out[i] *= 0.78 + 0.22 * sin(2 * PI * tremolo * i / sr)
        applyAttack(out, sr, attack)
    }

    /**
     * Karplus-Strong plucked string with a fractional-delay all-pass for exact tuning. [pluckPos] shapes the initial
     * excitation (near the bridge = brighter), [brightness] sets the loop filter.
     */
    private fun renderPluck(out: DoubleArray, f0: Double, sr: Int, brightness: Double, decay: Double, pluckPos: Double) {
        val period = sr / f0
        val n = (period - 0.5).toInt().coerceAtLeast(2)
        val frac = period - n - 0.5
        val c = (1 - frac) / (1 + frac)
        val buf = DoubleArray(n)
        val rnd = Random((f0 * 1000).toInt())
        var lp = 0.0
        val a = 0.2 + 0.75 * brightness
        for (i in 0 until n) {
            lp += a * ((rnd.nextDouble() * 2 - 1) - lp)
            buf[i] = lp
        }
        // Pluck-position comb: subtract a delayed copy.
        val comb = (n * pluckPos).toInt().coerceAtLeast(1)
        val shaped = DoubleArray(n) { buf[it] - 0.8 * buf[(it - comb + n) % n] }
        for (i in 0 until n) buf[i] = shaped[i]
        // Loop gain for the requested decay time.
        val g = exp(-period / (decay * sr) * 6.9).coerceAtMost(0.99995)
        val mix = 0.35 + 0.6 * brightness
        var idx = 0
        var apX = 0.0
        var apY = 0.0
        var prev = 0.0
        for (i in out.indices) {
            val x = buf[idx]
            out[i] = x
            val filtered = (mix * x + (1 - mix) * prev) * g
            prev = x
            val y = c * filtered + apX - c * apY
            apX = filtered; apY = y
            buf[idx] = y
            if (++idx >= n) idx = 0
        }
        applyAttack(out, sr, 0.0006)
    }

    private fun addDecayingPartial(
        out: DoubleArray, freq: Double, amp: Double, tShort: Double, tLong: Double,
        shortMix: Double, sr: Int, phase: Double,
    ) {
        val w = 2 * PI * freq / sr
        val c = 2 * cos(w)
        var y1 = sin(phase - w)
        var y2 = sin(phase - 2 * w)
        val dShort = exp(-1.0 / (tShort * sr))
        val dLong = exp(-1.0 / (tLong * sr))
        var eShort = amp * shortMix
        var eLong = amp * (1 - shortMix)
        for (i in out.indices) {
            val y = c * y1 - y2
            y2 = y1; y1 = y
            out[i] += y * (eShort + eLong)
            eShort *= dShort
            eLong *= dLong
            if (eShort + eLong < 1e-6) break
        }
    }

    private fun applyAttack(out: DoubleArray, sr: Int, seconds: Double) {
        val n = (seconds * sr).toInt().coerceAtMost(out.size)
        for (i in 0 until n) out[i] *= i.toDouble() / n
    }

    /** Single-cycle wavetables for sustained instruments. */
    fun wavetable(instrument: Instrument): FloatArray {
        val size = 2048
        val harmonics: List<Pair<Int, Double>> = when (instrument) {
            // Drawbars 16' 8' 5 1/3' 4' 2 2/3' 2' expressed relative to the 16' sub-octave.
            Instrument.ORGAN -> listOf(1 to 0.55, 2 to 1.0, 3 to 0.55, 4 to 0.65, 6 to 0.35, 8 to 0.3)
            // Principal 8' + 4' + mixture, an octave low like the jazz organ.
            Instrument.CHURCH_ORGAN -> listOf(2 to 1.0, 4 to 0.7, 6 to 0.35, 8 to 0.4, 12 to 0.2, 16 to 0.18, 3 to 0.15)
            Instrument.ACCORDION -> (1..14).map { it to (if (it % 2 == 1) 1.0 else 0.6) / it.toDouble().pow(0.8) }
            Instrument.STRINGS -> (1..12).map { it to 1.0 / it.toDouble().pow(1.15) }
            Instrument.VIOLIN -> (1..18).map { it to 1.0 / it.toDouble().pow(0.95) * (if (it in 3..5) 1.4 else 1.0) }
            Instrument.CHOIR -> listOf(1 to 1.0, 2 to 0.55, 3 to 0.35, 4 to 0.12, 5 to 0.18, 6 to 0.08, 7 to 0.05)
            Instrument.FLUTE -> listOf(1 to 1.0, 2 to 0.32, 3 to 0.12, 4 to 0.05)
            Instrument.WARM_PAD -> (1..10).map { it to 1.0 / it.toDouble().pow(1.6) }
            Instrument.SAW_LEAD -> (1..30).map { it to 1.0 / it }
            Instrument.SQUARE_LEAD -> (1..29 step 2).map { it to 1.0 / it }
            else -> listOf(1 to 1.0)
        }
        val table = DoubleArray(size)
        for ((h, a) in harmonics) for (i in 0 until size) table[i] += a * sin(2 * PI * h * i / size)
        var peak = 1e-9
        for (v in table) peak = max(peak, abs(v))
        return FloatArray(size) { (table[it] / peak).toFloat() }
    }

    /** Frequency multiplier applied when playing the wavetable (organ tables are built an octave low). */
    fun wavetableRatio(instrument: Instrument) = if (instrument == Instrument.ORGAN || instrument == Instrument.CHURCH_ORGAN) 0.5 else 1.0

    fun click(sampleRate: Int, accent: Boolean): FloatArray {
        val n = (0.035 * sampleRate).toInt()
        val f = if (accent) 1760.0 else 1175.0
        return FloatArray(n) { i ->
            val t = i.toDouble() / sampleRate
            (sin(2 * PI * f * t) * exp(-t / 0.008) * (if (accent) 0.7 else 0.5)).toFloat()
        }
    }
}
