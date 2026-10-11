package com.pianoquest.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.audio.Player
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.Notation
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.data.Step
import com.pianoquest.app.input.InputSource
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.nowMillis
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.isBlackKey
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.components.AppEmblem
import com.pianoquest.app.ui.components.MoodBadge
import com.pianoquest.app.ui.components.ResultBadge
import com.pianoquest.app.ui.components.TipCard
import com.pianoquest.app.ui.components.PianoKeyboard
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.components.StaffEl
import com.pianoquest.app.ui.components.StaffView
import com.pianoquest.app.ui.components.Stars
import com.pianoquest.app.ui.components.VisualView
import com.pianoquest.app.ui.components.FitToHeight
import com.pianoquest.app.ui.components.PagedRichText
import com.pianoquest.app.data.Visual
import com.pianoquest.app.ui.components.keyLabel
import com.pianoquest.app.ui.theme.PQ
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * Whether the octave matters for a keyboard exercise. Usually it does not - any C counts as C, so a MIDI keyboard or a
 * different part of the screen keyboard works too. It does matter when the exercise is about register: it asks for
 * middle C, octaves, high or low notes, or uses the same note name in different octaves.
 */
internal fun octaveMatters(midis: Collection<Int>, prompt: String, forceAny: Boolean = false): Boolean {
    if (forceAny) return false
    val p = prompt.lowercase()
    if (listOf("middle c", "octave", "highest", "lowest", "higher", "lower", " high ", " low ", "inversion", "voicing").any { it in " $p " }) return true
    return midis.groupBy { it.mod(12) }.values.any { g -> g.distinct().size > 1 }
}

/** Play/stop toggle for a notation snippet. */
@Composable
fun ListenButton(notes: String, bpm: Int, label: String = "Listen", autoPlay: Boolean = false, onIndex: (Int) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var job by remember(notes) { mutableStateOf<Job?>(null) }
    var playing by remember(notes) { mutableStateOf(false) }
    fun start() {
        job?.cancel()
        playing = true
        job = scope.launch {
            try { Player.playNotation(notes, bpm, onIndex) } finally { playing = false }
        }
    }
    LaunchedEffect(notes) { if (autoPlay) { delay(350); start() } }
    FilledTonalButton(onClick = { if (playing) { job?.cancel(); playing = false } else start() }) {
        Icon(if (playing) Icons.Default.Stop else Icons.Default.Headphones, null, Modifier.size(18.dp))
        Text("  " + if (playing) "Stop" else label)
    }
}

/**
 * A lesson keyboard that fills the space it is given (pinned to the bottom of the step), so every key is visible
 * without scrolling - including the keys you press on a connected MIDI keyboard.
 */
@Composable
fun LessonKeyboard(from: String, to: String, highlights: Map<Int, Color>, fingers: Map<Int, Int> = emptyMap(), labels: KeyLabels = KeyLabels.C_ONLY,
                   solfege: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth().height(170.dp)) {
    var start = Pitch.parse(from).midi
    var end = Pitch.parse(to).midi
    if (end - start < 11) { start -= (12 - (end - start)) / 2; end = start + 11 }
    if (isBlackKey(start)) start--
    if (isBlackKey(end)) end++
    PianoKeyboard(start, end, modifier.clip(RoundedCornerShape(12.dp)), highlights = highlights, fingers = fingers, labels = labels, solfege = solfege)
}

/**
 * Prompt and extras on top, keyboard pinned to the bottom. The keyboard takes the space the text leaves (between a
 * minimum and a comfortable height); only if that is still not enough is the text scaled down. Nothing is ever drawn
 * underneath the keys.
 */
@Composable
private fun KeyboardStepLayout(top: @Composable () -> Unit, keyboard: @Composable (Modifier) -> Unit) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.ui.layout.SubcomposeLayout(Modifier.fillMaxSize()) { constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val gap = with(density) { 8.dp.roundToPx() }
        val minKb = with(density) { 100.dp.roundToPx() }.coerceAtMost(h / 2)
        // The keyboard takes the room the prompt leaves - on tablets that means big, comfortable keys, not empty space.
        val prefKb = with(density) { (h.toDp() * 0.72f).coerceIn(110.dp, 420.dp).roundToPx() }
        val topNatural = subcompose("probe") { top() }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity)) }
        val topH = topNatural.maxOfOrNull { it.height } ?: 0
        val kbH = (h - topH - gap).coerceIn(minKb, maxOf(minKb, prefKb))
        val topSpace = (h - kbH - gap).coerceAtLeast(0)
        val topPlace = subcompose("top") {
            FitToHeight(Modifier.fillMaxWidth().height(with(density) { topSpace.toDp() }).clipToBounds(), minScale = 0.4f,
                alignment = Alignment.TopStart) { top() }
        }.map { it.measure(androidx.compose.ui.unit.Constraints.fixed(w, topSpace)) }
        val kb = subcompose("kb") { keyboard(Modifier.fillMaxWidth().height(with(density) { kbH.toDp() })) }
            .map { it.measure(androidx.compose.ui.unit.Constraints.fixed(w, kbH)) }
        layout(w, h) {
            topPlace.forEach { it.place(0, 0) }
            kb.forEach { it.place(0, h - kbH) }
        }
    }
}

/** Typography scaled up, for short pages that would otherwise leave the screen half empty. */
@Composable
private fun Roomy(factor: Float, content: @Composable () -> Unit) {
    if (factor <= 1.01f) { content(); return }
    val t = MaterialTheme.typography
    fun androidx.compose.ui.text.TextStyle.up() = copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)
    MaterialTheme(typography = t.copy(bodyLarge = t.bodyLarge.up(), bodyMedium = t.bodyMedium.up(), titleMedium = t.titleMedium.up(),
        headlineSmall = t.headlineSmall.up()), content = content)
}

@Composable
fun ReadStepView(step: Step.Read, solfege: Boolean, page: Int = 0, onPageCount: (Int) -> Unit = {}) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = isWideLesson(maxWidth, maxHeight)
        val hasSide = step.visual != null || step.play != null
        // Tables read best at full width under the text; short texts get a larger font to use the space.
        val tableBelow = step.visual is Visual.Table
        // Larger text when there is room for it: pages that are mostly text get more, pages with a picture a little.
        val roomy = when {
            !hasSide && step.body.length < 700 -> 1.22f
            step.body.length < 160 -> 1.22f
            step.body.length < 360 -> 1.12f
            else -> 1f
        }
        val title = @Composable {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MoodBadge(step.mood, 36.dp)
                Text(step.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f), maxLines = 2)
            }
        }
        val side = @Composable { m: Modifier ->
            FitToHeight(m, alignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    step.visual?.let { VisualView(it, Modifier.fillMaxWidth(), solfege) }
                    step.play?.let { ListenButton(it, step.playBpm) }
                }
            }
        }
        Roomy(if (tableBelow) 1f else roomy) {
            if (tableBelow) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    title()
                    if (step.body.isNotBlank()) RichText(step.body)
                    onPageCount(1)
                    side(Modifier.fillMaxWidth().weight(1f))
                }
            } else if (wide && hasSide) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        title()
                        if (step.body.isNotBlank()) PagedRichText(step.body, page, onPageCount, Modifier.fillMaxWidth().weight(1f))
                        else onPageCount(1)
                    }
                    side(Modifier.weight(if (step.visual is Visual.Composers) 1.25f else 1f).fillMaxHeight())
                }
            } else if (wide || !hasSide) {
                Column(Modifier.fillMaxSize().widthIn(max = 900.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    title()
                    if (step.body.isNotBlank()) PagedRichText(step.body, page, onPageCount, Modifier.fillMaxWidth().weight(1f))
                    else onPageCount(1)
                }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    title()
                    if (step.body.isNotBlank()) PagedRichText(step.body, page, onPageCount, Modifier.fillMaxWidth().weight(1f))
                    else onPageCount(1)
                    side(Modifier.fillMaxWidth().weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuizStepView(step: Step.Quiz, result: Boolean?, showAnswers: Boolean, solfege: Boolean, onResult: (Boolean) -> Unit) {
    val options = remember(step) { step.options.shuffled(Random(step.question.hashCode() * 31 + step.options.hashCode())) }
    val correct = step.options.first()
    var chosen by remember(step) { mutableStateOf<String?>(null) }
    val c = PQ.colors
    val optionView = @Composable { opt: String, m: Modifier ->
        val isChosen = chosen == opt
        val reveal = result != null && showAnswers
        val bg by animateColorAsState(when {
            reveal && opt == correct -> c.success.copy(alpha = 0.18f)
            isChosen && result == false -> c.error.copy(alpha = 0.16f)
            isChosen -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        })
        val border = when {
            reveal && opt == correct -> c.success
            isChosen && result == false -> c.error
            isChosen -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        }
        Row(
            m.clip(RoundedCornerShape(14.dp)).background(bg).border(2.dp, border, RoundedCornerShape(14.dp))
                .clickable(enabled = result == null) {
                    chosen = opt
                    onResult(opt == correct)
                }.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(opt, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (reveal && opt == correct) Icon(Icons.Default.Check, null, tint = c.success)
            else if (isChosen && result == false) Icon(Icons.Default.Close, null, tint = c.error)
        }
    }
    val hasSide = step.visual != null || step.play != null
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = isWideLesson(maxWidth, maxHeight)
        val question = @Composable {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(step.question, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth())
                step.visual?.let { VisualView(it, Modifier.fillMaxWidth(), solfege) }
                step.play?.let { ListenButton(it, step.playBpm, if (step.visual == null) "Play sound" else "Listen", autoPlay = step.visual == null) }
            }
        }
        if (wide && hasSide) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                FitToHeight(Modifier.weight(1.1f).fillMaxHeight(), alignment = Alignment.TopCenter) { question() }
                FitToHeight(Modifier.weight(0.9f).fillMaxHeight(), alignment = Alignment.Center) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { options.forEach { optionView(it, Modifier.fillMaxWidth()) } }
                }
            }
        } else {
            FitToHeight(Modifier.fillMaxSize()) {
                Column(Modifier.widthIn(max = 900.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    question()
                    // Two columns of answers keep everything on one screen.
                    options.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { optionView(it, Modifier.weight(1f)) }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TapKeysStepView(step: Step.TapKeys, result: Boolean?, solfege: Boolean, onResult: (Boolean) -> Unit) {
    val targets = remember(step) { step.targets.map { Pitch.parse(it) } }
    val targetMidis = targets.map { it.midi }
    val collected = remember(step) { mutableStateListOf<Int>() }
    var mistakes by remember(step) { mutableIntStateOf(0) }
    var lastWrong by remember(step) { mutableStateOf<Int?>(null) }
    val c = PQ.colors
    val strictOctave = remember(step) { octaveMatters(targetMidis, step.prompt, step.anyOctave) }
    LaunchedEffect(step, result) {
        if (result != null) return@LaunchedEffect
        PianoInput.events.collect { e ->
            if (!e.down || e.source == InputSource.DEMO) return@collect
            val match = targetMidis.firstOrNull { it == e.midi }
                ?: if (strictOctave) null
                else targetMidis.firstOrNull { it.mod(12) == e.midi.mod(12) && it !in collected } ?: targetMidis.firstOrNull { it.mod(12) == e.midi.mod(12) }
            if (match != null) {
                lastWrong = null
                if (match !in collected) collected += match
                if (step.matchAny || collected.size == targetMidis.size) onResult(mistakes <= (if (targets.size > 1) 1 else 0))
            } else {
                mistakes++
                lastWrong = e.midi
                if (mistakes >= 3) onResult(false)
            }
        }
    }
    val highlights = HashMap<Int, Color>()
    val revealAll = step.showHint || result == false
    if (revealAll) targetMidis.forEach { highlights[it] = c.keyTarget }
    collected.forEach { m -> highlights[m] = c.success }
    lastWrong?.let { highlights[it] = c.error }
    KeyboardStepLayout(top = {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.TouchApp, null, tint = MaterialTheme.colorScheme.primary)
                    Text(step.prompt, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                }
                val status = when {
                    lastWrong != null && result == null -> "That was ${keyLabel(lastWrong!!, solfege)} - try again!"
                    targets.size > 1 && !step.matchAny -> "${collected.size} / ${targets.size} notes found"
                    strictOctave -> "Tap the key - or play it on your MIDI keyboard."
                    else -> "Tap the key - any octave counts. You can also use a MIDI keyboard."
                }
                Text(status, style = MaterialTheme.typography.bodyMedium,
                    color = if (lastWrong != null && result == null) c.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (step.staff != null) {
                Box(Modifier.width(220.dp)) {
                    StaffView(targets.joinToString("+") { it.name } + ":w", clef = step.staff, space = if (step.staff == Clef.GRAND) 6.dp else 8.dp,
                        showNames = result != null)
                }
            }
        }
    }) { m -> LessonKeyboard(step.from, step.to, highlights, solfege = solfege, modifier = m) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayAlongStepView(step: Step.PlayAlong, result: Boolean?, solfege: Boolean, onResult: (Boolean) -> Unit) {
    val groups = remember(step) { Notation.parse(step.notes).groupBy { it.start }.toSortedMap().values.map { g -> g.map { it.midi }.distinct() } }
    var gi by remember(step) { mutableIntStateOf(0) }
    val got = remember(step) { mutableStateListOf<Int>() }
    var mistakes by remember(step) { mutableIntStateOf(0) }
    var lastWrong by remember(step) { mutableStateOf<Int?>(null) }
    var demoIndex by remember(step) { mutableStateOf(-1) }
    var attempt by remember(step) { mutableIntStateOf(0) }
    val c = PQ.colors
    val strictOctave = remember(step) { octaveMatters(groups.flatten(), step.prompt) }
    LaunchedEffect(step, attempt, result) {
        if (result != null) return@LaunchedEffect
        PianoInput.events.collect { e ->
            if (!e.down || e.source == InputSource.DEMO || gi >= groups.size) return@collect
            val expected = groups[gi]
            // The written note, or (when the octave doesn't matter) the same note name in any octave.
            val matched = if (e.midi in expected) e.midi
                else if (strictOctave) null
                else expected.firstOrNull { it.mod(12) == e.midi.mod(12) && it !in got } ?: expected.firstOrNull { it.mod(12) == e.midi.mod(12) }
            if (matched != null) {
                lastWrong = null
                if (matched !in got) got += matched
                if (got.containsAll(expected)) {
                    got.clear()
                    gi++
                    if (gi >= groups.size) onResult(mistakes <= maxOf(1, groups.size / 5))
                }
            } else {
                mistakes++
                lastWrong = e.midi
            }
        }
    }
    val highlights = HashMap<Int, Color>()
    val showIndex = if (demoIndex >= 0) demoIndex else gi
    if ((step.hints || mistakes >= 2) && showIndex < groups.size) groups[showIndex].forEach { highlights[it] = c.keyTarget }
    got.forEach { highlights[it] = c.success }
    lastWrong?.let { highlights[it] = c.error }
    val fingerMap = if (gi < groups.size && step.fingers.size > gi && groups[gi].size == 1) mapOf(groups[gi][0] to step.fingers[gi]) else emptyMap()

    KeyboardStepLayout(top = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                    Text(step.prompt, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ListenButton(step.notes, step.bpm, "Hear it", onIndex = { demoIndex = it })
                    if (result == null && (gi > 0 || mistakes > 0)) {
                        OutlinedButton(onClick = { gi = 0; got.clear(); mistakes = 0; lastWrong = null; attempt++ }) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Text(" Restart")
                        }
                    }
                    Text("${gi.coerceAtMost(groups.size)} / ${groups.size}", style = MaterialTheme.typography.labelLarge)
                }
                if (lastWrong != null && result == null) Text("Oops, that's ${keyLabel(lastWrong!!, solfege)}.", color = c.error,
                    style = MaterialTheme.typography.bodyMedium)
            }
            Box(Modifier.weight(1.1f)) {
                if (step.staff != null) {
                    val els = remember(step) { com.pianoquest.app.ui.components.staffElements(step.notes).filter { it !is StaffEl.Bar } }
                    StaffView("", clef = step.staff, elements = els, highlightIndex = showIndex.coerceAtMost(groups.size - 1),
                        noteColors = (0 until gi).associateWith { c.success }, space = if (step.staff == Clef.GRAND) 5.5.dp else 7.dp,
                        solfege = solfege, showNames = result != null)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        groups.forEachIndexed { i, g ->
                            val done = i < gi
                            val cur = i == showIndex
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when { done -> c.success.copy(alpha = 0.2f); cur -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.surfaceVariant },
                                contentColor = if (cur && !done) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            ) {
                                Column(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(g.joinToString("+") { keyLabel(it, solfege) }, fontWeight = FontWeight.Bold)
                                    step.fingers.getOrNull(i)?.let { Text("finger $it", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }) { m -> LessonKeyboard(step.from, step.to, highlights, fingerMap, solfege = solfege, modifier = m) }
}

@Composable
fun RhythmStepView(step: Step.RhythmTap, result: Boolean?, onResult: (Boolean) -> Unit) {
    val tokens = remember(step) { step.pattern.split(Regex("\\s+")).filter { it.isNotBlank() } }
    val durs = tokens.map { Notation.parseDuration(it.removePrefix("R").ifEmpty { "q" }) }
    val rests = tokens.map { it.startsWith("R") }
    val onsets = remember(step) {
        var t = 0.0
        tokens.indices.mapNotNull { i -> val s = t; t += durs[i]; if (rests[i]) null else s }
    }
    val total = durs.sum()
    val notation = tokens.indices.joinToString(" ") { i -> (if (rests[i]) "R" else "B4") + ":" + durs[i] }
    val scope = rememberCoroutineScope()
    var phase by remember(step) { mutableStateOf("idle") } // idle, countin, recording, done
    var beatShown by remember(step) { mutableIntStateOf(0) }
    val taps = remember(step) { mutableStateListOf<Double>() }
    var startMs by remember(step) { mutableStateOf(0L) }
    var colors by remember(step) { mutableStateOf<Map<Int, Color>>(emptyMap()) }
    var score by remember(step) { mutableStateOf<Int?>(null) }
    val c = PQ.colors
    val beatMs = 60000.0 / step.bpm
    // Instant feedback for every tap: a pulse on the pad and a verdict (on the beat / early / late / extra).
    var feedback by remember(step) { mutableStateOf<Pair<String, Color>?>(null) }
    val tapMarks = remember(step) { mutableStateListOf<Pair<Double, Color>>() }
    val pulse = remember(step) { androidx.compose.animation.core.Animatable(0f) }
    val warn = PQ.colors.warning
    val neutral = MaterialTheme.colorScheme.primary

    fun tap() {
        if (phase != "recording" && phase != "countin") return
        val t = (nowMillis() - startMs) / beatMs
        taps += t
        val verdict: Pair<String, Color> = if (t < -0.35) {
            "Wait for the count-in…" to neutral
        } else {
            val near = onsets.minByOrNull { abs(it - t) }
            val d = if (near == null) 99.0 else t - near
            when {
                abs(d) <= 0.12 -> "On the beat!" to c.success
                abs(d) <= 0.28 -> (if (d < 0) "A little early" else "A little late") to warn
                abs(d) <= 0.6 -> (if (d < 0) "Too early" else "Too late") to c.error
                else -> "Extra tap - no note here" to c.error
            }
        }
        feedback = verdict
        if (t >= -0.35) tapMarks += t to verdict.second
        scope.launch { pulse.snapTo(1f); pulse.animateTo(0f, androidx.compose.animation.core.tween(260)) }
    }

    LaunchedEffect(phase) {
        if (phase == "recording" || phase == "countin") {
            PianoInput.events.collect { e -> if (e.down && e.source != InputSource.DEMO) tap() }
        }
    }

    fun run() {
        taps.clear(); colors = emptyMap(); score = null; feedback = null; tapMarks.clear()
        scope.launch {
            phase = "countin"
            val countIn = step.beatsPerBar
            startMs = nowMillis() + (countIn * beatMs).toLong()
            for (b in 0 until countIn) {
                beatShown = -(countIn - b)
                Audio.click(b == 0)
                delay(beatMs.toLong())
            }
            phase = "recording"
            taps.removeAll { it < -0.5 }
            val beats = kotlin.math.ceil(total).toInt()
            for (b in 0 until beats) {
                beatShown = b + 1
                Audio.click(b % step.beatsPerBar == 0)
                delay(beatMs.toLong())
            }
            delay((beatMs * 0.4).toLong())
            phase = "done"
            // Evaluate: match each expected onset to the nearest unused tap.
            val used = BooleanArray(taps.size)
            val noteIdx = tokens.indices.filter { !rests[it] }
            val colorMap = HashMap<Int, Color>()
            var hits = 0
            onsets.forEachIndexed { k, on ->
                var best = -1
                var bestD = 0.28
                taps.forEachIndexed { i, t -> val d = abs(t - on); if (!used[i] && d < bestD) { bestD = d; best = i } }
                if (best >= 0) { used[best] = true; hits++; colorMap[noteIdx[k]] = c.success } else colorMap[noteIdx[k]] = c.error
            }
            val extra = used.count { !it }
            val s = (100 * hits / maxOf(onsets.size + extra, 1))
            colors = colorMap
            score = s
            onResult(s >= 75)
        }
    }

    val padColor by animateColorAsState(if (phase == "recording") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
    val pad = @Composable { m: Modifier ->
        val flash = feedback?.second ?: Color.Transparent
        Box(
            m.graphicsLayer { val s = 1f - 0.04f * pulse.value; scaleX = s; scaleY = s }
                .clip(RoundedCornerShape(24.dp)).background(padColor)
                .background(flash.copy(alpha = 0.55f * pulse.value))
                .border(6.dp * pulse.value, flash, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val ev = awaitPointerEvent()
                            if (ev.changes.any { it.pressed && !it.previousPressed }) tap()
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (phase) {
                        "countin" -> "Get ready… ${-beatShown}"
                        "recording" -> "TAP!  beat $beatShown"
                        "done" -> "Done!"
                        else -> "Tap pad (or any piano key)"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (phase == "recording") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                // The verdict for the last tap.
                feedback?.let { (msg, col) ->
                    if (phase == "recording" || phase == "countin") Surface(shape = RoundedCornerShape(50), color = col) {
                        Text(msg, Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.titleMedium,
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    val info = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(step.prompt, style = MaterialTheme.typography.headlineSmall)
            StaffView(notation, timeSig = "${step.beatsPerBar}/4", noteColors = colors, space = 8.dp)
            // Timeline: the rhythm's notes (rings) and where each tap landed (dots in the verdict colour).
            if (tapMarks.isNotEmpty() || phase == "recording") {
                val ink = MaterialTheme.colorScheme.onSurfaceVariant
                androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                    val pad = 10.dp.toPx()
                    val w = size.width - pad * 2
                    val y = size.height / 2
                    fun x(b: Double) = pad + (b / total.coerceAtLeast(0.001)).toFloat().coerceIn(0f, 1f) * w
                    drawLine(ink.copy(alpha = 0.4f), androidx.compose.ui.geometry.Offset(pad, y), androidx.compose.ui.geometry.Offset(pad + w, y), 2f)
                    for (on in onsets) drawCircle(ink, 6.dp.toPx(), androidx.compose.ui.geometry.Offset(x(on), y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                    for ((t, col) in tapMarks) if (t >= -0.3) drawCircle(col, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(x(t), y))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ListenButton(notation.replace("B4", "C5"), step.bpm, "Hear it")
                Button(onClick = { run() }, enabled = phase == "idle" || phase == "done") {
                    Text(if (phase == "done") "Try again" else "Start (${step.beatsPerBar}-beat count-in)")
                }
            }
            score?.let {
                Text("Timing accuracy: $it%", style = MaterialTheme.typography.titleMedium, color = if (it >= 75) c.success else c.error)
            }
            if (result == null && score == null) Text("Press Start, listen to the count-in, then tap the rhythm on the pad.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (isWideLesson(maxWidth, maxHeight)) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                FitToHeight(Modifier.weight(1.2f).fillMaxHeight()) { info() }
                pad(Modifier.weight(0.8f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FitToHeight(Modifier.fillMaxWidth().weight(1f)) { info() }
                pad(Modifier.fillMaxWidth().height(150.dp))
            }
        }
    }
}

@Composable
fun SongStepView(step: Step.SongPractice) {
    val song = SongLibrary.get(step.songId) ?: return
    val nav = LocalNavigator.current
    val p = com.pianoquest.app.data.ProgressStore.state.value
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoodBadge(MascotMood.EXCITED, 46.dp)
            Text("Song time!", style = MaterialTheme.typography.headlineSmall)
        }
        Text(step.text, style = MaterialTheme.typography.bodyLarge)
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { nav.openSong(song.id) }) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.titleLarge)
                    Text(song.composer, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(song.difficultyLabel, style = MaterialTheme.typography.labelLarge)
                        Stars(p.songStars[song.id] ?: 0)
                    }
                }
                Icon(Icons.Default.PlayArrow, "Open", Modifier.size(40.dp))
            }
        }
        Text("This step is optional - continue whenever you're ready.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

