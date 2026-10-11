package com.pianoquest.app

import com.pianoquest.app.audio.PianoSamples
import com.pianoquest.app.audio.SynthEngine
import com.pianoquest.app.data.Difficulty
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.data.SongRepository
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.abs
import kotlin.test.Test

/**
 * Material for the Play Store promo video (only with PROMO=true): piano audio rendered offline with the app's own
 * engine, from the same song data, pedal marks and default sound settings the player uses.
 * Output: promo/out/audio/<song>.wav (48 kHz mono float -> 16-bit).
 */
class PromoTest {
    private val enabled = System.getenv("PROMO") == "true"
    private val outDir = File("../promo/out/audio")

    /** Renders a song from its count-in (beat 0 = first downbeat) for [seconds], like Listen mode at 100%. */
    private fun renderSong(id: String, seconds: Double, fromBeat: Double = 0.0, file: String = id, holdLast: Double = 0.0, barPedal: Boolean = false) {
        val rate = 48000
        val song = SongLibrary.get(id) ?: error("no song $id")
        val data = runBlocking { PianoSamples.load(); SongRepository.load(song, Difficulty.HARD) }
        val engine = SynthEngine(rate).apply { masterVolume = 0.8f; reverbAmount = 0.12f }
        val bps = data.bpm / 60.0
        // Timed commands (seconds from the start of the excerpt); pedal before notes at the same instant.
        val cmds = ArrayList<Triple<Double, Int, () -> Unit>>()
        val endBeat = fromBeat + seconds * bps
        // [barPedal]: one pedal per bar (lifted on the bar line, caught again just after) - for pieces with one harmony per bar.
        val pedal = if (!barPedal) data.pedal.map { it.beat to it.down } else
            generateSequence(0.0) { it + data.barBeats }.takeWhile { it <= endBeat }.flatMap { b -> sequenceOf(b to false, b + 0.12 to true) }.toList()
        for ((beat, down) in pedal) {
            if (beat > endBeat) break
            cmds += Triple(((beat - fromBeat) / bps).coerceAtLeast(0.0), 0) { engine.setSustain(down) }
        }
        // With [holdLast], the final chord rings on for that many seconds (a fermata for an ending).
        val lastStart = data.events.filter { it.start <= endBeat }.maxOfOrNull { it.start } ?: 0.0
        for (e in data.events) {
            if (e.start < fromBeat - 1e-6 || e.start > endBeat) continue
            val vel = (e.velocity * 0.85f).toInt().coerceIn(20, 127)
            val end = if (holdLast > 0 && abs(e.start - lastStart) < 1e-6) (e.start - fromBeat) / bps + holdLast else (e.end - fromBeat) / bps
            cmds += Triple((e.start - fromBeat) / bps, 1) { engine.noteOn(e.midi, vel) }
            cmds += Triple(end, 2) { engine.noteOff(e.midi) }
        }
        cmds.sortWith(compareBy({ it.first }, { it.second }))
        val total = ((seconds + holdLast + 2.5) * rate).toInt()
        val out = FloatArray(total)
        val block = 64
        val buf = FloatArray(block)
        var ci = 0
        var pos = 0
        while (pos < total) {
            val t = pos.toDouble() / rate
            while (ci < cmds.size && cmds[ci].first <= t) { cmds[ci].third(); ci++ }
            if (t >= seconds + holdLast && ci >= cmds.size) engine.allNotesOff()
            val n = minOf(block, total - pos)
            engine.render(buf, n)
            System.arraycopy(buf, 0, out, pos, n)
            pos += n
        }
        outDir.mkdirs()
        writeWav(File(outDir, "$file.wav"), out, rate)
        println("$file: bpm=${data.bpm} peak=${out.maxOf { abs(it) }} pedal=${data.pedal.size}")
    }

    private fun writeWav(f: File, pcm: FloatArray, rate: Int) {
        RandomAccessFile(f, "rw").use { w ->
            w.setLength(0)
            val bytes = pcm.size * 2
            fun le32(v: Int) = byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte())
            fun le16(v: Int) = byteArrayOf(v.toByte(), (v shr 8).toByte())
            w.write("RIFF".toByteArray()); w.write(le32(36 + bytes)); w.write("WAVEfmt ".toByteArray())
            w.write(le32(16)); w.write(le16(1)); w.write(le16(1)); w.write(le32(rate)); w.write(le32(rate * 2)); w.write(le16(2)); w.write(le16(16))
            w.write("data".toByteArray()); w.write(le32(bytes))
            val b = ByteArray(bytes)
            for (i in pcm.indices) {
                val s = (pcm[i].coerceIn(-1f, 1f) * 32767).toInt()
                b[2 * i] = s.toByte(); b[2 * i + 1] = (s shr 8).toByte()
            }
            w.write(b)
        }
    }

    /** Writes a song's notes (start, duration in seconds at 100%, midi, hand, velocity) for choosing excerpts. */
    private fun dumpSong(id: String) {
        val song = SongLibrary.get(id) ?: error("no song $id")
        val data = runBlocking { SongRepository.load(song, Difficulty.HARD) }
        val bps = data.bpm / 60.0
        outDir.mkdirs()
        File(outDir, "$id.notes.csv").writeText(data.events.joinToString("\n") {
            "${it.start / bps},${it.duration / bps},${it.midi},${it.hand},${it.velocity}"
        })
    }

    /** The last [seconds] of a song (its ending), plus the ring-out. */
    private fun renderEnding(id: String, seconds: Double, file: String, holdLast: Double) {
        val song = SongLibrary.get(id) ?: error("no song $id")
        val data = runBlocking { SongRepository.load(song, Difficulty.HARD) }
        val from = (data.length - seconds * data.bpm / 60.0).coerceAtLeast(0.0)
        renderSong(id, seconds, from, file, holdLast)
    }

    @Test
    fun audio() {
        if (!enabled) return
        renderSong("fur-elise", 40.0)
        renderSong("turkish-march", 30.0)
        renderSong("moonlight-3", 30.0)
        renderSong("gymnopedie", 150.0)
        renderSong("prelude-c", 140.0, barPedal = true)
        dumpSong("turkish-march")
        dumpSong("revolutionary")
        renderEnding("revolutionary", 14.0, "finale", holdLast = 4.5)
    }
}
