package com.pianoquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.audio.Player
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.Notation
import com.pianoquest.app.data.Visual
import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.RomanNumerals
import com.pianoquest.app.ui.theme.PQ
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Renders any lesson [Visual]. */
@Composable
fun VisualView(visual: Visual, modifier: Modifier = Modifier, solfege: Boolean = false) {
    when (visual) {
        is Visual.Keyboard -> KeyboardVisual(visual, modifier, solfege)
        is Visual.Staff -> Column(modifier) {
            StaffView(visual.notes, clef = visual.clef, keySig = visual.keySig, timeSig = visual.timeSig, showNames = visual.showNames,
                labels = visual.labels, solfege = solfege, space = if (visual.clef == Clef.GRAND) 7.dp else 8.5.dp)
            visual.caption?.let { Caption(it) }
        }
        is Visual.CircleOfFifths -> CircleOfFifths(visual.highlight, modifier.widthIn(max = 360.dp).fillMaxWidth())
        is Visual.Rhythm -> RhythmVisual(visual, modifier)
        is Visual.Hands -> HandsDiagram(visual.highlightFinger, modifier)
        is Visual.Chords -> ChordDiagrams(visual.symbols, visual.numerals, modifier)
        is Visual.Progression -> ProgressionVisual(visual.numerals, visual.key, modifier)
        is Visual.Table -> TableVisual(visual, modifier)
        is Visual.Symbols -> SymbolsVisual(visual, modifier)
        is Visual.Image -> ImageVisual(visual, modifier)
        is Visual.Composers -> ComposerCards(visual.items, modifier)
        is Visual.Picture -> Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            IllustrationView(visual.kind, Modifier.fillMaxWidth())
            visual.caption?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun Caption(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(top = 6.dp), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
}

@Composable
private fun KeyboardVisual(v: Visual.Keyboard, modifier: Modifier, solfege: Boolean) {
    val c = PQ.colors
    val start = Pitch.parse(v.from).midi
    val end = Pitch.parse(v.to).midi
    val hl = HashMap<Int, Color>()
    v.secondary.forEach { hl[Pitch.parse(it).midi] = c.keyLeft }
    v.highlights.forEach { hl[Pitch.parse(it).midi] = c.keyTarget }
    val whites = (start..end).count { !com.pianoquest.app.theory.isBlackKey(it) }
    Column(modifier) {
        PianoKeyboard(
            start, end,
            Modifier.fillMaxWidth().height((if (whites > 22) 120 else if (whites > 14) 140 else 160).dp).clip(RoundedCornerShape(10.dp)),
            highlights = hl,
            labels = v.labels,
            customLabels = v.customLabels.mapKeys { Pitch.parse(it.key).midi },
            fingers = v.fingers.mapKeys { Pitch.parse(it.key).midi },
            solfege = solfege,
        )
        v.caption?.let { Caption(it) }
    }
}

// ------------------------------------------------------------------------------------------ Circle of fifths

private val minorDisplay = listOf("Am", "Em", "Bm", "F♯m", "C♯m", "G♯m", "D♯m", "B♭m", "Fm", "Cm", "Gm", "Dm")
private val majorDisplay = listOf("C", "G", "D", "A", "E", "B", "F♯/G♭", "D♭", "A♭", "E♭", "B♭", "F")
private val sigDisplay = listOf("", "1♯", "2♯", "3♯", "4♯", "5♯", "6♯/6♭", "5♭", "4♭", "3♭", "2♭", "1♭")

/** Interactive circle of fifths: tap a key to select it. */
@Composable
fun CircleOfFifths(highlight: String?, modifier: Modifier = Modifier, selected: Int? = null, onSelect: ((Int, Boolean) -> Unit)? = null) {
    val measurer = rememberTextMeasurer()
    val scheme = MaterialTheme.colorScheme
    val hlIndex = selected ?: highlight?.let { h ->
        val major = Key.CIRCLE_MAJOR.indexOf(h.removeSuffix("m").takeIf { !h.endsWith("m") } ?: "")
        if (major >= 0) major else Key.CIRCLE_MINOR.indexOf(h.removeSuffix("m"))
    }
    Canvas(
        modifier.aspectRatio(1f).then(if (onSelect != null) Modifier.pointerInput(Unit) {
            detectTapGestures { p ->
                val cx = size.width / 2f
                val cy = size.height / 2f
                val dist = hypot(p.x - cx, p.y - cy) / (size.width / 2f)
                if (dist > 1f || dist < 0.25f) return@detectTapGestures
                var ang = atan2(p.y - cy, p.x - cx) + PI / 2 + PI / 12
                if (ang < 0) ang += 2 * PI
                val idx = ((ang / (2 * PI)) * 12).toInt() % 12
                onSelect(idx, dist < 0.62f)
            }
        } else Modifier)
    ) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 0 until 12) {
            val startAngle = -90f - 15f + i * 30f
            val isHl = hlIndex == i
            val outer = if (isHl) scheme.primary else if (i % 2 == 0) scheme.primaryContainer else scheme.surfaceContainerHigh
            val inner = if (isHl) scheme.secondary else if (i % 2 == 0) scheme.surfaceContainerHigh else scheme.primaryContainer.copy(alpha = 0.6f)
            drawArc(outer, startAngle, 30f, true, Offset(center.x - r, center.y - r), Size(2 * r, 2 * r))
            drawArc(inner, startAngle, 30f, true, Offset(center.x - r * 0.62f, center.y - r * 0.62f), Size(2 * r * 0.62f, 2 * r * 0.62f))
        }
        drawCircle(scheme.surface, r * 0.3f, center)
        for (i in 0 until 12) {
            val a = (-90.0 + i * 30.0) * PI / 180
            val isHl = hlIndex == i
            fun label(text: String, rr: Float, size: Float, color: Color, bold: Boolean = true) {
                val layout = measurer.measure(text, TextStyle(fontSize = (size / density / fontScale).sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color))
                drawText(layout, topLeft = Offset(center.x + (cos(a) * rr).toFloat() - layout.size.width / 2f, center.y + (sin(a) * rr).toFloat() - layout.size.height / 2f))
            }
            label(majorDisplay[i], r * 0.85f, r * 0.11f, if (isHl) scheme.onPrimary else scheme.onPrimaryContainer)
            label(sigDisplay[i], r * 0.71f, r * 0.065f, if (isHl) scheme.onPrimary else scheme.onSurfaceVariant, bold = false)
            label(minorDisplay[i], r * 0.47f, r * 0.08f, if (isHl) scheme.onSecondary else scheme.onSurface)
        }
        val mid = measurer.measure("5ths →", TextStyle(fontSize = (r * 0.07f / density / fontScale).sp, color = scheme.onSurfaceVariant))
        drawText(mid, topLeft = Offset(center.x - mid.size.width / 2f, center.y - mid.size.height / 2f))
    }
}

// ------------------------------------------------------------------------------------------ Rhythm

@Composable
private fun RhythmVisual(v: Visual.Rhythm, modifier: Modifier) {
    val notes = v.pattern.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ") { tok ->
        if (tok.startsWith("R")) "R:${tok.removePrefix("R").removePrefix(":").ifEmpty { "q" }}" else "B4:$tok"
    }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var idx by remember { mutableStateOf<Int?>(null) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        StaffView(notes, timeSig = v.timeSig, highlightIndex = idx, labels = v.counts?.split(" ") ?: emptyList())
        FilledTonalButton(onClick = {
            if (job?.isActive == true) { job?.cancel(); return@FilledTonalButton }
            job = scope.launch {
                val pattern = v.pattern.split(Regex("\\s+")).filter { it.isNotEmpty() }
                var t = 0.0
                val evs = ArrayList<com.pianoquest.app.data.NoteEvent>()
                for (tok in pattern) {
                    val d = Notation.parseDuration(tok.removePrefix("R").removePrefix(":").ifEmpty { "q" })
                    if (!tok.startsWith("R")) evs += com.pianoquest.app.data.NoteEvent(Pitch.parse("C5"), t, d * 0.8, com.pianoquest.app.data.Hand.RIGHT)
                    t += d
                }
                // Map onset groups back to pattern indices (rests included) for highlighting.
                val onsetToIndex = ArrayList<Int>()
                pattern.forEachIndexed { i, tok -> if (!tok.startsWith("R")) onsetToIndex += i }
                Player.play(evs, 80.0) { g -> idx = if (g < 0) null else onsetToIndex.getOrNull(g) }
            }
        }) {
            Icon(if (job?.isActive == true) Icons.Default.Stop else Icons.Default.PlayArrow, null)
            Text(" Hear the rhythm")
        }
    }
}

// ------------------------------------------------------------------------------------------ Chords

@Composable
fun ChordDiagrams(symbols: List<String>, numerals: List<String> = emptyList(), modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val c = PQ.colors
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        symbols.forEachIndexed { i, sym ->
            val chord = remember(sym) { Chord.parseOrNull(sym) } ?: return@forEachIndexed
            val midis = chord.midis(4)
            val bassMidi = chord.bass?.let { chord.bassMidi() }
            val lo = minOf(midis.min(), bassMidi ?: 999).let { if (it % 12 == 0) it else it - (it % 12) }
            val hi = maxOf(midis.max(), lo + 11)
            Surface(
                Modifier.width(150.dp).clip(RoundedCornerShape(14.dp)).clickable { scope.launch { Player.playChord(chord) } },
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    numerals.getOrNull(i)?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
                    Text(chord.prettySymbol, style = MaterialTheme.typography.titleMedium)
                    val hl = midis.associateWith { c.keyTarget } + (bassMidi?.let { mapOf(it to c.keyLeft) } ?: emptyMap())
                    PianoKeyboard(minOf(lo, bassMidi ?: lo), hi, Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(6.dp)),
                        highlights = hl, labels = KeyLabels.HIGHLIGHTED, interactive = false)
                    Text(chord.pitches().joinToString(" ") { it.pretty }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("tap to hear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun ProgressionVisual(numerals: String, keyName: String, modifier: Modifier = Modifier) {
    val key = remember(keyName) { Key.parse(keyName) }
    val chords = remember(numerals, keyName) { RomanNumerals.progression(numerals, key) }
    val nums = numerals.trim().split(Regex("[\\s\\-–]+")).filter { it.isNotEmpty() }
    val scope = rememberCoroutineScope()
    var playing by remember { mutableStateOf<Job?>(null) }
    var current by remember { mutableStateOf(-1) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("In ${key.name}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chords.forEachIndexed { i, ch ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (current == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (current == i) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { scope.launch { Player.playChord(ch) } },
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nums.getOrElse(i) { "" }, style = MaterialTheme.typography.labelLarge)
                        Text(ch.prettySymbol, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        FilledTonalButton(onClick = {
            if (playing?.isActive == true) { playing?.cancel(); current = -1; return@FilledTonalButton }
            playing = scope.launch {
                try {
                    repeat(2) {
                        chords.forEachIndexed { i, ch ->
                            current = i
                            Player.playChord(ch, seconds = 1.1)
                        }
                    }
                } finally { current = -1 }
            }
        }) {
            Icon(if (playing?.isActive == true) Icons.Default.Stop else Icons.Default.PlayArrow, null)
            Text(" Play progression")
        }
    }
}

/**
 * A table that uses the full width: columns are sized by their content and text wraps inside them, so every column is
 * visible without sideways scrolling. Only on very narrow screens does it fall back to scrolling.
 */
@Composable
private fun TableVisual(v: Visual.Table, modifier: Modifier) {
    val rows = listOf(v.header) + v.rows
    val cols = v.header.indices
    // Weight of each column: its longest cell (within limits), so short columns stay narrow.
    val weights = cols.map { col -> rows.maxOf { it.getOrElse(col) { "" }.length }.coerceIn(5, 34).toFloat() }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
            val minWidth = (weights.sum() * 5.5f).dp
            val scrolls = maxWidth < minWidth * 0.75f
            Column((if (scrolls) Modifier.horizontalScroll(rememberScrollState()).width(minWidth) else Modifier.fillMaxWidth()).padding(4.dp)) {
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))) {
                    v.header.forEachIndexed { i, h ->
                        Text(h, Modifier.weight(weights[i]).padding(horizontal = 8.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                v.rows.forEachIndexed { r, row ->
                    Row(Modifier.fillMaxWidth().background(if (r % 2 == 1) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent)) {
                        cols.forEach { i ->
                            Text(inlineMarkdown(row.getOrElse(i) { "" }), Modifier.weight(weights[i]).padding(horizontal = 8.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SymbolsVisual(v: Visual.Symbols, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        v.items.forEach { (sym, meaning) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(92.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center) {
                    Text(sym, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold)
                }
                Text(inlineMarkdown(meaning), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Composable
fun ImageVisual(v: Visual.Image, modifier: Modifier = Modifier) {
    val res = LessonImages.resource(v.key) ?: return
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(10.dp), color = com.pianoquest.app.ui.theme.Wood.Walnut, shadowElevation = 3.dp) {
            androidx.compose.foundation.Image(
                org.jetbrains.compose.resources.painterResource(res), v.caption,
                Modifier.padding(5.dp).heightIn(max = 280.dp).clip(RoundedCornerShape(6.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            )
        }
        Caption(v.caption)
    }
}

@Composable
fun ComposerCards(items: List<com.pianoquest.app.data.ComposerCard>, modifier: Modifier = Modifier) {
    val nav = com.pianoquest.app.ui.LocalNavigator.current
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { c ->
            Surface(Modifier.width(176.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 2.dp) {
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LessonImages.resource(c.image)?.let { res ->
                        androidx.compose.foundation.Image(
                            org.jetbrains.compose.resources.painterResource(res), c.name,
                            Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                        )
                    }
                    Text(c.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                    Text(c.years, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (c.note.isNotBlank()) Text(c.note, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                    if (c.songId != null && com.pianoquest.app.data.SongLibrary.get(c.songId) != null) {
                        androidx.compose.material3.TextButton(onClick = { nav.openSong(c.songId) }) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp)); Text(" Hear a piece")
                        }
                    }
                }
            }
        }
    }
}
