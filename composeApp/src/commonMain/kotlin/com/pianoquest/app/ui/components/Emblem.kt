package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.ui.theme.Wood

/** The app emblem: a brass medallion with ivory and ebony keys, framed in walnut. */
@Composable
fun AppEmblem(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2
        val c = Offset(this.size.width / 2, this.size.height / 2)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFE9C27A), Wood.Brass, Color(0xFF7A5420)), center = c.copy(y = c.y - r * 0.3f), radius = r * 1.3f), r, c)
        drawCircle(Wood.Walnut, r * 0.80f, c)
        drawCircle(Color(0x55E9C27A), r * 0.80f, c, style = Stroke(r * 0.04f))
        // Five ivory keys with three ebony keys.
        val kw = r * 0.26f
        val kh = r * 0.72f
        val left = c.x - kw * 2.5f
        val top = c.y - kh * 0.42f
        for (i in 0 until 5) {
            drawRoundRect(Color(0xFFFBF4E4), Offset(left + i * kw + kw * 0.06f, top), Size(kw * 0.88f, kh), CornerRadius(kw * 0.12f))
        }
        for (i in listOf(1, 2, 4)) {
            drawRoundRect(Color(0xFF110C09), Offset(left + i * kw - kw * 0.3f, top), Size(kw * 0.6f, kh * 0.6f), CornerRadius(kw * 0.08f))
        }
        drawRect(Wood.Felt, Offset(left, top - r * 0.05f), Size(kw * 5, r * 0.05f))
    }
}

/** A small brass badge with an icon, used for lesson headings and feedback. */
@Composable
fun MoodBadge(mood: MascotMood, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val (icon, tint) = moodIcon(mood)
    Box(
        modifier.size(size).clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFFF0D9A0), Wood.Brass)))
            .border(1.5.dp, Wood.Chestnut.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

private fun moodIcon(mood: MascotMood): Pair<ImageVector, Color> = when (mood) {
    MascotMood.TEACHING -> Icons.AutoMirrored.Filled.MenuBook to Wood.Walnut
    MascotMood.THINKING -> Icons.Default.Lightbulb to Wood.Walnut
    MascotMood.EXCITED -> Icons.Default.EmojiEvents to Wood.Walnut
    MascotMood.SAD -> Icons.Default.Close to Wood.Burgundy
    MascotMood.HAPPY, MascotMood.WAVING -> Icons.Default.MusicNote to Wood.Walnut
}

/** Feedback badge: a tick or a cross. */
@Composable
fun ResultBadge(correct: Boolean, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(if (correct) Color(0xFF4E7A3A) else Wood.Burgundy),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (correct) Icons.Default.Check else Icons.Default.Close, null, tint = Wood.Ivory, modifier = Modifier.size(size * 0.6f))
    }
}

/** A parchment note card for tips and hints. */
@Composable
fun TipCard(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
        }
    }
}
