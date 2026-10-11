package com.pianoquest.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pianoquest.app.audio.Sfx
import com.pianoquest.app.data.CourseCatalog
import com.pianoquest.app.data.Levels
import com.pianoquest.app.data.MascotMood
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Step
import com.pianoquest.app.data.isGraded
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.components.FitToHeight
import com.pianoquest.app.ui.components.LinearMeter
import com.pianoquest.app.ui.components.MoodBadge
import com.pianoquest.app.ui.components.ResultBadge
import com.pianoquest.app.ui.components.Stars
import com.pianoquest.app.ui.theme.PQ

private val praise = listOf("Great job!", "Nailed it!", "Excellent!", "You've got it!", "Bravo!", "Perfect!", "Superb!")
private val encourage = listOf("Not quite - keep going!", "Almost! Learn from it.", "Mistakes help you learn.", "Nice try!")

/**
 * A lesson or unit test. Every step fits on one screen: the controls live in the top bar, keyboards are pinned to the
 * bottom edge, and long explanations are split into pages instead of scrolling.
 */
@Composable
fun LessonScreen(lessonId: String?, unitTestId: String?) {
    val nav = LocalNavigator.current
    val progress by ProgressStore.state.collectAsState()
    val solfege = progress.settings.solfege
    val key = lessonId ?: "test:$unitTestId"
    val session = remember(key) {
        LessonSessions.getOrCreate(key) {
            if (lessonId != null) {
                val l = CourseCatalog.lesson(lessonId)!!
                LessonSession(key, l.title, l.steps, isTest = false, minutes = l.minutes)
            } else {
                val u = CourseCatalog.courses.flatMap { it.units }.first { it.id == unitTestId }
                LessonSession(key, "${u.title} - Unit Test", u.test, isTest = true, minutes = 6)
            }
        }
    }
    DisposableEffect(Unit) { onDispose { PianoInput.releaseDemo() } }
    fun close() {
        LessonSessions.remove(key)
        nav.back()
    }

    Column(Modifier.fillMaxSize()) {
        if (session.finished) {
            LessonTopBar(session, onClose = { close() }, onRetry = null, onBack = null, onSkip = null, onContinue = null)
            ResultsView(session, onClose = { close() }, onRetry = {
                LessonSessions.remove(key)
                nav.replace(if (lessonId != null) Screen.LessonPlayer(lessonId) else Screen.UnitTest(unitTestId!!))
            })
            return@Column
        }
        val i = session.index
        val step = session.steps[i]
        val result = session.results[i]
        val lastPage = session.page >= session.pageCount - 1
        LessonTopBar(
            session,
            onClose = { close() },
            // Steps played on the keyboard can always be tried again.
            onRetry = if (result != null && (step is Step.TapKeys || step is Step.PlayAlong || step is Step.RhythmTap)) ({ session.retry(i) }) else null,
            onBack = when {
                session.page > 0 -> ({ session.page-- })
                i > 0 && !session.isTest -> ({ session.goTo(i - 1) })
                else -> null
            },
            onSkip = if (step.isGraded && result == null && (step is Step.TapKeys || step is Step.PlayAlong || step is Step.RhythmTap)) ({ session.results[i] = false }) else null,
            onContinue = {
                when {
                    !lastPage -> session.page++
                    i < session.steps.lastIndex -> session.goTo(i + 1)
                    else -> session.finished = true
                }
            },
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(i, transitionSpec = {
                (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
            }, modifier = Modifier.fillMaxSize()) { idx ->
                val s = session.steps[idx]
                val onResult: (Boolean) -> Unit = { ok ->
                    if (!session.results.containsKey(idx)) {
                        session.results[idx] = ok
                        if (ok) Sfx.correct() else Sfx.wrong()
                    }
                }
                Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.TopCenter) {
                  androidx.compose.runtime.key(session.attempts[idx] ?: 0) {
                    when (s) {
                        is Step.Read -> ReadStepView(s, solfege, page = if (idx == session.index) session.page else 0,
                            onPageCount = { n -> if (idx == session.index && session.pageCount != n) session.pageCount = n })
                        is Step.Quiz -> QuizStepView(s, session.results[idx], showAnswers = true, solfege = solfege, onResult = onResult)
                        is Step.TapKeys -> TapKeysStepView(s, session.results[idx], solfege, onResult)
                        is Step.PlayAlong -> PlayAlongStepView(s, session.results[idx], solfege, onResult)
                        is Step.RhythmTap -> RhythmStepView(s, session.results[idx], onResult)
                        is Step.SongPractice -> FitToHeight(Modifier.widthIn(max = 760.dp).fillMaxSize()) { SongStepView(s) }
                        is Step.Drill -> Text("…")
                    }
                  }
                }
            }
        }
    }
}

/** Title, progress, feedback and the navigation buttons - all in one compact bar. */
@Composable
private fun LessonTopBar(session: LessonSession, onClose: () -> Unit, onRetry: (() -> Unit)?, onBack: (() -> Unit)?, onSkip: (() -> Unit)?,
                         onContinue: (() -> Unit)?) {
    val c = PQ.colors
    val i = session.index
    val result = if (session.finished) null else session.results[i]
    val step = session.steps.getOrNull(i)
    Surface(color = when (result) {
        true -> lerpColor(MaterialTheme.colorScheme.surfaceContainerHigh, c.success, 0.18f)
        false -> lerpColor(MaterialTheme.colorScheme.surfaceContainerHigh, c.error, 0.16f)
        null -> MaterialTheme.colorScheme.surfaceContainerHigh
    }) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close") }
            Column(Modifier.weight(1f)) {
                if (result != null && step != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResultBadge(result, 30.dp)
                        Column(Modifier.weight(1f)) {
                            Text(if (result) praise[i % praise.size] else encourage[i % encourage.size],
                                style = MaterialTheme.typography.titleSmall, color = if (result) c.success else c.error, maxLines = 1)
                            val exp = when (step) {
                                is Step.Quiz -> step.explanation.ifBlank { if (!result) "Answer: ${step.options.first()}" else "" }
                                is Step.TapKeys -> step.explanation
                                else -> ""
                            }
                            if (exp.isNotBlank()) Text(exp, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(session.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f, fill = false))
                        if (session.pageCount > 1) Text("  ·  page ${session.page + 1} of ${session.pageCount}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    }
                    LinearMeter((i + if (session.finished) 1 else 0).toFloat() / session.steps.size, Modifier.fillMaxWidth().padding(top = 5.dp), height = 8.dp)
                }
            }
            if (session.isTest && !session.finished) Text("${session.correctCount} ✓", style = MaterialTheme.typography.titleMedium)
            if (onSkip != null) TextButton(onClick = onSkip) { Text("Skip") }
            if (onRetry != null) OutlinedButton(onClick = onRetry, Modifier.height(44.dp)) {
                Icon(Icons.Default.Refresh, null, Modifier.padding(end = 4.dp)); Text("Retry")
            }
            if (onBack != null) OutlinedButton(onClick = onBack, Modifier.height(44.dp)) { Text("Back") }
            if (onContinue != null) {
                val lastPage = session.page >= session.pageCount - 1
                Button(onClick = onContinue, enabled = session.isStepDone(i), modifier = Modifier.height(44.dp).widthIn(min = 128.dp)) {
                    Text(when {
                        !lastPage -> "Next page"
                        i < session.steps.lastIndex -> "Continue"
                        else -> "Finish"
                    }, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

private fun lerpColor(a: androidx.compose.ui.graphics.Color, b: androidx.compose.ui.graphics.Color, t: Float) =
    androidx.compose.ui.graphics.lerp(a, b, t)

@Composable
private fun ResultsView(session: LessonSession, onClose: () -> Unit, onRetry: () -> Unit) {
    val nav = LocalNavigator.current
    val score = session.scorePercent
    val passed = !session.isTest || score >= ProgressStore.PASS_PERCENT
    val before = remember { ProgressStore.current.xp }
    LaunchedEffect(Unit) {
        if (!session.recorded) {
            session.recorded = true
            if (session.isTest) ProgressStore.recordTest(session.key.removePrefix("test:") + ".test", score, session.minutes)
            else ProgressStore.completeLesson(session.key, session.minutes, score)
            if (passed) Sfx.fanfare()
        }
    }
    val p by ProgressStore.state.collectAsState()
    val gained = p.xp - before
    val stars = when {
        score >= 95 -> 3
        score >= 80 -> 2
        score >= 60 || !session.isTest -> 1
        else -> 0
    }
    val lessonId = session.key
    val unit = CourseCatalog.unitOf(if (session.isTest) session.key.removePrefix("test:") + ".test" else lessonId)
    val nextLesson = unit?.lessons?.let { ls -> ls.getOrNull(ls.indexOfFirst { it.id == lessonId } + 1) }
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        val wide = maxWidth > maxHeight
        val summary = @Composable {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoodBadge(if (passed) MascotMood.EXCITED else MascotMood.SAD, 88.dp)
                Text(
                    when {
                        session.isTest && passed -> "Test passed!"
                        session.isTest -> "Not quite there yet"
                        else -> "Lesson complete!"
                    }, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                )
                Stars(stars, size = 32.dp)
                if (session.gradedCount > 0) Text("Score: $score%  (${session.correctCount} / ${session.gradedCount})", style = MaterialTheme.typography.titleMedium)
                if (gained > 0) Text("+$gained XP  •  Level ${Levels.level(p.xp)} ${Levels.title(Levels.level(p.xp))}", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
                if (session.isTest && !passed) Text("You need ${ProgressStore.PASS_PERCENT}% to pass. Review the lessons and try again - you've got this!",
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }
        val actions = @Composable {
            Column(Modifier.widthIn(max = 380.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!session.isTest && nextLesson != null) {
                    Button(onClick = {
                        LessonSessions.remove(session.key)
                        nav.replace(Screen.LessonPlayer(nextLesson.id))
                    }, Modifier.fillMaxWidth().height(52.dp)) { Text("Next: ${nextLesson.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                } else if (!session.isTest && unit != null) {
                    Button(onClick = {
                        LessonSessions.remove(session.key)
                        nav.replace(Screen.UnitTest(unit.id))
                    }, Modifier.fillMaxWidth().height(52.dp)) { Text("Take the unit test") }
                }
                OutlinedButton(onClick = onRetry, Modifier.fillMaxWidth()) { Text(if (session.isTest) "Retake test" else "Practise again") }
                TextButton(onClick = onClose) { Text("Back to course") }
            }
        }
        FitToHeight(Modifier.fillMaxSize(), alignment = Alignment.Center) {
            if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { summary() }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { actions() }
            } else Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { summary(); actions() }
        }
    }
}

/** True when there is room for a side-by-side lesson layout (the landscape default). */
internal fun isWideLesson(w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) = w > 560.dp && w > h * 1.15f
