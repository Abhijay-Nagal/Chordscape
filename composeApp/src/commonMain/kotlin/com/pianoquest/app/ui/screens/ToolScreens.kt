package com.pianoquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.pianoquest.app.ui.components.FitToHeight
import com.pianoquest.app.ui.components.ScrollHintRow
import androidx.compose.ui.unit.dp
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.audio.Player
import com.pianoquest.app.audio.Sfx
import androidx.compose.material.icons.filled.Check
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.DrillKind
import com.pianoquest.app.data.Drills
import com.pianoquest.app.data.Glossary
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.NoteEvent
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Step
import com.pianoquest.app.input.InputSource
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.Storage
import com.pianoquest.app.platform.nowMillis
import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.ChordType
import com.pianoquest.app.theory.Interval
import com.pianoquest.app.theory.Key
import com.pianoquest.app.theory.Pitch
import com.pianoquest.app.theory.RomanNumerals
import com.pianoquest.app.theory.ScaleType
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.ToolId
import com.pianoquest.app.ui.components.ChordDiagrams
import com.pianoquest.app.ui.components.CircleOfFifths
import com.pianoquest.app.ui.components.TipCard
import com.pianoquest.app.ui.components.PianoKeyboard
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.components.ScreenHeader
import com.pianoquest.app.ui.components.StaffView
import com.pianoquest.app.ui.components.VisualView
import com.pianoquest.app.ui.components.keyLabel
import com.pianoquest.app.ui.theme.PQ
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ToolScreen(tool: ToolId) {
    val nav = LocalNavigator.current
    Column(Modifier.fillMaxSize()) {
        // Compact header: every tool gets the full landscape height.
        Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(tool.title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text("  ·  ${tool.subtitle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 10.dp)) {
            when (tool) {
                ToolId.CHORDS -> ChordExplorer()
                ToolId.SCALES -> ScaleExplorer()
                ToolId.CIRCLE -> CircleTool()
                ToolId.PROGRESSIONS -> ProgressionTool()
                ToolId.METRONOME -> MetronomeTool()
                ToolId.EAR -> EarTrainerTool()
                ToolId.READING -> ReadingTool()
                ToolId.CHORD_FINDER -> ChordFinderTool()
                ToolId.GLOSSARY -> GlossaryTool()
            }
        }
    }
}

/**
 * Controls on top, a keyboard pinned to the bottom - no scrolling. The keyboard takes the height the controls leave
 * free (between [keyboardFraction] and 55% of the screen), so big screens get big keys instead of empty space; only if
 * the controls still do not fit are they scaled down.
 */
@Composable
private fun ToolWithKeyboard(keyboardFraction: Float = 0.3f, top: @Composable () -> Unit, keyboard: @Composable (Modifier) -> Unit) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.ui.layout.SubcomposeLayout(Modifier.fillMaxSize()) { constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val gap = with(density) { 8.dp.roundToPx() }
        val minKb = (h * keyboardFraction).toInt().coerceAtLeast(with(density) { 72.dp.roundToPx() }).coerceAtMost(h / 2)
        val maxKb = (h * 0.55f).toInt().coerceAtMost(with(density) { 300.dp.roundToPx() }).coerceAtLeast(minKb)
        val probe = subcompose("probe") { top() }.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity))
        }
        val topH = probe.maxOfOrNull { it.height } ?: 0
        val kbH = (h - topH - gap).coerceIn(minKb, maxKb)
        val topSpace = (h - kbH - gap).coerceAtLeast(0)
        val topPlace = subcompose("top") {
            FitToHeight(Modifier.fillMaxWidth().height(with(density) { topSpace.toDp() }), minScale = 0.55f, alignment = Alignment.TopStart) { top() }
        }.map { it.measure(androidx.compose.ui.unit.Constraints.fixed(w, topSpace)) }
        val kb = subcompose("kb") { keyboard(Modifier.fillMaxWidth().height(with(density) { kbH.toDp() }).clip(RoundedCornerShape(10.dp))) }
            .map { it.measure(androidx.compose.ui.unit.Constraints.fixed(w, kbH)) }
        layout(w, h) {
            topPlace.forEach { it.place(0, 0) }
            kb.forEach { it.place(0, h - kbH) }
        }
    }
}

private val ROOTS12 = listOf("C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

@Composable
private fun RootPicker(selected: String, onSelect: (String) -> Unit, roots: List<String> = ROOTS12) {
    ScrollHintRow(Modifier.fillMaxWidth(), spacing = 6.dp) {
        roots.forEach { r -> FilterChip(selected == r, { onSelect(r) }, { Text(Pitch.parse(r + "4").pretty) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChordExplorer() {
    var root by remember { mutableStateOf("C") }
    var type by remember { mutableStateOf(ChordType.MAJOR) }
    var category by remember { mutableStateOf(ChordType.MAJOR.category) }
    var inversion by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val chord = Chord(Pitch.parse(root + "4"), type)
    val maxInv = type.intervals.size - 1
    val inv = inversion.coerceAtMost(maxInv)
    val pitches = chord.pitches(if (Pitch.parse(root + "4").midi > 66) 3 else 4, inv).map { it.simplified() }
    val c = PQ.colors
    ToolWithKeyboard(top = {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Phones: one scrolling row per choice with its label in front (everything fits at full size).
            // Tablets: the chips wrap onto several rows and use the extra room.
            val compact = com.pianoquest.app.ui.LocalFormFactor.current == com.pianoquest.app.ui.FormFactor.PHONE
            Column(Modifier.weight(if (compact) 1.4f else 1.1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ChoiceGroup("Root", compact) {
                    ROOTS12.forEach { r -> FilterChip(root == r, { root = r }, { Text(Pitch.parse(r + "4").pretty) }) }
                }
                ChoiceGroup("Family", compact) {
                    ChordType.entries.map { it.category }.distinct().forEach { cat ->
                        // Choosing a family also selects its first chord, so the card always matches a highlighted chip.
                        FilterChip(category == cat, {
                            category = cat
                            if (type.category != cat) { type = ChordType.entries.first { it.category == cat }; inversion = 0 }
                        }, { Text(cat) })
                    }
                }
                ChoiceGroup("Chord", compact) {
                    ChordType.entries.filter { it.category == category }.forEach { t ->
                        FilterChip(type == t, { type = t; inversion = 0 }, { Text(Pitch.parse(root + "4").pretty + t.symbol + if (t.symbol.isEmpty()) " (maj)" else "") })
                    }
                }
                ChoiceGroup("Position", compact) {
                    (0..maxInv).forEach { i ->
                        FilterChip(inv == i, { inversion = i }, { Text(when (i) { 0 -> "Root position"; 1 -> "1st inversion"; 2 -> "2nd inversion"; else -> "${i}th inversion" }) })
                    }
                }
            }
            val playChord = { scope.launch { Player.play(pitches.map { NoteEvent(it, 0.0, 3.0, com.pianoquest.app.data.Hand.RIGHT) }, 60.0) } }
            val arpeggiate = { scope.launch { Player.play(pitches.mapIndexed { i, p -> NoteEvent(p, i * 0.5, 2.0 - i * 0.25, com.pianoquest.app.data.Hand.RIGHT) }, 90.0) } }
            val staff = @Composable { m: Modifier ->
                Box(m) { StaffView(pitches.joinToString("+") { it.name } + ":w", clef = if (pitches.first().midi < 57) Clef.GRAND else Clef.TREBLE, space = if (compact) 5.dp else 7.dp) }
            }
            Surface(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                if (compact) {
                    // Phones: a compact card - name and formula side by side, staff beside the buttons.
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(chord.prettySymbol, style = MaterialTheme.typography.headlineMedium)
                            Column(Modifier.weight(1f)) {
                                Text("${type.displayName} · ${type.formula}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, maxLines = 2)
                                Text(type.sound, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                        Text("Notes: " + pitches.joinToString("  ") { it.pretty }, style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            staff(Modifier.weight(1f))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(onClick = { playChord() }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)) { Text("Play chord") }
                                FilledTonalButton(onClick = { arpeggiate() }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)) { Text("Arpeggiate") }
                            }
                        }
                    }
                } else {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(chord.prettySymbol, style = MaterialTheme.typography.displaySmall)
                        Text("${type.displayName} · formula ${type.formula}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(type.sound, style = MaterialTheme.typography.bodyMedium)
                        Text("Notes: " + pitches.joinToString("  ") { it.pretty }, style = MaterialTheme.typography.titleSmall)
                        staff(Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { playChord() }) { Text("Play chord") }
                            FilledTonalButton(onClick = { arpeggiate() }) { Text("Arpeggiate") }
                        }
                    }
                }
            }
        }
    }) { m -> PianoKeyboard(48, 84, m, highlights = pitches.associate { it.midi to c.keyTarget }, labels = KeyLabels.HIGHLIGHTED,
        // Spelled as in the chord (B♭ in C7, not A♯).
        customLabels = pitches.associate { it.midi to it.pretty }) }
}

/** A labelled group of choice chips: one scrolling row on phones, wrapping rows on tablets. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceGroup(label: String, compact: Boolean, chips: @Composable () -> Unit) {
    if (compact) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(70.dp))
            ScrollHintRow(Modifier.weight(1f), spacing = 6.dp) { chips() }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { chips() }
        }
    }
}

@Composable
private fun ScaleExplorer() {
    var root by remember { mutableStateOf("C") }
    var type by remember { mutableStateOf(ScaleType.MAJOR) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var idx by remember { mutableIntStateOf(-1) }
    val tonic = Pitch.parse(root + "4").let { if (it.midi > 66) it.withOctave(3) else it }
    val ps = type.pitches(tonic).map { if (it.accidental !in -1..1) it.simplified() else it }
    val c = PQ.colors
    val hl = HashMap<Int, Color>()
    ps.forEach { hl[it.midi] = c.keyTarget }
    hl[ps.first().midi] = c.keyLeft
    if (idx in ps.indices) hl[ps[idx].midi] = c.keyDemo
    ToolWithKeyboard(top = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            RootPicker(root, { root = it })
            ScrollHintRow(Modifier.fillMaxWidth(), spacing = 6.dp) {
                ScaleType.entries.forEach { t -> FilterChip(type == t, { type = t }, { Text(t.displayName) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${tonic.pretty} ${type.displayName}", style = MaterialTheme.typography.headlineSmall)
                    Text(type.family + " · " + type.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                    Text("Steps: " + type.semitones.zipWithNext { a, b -> when (b - a) { 1 -> "H"; 2 -> "W"; 3 -> "W+H"; else -> "${b - a}" } }.plus(
                        when (12 - type.semitones.last()) { 1 -> "H"; 2 -> "W"; 3 -> "W+H"; else -> "" }).joinToString(" - "), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(ps.joinToString("  ") { it.pretty }, style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    StaffView(ps.joinToString(" ") { it.name + ":q" }, highlightIndex = idx.takeIf { it >= 0 }, space = 6.dp)
                    Button(onClick = {
                        if (job?.isActive == true) { job?.cancel(); return@Button }
                        job = scope.launch { Player.playNotation(ps.joinToString(" ") { it.name + ":e" }, 100) { idx = it } }
                    }) { Icon(if (job?.isActive == true) Icons.Default.Stop else Icons.Default.PlayArrow, null); Text(" Play scale") }
                }
            }
        }
    }) { m ->
        PianoKeyboard(ps.first().midi - ps.first().midi.mod(12), ps.last().midi + (11 - ps.last().midi.mod(12)), m, highlights = hl, labels = KeyLabels.HIGHLIGHTED,
            customLabels = ps.associate { it.midi to it.pretty })
    }
}

@Composable
private fun CircleTool() {
    var sel by remember { mutableIntStateOf(0) }
    var minor by remember { mutableStateOf(false) }
    val key = if (minor) Key.minor(Key.CIRCLE_MINOR[sel]) else Key.major(Key.CIRCLE_MAJOR[sel])
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight
        val circleSize = if (wide) minOf(maxHeight, maxWidth * 0.42f) else minOf(maxWidth, maxHeight * 0.48f)
        val circle = @Composable { CircleOfFifths(null, Modifier.size(circleSize), selected = sel) { i, inner -> sel = i; minor = inner } }
        if (wide) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                circle()
                Box(Modifier.weight(1f).fillMaxSize()) { CircleDetails(key, minor) }
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                circle()
                Box(Modifier.fillMaxWidth().weight(1f)) { CircleDetails(key, minor) }
            }
        }
    }
}

/** Key details beside the circle: large, readable text that fills the space, chords you can tap, and a cadence to play. */
@Composable
private fun CircleDetails(key: Key, minor: Boolean) {
    val scope = rememberCoroutineScope()
    val triads = key.diatonicTriads()
    val cadence = remember(key) { RomanNumerals.progression(if (minor) "i iv V i" else "I IV V I", key) }
    var playing by remember { mutableStateOf<Job?>(null) }
    var current by remember { mutableIntStateOf(-1) }
    FitToHeight(Modifier.fillMaxSize(), minScale = 0.8f) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(key.name, style = MaterialTheme.typography.headlineMedium)
                    val sig = key.signature
                    Text(when { sig == 0 -> "No sharps or flats"; sig > 0 -> "$sig sharp(s): " + key.signatureAccidentals().joinToString(" ") { it.pretty }
                        else -> "${-sig} flat(s): " + key.signatureAccidentals().joinToString(" ") { it.pretty } }, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Relative ${if (minor) "major" else "minor"}: ${key.relative.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Parallel: ${key.parallel.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Scale: " + key.scalePitches().joinToString(" ") { it.pretty }, style = MaterialTheme.typography.bodyLarge, maxLines = 1, softWrap = false)
                }
                Box(Modifier.width(170.dp)) { StaffView("", keySig = key.signature, space = 8.dp) }
            }
            Text("Chords in ${key.name} - tap to hear", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                triads.forEach { (num, chord) ->
                    Surface(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { scope.launch { Player.playChord(chord) } },
                        color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(num, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                            // Diminished as "°" (C♯°), so the narrow chips on phones never cut the name off.
                            Text(chord.prettySymbol.replace("dim", "°"), style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
            Text(if (minor) "Cadence i - iv - V - i" else "Cadence I - IV - V - I", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cadence.forEachIndexed { i, ch ->
                    Surface(shape = RoundedCornerShape(10.dp), color = if (current == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (current == i) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) {
                        Text(ch.prettySymbol, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    if (playing?.isActive == true) { playing?.cancel(); current = -1; return@Button }
                    playing = scope.launch {
                        try { cadence.forEachIndexed { i, ch -> current = i; Player.playChord(ch, seconds = 1.1) } } finally { current = -1 }
                    }
                }) { Icon(if (playing?.isActive == true) Icons.Default.Stop else Icons.Default.PlayArrow, null); Text(" Play", maxLines = 1, softWrap = false) }
            }
        }
    }
}

private data class Preset(val name: String, val numerals: String, val about: String, val minor: Boolean = false)

private val PRESETS = listOf(
    Preset("Axis (pop)", "I V vi IV", "The four chords behind countless pop songs"),
    Preset("Sad pop loop", "vi IV I V", "The same chords, starting on the minor one"),
    Preset("50s / doo-wop", "I vi IV V", "The classic 1950s progression"),
    Preset("Three-chord rock", "I IV V I", "Twist and Shout, La Bamba, countless folk songs"),
    Preset("ii-V-I (jazz)", "ii7 V7 Imaj7 Imaj7", "The backbone of jazz standards"),
    Preset("Jazz turnaround", "Imaj7 vi7 ii7 V7", "The I-vi-ii-V 'rhythm changes' turnaround"),
    Preset("Circle of fifths", "vi ii V I", "Circle-of-fifths motion"),
    Preset("Pachelbel", "I V vi iii IV I IV V", "Pachelbel's Canon in D"),
    Preset("Andalusian cadence", "i bVII bVI V", "Flamenco and Spanish music", true),
    Preset("Minor epic", "i VI III VII", "Film scores and epic themes", true),
    Preset("Minor loop", "i iv VI VII", "A minor loop with a 3-3-2 groove", true),
    Preset("Mixolydian rock", "I bVII IV I", "Rock with a flattened seventh"),
    Preset("Mario cadence", "I bVI bVII I", "Triumphant borrowed-chord ending"),
    Preset("Gospel amen", "I IV iv I", "The emotional minor-iv plagal cadence"),
    Preset("12-bar blues", "I7 I7 I7 I7 IV7 IV7 I7 I7 V7 IV7 I7 V7", "The blues form"),
    Preset("Royal Road (J-pop)", "IVmaj7 V7 iii7 vi", "The favourite progression of Japanese pop and anime"),
)

@Composable
private fun ProgressionTool() {
    var preset by remember { mutableStateOf(PRESETS.first()) }
    var root by remember { mutableStateOf("C") }
    var custom by remember { mutableStateOf("") }
    var bpm by remember { mutableIntStateOf(90) }
    var current by remember { mutableIntStateOf(-1) }
    var job by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val numerals = custom.ifBlank { preset.numerals }
    val key = if (preset.minor && custom.isBlank()) Key.minor(root) else Key.major(root)
    val chords = runCatching { RomanNumerals.progression(numerals, key) }.getOrNull()
    val hl = chords?.getOrNull(current)?.let { ch -> (listOf(ch.bassMidi()) + ch.compVoicing(64)).associateWith { PQ.colors.keyTarget } } ?: emptyMap()
    ToolWithKeyboard(keyboardFraction = 0.28f, top = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ScrollHintRow(Modifier.fillMaxWidth(), spacing = 6.dp) {
                PRESETS.forEach { p -> FilterChip(preset == p && custom.isBlank(), { preset = p; custom = "" }, { Text(p.name) }) }
            }
            Text(preset.about, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1.2f)) { RootPicker(root, { root = it }) }
                OutlinedTextField(custom, { custom = it }, Modifier.weight(1f), label = { Text("Your own numerals (e.g. I iii IV iv)") }, singleLine = true)
            }
            if (chords == null) Text("Couldn't read those numerals. Use I ii iii IV V vi vii°, with b/# prefixes and 7/maj7 suffixes.", color = PQ.colors.error)
            else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScrollHintRow(Modifier.weight(1f)) {
                    val nums = numerals.trim().split(Regex("\\s+"))
                    chords.forEachIndexed { i, ch ->
                        Surface(shape = RoundedCornerShape(14.dp), color = if (i == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (i == current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(nums.getOrElse(i) { "" }, style = MaterialTheme.typography.labelMedium)
                                Text(ch.prettySymbol, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                Button(onClick = {
                    if (job?.isActive == true) { job?.cancel(); current = -1; return@Button }
                    job = scope.launch {
                        try {
                            while (isActive) {
                                chords.forEachIndexed { i, ch ->
                                    current = i
                                    val notes = listOf(ch.bassMidi()) + ch.compVoicing(64)
                                    notes.forEach { PianoInput.noteOn(it, 80, InputSource.DEMO) }
                                    for (b in 0 until 4) { Audio.click(b == 0); delay((60000 / bpm).toLong()) }
                                    notes.forEach { PianoInput.noteOff(it, InputSource.DEMO) }
                                }
                            }
                        } finally { current = -1; PianoInput.releaseDemo() }
                    }
                }) { Icon(if (job?.isActive == true) Icons.Default.Stop else Icons.Default.PlayArrow, null); Text(if (job?.isActive == true) " Stop" else " Play loop") }
                Text("$bpm bpm", style = MaterialTheme.typography.labelLarge)
                Slider(bpm.toFloat(), { bpm = it.toInt() }, valueRange = 50f..180f, modifier = Modifier.width(150.dp))
            }
        }
    }) { m -> PianoKeyboard(36, 83, m, highlights = hl, labels = KeyLabels.C_ONLY) }
}

@Composable
private fun MetronomeTool() {
    var bpm by remember { mutableIntStateOf(90) }
    var beats by remember { mutableIntStateOf(4) }
    var running by remember { mutableStateOf(false) }
    var beat by remember { mutableIntStateOf(-1) }
    val taps = remember { mutableStateListOf<Long>() }
    LaunchedEffect(running, bpm, beats) {
        if (!running) { beat = -1; return@LaunchedEffect }
        var b = 0
        val start = nowMillis()
        var n = 0L
        while (running) {
            beat = b
            Audio.click(b == 0)
            b = (b + 1) % beats
            n++
            delay((start + n * 60000 / bpm - nowMillis()).coerceAtLeast(1))
        }
    }
    FitToHeight(Modifier.fillMaxSize(), minScale = 0.6f, alignment = Alignment.Center) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("$bpm", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold)
                Text(tempoName(bpm), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(beats) { i ->
                        Box(Modifier.size(if (i == 0) 30.dp else 24.dp).clip(CircleShape).background(
                            if (i == beat) (if (i == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
            }
            Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { bpm = (bpm - 1).coerceAtLeast(30) }) { Icon(Icons.Default.Remove, "Slower") }
                    Slider(bpm.toFloat(), { bpm = it.toInt() }, valueRange = 30f..240f, modifier = Modifier.weight(1f))
                    IconButton(onClick = { bpm = (bpm + 1).coerceAtMost(240) }) { Icon(Icons.Default.Add, "Faster") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2, 3, 4, 5, 6, 7).forEach { b -> FilterChip(beats == b, { beats = b }, { Text("$b/4") }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { running = !running }, Modifier.height(52.dp).widthIn(min = 140.dp)) { Text(if (running) "Stop" else "Start", style = MaterialTheme.typography.titleMedium) }
                    OutlinedButton(onClick = {
                        val now = nowMillis()
                        if (taps.isNotEmpty() && now - taps.last() > 2000) taps.clear()
                        taps += now
                        if (taps.size >= 2) {
                            val avg = taps.zipWithNext { a, b -> b - a }.takeLast(4).average()
                            bpm = (60000 / avg).toInt().coerceIn(30, 240)
                        }
                    }, Modifier.height(52.dp)) { Text("Tap tempo") }
                }
            }
        }
    }
}

private fun tempoName(bpm: Int) = when {
    bpm < 60 -> "Largo"
    bpm < 66 -> "Larghetto"
    bpm < 76 -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    bpm < 156 -> "Allegro"
    bpm < 176 -> "Vivace"
    else -> "Presto"
}

@Composable
private fun EarTrainerTool() {
    val kinds = listOf(DrillKind.INTERVAL_EAR to "Intervals", DrillKind.CHORD_QUALITY_EAR to "Triads", DrillKind.SEVENTH_EAR to "7th chords",
        DrillKind.CADENCE_EAR to "Cadences", DrillKind.PROGRESSION_EAR to "Progressions", DrillKind.MODE_ID to "Modes")
    var kind by remember { mutableStateOf(kinds.first().first) }
    var level by remember { mutableIntStateOf(1) }
    var counter by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    val step = remember(kind, level, counter) { Drills.expand(Step.Drill(kind, 1, level), Random.Default).first() as Step.Quiz }
    var chosen by remember(step) { mutableStateOf<String?>(null) }
    // A fresh random order every question, so the right answer is never in a predictable place.
    val options = remember(step) { step.options.shuffled(Random.Default) }
    val answer = step.options.first()
    val result = chosen?.let { it == answer }
    val scope = rememberCoroutineScope()
    var playJob by remember { mutableStateOf<Job?>(null) }
    var playing by remember { mutableStateOf(false) }
    fun play() {
        val notes = step.play ?: return
        playJob?.cancel()
        playing = true
        playJob = scope.launch { try { Player.playNotation(notes, step.playBpm) } finally { playing = false } }
    }
    // Every new question plays by itself.
    LaunchedEffect(step) { delay(350); play() }
    val c = PQ.colors
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScrollHintRow(Modifier.weight(1f), spacing = 6.dp) {
                kinds.forEach { (k, n) -> FilterChip(kind == k, { kind = k }, { Text(n) }) }
                Spacer(Modifier.width(8.dp))
                (1..3).forEach { l -> FilterChip(level == l, { level = l }, { Text(listOf("Easy", "Medium", "Hard")[l - 1]) }) }
            }
            Text("$correct / $total · streak $streak 🔥", style = MaterialTheme.typography.titleSmall)
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Question, sound and feedback.
            Surface(Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(20.dp), color = when (result) {
                true -> c.success.copy(alpha = 0.12f); false -> c.error.copy(alpha = 0.12f); null -> MaterialTheme.colorScheme.surfaceContainer
            }) {
                FitToHeight(Modifier.fillMaxSize().padding(16.dp), alignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(step.question, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        step.visual?.let { VisualView(it, Modifier.fillMaxWidth()) }
                        Button(onClick = { if (playing) { playJob?.cancel(); playing = false } else play() }, modifier = Modifier.size(width = 200.dp, height = 64.dp),
                            shape = RoundedCornerShape(32.dp)) {
                            Icon(if (playing) Icons.Default.Stop else Icons.Default.PlayArrow, null, Modifier.size(30.dp))
                            Text(if (playing) "  Stop" else "  Play again", style = MaterialTheme.typography.titleMedium)
                        }
                        when (result) {
                            null -> Text("Listen, then choose your answer.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            true -> Text("✓ Correct!", style = MaterialTheme.typography.headlineSmall, color = c.success)
                            false -> Text("✗ It was: $answer", style = MaterialTheme.typography.titleLarge, color = c.error, textAlign = TextAlign.Center)
                        }
                        if (result != null && step.explanation.isNotBlank()) Text(step.explanation, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        if (result != null) Button(onClick = { counter++ }, Modifier.height(52.dp).widthIn(min = 200.dp)) {
                            Text("Next question  →", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            // Answers as big tiles that fill the space.
            Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.chunked(2).forEach { pair ->
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { opt ->
                            val isAnswer = result != null && opt == answer
                            val isWrong = result == false && opt == chosen
                            Surface(
                                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp))
                                    .border(2.dp, when { isAnswer -> c.success; isWrong -> c.error; else -> MaterialTheme.colorScheme.outlineVariant }, RoundedCornerShape(18.dp))
                                    .clickable(enabled = result == null) {
                                        chosen = opt
                                        val ok = opt == answer
                                        total++
                                        if (ok) { correct++; streak++; Sfx.correct() } else { streak = 0; Sfx.wrong() }
                                        ProgressStore.recordDrill(if (ok) 1 else 0, 0)
                                    },
                                color = when { isAnswer -> c.success.copy(alpha = 0.18f); isWrong -> c.error.copy(alpha = 0.16f); else -> MaterialTheme.colorScheme.surface },
                            ) {
                                Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                                    Text(opt, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Feedback for the last answer in the note reading trainer. */
private class ReadingAnswer(val note: Pitch, val chosen: Int, val correct: Boolean)

@Composable
private fun ReadingTool() {
    var clef by remember { mutableStateOf(Clef.TREBLE) }
    var running by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(60) }
    var score by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var counter by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf<ReadingAnswer?>(null) }
    var runClef by remember { mutableStateOf(Clef.TREBLE) }
    val bestKey = "reading-best-${clef.name}"
    var best by remember(clef) { mutableIntStateOf(Storage.read(bestKey)?.toIntOrNull() ?: 0) }
    val pool = when (clef) {
        Clef.TREBLE -> "A3 B3 C4 D4 E4 F4 G4 A4 B4 C5 D5 E5 F5 G5 A5 B5 C6".split(" ")
        Clef.BASS -> "C2 D2 E2 F2 G2 A2 B2 C3 D3 E3 F3 G3 A3 B3 C4 D4 E4".split(" ")
        Clef.GRAND -> "C2 E2 G2 B2 D3 F3 A3 C4 E4 G4 B4 D5 F5 A5 C6".split(" ")
    }
    val note = remember(counter, clef) { Pitch.parse(pool[Random.Default.nextInt(pool.size)]) }
    val letters = listOf("C", "D", "E", "F", "G", "A", "B")
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        timeLeft = 60
        while (timeLeft > 0 && running) { delay(1000); timeLeft-- }
        running = false
        // The best score belongs to the clef the round was played in (a round that changed clef counts as "Both").
        val key = "reading-best-${runClef.name}"
        val prev = Storage.read(key)?.toIntOrNull() ?: 0
        if (score > prev) Storage.write(key, score.toString())
        best = Storage.read(bestKey)?.toIntOrNull() ?: 0
        ProgressStore.recordDrill(score, 1)
    }
    // After each answer: show the right note and whether the answer was right, then move on to the next note.
    LaunchedEffect(answer) {
        val a = answer ?: return@LaunchedEffect
        delay(if (a.correct) 900 else 1700)
        if (answer === a) { answer = null; counter++ }
    }
    fun answer(letter: Int) {
        if (!running || answer != null) return
        val ok = letter == note.letter
        if (ok) { score++; Sfx.correct() } else { misses++; Sfx.wrong() }
        answer = ReadingAnswer(note, letter, ok)
    }
    LaunchedEffect(running) {
        if (running) PianoInput.events.collect { e -> if (e.down && e.source != InputSource.DEMO) answer(Pitch.fromMidi(e.midi).letter.takeIf { Pitch.fromMidi(e.midi).accidental == 0 } ?: -1) }
    }
    val c = PQ.colors
    FitToHeight(Modifier.fillMaxSize(), minScale = 0.6f) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // The clef can be changed at any time, even during a round.
                listOf(Clef.TREBLE to "Treble", Clef.BASS to "Bass", Clef.GRAND to "Both").forEach { (cl, n) ->
                    FilterChip(clef == cl, {
                        if (clef != cl) {
                            clef = cl
                            if (running) { runClef = Clef.GRAND; answer = null; counter++ }
                        }
                    }, { Text(n) })
                }
                Spacer(Modifier.weight(1f))
                Text(if (running) "⏱ $timeLeft s" else "60-second challenge", style = MaterialTheme.typography.titleMedium)
                Text("Score $score · best $best", style = MaterialTheme.typography.titleSmall)
                if (!running) Button(onClick = { score = 0; misses = 0; counter++; answer = null; runClef = clef; running = true }) { Text(if (score + misses > 0) "Again" else "Start") }
            }
            val a = answer
            Surface(shape = RoundedCornerShape(20.dp), color = when (a?.correct) {
                true -> c.success.copy(alpha = 0.14f); false -> c.error.copy(alpha = 0.14f); null -> MaterialTheme.colorScheme.surfaceContainer
            }) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.weight(1f)) {
                        StaffView((a?.note ?: note).name + ":w", clef = clef, space = if (clef == Clef.GRAND) 7.dp else 10.dp, showNames = a != null)
                    }
                    Column(Modifier.width(280.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        when {
                            a == null && !running -> Text(if (score + misses > 0) "Round over: $score right, $misses wrong" else "Press Start, then name each note as fast as you can.",
                                style = MaterialTheme.typography.titleMedium)
                            a == null -> Text("Which note is this?", style = MaterialTheme.typography.titleMedium)
                            a.correct -> {
                                Text("✓ Correct!", style = MaterialTheme.typography.headlineSmall, color = c.success)
                                Text("It's ${a.note.pretty} (${a.note.name}).", style = MaterialTheme.typography.titleMedium)
                            }
                            else -> {
                                Text("✗ Not quite", style = MaterialTheme.typography.headlineSmall, color = c.error)
                                Text("You chose ${if (a.chosen in 0..6) letters[a.chosen] else "a black key"} - the answer is ${a.note.pretty} (${a.note.name}).",
                                    style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                letters.forEachIndexed { i, l ->
                    val isAnswer = a != null && i == a.note.letter
                    val isWrongChoice = a != null && !a.correct && i == a.chosen
                    Button(onClick = { answer(i) }, enabled = running, modifier = Modifier.weight(1f).height(64.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = when { isAnswer -> c.success; isWrongChoice -> c.error; else -> MaterialTheme.colorScheme.primary },
                            disabledContainerColor = when { isAnswer -> c.success; isWrongChoice -> c.error; else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) },
                            disabledContentColor = if (isAnswer || isWrongChoice) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        )) {
                        Text(l, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Text("You can also answer by playing the note on a MIDI keyboard. Switch between treble, bass and both at any time.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChordFinderTool() {
    val held by PianoInput.held.collectAsState()
    val latched = remember { mutableStateListOf<Int>() }
    var latch by remember { mutableStateOf(true) }
    val notes = if (latch) latched.toSet() else held
    val chord = Chord.identify(notes)
    val c = PQ.colors
    ToolWithKeyboard(keyboardFraction = 0.36f, top = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("How to use", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("1. Choose how you enter notes:", style = MaterialTheme.typography.bodyMedium)
                        // Both modes stay visible; the selected one is filled.
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            ModeButton("Tap to toggle notes", latch, Modifier.weight(1f)) { if (!latch) { latch = true; latched.clear() } }
                            ModeButton("Hold notes (MIDI)", !latch, Modifier.weight(1f)) { if (latch) { latch = false; latched.clear() } }
                        }
                        Text(if (latch) "Tap a key to add it, tap it again to remove it - build the chord one note at a time."
                            else "Press and hold the notes together - on your MIDI keyboard, or with several fingers on the screen. Let go to clear.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("2. Pick two or more notes on the keyboard below.", style = MaterialTheme.typography.bodyMedium)
                        Text("3. Read the chord name, its formula and the bass note on the right.", style = MaterialTheme.typography.bodyMedium)
                        if (latch) OutlinedButton(onClick = { latched.clear() }, enabled = latched.isNotEmpty()) { Text("Clear notes") }
                    }
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Two notes are an interval, not a chord: name the interval instead of "Unknown chord".
                            val two = notes.sorted().takeIf { it.size == 2 }
                            Text(chord?.prettySymbol ?: when {
                                notes.size < 2 -> "Pick 2+ notes"
                                two != null -> com.pianoquest.app.theory.INTERVAL_FRIENDLY[(two[1] - two[0]).coerceAtMost(12)]
                                else -> "Unknown chord"
                            }, style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center)
                            chord?.let { Text("${it.type.displayName} · ${it.type.formula}" + (it.bass?.let { b -> " · ${b.pretty} in the bass" } ?: ""),
                                style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center) }
                            if (notes.isNotEmpty()) Text(notes.sorted().joinToString("  ") { m -> chord?.spell(m)?.prettyName ?: keyLabel(m, false, true) }, style = MaterialTheme.typography.titleMedium)
                            if (notes.size == 2 && chord != null) {
                                val (a, b) = notes.sorted()
                                Text("Interval: " + com.pianoquest.app.theory.INTERVAL_FRIENDLY[(b - a).coerceAtMost(12)], style = MaterialTheme.typography.bodyMedium)
                            }
                            if (notes.size >= 2 && chord != null) StaffView(notes.sorted().joinToString("+") { chord.spell(it).name } + ":w",
                                clef = if (notes.minOrNull()!! < 57) Clef.GRAND else Clef.TREBLE, space = 6.dp)
                        }
                    }
                }
    }) { m ->
        PianoKeyboard(48, 83, m, highlights = notes.associateWith { c.keyTarget }, labels = KeyLabels.C_ONLY,
            customLabels = chord?.let { ch -> notes.associateWith { ch.spell(it).pretty }.filter { (m, l) -> l != Pitch.fromMidi(m).pretty } } ?: emptyMap(),
            onDown = { midi, v -> PianoInput.noteOn(midi, v); if (latch) { if (midi in latched) latched.remove(midi) else latched.add(midi) } },
            onUp = { PianoInput.noteOff(it) })
    }
}

/** One of a pair of always-visible mode buttons. */
@Composable
private fun ModeButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier) { Icon(Icons.Default.Check, null, Modifier.size(18.dp)); Text(" $label", maxLines = 1) }
    else OutlinedButton(onClick = onClick, modifier) { Text(label, maxLines = 1) }
}

/** Encyclopedia as a list beside a detail panel: no long scrolling through expanded entries. */
@Composable
private fun GlossaryTool() {
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<String?>(null) }
    val list = Glossary.entries.filter { (cat == null || it.category == cat) && (query.isBlank() || it.term.contains(query, true) || it.definition.contains(query, true)) }
    var selected by remember { mutableStateOf(Glossary.entries.firstOrNull()?.term) }
    val entry = list.firstOrNull { it.term == selected } ?: list.firstOrNull()
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(query, { query = it }, Modifier.width(260.dp), placeholder = { Text("Search ${Glossary.entries.size} terms…") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            ScrollHintRow(Modifier.weight(1f), spacing = 6.dp) {
                FilterChip(cat == null, { cat = null }, { Text("All") })
                Glossary.categories.forEach { c -> FilterChip(cat == c, { cat = if (cat == c) null else c }, { Text(c) }) }
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(0.8f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(list.size) { i ->
                    val e = list[i]
                    val on = e.term == entry?.term
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { selected = e.term }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(e.term, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(e.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    }
                }
                if (list.isEmpty()) item { Text("No terms found.", Modifier.padding(12.dp)) }
            }
            Surface(Modifier.weight(1.2f).fillMaxSize(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                if (entry != null) FitToHeight(Modifier.padding(14.dp), minScale = 0.6f) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.term, style = MaterialTheme.typography.headlineSmall)
                        Text(entry.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        RichText(entry.definition)
                        entry.visual?.let { VisualView(it, Modifier.fillMaxWidth()) }
                        entry.play?.let { ListenButton(it, 90) }
                    }
                }
            }
        }
    }
}
