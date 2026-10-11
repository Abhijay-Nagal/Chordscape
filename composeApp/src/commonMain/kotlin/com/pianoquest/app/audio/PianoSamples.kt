package com.pianoquest.app.audio

import com.pianoquest.app.generated.resources.Res
import kotlin.concurrent.Volatile
import kotlin.math.pow

/** One recorded note chosen for a played key: the PCM data, playback-rate multiplier and gain. */
class PianoHit(val pcm: ShortArray, val pitchRatio: Double, val gain: Float)

/**
 * The recorded concert grand (Salamander Grand Piano V3 - Yamaha C5, Alexander Holm, CC BY 3.0).
 * 30 notes sampled every minor third from A0 to C8, at three velocity layers, stored as 32 kHz mono 16-bit PCM.
 * Notes in between are pitch-shifted by at most one semitone, so the timbre stays natural.
 */
object PianoSamples {
    const val RATE = 32000
    private val roots = IntArray(30) { 21 + it * 3 }

    @Volatile private var data: Array<Array<ShortArray?>>? = null
    /** Loudness (RMS of the attack) of every sample, so velocity can be mapped to an even loudness curve. */
    private var loudness: Array<DoubleArray> = Array(30) { DoubleArray(3) { 1.0 } }

    val isLoaded: Boolean get() = data != null

    suspend fun load() {
        if (data != null) return
        val loaded = Array(roots.size) { arrayOfNulls<ShortArray>(3) }
        for ((i, root) in roots.withIndex()) {
            for (layer in 0 until 3) {
                val bytes = runCatching { Res.readBytes("files/piano/p${root}_$layer.raw") }.getOrNull() ?: continue
                val pcm = ShortArray(bytes.size / 2)
                for (k in pcm.indices) {
                    pcm[k] = ((bytes[2 * k].toInt() and 0xFF) or (bytes[2 * k + 1].toInt() shl 8)).toShort()
                }
                loaded[i][layer] = pcm
            }
        }
        if (loaded.all { row -> row.all { it != null } }) {
            loudness = Array(roots.size) { i ->
                DoubleArray(3) { l ->
                    val pcm = loaded[i][l]!!
                    var sum = 0.0
                    val n = minOf(pcm.size, 8000)
                    for (k in 0 until n) sum += pcm[k].toDouble() * pcm[k]
                    kotlin.math.sqrt(sum / n.coerceAtLeast(1)).coerceAtLeast(1.0)
                }
            }
            data = loaded
        }
    }

    fun pick(midi: Int, velocity: Int): PianoHit? {
        val d = data ?: return null
        val idx = ((midi - 21 + 1) / 3).coerceIn(0, roots.lastIndex)
        val root = roots[idx]
        val layer = when {
            velocity < 58 -> 0
            velocity < 100 -> 1
            else -> 2
        }
        val pcm = d[idx][layer] ?: return null
        // Loudness follows one smooth curve across all three layers (pp to ff about 16 dB), while each layer
        // still gives its own tone colour - soft notes sound mellow, loud notes bright.
        val target = (velocity.coerceIn(1, 127) / 100.0).pow(1.4)
        val gain = (1.15 * target * loudness[idx][2] / loudness[idx][layer]).toFloat().coerceIn(0.05f, 4f)
        return PianoHit(pcm, 2.0.pow((midi - root) / 12.0), gain)
    }
}
