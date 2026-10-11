package com.pianoquest.app.data

import com.pianoquest.app.theory.Pitch

/** A note read from a Standard MIDI File, timed in seconds. */
data class MidiNote(val midi: Int, val startSec: Double, val durSec: Double, val velocity: Int, val track: Int, val channel: Int)

class ParsedMidi(
    val notes: List<MidiNote>,
    /** Tempo of the first beat (quarter notes per minute). */
    val initialBpm: Double,
    val meterNum: Int,
    val meterDen: Int,
    /** Bar-line positions in seconds. */
    val barTimes: List<Double>,
    val lengthSec: Double,
    val keySignature: Int,
    val keyIsMinor: Boolean,
    val trackNames: Map<Int, String>,
    /** Sustain-pedal changes from the score (controller 64): time in seconds and whether the pedal goes down. */
    val pedal: List<Pair<Double, Boolean>> = emptyList(),
)

/** Reader for Standard MIDI Files (format 0 and 1). Pure Kotlin, used on every platform. */
object MidiFileReader {

    private class Reader(val b: ByteArray, var p: Int = 0) {
        fun u8(): Int = b[p++].toInt() and 0xFF
        fun u16(): Int = (u8() shl 8) or u8()
        fun u32(): Int = (u16() shl 16) or u16()
        fun vlq(): Int {
            var v = 0
            while (true) {
                val c = u8()
                v = (v shl 7) or (c and 0x7F)
                if (c and 0x80 == 0) return v
            }
        }
        fun str(n: Int): String { val s = b.decodeToString(p, p + n); p += n; return s }
    }

    private data class Raw(val tick: Long, val kind: Int, val a: Int, val b: Int, val track: Int, val channel: Int)

    fun parse(bytes: ByteArray): ParsedMidi {
        val r = Reader(bytes)
        require(r.str(4) == "MThd") { "Not a MIDI file" }
        val hdrLen = r.u32()
        r.u16() // format
        val nTracks = r.u16()
        val division = r.u16()
        r.p = 8 + hdrLen
        require(division and 0x8000 == 0) { "SMPTE time division is not supported" }
        val tpq = division.toDouble()

        val tempos = ArrayList<Pair<Long, Int>>() // tick -> microseconds per quarter
        val meters = ArrayList<Triple<Long, Int, Int>>()
        var keySig = 0
        var minor = false
        val raws = ArrayList<Raw>()
        val names = HashMap<Int, String>()

        for (t in 0 until nTracks) {
            if (r.p + 8 > bytes.size) break
            val id = r.str(4)
            val len = r.u32()
            val end = r.p + len
            if (id != "MTrk") { r.p = end; continue }
            var tick = 0L
            var status = 0
            while (r.p < end) {
                tick += r.vlq()
                var st = r.u8()
                if (st < 0x80) { r.p--; st = status } else if (st < 0xF0) status = st
                when {
                    st == 0xFF -> {
                        val type = r.u8()
                        val l = r.vlq()
                        val start = r.p
                        when (type) {
                            0x51 -> if (l >= 3) tempos += tick to ((r.u8() shl 16) or (r.u8() shl 8) or r.u8())
                            0x58 -> if (l >= 2) { val n = r.u8(); val d = 1 shl r.u8(); meters += Triple(tick, n, d) }
                            0x59 -> if (l >= 2 && keySig == 0 && !minor) { keySig = bytes[r.p].toInt(); minor = bytes[r.p + 1].toInt() == 1 }
                            0x03 -> names[t] = bytes.decodeToString(r.p, r.p + l).trim()
                        }
                        r.p = start + l
                    }
                    st == 0xF0 || st == 0xF7 -> { val l = r.vlq(); r.p += l }
                    else -> {
                        val kind = st and 0xF0
                        val ch = st and 0x0F
                        val a = r.u8()
                        val b2 = if (kind == 0xC0 || kind == 0xD0) 0 else r.u8()
                        if (kind == 0x90 || kind == 0x80 || (kind == 0xB0 && a == 64)) raws += Raw(tick, kind, a, b2, t, ch)
                    }
                }
            }
            r.p = end
        }

        // Tempo map: tick -> seconds.
        tempos.sortBy { it.first }
        if (tempos.isEmpty() || tempos[0].first > 0) tempos.add(0, 0L to 500000)
        val segTick = LongArray(tempos.size) { tempos[it].first }
        val segSec = DoubleArray(tempos.size)
        for (i in 1 until tempos.size) segSec[i] = segSec[i - 1] + (segTick[i] - segTick[i - 1]) / tpq * tempos[i - 1].second / 1e6
        fun sec(tick: Long): Double {
            var i = segTick.size - 1
            while (i > 0 && segTick[i] > tick) i--
            return segSec[i] + (tick - segTick[i]) / tpq * tempos[i].second / 1e6
        }

        // Pair note-ons with note-offs (FIFO per track/channel/pitch).
        raws.sortWith(compareBy({ it.tick }, { if (it.kind == 0x80 || (it.kind == 0x90 && it.b == 0)) 0 else 1 }))
        val open = HashMap<Long, ArrayDeque<Raw>>()
        val notes = ArrayList<MidiNote>()
        var lastTick = 0L
        for (e in raws) {
            lastTick = maxOf(lastTick, e.tick)
            if (e.kind == 0xB0) continue
            val key = (e.track.toLong() shl 16) or (e.channel.toLong() shl 8) or e.a.toLong()
            if (e.kind == 0x90 && e.b > 0) {
                open.getOrPut(key) { ArrayDeque() }.addLast(e)
            } else {
                val on = open[key]?.removeFirstOrNull() ?: continue
                val s = sec(on.tick)
                notes += MidiNote(e.a, s, (sec(e.tick) - s).coerceAtLeast(0.03), on.b, on.track, on.channel)
            }
        }
        for ((_, q) in open) for (on in q) {
            val s = sec(on.tick)
            notes += MidiNote(on.a, s, 0.5, on.b, on.track, on.channel)
        }
        notes.sortWith(compareBy({ it.startSec }, { it.midi }))

        // Bar lines from the meter changes.
        meters.sortBy { it.first }
        if (meters.isEmpty() || meters[0].first > 0) meters.add(0, Triple(0L, 4, 4))
        val bars = ArrayList<Double>()
        val endTick = lastTick
        for ((i, m) in meters.withIndex()) {
            val barTicks = (tpq * 4 * m.second / m.third).toLong().coerceAtLeast(1)
            val until = if (i + 1 < meters.size) meters[i + 1].first else endTick + barTicks
            var tk = m.first
            while (tk < until) { bars += sec(tk); tk += barTicks }
        }
        val length = notes.maxOfOrNull { it.startSec + it.durSec } ?: 0.0
        // Sustain pedal: keep only real changes (several tracks often repeat the same pedal marks).
        val pedal = ArrayList<Pair<Double, Boolean>>()
        for (e in raws) {
            if (e.kind != 0xB0 || e.a != 64) continue
            val down = e.b >= 64
            if (pedal.lastOrNull()?.second == down) continue
            pedal += sec(e.tick) to down
        }
        return ParsedMidi(notes, 60e6 / tempos[0].second, meters[0].second, meters[0].third, bars, length, keySig, minor, names, pedal)
    }

    /**
     * Converts a parsed file to app note events. Time is expressed in beats of the file's opening tempo, so tempo changes
     * (ritardandos, fermatas) are preserved exactly. Hands: the lowest-pitched track is the left hand; if there is
     * only one track, notes below middle C go to the left hand.
     */
    fun toEvents(m: ParsedMidi): Pair<List<NoteEvent>, List<Double>> {
        val bps = m.initialBpm / 60.0
        val tracks = m.notes.groupBy { it.track }
        val handOf: (MidiNote) -> Hand = if (tracks.size >= 2) {
            val lowest = tracks.minBy { (_, ns) -> ns.map { it.midi }.average() }.key
            { n -> if (n.track == lowest) Hand.LEFT else Hand.RIGHT }
        } else {
            { n -> if (n.midi < 60) Hand.LEFT else Hand.RIGHT }
        }
        val events = m.notes.filter { it.midi in 21..108 }.map { n ->
            NoteEvent(Pitch.fromMidi(n.midi, m.keySignature < 0), n.startSec * bps, n.durSec * bps, handOf(n), n.velocity.coerceIn(30, 127))
        }
        return events to m.barTimes.map { it * bps }
    }

    /** The score's pedal changes in beats of the opening tempo (the same time base as [toEvents]). */
    fun pedalBeats(m: ParsedMidi): List<PedalChange> {
        val bps = m.initialBpm / 60.0
        return m.pedal.map { (s, down) -> PedalChange(s * bps, down) }
    }
}
