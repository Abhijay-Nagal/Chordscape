package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Both hands seen from above, resting palm-down as on the keys, with the standard finger numbers
 * (thumb = 1 ... little finger = 5 on both hands). [highlight] marks one finger on both hands.
 */
@Composable
fun HandsDiagram(highlight: Int?, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ink = MaterialTheme.colorScheme.onSurface
    val badge = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.fillMaxWidth().height(200.dp)) {
        val labelSpace = 22.dp.toPx()
        // Hand geometry is defined in a unit box (x 0..1, y 0..1.25); 1 unit = handW pixels.
        val handW = minOf(size.width / 2.5f, (size.height - labelSpace) / 1.36f)
        val top = (size.height - labelSpace - handW * 1.28f) / 2 + handW * 0.06f
        val gap = (size.width - 2 * handW) / 3
        drawHand(Offset(gap, top), handW, mirrored = true, highlight, accent)
        drawHand(Offset(gap * 2 + handW, top), handW, mirrored = false, highlight, accent)
        // Finger-number badges and titles (drawn without the mirror so the text reads normally).
        for ((ox, mirrored) in listOf(gap to true, gap * 2 + handW to false)) {
            for (f in FINGERS) {
                val tip = f.tip()
                val p = Offset(ox + (if (mirrored) 1 - tip.x else tip.x) * handW, top + tip.y * handW)
                val r = handW * 0.062f
                val on = highlight == f.number
                drawCircle(if (on) accent else badge, r, p)
                drawCircle(if (on) accent else ink.copy(alpha = 0.35f), r, p, style = Stroke(1.2f))
                val l = measurer.measure(f.number.toString(), TextStyle(fontSize = (r * 1.15f / density / fontScale).sp, fontWeight = FontWeight.Bold,
                    color = if (on) onAccent else ink))
                drawText(l, topLeft = Offset(p.x - l.size.width / 2f, p.y - l.size.height / 2f))
            }
            val title = measurer.measure(if (mirrored) "Left hand" else "Right hand",
                TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ink))
            drawText(title, topLeft = Offset(ox + handW / 2 - title.size.width / 2f, size.height - title.size.height))
        }
    }
}

private val SKIN_LIGHT = Color(0xFFF4D2B6)
private val SKIN = Color(0xFFE8B996)
private val SKIN_SHADE = Color(0xFFCF9A76)
private val SKIN_EDGE = Color(0xFFA9714F)
private val NAIL = Color(0xFFF9E3D6)

/** One finger in hand units: base centre, direction (degrees from straight up, positive = towards the little finger), length, width. */
private class Finger(val number: Int, val bx: Float, val by: Float, val angle: Float, val length: Float, val width: Float) {
    val dx get() = sin(angle * PI.toFloat() / 180f)
    val dy get() = -cos(angle * PI.toFloat() / 180f)
    fun tip(): Offset = Offset(bx + dx * (length + 0.075f), by + dy * (length + 0.075f))
}

// A right hand (thumb on the left). The left hand is its mirror image.
private val FINGERS = listOf(
    Finger(1, 0.235f, 0.74f, -47f, 0.34f, 0.158f),
    Finger(2, 0.315f, 0.47f, -6f, 0.38f, 0.150f),
    Finger(3, 0.475f, 0.43f, -1f, 0.43f, 0.154f),
    Finger(4, 0.630f, 0.46f, 5f, 0.395f, 0.146f),
    Finger(5, 0.765f, 0.535f, 12f, 0.30f, 0.126f),
)

private fun DrawScope.drawHand(origin: Offset, w: Float, mirrored: Boolean, highlight: Int?, accent: Color) {
    fun pt(x: Float, y: Float) = Offset(origin.x + (if (mirrored) 1 - x else x) * w, origin.y + y * w)
    val edge = Stroke(width = (w * 0.012f).coerceAtLeast(1.2f))

    // Fingers first, so the back of the hand covers their bases.
    for (f in FINGERS) {
        val path = fingerPath(f, ::pt)
        val tipPt = pt(f.bx + f.dx * f.length, f.by + f.dy * f.length)
        val basePt = pt(f.bx, f.by)
        drawPath(path, Brush.linearGradient(listOf(SKIN_LIGHT, SKIN, SKIN_SHADE), start = tipPt, end = basePt))
        if (highlight == f.number) drawPath(path, accent.copy(alpha = 0.45f))
        drawPath(path, SKIN_EDGE, style = edge)
        // Knuckle creases and the fingernail.
        val n = Offset(-f.dy, f.dx)
        for (t in listOf(0.38f, 0.66f)) {
            val c = Offset(f.bx + f.dx * f.length * t, f.by + f.dy * f.length * t)
            val half = f.width * 0.26f
            drawLine(SKIN_EDGE.copy(alpha = 0.45f), pt(c.x - n.x * half, c.y - n.y * half), pt(c.x + n.x * half, c.y + n.y * half),
                strokeWidth = w * 0.007f)
        }
        val nailC = Offset(f.bx + f.dx * f.length * 0.9f, f.by + f.dy * f.length * 0.9f)
        drawPath(fingerPath(Finger(f.number, nailC.x - f.dx * f.length * 0.1f, nailC.y - f.dy * f.length * 0.1f, f.angle, f.length * 0.17f, f.width * 0.58f), ::pt),
            NAIL)
    }

    // Back of the hand and wrist.
    val palm = Path().apply {
        val p0 = pt(0.30f, 1.25f)
        moveTo(p0.x, p0.y)
        curveTo(pt(0.29f, 1.05f), pt(0.18f, 0.98f), pt(0.15f, 0.84f))           // wrist to thumb root
        curveTo(pt(0.13f, 0.72f), pt(0.22f, 0.58f), pt(0.25f, 0.50f))           // thumb web
        curveTo(pt(0.30f, 0.43f), pt(0.40f, 0.40f), pt(0.47f, 0.40f))           // knuckles: index - middle
        curveTo(pt(0.56f, 0.40f), pt(0.64f, 0.42f), pt(0.70f, 0.46f))           // middle - ring
        curveTo(pt(0.76f, 0.49f), pt(0.82f, 0.52f), pt(0.83f, 0.58f))           // little finger knuckle
        curveTo(pt(0.86f, 0.72f), pt(0.82f, 0.92f), pt(0.76f, 1.02f))           // outer edge
        curveTo(pt(0.73f, 1.08f), pt(0.72f, 1.16f), pt(0.72f, 1.25f))           // wrist
        close()
    }
    drawPath(palm, Brush.verticalGradient(listOf(SKIN, SKIN_LIGHT, SKIN), startY = origin.y + 0.4f * w, endY = origin.y + 1.25f * w))
    drawPath(palm, SKIN_EDGE, style = edge)
    // Soft knuckle highlights and tendon hints.
    for (f in FINGERS.drop(1)) {
        val k = pt(f.bx, f.by + 0.025f)
        drawOval(Color.White.copy(alpha = 0.22f), Offset(k.x - w * 0.045f, k.y - w * 0.022f), Size(w * 0.09f, w * 0.044f))
        drawLine(SKIN_SHADE.copy(alpha = 0.18f), pt(f.bx, f.by + 0.08f), pt(0.5f + (f.bx - 0.5f) * 0.4f, 1.0f), strokeWidth = w * 0.008f)
    }
    // Sleeve cuff so the wrist does not end abruptly.
    val cuffTop = origin.y + 1.17f * w
    val cuffL = pt(0.27f, 1.17f).x
    val cuffR = pt(0.75f, 1.17f).x
    drawRoundRect(Color(0xFF6B3C21), Offset(minOf(cuffL, cuffR), cuffTop), Size(kotlin.math.abs(cuffR - cuffL), w * 0.10f),
        androidx.compose.ui.geometry.CornerRadius(w * 0.03f))
}

/** A finger outline: slightly tapered, with a rounded tip. */
private fun fingerPath(f: Finger, pt: (Float, Float) -> Offset): Path {
    val nX = -f.dy
    val nY = f.dx
    val half = f.width / 2
    val tipHalf = half * 0.84f
    val bodyLen = f.length - tipHalf
    fun at(along: Float, across: Float) = pt(f.bx + f.dx * along + nX * across, f.by + f.dy * along + nY * across)
    return Path().apply {
        val a = at(-0.06f, -half)
        moveTo(a.x, a.y)
        val b1 = at(bodyLen * 0.5f, -half * 1.04f); val b2 = at(bodyLen, -tipHalf)
        quadraticTo(b1.x, b1.y, b2.x, b2.y)
        val c1 = at(f.length + tipHalf * 0.15f, -tipHalf); val c2 = at(f.length + tipHalf * 0.15f, tipHalf); val c3 = at(bodyLen, tipHalf)
        cubicTo(c1.x, c1.y, c2.x, c2.y, c3.x, c3.y)
        val d1 = at(bodyLen * 0.5f, half * 1.04f); val d2 = at(-0.06f, half)
        quadraticTo(d1.x, d1.y, d2.x, d2.y)
        close()
    }
}

private fun Path.curveTo(c1: Offset, c2: Offset, end: Offset) = cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
