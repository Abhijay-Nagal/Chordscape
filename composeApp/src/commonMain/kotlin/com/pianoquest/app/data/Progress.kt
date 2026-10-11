package com.pianoquest.app.data

import com.pianoquest.app.platform.Storage
import com.pianoquest.app.platform.currentEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.pow

@Serializable
data class Settings(
    val instrument: String = "GRAND_PIANO",
    val volume: Float = 0.8f,
    val reverb: Float = 0.12f,
    val keyLabels: String = "C_ONLY",
    val solfege: Boolean = false,
    val theme: String = "light",
    val midiThrough: Boolean = true,
    val keyWidthDp: Float = 46f,
    val showStaffInSongs: Boolean = false,
    val dailyGoalMinutes: Int = 15,
    /** Number of white keys visible on the piano stage (8 = one octave ... 52 = all 88 keys). */
    val stageWhites: Int = 22,
    /** Index (0-51) of the left-most visible white key. */
    val stageStart: Int = 16,
    val songSpeed: Float = 0.8f,
    val difficulty: String = "HARD",
    val playMode: String = "LISTEN",
    val hand: String = "RIGHT",
    val metronomeBpm: Int = 90,
    val accompanimentVolume: Float = 0.85f,
    /** How notes are shown in the song player: WATERFALL, FALLING_NOTE or SHEET_MUSIC. */
    val guideMode: String = "FALLING_NOTE",
    /** Keyboard follows the music in the song player. */
    val keyboardAutoScroll: Boolean = true,
    /** Landscape is the default; this lets the app rotate to portrait as well. */
    val allowPortrait: Boolean = false,
    /** Set once the light theme has become the default (earlier versions stored "system"). */
    val lightDefaultApplied: Boolean = false,
    /** OFF (every note the same loudness), LIGHT, NORMAL or HEAVY - how strongly touch / key speed changes loudness. */
    val touchSensitivity: String = "NORMAL",
    /** Number of white keys used last in the song player (kept between songs). */
    val songWhites: Int = 0,
    /** Songs press the sustain pedal where the music needs it (from the score, or automatically for Romantic pieces). */
    val songPedal: Boolean = true,
    // Sound engineering (piano stage only)
    val fxTranspose: Int = 0,
    val fxTuneCents: Float = 0f,
    val fxBassDb: Float = 0f,
    val fxTrebleDb: Float = 0f,
    val fxDrive: Float = 0f,
    val fxChorus: Float = 0f,
    val fxTremoloDepth: Float = 0f,
    val fxTremoloRate: Float = 5f,
    val fxReleaseScale: Float = 1f,
    /** How long notes ring while the sustain pedal is down (1 = like a real piano, lower = drier). Default 50%. */
    val fxSustainLevel: Float = 0.5f,
    val fxRoomSize: Float = 0.5f,
    // Accessibility (Settings → Accessibility; "Reset" restores these defaults)
    /** Text size multiplier on top of the device's own font size. */
    val textScale: Float = 1f,
    /** Size of everything (text, icons, buttons) on top of the automatic size for the screen. */
    val displayScale: Float = 1f,
    val boldText: Boolean = false,
    val highContrast: Boolean = false,
    /** Blue / orange hand colours and blue-green / vermilion right / wrong colours that work with colour blindness. */
    val colorBlindSafe: Boolean = false,
    /** Fewer animations and screen transitions. */
    val reduceMotion: Boolean = false,
    /** Bigger note names on the piano keys. */
    val largeKeyLabels: Boolean = false,
    /** A light vibration on every on-screen key press. */
    val hapticKeys: Boolean = false,
    /** The screen flashes on every metronome beat (for players who cannot hear the click well). */
    val visualMetronome: Boolean = false,
    /** Keep the screen awake while the app is open. */
    val keepScreenOn: Boolean = true,
    /** Set once the 50% pedal-sustain default has been applied to an existing install. */
    val sustainDefaultApplied: Boolean = false,
) {
    fun soundFx() = com.pianoquest.app.audio.SoundFx(fxTranspose, fxTuneCents, fxBassDb, fxTrebleDb, fxDrive, fxChorus,
        fxTremoloDepth, fxTremoloRate, fxReleaseScale, fxSustainLevel, fxRoomSize)

    fun withDefaultAccessibility(): Settings = Settings().let { d ->
        copy(textScale = d.textScale, displayScale = d.displayScale, boldText = d.boldText, highContrast = d.highContrast,
            colorBlindSafe = d.colorBlindSafe, reduceMotion = d.reduceMotion, largeKeyLabels = d.largeKeyLabels,
            hapticKeys = d.hapticKeys, visualMetronome = d.visualMetronome, keepScreenOn = d.keepScreenOn)
    }

    val accessibilityIsDefault: Boolean get() = withDefaultAccessibility() == this

    fun withoutFx() = copy(fxTranspose = 0, fxTuneCents = 0f, fxBassDb = 0f, fxTrebleDb = 0f, fxDrive = 0f, fxChorus = 0f,
        fxTremoloDepth = 0f, fxTremoloRate = 5f, fxReleaseScale = 1f, fxSustainLevel = 0.5f, fxRoomSize = 0.5f)
}

@Serializable
data class ProgressData(
    val name: String = "",
    val onboarded: Boolean = false,
    val experience: String = "beginner",
    val lessonsDone: Set<String> = emptySet(),
    val testScores: Map<String, Int> = emptyMap(),
    val songStars: Map<String, Int> = emptyMap(),
    val songBest: Map<String, Int> = emptyMap(),
    /** All-time best accuracy (percent) per song for each hand. */
    val songBestRight: Map<String, Int> = emptyMap(),
    val songBestLeft: Map<String, Int> = emptyMap(),
    val xp: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = 0,
    val practiceDays: List<Long> = emptyList(),
    val minutesByDay: Map<String, Int> = emptyMap(),
    val achievements: Set<String> = emptySet(),
    val drillCorrect: Int = 0,
    val notesPlayed: Long = 0,
    val midiUsed: Boolean = false,
    val lastLessonId: String? = null,
    val favoriteSongs: Set<String> = emptySet(),
    val settings: Settings = Settings(),
)

data class Achievement(val id: String, val title: String, val description: String, val emoji: String)

object Achievements {
    val all = listOf(
        Achievement("first-lesson", "First Notes", "Complete your first lesson", "🎵"),
        Achievement("five-lessons", "Getting Started", "Complete 5 lessons", "🌱"),
        Achievement("twentyfive-lessons", "Dedicated", "Complete 25 lessons", "📚"),
        Achievement("hundred-lessons", "Scholar", "Complete 100 lessons", "🎓"),
        Achievement("first-test", "Tested", "Pass your first unit test", "✅"),
        Achievement("perfect-test", "Flawless", "Score 100% on a unit test", "💯"),
        Achievement("first-song", "Performer", "Finish a song in the song player", "🎹"),
        Achievement("three-star", "Star Player", "Earn 3 stars on a song", "⭐"),
        Achievement("ten-songs", "Repertoire", "Earn stars on 10 different songs", "🎼"),
        Achievement("streak-3", "On a Roll", "Practise 3 days in a row", "🔥"),
        Achievement("streak-7", "Week Warrior", "Practise 7 days in a row", "🏅"),
        Achievement("streak-30", "Unstoppable", "Practise 30 days in a row", "🏆"),
        Achievement("midi", "Plugged In", "Play with a MIDI keyboard", "🔌"),
        Achievement("course-complete", "Graduate", "Complete every lesson in a course", "🎖️"),
        Achievement("level-10", "Apprentice", "Reach level 10", "🥉"),
        Achievement("level-50", "Musician", "Reach level 50", "🥈"),
        Achievement("level-100", "Virtuoso", "Reach level 100", "👑"),
        Achievement("notes-10000", "Ten Thousand Notes", "Play 10,000 notes", "🎶"),
    )
}

object Levels {
    /** Total XP that maps to level 100 - roughly every lesson and test done plus a good share of the song library. */
    val maxXp: Int by lazy {
        val list = CourseCatalog.courses
        list.sumOf { it.lessonCount } * 24 + list.sumOf { it.units.size } * 60 + SongLibrary.all.size * 20
    }

    fun level(xp: Int): Int = (1 + (99 * (xp.toDouble() / maxXp).coerceIn(0.0, 1.0).pow(0.62)).toInt()).coerceIn(1, 100)

    fun xpForLevel(level: Int): Int {
        if (level <= 1) return 0
        val frac = ((level - 1) / 99.0).pow(1 / 0.62)
        return kotlin.math.ceil(frac * maxXp).toInt()
    }

    fun title(level: Int): String = when {
        level >= 100 -> "Virtuoso"
        level >= 80 -> "Maestro"
        level >= 60 -> "Artist"
        level >= 40 -> "Musician"
        level >= 25 -> "Player"
        level >= 10 -> "Apprentice"
        else -> "Novice"
    }
}

object ProgressStore {
    private const val FILE = "progress.json"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _state = MutableStateFlow(load())
    val state: StateFlow<ProgressData> = _state.asStateFlow()

    /** Fired with newly unlocked achievements so the UI can celebrate. */
    val newAchievements = MutableStateFlow<List<Achievement>>(emptyList())

    val current: ProgressData get() = _state.value

    private fun load(): ProgressData = (runCatching {
        Storage.read(FILE)?.let { json.decodeFromString(ProgressData.serializer(), it) }
    }.getOrNull() ?: ProgressData()).let { p ->
        // The light theme is the default; switch existing installs over once (a later choice in Settings is kept).
        if (p.settings.lightDefaultApplied) p
        else p.copy(settings = p.settings.copy(theme = if (p.settings.theme == "system") "light" else p.settings.theme, lightDefaultApplied = true))
    }.let { p ->
        // Pedal sustain now defaults to 50%; move existing installs over once (a later choice is kept).
        if (p.settings.sustainDefaultApplied) p
        else p.copy(settings = p.settings.copy(fxSustainLevel = if (p.settings.fxSustainLevel >= 0.999f) 0.5f else p.settings.fxSustainLevel,
            sustainDefaultApplied = true))
    }

    fun update(transform: (ProgressData) -> ProgressData) {
        val before = _state.value
        var next = transform(before)
        next = checkAchievements(before, next)
        _state.value = next
        runCatching { Storage.write(FILE, json.encodeToString(ProgressData.serializer(), next)) }
    }

    fun updateSettings(transform: (Settings) -> Settings) = update { it.copy(settings = transform(it.settings)) }

    fun resetAll() {
        val settings = current.settings
        _state.value = ProgressData(settings = settings)
        runCatching { Storage.write(FILE, json.encodeToString(ProgressData.serializer(), _state.value)) }
    }

    private fun touchDay(p: ProgressData, minutes: Int): ProgressData {
        val today = currentEpochDay()
        val streak = when (p.lastDay) {
            today -> p.streak
            today - 1 -> p.streak + 1
            else -> 1
        }
        val days = if (p.practiceDays.lastOrNull() == today) p.practiceDays else (p.practiceDays + today).takeLast(120)
        val key = today.toString()
        val mins = p.minutesByDay.toMutableMap().apply { this[key] = (this[key] ?: 0) + minutes }
            .filterKeys { (it.toLongOrNull() ?: 0) > today - 60 }
        return p.copy(lastDay = today, streak = streak, bestStreak = maxOf(p.bestStreak, streak), practiceDays = days, minutesByDay = mins)
    }

    fun minutesToday(p: ProgressData = current): Int = p.minutesByDay[currentEpochDay().toString()] ?: 0

    fun completeLesson(lessonId: String, minutes: Int, scorePercent: Int) {
        update { p ->
            val first = lessonId !in p.lessonsDone
            val xp = if (first) 20 + scorePercent / 10 else 4
            touchDay(p, minutes).copy(lessonsDone = p.lessonsDone + lessonId, xp = p.xp + xp, lastLessonId = lessonId)
        }
    }

    fun recordTest(testId: String, scorePercent: Int, minutes: Int = 5) {
        update { p ->
            val prev = p.testScores[testId] ?: -1
            val passedBefore = prev >= PASS_PERCENT
            val passedNow = scorePercent >= PASS_PERCENT
            var gain = 0
            if (passedNow && !passedBefore) gain += 50
            if (scorePercent > prev) gain += ((scorePercent - maxOf(prev, 0)) / 5)
            touchDay(p, minutes).copy(testScores = p.testScores + (testId to maxOf(prev, scorePercent)), xp = p.xp + gain)
        }
    }

    /** Records a finished play-through. [right] / [left] are the per-hand accuracies (null when that hand was not played). */
    fun recordSong(songId: String, scorePercent: Int, stars: Int, minutes: Int, right: Int? = null, left: Int? = null) {
        update { p ->
            val prevStars = p.songStars[songId] ?: 0
            val gain = (stars - prevStars).coerceAtLeast(0) * 10 + 2
            fun Map<String, Int>.best(v: Int?) = if (v == null) this else this + (songId to maxOf(this[songId] ?: 0, v.coerceIn(0, 100)))
            touchDay(p, minutes).copy(
                songStars = p.songStars + (songId to maxOf(prevStars, stars)),
                songBest = p.songBest + (songId to maxOf(p.songBest[songId] ?: 0, scorePercent)),
                songBestRight = p.songBestRight.best(right),
                songBestLeft = p.songBestLeft.best(left),
                xp = p.xp + gain,
            )
        }
    }

    fun recordDrill(correct: Int, minutes: Int = 1) {
        update { p -> touchDay(p, minutes).copy(drillCorrect = p.drillCorrect + correct, xp = p.xp + correct) }
    }

    fun addNotesPlayed(n: Int) {
        if (n <= 0) return
        val p = current
        // Avoid a disk write per note: persist in batches.
        _state.value = p.copy(notesPlayed = p.notesPlayed + n)
        if ((p.notesPlayed / 200) != ((p.notesPlayed + n) / 200)) update { it }
    }

    fun toggleFavorite(songId: String) {
        update { p -> p.copy(favoriteSongs = if (songId in p.favoriteSongs) p.favoriteSongs - songId else p.favoriteSongs + songId) }
    }

    fun markMidiUsed() {
        if (!current.midiUsed) update { it.copy(midiUsed = true) }
    }

    private fun checkAchievements(before: ProgressData, p: ProgressData): ProgressData {
        val got = p.achievements.toMutableSet()
        fun award(id: String, cond: Boolean) { if (cond) got += id }
        award("first-lesson", p.lessonsDone.isNotEmpty())
        award("five-lessons", p.lessonsDone.size >= 5)
        award("twentyfive-lessons", p.lessonsDone.size >= 25)
        award("hundred-lessons", p.lessonsDone.size >= 100)
        award("first-test", p.testScores.values.any { it >= PASS_PERCENT })
        award("perfect-test", p.testScores.values.any { it >= 100 })
        award("first-song", p.songBest.isNotEmpty())
        award("three-star", p.songStars.values.any { it >= 3 })
        award("ten-songs", p.songStars.count { it.value > 0 } >= 10)
        award("streak-3", p.streak >= 3)
        award("streak-7", p.streak >= 7)
        award("streak-30", p.streak >= 30)
        award("midi", p.midiUsed)
        award("course-complete", CourseCatalog.courses.any { c -> c.allLessons.isNotEmpty() && c.allLessons.all { it.id in p.lessonsDone } })
        val level = Levels.level(p.xp)
        award("level-10", level >= 10)
        award("level-50", level >= 50)
        award("level-100", level >= 100)
        award("notes-10000", p.notesPlayed >= 10000)
        val fresh = got - p.achievements
        if (fresh.isNotEmpty() && before.onboarded) {
            newAchievements.value = newAchievements.value + Achievements.all.filter { it.id in fresh }
        }
        return if (fresh.isEmpty()) p else p.copy(achievements = got)
    }

    const val PASS_PERCENT = 70
}
