package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.Notation
import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.generated.resources.bravura
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.SOLFEGE
import com.pianoquest.app.theory.accidentalPretty
import com.pianoquest.app.ui.theme.PQ
import org.jetbrains.compose.resources.Font
import kotlin.math.abs
import kotlin.math.max

/** Something drawn on the staff: a note/chord, a rest, or a bar line. */
sealed interface StaffEl {
    data class Note(val pitches: List<Pitch>, val duration: Double) : StaffEl
    data class Rest(val duration: Double) : StaffEl
    data object Bar : StaffEl
}

/** Converts app notation ("C4:q E4 | G4:h") to staff elements, keeping rests and bar lines. */
fun staffElements(text: String): List<StaffEl> {
    val out = ArrayList<StaffEl>()
    var dur = 1.0
    for (tok in Notation.expandRepeats(text).split(Regex("\\s+")).filter { it.isNotEmpty() }) {
        if (tok == "|") { out += StaffEl.Bar; continue }
        val c = tok.indexOf(':')
        val body = if (c >= 0) tok.substring(0, c) else tok
        if (c >= 0) dur = Notation.parseDuration(tok.substring(c + 1))
        out += if (body == "R") StaffEl.Rest(dur) else StaffEl.Note(body.split('+').map { Pitch.parse(it) }, dur)
    }
    return out
}

internal object Glyph {
    const val G_CLEF = ""
    const val F_CLEF = ""
    const val HEAD_WHOLE = ""
    const val HEAD_HALF = ""
    const val HEAD_BLACK = ""
    const val SHARP = ""
    const val FLAT = ""
    const val NATURAL = ""
    const val DOUBLE_SHARP = ""
    const val DOUBLE_FLAT = ""
    const val FLAG8_UP = ""
    const val FLAG8_DOWN = ""
    const val FLAG16_UP = ""
    const val FLAG16_DOWN = ""
    const val DOT = ""
    const val REST_WHOLE = ""
    const val REST_HALF = ""
    const val REST_QUARTER = ""
    const val REST_8 = ""
    const val REST_16 = ""
    fun timeDigit(d: Int) = (0xE080 + d).toChar().toString()
}

@Composable
fun rememberBravura(): FontFamily {
    val font = Font(Res.font.bravura)
    return remember(font) { FontFamily(font) }
}

/**
 * Renders a musical staff (treble, bass or grand staff) with real engraving glyphs (Bravura / SMuFL).
 * [highlightIndex] counts only note/rest elements (bar lines excluded).
 */
@Composable
fun StaffView(
    notes: String,
    modifier: Modifier = Modifier,
    clef: Clef = Clef.TREBLE,
    keySig: Int = 0,
    timeSig: String? = null,
    showNames: Boolean = false,
    labels: List<String> = emptyList(),
    highlightIndex: Int? = null,
    noteColors: Map<Int, Color> = emptyMap(),
    space: Dp = 9.dp,
    solfege: Boolean = false,
    elements: List<StaffEl>? = null,
) {
    val els = remember(notes, elements) { elements ?: runCatching { staffElements(notes) }.getOrDefault(emptyList()) }
    val bravura = rememberBravura()
    val measurer = rememberTextMeasurer(cacheSize = 64)
    val colors = PQ.colors
    val ink = colors.staffInk
    val accent = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val grand = clef == Clef.GRAND
    val bottomExtra = if (showNames || labels.isNotEmpty()) 3.2f else 0f
    val heightSpaces = (if (grand) 23f else 13f) + bottomExtra

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val s = with(androidx.compose.ui.platform.LocalDensity.current) { space.toPx() }
        val headerSpaces = 4f + abs(keySig) * 1.0f + (if (timeSig != null) 2.4f else 0f)
        val noteCount = els.count { it !is StaffEl.Bar }
        val barCount = els.count { it is StaffEl.Bar }
        val minWidthPx = (headerSpaces + noteCount * 3.4f + barCount * 1.5f + 2f) * s
        val availPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }
        val widthPx = max(minWidthPx, availPx)
        val widthDp = with(androidx.compose.ui.platform.LocalDensity.current) { widthPx.toDp() }
        val heightDp = space * heightSpaces
        val scroll = rememberScrollState()
        Canvas(
            Modifier.then(if (widthPx > availPx + 1) Modifier.horizontalScroll(scroll) else Modifier)
                .width(widthDp).height(heightDp)
        ) {
            val trebleTop = s * 4.5f
            val bassTop = if (grand) trebleTop + s * 10f else trebleTop
            val staves = when (clef) {
                Clef.TREBLE -> listOf(Clef.TREBLE to trebleTop)
                Clef.BASS -> listOf(Clef.BASS to trebleTop)
                Clef.GRAND -> listOf(Clef.TREBLE to trebleTop, Clef.BASS to bassTop)
            }
            val lineW = s * 0.11f
            // Staff lines
            for ((_, top) in staves) {
                for (i in 0..4) drawRect(ink, Offset(s * 0.5f, top + i * s - lineW / 2), Size(size.width - s, lineW))
            }
            if (grand) {
                drawRect(ink, Offset(s * 0.5f, trebleTop), Size(lineW * 2, bassTop + 4 * s - trebleTop))
            }
            // Clefs, key signature, time signature
            var x = s * 1.0f
            for ((c, top) in staves) {
                if (c == Clef.TREBLE) glyph(measurer, bravura, Glyph.G_CLEF, x, yFor(top, 2, s), s, ink)
                else glyph(measurer, bravura, Glyph.F_CLEF, x, yFor(top, 6, s), s, ink)
            }
            x += s * 3.4f
            val sharpPos = listOf(8, 5, 9, 6, 3, 7, 4)
            val flatPos = listOf(4, 7, 3, 6, 2, 5, 1)
            for (i in 0 until abs(keySig)) {
                for ((c, top) in staves) {
                    val shift = if (c == Clef.BASS) -2 else 0
                    val pos = (if (keySig > 0) sharpPos[i] else flatPos[i]) + shift
                    glyph(measurer, bravura, if (keySig > 0) Glyph.SHARP else Glyph.FLAT, x, yFor(top, pos, s), s, ink)
                }
                x += s * 1.0f
            }
            if (timeSig != null) {
                val parts = timeSig.split("/")
                x += s * 0.4f
                for ((_, top) in staves) {
                    drawDigits(measurer, bravura, parts[0], x, yFor(top, 6, s), s, ink)
                    drawDigits(measurer, bravura, parts.getOrElse(1) { "4" }, x, yFor(top, 2, s), s, ink)
                }
                x += s * 2.4f
            }
            x += s * 1.2f
            val usable = size.width - x - s * 1.5f
            val slot = usable / (noteCount * 3.4f + barCount * 1.5f).coerceAtLeast(1f)
            val keyAcc = keyAccidentals(keySig)
            var idx = 0
            for (el in els) {
                when (el) {
                    StaffEl.Bar -> {
                        val bx = x + slot * 0.75f
                        val top = staves.first().second
                        val bottom = staves.last().second + 4 * s
                        drawRect(ink, Offset(bx, top), Size(lineW * 1.3f, bottom - top))
                        x += slot * 1.5f
                    }
                    is StaffEl.Rest -> {
                        val cx = x + slot * 1.2f
                        val color = noteColors[idx] ?: if (highlightIndex == idx) accent else ink
                        if (highlightIndex == idx) highlightColumn(cx, staves, s, accent)
                        for ((_, top) in staves.take(1)) drawRest(measurer, bravura, el.duration, cx, top, s, color)
                        x += slot * 3.4f
                        idx++
                    }
                    is StaffEl.Note -> {
                        val cx = x + slot * 1.2f
                        val color = noteColors[idx] ?: if (highlightIndex == idx) accent else ink
                        if (highlightIndex == idx) highlightColumn(cx, staves, s, accent)
                        val groups = if (grand) el.pitches.groupBy { if (it.midi >= 60) Clef.TREBLE else Clef.BASS } else mapOf(clef to el.pitches)
                        for ((c, ps) in groups) {
                            val top = staves.first { it.first == c }.second
                            drawChord(measurer, bravura, ps, el.duration, cx, top, c, s, color, keyAcc)
                        }
                        val label = labels.getOrNull(idx) ?: if (showNames) el.pitches.joinToString("\n") { n ->
                            if (solfege) SOLFEGE[n.letter] + accidentalPretty(n.accidental) else n.pretty
                        } else null
                        if (label != null) {
                            val layout = measurer.measure(label, TextStyle(fontSize = (s * 1.25f / density / fontScale).sp, fontWeight = FontWeight.Bold, color = color))
                            val ly = staves.last().second + s * 4f + s * 4.2f
                            drawText(layout, topLeft = Offset(cx + s * 0.6f - layout.size.width / 2f, ly))
                        }
                        x += slot * 3.4f
                        idx++
                    }
                }
            }
        }
    }
}

/** y of staff position p (0 = bottom line, 1 = first space ... 8 = top line) for a staff whose top line is at [top]. */
internal fun yFor(top: Float, p: Int, s: Float): Float = top + (8 - p) * s / 2

private fun DrawScope.highlightColumn(cx: Float, staves: List<Pair<Clef, Float>>, s: Float, color: Color) {
    val top = staves.first().second - s * 3.5f
    val bottom = staves.last().second + s * 7.5f
    drawRoundRect(color.copy(alpha = 0.13f), Offset(cx - s * 1.0f, top), Size(s * 3.2f, bottom - top), CornerRadius(s))
}

internal fun DrawScope.glyph(m: TextMeasurer, font: FontFamily, code: String, x: Float, baseline: Float, s: Float, color: Color) {
    val layout = m.measure(code, TextStyle(fontFamily = font, fontSize = (s * 4f / density / fontScale).sp, color = color))
    drawText(layout, topLeft = Offset(x, baseline - layout.firstBaseline))
}

private fun DrawScope.drawDigits(m: TextMeasurer, font: FontFamily, digits: String, x: Float, baseline: Float, s: Float, color: Color) {
    var dx = x
    for (ch in digits) {
        val d = ch.digitToIntOrNull() ?: continue
        glyph(m, font, Glyph.timeDigit(d), dx, baseline, s, color)
        dx += s * 1.6f
    }
}

private fun DrawScope.drawRest(m: TextMeasurer, font: FontFamily, dur: Double, cx: Float, top: Float, s: Float, color: Color) {
    val (g, pos) = when {
        dur >= 4 -> Glyph.REST_WHOLE to 6
        dur >= 2 -> Glyph.REST_HALF to 4
        dur >= 1 -> Glyph.REST_QUARTER to 4
        dur >= 0.5 -> Glyph.REST_8 to 4
        else -> Glyph.REST_16 to 4
    }
    glyph(m, font, g, cx, yFor(top, pos, s), s, color)
    if (isDotted(dur)) glyph(m, font, Glyph.DOT, cx + s * 1.6f, yFor(top, 5, s), s, color)
}

internal fun isDotted(d: Double): Boolean = listOf(6.0, 3.0, 1.5, 0.75, 0.375).any { abs(it - d) < 1e-6 }

private fun keyAccidentals(sig: Int): Map<Int, Int> =
    Key.signatureAccidentals(sig).associate { it.letter to it.accidental }

internal fun DrawScope.drawChord(
    m: TextMeasurer, font: FontFamily, pitches: List<Pitch>, dur: Double, cx: Float, top: Float, clef: Clef, s: Float,
    color: Color, keyAcc: Map<Int, Int>,
) {
    val bottomLine = if (clef == Clef.BASS) 18 else 30
    val positions = pitches.map { it.diatonicIndex - bottomLine }
    val headW = s * 1.18f
    val head = when {
        dur >= 4 -> Glyph.HEAD_WHOLE
        dur >= 2 -> Glyph.HEAD_HALF
        else -> Glyph.HEAD_BLACK
    }
    val lineW = s * 0.11f
    // Ledger lines
    val minP = positions.min()
    val maxP = positions.max()
    var lp = -2
    while (lp >= minP) { drawRect(color, Offset(cx - s * 0.35f, yFor(top, lp, s) - lineW), Size(headW + s * 0.7f, lineW * 1.6f)); lp -= 2 }
    lp = 10
    while (lp <= maxP) { drawRect(color, Offset(cx - s * 0.35f, yFor(top, lp, s) - lineW), Size(headW + s * 0.7f, lineW * 1.6f)); lp += 2 }
    // Note heads (seconds are offset to the side)
    val sorted = pitches.zip(positions).sortedBy { it.second }
    var prevP = Int.MIN_VALUE
    var shifted = false
    for ((pitch, p) in sorted) {
        shifted = if (p - prevP == 1) !shifted else false
        val hx = cx + if (shifted) headW else 0f
        glyph(m, font, head, hx, yFor(top, p, s), s, color)
        val shown = pitch.accidental
        val expected = keyAcc[pitch.letter] ?: 0
        if (shown != expected) {
            val g = when (shown) { 1 -> Glyph.SHARP; -1 -> Glyph.FLAT; 2 -> Glyph.DOUBLE_SHARP; -2 -> Glyph.DOUBLE_FLAT; else -> Glyph.NATURAL }
            glyph(m, font, g, cx - s * 1.25f, yFor(top, p, s), s, color)
        }
        if (isDotted(dur)) {
            val dotP = if (p % 2 == 0) p + 1 else p
            glyph(m, font, Glyph.DOT, cx + headW + s * 0.35f, yFor(top, dotP, s), s, color)
        }
        prevP = p
    }
    // Stem and flags
    if (dur < 4) {
        val avg = positions.average()
        val up = avg < 4
        val stemLen = s * 3.4f
        if (up) {
            val sx = cx + headW - lineW * 1.2f
            val yBottom = yFor(top, minP, s) - s * 0.15f
            val yTop = yFor(top, maxP, s) - stemLen
            drawRect(color, Offset(sx, yTop), Size(lineW * 1.2f, yBottom - yTop))
            if (dur < 1) glyph(m, font, if (dur < 0.5) Glyph.FLAG16_UP else Glyph.FLAG8_UP, sx, yTop, s, color)
        } else {
            val sx = cx
            val yTop = yFor(top, maxP, s) + s * 0.15f
            val yBottom = yFor(top, minP, s) + stemLen
            drawRect(color, Offset(sx, yTop), Size(lineW * 1.2f, yBottom - yTop))
            if (dur < 1) glyph(m, font, if (dur < 0.5) Glyph.FLAG16_DOWN else Glyph.FLAG8_DOWN, sx, yBottom, s, color)
        }
    }
}
