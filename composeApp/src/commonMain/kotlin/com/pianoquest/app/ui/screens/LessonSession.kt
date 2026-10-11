package com.pianoquest.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pianoquest.app.data.Drills
import com.pianoquest.app.data.Step
import com.pianoquest.app.data.isGraded
import kotlin.random.Random

/** Runtime state of one lesson or test attempt. Kept outside composition so it survives visiting the song player. */
class LessonSession(
    val key: String,
    val title: String,
    rawSteps: List<Step>,
    val isTest: Boolean,
    val minutes: Int,
) {
    val steps: List<Step> = rawSteps.flatMap { if (it is Step.Drill) Drills.expand(it, Random(seed())) else listOf(it) }
    var index by mutableIntStateOf(0)
    /** Page within the current step (long explanations are split over several pages instead of scrolling). */
    var page by mutableIntStateOf(0)
    var pageCount by mutableIntStateOf(1)
    val results = mutableStateMapOf<Int, Boolean>()
    /** How many times each step was restarted with Retry (a new attempt starts the step's state from scratch). */
    val attempts = mutableStateMapOf<Int, Int>()

    /** Clears the result of step [i] so it can be played again. */
    fun retry(i: Int) {
        results.remove(i)
        attempts[i] = (attempts[i] ?: 0) + 1
    }
    var finished by mutableStateOf(false)
    var recorded = false

    val gradedCount: Int get() = steps.count { it.isGraded }
    val correctCount: Int get() = results.values.count { it }
    val scorePercent: Int get() = if (gradedCount == 0) 100 else (100 * correctCount / gradedCount)

    fun isStepDone(i: Int): Boolean = !steps[i].isGraded || results.containsKey(i)

    fun goTo(i: Int) {
        index = i
        page = 0
        pageCount = 1
    }

    companion object {
        private var counter = 0L
        private fun seed(): Long = (com.pianoquest.app.platform.nowMillis() + counter++)
    }
}

object LessonSessions {
    private val map = HashMap<String, LessonSession>()
    fun getOrCreate(key: String, create: () -> LessonSession): LessonSession = map.getOrPut(key, create)
    fun remove(key: String) { map.remove(key) }
}
