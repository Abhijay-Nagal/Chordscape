package com.pianoquest.app.data

import com.pianoquest.app.platform.currentEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * The current calendar day as observable state, so "today" content (song of the day, tip of the day) changes at
 * midnight and when the app comes back to the foreground on a new day - not only when it is restarted.
 */
object Today {
    private val _day = MutableStateFlow(currentEpochDay())
    val day: StateFlow<Long> = _day.asStateFlow()

    fun refresh() {
        val d = currentEpochDay()
        if (d != _day.value) _day.value = d
    }

    /**
     * The well-known pieces in a fixed shuffled order: one per day, a different one every day, and every piece comes
     * round once before any repeats.
     */
    private val rotation: List<Song> by lazy {
        Popularity.sort(SongLibrary.all).take(120).shuffled(Random(0x6B657973))
    }

    fun songOfTheDay(day: Long): Song = rotation[Math.floorMod(day, rotation.size.toLong()).toInt()]
}
