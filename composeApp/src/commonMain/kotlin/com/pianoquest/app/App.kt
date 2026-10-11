package com.pianoquest.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.audio.Instrument
import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.input.ComputerKeyboard
import com.pianoquest.app.input.InputSource
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.PlatformBackHandler
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Navigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.Tab
import com.pianoquest.app.ui.components.AppEmblem
import com.pianoquest.app.ui.components.MoodBadge
import com.pianoquest.app.ui.components.ResultBadge
import com.pianoquest.app.ui.components.TipCard
import com.pianoquest.app.ui.screens.CourseScreen
import com.pianoquest.app.ui.screens.HomeScreen
import com.pianoquest.app.ui.screens.LearnScreen
import com.pianoquest.app.ui.screens.LessonScreen
import com.pianoquest.app.ui.screens.MoreScreen
import com.pianoquest.app.ui.screens.OnboardingScreen
import com.pianoquest.app.ui.screens.ProfileScreen
import com.pianoquest.app.ui.screens.SettingsScreen
import com.pianoquest.app.ui.screens.PianoStageScreen
import com.pianoquest.app.ui.screens.SongsScreen
import com.pianoquest.app.ui.screens.ToolScreen
import com.pianoquest.app.ui.theme.KeyscapeTheme
import com.pianoquest.app.ui.screens.KeyscapeLogo
import com.pianoquest.app.ui.screens.SplashScreen
import com.pianoquest.app.platform.setAllowPortrait
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun App() {
    val progress by ProgressStore.state.collectAsState()
    val nav = remember { Navigator() }

    // Apply persisted settings to the audio engine and input.
    // The chosen instrument is used only on the piano stage; lessons and everything else always use the grand piano.
    LaunchedEffect(progress.settings, nav.stage) {
        val s = progress.settings
        PianoInput.playMidiThroughApp = s.midiThrough
        PianoInput.touchCurve = s.touchSensitivity
        if (Audio.isReady) {
            // Sound effects and transposition are for the piano stage; lessons always sound neutral and in tune.
            Audio.setFx(if (nav.stage) s.soundFx() else com.pianoquest.app.audio.SoundFx(roomSize = s.fxRoomSize))
            Audio.engine.masterVolume = s.volume
            Audio.engine.reverbAmount = s.reverb
            val chosen = runCatching { Instrument.valueOf(s.instrument) }.getOrDefault(Instrument.GRAND_PIANO)
            Audio.setInstrument(if (nav.stage) chosen else Instrument.GRAND_PIANO)
        }
    }
    LaunchedEffect(progress.settings.allowPortrait) { setAllowPortrait(progress.settings.allowPortrait) }
    LaunchedEffect(progress.settings.keepScreenOn) { com.pianoquest.app.platform.setKeepScreenOn(progress.settings.keepScreenOn) }
    // "Today" (song of the day, tip, daily goal) follows the calendar even if the app stays open overnight.
    LaunchedEffect(Unit) { while (true) { com.pianoquest.app.data.Today.refresh(); delay(30_000) } }
    LaunchedEffect(Unit) {
        CourseCatalog.courses.size // build the catalogue (and level scale) early
        if (!progress.onboarded) nav.stack.apply { clear(); add(Screen.Onboarding) }
        // Count notes played and note MIDI usage for achievements.
        PianoInput.events.collect { e ->
            if (e.down && e.source != InputSource.DEMO) {
                ProgressStore.addNotesPlayed(1)
                if (e.source == InputSource.MIDI) ProgressStore.markMidiUsed()
            }
        }
    }

    val s = progress.settings
    KeyscapeTheme(s.theme, boldText = s.boldText, highContrast = s.highContrast, colorBlindSafe = s.colorBlindSafe) {
        CompositionLocalProvider(LocalNavigator provides nav) {
            val focus = remember { FocusRequester() }
            LaunchedEffect(nav.current) { runCatching { focus.requestFocus() } }
            PlatformBackHandler(enabled = nav.stack.size > 1 || nav.current != Screen.Home) { nav.back() }
            if (!Launch.splashShown) {
                SplashScreen { Launch.splashShown = true }
                return@CompositionLocalProvider
            }
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .focusRequester(focus).focusable()
                    .onKeyEvent { e ->
                        if (e.key == Key.Escape && e.type == KeyEventType.KeyUp) { nav.back(); true }
                        else ComputerKeyboard.handle(e)
                    }
            ) {
                AppShell(nav)
                AchievementToasts(Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

/** Process-wide launch state: the logo screen shows once per app start. */
internal object Launch {
    var splashShown by mutableStateOf(false)
}

@Composable
internal fun AppShell(nav: Navigator) {
    val settings by ProgressStore.state.collectAsState()
    // Only the piano stage is dark; every other screen keeps the theme background, so text stays readable.
    val stage = nav.stage
    com.pianoquest.app.ui.AdaptiveRoot(settings.settings) {
        Surface(Modifier.fillMaxSize(), color = if (stage) com.pianoquest.app.ui.theme.PQ.colors.stage else MaterialTheme.colorScheme.background,
            contentColor = if (stage) com.pianoquest.app.ui.theme.Wood.Parchment else MaterialTheme.colorScheme.onBackground) { AppShellContent(nav) }
    }
}

@Composable
private fun AppShellContent(nav: Navigator) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Landscape (the default) uses a side rail so the full height is available for content.
        val wide = maxWidth > 840.dp || maxWidth > maxHeight
        val showNav = !nav.immersive
        val compact = maxHeight < 480.dp
        if (wide) {
            Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                if (showNav) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, header = {
                        KeyscapeLogo(Modifier.padding(top = 6.dp, bottom = if (compact) 0.dp else 6.dp).size(if (compact) 40.dp else 52.dp))
                    }) {
                        Tab.entries.forEach { t ->
                            NavigationRailItem(selected = nav.tab == t, onClick = { nav.switchTab(t) },
                                icon = { Icon(tabIcon(t), t.label) }, label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                                alwaysShowLabel = true, colors = railColors(),
                                modifier = if (compact) Modifier.height(54.dp) else Modifier)
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxSize()) { ScreenHost(nav) }
            }
        } else {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Box(Modifier.weight(1f)) { ScreenHost(nav) }
                if (showNav) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, windowInsets = WindowInsets(0)) {
                        Tab.entries.forEach { t ->
                            NavigationBarItem(selected = nav.tab == t, onClick = { nav.switchTab(t) },
                                icon = { Icon(tabIcon(t), t.label) }, label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary, indicatorColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary, unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun railColors() = androidx.compose.material3.NavigationRailItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
    indicatorColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
)

private fun tabIcon(t: Tab) = when (t) {
    Tab.HOME -> Icons.Default.Home
    Tab.LEARN -> Icons.Default.School
    Tab.SONGS -> Icons.Default.LibraryMusic
    Tab.PLAY -> Icons.Default.Piano
    Tab.MORE -> Icons.Default.MoreHoriz
}

@Composable
private fun ScreenHost(nav: Navigator) {
    val still = com.pianoquest.app.ui.LocalA11y.current.reduceMotion
    AnimatedContent(nav.current, transitionSpec = {
        if (still) fadeIn(tween(0)) togetherWith fadeOut(tween(0)) else fadeIn(tween(180)) togetherWith fadeOut(tween(120))
    }) { screen ->
        when (screen) {
            Screen.Home -> HomeScreen()
            Screen.Learn -> LearnScreen()
            Screen.Songs -> SongsScreen()
            Screen.Play -> PianoStageScreen(null)
            Screen.More -> MoreScreen()
            is Screen.CourseDetail -> CourseScreen(screen.courseId)
            is Screen.LessonPlayer -> LessonScreen(lessonId = screen.lessonId, unitTestId = null)
            is Screen.UnitTest -> LessonScreen(lessonId = null, unitTestId = screen.unitId)
            is Screen.SongSetup -> com.pianoquest.app.ui.screens.SongSetupScreen(screen.songId)
            is Screen.SongPlayer -> PianoStageScreen(screen.songId)
            is Screen.Tool -> ToolScreen(screen.tool)
            Screen.Settings -> SettingsScreen()
            Screen.Profile -> ProfileScreen()
            Screen.Onboarding -> OnboardingScreen()
            Screen.About -> com.pianoquest.app.ui.screens.AboutScreen()
            Screen.Accessibility -> com.pianoquest.app.ui.screens.AccessibilityScreen()
            Screen.Guide -> com.pianoquest.app.ui.screens.GuideScreen()
            is Screen.ComposerDetail -> com.pianoquest.app.ui.screens.ComposerScreen(screen.composerId, screen.openStory, screen.storyPage)
        }
    }
}

@Composable
private fun AchievementToasts(modifier: Modifier) {
    val pending by ProgressStore.newAchievements.collectAsState()
    val first = pending.firstOrNull()
    LaunchedEffect(first) {
        if (first != null) {
            delay(3200)
            ProgressStore.newAchievements.value = ProgressStore.newAchievements.value.drop(1)
        }
    }
    AnimatedVisibility(first != null, modifier.padding(top = 24.dp), enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
        val a = first ?: return@AnimatedVisibility
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, shadowElevation = 8.dp,
            modifier = Modifier.widthIn(max = 420.dp).padding(horizontal = 16.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoodBadge(MascotMood.EXCITED, 48.dp)
                Column {
                    Text("Achievement unlocked!", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("${a.emoji} ${a.title}", style = MaterialTheme.typography.titleMedium)
                    Text(a.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
