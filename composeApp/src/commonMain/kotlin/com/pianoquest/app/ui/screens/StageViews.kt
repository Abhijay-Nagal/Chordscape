package com.pianoquest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.Hand
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.ui.components.Glyph
import com.pianoquest.app.ui.components.drawChord
import com.pianoquest.app.ui.components.glyph
import com.pianoquest.app.ui.components.keyLabel
import com.pianoquest.app.ui.components.layoutKeys
import com.pianoquest.app.ui.components.rememberBravura
import com.pianoquest.app.ui.components.yFor
import com.pianoquest.app.ui.theme.PQ
import com.pianoquest.app.ui.theme.Wood
import kotlin.math.abs
import kotlin.math.floor

/** Colour of the A-B loop range on the seek bar. */
val LoopColor = Color(0xFF5FB3A6)

/**
 * Progress bar that seeks on tap/drag. When an A-B loop is set, the looped range is coloured
 * (while only A is set, the range from A to the current position is shown).
 */
@Composable
fun LoopSeekBar(progress: () -> Float, loopA: Float?, loopB: Float?, onSeek: (Float) -> Unit, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier
            .pointerInput(Unit) { detectTapGestures { onSeek((it.x / size.width).coerceIn(0f, 1f)) } }
            .pointerInput(Unit) { detectHorizontalDragGestures { change, _ -> onSeek((change.position.x / size.width).coerceIn(0f, 1f)) } }
    ) {
        val progress = progress()
        val h = size.height
        val trackH = h * 0.28f
        val y = (h - trackH) / 2
        val r = CornerRadius(trackH / 2)
        drawRoundRect(Wood.Sand.copy(alpha = 0.25f), Offset(0f, y), Size(size.width, trackH), r)
        drawRoundRect(Wood.Gold, Offset(0f, y), Size(size.width * progress, trackH), r)
        if (loopA != null) {
            val a = loopA * size.width
            val b = (loopB ?: progress).coerceAtLeast(loopA) * size.width
            val alpha = if (loopB != null) 0.95f else 0.55f
            drawRoundRect(LoopColor.copy(alpha = alpha), Offset(a, y - trackH * 0.45f), Size((b - a).coerceAtLeast(3f), trackH * 1.9f), CornerRadius(trackH))
            for ((x, label) in listOfNotNull(a to "A", loopB?.let { b to "B" })) {
                drawRect(LoopColor, Offset(x - 1.5f, 0f), Size(3f, h))
                val l = measurer.measure(label, TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Wood.Ivory))
                drawText(l, topLeft = Offset((x - l.size.width / 2f).coerceIn(0f, size.width - l.size.width), -2f))
            }
        }
        val x = size.width * progress
        drawCircle(Wood.Ivory, h * 0.30f, Offset(x, h / 2))
        drawCircle(Wood.Gold, h * 0.30f, Offset(x, h / 2), style = Stroke(2.5f))
    }
}

/** Seconds of music visible above the keys in the falling-notes views. */
internal fun lookaheadBeats(barBeats: Double, speed: Float) = (barBeats * 2).coerceAtLeast(6.0) / (0.6 + speed * 0.4)

/** Waterfall (bars) or Fallen Note (balls) view above the keyboard. */
@Composable
internal fun FallingNotes(r: SongRun, pb: Playback, lo: Int, hi: Int, speed: Float, mode: PlayMode, solfege: Boolean, balls: Boolean, modifier: Modifier) {
    val c = PQ.colors
    val measurer = rememberTextMeasurer(cacheSize = 96)
    val longest = remember(r) { r.events.maxOfOrNull { it.duration } ?: 0.0 }
    Canvas(modifier.clipToBounds()) {
        // Reading the clock here (in the draw phase) redraws only this canvas on each frame.
        pb.frame.let { }
        val time = pb.time
        val keys = layoutKeys(lo, hi, size.width, 100f)
        val byMidi = keys.associateBy { it.midi }
        val lookahead = lookaheadBeats(r.data.barBeats, speed)
        val pxPerBeat = size.height / lookahead.toFloat()
        // Wooden lanes: subtle stripes for every C and F.
        keys.filter { !it.black && (it.midi.mod(12) == 0 || it.midi.mod(12) == 5) }.forEach { k ->
            drawRect(Color.White.copy(alpha = if (k.midi.mod(12) == 0) 0.08f else 0.04f), Offset(k.rect.left, 0f), Size(1f, size.height))
        }
        for (b in r.data.barLines) {
            if (b < time - 0.5 || pb.outsideLoop(b)) continue
            if (b > time + lookahead) break
            val y = size.height - ((b - time) * pxPerBeat).toFloat()
            drawRect(Color(0x33E8C9A0), Offset(0f, y), Size(size.width, 1.5f))
        }
        val keyW = keys.firstOrNull { !it.black }?.rect?.width ?: 10f
        // Events are sorted by start: skip straight to the first one that can still be on screen.
        var first = 0
        var hiIdx = r.events.size
        val from = time - 0.5 - longest
        while (first < hiIdx) { val mid = (first + hiIdx) / 2; if (r.events[mid].start < from) first = mid + 1 else hiIdx = mid }
        for (i in first until r.events.size) {
            val ev = r.events[i]
            if (ev.start > time + lookahead) break
            if (ev.end < time - 0.5) continue
            // While an A-B loop is set, only the looped notes fall.
            if (pb.outsideLoop(ev.start)) continue
            // Listen mode: notes of a hidden hand still sound but do not fall.
            if (mode == PlayMode.LISTEN && !r.shows(ev.hand)) continue
            val yBottom = size.height - ((ev.start - time) * pxPerBeat).toFloat()
            val h = (ev.duration * pxPerBeat).toFloat().coerceAtLeast(6f)
            val base = if (ev.hand == Hand.RIGHT) c.keyRight else c.keyLeft
            val user = r.isUser[i]
            val color = when {
                r.hit[i] -> c.success
                r.missed[i] -> c.error
                !user && mode != PlayMode.LISTEN -> base.copy(alpha = 0.35f)
                else -> base
            }
            val k = byMidi[ev.midi]
            if (k == null) {
                // Off-screen note: show a marker at the edge so the player knows to move the keyboard.
                val left = ev.midi < lo
                drawRoundRect(color.copy(alpha = 0.6f), Offset(if (left) 0f else size.width - keyW * 0.35f, yBottom - h), Size(keyW * 0.35f, h - 2f), CornerRadius(4f))
                continue
            }
            if (balls) {
                val d = (keyW * 0.92f).coerceAtMost(48f)
                val rad = d / 2
                // A thin tail shows how long the note lasts; the ball marks when to play it.
                drawRoundRect(color.copy(alpha = color.alpha * 0.45f), Offset(k.rect.center.x - d * 0.12f, yBottom - h), Size(d * 0.24f, h), CornerRadius(d * 0.12f))
                val centre = Offset(k.rect.center.x, yBottom - rad)
                drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.55f), color, color.copy(alpha = color.alpha * 0.85f)),
                    center = centre - Offset(rad * 0.35f, rad * 0.35f), radius = d), rad, centre)
                if (user && !r.hit[i]) drawCircle(Color(0xB0FFF3DC), rad, centre, style = Stroke(1.5f))
                if (d > 16f) {
                    val layout = measurer.measure(keyLabel(ev.midi, solfege), TextStyle(fontSize = (d * 0.36f / density / fontScale).coerceIn(7f, 13f).sp,
                        fontWeight = FontWeight.Bold, color = Color.White))
                    if (layout.size.width < d) drawText(layout, topLeft = Offset(centre.x - layout.size.width / 2f, centre.y - layout.size.height / 2f))
                }
                continue
            }
            val w = k.rect.width * (if (k.black) 0.95f else 0.86f)
            val x = k.rect.center.x - w / 2
            drawRoundRect(Brush.verticalGradient(listOf(color, color.copy(alpha = color.alpha * 0.72f)), startY = yBottom - h, endY = yBottom),
                Offset(x, yBottom - h), Size(w, h - 2f), CornerRadius(w * 0.25f))
            if (user && !r.hit[i]) drawRoundRect(Color(0x80FFF3DC), Offset(x, yBottom - h), Size(w, h - 2f), CornerRadius(w * 0.25f), style = Stroke(1.5f))
            if (h > 18f && w > 14f) {
                val layout = measurer.measure(keyLabel(ev.midi, solfege), TextStyle(fontSize = (w * 0.38f / density / fontScale).coerceIn(7f, 13f).sp,
                    fontWeight = FontWeight.Bold, color = Color.White))
                if (layout.size.width < w + 4) drawText(layout, topLeft = Offset(k.rect.center.x - layout.size.width / 2f, yBottom - layout.size.height - 3f))
            }
        }
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x66D9A84E))), Offset(0f, size.height - 18f), Size(size.width, 18f))
        countIn(time, measurer)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.countIn(time: Double, measurer: androidx.compose.ui.text.TextMeasurer, color: Color = Color(0xCCF0DFC0)) {
    if (time >= 0) return
    val n = (-floor(time)).toInt()
    if (n in 1..8) {
        val l = measurer.measure("$n", TextStyle(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = color, textAlign = TextAlign.Center))
        drawText(l, topLeft = Offset(size.width / 2 - l.size.width / 2f, size.height / 2 - l.size.height / 2f))
    }
}

/** One chord (or single note) on one staff of the scrolling score. */
private class SheetChord(val start: Double, val duration: Double, val clef: Clef, val pitches: List<Pitch>, val indices: List<Int>, val hand: Hand)

private val NOTE_VALUES = doubleArrayOf(4.0, 3.0, 2.0, 1.5, 1.0, 0.75, 0.5, 0.25)

private fun quantize(d: Double): Double = NOTE_VALUES.minBy { abs(it - d) }

/**
 * Sheet Music guide mode: a grand staff that scrolls from right to left; notes are played when they reach the
 * gold play line. Notes are spaced in proportion to time, so rhythms are easy to follow.
 */
@Composable
internal fun ScrollingSheet(r: SongRun, pb: Playback, speed: Float, mode: PlayMode, modifier: Modifier) {
    val c = PQ.colors
    val measurer = rememberTextMeasurer(cacheSize = 128)
    val bravura = rememberBravura()
    val chords = remember(r) {
        val byHand = r.data.hasLeftHand && r.data.hasRightHand
        r.events.indices.groupBy { i ->
            val e = r.events[i]
            val clef = if (byHand) (if (e.hand == Hand.LEFT) Clef.BASS else Clef.TREBLE) else (if (e.midi >= 60) Clef.TREBLE else Clef.BASS)
            Triple(e.start, clef, 0)
        }.map { (k, idx) ->
            SheetChord(k.first, quantize(idx.maxOf { r.events[it].duration }), k.second, idx.map { r.events[it].pitch }.distinctBy { it.midi },
                idx, r.events[idx.first()].hand)
        }.sortedBy { it.start }
    }
    val paper = Color(0xFFF7EEDC)
    val ink = Color(0xFF2A170D)
    Canvas(modifier.clipToBounds()) {
        pb.frame.let { }
        val time = pb.time
        drawRect(paper)
        val s = (size.height / 22f).coerceAtMost(15f * density).coerceAtLeast(5f)
        val trebleTop = (size.height - s * 14f) / 2 - s * 0.5f
        val bassTop = trebleTop + s * 10f
        val playX = size.width * 0.24f
        val pxPerBeat = s * 7.5f / (0.6f + speed * 0.4f)
        val lineW = s * 0.11f
        val clefZone = s * 4.6f
        // Bar lines
        for (b in r.data.barLines) {
            val x = playX + ((b - time) * pxPerBeat).toFloat()
            if (x < clefZone || x > size.width) continue
            drawRect(ink.copy(alpha = 0.55f), Offset(x, trebleTop), Size(lineW * 1.4f, bassTop + 4 * s - trebleTop))
        }
        // Duration tails (how long each note sounds) behind the noteheads.
        val visible = chords.filter { ch ->
            val x = playX + ((ch.start - time) * pxPerBeat).toFloat()
            x > clefZone - s * 4 && x < size.width + s * 2 && !pb.outsideLoop(ch.start) && (mode != PlayMode.LISTEN || r.shows(ch.hand))
        }
        for (ch in visible) {
            val x = playX + ((ch.start - time) * pxPerBeat).toFloat()
            val top = if (ch.clef == Clef.TREBLE) trebleTop else bassTop
            val base = if (ch.hand == Hand.RIGHT) c.keyRight else c.keyLeft
            val bottomLine = if (ch.clef == Clef.BASS) 18 else 30
            for (p in ch.pitches) {
                val y = yFor(top, p.diatonicIndex - bottomLine, s)
                drawRoundRect(base.copy(alpha = 0.22f), Offset(x + s * 0.6f, y - s * 0.32f), Size((ch.duration * pxPerBeat).toFloat() - s * 0.6f, s * 0.64f), CornerRadius(s * 0.3f))
            }
        }
        // Staff lines over the tails.
        for (top in listOf(trebleTop, bassTop)) for (i in 0..4) drawRect(ink, Offset(0f, top + i * s - lineW / 2), Size(size.width, lineW))
        // Notes
        for (ch in visible) {
            val x = playX + ((ch.start - time) * pxPerBeat).toFloat()
            val top = if (ch.clef == Clef.TREBLE) trebleTop else bassTop
            val user = ch.indices.any { r.isUser[it] }
            val base = if (ch.hand == Hand.RIGHT) c.keyRight else c.keyLeft
            val color = when {
                ch.indices.all { !r.isUser[it] || r.hit[it] } && user -> c.success
                mode == PlayMode.LISTEN && ch.indices.any { r.hit[it] } -> c.success
                ch.indices.any { r.missed[it] } -> c.error
                user || mode == PlayMode.LISTEN -> lerpInk(base, ink)
                else -> ink.copy(alpha = 0.55f)
            }
            drawChord(measurer, bravura, ch.pitches, ch.duration, x - s * 0.6f, top, ch.clef, s, color, emptyMap())
        }
        // Clef area stays fixed on the left.
        drawRect(paper, Offset.Zero, Size(clefZone, size.height))
        for (top in listOf(trebleTop, bassTop)) for (i in 0..4) drawRect(ink, Offset(s * 0.4f, top + i * s - lineW / 2), Size(clefZone - s * 0.4f, lineW))
        drawRect(ink, Offset(s * 0.4f, trebleTop), Size(lineW * 2.5f, bassTop + 4 * s - trebleTop))
        glyph(measurer, bravura, Glyph.G_CLEF, s * 1.0f, yFor(trebleTop, 2, s), s, ink)
        glyph(measurer, bravura, Glyph.F_CLEF, s * 1.0f, yFor(bassTop, 6, s), s, ink)
        // Play line
        drawRect(Wood.Gold.copy(alpha = 0.25f), Offset(playX - s * 1.1f, trebleTop - s * 3f), Size(s * 2.2f, bassTop + 7 * s - trebleTop))
        drawRect(Wood.Gold, Offset(playX - 1.5f, trebleTop - s * 3f), Size(3f, bassTop + 7 * s - trebleTop))
        countIn(time, measurer, Wood.Chestnut)
    }
}

private fun lerpInk(a: Color, ink: Color) = androidx.compose.ui.graphics.lerp(a, ink, 0.35f)
