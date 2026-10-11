package com.pianoquest.app.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf

/** Navigation destinations in rail / bar order: Play sits right after Home. */
enum class Tab(val label: String) { HOME("Home"), PLAY("Play"), LEARN("Learn"), SONGS("Songs"), MORE("More") }

enum class ToolId(val title: String, val subtitle: String) {
    CHORDS("Chord Explorer", "Any chord, any key - see it, hear it"),
    SCALES("Scale & Mode Explorer", "23 scales and modes on the keyboard"),
    CIRCLE("Circle of Fifths", "Interactive key map with diatonic chords"),
    PROGRESSIONS("Progression Player", "Play famous progressions in any key"),
    METRONOME("Metronome", "Keep perfect time"),
    EAR("Ear Trainer", "Endless interval, chord & cadence drills"),
    READING("Note Reading Trainer", "Speed-read treble & bass clef"),
    CHORD_FINDER("Chord Finder", "Play any notes - get the chord name"),
    GLOSSARY("Theory Encyclopedia", "Every music term explained"),
}

sealed interface Screen {
    data object Home : Screen
    data object Learn : Screen
    data object Songs : Screen
    data object Play : Screen
    data object More : Screen
    data class CourseDetail(val courseId: String) : Screen
    data class LessonPlayer(val lessonId: String) : Screen
    data class UnitTest(val unitId: String) : Screen
    /** Difficulty / guide mode / play mode chooser shown before a song starts. */
    data class SongSetup(val songId: String) : Screen
    data class SongPlayer(val songId: String) : Screen
    data class Tool(val tool: ToolId) : Screen
    data object Settings : Screen
    data object Profile : Screen
    data object Onboarding : Screen
    data object About : Screen
    /** Text size, display size, contrast, colours, motion and other accessibility options. */
    data object Accessibility : Screen
    /** The illustrated user guide. */
    data object Guide : Screen
    /** A composer's page: their pieces and an illustrated storybook of their life. */
    data class ComposerDetail(val composerId: String, val openStory: Boolean = false, val storyPage: Int = 0) : Screen
}

class Navigator {
    val stack = mutableStateListOf<Screen>(Screen.Home)

    val current: Screen get() = stack.last()

    val tab: Tab get() = when (stack.first()) {
        Screen.Learn -> Tab.LEARN
        Screen.Songs -> Tab.SONGS
        Screen.Play -> Tab.PLAY
        Screen.More -> Tab.MORE
        else -> Tab.HOME
    }

    fun push(s: Screen) { stack.add(s) }

    fun back(): Boolean {
        if (stack.size > 1) { stack.removeAt(stack.lastIndex); return true }
        if (stack.first() != Screen.Home) { stack.clear(); stack.add(Screen.Home); return true }
        return false
    }

    fun replace(s: Screen) {
        if (stack.size > 1) stack[stack.lastIndex] = s else push(s)
    }

    fun switchTab(t: Tab) {
        stack.clear()
        stack.add(when (t) {
            Tab.HOME -> Screen.Home
            Tab.LEARN -> Screen.Learn
            Tab.SONGS -> Screen.Songs
            Tab.PLAY -> Screen.Play
            Tab.MORE -> Screen.More
        })
    }

    /** Opens a song: first the setup screen, which then starts the player. */
    fun openSong(songId: String) {
        // Every song opens with the same starting choices; the player can change them on the setup screen.
        com.pianoquest.app.data.ProgressStore.updateSettings {
            it.copy(difficulty = "HARD", guideMode = "FALLING_NOTE", playMode = "LISTEN")
        }
        push(Screen.SongSetup(songId))
    }

    /** Full-screen experiences hide the navigation bar. */
    val immersive: Boolean get() = when (current) {
        is Screen.LessonPlayer, is Screen.UnitTest, is Screen.SongPlayer, is Screen.SongSetup, Screen.Play, Screen.Onboarding -> true
        else -> false
    }

    /** The dark piano stage (free play and the song player). */
    val stage: Boolean get() = current == Screen.Play || current is Screen.SongPlayer
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator") }
