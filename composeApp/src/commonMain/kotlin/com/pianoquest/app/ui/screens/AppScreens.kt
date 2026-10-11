package com.pianoquest.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.audio.Instrument
import com.pianoquest.app.data.Achievements
import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.Levels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.currentEpochDay
import com.pianoquest.app.platform.isDesktop
import com.pianoquest.app.platform.platformName
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.Tab
import com.pianoquest.app.ui.ToolId
import com.pianoquest.app.ui.components.Card
import com.pianoquest.app.ui.components.LinearMeter
import com.pianoquest.app.ui.components.AppEmblem
import com.pianoquest.app.ui.components.MoodBadge
import com.pianoquest.app.ui.components.ResultBadge
import com.pianoquest.app.ui.components.TipCard
import com.pianoquest.app.ui.components.PianoKeyboard
import com.pianoquest.app.ui.components.ProgressRing
import com.pianoquest.app.ui.components.ScreenHeader
import com.pianoquest.app.ui.components.SectionTitle
import com.pianoquest.app.ui.theme.PQ
import com.pianoquest.app.ui.theme.woodGrain

private fun toolIcon(t: ToolId): ImageVector = when (t) {
    ToolId.CHORDS -> Icons.Default.AutoAwesome
    ToolId.SCALES -> Icons.Default.GraphicEq
    ToolId.CIRCLE -> Icons.Default.Album
    ToolId.PROGRESSIONS -> Icons.Default.Piano
    ToolId.METRONOME -> Icons.Default.Timer
    ToolId.EAR -> Icons.Default.Hearing
    ToolId.READING -> Icons.Default.Quiz
    ToolId.CHORD_FINDER -> Icons.Default.TravelExplore
    ToolId.GLOSSARY -> Icons.AutoMirrored.Filled.MenuBook
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreScreen() {
    val nav = LocalNavigator.current
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 1000.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tools & More", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuTile("User Guide", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f)) { nav.push(Screen.Guide) }
                MenuTile("Profile & Achievements", Icons.Default.Person, Modifier.weight(1f)) { nav.push(Screen.Profile) }
                MenuTile("Settings & MIDI", Icons.Default.Settings, Modifier.weight(1f)) { nav.push(Screen.Settings) }
                MenuTile("Accessibility", Icons.Default.Accessibility, Modifier.weight(1f)) { nav.push(Screen.Accessibility) }
            }
            Card(Modifier.fillMaxWidth(), onClick = { nav.push(Screen.About) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("About, privacy & licences", style = MaterialTheme.typography.titleSmall)
                        Text("Version, privacy policy and credits for the music, sounds and pictures", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            SectionTitle("Practice tools")
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cols = when { maxWidth > 740.dp -> 3; maxWidth > 500.dp -> 2; else -> 1 }
                val w = (maxWidth - 12.dp * (cols - 1)) / cols
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolId.entries.forEach { t ->
                        Card(Modifier.width(w), onClick = { nav.push(Screen.Tool(t)) }) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                    Icon(toolIcon(t), null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text(t.title, style = MaterialTheme.typography.titleSmall)
                                    Text(t.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            SectionTitle("About")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${com.pianoquest.app.AppInfo.NAME} ${com.pianoquest.app.AppInfo.VERSION} · $platformName", style = MaterialTheme.typography.titleSmall)
                    Text("${CourseCatalog.courses.size} courses · ${CourseCatalog.courses.sumOf { it.lessonCount }} lessons · ${com.pianoquest.app.data.SongLibrary.all.size} songs · " +
                        "${com.pianoquest.app.data.Glossary.entries.size} encyclopedia terms", style = MaterialTheme.typography.bodySmall)
                    Text("Concert grand: Salamander Grand Piano samples. Complete scores from the Mutopia Project. Pop song entries are chord and accompaniment studies.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MenuTile(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier, onClick = onClick, color = MaterialTheme.colorScheme.primaryContainer, border = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val s = p.settings
    val midi = PianoInput.midi
    val devices by midi.devices.collectAsState()
    val connected by midi.connected.collectAsState()
    val status by midi.status.collectAsState()
    val lastActivity by midi.lastActivity.collectAsState()
    var nowTick by remember { mutableStateOf(0L) }
    androidx.compose.runtime.LaunchedEffect(Unit) { while (true) { nowTick = com.pianoquest.app.platform.nowWallMillis(); kotlinx.coroutines.delay(250) } }
    val held by PianoInput.held.collectAsState()
    var confirmReset by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Settings", onBack = { nav.back() })
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle("MIDI keyboard")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Usb, null, tint = if (connected != null) PQ.colors.success else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(status, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { midi.refresh() }) { Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Text(" Refresh") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val age = if (lastActivity == 0L) -1L else (nowTick - lastActivity)
                            Box(Modifier.size(14.dp).clip(CircleShape).background(when {
                                age in 0..400 -> PQ.colors.success
                                connected != null -> PQ.colors.gold.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }))
                            Text(when {
                                connected == null -> "Not receiving"
                                age < 0 -> "Connected - play a key to test"
                                age < 2000 -> "Receiving notes"
                                else -> "Last note ${age / 1000} s ago"
                            }, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { midi.reconnect() }) { Text("Reconnect") }
                        }
                        Text("If notes stop arriving: check the OTG adapter is pushed in fully, then tap Reconnect. The app also reconnects automatically when the keyboard reappears.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        devices.forEach { d ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(d.name, style = MaterialTheme.typography.bodyLarge)
                                    if (d.detail.isNotBlank()) Text(d.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (connected?.id == d.id) OutlinedButton(onClick = { midi.disconnect() }) { Text("Disconnect") }
                                else Button(onClick = { midi.connect(d) }) { Text("Connect") }
                            }
                        }
                        Text(if (isDesktop) "Plug your keyboard in via USB (or a USB-MIDI interface), then press Refresh. Windows may need you to restart the app after plugging in."
                            else "Connect your digital piano with a USB-OTG adapter (USB-C/micro-USB to USB-B). It connects automatically. Bluetooth MIDI keyboards paired in Android's MIDI settings also appear here.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        SettingSwitch("Play sound through the app", "Turn off if your digital piano makes its own sound", s.midiThrough) { v -> ProgressStore.updateSettings { it.copy(midiThrough = v) } }
                        Text("Test: play your keyboard - keys light up here.", style = MaterialTheme.typography.labelMedium)
                        PianoKeyboard(36, 96, Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(8.dp)), labels = KeyLabels.NONE, interactive = false)
                        if (held.isNotEmpty()) Text("Playing: " + held.sorted().joinToString(" ") { com.pianoquest.app.ui.components.keyLabel(it, false, true) })
                    }
                }

                SectionTitle("Sound")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Instrument for Free Play and songs", style = MaterialTheme.typography.titleSmall)
                        Text("Lessons, tests and tools always use the grand piano.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Instrument.entries.forEach { inst ->
                                FilterChip(s.instrument == inst.name, {
                                    ProgressStore.updateSettings { it.copy(instrument = inst.name) }
                                    Audio.setInstrument(inst)
                                    PianoInput.noteOn(60); PianoInput.noteOff(60)
                                }, { Text(inst.displayName) })
                            }
                        }
                        Text("Volume ${(s.volume * 100).toInt()}%", style = MaterialTheme.typography.titleSmall)
                        Slider(s.volume, { v -> ProgressStore.updateSettings { it.copy(volume = v) }; if (Audio.isReady) Audio.engine.masterVolume = v }, valueRange = 0f..1f)
                        Text("Reverb ${(s.reverb * 100).toInt()}%", style = MaterialTheme.typography.titleSmall)
                        Slider(s.reverb, { v -> ProgressStore.updateSettings { it.copy(reverb = v) }; if (Audio.isReady) Audio.engine.reverbAmount = v }, valueRange = 0f..0.6f)
                    }
                }

                SectionTitle("Accessibility")
                Card(Modifier.fillMaxWidth(), onClick = { nav.push(Screen.Accessibility) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.Accessibility, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text("Accessibility options", style = MaterialTheme.typography.titleSmall)
                            Text("Text and display size, bold text, high contrast, colour-blind colours, reduced motion, vibration - with a reset to defaults",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                SectionTitle("Display & learning")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Theme", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("light" to "Light", "dark" to "Dark", "system" to "Follow device").forEach { (k, n) ->
                                FilterChip(s.theme == k, { ProgressStore.updateSettings { it.copy(theme = k) } }, { Text(n) })
                            }
                        }
                        Text("Key labels", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(KeyLabels.NONE to "None", KeyLabels.C_ONLY to "C only", KeyLabels.WHITE to "White keys", KeyLabels.ALL to "All").forEach { (k, n) ->
                                FilterChip(s.keyLabels == k.name, { ProgressStore.updateSettings { it.copy(keyLabels = k.name) } }, { Text(n) })
                            }
                        }
                        SettingSwitch("Allow portrait mode", "Keyscape is designed for landscape. Turn on to let the app rotate to portrait too (the piano screens stay landscape).",
                            s.allowPortrait) { v -> ProgressStore.updateSettings { it.copy(allowPortrait = v) } }
                        SettingSwitch("Solfège note names", "Do Re Mi instead of C D E", s.solfege) { v -> ProgressStore.updateSettings { it.copy(solfege = v) } }
                        SettingSwitch("Sheet music strip in song player", "Show upcoming notes on a small staff under the falling notes", s.showStaffInSongs) { v -> ProgressStore.updateSettings { it.copy(showStaffInSongs = v) } }
                        Text("Daily goal: ${s.dailyGoalMinutes} minutes", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 15, 30, 60).forEach { m -> FilterChip(s.dailyGoalMinutes == m, { ProgressStore.updateSettings { it.copy(dailyGoalMinutes = m) } }, { Text("$m") }) }
                        }
                    }
                }
                if (isDesktop) {
                    SectionTitle("Computer keyboard")
                    Card(Modifier.fillMaxWidth()) {
                        Text("Play with your computer keyboard anywhere in the app:\n• A S D F G H J K L ; = white keys (C D E F G A B C D E)\n• W E T Y U O P = black keys\n• Z / X = octave down / up · Space = sustain · Esc = back",
                            Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                SectionTitle("Data")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        var name by remember { mutableStateOf(p.name) }
                        OutlinedTextField(name, { name = it; ProgressStore.update { pr -> pr.copy(name = it.take(24)) } }, Modifier.fillMaxWidth(), label = { Text("Your name") }, singleLine = true)
                        OutlinedButton(onClick = { confirmReset = true }) { Text("Reset all progress") }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("Reset progress?") },
        text = { Text("This erases lessons, scores, XP, streaks and achievements. Settings are kept.") },
        confirmButton = { TextButton(onClick = { ProgressStore.resetAll(); confirmReset = false }) { Text("Reset", color = PQ.colors.error) } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
    )
}

@Composable
internal fun SettingSwitch(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(value, onChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val level = Levels.level(p.xp)
    val next = Levels.xpForLevel(level + 1)
    val cur = Levels.xpForLevel(level)
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Profile", onBack = { nav.back() })
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 860.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProgressRing(if (level >= 100) 1f else (p.xp - cur).toFloat() / (next - cur).coerceAtLeast(1), 110.dp, 10.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$level", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                            Text("level", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(p.name.ifBlank { "Pianist" }, style = MaterialTheme.typography.headlineMedium)
                        Text(Levels.title(level), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(if (level >= 100) "Maximum level - you're a Virtuoso!" else "${p.xp} XP · ${next - p.xp} XP to level ${level + 1}", style = MaterialTheme.typography.bodyMedium)
                        Text("Journey: Level 1 Novice → 10 Apprentice → 25 Player → 40 Musician → 60 Artist → 80 Maestro → 100 Virtuoso",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    KeyscapeLogo(Modifier.size(84.dp))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Stat("🔥 Streak", "${p.streak} days (best ${p.bestStreak})")
                    Stat("📚 Lessons", "${p.lessonsDone.size}")
                    Stat("✅ Tests passed", "${p.testScores.count { it.value >= ProgressStore.PASS_PERCENT }}")
                    Stat("⭐ Song stars", "${p.songStars.values.sum()}")
                    Stat("🎹 Notes played", "${p.notesPlayed}")
                    Stat("👂 Drill answers", "${p.drillCorrect}")
                }
                SectionTitle("Last 28 days")
                val today = currentEpochDay()
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (27 downTo 0).forEach { ago ->
                        val day = today - ago
                        val mins = p.minutesByDay[day.toString()] ?: 0
                        Box(Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(4.dp)).background(
                            when { mins >= p.settings.dailyGoalMinutes -> PQ.colors.success; mins > 0 -> PQ.colors.success.copy(alpha = 0.45f); else -> MaterialTheme.colorScheme.surfaceVariant }))
                    }
                }
                SectionTitle("Courses")
                CourseCatalog.courses.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { nav.push(Screen.CourseDetail(c.id)) }) {
                        Text(c.title, Modifier.width(200.dp), style = MaterialTheme.typography.bodyMedium)
                        LinearMeter(CourseCatalog.courseProgress(c, p), Modifier.weight(1f), color = Color(c.color))
                        Text(" ${(CourseCatalog.courseProgress(c, p) * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    }
                }
                SectionTitle("Achievements (${p.achievements.size}/${Achievements.all.size})")
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val cols = if (maxWidth > 600.dp) 3 else 2
                    val w = (maxWidth - 10.dp * (cols - 1)) / cols
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Achievements.all.forEach { a ->
                            val got = a.id in p.achievements
                            Surface(Modifier.width(w), shape = RoundedCornerShape(16.dp),
                                color = if (got) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (got) a.emoji else "🔒", style = MaterialTheme.typography.headlineMedium)
                                    Text(a.title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                                    Text(a.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun OnboardingScreen() {
    val nav = LocalNavigator.current
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    val ink = Color(0xFF3A1C10)
    val soft = Color(0xFF6B4A35)
    val buttonColors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.pianoquest.app.ui.theme.Wood.Chestnut,
        contentColor = com.pianoquest.app.ui.theme.Wood.Ivory)
    BoxWithConstraints(Modifier.fillMaxSize().background(LogoCream).padding(horizontal = 24.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
        val wide = maxWidth > maxHeight
        val logoSize = minOf(maxHeight * 0.62f, 260.dp)
        val brand = @Composable { logo: androidx.compose.ui.unit.Dp ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KeyscapeLogo(Modifier.size(logo))
                Tagline(logo, ink)
            }
        }
        val content = @Composable {
            Column(Modifier.widthIn(max = 520.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (page) {
                    0 -> {
                        Text("Welcome to Keyscape", style = MaterialTheme.typography.headlineLarge, color = ink, textAlign = TextAlign.Center)
                        Text("A complete piano school: from your very first note to level 100 - classical, jazz, pop, blues and every part of music theory, " +
                            "with complete pieces, a concert grand sound and MIDI keyboard support.",
                            style = MaterialTheme.typography.bodyLarge, color = soft, textAlign = TextAlign.Center)
                        Button(onClick = { page = 1 }, Modifier.fillMaxWidth().height(54.dp), colors = buttonColors) {
                            Text("Let's begin", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    1 -> {
                        Text("What should we call you?", style = MaterialTheme.typography.headlineSmall, color = ink)
                        OutlinedTextField(name, { name = it.take(24) }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Your name (optional)", color = soft) },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = ink, unfocusedTextColor = ink,
                                focusedContainerColor = Color.White.copy(alpha = 0.6f), unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                                focusedBorderColor = com.pianoquest.app.ui.theme.Wood.Chestnut, unfocusedBorderColor = com.pianoquest.app.ui.theme.Wood.Tan))
                        Button(onClick = { page = 2 }, Modifier.fillMaxWidth().height(52.dp), colors = buttonColors) { Text("Continue") }
                    }
                    else -> {
                        Text("Perfect! We'll start with Piano Foundations.", style = MaterialTheme.typography.titleLarge, color = ink, textAlign = TextAlign.Center)
                        Text("Plug in a MIDI keyboard any time (Settings → MIDI). Try the keys below!", color = soft,
                            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        PianoKeyboard(60, 76, Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(12.dp)), labels = KeyLabels.WHITE)
                        Button(onClick = {
                            ProgressStore.update { it.copy(name = name.trim(), onboarded = true) }
                            nav.stack.clear()
                            nav.stack.add(Screen.Home)
                        }, Modifier.fillMaxWidth().height(52.dp), colors = buttonColors) {
                            Text("Go to Dashboard")
                        }
                    }
                }
            }
        }
        if (wide) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Box(Modifier.weight(0.8f), contentAlignment = Alignment.Center) { brand(logoSize) }
                com.pianoquest.app.ui.components.FitToHeight(Modifier.weight(1.2f).fillMaxSize(), alignment = Alignment.Center) { content() }
            }
        } else {
            com.pianoquest.app.ui.components.FitToHeight(Modifier.fillMaxSize(), alignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    brand(180.dp)
                    content()
                }
            }
        }
    }
}
