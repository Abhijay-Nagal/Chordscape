package com.pianoquest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pianoquest.app.data.Illustration
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.IllustrationView
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.theme.PQ
import com.pianoquest.app.ui.theme.Wood
import com.pianoquest.app.ui.theme.woodGrain

/** One chapter of the user guide: a short explanation and a picture. */
private class GuideTopic(val title: String, val icon: ImageVector, val body: String, val picture: @Composable () -> Unit)

private val TOPICS: List<GuideTopic> = listOf(
    GuideTopic("Getting around", Icons.Default.Home, """
        The bar on the left takes you anywhere in one tap:

        - **Home** - continue your course, today's practice, the song of the day and quick tools.
        - **Play** - a free piano: play, record yourself and change the sound.
        - **Learn** - every course, unit and lesson, from your first note to advanced harmony.
        - **Songs** - the song library, your favourites and your own MIDI files.
        - **More** - tools, this guide, your profile, settings and accessibility.

        The back arrow (or your phone's back gesture) always returns to where you were.
    """) { IconLegend(listOf(Icons.Default.Home to "Home", Icons.Default.Piano to "Play", Icons.Default.School to "Learn",
        Icons.Default.LibraryMusic to "Songs", Icons.Default.MoreHoriz to "More")) },

    GuideTopic("Lessons", Icons.Default.School, """
        Each lesson is a short series of pages. Read, listen, answer and play - then press **Continue**.

        - Pages never scroll: long explanations are split into pages (**Next page**).
        - **Playing steps**: tap the keys on screen or play them on a MIDI keyboard. Unless the exercise is about a particular
          octave, **any octave counts**.
        - Made a mistake? Press **Retry** in the top bar to play the step again.
        - **Rhythm steps**: after the count-in, tap the pad (or any key). Each tap flashes green (on the beat), amber (a little
          early or late) or red (off the beat).
        - At the end of each unit there is a **unit test** - 70% passes.
    """) { IconLegend(listOf(Icons.Default.TouchApp to "Tap the keys", Icons.Default.Refresh to "Retry", Icons.Default.Hearing to "Listen",
        Icons.Default.Quiz to "Answer")) },

    GuideTopic("Playing a song", Icons.Default.LibraryMusic, """
        Open any song to choose how to play it, then press **START**:

        - **Difficulty** - Easy (melody and bass), Medium, or Hard (every note of the score).
        - **Guide mode** - how the notes are shown: **Sheet Music**, **Waterfall** bars or **Fallen Note** balls.
        - **Play mode** - **Note Wait** waits for you to play each note, **Practice** keeps time and scores you,
          **Listen** plays the piece for you (play along if you like - in-time notes turn green).
        - In Note Wait and Practice choose which **hand** you play; the app plays the other.
    """) { GuideModes() },

    GuideTopic("The song player", Icons.Default.PlayCircle, """
        Tap the dark area (or ▶) to start and pause. Along the top:

        - **Restart**, **Speed** (slow down to learn), **Metronome** (tempo follows the music).
        - **Hand** (Listen mode) - show both hands' notes, only the right or only the left. The music still plays both hands, and
          only the notes you see are scored.
        - **Pedal** - songs press the sustain pedal where the music needs it; you can also hold it yourself.
        - **Keys** - how many keys are shown; drag the strip above the keys to move along the keyboard.
        - **A-B loop** (next to the progress bar) - repeat a passage until it is easy.

        When the piece ends you see your score for each hand, and can play again, change the setup or go back to the song list.
    """) { IconLegend(listOf(Icons.Default.Pause to "Play / pause", Icons.Default.Replay to "Restart", Icons.Default.Speed to "Speed",
        Icons.Default.Timer to "Metronome", Icons.Default.PanTool to "Hand", PedalIcon to "Pedal", Icons.Default.Piano to "Keys",
        Icons.Default.Repeat to "A-B loop", Icons.Default.MoreVert to "Menu"), dark = true) },

    GuideTopic("Your progress", Icons.Default.EmojiEvents, """
        - Every song shows your **best accuracy for each hand** - one bar for the right hand, one for the left. They only go up.
        - Songs also earn **stars** (up to 3) for your best overall score.
        - Lessons, tests, songs and drills give **XP**; XP raises your **level** from 1 (Novice) to 100 (Virtuoso).
        - Practise on consecutive days to grow your **streak** 🔥 and reach your daily goal (set it in Settings).
    """) { HandBarsDemo() },

    GuideTopic("Free play & recording", Icons.Default.Piano, """
        **Play** is a full 88-key piano.

        - **Pinch** with two fingers above the keys to show more or fewer keys; drag the strip to move.
        - **Record** (●) yourself, then press ▶ to listen back.
        - Choose an **instrument** (grand piano, electric piano, organ, strings ...) and shape the sound in the menu.
        - **Dual keyboard** (menu) gives you two keyboards, for duets or two registers.
    """) { IconLegend(listOf(Icons.Default.FiberManualRecord to "Record", Icons.Default.PlayCircle to "Replay", Icons.Default.MusicNote to "Instrument",
        Icons.Default.MoreVert to "Menu"), dark = true) },

    GuideTopic("Connecting a MIDI keyboard", Icons.Default.Usb, """
        Practise on real keys:

        1. Connect your digital piano's **USB-B (printer-type) port** to your phone or tablet with a **USB-OTG adapter** or cable.
        2. Keyscape connects automatically - check **Settings → MIDI keyboard**: the light turns green when notes arrive.
        3. If your piano makes its own sound, turn off *Play sound through the app*.

        Bluetooth MIDI keyboards work too once paired in Android's settings. If notes stop, press **Reconnect**.
    """) { MidiDiagram() },

    GuideTopic("Practice tools", Icons.Default.AutoAwesome, """
        In **More** you'll find tools for everyday practice:

        - **Chord Explorer** and **Scale Explorer** - see and hear any chord or scale.
        - **Chord Finder** - play notes and Keyscape names the chord.
        - **Circle of Fifths** and **Progression Player** - harmony at a glance.
        - **Ear Trainer** and **Note Reading Trainer** - quick drills with instant feedback.
        - **Metronome** and the **Theory Encyclopedia**.
    """) { IconLegend(listOf(Icons.Default.AutoAwesome to "Chords", Icons.Default.TravelExplore to "Chord Finder", Icons.Default.Hearing to "Ear Trainer",
        Icons.Default.Quiz to "Note Reading", Icons.Default.Timer to "Metronome", Icons.AutoMirrored.Filled.MenuBook to "Encyclopedia")) },

    GuideTopic("Good posture", Icons.Default.Star, """
        Sit on the front half of the bench, facing middle C, forearms level with the keys and feet flat on the floor.
        Keep your hands rounded - as if holding a small bubble - and play on your fingertips.

        Short, regular practice beats long, rare sessions: 10-15 focused minutes a day is a great start.
    """) { IllustrationView(Illustration.POSTURE) },

    GuideTopic("Settings & accessibility", Icons.Default.Accessibility, """
        - **Settings** - MIDI, sound, theme (light or dark), key labels, solfège, daily goal and your name.
        - **Accessibility** - text size, display size, bold text, high contrast, colour-blind friendly colours, reduced motion,
          large note names on the keys, vibration on key press, a visual metronome flash and keeping the screen on.
          **Reset to defaults** puts them all back.

        Keyscape also follows your device's own font size and works with TalkBack.
    """) { IconLegend(listOf(Icons.Default.Settings to "Settings", Icons.Default.Accessibility to "Accessibility", Icons.Default.Whatshot to "Daily goal")) },
)

/** The illustrated user guide: chapters on the left, the chosen chapter with its picture on the right. */
@Composable
fun GuideScreen() {
    val nav = LocalNavigator.current
    var index by remember { mutableIntStateOf(0) }
    val topic = TOPICS[index]
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("User Guide", style = MaterialTheme.typography.titleLarge)
            Text("  ·  how to use Keyscape", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
        Row(Modifier.weight(1f).fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Chapters
            LazyColumn(Modifier.width(200.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(TOPICS) { i, t ->
                    val on = i == index
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { index = i }.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(t.icon, null, tint = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(t.title, style = MaterialTheme.typography.titleSmall, maxLines = 2,
                            color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            // The chapter
            Surface(Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        val wide = maxWidth > 460.dp
                        if (wide) {
                            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                Column(Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(topic.title, style = MaterialTheme.typography.headlineSmall)
                                    RichText(topic.body)
                                }
                                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { topic.picture() }
                            }
                        } else {
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(topic.title, style = MaterialTheme.typography.headlineSmall)
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { topic.picture() }
                                RichText(topic.body)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { index-- }, enabled = index > 0) { Icon(Icons.Default.ChevronLeft, null); Text("Previous") }
                        Text("${index + 1} / ${TOPICS.size}", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Button(onClick = { index++ }, enabled = index < TOPICS.lastIndex) { Text("Next"); Icon(Icons.Default.ChevronRight, null) }
                    }
                }
            }
        }
    }
}

/** Icons with their names, as they appear in the app. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconLegend(items: List<Pair<ImageVector, String>>, vertical: Boolean = false, dark: Boolean = false) {
    val bg = if (dark) Modifier.clip(RoundedCornerShape(16.dp)).woodGrain(Wood.Mahogany) else
        Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)
    val fg = if (dark) Wood.Parchment else MaterialTheme.colorScheme.onSurface
    val chip = @Composable { icon: ImageVector, label: String ->
        Column(Modifier.width(96.dp).padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(if (dark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) { Icon(icon, label, tint = if (dark) Wood.Gold else MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) }
            Text(label, style = MaterialTheme.typography.labelMedium, color = fg, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
    if (vertical) {
        Column(bg.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { items.forEach { (i, l) -> chip(i, l) } }
    } else {
        FlowRow(bg.padding(10.dp), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEach { (i, l) -> chip(i, l) }
        }
    }
}

/** The three guide modes and play modes with their own pictures. */
@Composable
private fun GuideModes() {
    Column(Modifier.clip(RoundedCornerShape(16.dp)).woodGrain(Wood.Walnut).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Guide mode", color = Wood.Ivory, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GuideMode.entries.forEach { g ->
                Column(Modifier.width(100.dp).clip(RoundedCornerShape(10.dp)).background(Wood.Ivory).padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GuideIcon(g, Modifier.size(34.dp)); Text(g.label, color = Wood.Walnut, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Text("Play mode", color = Wood.Ivory, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(PlayMode.PRACTICE, PlayMode.PERFORM, PlayMode.LISTEN).forEach { m ->
                Column(Modifier.width(100.dp).clip(RoundedCornerShape(10.dp)).background(Wood.Ivory).padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(playIcon(m), null, tint = Wood.Chestnut, modifier = Modifier.size(30.dp)); Text(m.label, color = Wood.Walnut, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/** A song card's two hand bars, as in the song list. */
@Composable
private fun HandBarsDemo() {
    val c = PQ.colors
    Surface(Modifier.widthIn(max = 380.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Für Elise", style = MaterialTheme.typography.titleMedium)
            listOf("Right hand" to 0.86f, "Left hand" to 0.62f).forEachIndexed { i, (label, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(v * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
                }
                com.pianoquest.app.ui.components.LinearMeter(v, Modifier.fillMaxWidth(), color = if (i == 0) c.keyRight else c.keyLeft, height = 10.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                com.pianoquest.app.ui.components.Stars(2, size = 22.dp)
                Spacer(Modifier.width(8.dp))
                Text("best score: 2 stars", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Phone or tablet - OTG adapter - USB cable - digital piano. */
@Composable
private fun MidiDiagram() {
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    Canvas(Modifier.fillMaxWidth().aspectRatio(1.8f)) {
        val u = size.width / 1.8f
        fun label(s: String, x: Float, y: Float) {
            val l = measurer.measure(s, androidx.compose.ui.text.TextStyle(fontSize = androidx.compose.ui.unit.TextUnit(0.05f * u / density / fontScale,
                androidx.compose.ui.unit.TextUnitType.Sp), fontWeight = FontWeight.SemiBold, color = ink))
            drawText(l, topLeft = Offset(x * u - l.size.width / 2f, y * u))
        }
        // Tablet / phone
        drawRoundRect(Color(0xFF2A2A2A), Offset(0.05f * u, 0.2f * u), Size(0.42f * u, 0.3f * u), CornerRadius(0.03f * u))
        drawRoundRect(Color(0xFFF3E7CF), Offset(0.07f * u, 0.22f * u), Size(0.38f * u, 0.26f * u), CornerRadius(0.02f * u))
        for (i in 0 until 10) drawRect(if (i % 2 == 0) Color.White else Color(0xFFE0D6C2), Offset((0.09f + i * 0.034f) * u, 0.36f * u), Size(0.03f * u, 0.1f * u))
        label("Phone / tablet", 0.26f, 0.53f)
        // OTG adapter and cable
        drawRoundRect(Color(0xFF555555), Offset(0.47f * u, 0.32f * u), Size(0.1f * u, 0.06f * u), CornerRadius(0.01f * u))
        label("USB-OTG adapter", 0.52f, 0.12f)
        drawLine(accent, Offset(0.52f * u, 0.2f * u), Offset(0.52f * u, 0.31f * u), strokeWidth = 0.006f * u)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0.57f * u, 0.35f * u); cubicTo(0.75f * u, 0.35f * u, 0.8f * u, 0.7f * u, 1.0f * u, 0.62f * u)
        }
        drawPath(path, Color(0xFF333333), style = Stroke(0.014f * u))
        label("USB cable (A to B)", 0.82f, 0.4f)
        // Digital piano
        drawRoundRect(Wood.Walnut, Offset(0.98f * u, 0.5f * u), Size(0.78f * u, 0.22f * u), CornerRadius(0.02f * u))
        for (i in 0 until 22) drawRect(Wood.Ivory, Offset((1.0f + i * 0.034f) * u, 0.62f * u), Size(0.031f * u, 0.09f * u))
        drawRect(Color(0xFF1B140F), Offset(1.0f * u, 0.52f * u), Size(0.74f * u, 0.08f * u))
        drawRoundRect(accent, Offset(1.0f * u, 0.555f * u), Size(0.05f * u, 0.025f * u), CornerRadius(0.005f * u))
        label("Digital piano (USB-B port)", 1.37f, 0.78f)
    }
}
