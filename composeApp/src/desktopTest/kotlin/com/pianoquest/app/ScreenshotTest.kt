package com.pianoquest.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.use
import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Step
import com.pianoquest.app.data.Visual
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Navigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.ToolId
import com.pianoquest.app.ui.screens.KeyscapeLogo
import com.pianoquest.app.ui.screens.LessonSession
import com.pianoquest.app.ui.screens.LessonSessions
import com.pianoquest.app.ui.screens.LogoCream
import com.pianoquest.app.ui.screens.SplashScreen
import com.pianoquest.app.ui.screens.TAGLINE
import com.pianoquest.app.ui.theme.KeyscapeTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test

/**
 * Renders key screens off-screen to PNG files (build/screenshots) so the UI can be checked without a device.
 * Run with: SCREENSHOTS=true ./gradlew :composeApp:desktopTest --tests '*ScreenshotTest*'
 */
class ScreenshotTest {
    private val out = File("build/screenshots").apply { mkdirs() }
    private val enabled get() = System.getProperty("screenshots") == "true" || System.getenv("SCREENSHOTS") == "true"

    /** A typical phone held in landscape (the app's default): 892 x 412 dp. */
    private val phone = 892 to 412
    /** A small, wide phone: 800 x 360 dp. */
    private val small = 800 to 360
    private val tablet = 1280 to 800

    init {
        System.setProperty("pianoquest.dataDir", File("build/test-data").apply { mkdirs() }.absolutePath)
        Launch.splashShown = true
    }

    private fun render(scene: ImageComposeScene, file: File, frames: Int) {
        var t = 0L
        repeat(frames) { scene.render(t); t += 250_000_000L; Thread.sleep(120) }
        file.writeBytes(scene.render(t).encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }

    private fun shot(name: String, size: Pair<Int, Int>, dark: Boolean = false, screens: List<Screen>, frames: Int = 8, density: Float = 2f,
                     dir: File = out) {
        val nav = Navigator()
        nav.stack.clear()
        nav.stack.addAll(screens)
        ImageComposeScene((size.first * density).toInt(), (size.second * density).toInt(), Density(density)) {
            KeyscapeTheme(if (dark) "dark" else "light") {
                CompositionLocalProvider(LocalNavigator provides nav) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { AppShell(nav) }
                }
            }
        }.use { render(it, File(dir, "$name.png"), frames) }
    }

    /** Opens [lessonId] at the first step matching [pick]. */
    private fun lessonAt(lessonId: String, pick: (Step) -> Boolean): Screen {
        LessonSessions.remove(lessonId)
        val l = CourseCatalog.lesson(lessonId)!!
        val session = LessonSessions.getOrCreate(lessonId) { LessonSession(lessonId, l.title, l.steps, isTest = false, minutes = l.minutes) }
        session.goTo(session.steps.indexOfFirst(pick).coerceAtLeast(0))
        return Screen.LessonPlayer(lessonId)
    }

    private fun sampleProgress() = ProgressStore.update {
        it.copy(onboarded = true, name = "Alex", xp = 1350, streak = 4, favoriteSongs = setOf("fur-elise", "moonlight", "clair-de-lune", "turkish-march"),
            lessonsDone = setOf("foundations.keyboard.welcome", "foundations.keyboard.black-keys", "foundations.keyboard.note-names"))
    }

    /** Google Play listing graphics: 512x512 icon, 1024x500 feature graphic and landscape phone screenshots. */
    @Test
    fun storeAssets() {
        if (!enabled) return
        val dir = File("../docs/play-store").apply { mkdirs() }
        ImageComposeScene(1024, 500, Density(1f)) {
            KeyscapeTheme("light") {
                Row(Modifier.fillMaxSize().background(LogoCream).padding(horizontal = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyscapeLogo(Modifier.size(380.dp))
                    Spacer(Modifier.width(44.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Keyscape", style = MaterialTheme.typography.displayLarge, color = Color(0xFF3A1C10))
                        Text(TAGLINE, style = MaterialTheme.typography.headlineSmall, fontStyle = FontStyle.Italic, color = Color(0xFF3A1C10))
                        Text("Courses · complete classics · falling notes & sheet music · MIDI keyboards", fontSize = 18.sp, color = Color(0xFF6B4A35))
                    }
                }
            }
        }.use { render(it, File(dir, "feature-graphic-1024x500.png"), 4) }
        sampleProgress()
        // 1920 x 1080 landscape screenshots (16:9): phone layout (960 x 540 dp) and tablet layout (1280 x 720 dp).
        dir.listFiles { f -> f.name.startsWith("phone-") || f.name.startsWith("tablet-") }?.forEach { it.delete() }
        val shots = listOf(
            "1-player" to { listOf(Screen.Songs, Screen.SongSetup("fur-elise"), Screen.SongPlayer("fur-elise")) },
            "2-setup" to { listOf(Screen.Songs, Screen.SongSetup("fur-elise")) },
            "3-songs" to { listOf<Screen>(Screen.Songs) },
            "4-lesson" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.title == "Sitting at the piano" }) },
            "5-storybook" to { listOf(Screen.Home, Screen.ComposerDetail("beethoven", openStory = true, storyPage = 3)) },
            "6-chords" to { listOf(Screen.More, Screen.Tool(ToolId.CHORDS)) },
            "7-home" to { listOf<Screen>(Screen.Home) },
            "8-courses" to { listOf<Screen>(Screen.Learn) },
        )
        for ((name, screens) in shots) {
            // The player needs more frames so the music is under way (past the count-in) when captured.
            val frames = if (name.endsWith("player")) 110 else 8
            shot("phone-$name", 1920 / 2 to 1080 / 2, screens = screens(), frames = frames, density = 2f, dir = dir)
            shot("tablet-$name", 1280 to 720, screens = screens(), frames = frames, density = 1.5f, dir = dir)
        }
        // Google Play accepts only 24-bit PNGs (no alpha channel) for the feature graphic and screenshots.
        dir.listFiles { f -> f.extension == "png" && f.name != "icon-512.png" }?.forEach { f ->
            val src = javax.imageio.ImageIO.read(f)
            val rgb = java.awt.image.BufferedImage(src.width, src.height, java.awt.image.BufferedImage.TYPE_INT_RGB)
            rgb.createGraphics().apply { color = java.awt.Color.WHITE; fillRect(0, 0, src.width, src.height); drawImage(src, 0, 0, null); dispose() }
            javax.imageio.ImageIO.write(rgb, "png", f)
        }
    }

    /** The same key screens on a 10" tablet (1280 x 800 dp) and a phone, into build/screenshots/devices. */
    @Test
    fun captureDevices() {
        if (!enabled) return
        sampleProgress()
        val dir = File(out, "devices").apply { mkdirs() }
        val only = System.getenv("SHOTS")?.split(',')?.map { it.trim() }?.toSet()
        val screens: List<Pair<String, () -> List<Screen>>> = listOf(
            "home" to { listOf<Screen>(Screen.Home) },
            "learn" to { listOf(Screen.Learn) },
            "songs" to { listOf(Screen.Songs) },
            "setup" to { listOf(Screen.Songs, Screen.SongSetup("fur-elise")) },
            "player" to { listOf(Screen.Songs, Screen.SongSetup("fur-elise"), Screen.SongPlayer("fur-elise")) },
            "more" to { listOf(Screen.More) },
            "settings" to { listOf(Screen.More, Screen.Settings) },
            "chords" to { listOf(Screen.More, Screen.Tool(ToolId.CHORDS)) },
            "finder" to { listOf(Screen.More, Screen.Tool(ToolId.CHORD_FINDER)) },
            "ear" to { listOf(Screen.More, Screen.Tool(ToolId.EAR)) },
            "reading" to { listOf(Screen.More, Screen.Tool(ToolId.READING)) },
            "circle" to { listOf(Screen.More, Screen.Tool(ToolId.CIRCLE)) },
            "story" to { listOf(Screen.Home, Screen.ComposerDetail("beethoven", openStory = true)) },
            "composer" to { listOf(Screen.Home, Screen.ComposerDetail("mozart")) },
            "lesson-read" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read }) },
            "lesson-hands" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.visual is Visual.Hands }) },
            "lesson-tap" to { listOf(Screen.Home, lessonAt("foundations.keyboard.octaves") { it is Step.TapKeys }) },
            "lesson-play" to { listOf(Screen.Home, lessonAt("foundations.keyboard.five-finger") { it is Step.PlayAlong }) },
            "lesson-quiz" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Quiz }) },
            "lesson-rhythm" to { listOf(Screen.Home, lessonAt("foundations.rhythm.note-values") { it is Step.RhythmTap }) },
            "onboarding" to { listOf(Screen.Onboarding) },
            "lesson-posture" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.title == "Sitting at the piano" }) },
            "lesson-handshape" to { listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.title == "Hand shape" }) },
            "lesson-tie" to { listOf(Screen.Home, lessonAt(com.pianoquest.app.data.CourseCatalog.courses.flatMap { c -> c.allLessons }
                .first { l -> l.steps.any { s -> s is Step.Read && s.title == "The tie" } }.id) { it is Step.Read && it.title == "The tie" }) },
            "lesson-pedals" to { listOf(Screen.Home, lessonAt(com.pianoquest.app.data.CourseCatalog.courses.flatMap { c -> c.allLessons }
                .first { l -> l.steps.any { s -> s is Step.Read && s.title == "The three pedals" } }.id) { it is Step.Read && it.title == "The three pedals" }) },
            "accessibility" to { listOf(Screen.More, Screen.Accessibility) },
            "metronome" to { listOf(Screen.More, Screen.Tool(ToolId.METRONOME)) },
            "scales" to { listOf(Screen.More, Screen.Tool(ToolId.SCALES)) },
            "glossary" to { listOf(Screen.More, Screen.Tool(ToolId.GLOSSARY)) },
            "progressions" to { listOf(Screen.More, Screen.Tool(ToolId.PROGRESSIONS)) },
            "profile" to { listOf(Screen.More, Screen.Profile) },
            "course" to { listOf(Screen.Learn, Screen.CourseDetail("foundations")) },
            "test" to { listOf(Screen.Home, Screen.UnitTest("theory.scales")) },
            "about" to { listOf(Screen.More, Screen.About) },
            "guide" to { listOf(Screen.More, Screen.Guide) },
            "story-beethoven-3" to { listOf(Screen.Home, Screen.ComposerDetail("beethoven", openStory = true, storyPage = 3)) },
            "story-beethoven-5" to { listOf(Screen.Home, Screen.ComposerDetail("beethoven", openStory = true, storyPage = 5)) },
            "story-bach-5" to { listOf(Screen.Home, Screen.ComposerDetail("bach", openStory = true, storyPage = 5)) },
        )
        for ((name, s) in screens) {
            if (only != null && name !in only) continue
            if (name == "player") ProgressStore.updateSettings { it.copy(guideMode = "WATERFALL", playMode = "LISTEN") }
            shot("tablet-$name", tablet, screens = s(), dir = dir, frames = if (name == "player") 30 else 8)
            shot("phone-$name", phone, screens = s(), dir = dir, frames = if (name == "player") 30 else 8)
        }
    }

    /** The song result popup and the splash on a phone, a small phone and a tablet. */
    @Test
    fun captureDialogs() {
        if (!enabled) return
        val dir = File(out, "devices").apply { mkdirs() }
        for ((name, size) in listOf("phone" to phone, "small" to small, "tablet" to tablet)) {
            ImageComposeScene(size.first * 2, size.second * 2, Density(2f)) {
                KeyscapeTheme("light") {
                    Box(Modifier.fillMaxSize().background(Color(0xFF1C130D))) {
                        com.pianoquest.app.ui.screens.SongResultDialog(scored = true, stars = 2, score = 84, right = 91, left = 72, wrong = 6, bestCombo = 40,
                            speed = 0.8f, difficulty = com.pianoquest.app.data.Difficulty.MEDIUM, listen = true, backLabel = "Back to the song list",
                            onDismiss = {}, onAgain = {}, onSetup = {}, onBack = {})
                    }
                }
            }.use { render(it, File(dir, "$name-result-dialog.png"), 6) }
            ImageComposeScene(size.first * 2, size.second * 2, Density(2f)) {
                KeyscapeTheme("light") { SplashScreen {} }
            }.use { render(it, File(dir, "$name-splash.png"), 14) }
        }
    }

    @Test
    fun captureScreens() {
        if (!enabled) return
        sampleProgress()
        ImageComposeScene(phone.first * 2, phone.second * 2, Density(2f)) {
            KeyscapeTheme("light") { SplashScreen {} }
        }.use { render(it, File(out, "00-splash.png"), 14) }
        shot("01-onboarding", phone, screens = listOf(Screen.Onboarding))
        shot("02-home", phone, screens = listOf(Screen.Home))
        shot("03-home-tablet", tablet, screens = listOf(Screen.Home))
        shot("04-learn", phone, screens = listOf(Screen.Learn))
        shot("05-course", phone, screens = listOf(Screen.Learn, Screen.CourseDetail("foundations")))
        shot("06-lesson-welcome", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read }))
        shot("06b-lesson-hands", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.visual is Visual.Hands }))
        shot("06c-lesson-hands-dark", small, dark = true, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Read && it.visual is Visual.Hands }))
        shot("07-lesson-tap", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.octaves") { it is Step.TapKeys }))
        shot("07b-lesson-tap-small", small, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.note-names") { it is Step.TapKeys }))
        shot("08-lesson-play", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.five-finger") { it is Step.PlayAlong }))
        shot("08b-lesson-play-staff", small, screens = listOf(Screen.Home, lessonAt("foundations.reading.treble") { it is Step.PlayAlong || it is Step.TapKeys }))
        shot("09-lesson-quiz", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.welcome") { it is Step.Quiz }))
        shot("09b-lesson-quiz-visual", small, screens = listOf(Screen.Home, lessonAt("foundations.reading.treble") { it is Step.Quiz }))
        shot("10-lesson-rhythm", phone, screens = listOf(Screen.Home, lessonAt("foundations.rhythm.note-values") { it is Step.RhythmTap }))
        shot("11-lesson-classical", phone, screens = listOf(Screen.Home, lessonAt("classical.classical-era.era") { it is Step.Read && it.visual != null }))
        shot("11b-lesson-theory", small, dark = true, screens = listOf(Screen.Home, lessonAt("theory.harmony.circle-progressions") { it is Step.Read }))
        shot("12-songs", phone, screens = listOf(Screen.Songs))
        shot("13-song-setup", phone, screens = listOf(Screen.Songs, Screen.SongSetup("moonlight-3")))
        shot("13b-song-setup-small", small, screens = listOf(Screen.Songs, Screen.SongSetup("fur-elise")))
        for (g in listOf("WATERFALL", "FALLING_NOTE", "SHEET_MUSIC")) {
            ProgressStore.updateSettings { it.copy(guideMode = g, playMode = "LISTEN") }
            shot("14-player-${g.lowercase()}", phone, screens = listOf(Screen.Songs, Screen.SongSetup("fur-elise"), Screen.SongPlayer("fur-elise")), frames = 40)
        }
        ProgressStore.updateSettings { it.copy(guideMode = "WATERFALL", playMode = "PRACTICE") }
        shot("15-free-play", phone, dark = true, screens = listOf(Screen.Play))
        shot("16-chords", phone, screens = listOf(Screen.More, Screen.Tool(ToolId.CHORDS)))
        shot("17-circle", phone, dark = true, screens = listOf(Screen.More, Screen.Tool(ToolId.CIRCLE)))
        shot("18-profile", phone, screens = listOf(Screen.More, Screen.Profile))
        shot("19-settings", phone, dark = true, screens = listOf(Screen.More, Screen.Settings))
        shot("20-reading", phone, screens = listOf(Screen.More, Screen.Tool(ToolId.READING)))
        shot("21-more", phone, screens = listOf(Screen.More))
        shot("22-test", phone, screens = listOf(Screen.Home, Screen.UnitTest("theory.scales")))
        shot("23-about", phone, screens = listOf(Screen.More, Screen.About))
        shot("24-ear", phone, screens = listOf(Screen.More, Screen.Tool(ToolId.EAR)))
        shot("25-metronome", phone, screens = listOf(Screen.More, Screen.Tool(ToolId.METRONOME)))
        shot("26-progressions", small, screens = listOf(Screen.More, Screen.Tool(ToolId.PROGRESSIONS)))
        shot("27-scales", small, screens = listOf(Screen.More, Screen.Tool(ToolId.SCALES)))
        shot("28-glossary", small, screens = listOf(Screen.More, Screen.Tool(ToolId.GLOSSARY)))
        shot("29-finder", small, screens = listOf(Screen.More, Screen.Tool(ToolId.CHORD_FINDER)))
        shot("30-composer", phone, screens = listOf(Screen.Home, Screen.ComposerDetail("mozart")))
        shot("31-storybook", phone, screens = listOf(Screen.Home, Screen.ComposerDetail("beethoven", openStory = true)))
        shot("32-scale-fingering", small, screens = listOf(Screen.Home, lessonAt("classical.technique.scales") { it is Step.PlayAlong }))
        shot("33-home-lower", phone, screens = listOf(Screen.Home), frames = 6)
        shot("34-sevenths-table", small, screens = listOf(Screen.Home, lessonAt("theory.chords.sevenths") { it is Step.Read && it.visual is Visual.Table }))
        shot("35-short-read", phone, screens = listOf(Screen.Home, lessonAt("foundations.keyboard.black-keys") { it is Step.Read }))
    }
}
