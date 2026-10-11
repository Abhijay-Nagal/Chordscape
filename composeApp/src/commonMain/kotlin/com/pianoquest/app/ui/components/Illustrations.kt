package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.Illustration
import com.pianoquest.app.ui.theme.PQ
import com.pianoquest.app.ui.theme.Wood

private val SKIN = Color(0xFFE8B996)
private val SKIN_SHADE = Color(0xFFCF9A76)
private val SKIN_EDGE = Color(0xFFA9714F)
private val HAIR = Color(0xFF4A2E1E)
private val SHIRT = Color(0xFF6B3C21)
private val TROUSERS = Color(0xFF2E3A4A)
private val IVORY = Color(0xFFFBF4E4)
private val EBONY = Color(0xFF1B140F)

/** Drawn teaching pictures (posture, hand shape, pedals ...) with labelled pointers. Width : height = 1.6 : 1. */
@Composable
fun IllustrationView(kind: Illustration, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val labelBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val accent = MaterialTheme.colorScheme.primary
    val pq = PQ.colors
    Canvas(modifier.fillMaxWidth().aspectRatio(1.6f)) {
        val u = size.width / 1.6f // one unit of the 1.6 x 1.0 drawing box
        val ctx = Ctx(this, measurer, u, ink, labelBg, accent)
        when (kind) {
            Illustration.POSTURE -> ctx.posture()
            Illustration.HAND_SHAPE -> ctx.handShape(armWeight = false)
            Illustration.ARM_WEIGHT -> ctx.handShape(armWeight = true)
            Illustration.HIGH_LOW -> ctx.highLow()
            Illustration.BEAT -> ctx.beat()
            Illustration.TIE -> ctx.tie()
            Illustration.TOUCH -> ctx.touch(pq.keyRight)
            Illustration.PEDALS -> ctx.pedals()
        }
    }
}

private class Ctx(val d: DrawScope, val m: TextMeasurer, val u: Float, val ink: Color, val labelBg: Color, val accent: Color) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun sz(w: Float, h: Float) = Size(w * u, h * u)

    fun text(s: String, x: Float, y: Float, size: Float = 0.052f, bold: Boolean = false, color: Color = ink, center: Boolean = false, maxW: Float = 0.6f) {
        val style = TextStyle(fontSize = (size * u / d.density / d.fontScale).sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = color, textAlign = if (center) TextAlign.Center else TextAlign.Start)
        val l = m.measure(s, style, constraints = Constraints(maxWidth = (maxW * u).toInt().coerceAtLeast(1)))
        val tl = Offset(x * u - if (center) l.size.width / 2f else 0f, y * u - l.size.height / 2f)
        d.drawText(l, topLeft = tl)
    }

    /** A label in a rounded box at (lx, ly) with a pointer line to (ax, ay). */
    fun label(s: String, ax: Float, ay: Float, lx: Float, ly: Float, maxW: Float = 0.52f) {
        val style = TextStyle(fontSize = (0.046f * u / d.density / d.fontScale).sp, fontWeight = FontWeight.SemiBold, color = ink)
        val l = m.measure(s, style, constraints = Constraints(maxWidth = (maxW * u).toInt().coerceAtLeast(1)))
        val pad = 0.014f * u
        val tl = Offset(lx * u, ly * u - l.size.height / 2f)
        val box = Size(l.size.width + pad * 2, l.size.height + pad)
        // Pointer from the nearest edge of the box to the anchor.
        val bx = (ax * u).coerceIn(tl.x, tl.x + box.width)
        val by = (ay * u).coerceIn(tl.y - pad / 2, tl.y - pad / 2 + box.height)
        d.drawLine(accent, Offset(bx, by), p(ax, ay), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
        d.drawCircle(accent, 0.012f * u, p(ax, ay))
        d.drawRoundRect(labelBg, Offset(tl.x - pad, tl.y - pad / 2), Size(box.width, box.height), CornerRadius(0.02f * u))
        d.drawRoundRect(accent.copy(alpha = 0.6f), Offset(tl.x - pad, tl.y - pad / 2), Size(box.width, box.height), CornerRadius(0.02f * u),
            style = Stroke(0.004f * u))
        d.drawText(l, topLeft = Offset(tl.x, tl.y))
    }

    fun limb(a: Offset, b: Offset, width: Float, color: Color) {
        d.drawLine(color, a, b, strokeWidth = width * u, cap = StrokeCap.Round)
    }

    fun arrow(from: Offset, to: Offset, color: Color, width: Float = 0.012f) {
        d.drawLine(color, from, to, strokeWidth = width * u, cap = StrokeCap.Round)
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val ux = dx / len
        val uy = dy / len
        val head = 0.045f * u
        val left = Offset(to.x - ux * head - uy * head * 0.6f, to.y - uy * head + ux * head * 0.6f)
        val right = Offset(to.x - ux * head + uy * head * 0.6f, to.y - uy * head - ux * head * 0.6f)
        d.drawPath(Path().apply { moveTo(to.x, to.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close() }, color)
    }

    // ------------------------------------------------------------------ Sitting at the piano
    fun posture() = with(d) {
        val floor = 0.86f
        drawRect(Wood.Sand.copy(alpha = 0.45f), p(0f, floor), sz(1.6f, 1f - floor))
        // Piano (side view): body, key bed and keys, pedal.
        drawRoundRect(Brush.verticalGradient(listOf(Wood.Chestnut, Wood.Walnut)), p(1.28f, 0.08f), sz(0.27f, floor - 0.08f), CornerRadius(0.02f * u))
        drawRect(Wood.Mahogany, p(1.0f, 0.53f), sz(0.3f, 0.07f))
        drawRect(IVORY, p(1.0f, 0.505f), sz(0.3f, 0.03f))
        drawRect(EBONY, p(1.12f, 0.49f), sz(0.16f, 0.018f))
        drawRect(Wood.Walnut, p(1.24f, 0.6f), sz(0.04f, floor - 0.6f))
        drawRoundRect(Wood.Brass, p(1.02f, floor - 0.022f), sz(0.12f, 0.02f), CornerRadius(0.01f * u))
        // Bench.
        drawRoundRect(Wood.Caramel, p(0.26f, 0.6f), sz(0.48f, 0.055f), CornerRadius(0.012f * u))
        drawRect(Wood.Chestnut, p(0.29f, 0.655f), sz(0.035f, floor - 0.655f))
        drawRect(Wood.Chestnut, p(0.67f, 0.655f), sz(0.035f, floor - 0.655f))
        // Pianist: on the front half of the bench, forearms level with the keys, feet flat.
        val hip = p(0.58f, 0.575f)
        val knee = p(0.86f, 0.585f)
        val ankle = p(0.88f, floor - 0.035f)
        limb(hip, knee, 0.085f, TROUSERS)
        limb(knee, ankle, 0.07f, TROUSERS)
        drawRoundRect(EBONY, p(0.85f, floor - 0.045f), sz(0.19f, 0.045f), CornerRadius(0.02f * u))
        val shoulder = p(0.6f, 0.28f)
        limb(hip, shoulder, 0.13f, SHIRT)
        drawCircle(SKIN, 0.026f * u, p(0.605f, 0.21f)) // neck
        drawCircle(SKIN, 0.075f * u, p(0.62f, 0.15f))
        drawArc(HAIR, 180f, 200f, true, p(0.545f, 0.07f), sz(0.15f, 0.12f))
        drawCircle(EBONY, 0.008f * u, p(0.665f, 0.145f))
        val elbow = p(0.7f, 0.46f)
        val wrist = p(0.98f, 0.475f)
        limb(shoulder, elbow, 0.06f, SHIRT)
        limb(elbow, wrist, 0.05f, SKIN)
        // Hand curving down onto the keys.
        val path = Path().apply {
            moveTo(wrist.x, wrist.y - 0.02f * u)
            quadraticTo(1.06f * u, 0.43f * u, 1.1f * u, 0.5f * u)
            lineTo(1.08f * u, 0.505f * u)
            quadraticTo(1.03f * u, 0.47f * u, wrist.x, wrist.y + 0.022f * u)
            close()
        }
        drawPath(path, SKIN)
        drawPath(path, SKIN_EDGE, style = Stroke(0.004f * u))
        // Level line from forearm to keys.
        drawLine(accent.copy(alpha = 0.7f), p(0.72f, 0.505f), p(1.3f, 0.505f), strokeWidth = 0.004f * u,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(0.02f * u, 0.015f * u)))
        label("Shoulders relaxed, back tall", 0.56f, 0.3f, 0.03f, 0.1f, maxW = 0.32f)
        label("Elbows a little in front of your body", 0.7f, 0.47f, 0.03f, 0.46f, maxW = 0.32f)
        label("Sit on the front half of the bench", 0.66f, 0.61f, 0.03f, 0.78f, maxW = 0.36f)
        label("Forearms level with the keys", 0.86f, 0.47f, 0.82f, 0.04f, maxW = 0.44f)
        label("Feet flat - right foot near the pedal", 1.0f, floor - 0.02f, 1.02f, 0.95f, maxW = 0.55f)
    }

    // ------------------------------------------------------------------ Hand shape / arm weight
    fun handShape(armWeight: Boolean) = with(d) {
        // A white key seen from the side, with a black key behind.
        drawRect(Wood.Walnut, p(0f, 0.82f), sz(1.6f, 0.18f))
        drawRoundRect(IVORY, p(0.05f, 0.72f), sz(1.5f, 0.1f), CornerRadius(0.01f * u))
        drawRoundRect(EBONY, p(0.95f, 0.62f), sz(0.6f, 0.1f), CornerRadius(0.01f * u))
        // Forearm and hand (pianist on the left, fingers reaching to the right).
        val wrist = p(0.5f, 0.44f)
        limb(p(-0.05f, 0.42f), wrist, 0.12f, SKIN)
        val back = Path().apply {
            moveTo(0.45f * u, 0.38f * u)
            cubicTo(0.6f * u, 0.3f * u, 0.8f * u, 0.3f * u, 0.9f * u, 0.36f * u)  // top of the hand up to the knuckle
            cubicTo(1.0f * u, 0.4f * u, 1.08f * u, 0.5f * u, 1.1f * u, 0.7f * u)  // finger curving down
            lineTo(1.05f * u, 0.715f * u)                                           // fingertip pad on the key
            cubicTo(1.02f * u, 0.55f * u, 0.96f * u, 0.47f * u, 0.86f * u, 0.46f * u) // underside of the finger
            cubicTo(0.75f * u, 0.47f * u, 0.62f * u, 0.52f * u, 0.47f * u, 0.5f * u)  // palm
            close()
        }
        drawPath(back, SKIN)
        drawPath(back, SKIN_EDGE, style = Stroke(0.005f * u))
        // Thumb resting on its side.
        limb(p(0.6f, 0.5f), p(0.78f, 0.66f), 0.05f, SKIN_SHADE)
        // The "bubble" under the palm.
        drawCircle(accent.copy(alpha = 0.16f), 0.11f * u, p(0.76f, 0.58f))
        drawCircle(accent.copy(alpha = 0.55f), 0.11f * u, p(0.76f, 0.58f), style = Stroke(0.005f * u))
        drawCircle(Color.White.copy(alpha = 0.7f), 0.02f * u, p(0.72f, 0.54f))
        if (!armWeight) {
            drawLine(accent.copy(alpha = 0.7f), p(0.1f, 0.44f), p(0.7f, 0.44f), strokeWidth = 0.004f * u,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(0.02f * u, 0.015f * u)))
            label("Rounded hand - as if holding a small bubble", 0.76f, 0.58f, 0.03f, 0.9f, maxW = 0.6f)
            label("Play on the fingertip pad", 1.08f, 0.71f, 1.12f, 0.9f, maxW = 0.42f)
            label("Wrist level - not drooping, not lifted", 0.35f, 0.44f, 0.03f, 0.18f, maxW = 0.45f)
            label("Knuckles stay up", 0.9f, 0.35f, 0.95f, 0.12f, maxW = 0.4f)
        } else {
            // Weight flows from the relaxed arm through a firm fingertip into the key.
            arrow(p(0.05f, 0.25f), p(0.45f, 0.36f), accent)
            arrow(p(0.92f, 0.38f), p(1.06f, 0.66f), accent)
            label("Relaxed arm - let its weight sink", 0.15f, 0.27f, 0.03f, 0.09f, maxW = 0.45f)
            label("Firm fingertip carries the weight into the key", 1.07f, 0.7f, 0.9f, 0.9f, maxW = 0.62f)
            label("Free, loose wrist", 0.5f, 0.44f, 0.03f, 0.9f, maxW = 0.4f)
        }
    }

    // ------------------------------------------------------------------ High and low
    fun highLow() = with(d) {
        val whites = 22
        val x0 = 0.05f
        val w = 1.5f / whites
        for (i in 0 until whites) {
            drawRoundRect(IVORY, p(x0 + i * w, 0.48f), sz(w - 0.004f, 0.38f), CornerRadius(0.008f * u))
            drawRoundRect(ink.copy(alpha = 0.25f), p(x0 + i * w, 0.48f), sz(w - 0.004f, 0.38f), CornerRadius(0.008f * u), style = Stroke(0.003f * u))
        }
        for (i in 0 until whites - 1) {
            val n = i % 7
            if (n == 2 || n == 6) continue
            drawRoundRect(EBONY, p(x0 + (i + 1) * w - w * 0.3f, 0.48f), sz(w * 0.6f, 0.23f), CornerRadius(0.006f * u))
        }
        arrow(p(0.78f, 0.3f), p(0.12f, 0.3f), Color(0xFF5E8C7A), 0.016f)
        arrow(p(0.82f, 0.3f), p(1.48f, 0.3f), Color(0xFFC8813A), 0.016f)
        text("Lower - deeper sound", 0.42f, 0.18f, size = 0.06f, bold = true, color = Color(0xFF5E8C7A), center = true, maxW = 0.75f)
        text("Higher - brighter sound", 1.18f, 0.18f, size = 0.06f, bold = true, color = Color(0xFFC8813A), center = true, maxW = 0.75f)
        text("◀ left", 0.1f, 0.93f, size = 0.05f)
        text("right ▶", 1.38f, 0.93f, size = 0.05f)
    }

    // ------------------------------------------------------------------ The beat
    fun beat() = with(d) {
        // Metronome.
        val body = Path().apply { moveTo(0.12f * u, 0.88f * u); lineTo(0.42f * u, 0.88f * u); lineTo(0.33f * u, 0.12f * u); lineTo(0.21f * u, 0.12f * u); close() }
        drawPath(body, Brush.verticalGradient(listOf(Wood.Caramel, Wood.Chestnut)))
        drawPath(Path().apply { moveTo(0.18f * u, 0.7f * u); lineTo(0.36f * u, 0.7f * u); lineTo(0.31f * u, 0.22f * u); lineTo(0.23f * u, 0.22f * u); close() }, IVORY)
        limb(p(0.27f, 0.68f), p(0.33f, 0.24f), 0.012f, EBONY)
        drawRoundRect(Wood.Brass, p(0.295f, 0.36f), sz(0.05f, 0.05f), CornerRadius(0.008f * u))
        text("♩ = 60 BPM", 0.27f, 0.95f, size = 0.05f, bold = true, center = true)
        // Even beats.
        val labels = listOf("1", "2", "3", "4", "1", "2", "3", "4")
        for (i in 0 until 8) {
            val x = 0.6f + i * 0.125f
            val strong = i % 4 == 0
            drawCircle(if (strong) accent else accent.copy(alpha = 0.55f), (if (strong) 0.045f else 0.033f) * u, p(x, 0.48f))
            text(labels[i], x, 0.6f, size = 0.055f, bold = strong, center = true)
        }
        drawLine(ink.copy(alpha = 0.35f), p(1.075f, 0.36f), p(1.075f, 0.62f), strokeWidth = 0.005f * u)
        text("A steady pulse - every beat the same distance apart", 1.04f, 0.2f, size = 0.05f, center = true, maxW = 0.95f)
        text("Tap your foot on every beat", 1.04f, 0.8f, size = 0.05f, center = true, maxW = 0.95f)
    }

    // ------------------------------------------------------------------ Tie vs slur
    fun tie() = with(d) {
        fun staff(x: Float, w: Float) {
            for (i in 0 until 5) drawLine(ink, p(x, 0.3f + i * 0.06f), p(x + w, 0.3f + i * 0.06f), strokeWidth = 0.004f * u)
        }
        fun note(x: Float, y: Float, half: Boolean) {
            val r = Size(0.075f * u, 0.052f * u)
            val tl = Offset(x * u - r.width / 2, y * u - r.height / 2)
            if (half) {
                drawOval(ink, tl, r, style = Stroke(0.012f * u))
            } else drawOval(ink, tl, r)
            drawLine(ink, Offset(x * u + r.width / 2 - 0.004f * u, y * u), Offset(x * u + r.width / 2 - 0.004f * u, (y - 0.2f) * u), strokeWidth = 0.007f * u)
        }
        staff(0.05f, 0.7f)
        drawLine(ink, p(0.4f, 0.3f), p(0.4f, 0.54f), strokeWidth = 0.006f * u)
        note(0.25f, 0.42f, true); note(0.56f, 0.42f, true)
        drawArc(accent, 20f, 140f, false, p(0.25f, 0.43f), sz(0.31f, 0.12f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
        text("TIE", 0.4f, 0.12f, size = 0.07f, bold = true, center = true, color = accent)
        text("Same pitch: play once, hold for both", 0.4f, 0.75f, size = 0.05f, center = true, maxW = 0.7f)
        staff(0.85f, 0.7f)
        note(1.0f, 0.48f, false); note(1.2f, 0.42f, false); note(1.4f, 0.36f, false)
        drawArc(accent, 20f, 140f, false, p(0.98f, 0.42f), sz(0.44f, 0.2f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
        text("SLUR", 1.2f, 0.12f, size = 0.07f, bold = true, center = true, color = accent)
        text("Different pitches: play smoothly", 1.2f, 0.75f, size = 0.05f, center = true, maxW = 0.7f)
    }

    // ------------------------------------------------------------------ Touch sensitivity
    fun touch(loud: Color) = with(d) {
        fun key(x: Float, pressed: Boolean) {
            drawRect(Wood.Walnut, p(x, 0.8f), sz(0.5f, 0.06f))
            val y = if (pressed) 0.7f else 0.66f
            drawRoundRect(IVORY, p(x, y), sz(0.5f, 0.1f), CornerRadius(0.01f * u))
            drawRoundRect(ink.copy(alpha = 0.3f), p(x, y), sz(0.5f, 0.1f), CornerRadius(0.01f * u), style = Stroke(0.004f * u))
        }
        key(0.12f, true)
        key(0.98f, true)
        arrow(p(0.37f, 0.38f), p(0.37f, 0.6f), accent.copy(alpha = 0.6f), 0.01f)
        arrow(p(1.23f, 0.12f), p(1.23f, 0.6f), accent, 0.024f)
        // Sound waves: small for soft, large for loud.
        for (i in 1..2) drawArc(accent.copy(alpha = 0.5f), -40f, 80f, false, p(0.55f + i * 0.02f, 0.5f - i * 0.04f), sz(0.08f * i, 0.08f * i),
            style = Stroke(0.006f * u))
        for (i in 1..4) drawArc(loud, -40f, 80f, false, p(1.41f - 0.02f, 0.45f - i * 0.06f), sz(0.12f * i * 0.8f, 0.12f * i),
            style = Stroke(0.01f * u))
        text("Slow, gentle press", 0.37f, 0.94f, size = 0.05f, center = true, maxW = 0.7f)
        text("= soft  (p)", 0.37f, 0.2f, size = 0.06f, bold = true, center = true)
        text("Fast, firm press", 1.23f, 0.94f, size = 0.05f, center = true, maxW = 0.7f)
        text("= loud  (f)", 1.0f, 0.08f, size = 0.06f, bold = true, center = true)
    }

    // ------------------------------------------------------------------ The three pedals
    fun pedals() = with(d) {
        // Lyre (the frame that holds the pedals).
        drawRoundRect(Brush.verticalGradient(listOf(Wood.Chestnut, Wood.Walnut)), p(0.25f, 0.08f), sz(1.1f, 0.42f), CornerRadius(0.04f * u))
        drawRect(Wood.Mahogany, p(0.3f, 0.5f), sz(1.0f, 0.06f))
        val names = listOf("Soft (una corda)", "Sostenuto", "Sustain (damper)")
        val feet = listOf("left foot", "", "right foot - the one you use most")
        val xs = listOf(0.42f, 0.8f, 1.18f)
        for (i in 0..2) {
            val main = i == 2
            val c = if (main) Wood.Gold else Wood.Brass
            drawRoundRect(c, p(xs[i] - 0.06f, 0.52f), sz(0.12f, 0.24f), CornerRadius(0.04f * u))
            if (main) drawRoundRect(accent, p(xs[i] - 0.07f, 0.51f), sz(0.14f, 0.26f), CornerRadius(0.045f * u), style = Stroke(0.01f * u))
            text(names[i], xs[i], 0.83f, size = 0.048f, bold = main, center = true, maxW = 0.42f)
            if (feet[i].isNotEmpty()) text(feet[i], xs[i], 0.94f, size = 0.04f, center = true, color = ink.copy(alpha = 0.7f), maxW = 0.5f)
        }
        text("Grand piano pedals", 0.8f, 0.28f, size = 0.06f, bold = true, center = true, color = IVORY)
    }
}
