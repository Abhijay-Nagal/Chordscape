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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pianoquest.app.data.Course
import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.CourseIcon
import com.pianoquest.app.data.Levels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.data.Step
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.currentEpochDay
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.Tab
import com.pianoquest.app.ui.ToolId
import com.pianoquest.app.ui.components.Card
import com.pianoquest.app.ui.components.GradientBanner
import com.pianoquest.app.ui.components.LinearMeter
import com.pianoquest.app.ui.components.AppEmblem
import com.pianoquest.app.ui.components.MoodBadge
import com.pianoquest.app.ui.components.ResultBadge
import com.pianoquest.app.ui.components.TipCard
import com.pianoquest.app.ui.components.Pill
import com.pianoquest.app.ui.components.ProgressRing
import com.pianoquest.app.ui.components.ScreenHeader
import com.pianoquest.app.ui.components.SectionTitle
import com.pianoquest.app.ui.components.Stars
import com.pianoquest.app.ui.components.toColor
import com.pianoquest.app.ui.theme.woodGrain

val TIPS = listOf(
    "Practise slowly. Speed is a by-product of accuracy!",
    "Count out loud - it fixes most rhythm problems.",
    "Keep your fingers curved, like holding a bubble.",
    "Short daily sessions beat one long weekly session.",
    "Learn hands separately, then put them together slowly.",
    "Listen to recordings of pieces you're learning.",
    "Every mistake is information. Slow down and fix it.",
    "Name the chords in the pieces you play - it speeds up memorisation.",
    "Try the Ear Trainer for 5 minutes a day.",
    "Connect a MIDI keyboard in Settings for the real feel.",
    "Record yourself in Play mode and listen back.",
    "Relax your shoulders. Tension is the enemy of speed.",
    "Sight-read something new every day, even if it's easy.",
    "Use the metronome - but also practise feeling the pulse without it.",
)

fun courseIcon(icon: CourseIcon): ImageVector = when (icon) {
    CourseIcon.FOUNDATIONS -> Icons.Default.Piano
    CourseIcon.THEORY -> Icons.AutoMirrored.Filled.MenuBook
    CourseIcon.EAR -> Icons.Default.Hearing
    CourseIcon.SIGHT -> Icons.Default.Quiz
    CourseIcon.TECHNIQUE -> Icons.Default.Whatshot
    CourseIcon.IMPROV -> Icons.Default.AutoAwesome
    else -> Icons.Default.MusicNote
}

@Composable
fun HomeScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val midiDevice by PianoInput.midi.connected.collectAsState()
    val level = Levels.level(p.xp)
    val nextXp = Levels.xpForLevel(level + 1)
    val curXp = Levels.xpForLevel(level)
    val levelProgress = if (level >= 100) 1f else (p.xp - curXp).toFloat() / (nextXp - curXp).coerceAtLeast(1)
    val minutesToday = ProgressStore.minutesToday(p)
    val courses = CourseCatalog.courses
    val currentCourse = p.lastLessonId?.let { CourseCatalog.courseOf(it) }?.takeIf { c -> CourseCatalog.nextLesson(c, p.lessonsDone) != null }
        ?: courses.firstOrNull { CourseCatalog.nextLesson(it, p.lessonsDone) != null } ?: courses.first()
    val next = CourseCatalog.nextLesson(currentCourse, p.lessonsDone)
    val today by com.pianoquest.app.data.Today.day.collectAsState()
    val tip = TIPS[Math.floorMod(today, TIPS.size.toLong()).toInt()]
    val songOfDay = com.pianoquest.app.data.Today.songOfTheDay(today)

    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text((if (p.lessonsDone.isEmpty()) "Welcome" else greeting()) + if (p.name.isNotBlank()) ", ${p.name}" else "", style = MaterialTheme.typography.headlineMedium)
                    Text("Level $level · ${Levels.title(level)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill("${p.streak}", icon = Icons.Default.Whatshot, color = Color(0xFFFFE3C2), contentColor = Color(0xFFB34700))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clip(CircleShape).clickable { nav.push(Screen.Profile) }) {
                        ProgressRing(levelProgress, 52.dp, 5.dp) {
                            Text("$level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            TipCard(tip)

            // Continue card
            GradientBanner(listOf(currentCourse.color.toColor(), com.pianoquest.app.ui.theme.Wood.Walnut), Modifier.fillMaxWidth(),
                onClick = { next?.let { nav.push(Screen.LessonPlayer(it.id)) } ?: nav.push(Screen.CourseDetail(currentCourse.id)) }) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (p.lessonsDone.isEmpty()) "START YOUR JOURNEY" else "CONTINUE LEARNING", style = MaterialTheme.typography.labelLarge)
                        Text(next?.title ?: currentCourse.title, style = MaterialTheme.typography.headlineSmall, maxLines = 2)
                        Text(currentCourse.title + (next?.let { " · ${it.minutes} min" } ?: ""), style = MaterialTheme.typography.bodyMedium)
                        LinearMeter(CourseCatalog.courseProgress(currentCourse, p), Modifier.width(220.dp).padding(top = 6.dp), color = Color.White)
                    }
                    Icon(Icons.Default.PlayCircle, "Start", Modifier.size(64.dp))
                }
            }

            // Today
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Today", "$minutesToday / ${p.settings.dailyGoalMinutes} min", (minutesToday.toFloat() / p.settings.dailyGoalMinutes).coerceIn(0f, 1f), Modifier.weight(1f))
                StatCard("Lessons", "${p.lessonsDone.size} / ${courses.sumOf { it.lessonCount }}", p.lessonsDone.size.toFloat() / courses.sumOf { it.lessonCount }, Modifier.weight(1f))
                StatCard("XP", "${p.xp}", levelProgress, Modifier.weight(1f))
            }

            // Quick practice
            SectionTitle("Quick practice")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("Free Play", "Just play", Icons.Default.Piano, Color(0xFF6B3C21)) { nav.switchTab(Tab.PLAY) }
                QuickAction("Ear Trainer", "5-minute drill", Icons.Default.Hearing, Color(0xFF55693A)) { nav.push(Screen.Tool(ToolId.EAR)) }
                QuickAction("Note Reading", "Speed drill", Icons.Default.Quiz, Color(0xFF3A4A5E)) { nav.push(Screen.Tool(ToolId.READING)) }
                QuickAction("Song of the day", songOfDay.title, Icons.Default.MusicNote, Color(0xFF7A2E2A)) { nav.openSong(songOfDay.id) }
                QuickAction("Chords", "Explorer", Icons.Default.AutoAwesome, Color(0xFFA07A2E)) { nav.push(Screen.Tool(ToolId.CHORDS)) }
            }

            // MIDI status
            Card(Modifier.fillMaxWidth(), onClick = { nav.push(Screen.Settings) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Usb, null, tint = if (midiDevice != null) PQColorsSuccess() else MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(if (midiDevice != null) "MIDI keyboard connected" else "Got a digital piano or MIDI keyboard?", style = MaterialTheme.typography.titleSmall)
                        Text(midiDevice?.name ?: "Connect it via USB to play lessons and songs on real keys.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(if (midiDevice != null) "✓" else "Set up", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Courses
            SectionTitle("Your courses", action = "See all", onAction = { nav.switchTab(Tab.LEARN) })
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                courses.forEach { c -> CourseMiniCard(c, CourseCatalog.courseProgress(c, p)) { nav.push(Screen.CourseDetail(c.id)) } }
            }

            // Composers - most famous first
            SectionTitle("Composers")
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(com.pianoquest.app.data.Composers.all.size) { i ->
                    val comp = com.pianoquest.app.data.Composers.all[i]
                    Card(Modifier.width(132.dp), onClick = { nav.push(Screen.ComposerDetail(comp.id)) }) {
                        Column {
                            ComposerPortrait(comp, Modifier.fillMaxWidth().height(150.dp), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(comp.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(comp.years, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${comp.songs.size} pieces", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Popular pieces
            SectionTitle("Play a song", action = "Library", onAction = { nav.switchTab(Tab.SONGS) })
            val popular = remember { com.pianoquest.app.data.Popularity.sort(SongLibrary.all).take(30) }
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(popular.size) { i ->
                    val s = popular[i]
                    Card(Modifier.width(190.dp), onClick = { nav.openSong(s.id) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
                            Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.height(40.dp))
                            Text(s.composer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(s.difficultyLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Stars(p.songStars[s.id] ?: 0, size = 14.dp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PQColorsSuccess() = com.pianoquest.app.ui.theme.PQ.colors.success

private fun greeting(): String = "Welcome back"

@Composable
private fun StatCard(label: String, value: String, progress: Float, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            LinearMeter(progress, Modifier.fillMaxWidth(), height = 6.dp)
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        Modifier.width(140.dp).clip(RoundedCornerShape(12.dp)).woodGrain(color, seed = title.length)
            .clickable(onClick = onClick).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(30.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall)
        Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CourseMiniCard(c: Course, progress: Float, onClick: () -> Unit) {
    Card(Modifier.width(200.dp), onClick = onClick) {
        Column {
            Box(Modifier.fillMaxWidth().height(72.dp).woodGrain(c.color.toColor(), seed = c.id.length),
                contentAlignment = Alignment.CenterStart) {
                Icon(courseIcon(c.icon), null, tint = Color.White, modifier = Modifier.padding(16.dp).size(36.dp))
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(c.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${c.lessonCount} lessons · ${c.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                LinearMeter(progress, Modifier.fillMaxWidth().padding(top = 4.dp), color = c.color.toColor(), height = 6.dp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val groups = listOf(
        "Start here" to listOf("foundations", "theory"),
        "Styles & genres" to listOf("classical", "romantic", "jazz", "pop", "blues", "gospel", "latin", "film"),
        "Skills" to listOf("ear", "sight", "technique", "improv"),
    )
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 1100.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Learn", style = MaterialTheme.typography.headlineLarge)
            TipCard("Every course goes from the basics to advanced. New here? Start with Piano Foundations, then Music Theory A-Z!")
            for ((title, ids) in groups) {
                SectionTitle(title)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val cols = when { maxWidth > 900.dp -> 3; maxWidth > 560.dp -> 2; else -> 1 }
                    val w = (maxWidth - 12.dp * (cols - 1)) / cols
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ids.mapNotNull { CourseCatalog.course(it) }.forEach { c ->
                            CourseCard(c, CourseCatalog.courseProgress(c, p), Modifier.width(w)) { nav.push(Screen.CourseDetail(c.id)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CourseCard(c: Course, progress: Float, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier, onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).woodGrain(c.color.toColor(), seed = c.id.length),
                contentAlignment = Alignment.Center) {
                Icon(courseIcon(c.icon), null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(c.title, style = MaterialTheme.typography.titleMedium)
                Text(c.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearMeter(progress, Modifier.weight(1f), color = c.color.toColor(), height = 6.dp)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                }
                Text("${c.units.size} units · ${c.lessonCount} lessons · ${c.level}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CourseScreen(courseId: String) {
    val nav = LocalNavigator.current
    val course = CourseCatalog.course(courseId) ?: return
    val p by ProgressStore.state.collectAsState()
    val c = com.pianoquest.app.ui.theme.PQ.colors
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(course.title, onBack = { nav.back() }, subtitle = course.level)
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 860.dp).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GradientBanner(listOf(course.color.toColor(), com.pianoquest.app.ui.theme.Wood.Walnut), Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(course.subtitle, style = MaterialTheme.typography.titleMedium)
                            Text(course.description, style = MaterialTheme.typography.bodyMedium)
                            course.recommendedAfter?.let { r -> CourseCatalog.course(r)?.let { Text("Recommended after: ${it.title}", style = MaterialTheme.typography.labelMedium) } }
                        }
                        ProgressRing(CourseCatalog.courseProgress(course, p), 72.dp, 7.dp, color = Color.White, track = Color.White.copy(alpha = 0.25f)) {
                            Text("${(CourseCatalog.courseProgress(course, p) * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                course.units.forEachIndexed { ui, unit ->
                    val doneCount = unit.lessons.count { it.id in p.lessonsDone }
                    val testScore = p.testScores[unit.testId]
                    val isOpen = expanded[unit.id] ?: (doneCount < unit.lessons.size || ui == 0)
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth().clickable { expanded[unit.id] = !isOpen }, verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(36.dp).clip(CircleShape).background(course.color.toColor()), contentAlignment = Alignment.Center) {
                                    Text("${ui + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(unit.title, style = MaterialTheme.typography.titleMedium)
                                    Text(unit.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("$doneCount/${unit.lessons.size}", style = MaterialTheme.typography.labelLarge)
                            }
                            if (isOpen) {
                                unit.lessons.forEach { l ->
                                    val done = l.id in p.lessonsDone
                                    val unlocked = CourseCatalog.isUnlocked(l, p)
                                    val interactive = l.steps.count { it !is Step.Read }
                                    Row(
                                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                            .background(if (done) c.success.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer)
                                            .clickable(enabled = unlocked) { nav.push(Screen.LessonPlayer(l.id)) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            when { done -> Icons.Default.CheckCircle; unlocked -> Icons.Default.PlayArrow; else -> Icons.Default.Lock },
                                            null, tint = when { done -> c.success; unlocked -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline },
                                        )
                                        Column(Modifier.weight(1f)) {
                                            Text(l.title, style = MaterialTheme.typography.titleSmall,
                                                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                                            Text("${l.summary}  ·  ${l.minutes} min · $interactive activities", style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.EmojiEvents, null, tint = c.gold)
                                    Column(Modifier.weight(1f)) {
                                        Text("Unit test", style = MaterialTheme.typography.titleSmall)
                                        Text(when {
                                            testScore == null -> "Pass with ${ProgressStore.PASS_PERCENT}% - or test out to skip ahead"
                                            testScore >= ProgressStore.PASS_PERCENT -> "Passed · best $testScore%"
                                            else -> "Best $testScore% - try again"
                                        }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (doneCount == unit.lessons.size) Button(onClick = { nav.push(Screen.UnitTest(unit.id)) }) { Text("Take test") }
                                    else OutlinedButton(onClick = { nav.push(Screen.UnitTest(unit.id)) }) { Text("Test out") }
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

