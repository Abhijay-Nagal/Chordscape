package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.SOLFEGE
import com.pianoquest.app.theory.accidentalPretty
import com.pianoquest.app.theory.isBlackKey
import com.pianoquest.app.ui.theme.PQ

/** Pastel chip colours per octave, as on many digital-piano apps. */
val OCTAVE_COLORS = listOf(
    Color(0xFFF2B8B0), Color(0xFFF4C9A0), Color(0xFFF2DE9C), Color(0xFFCFE3A4), Color(0xFFA8DCC0),
    Color(0xFFA6D0E6), Color(0xFFB9B4EA), Color(0xFFDDB0E0), Color(0xFFF0B0C8), Color(0xFFF2B8B0),
)

data class KeyShape(val midi: Int, val rect: Rect, val black: Boolean)

/** Lays out keys from [start] to [end] (inclusive), widening the range to white keys at both ends. */
fun layoutKeys(start: Int, end: Int, width: Float, height: Float, blackHeightRatio: Float = 0.62f): List<KeyShape> {
    val s = if (isBlackKey(start)) start - 1 else start
    val e = if (isBlackKey(end)) end + 1 else end
    val whites = (s..e).filter { !isBlackKey(it) }
    if (whites.isEmpty() || width <= 0f) return emptyList()
    val ww = width / whites.size
    val bw = ww * 0.62f
    val bh = height * blackHeightRatio
    val out = ArrayList<KeyShape>()
    whites.forEachIndexed { i, m -> out += KeyShape(m, Rect(i * ww, 0f, (i + 1) * ww, height), false) }
    for (m in s..e) {
        if (!isBlackKey(m)) continue
        val leftWhite = whites.indexOf(m - 1)
        if (leftWhite < 0) continue
        val offset = when (m.mod(12)) { 1 -> -0.07f; 3 -> 0.07f; 6 -> -0.09f; 8 -> 0f; else -> 0.09f }
        val cx = (leftWhite + 1) * ww + offset * ww
        out += KeyShape(m, Rect(cx - bw / 2, 0f, cx + bw / 2, bh), true)
    }
    return out
}

fun hitTest(keys: List<KeyShape>, p: Offset): KeyShape? =
    keys.firstOrNull { it.black && it.rect.contains(p) } ?: keys.firstOrNull { !it.black && it.rect.contains(p) }

fun keyLabel(midi: Int, solfege: Boolean, withOctave: Boolean = false): String {
    val p = Pitch.fromMidi(midi)
    val base = if (solfege) SOLFEGE[p.letter] + accidentalPretty(p.accidental) else p.pretty
    return if (withOctave) base + p.octave else base
}

/**
 * Interactive piano keyboard. Every touch goes through [PianoInput] by default, so lessons, games and the
 * synth all hear it; MIDI keyboard presses light up the keys too.
 */
@Composable
fun PianoKeyboard(
    startMidi: Int,
    endMidi: Int,
    modifier: Modifier = Modifier,
    highlights: Map<Int, Color> = emptyMap(),
    labels: KeyLabels = KeyLabels.C_ONLY,
    customLabels: Map<Int, String> = emptyMap(),
    fingers: Map<Int, Int> = emptyMap(),
    interactive: Boolean = true,
    solfege: Boolean = false,
    blackHeightRatio: Float = 0.62f,
    handColors: Map<Int, Color> = emptyMap(),
    onDown: (Int, Int) -> Unit = { m, v -> PianoInput.noteOn(m, v) },
    onUp: (Int) -> Unit = { PianoInput.noteOff(it) },
) {
    val colors = PQ.colors
    val held by PianoInput.held.collectAsState()
    val demo by PianoInput.demo.collectAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val keys = remember(startMidi, endMidi, size, blackHeightRatio) {
        layoutKeys(startMidi, endMidi, size.width.toFloat(), size.height.toFloat(), blackHeightRatio)
    }
    val currentKeys by rememberUpdatedState(keys)
    val down by rememberUpdatedState(onDown)
    val up by rememberUpdatedState(onUp)
    val active = remember { HashMap<PointerId, Int>() }
    val measurer = rememberTextMeasurer(cacheSize = 128)
    val a11y = com.pianoquest.app.ui.LocalA11y.current
    val haptics by rememberUpdatedState(a11y.haptics)
    // Note names are sized from the key width (in dp), larger when the accessibility option asks for it.
    val labelBoost = if (a11y.largeKeyLabels) 1.45f else 1f

    DisposableEffect(Unit) {
        onDispose {
            active.values.forEach { up(it) }
            active.clear()
        }
    }

    val inputModifier = if (!interactive) Modifier else Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                for (change in event.changes) {
                    val id = change.id
                    if (change.pressed) {
                        val key = hitTest(currentKeys, change.position)
                        val prev = active[id]
                        if (key?.midi != prev) {
                            prev?.let { up(it) }
                            if (key != null) {
                                val rel = ((change.position.y - key.rect.top) / key.rect.height).coerceIn(0f, 1f)
                                val vel = (48 + 79 * rel).toInt().coerceIn(30, 127)
                                active[id] = key.midi
                                down(key.midi, vel)
                                if (haptics) com.pianoquest.app.platform.hapticTick()
                            } else {
                                active.remove(id)
                            }
                        }
                        change.consume()
                    } else {
                        active.remove(id)?.let { up(it) }
                    }
                }
            }
        }
    }

    Canvas(modifier.onSizeChanged { size = it }.then(inputModifier)) {
        if (keys.isEmpty()) return@Canvas
        val radius = (keys.first { !it.black }.rect.width * 0.12f).coerceAtMost(10f)
        // White keys
        for (k in keys) {
            if (k.black) continue
            val isHeld = k.midi in held
            val isDemo = k.midi in demo
            val hl = highlights[k.midi]
            val base = when {
                isHeld -> handColors[k.midi]?.let { lerp(colors.keyWhite, it, 0.55f) } ?: colors.keyWhitePressed
                isDemo -> lerp(colors.keyWhite, handColors[k.midi] ?: colors.keyDemo, 0.6f)
                hl != null -> lerp(colors.keyWhite, hl, 0.75f)
                else -> colors.keyWhite
            }
            drawWhiteKey(k.rect, base, radius, isHeld || isDemo, colors.isDark)
        }
        // Labels and finger numbers on white keys
        for (k in keys) {
            if (k.black) continue
            val text = labelFor(k.midi, labels, highlights, customLabels, solfege) ?: continue
            val fsDp = (k.rect.width / density * 0.3f * labelBoost).coerceIn(9f, 18f * labelBoost)
            val style = TextStyle(fontSize = (fsDp / fontScale).sp, fontWeight = FontWeight.Bold,
                color = if (highlights.containsKey(k.midi) || k.midi in held) Color(0xFF1B1640) else Color(0xFF6B6690))
            val layout = measurer.measure(text, style)
            val tl = Offset(k.rect.center.x - layout.size.width / 2f, k.rect.bottom - layout.size.height - k.rect.width * 0.18f)
            if (labels == KeyLabels.COLORED) {
                val chip = OCTAVE_COLORS[(k.midi / 12).coerceIn(0, OCTAVE_COLORS.lastIndex)]
                val pad = 3f
                drawRoundRect(chip, Offset(tl.x - pad, tl.y - pad / 2), Size(layout.size.width + pad * 2, layout.size.height + pad), CornerRadius(4f))
            }
            drawText(layout, topLeft = tl)
        }
        for (k in keys) {
            if (k.black) continue
            val f = fingers[k.midi] ?: continue
            drawFinger(measurer, f, Offset(k.rect.center.x, k.rect.bottom - k.rect.height * 0.36f), k.rect.width * 0.36f)
        }
        // Black keys
        for (k in keys) {
            if (!k.black) continue
            val isHeld = k.midi in held
            val isDemo = k.midi in demo
            val hl = highlights[k.midi]
            val base = when {
                isHeld -> handColors[k.midi] ?: colors.keyBlackPressed
                isDemo -> handColors[k.midi]?.let { lerp(colors.keyBlack, it, 0.75f) } ?: lerp(colors.keyBlack, colors.keyDemo, 0.7f)
                hl != null -> lerp(colors.keyBlack, hl, 0.8f)
                else -> colors.keyBlack
            }
            drawBlackKey(k.rect, base, isHeld || isDemo)
            val text = labelFor(k.midi, labels, highlights, customLabels, solfege)
            if (text != null) {
                val fsDp = (k.rect.width / density * 0.36f * labelBoost).coerceIn(8f, 14f * labelBoost)
                val layout = measurer.measure(text, TextStyle(fontSize = (fsDp / fontScale).sp, fontWeight = FontWeight.Bold,
                    color = if (hl != null) Color(0xFF1B1640) else Color.White))
                drawText(layout, topLeft = Offset(k.rect.center.x - layout.size.width / 2f, k.rect.bottom - layout.size.height - 6f))
            }
            fingers[k.midi]?.let { drawFinger(measurer, it, Offset(k.rect.center.x, k.rect.bottom - k.rect.height * 0.45f), k.rect.width * 0.42f) }
        }
        // Felt strip at the top
        drawRect(Brush.verticalGradient(listOf(Color(0xFF8A2A22), Color(0xFF4E140F))), Offset.Zero, Size(size.width.toFloat(), 5f))
    }
}

private fun labelFor(midi: Int, mode: KeyLabels, highlights: Map<Int, Color>, custom: Map<Int, String>, solfege: Boolean): String? {
    custom[midi]?.let { return it }
    val black = isBlackKey(midi)
    return when (mode) {
        KeyLabels.NONE -> null
        KeyLabels.HIGHLIGHTED -> if (highlights.containsKey(midi)) keyLabel(midi, solfege) else null
        KeyLabels.C_ONLY -> if (midi.mod(12) == 0) keyLabel(midi, solfege, withOctave = true) else if (highlights.containsKey(midi)) keyLabel(midi, solfege) else null
        KeyLabels.WHITE -> if (!black) keyLabel(midi, solfege, withOctave = midi.mod(12) == 0) else if (highlights.containsKey(midi)) keyLabel(midi, solfege) else null
        KeyLabels.ALL -> keyLabel(midi, solfege, withOctave = midi.mod(12) == 0)
        KeyLabels.COLORED -> if (!black) keyLabel(midi, solfege, withOctave = true) else null
    }
}

private fun DrawScope.drawWhiteKey(r: Rect, color: Color, radius: Float, pressed: Boolean, dark: Boolean) {
    val inset = 1.2f
    val rect = Rect(r.left + inset, r.top, r.right - inset, r.bottom - if (pressed) 0f else 3f)
    // Shadow / key edge
    drawRoundRect(Color(0x33000000), Offset(rect.left, rect.top), Size(rect.width, r.height), CornerRadius(radius))
    val path = Path().apply {
        moveTo(rect.left, rect.top)
        lineTo(rect.right, rect.top)
        lineTo(rect.right, rect.bottom - radius)
        quadraticTo(rect.right, rect.bottom, rect.right - radius, rect.bottom)
        lineTo(rect.left + radius, rect.bottom)
        quadraticTo(rect.left, rect.bottom, rect.left, rect.bottom - radius)
        close()
    }
    val bottomShade = if (dark) lerp(color, Color.Black, 0.12f) else lerp(color, Color(0xFFDCD8EA), 0.5f)
    drawPath(path, Brush.verticalGradient(listOf(color, color, bottomShade), startY = rect.top, endY = rect.bottom))
    drawPath(path, Color(0x22000000), style = Stroke(1f))
}

private fun DrawScope.drawBlackKey(r: Rect, color: Color, pressed: Boolean) {
    val radius = r.width * 0.14f
    drawRoundRect(Color(0x55000000), Offset(r.left + 2f, r.top), Size(r.width, r.height + 3f), CornerRadius(radius))
    drawRoundRect(
        Brush.verticalGradient(listOf(lerp(color, Color.White, 0.12f), color, lerp(color, Color.Black, 0.2f)), startY = r.top, endY = r.bottom),
        Offset(r.left, r.top), Size(r.width, r.height - if (pressed) 0f else 2f), CornerRadius(radius),
    )
    if (!pressed) {
        drawRoundRect(Color(0x30FFFFFF), Offset(r.left + r.width * 0.18f, r.bottom - r.height * 0.14f), Size(r.width * 0.64f, r.height * 0.08f), CornerRadius(radius))
    }
}

private fun DrawScope.drawFinger(measurer: androidx.compose.ui.text.TextMeasurer, finger: Int, center: Offset, diameter: Float) {
    val d = diameter.coerceIn(14f, 36f)
    drawCircle(Color(0xFF4B3BD1), d / 2, center)
    val layout = measurer.measure(finger.toString(), TextStyle(fontSize = (d * 0.6f / density / fontScale).sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
    drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}

/** A static (non-interactive) keyboard drawing that fills its box - used for chord diagrams. */
@Composable
fun MiniKeyboard(startMidi: Int, endMidi: Int, highlights: Map<Int, Color>, modifier: Modifier = Modifier, labels: KeyLabels = KeyLabels.HIGHLIGHTED) {
    PianoKeyboard(startMidi, endMidi, modifier, highlights = highlights, labels = labels, interactive = false)
}
