package com.pianoquest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.animateScrollBy
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pianoquest.app.ui.theme.woodGrain

/** Inline formatting: **bold**, *italic*, `highlight`. */
@Composable
fun inlineMarkdown(text: String): AnnotatedString {
    val accent = MaterialTheme.colorScheme.primary
    val chip = MaterialTheme.colorScheme.primaryContainer
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end < 0) { append(text.substring(i)); break }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                }
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end < 0) { append(text.substring(i)); break }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accent, background = chip)) { append(" ${text.substring(i + 1, end)} ") }
                    i = end + 1
                }
                text[i] == '*' && i + 1 < text.length && text[i + 1] != ' ' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end < 0) { append(text.substring(i)); break }
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                }
                else -> { append(text[i]); i++ }
            }
        }
    }
}

/** Paragraphs, "- " bullets, "1. " numbered items, "## " sub-headings and "> " tip boxes. */
@Composable
fun RichText(text: String, modifier: Modifier = Modifier) {
    val blocks = remember(text) { richBlocks(text) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) { blocks.forEach { it() } }
}

/**
 * Splits rich text into independent blocks (paragraphs, list items, tips). Very long paragraphs are split at sentence
 * boundaries so that [PagedRichText] can always break pages between blocks.
 */
fun richBlocks(text: String): List<@Composable () -> Unit> {
    val out = ArrayList<@Composable () -> Unit>()
    val isItem = { l: String -> l.startsWith("- ") || l.startsWith("• ") || Regex("^\\d+\\. ").containsMatchIn(l) }
    fun paragraph(p: String) {
        for (chunk in splitSentences(p, 260)) out += { Text(inlineMarkdown(chunk), style = MaterialTheme.typography.bodyLarge) }
    }
    for (block in text.trim().split(Regex("\n\\s*\n"))) {
        val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) continue
        when {
            lines.first().startsWith("## ") -> {
                val head = lines.first().removePrefix("## ")
                out += { Text(inlineMarkdown(head), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                if (lines.size > 1) paragraph(lines.drop(1).joinToString(" "))
            }
            lines.first().startsWith("> ") -> {
                val tip = lines.joinToString(" ") { it.removePrefix("> ") }
                out += {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💡")
                            Text(inlineMarkdown(tip), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
            else -> {
                // Mixed blocks: list lines become separate items, other consecutive lines a paragraph.
                var i = 0
                while (i < lines.size) {
                    if (isItem(lines[i])) {
                        val l = lines[i]
                        val num = Regex("^(\\d+)\\. ").find(l)?.groupValues?.get(1)
                        val body = l.replace(Regex("^(- |• |\\d+\\. )"), "")
                        out += {
                            Row {
                                Text(if (num != null) "$num." else "•", Modifier.width(22.dp), style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(inlineMarkdown(body), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        i++
                    } else {
                        val start = i
                        while (i < lines.size && !isItem(lines[i])) i++
                        paragraph(lines.subList(start, i).joinToString(" "))
                    }
                }
            }
        }
    }
    return out
}

private fun splitSentences(p: String, max: Int): List<String> {
    if (p.length <= max) return listOf(p)
    val sentences = Regex("(?<=[.!?])\\s+(?=[A-Z*`(\"])").split(p)
    val out = ArrayList<String>()
    val cur = StringBuilder()
    for (sn in sentences) {
        if (cur.isNotEmpty() && cur.length + sn.length + 1 > max) { out += cur.toString(); cur.clear() }
        if (cur.isNotEmpty()) cur.append(' ')
        cur.append(sn)
    }
    if (cur.isNotEmpty()) out += cur.toString()
    return out
}

/**
 * Shows rich text one page at a time - no scrolling. Blocks are measured at the available size and packed into pages;
 * [onPageCount] reports how many pages there are, [page] selects the one to show.
 */
@Composable
fun PagedRichText(text: String, page: Int, onPageCount: (Int) -> Unit, modifier: Modifier = Modifier) {
    val blocks = remember(text) { richBlocks(text) }
    val gap = with(LocalDensity.current) { 8.dp.roundToPx() }
    SubcomposeLayout(modifier) { constraints ->
        val c = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val placeables = blocks.mapIndexed { i, b -> subcompose(i, b).map { it.measure(c) } }
        val heights = placeables.map { ps -> ps.sumOf { it.height } }
        val maxH = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
        val pages = ArrayList<IntRange>()
        var first = 0
        var used = 0
        for (i in blocks.indices) {
            val need = heights[i] + if (i > first) gap else 0
            if (i > first && used + need > maxH) {
                pages += first until i
                first = i
                used = heights[i]
            } else used += need
        }
        if (blocks.isNotEmpty()) pages += first until blocks.size
        onPageCount(pages.size.coerceAtLeast(1))
        val range = pages.getOrNull(page.coerceIn(0, (pages.size - 1).coerceAtLeast(0))) ?: IntRange.EMPTY
        val shownH = range.sumOf { heights[it] } + (range.count() - 1).coerceAtLeast(0) * gap
        layout(constraints.maxWidth, if (constraints.hasBoundedHeight) constraints.maxHeight else shownH) {
            var y = 0
            for (i in range) {
                for (p in placeables[i]) { p.place(0, y); y += p.height }
                y += gap
            }
        }
    }
}

/**
 * Lays out [content] at its natural height; if that is taller than the space available it is scaled down to fit,
 * so lesson pages never need scrolling. Content must not contain vertically scrolling children.
 */
@Composable
fun FitToHeight(modifier: Modifier = Modifier, minScale: Float = 0.6f, alignment: Alignment = Alignment.TopCenter, content: @Composable () -> Unit) {
    Layout(content, modifier.clipToBounds()) { measurables, constraints ->
        val probe = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val maxH = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
        val placeables = measurables.map { it.measure(probe) }
        val h = placeables.maxOfOrNull { it.height } ?: 0
        val scale = if (h > maxH && h > 0) (maxH.toFloat() / h).coerceIn(minScale, 1f) else 1f
        val w = if (constraints.hasBoundedWidth) constraints.maxWidth else placeables.maxOfOrNull { it.width } ?: 0
        val outH = if (constraints.hasBoundedHeight) constraints.maxHeight else (h * scale).toInt()
        layout(w, outH) {
            for (p in placeables) {
                val pw = (p.width * scale).toInt()
                val ph = (p.height * scale).toInt()
                val off = alignment.align(IntSize(pw, ph), IntSize(w, outH), layoutDirection)
                // Scaling happens around the centre, so shift by half the shrinkage.
                p.placeWithLayer(off.x - (p.width - pw) / 2, off.y - (p.height - ph) / 2) { scaleX = scale; scaleY = scale }
            }
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        } else Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        actions()
    }
}

@Composable
fun ProgressRing(progress: Float, size: Dp, stroke: Dp = 6.dp, color: Color = MaterialTheme.colorScheme.primary,
                 track: Color = MaterialTheme.colorScheme.surfaceVariant, content: @Composable () -> Unit = {}) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700))
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val sw = stroke.toPx()
            drawArc(track, 0f, 360f, false, Offset(sw / 2, sw / 2), Size(this.size.width - sw, this.size.height - sw), style = Stroke(sw))
            drawArc(color, -90f, 360f * p, false, Offset(sw / 2, sw / 2), Size(this.size.width - sw, this.size.height - sw),
                style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun LinearMeter(progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, height: Dp = 8.dp) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600))
    Box(modifier.height(height).clip(RoundedCornerShape(height)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxWidth(p).height(height).clip(RoundedCornerShape(height)).background(color))
    }
}

@Composable
fun Pill(text: String, color: Color = MaterialTheme.colorScheme.primaryContainer, contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
         icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Surface(color = color, contentColor = contentColor, shape = CircleShape, modifier = modifier) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) Icon(icon, null, Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, color: Color = MaterialTheme.colorScheme.surface,
         border: Boolean = true, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (border) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = color, shape = shape,
    ) { content() }
}

@Composable
fun GradientBanner(colors: List<Color>, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.clip(shape).woodGrain(colors.first(), seed = colors.first().hashCode())
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides com.pianoquest.app.ui.theme.Wood.Ivory) { content() }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(horizontal = 10.dp, vertical = 6.dp))
        }
    }
}

@Composable
fun Stars(count: Int, max: Int = 3, size: Dp = 16.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(max) { i ->
            Text(if (i < count) "★" else "☆", color = if (i < count) com.pianoquest.app.ui.theme.Wood.Gold else MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = androidx.compose.ui.unit.TextUnit(size.value, androidx.compose.ui.unit.TextUnitType.Sp)))
        }
    }
}

fun Long.toColor() = Color(this)


/**
 * A horizontally scrolling row that shows the user there is more to see: the edge fades and a "More ›" button
 * appears while there are hidden items (tapping it scrolls on).
 */
@Composable
fun ScrollHintRow(modifier: Modifier = Modifier, spacing: Dp = 8.dp, content: @Composable RowScope.() -> Unit) {
    val state = androidx.compose.foundation.rememberScrollState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val bg = MaterialTheme.colorScheme.background
    Box(modifier) {
        Row(Modifier.horizontalScroll(state).padding(end = if (state.canScrollForward) 72.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically, content = content)
        if (state.canScrollBackward) {
            Box(Modifier.align(Alignment.CenterStart).width(28.dp).height(40.dp)
                .background(Brush.horizontalGradient(listOf(bg, bg.copy(alpha = 0f)))))
        }
        if (state.canScrollForward) {
            Row(Modifier.align(Alignment.CenterEnd).height(40.dp)
                .background(Brush.horizontalGradient(listOf(bg.copy(alpha = 0f), bg, bg), endX = 60f))
                .padding(start = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(CircleShape).clickable {
                        scope.launch { state.animateScrollBy(state.viewportSize * 0.75f) }
                    }) {
                    Text("More ›", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
