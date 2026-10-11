package com.pianoquest.app.data

import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.platform.Storage
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Difficulty(val label: String, val blurb: String) {
    EASY("Easy", "Main melody notes and one bass note per beat"),
    MEDIUM("Medium", "Melody with two-note chords and a sustained bass"),
    HARD("Hard", "The complete piece - every note of the score"),
}

/** A sustain-pedal change at [beat] (beats of the opening tempo). */
data class PedalChange(val beat: Double, val down: Boolean)

enum class PedalSource(val label: String) { SCORE("from the score"), AUTO("automatic, changing with the harmony"), NONE("none") }

/** A song ready to play: note events in beats of the opening tempo plus bar-line positions and pedalling. */
class SongData(
    val events: List<NoteEvent>,
    val bpm: Double,
    val barBeats: Double,
    val barLines: List<Double>,
    val pedal: List<PedalChange> = emptyList(),
    val pedalSource: PedalSource = PedalSource.NONE,
) {
    val length: Double = events.maxOfOrNull { it.end } ?: 0.0
    val hasLeftHand: Boolean = events.any { it.hand == Hand.LEFT }
    val hasRightHand: Boolean = events.any { it.hand == Hand.RIGHT }
    val durationSeconds: Double get() = length * 60.0 / bpm
}

/** Reduces a full arrangement to easier versions. Hard returns the input unchanged. */
object Arranger {
    private fun q(x: Double) = round(x * 48) / 48.0

    fun arrange(events: List<NoteEvent>, level: Difficulty): List<NoteEvent> {
        if (level == Difficulty.HARD || events.isEmpty()) return events
        val rh = events.filter { it.hand == Hand.RIGHT }
        val lh = events.filter { it.hand == Hand.LEFT }
        return (reduceRight(rh, level) + reduceLeft(lh, level)).sortedWith(compareBy({ it.start }, { it.midi }))
    }

    private fun reduceRight(rh: List<NoteEvent>, level: Difficulty): List<NoteEvent> {
        val groups = rh.groupBy { q(it.start) }.toSortedMap().values
        val out = ArrayList<NoteEvent>()
        var lastKept = -10.0
        for (g in groups) {
            val sorted = g.sortedByDescending { it.midi }
            if (level == Difficulty.MEDIUM) {
                out += sorted.take(2)
            } else {
                val top = sorted.first()
                val onBeat = abs(top.start - round(top.start)) < 0.02
                // Easy: keep the melody's structural notes - skip fast passing notes between beats.
                if (onBeat || top.start - lastKept >= 0.45 || top.duration >= 0.75) {
                    out += top
                    lastKept = top.start
                }
            }
        }
        if (level == Difficulty.EASY) {
            // Let each kept note ring until the next one so the melody stays connected.
            for (i in out.indices) {
                val next = out.getOrNull(i + 1)?.start ?: continue
                if (out[i].end < next && next - out[i].start <= 2.0) out[i] = out[i].copy(duration = next - out[i].start)
            }
        }
        return out
    }

    private fun reduceLeft(lh: List<NoteEvent>, level: Difficulty): List<NoteEvent> {
        if (lh.isEmpty()) return lh
        // One bass note per beat: the lowest note that starts within that beat.
        val byBeat = lh.groupBy { floor(it.start + 0.01) }.toSortedMap()
        val out = ArrayList<NoteEvent>()
        for ((beat, notes) in byBeat) {
            val lowest = notes.minBy { it.midi }
            val start = if (abs(lowest.start - beat) < 0.26) beat else lowest.start
            out += lowest.copy(start = start)
            if (level == Difficulty.MEDIUM) {
                val second = notes.filter { it.start <= lowest.start + 0.05 && it.midi >= lowest.midi + 3 }.maxByOrNull { it.midi }
                if (second != null) out += second.copy(start = start)
            }
        }
        // Sustain each bass note until the next one (up to a bar), which sounds full even with few notes.
        val starts = out.map { it.start }.distinct().sorted()
        return out.map { n ->
            val next = starts.firstOrNull { it > n.start + 1e-6 } ?: (n.start + n.duration)
            n.copy(duration = (next - n.start).coerceIn(0.25, 4.0))
        }
    }
}

/** Loads songs (built-in notation, bundled MIDI scores or user-imported MIDI) and caches the result. */
object SongRepository {
    private val cache = HashMap<String, SongData>()

    suspend fun load(song: Song, level: Difficulty): SongData {
        val key = "${song.id}|$level"
        cache[key]?.let { return it }
        val full = cache["${song.id}|HARD"] ?: loadFull(song).also { cache["${song.id}|HARD"] = it }
        val data = if (level == Difficulty.HARD) full
            else SongData(Arranger.arrange(full.events, level), full.bpm, full.barBeats, full.barLines, full.pedal, full.pedalSource)
        cache[key] = data
        return data
    }

    private suspend fun loadFull(song: Song): SongData = withContext(Dispatchers.Default) {
        val bytes: ByteArray? = when {
            song.midiResource != null -> Res.readBytes("files/midi/${song.midiResource}.mid")
            song.importedFile != null -> Storage.readBytes(song.importedFile)
            else -> null
        }
        if (bytes != null) {
            val parsed = MidiFileReader.parse(bytes)
            val (events, bars) = MidiFileReader.toEvents(parsed)
            val barBeats = parsed.meterNum * 4.0 / parsed.meterDen
            val shaped = Dynamics.shape(events, bars, barBeats)
            val scorePedal = MidiFileReader.pedalBeats(parsed)
            val (pedal, source) = when {
                scorePedal.any { it.down } -> scorePedal to PedalSource.SCORE
                AutoPedal.wanted(song) -> AutoPedal.generate(shaped, barBeats, bars, AutoPedal.liftForRuns(song)) to PedalSource.AUTO
                else -> emptyList<PedalChange>() to PedalSource.NONE
            }
            return@withContext SongData(shaped, parsed.initialBpm, barBeats, bars, pedal, if (pedal.isEmpty()) PedalSource.NONE else source)
        }
        val bars = ArrayList<Double>()
        val len = song.lengthBeats
        var b = song.pickup
        if (song.pickup > 0) bars += 0.0
        while (b <= len + 1e-6) { bars += b; b += song.barBeats }
        val shaped = Dynamics.shape(song.events, bars, song.barBeats)
        val pedal = if (AutoPedal.wanted(song)) AutoPedal.generate(shaped, song.barBeats, bars, AutoPedal.liftForRuns(song)) else emptyList()
        SongData(shaped, song.bpm.toDouble(), song.barBeats, bars, pedal, if (pedal.isEmpty()) PedalSource.NONE else PedalSource.AUTO)
    }
}

/**
 * Pedalling for pieces whose score file has no pedal marks but which are always played with pedal (Romantic and
 * Impressionist music). It works like a pianist's "legato" pedal: the pedal comes up exactly as a new bass note
 * (a new harmony) sounds and goes down again just after it, so the harmony rings without blurring into the next one.
 */
object AutoPedal {
    private val STYLES = setOf(Genre.ROMANTIC, Genre.MODERN_CLASSICAL, Genre.CONTEMPORARY)
    /** Classical-era music is pedalled more sparingly: chords and broken chords yes, fast scales and turns no. */
    private val LIGHT_STYLES = setOf(Genre.CLASSICAL)
    /** Other pieces that need pedal although their style usually doesn't. */
    private val PIECES = setOf("moonlight", "sonata-facile-2", "k331-theme", "traumerei", "fantasy-d")

    fun wanted(song: Song) = song.genre in STYLES || song.genre in LIGHT_STYLES || song.id in PIECES

    /** Whether the pedal must come up for fast step-wise melody (scales, turns, trills) in this song. */
    fun liftForRuns(song: Song) = song.genre in LIGHT_STYLES && song.id !in PIECES

    /**
     * Fast step-wise passages in the melody (the top notes of the right hand): three or more notes in a row, each a
     * semitone or tone from the last, at least two to a beat. Pedal there would blur the notes. Returns (start, end).
     */
    private fun runs(events: List<NoteEvent>): List<Pair<Double, Double>> {
        val rh = events.filter { it.hand == Hand.RIGHT }.ifEmpty { events }
        val melody = rh.groupBy { kotlin.math.round(it.start * 20) / 20.0 }.toSortedMap().values.map { g -> g.maxBy { it.midi } }
        val out = ArrayList<Pair<Double, Double>>()
        var k = 0
        while (k + 2 < melody.size) {
            var n = k
            while (n + 1 < melody.size && melody[n + 1].start - melody[n].start <= 0.51 &&
                kotlin.math.abs(melody[n + 1].midi - melody[n].midi) in 1..2) n++
            if (n - k >= 2) { out += melody[k].start to melody[n].end; k = n } else k++
        }
        return out
    }

    /**
     * Classical-era texture that wants pedal between [from] and [to]: a sustained bass note, or a broken chord /
     * arpeggio / octave figure in the left hand. Repeated short chords and single-line bass do not.
     */
    private fun pedalTexture(left: List<NoteEvent>, from: Double, to: Double): Boolean {
        val span = left.filter { it.start >= from - 0.01 && it.start < to }
        if (span.isEmpty()) return false
        val bass = span.filter { it.start < from + 0.05 }.minByOrNull { it.midi } ?: span.minBy { it.midi }
        if (bass.duration >= 0.9) return true
        val singles = span.groupBy { kotlin.math.round(it.start * 20) }.values.filter { it.size == 1 }.map { it.first() }
        return singles.size >= 3 && singles.map { it.midi }.distinct().size >= 2 && singles.maxOf { it.midi } - singles.minOf { it.midi } >= 5
    }

    fun generate(events: List<NoteEvent>, barBeats: Double, bars: List<Double> = emptyList(), liftForRuns: Boolean = false): List<PedalChange> {
        if (events.size < 4) return emptyList()
        val sorted = events.sortedBy { it.start }
        fun onBarLine(t: Double) = if (bars.isEmpty()) kotlin.math.abs(t / barBeats - kotlin.math.round(t / barBeats)) < 0.02
            else bars.any { kotlin.math.abs(it - t) < 0.05 }
        // A new bass note (a new harmony) is the lowest note of its chord that either falls on a bar line or has no lower
        // note before it within the last three quarters of a bar - so the higher notes of a broken-chord or waltz
        // accompaniment do not count as new harmonies.
        val lookBack = barBeats * 0.75
        val bass = ArrayList<NoteEvent>()
        var lo = 0
        var i = 0
        while (i < sorted.size) {
            var j = i
            while (j < sorted.size && sorted[j].start - sorted[i].start < 0.1) j++
            val low = sorted.subList(i, j).minBy { it.midi }
            while (lo < i && sorted[lo].start < low.start - lookBack) lo++
            val lowerBefore = (lo until i).any { sorted[it].midi < low.midi }
            if (onBarLine(low.start) || !lowerBefore) bass += low
            i = j
        }
        val out = ArrayList<PedalChange>()
        var lastChange = Double.NEGATIVE_INFINITY
        var lastBass = -1
        val lastEnd = sorted.maxOf { it.end }
        for (b in bass) {
            val changed = b.midi != lastBass || b.start - lastChange >= barBeats - 0.01
            if (!changed || b.start - lastChange < 0.45) continue
            // A long silence before this note: let the pedal come up at the end of the previous sound.
            val gapStart = sorted.filter { it.start < b.start }.maxOfOrNull { it.end } ?: b.start
            if (out.lastOrNull()?.down == true && b.start - gapStart > 1.0) out += PedalChange(gapStart, false)
            if (out.lastOrNull()?.down == true) out += PedalChange(b.start, false)
            out += PedalChange(b.start + 0.12, true)
            lastChange = b.start
            lastBass = b.midi
        }
        if (out.lastOrNull()?.down == true) out += PedalChange(lastEnd, false)
        if (!liftForRuns) return out
        // Classical style: pedal only chordal / broken-chord passages, and keep it up through fast scales and turns.
        val runList = runs(sorted)
        val left = sorted.filter { it.hand == Hand.LEFT }.ifEmpty { sorted.filter { it.midi < 60 } }
        val result = ArrayList<PedalChange>()
        var p = 0
        while (p + 1 < out.size) {
            val down = out[p]
            val up = out[p + 1]
            p += 2
            if (!down.down || up.down) continue
            if (!pedalTexture(left, down.beat - 0.12, up.beat)) continue
            var start = down.beat
            var end = up.beat
            for ((rs, re) in runList) {
                if (re <= start || rs >= end) continue
                if (rs <= start + 0.25) start = maxOf(start, re + 0.05) else end = minOf(end, rs - 0.02)
            }
            if (end - start >= 0.4) { result += PedalChange(start, true); result += PedalChange(end, false) }
        }
        return result
    }
}

/**
 * Touch-sensitive playback. Many score files store every note at one loudness, which sounds mechanical. Such files
 * get musical dynamics:
 * - a dynamic level that follows the music's energy (busier, higher, thicker passages louder; thin ones softer),
 *   smoothed over several bars so it swells and fades like a pianist's crescendos and diminuendos;
 * - a singing top melody, softer inner voices and a supporting bass;
 * - metric accents, accented big chords, phrase arcs and tiny human variations.
 * Files that already contain a wide range of dynamics are kept as written.
 */
object Dynamics {
    fun shape(events: List<NoteEvent>, bars: List<Double>, barBeats: Double): List<NoteEvent> {
        if (events.size < 8) return events
        val vels = events.map { it.velocity }
        if (vels.distinct().size > 6 && (vels.max() - vels.min()) >= 35) return events
        val sorted = events.sortedBy { it.start }
        val n = sorted.size

        // Onset groups (chords) and the melody / bass roles.
        val melody = HashSet<NoteEvent>()
        val bass = HashSet<NoteEvent>()
        val groupSize = HashMap<NoteEvent, Int>()
        var i = 0
        while (i < n) {
            var j = i
            while (j < n && sorted[j].start - sorted[i].start < 0.06) j++
            val group = sorted.subList(i, j)
            group.filter { it.hand == Hand.RIGHT }.maxByOrNull { it.midi }?.let { melody += it }
            group.filter { it.hand == Hand.LEFT }.minByOrNull { it.midi }?.let { bass += it }
            for (e in group) groupSize[e] = group.size
            i = j
        }

        // Energy around each note: notes per beat, average pitch and chord thickness within a +-2 beat window.
        val raw = DoubleArray(n)
        var lo = 0
        var hi = 0
        var pitchSum = 0.0
        for (k in 0 until n) {
            val t = sorted[k].start
            while (hi < n && sorted[hi].start <= t + 2.0) { pitchSum += sorted[hi].midi; hi++ }
            while (lo < n && sorted[lo].start < t - 2.0) { pitchSum -= sorted[lo].midi; lo++ }
            val count = (hi - lo).coerceAtLeast(1)
            raw[k] = count / 4.0 * 0.6 + (pitchSum / count - 60) / 12.0 * 0.5 + (groupSize[sorted[k]] ?: 1) * 0.15
        }
        // Smooth over about two bars, then rank within the piece (0 = its quietest passage, 1 = its loudest).
        val smooth = DoubleArray(n)
        val span = (barBeats * 2).coerceAtLeast(4.0)
        var a = 0
        var b = 0
        var acc = 0.0
        for (k in 0 until n) {
            val t = sorted[k].start
            while (b < n && sorted[b].start <= t + span) { acc += raw[b]; b++ }
            while (a < n && sorted[a].start < t - span) { acc -= raw[a]; a++ }
            smooth[k] = acc / (b - a).coerceAtLeast(1)
        }
        val order = smooth.sortedArray()
        val level = HashMap<NoteEvent, Double>()
        for (k in 0 until n) {
            val rank = order.binarySearch(smooth[k]).let { if (it < 0) -it - 1 else it }.toDouble() / (n - 1).coerceAtLeast(1)
            level[sorted[k]] = rank
        }

        // Melody notes above their neighbours sing out a little more.
        val mel = sorted.filter { it in melody }
        val avg = HashMap<NoteEvent, Double>()
        for ((k, e) in mel.withIndex()) {
            val w = mel.subList(maxOf(0, k - 8), minOf(mel.size, k + 9))
            avg[e] = w.sumOf { it.midi }.toDouble() / w.size
        }
        val phraseLen = barBeats * 4
        val lastStart = sorted.last().start
        return events.map { e ->
            // Base dynamic: from about piano (50) in the calmest passages to forte (90) in the most intense.
            var v = 50.0 + 40.0 * (level[e] ?: 0.5)
            v += when {
                e in melody -> 10.0
                e.hand == Hand.RIGHT -> -8.0
                e in bass -> -2.0
                else -> -10.0
            }
            val bi = bars.binarySearch(e.start).let { if (it >= 0) it else -it - 2 }.coerceAtLeast(0)
            val inBar = e.start - bars.getOrElse(bi) { 0.0 }
            val frac = inBar - kotlin.math.floor(inBar + 1e-6)
            v += when {
                inBar < 0.05 -> 6.0
                frac < 0.05 || frac > 0.95 -> 2.0
                else -> -2.0
            }
            if ((groupSize[e] ?: 1) >= 4) v += 5.0
            v += (e.duration * 2.5).coerceAtMost(5.0)
            avg[e]?.let { v += ((e.midi - it) * 0.8).coerceIn(-7.0, 7.0) }
            if (e.midi < 36) v -= 4.0
            val phrasePos = ((e.start % phraseLen) + phraseLen) % phraseLen / phraseLen
            v += kotlin.math.sin(kotlin.math.PI * phrasePos) * 6.0 - 3.0
            if (e.start >= lastStart - 0.01) v += 4.0
            v += (((e.midi * 7919 + (e.start * 97).toInt() * 104729) % 7) - 3).toDouble() * 0.9
            e.copy(velocity = v.toInt().coerceIn(28, 118))
        }
    }
}
