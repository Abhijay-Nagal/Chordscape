package com.pianoquest.app.data

import com.pianoquest.app.data.courses.bluesCourse
import com.pianoquest.app.data.courses.classicalCourse
import com.pianoquest.app.data.courses.earCourse
import com.pianoquest.app.data.courses.filmCourse
import com.pianoquest.app.data.courses.foundationsCourse
import com.pianoquest.app.data.courses.gospelCourse
import com.pianoquest.app.data.courses.improvCourse
import com.pianoquest.app.data.courses.jazzCourse
import com.pianoquest.app.data.courses.latinCourse
import com.pianoquest.app.data.courses.popCourse
import com.pianoquest.app.data.courses.romanticCourse
import com.pianoquest.app.data.courses.sightCourse
import com.pianoquest.app.data.courses.techniqueCourse
import com.pianoquest.app.data.courses.theoryCourse

object CourseCatalog {
    val courses: List<Course> by lazy {
        listOf(
            foundationsCourse(), theoryCourse(), classicalCourse(), romanticCourse(), jazzCourse(), popCourse(),
            bluesCourse(), gospelCourse(), latinCourse(), filmCourse(), improvCourse(), earCourse(), sightCourse(), techniqueCourse(),
        )
    }

    fun course(id: String) = courses.firstOrNull { it.id == id }

    fun unitOf(lessonOrTestId: String): CourseUnit? = courses.flatMap { it.units }
        .firstOrNull { u -> u.testId == lessonOrTestId || u.lessons.any { it.id == lessonOrTestId } }

    fun courseOf(lessonOrUnitId: String): Course? = courses.firstOrNull { lessonOrUnitId.startsWith(it.id + ".") }

    fun lesson(id: String): Lesson? = courses.asSequence().flatMap { it.allLessons }.firstOrNull { it.id == id }

    /** The next lesson the learner should do in a course (first incomplete, respecting order). */
    fun nextLesson(course: Course, done: Set<String>): Lesson? = course.allLessons.firstOrNull { it.id !in done }

    fun courseProgress(course: Course, p: ProgressData): Float {
        val total = course.lessonCount + course.units.size
        if (total == 0) return 0f
        val done = course.allLessons.count { it.id in p.lessonsDone } +
            course.units.count { (p.testScores[it.testId] ?: 0) >= ProgressStore.PASS_PERCENT }
        return done.toFloat() / total
    }

    /** Every lesson is open from the start - learners can skip anything they already know. */
    @Suppress("UNUSED_PARAMETER")
    fun isUnlocked(lesson: Lesson, p: ProgressData): Boolean = true
}
