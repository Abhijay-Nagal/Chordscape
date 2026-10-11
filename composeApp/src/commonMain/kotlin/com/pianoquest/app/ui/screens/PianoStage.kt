package com.pianoquest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.audio.Instrument
import com.pianoquest.app.audio.InstrumentFamily
import com.pianoquest.app.data.Clef
import com.pianoquest.app.data.Difficulty
import com.pianoquest.app.data.Hand
import com.pianoquest.app.data.KeyLabels
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Song
import com.pianoquest.app.data.SongData
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.data.SongRepository
import com.pianoquest.app.input.ComputerKeyboard
import com.pianoquest.app.input.InputSource
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.isDesktop
import com.pianoquest.app.platform.nowMillis
import com.pianoquest.app.platform.requestLandscape
import com.pianoquest.app.platform.setImmersive
import com.pianoquest.app.theory.Chord
import com.pianoquest.app.theory.isBlackKey
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.PianoKeyboard
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.components.StaffEl
import com.pianoquest.app.ui.components.StaffView
import com.pianoquest.app.ui.components.Stars
import com.pianoquest.app.ui.components.keyLabel
import com.pianoquest.app.ui.theme.LocalPQColors
import com.pianoquest.app.ui.theme.PQ
import com.pianoquest.app.ui.theme.Wood
import com.pianoquest.app.ui.theme.woodGrain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

internal val WHITE_KEYS: List<Int> = (21..108).filter { !isBlackKey(it) }
internal fun whiteIndexOf(midi: Int) = WHITE_KEYS.indexOf(midi).coerceAtLeast(0)

/** Number of white keys that gives a familiar total key count. */
private val KEY_PRESETS = listOf(25 to 15, 37 to 22, 49 to 29, 61 to 36, 76 to 45, 88 to 52)

private class RecEvent(val t: Long, val midi: Int, val down: Boolean, val vel: Int)

/** Recordings survive leaving the screen for the session. */
private object Recordings {
    val takes = mutableStateListOf<List<RecEvent>>()
}

/** A sustain pedal seen from the side. */
internal val PedalIcon: ImageVector by lazy {
    ImageVector.Builder("Pedal", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 16f); lineTo(19f, 9.5f); quadTo(21.5f, 8.8f, 21.5f, 11f); lineTo(21.5f, 12f)
            lineTo(6f, 18.6f); lineTo(3f, 18.6f); close()
            moveTo(2f, 19.6f); lineTo(22f, 19.6f); lineTo(22f, 21.6f); lineTo(2f, 21.6f); close()
            moveTo(5f, 15.2f); lineTo(5f, 11f); lineTo(7f, 11f); lineTo(7f, 14.4f); close()
        }
    }.build()
}

/**
 * The piano stage: free play when [songId] is null, otherwise the falling-notes song player.
 * Both share the same top bar, keyboard and controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PianoStageScreen(songId: String?) {
    val nav = LocalNavigator.current
    val prefs by ProgressStore.state.collectAsState()
    val settings = prefs.settings
    val c = PQ.colors
    val scope = rememberCoroutineScope()
    val song: Song? = remember(songId) { songId?.let { SongLibrary.get(it) } }
    val songMode = song != null

    // ---------------------------------------------------------------- shared state
    var menu by remember { mutableStateOf<String?>(null) }
    var advanced by remember { mutableStateOf(false) }
    var labels by remember { mutableStateOf(runCatching { KeyLabels.valueOf(settings.keyLabels) }.getOrDefault(KeyLabels.C_ONLY)) }
    // The song player opens with the number of keys used last time; free play remembers its own.
    var whites by remember {
        mutableIntStateOf((if (songMode && settings.songWhites > 0) settings.songWhites else settings.stageWhites).coerceIn(8, 52))
    }
    var start by remember { mutableIntStateOf(settings.stageStart) }
    var dual by remember { mutableStateOf(false) }
    var start2 by remember { mutableIntStateOf(whiteIndexOf(36)) }
    var metronome by remember { mutableStateOf(false) }
    var freeBpm by remember { mutableIntStateOf(settings.metronomeBpm) }
    var recording by remember { mutableStateOf(false) }
    var recStart by remember { mutableStateOf(0L) }
    val current = remember { mutableStateListOf<RecEvent>() }
    var playJob by remember { mutableStateOf<Job?>(null) }
    var replaying by remember { mutableStateOf(false) }
    val sustain by PianoInput.sustain.collectAsState()

    fun setWhites(w: Int, keepCentre: Boolean = true) {
        val nw = w.coerceIn(8, 52)
        if (nw == whites) return
        val centre = start + whites / 2
        whites = nw
        if (keepCentre) start = (centre - nw / 2).coerceIn(0, 52 - nw)
    }
    // Save the keyboard size and position once the user stops changing them (not on every drag step).
    LaunchedEffect(whites, start) {
        delay(800)
        ProgressStore.updateSettings { s -> if (songMode) s.copy(songWhites = whites) else s.copy(stageWhites = whites, stageStart = start) }
    }

    // ---------------------------------------------------------------- song state
    var difficulty by remember { mutableStateOf(runCatching { Difficulty.valueOf(settings.difficulty) }.getOrDefault(Difficulty.HARD)) }
    var mode by remember { mutableStateOf(runCatching { PlayMode.valueOf(settings.playMode) }.getOrDefault(PlayMode.PRACTICE)) }
    val hands = remember { runCatching { HandChoice.valueOf(settings.hand) }.getOrDefault(HandChoice.RIGHT) }
    // Every song starts at its real tempo.
    var speed by remember { mutableStateOf(1f) }
    var showSheet by remember { mutableStateOf(settings.showStaffInSongs) }
    var guide by remember { mutableStateOf(runCatching { GuideMode.valueOf(settings.guideMode) }.getOrDefault(GuideMode.WATERFALL)) }
    // Listen mode: which hands' falling notes are shown (and scored). The music always plays both hands.
    var shownHands by remember { mutableStateOf(HandChoice.BOTH) }
    val pedalOn by rememberUpdatedState(settings.songPedal)
    val a11y = com.pianoquest.app.ui.LocalA11y.current
    // Visual metronome: a flash around the screen on every click.
    val beatFlash = remember { androidx.compose.animation.core.Animatable(0f) }
    fun flashBeat(accent: Boolean) {
        if (!a11y.visualMetronome) return
        scope.launch { beatFlash.snapTo(if (accent) 1f else 0.6f); beatFlash.animateTo(0f, androidx.compose.animation.core.tween(240)) }
    }
    var autoStart by remember { mutableStateOf(songMode) }
    var infoOpen by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<SongData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var runKey by remember { mutableIntStateOf(0) }
    val pb = remember { Playback() }

    LaunchedEffect(song, difficulty) {
        if (song == null) return@LaunchedEffect
        data = null
        loadError = null
        data = runCatching { SongRepository.load(song, difficulty) }.onFailure { loadError = it.message }.getOrNull()
    }
    val run = remember(data, mode, hands, runKey) { data?.let { SongRun(it, mode, hands, song?.meterNum ?: 4, song?.meterDen ?: 4) } }
    val preRoll = (data?.barBeats ?: 4.0).coerceAtLeast(2.0)

    /** Chooses the keyboard range for a song: the saved key count (or a fitted one) centred on the music. */
    fun placeForSong(d: SongData, maxWhites: Int, fit: Boolean) {
        if (d.events.isEmpty()) return
        val sorted = d.events.map { it.midi }.sorted()
        val lo = sorted[(sorted.size * 0.01).toInt()]
        val hi = sorted[(sorted.size * 0.99).toInt().coerceAtMost(sorted.lastIndex)]
        val loW = whiteIndexOf(if (isBlackKey(lo)) lo - 1 else lo)
        val hiW = whiteIndexOf(if (isBlackKey(hi)) hi + 1 else hi)
        if (fit) whites = (hiW - loW + 3).coerceIn(15, maxWhites)
        start = (loW - (whites - (hiW - loW + 1)) / 2).coerceIn(0, 52 - whites)
    }
    var maxFitWhites by remember { mutableIntStateOf(52) }
    LaunchedEffect(data) { data?.let { placeForSong(it, maxFitWhites, fit = settings.songWhites <= 0) } }

    fun stopDemo() = PianoInput.releaseDemo()
    fun restart() {
        stopDemo()
        pb.playing = false
        pb.finished = false
        pb.waiting = false
        pb.time = pb.loopA ?: -preRoll
        runKey++
    }
    fun seek(t: Double) {
        stopDemo()
        pb.time = t
        run?.seek(t)
        pb.finished = false
        pb.seeks++
    }
    /** Loop restart: the notes at A start above the screen and fall in, so there is a short lead-in before A sounds. */
    fun jumpToLoopStart(r: SongRun) {
        val a = pb.loopA ?: return
        stopDemo()
        r.seek(a)
        pb.time = a - lookaheadBeats(r.data.barBeats, speed)
        pb.finished = false
        pb.seeks++
    }
    fun togglePlay() {
        if (run == null) return
        if (pb.finished) restart()
        pb.playing = !pb.playing
        if (!pb.playing) stopDemo()
    }

    LaunchedEffect(run) {
        if (run != null && pb.loopA != null) jumpToLoopStart(run)
        if (autoStart && run != null) {
            autoStart = false
            restart()
            pb.playing = true
        }
    }

    DisposableEffect(Unit) {
        // The keyboard always starts with the grand piano; other sounds are chosen per session.
        ProgressStore.updateSettings { it.copy(instrument = Instrument.GRAND_PIANO.name) }
        Audio.setInstrument(Instrument.GRAND_PIANO)
        requestLandscape(true)
        setImmersive(true)
        onDispose {
            setImmersive(false)
            requestLandscape(false)
            PianoInput.releaseAll()
            playJob?.cancel()
            ProgressStore.updateSettings { it.copy(keyLabels = labels.name) }
        }
    }

    // Recording (works in both modes).
    LaunchedEffect(recording) {
        if (recording) PianoInput.events.collect { e ->
            if (e.source != InputSource.DEMO) current += RecEvent(nowMillis() - recStart, e.midi, e.down, e.velocity)
        }
    }
    // Free-play metronome: starts on the first beat the moment it is switched on.
    // Tempo changes take effect from the next beat without restarting the count.
    val liveBpm by rememberUpdatedState(freeBpm)
    LaunchedEffect(metronome, songMode) {
        if (songMode || !metronome) return@LaunchedEffect
        var beat = 0
        var next = nowMillis().toDouble()
        while (true) {
            Audio.click(beat % 4 == 0); flashBeat(beat % 4 == 0); beat++
            next += 60000.0 / liveBpm
            delay((next - nowMillis()).toLong().coerceAtLeast(1))
        }
    }

    // ---------------------------------------------------------------- song engine: the player's notes
    val currentSpeed by rememberUpdatedState(speed)
    LaunchedEffect(run) {
        val r = run ?: return@LaunchedEffect
        PianoInput.events.collect { e ->
            if (!e.down || e.source == InputSource.DEMO || !pb.playing) return@collect
            val bps = r.data.bpm / 60.0 * currentSpeed
            val t = pb.time
            when (r.mode) {
                PlayMode.PRACTICE -> {
                    val g = r.userGroups.getOrNull(r.groupPtr) ?: return@collect
                    val st = r.groupStart(g)
                    // Accept the chord a little before it reaches the keys (about 0.4 s, at least half a beat).
                    if (t < st - maxOf(0.5, 0.4 * bps)) return@collect
                    val idx = g.firstOrNull { r.events[it].midi == e.midi && !r.hit[it] }
                    if (idx != null) {
                        r.hit[idx] = true
                        r.handPerfect[if (r.events[idx].hand == Hand.RIGHT) 0 else 1]++
                        r.combo++; r.bestCombo = maxOf(r.bestCombo, r.combo)
                        if (g.all { r.hit[it] }) { r.groupPtr++; r.perfect += g.size }
                    } else if (g.none { r.events[it].midi == e.midi } &&
                        r.userGroups.getOrNull(r.groupPtr - 1)?.none { r.events[it].midi == e.midi } != false) {
                        r.wrong++; r.combo = 0
                        r.handWrong[r.handForWrong(e.midi, g)]++
                    }
                    pb.frame++
                }
                // Listen mode scores like Practice: any note of the piece played in time turns green and counts.
                PlayMode.PERFORM, PlayMode.LISTEN -> {
                    // Timing windows in seconds (perfect ±0.12 s, good ±0.28 s), converted to beats at the current tempo.
                    val tol = 0.28 * bps
                    var best = -1
                    var bestD = Double.MAX_VALUE
                    var lo = 0
                    var hi = r.events.size
                    while (lo < hi) { val mid = (lo + hi) / 2; if (r.events[mid].start < t - tol) lo = mid + 1 else hi = mid }
                    for (i in lo until r.events.size) {
                        val ev = r.events[i]
                        if (ev.start > t + tol) break
                        if (!r.playable(i) || r.hit[i] || ev.midi != e.midi) continue
                        val d = abs(ev.start - t)
                        if (d < bestD) { bestD = d; best = i }
                    }
                    if (best >= 0) {
                        r.hit[best] = true
                        r.missed[best] = false
                        val h = if (r.events[best].hand == Hand.RIGHT) 0 else 1
                        if (bestD <= 0.12 * bps) { r.perfect++; r.handPerfect[h]++ } else { r.good++; r.handGood[h]++ }
                        r.combo++; r.bestCombo = maxOf(r.bestCombo, r.combo)
                    } else {
                        r.wrong++; r.combo = 0
                        // Blame the hand whose notes are nearest (in time, then pitch) to the wrong key.
                        val near = ArrayList<Int>()
                        var k = lo
                        while (k > 0 && r.events[k - 1].start > t - 1.0) k--
                        while (k < r.events.size && r.events[k].start <= t + 1.0) { if (r.playable(k)) near += k; k++ }
                        r.handWrong[r.handForWrong(e.midi, near)]++
                    }
                    pb.frame++
                }
            }
        }
    }

    // ---------------------------------------------------------------- song engine: the clock
    LaunchedEffect(run, pb.playing) {
        val r = run ?: return@LaunchedEffect
        if (!pb.playing) return@LaunchedEffect
        var last = -1L
        var scan = 0
        fun clickIndex() = r.clicks.indexOfFirst { it.first >= pb.time - 1e-6 }.let { if (it < 0) r.clicks.size else it }
        var clickPtr = clickIndex()
        var seenSeeks = pb.seeks
        val accomp = settings.accompanimentVolume
        // Sustain pedal: follow the song's pedal marks; restore the state at the current position after a jump.
        val pedal = r.data.pedal
        fun pedalIndex() = pedal.indexOfFirst { it.beat > pb.time + 1e-6 }.let { if (it < 0) pedal.size else it }
        var pedalPtr = pedalIndex()
        var pedalPressed = false
        fun applyPedalState() {
            if (!pedalOn || pedal.isEmpty()) return
            val down = pedal.getOrNull(pedalPtr - 1)?.down == true
            if (down != PianoInput.sustain.value) PianoInput.setSustain(down)
            pedalPressed = down
        }
        applyPedalState()
        try {
        while (pb.playing) {
            val now = withFrameNanos { it }
            // After any jump on the progress bar (backwards or forwards), start scanning notes and clicks from the new position.
            if (pb.seeks != seenSeeks) {
                seenSeeks = pb.seeks
                scan = 0
                clickPtr = clickIndex()
                pedalPtr = pedalIndex()
                applyPedalState()
            }
            if (last < 0) { last = now; continue }
            val dt = ((now - last) / 1e9).coerceAtMost(0.1)
            last = now
            val bps = r.data.bpm / 60.0 * currentSpeed
            var next = pb.time + dt * bps
            if (r.mode == PlayMode.PRACTICE) {
                val g = r.userGroups.getOrNull(r.groupPtr)
                if (g != null) {
                    val st = r.groupStart(g)
                    if (next >= st) { next = st; pb.waiting = true } else pb.waiting = false
                } else pb.waiting = false
            }
            val b = pb.loopB
            if (b != null && next >= b) {
                jumpToLoopStart(r)
                continue
            }
            pb.time = next
            // Metronome: click exactly on the beats of the score (follows tempo and meter changes).
            while (clickPtr < r.clicks.size && r.clicks[clickPtr].first <= next) {
                val (ct, accent) = r.clicks[clickPtr]
                if ((metronome || ct < 0) && !pb.waiting && next - ct < 0.25) { Audio.click(accent); flashBeat(accent) }
                clickPtr++
            }
            while (pedalOn && pedalPtr < pedal.size && pedal[pedalPtr].beat <= next) {
                val pc = pedal[pedalPtr++]
                if (pc.down != PianoInput.sustain.value) PianoInput.setSustain(pc.down)
                pedalPressed = pc.down
            }
            while (scan < r.events.size && r.ended[scan] && r.events[scan].end < next - 8) scan++
            var i = scan
            while (i < r.events.size) {
                val ev = r.events[i]
                if (ev.start > next + 0.05) break
                if (r.mode == PlayMode.LISTEN && !r.counted[i] && next >= ev.start && r.shows(ev.hand) && !pb.outsideLoop(ev.start)) {
                    r.counted[i] = true
                    r.countedTotal++
                }
                if (!r.isUser[i]) {
                    if (!r.started[i] && next >= ev.start && !pb.outsideLoop(ev.start)) {
                        r.started[i] = true
                        PianoInput.noteOn(ev.midi, (ev.velocity * accomp).toInt().coerceIn(20, 127), InputSource.DEMO)
                    }
                } else if (r.mode == PlayMode.PERFORM && !r.hit[i] && !r.missed[i]) {
                    if (next > ev.start + 0.28 * bps + 0.02) { r.missed[i] = true; r.combo = 0 }
                }
                i++
            }
            for (k in scan until minOf(i + 64, r.events.size)) {
                if (!r.isUser[k] && r.started[k] && !r.ended[k] && next >= r.events[k].end - 0.02) {
                    r.ended[k] = true
                    PianoInput.noteOff(r.events[k].midi, InputSource.DEMO)
                }
            }
            pb.frame++
            if (next >= r.length + 0.5 && (r.mode != PlayMode.PRACTICE || r.groupPtr >= r.userGroups.size)) {
                pb.playing = false
                stopDemo()
                pb.finished = true
            }
        }
        } finally {
            // Pausing, finishing or leaving lets go of a pedal the song pressed.
            if (pedalPressed) PianoInput.setSustain(false)
        }
    }
    LaunchedEffect(run, shownHands) { run?.shown = shownHands }

    fun scoreOf(r: SongRun?): Int = r?.let {
        when (it.mode) {
            PlayMode.LISTEN -> if (it.countedTotal == 0) 0 else ((it.perfect + it.good * 0.7) * 100 / (it.countedTotal + it.wrong * 0.5)).toInt().coerceIn(0, 100)
            PlayMode.PRACTICE -> if (it.userTotal == 0) 0 else (100 * it.perfect / (it.perfect + it.wrong).coerceAtLeast(1)).coerceAtMost(100)
            PlayMode.PERFORM -> if (it.userTotal == 0) 0 else ((it.perfect + it.good * 0.7) * 100 / (it.userTotal + it.wrong * 0.5)).toInt().coerceIn(0, 100)
        }
    } ?: 0

    // ---------------------------------------------------------------- layout
    BoxWithConstraints(Modifier.fillMaxSize().background(c.stage)) {
        val screenH = maxHeight
        val screenW = maxWidth
        maxFitWhites = if (screenW < 900.dp) 36 else 52
        val loMidi = WHITE_KEYS[start.coerceIn(0, 52 - whites)]
        val hiMidi = WHITE_KEYS[(start.coerceIn(0, 52 - whites) + whites - 1).coerceAtMost(51)]
        // Two-finger pinch above the keys: spread to see fewer, bigger keys; pinch together for more keys.
        var pinchBase by remember { mutableIntStateOf(whites) }
        val zoom = { scale: Float, begin: Boolean ->
            if (begin) pinchBase = whites
            setWhites((pinchBase / scale).roundToInt())
        }
        Column(Modifier.fillMaxSize()) {
            // ===== Top bar =====
            Row(
                Modifier.fillMaxWidth().height(52.dp).graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).woodGrain(Wood.Mahogany).horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarIcon(Icons.AutoMirrored.Filled.ArrowBack, "Back") { stopDemo(); nav.back() }
                if (songMode) {
                    BarIcon(if (pb.playing) Icons.Default.Pause else Icons.Default.PlayArrow, if (pb.playing) "Pause" else "Play", highlight = true,
                        reserve = listOf("Play", "Pause")) { togglePlay() }
                    BarIcon(Icons.Default.Replay, "Restart") { restart() }
                }
                BarIcon(if (recording) Icons.Default.Stop else Icons.Default.FiberManualRecord, if (recording) "Stop" else "Record",
                    tint = Color(0xFFE0574F), reserve = listOf("Record", "Stop")) {
                    if (recording) {
                        recording = false
                        if (current.isNotEmpty()) Recordings.takes.add(0, current.toList())
                        current.clear()
                    } else { current.clear(); recStart = nowMillis(); recording = true }
                }
                if (Recordings.takes.isNotEmpty()) {
                    // Replay: the play icon starts the recording from the beginning, the square stops it;
                    // the icon returns to play by itself when the recording ends.
                    BarIcon(if (replaying) Icons.Default.Stop else Icons.Default.PlayCircle, if (replaying) "Stop" else "Replay", active = replaying,
                        reserve = listOf("Replay", "Stop")) {
                        if (replaying) { playJob?.cancel(); replaying = false; stopDemo(); return@BarIcon }
                        val take = Recordings.takes.first()
                        replaying = true
                        playJob = scope.launch {
                            val t0 = nowMillis()
                            try {
                                for (e in take) {
                                    val wait = e.t - (nowMillis() - t0)
                                    if (wait > 0) delay(wait)
                                    if (e.down) PianoInput.noteOn(e.midi, e.vel, InputSource.DEMO) else PianoInput.noteOff(e.midi, InputSource.DEMO)
                                }
                                delay(400)
                            } finally { stopDemo(); replaying = false }
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                if (song != null) {
                    Column(Modifier.widthIn(max = 230.dp).padding(horizontal = 4.dp)) {
                        Text(song.title, color = Wood.Ivory, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${difficulty.label} · ${mode.label}" + (if (mode != PlayMode.LISTEN) " · ${hands.label} hand" else ""),
                            color = Wood.Parchment, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
                Spacer(Modifier.width(6.dp))
                if (songMode && mode == PlayMode.LISTEN) {
                    // Which hand's falling notes to show: both -> right -> left -> both. The music keeps playing both.
                    BarIcon(Icons.Default.PanTool, when (shownHands) {
                        HandChoice.BOTH -> "Both hands"; HandChoice.RIGHT -> "Right hand"; HandChoice.LEFT -> "Left hand"
                    }, active = shownHands != HandChoice.BOTH, reserve = listOf("Both hands", "Right hand", "Left hand")) {
                        shownHands = when (shownHands) { HandChoice.BOTH -> HandChoice.RIGHT; HandChoice.RIGHT -> HandChoice.LEFT; HandChoice.LEFT -> HandChoice.BOTH }
                    }
                }
                if (songMode) {
                    Box {
                        BarIcon(Icons.Default.Speed, "${(speed * 100).roundToInt()}%", reserve = listOf("100%", "200%")) { menu = "speed" }
                        DropdownMenu(menu == "speed", { menu = null }) { SpeedPanel(speed) { speed = it } }
                    }
                }
                // Metronome: tap to open the tempo panel (on/off, slider, typed number, -1 / +1).
                // In a song the tempo is the music's tempo, so changing it changes the playback speed smoothly;
                // the clicks stay on the score's beats and the music is never interrupted.
                // Derived, so the screen recomposes only when the score's tempo changes, not on every frame.
                val songBaseState = remember(run, songMode) {
                    derivedStateOf { if (songMode) run?.let { r -> 60.0 / (r.clickLength(pb.time.coerceAtLeast(0.0)) / (r.data.bpm / 60.0)) } else null }
                }
                val songBase = songBaseState.value
                val metroBpm = if (songMode) songBase?.let { (it * speed).roundToInt() } ?: 0 else freeBpm
                Box {
                    BarIcon(Icons.Default.Timer, if (metronome && metroBpm > 0) "\u2669 $metroBpm" else "Metro", active = metronome,
                        reserve = listOf("Metro", "\u2669 888")) { menu = "metro" }
                    DropdownMenu(menu == "metro", { menu = null }) {
                        MetronomePanel(
                            on = metronome, bpm = metroBpm,
                            range = if (songBase != null) (songBase * 0.1).roundToInt().coerceAtLeast(10)..(songBase * 2).roundToInt() else 30..260,
                            onToggle = { metronome = !metronome },
                            onBpm = { bv ->
                                if (songMode) {
                                    if (songBase != null) speed = (bv / songBase).toFloat().coerceIn(0.1f, 2f)
                                } else {
                                    freeBpm = bv.coerceIn(30, 260)
                                    ProgressStore.updateSettings { st -> st.copy(metronomeBpm = freeBpm) }
                                }
                            },
                            note = if (songMode) "Changing the tempo changes the playback speed (now ${(speed * 100).roundToInt()}%), so the clicks always match the music." else null,
                        )
                    }
                }
                // The label stays "Pedal" (so the bar never shifts); the gold highlight shows when it is down.
                BarIcon(PedalIcon, "Pedal", active = sustain, description = if (sustain) "Pedal on" else "Pedal off") { PianoInput.setSustain(!sustain) }
                Box {
                    BarIcon(Icons.Default.Piano, "${totalKeys(loMidi, hiMidi)} keys", reserve = listOf("88 keys")) { menu = "keys" }
                    DropdownMenu(menu == "keys", { menu = null }) {
                        KeysPanel(whites, totalKeys(loMidi, hiMidi), onWhites = { setWhites(it) },
                            onFit = data?.let { d -> { placeForSong(d, maxFitWhites, fit = true); ProgressStore.updateSettings { s -> s.copy(songWhites = whites) } } })
                    }
                }
                Box {
                    val inst = runCatching { Instrument.valueOf(settings.instrument) }.getOrDefault(Instrument.GRAND_PIANO)
                    BarIcon(Icons.Default.MusicNote, inst.displayName.substringBefore(' ')) { menu = "inst" }
                    DropdownMenu(menu == "inst", { menu = null }) {
                        InstrumentFamily.entries.forEach { fam ->
                            Text(fam.label, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                            Instrument.entries.filter { it.family == fam }.forEach { i ->
                                DropdownMenuItem({ Text(i.displayName + if (i == inst) "  ✓" else "") }, onClick = {
                                    menu = null
                                    ProgressStore.updateSettings { it.copy(instrument = i.name) }
                                    Audio.setInstrument(i)
                                })
                            }
                        }
                    }
                }
                Box {
                    BarIcon(Icons.Default.MoreVert, "Menu") { menu = "menu" }
                    DropdownMenu(menu == "menu", { menu = null }) {
                        Text("Key labels", Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                        FlowRow(Modifier.padding(horizontal = 12.dp).widthIn(max = 330.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(KeyLabels.NONE to "None", KeyLabels.C_ONLY to "C only", KeyLabels.WHITE to "White", KeyLabels.ALL to "All", KeyLabels.COLORED to "Coloured")
                                .forEach { (k, n) -> FilterChip(labels == k, { labels = k }, { Text(n) }) }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        if (songMode) {
                            Text("Guide mode", Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                            FlowRow(Modifier.padding(horizontal = 12.dp).widthIn(max = 330.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GuideMode.entries.forEach { g ->
                                    FilterChip(guide == g, { guide = g; ProgressStore.updateSettings { st -> st.copy(guideMode = g.name) } }, { Text(g.label) })
                                }
                            }
                            val pedalLabel = when (data?.pedalSource) {
                                com.pianoquest.app.data.PedalSource.SCORE -> "Sustain pedal from the score"
                                com.pianoquest.app.data.PedalSource.AUTO -> "Automatic sustain pedal"
                                else -> null
                            }
                            if (pedalLabel != null) DropdownMenuItem({ Text("$pedalLabel: " + if (settings.songPedal) "on" else "off") }, onClick = {
                                if (settings.songPedal) PianoInput.setSustain(false)
                                ProgressStore.updateSettings { st -> st.copy(songPedal = !st.songPedal) }
                            })
                            DropdownMenuItem({ Text("Song setup (difficulty, mode, hand)…") }, onClick = { menu = null; pb.playing = false; stopDemo(); nav.back() })
                            if (guide != GuideMode.SHEET_MUSIC) DropdownMenuItem({ Text((if (showSheet) "Hide" else "Show") + " sheet music strip") }, onClick = { showSheet = !showSheet; menu = null })
                            DropdownMenuItem({ Text("About this piece") }, onClick = { menu = null; infoOpen = true })
                        } else {
                            DropdownMenuItem({ Text((if (dual) "Single" else "Dual") + " keyboard") }, onClick = { dual = !dual; menu = null })
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        SoundPanel(songMode, onAdvanced = { menu = null; advanced = true })
                    }
                }
            }

            if (songMode) {
                // ===== Falling notes / sheet music =====
                val r = run
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (r == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (loadError != null) Text("Could not load this song: $loadError", color = Wood.Parchment)
                            else CircularProgressIndicator(color = Wood.Honey)
                        }
                    } else {
                        val gestures = Modifier.fillMaxSize().stageGestures(onTap = { togglePlay() }, onZoom = zoom)
                        if (guide == GuideMode.SHEET_MUSIC) {
                            ScrollingSheet(r, pb, speed, mode, gestures.padding(top = 50.dp))
                        } else {
                            FallingNotes(r, pb, loMidi, hiMidi, speed, mode, settings.solfege, balls = guide == GuideMode.FALLING_NOTE, gestures)
                        }
                        SongStatusBar(r, pb, mode, preRoll, onSeek = { seek(it) }) { scoreOf(r) }
                    }
                }
                if (showSheet && run != null && guide != GuideMode.SHEET_MUSIC) SheetPreview(run, pb, Modifier.fillMaxWidth().background(Color(0xFFF7EEDC)))
            } else {
                // ===== Free play info strip: fixed height so the keyboard never jumps =====
                Box(Modifier.fillMaxWidth().weight(if (dual) 0.35f else 1f).stageGestures(onTap = {}, onZoom = zoom)) {
                    FreePlayInfo(settings.solfege, recording, Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp))
                    if (!dual) Text("Pinch with two fingers here to change the number of keys", color = Wood.Sand.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
                }
            }

            // ===== Keyboard(s) =====
            if (dual && !songMode) {
                StageKeyboard(start2, whites, { start2 = it }, labels, settings.solfege, { emptyMap<Int, Color>() to emptyMap() }, Modifier.weight(1f))
                Spacer(Modifier.height(4.dp))
            }
            val kbModifier = if (songMode) {
                val byWidth = (screenW / whites) * 5.0f
                Modifier.height(minOf(byWidth.coerceIn(96.dp, 230.dp), screenH * 0.40f))
            } else Modifier.weight(if (dual) 1f else 2.4f)
            val keyRight = c.keyRight
            val keyLeft = c.keyLeft
            // Highlights are derived from the playback state, so the keyboard only redraws when they actually change.
            val highlights = remember(run, mode) {
                derivedStateOf {
                    val targets = HashMap<Int, Color>()
                    val demoHands = HashMap<Int, Color>()
                    run?.let { r ->
                        pb.frame.let { }
                        if (mode == PlayMode.PRACTICE) r.userGroups.getOrNull(r.groupPtr)?.forEach { i ->
                            if (!r.hit[i]) targets[r.events[i].midi] = if (r.events[i].hand == Hand.RIGHT) keyRight else keyLeft
                        }
                        for (i in r.events.indices) if (r.started[i] && !r.ended[i]) demoHands[r.events[i].midi] = if (r.events[i].hand == Hand.RIGHT) keyRight else keyLeft
                    }
                    targets to demoHands
                }
            }
            StageKeyboard(start, whites, { s -> start = s }, labels, settings.solfege, { highlights.value }, kbModifier, onZoom = zoom)
        }
        if (a11y.visualMetronome) {
            val gold = Wood.Gold
            Canvas(Modifier.fillMaxSize()) {
                val a = beatFlash.value
                if (a > 0.01f) drawRect(gold.copy(alpha = a), style = Stroke(width = 14.dp.toPx() * a))
            }
        }
    }

    if (advanced) AdvancedSoundDialog { advanced = false }

    if (pb.finished && song != null) {
        val score = scoreOf(run)
        // In Listen mode the result counts only when the player played along.
        val scored = mode != PlayMode.LISTEN || (run?.let { it.perfect + it.good > 0 } == true)
        val stars = when {
            !scored -> 0
            score >= 92 -> 3
            score >= 75 -> 2
            score >= 50 -> 1
            else -> 0
        }
        val recorded = remember(runKey) { mutableStateOf(false) }
        LaunchedEffect(runKey) {
            if (!recorded.value && scored) {
                recorded.value = true
                ProgressStore.recordSong(song.id, score, stars, ((data?.durationSeconds ?: 60.0) / speed / 60).toInt().coerceAtLeast(1),
                    right = run?.handScore(0), left = run?.handScore(1))
            }
        }
        // Back to wherever the song was opened from (song list, home, a composer page or a lesson).
        val origin = nav.stack.getOrNull(nav.stack.indexOfLast { it is com.pianoquest.app.ui.Screen.SongSetup } - 1)
        SongResultDialog(
            scored = scored, stars = stars, score = score, right = run?.handScore(0), left = run?.handScore(1),
            wrong = run?.wrong ?: 0, bestCombo = run?.bestCombo ?: 0, speed = speed, difficulty = difficulty, listen = mode == PlayMode.LISTEN,
            backLabel = when (origin) {
                com.pianoquest.app.ui.Screen.Songs -> "Back to the song list"
                com.pianoquest.app.ui.Screen.Home -> "Back to Home"
                is com.pianoquest.app.ui.Screen.ComposerDetail -> "Back to the composer"
                is com.pianoquest.app.ui.Screen.LessonPlayer, is com.pianoquest.app.ui.Screen.UnitTest -> "Back to the lesson"
                else -> "Back"
            },
            onDismiss = { pb.finished = false },
            onAgain = { restart(); if (mode == PlayMode.LISTEN) mode = PlayMode.PRACTICE },
            onSetup = { pb.finished = false; stopDemo(); nav.back() },
            onBack = {
                pb.finished = false
                stopDemo()
                while (nav.current is com.pianoquest.app.ui.Screen.SongPlayer || nav.current is com.pianoquest.app.ui.Screen.SongSetup) {
                    if (!nav.back()) break
                }
            },
        )
    }
    if (infoOpen && song != null) {
        AlertDialog(
            onDismissRequest = { infoOpen = false },
            title = { Text(song.title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(song.composer, style = MaterialTheme.typography.titleSmall)
                    Text("${song.genre.displayName} · ${song.difficultyLabel} · key ${song.key} · ${song.meter}", style = MaterialTheme.typography.labelMedium)
                    if (song.about.isNotBlank()) RichText(song.about)
                    if (song.tips.isNotEmpty()) RichText(song.tips.joinToString("\n") { "- $it" })
                    Text(song.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { infoOpen = false }) { Text("Close") } },
        )
    }
}

/**
 * One-finger tap (start/pause) and two-finger pinch (key count). [onZoom] gets the scale relative to the start of
 * the pinch and whether this is its first step.
 */
private fun Modifier.stageGestures(onTap: () -> Unit, onZoom: (Float, Boolean) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downTime = down.uptimeMillis
        var startDist = 0f
        var pinching = false
        var moved = false
        while (true) {
            val ev = awaitPointerEvent(PointerEventPass.Main)
            val pressed = ev.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                val a = pressed[0].position
                val b = pressed[1].position
                val d = hypot(a.x - b.x, a.y - b.y)
                if (!pinching) { pinching = true; startDist = d; onZoom(1f, true) }
                else if (startDist > 10f) onZoom(d / startDist, false)
                ev.changes.forEach { it.consume() }
            } else if (pressed.isEmpty()) {
                val up = ev.changes.firstOrNull()
                if (!pinching && !moved && up != null && up.uptimeMillis - downTime < 450) onTap()
                break
            } else {
                val ch = pressed[0]
                if ((ch.position - down.position).getDistance() > 24f) moved = true
            }
        }
    }
}

@Composable
private fun FreePlayInfo(solfege: Boolean, recording: Boolean, modifier: Modifier) {
    val held by PianoInput.held.collectAsState()
    val chordName = remember(held) { Chord.identify(held)?.prettySymbol }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Column {
            Text(
                chordName ?: if (held.isNotEmpty()) held.sorted().joinToString("  ") { keyLabel(it, solfege, true) } else "Play something",
                style = MaterialTheme.typography.headlineSmall, color = Wood.Parchment, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                when {
                    recording -> "● Recording…"
                    chordName != null -> held.sorted().joinToString("  ") { keyLabel(it, solfege, true) }
                    isDesktop -> "Computer keys: A W S E D F T G Y H U J K · Z/X octave (C${ComputerKeyboard.baseOctave.value}) · Space = sustain"
                    else -> "Touch lower on a key to play louder"
                },
                style = MaterialTheme.typography.bodySmall, color = if (recording) Color(0xFFE0574F) else Wood.Sand,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Seek bar with the A-B loop, loop button and the status line. Reads the fast-changing state itself. */
@Composable
private fun SongStatusBar(r: SongRun, pb: Playback, mode: PlayMode, preRoll: Double, onSeek: (Double) -> Unit, score: () -> Int) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val len = r.length.coerceAtLeast(1.0)
            LoopSeekBar(
                progress = { (pb.time / len).toFloat().coerceIn(0f, 1f) },
                loopA = pb.loopA?.let { (it / len).toFloat() }, loopB = pb.loopB?.let { (it / len).toFloat() },
                onSeek = { onSeek(it * r.length) },
                modifier = Modifier.weight(1f).height(28.dp),
            )
            Spacer(Modifier.width(8.dp))
            LoopButton(pb.loopA, pb.loopB) {
                when {
                    pb.loopA == null -> pb.loopA = pb.time.coerceAtLeast(0.0)
                    pb.loopB == null -> { if (pb.time > pb.loopA!! + 0.5) pb.loopB = pb.time else pb.loopA = pb.time.coerceAtLeast(0.0) }
                    else -> { pb.loopA = null; pb.loopB = null }
                }
            }
        }
        val status by remember(r, mode) {
            derivedStateOf {
                pb.frame.let { }
                when {
                    !pb.playing && !pb.finished -> if (pb.time <= -preRoll + 0.01) "Tap the dark area to start" else "Paused - tap the dark area to resume"
                    mode == PlayMode.LISTEN -> if (r.perfect + r.good == 0) "${mode.help} - or play along on the keys"
                        else "Played along ${score()}%  ·  perfect ${r.perfect}  ·  good ${r.good}  ·  combo ${r.combo}"
                    mode == PlayMode.PRACTICE -> if (pb.waiting) "Waiting for you - play the highlighted keys" else "Accuracy ${score()}%  ·  combo ${r.combo}"
                    else -> "Score ${score()}%  ·  perfect ${r.perfect}  ·  good ${r.good}  ·  combo ${r.combo}"
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(status, color = Wood.Parchment, style = MaterialTheme.typography.labelLarge, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            if (pb.loopA != null) Text(if (pb.loopB == null) "   ·   Loop: A set - press the loop button again at B" else "   ·   Looping A-B - press loop to clear",
                color = LoopColor, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

private fun totalKeys(lo: Int, hi: Int) = hi - lo + 1

@Composable
private fun BarIcon(
    icon: ImageVector, label: String, tint: Color = Wood.Parchment, highlight: Boolean = false, active: Boolean = false,
    reserve: List<String> = emptyList(), description: String = label, onClick: () -> Unit,
) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (highlight) Wood.Gold.copy(alpha = 0.9f) else if (active) Wood.Gold.copy(alpha = 0.45f) else Color.Transparent)
            .clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, description, tint = if (highlight) Wood.Walnut else tint, modifier = Modifier.size(24.dp))
        // Invisible copies of every label this button can show keep its width fixed, so the bar never shifts
        // when a label changes (Play/Pause, hand, tempo, ...).
        Box(contentAlignment = Alignment.Center) {
            reserve.forEach { Text(it, fontSize = 10.sp, maxLines = 1, modifier = Modifier.alpha(0f).clearAndSetSemantics { }) }
            Text(label, fontSize = 10.sp, color = if (highlight) Wood.Walnut else if (active) Wood.Ivory else Wood.Sand, maxLines = 1,
                modifier = Modifier.clearAndSetSemantics { })
        }
    }
}

/**
 * The result shown when a song ends: stars, overall and per-hand scores, then three full-width buttons stacked so they
 * fit any screen - play again (or practise it, after Listen), change the setup, or go back to where the song came from.
 */
@Composable
internal fun SongResultDialog(
    scored: Boolean, stars: Int, score: Int, right: Int?, left: Int?, wrong: Int, bestCombo: Int, speed: Float, difficulty: Difficulty,
    listen: Boolean, backLabel: String, onDismiss: () -> Unit, onAgain: () -> Unit, onSetup: () -> Unit, onBack: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = 620.dp).padding(horizontal = 16.dp),
        text = {
            val summary = @Composable { m: Modifier ->
                Column(m, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (!scored) "That's how it goes!" else if (stars >= 2) "Bravo!" else "Piece complete",
                        style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (scored) {
                        Stars(stars, size = 28.dp)
                        Text("Score $score%", style = MaterialTheme.typography.titleLarge)
                        if (right != null || left != null) Text(listOfNotNull(right?.let { "Right hand $it%" }, left?.let { "Left hand $it%" }).joinToString("  ·  "),
                            style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Wrong notes: $wrong  ·  best combo: $bestCombo", style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        if (speed < 1f) Text("Played at ${(speed * 100).roundToInt()}% speed - try faster next time!", style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        if (difficulty != Difficulty.HARD) Text("Ready for more? Try ${Difficulty.entries[difficulty.ordinal + 1].label}.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (listen) Text("Now try Note Wait - the music waits for you.", style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            val buttons = @Composable { m: Modifier ->
                Column(m, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAgain, Modifier.fillMaxWidth().height(50.dp)) {
                        Icon(if (listen) Icons.Default.Piano else Icons.Default.Replay, null, Modifier.size(20.dp))
                        Text(if (listen) "  Practise it" else "  Play again", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    }
                    OutlinedButton(onClick = onSetup, Modifier.fillMaxWidth().height(46.dp)) { Text("Song setup", maxLines = 1) }
                    OutlinedButton(onClick = onBack, Modifier.fillMaxWidth().height(46.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp)); Text("  $backLabel", maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            // Results beside the buttons (landscape phones are short but wide), or above them on narrow screens.
            BoxWithConstraints {
                if (maxWidth > 440.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        summary(Modifier.weight(1f))
                        buttons(Modifier.width(230.dp))
                    }
                } else {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        summary(Modifier.fillMaxWidth())
                        buttons(Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
    )
}

/** Metronome panel: on/off, the tempo in BPM with -1 / +1, a slider and a box to type a number. */
@Composable
private fun MetronomePanel(on: Boolean, bpm: Int, range: IntRange, onToggle: () -> Unit, onBpm: (Int) -> Unit, note: String?) {
    var typed by remember(bpm) { mutableStateOf(bpm.toString()) }
    Column(Modifier.width(320.dp).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onToggle, modifier = Modifier.fillMaxWidth().height(46.dp)) {
            Icon(Icons.Default.Timer, null); Text(if (on) "  Stop metronome" else "  Start metronome")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onBpm((bpm - 1).coerceIn(range)) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.size(48.dp)) { Text("\u2212", fontSize = 22.sp) }
            androidx.compose.material3.OutlinedTextField(
                typed, { v ->
                    typed = v.filter { it.isDigit() }.take(3)
                    typed.toIntOrNull()?.let { if (it in range) onBpm(it) }
                },
                Modifier.weight(1f), singleLine = true, label = { Text("BPM") },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )
            OutlinedButton(onClick = { onBpm((bpm + 1).coerceIn(range)) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier.size(48.dp)) { Text("+", fontSize = 22.sp) }
        }
        Slider(bpm.toFloat().coerceIn(range.first.toFloat(), range.last.toFloat()), { onBpm(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat())
        Text("${range.first} - ${range.last} BPM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun LoopButton(a: Double?, b: Double?, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(10.dp), color = if (a != null) LoopColor else Color.White.copy(alpha = 0.12f),
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Repeat, "A-B loop", tint = if (a != null) Wood.Walnut else Wood.Parchment, modifier = Modifier.size(18.dp))
            Text(when { a == null -> " A-B"; b == null -> " A→?"; else -> " A↔B" }, color = if (a != null) Wood.Walnut else Wood.Parchment,
                fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SpeedPanel(speed: Float, onChange: (Float) -> Unit) {
    Column(Modifier.width(300.dp).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Playback speed: ${(speed * 100).roundToInt()}%", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onChange((speed - 0.05f).coerceAtLeast(0.1f)) }) { Text("−", fontSize = 20.sp) }
            Slider(speed, { onChange((it * 20).roundToInt() / 20f) }, valueRange = 0.1f..2f, modifier = Modifier.weight(1f))
            TextButton(onClick = { onChange((speed + 0.05f).coerceAtMost(2f)) }) { Text("+", fontSize = 20.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { v -> FilterChip(abs(speed - v) < 0.01f, { onChange(v) }, { Text("${(v * 100).roundToInt()}") }) }
        }
    }
}

@Composable
private fun KeysPanel(whites: Int, total: Int, onWhites: (Int) -> Unit, onFit: (() -> Unit)?) {
    Column(Modifier.width(320.dp).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Keys on screen: $total", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onWhites(whites - 1) }, enabled = whites > 8) { Text("Fewer") }
            Slider(whites.toFloat(), { onWhites(it.roundToInt()) }, valueRange = 8f..52f, modifier = Modifier.weight(1f).padding(horizontal = 6.dp))
            OutlinedButton(onClick = { onWhites(whites + 1) }, enabled = whites < 52) { Text("More") }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            KEY_PRESETS.forEach { (n, w) -> FilterChip(whites == w, { onWhites(w) }, { Text("$n") }) }
            if (onFit != null) FilterChip(false, onFit, { Text("Fit song") })
        }
        Text("Tip: pinch with two fingers above the keys to zoom. Drag the strip above the keys to move along the keyboard.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Sound section of the menu: the everyday mixing controls. */
@Composable
private fun SoundPanel(songMode: Boolean, onAdvanced: () -> Unit) {
    val s = ProgressStore.state.collectAsState().value.settings
    Column(Modifier.width(330.dp).padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("Sound", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        LabeledSlider("Volume", "${(s.volume * 100).roundToInt()}%", s.volume, 0f..1f) { v ->
            ProgressStore.updateSettings { it.copy(volume = v) }; if (Audio.isReady) Audio.engine.masterVolume = v
        }
        LabeledSlider("Reverb", "${(s.reverb * 100).roundToInt()}%", s.reverb, 0f..0.6f) { v ->
            ProgressStore.updateSettings { it.copy(reverb = v) }; if (Audio.isReady) Audio.engine.reverbAmount = v
        }
        LabeledSlider("Room size", if (s.fxRoomSize < 0.33f) "Room" else if (s.fxRoomSize < 0.7f) "Hall" else "Cathedral", s.fxRoomSize, 0f..1f) { v ->
            ProgressStore.updateSettings { it.copy(fxRoomSize = v) }
        }
        LabeledSlider("Pedal sustain", "${(s.fxSustainLevel * 100).roundToInt()}%", s.fxSustainLevel, 0f..1f) { v ->
            ProgressStore.updateSettings { it.copy(fxSustainLevel = v) }
        }
        if (songMode) LabeledSlider("Accompaniment", "${(s.accompanimentVolume * 100).roundToInt()}%", s.accompanimentVolume, 0.2f..1.2f) { v ->
            ProgressStore.updateSettings { it.copy(accompanimentVolume = v) }
        }
        Text("Touch sensitivity", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("OFF" to "Off", "LIGHT" to "Light", "NORMAL" to "Normal", "HEAVY" to "Heavy").forEach { (k, n) ->
                FilterChip(s.touchSensitivity == k, { ProgressStore.updateSettings { it.copy(touchSensitivity = k) } }, { Text(n) })
            }
        }
        TextButton(onClick = onAdvanced) { Text("Advanced sound engineering…") }
    }
}

@Composable
private fun LabeledSlider(label: String, value: String, v: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Slider(v, onChange, valueRange = range, modifier = Modifier.height(30.dp))
}

/** Keyboard with the draggable 88-key navigator strip above it. */
@Composable
private fun StageKeyboard(
    start: Int, whites: Int, onStart: (Int) -> Unit, labels: KeyLabels, solfege: Boolean,
    highlights: () -> Pair<Map<Int, Color>, Map<Int, Color>>, modifier: Modifier, onZoom: ((Float, Boolean) -> Unit)? = null,
) {
    val s = start.coerceIn(0, 52 - whites)
    val lo = WHITE_KEYS[s]
    val hi = WHITE_KEYS[(s + whites - 1).coerceAtMost(51)]
    val (targets, hands) = highlights()
    // Its own offscreen layer: the GPU reuses the rendered keyboard until a key actually changes.
    Column(modifier.fillMaxWidth().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).woodGrain(Wood.Walnut, seed = 3)) {
        Row(Modifier.height(34.dp).then(if (onZoom != null) Modifier.stageGestures({}, onZoom) else Modifier), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onStart((s - 7).coerceAtLeast(0)) }, Modifier.size(30.dp)) { Icon(Icons.Default.ChevronLeft, "Octave down", tint = Wood.Parchment) }
            Navigator88(s, whites, onStart, Modifier.weight(1f).height(26.dp))
            IconButton(onClick = { onStart((s + 7).coerceAtMost(52 - whites)) }, Modifier.size(30.dp)) { Icon(Icons.Default.ChevronRight, "Octave up", tint = Wood.Parchment) }
        }
        PianoKeyboard(lo, hi, Modifier.fillMaxWidth().weight(1f), highlights = targets, handColors = hands, labels = labels, solfege = solfege)
    }
}

/**
 * The 88-key map above the keyboard. The highlighted window shows the visible keys: drag it (it follows the finger
 * exactly where you grabbed it) or tap anywhere to jump there.
 */
@Composable
private fun Navigator88(start: Int, visible: Int, onStart: (Int) -> Unit, modifier: Modifier) {
    val currentStart by rememberUpdatedState(start)
    val currentVisible by rememberUpdatedState(visible)
    val move by rememberUpdatedState(onStart)
    Canvas(
        modifier.clip(RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                detectTapGestures { p -> move(((p.x / size.width) * 52 - currentVisible / 2f).roundToInt().coerceIn(0, 52 - currentVisible)) }
            }
            .pointerInput(Unit) {
                var grab = 0f
                detectDragGestures(
                    onDragStart = { p ->
                        val w = size.width / 52f
                        val left = currentStart * w
                        val right = left + currentVisible * w
                        // Grabbing the window keeps the finger's spot inside it; grabbing outside centres the window there.
                        grab = if (p.x in left..right) p.x - left else currentVisible * w / 2
                    },
                ) { change, _ ->
                    val w = size.width / 52f
                    val ns = ((change.position.x - grab) / w).roundToInt().coerceIn(0, 52 - currentVisible)
                    if (ns != currentStart) move(ns)
                    change.consume()
                }
            }
    ) {
        val w = size.width / 52
        WHITE_KEYS.forEachIndexed { i, _ -> drawRect(Color(0xFFFBF4E4), Offset(i * w + 0.5f, 0f), Size(w - 1f, size.height)) }
        WHITE_KEYS.forEachIndexed { i, m ->
            if (i < 51 && isBlackKey(m + 1)) drawRect(Color(0xFF17110D), Offset((i + 1) * w - w * 0.3f, 0f), Size(w * 0.6f, size.height * 0.6f))
        }
        drawRoundRect(Wood.Gold.copy(alpha = 0.38f), Offset(start * w, 0f), Size(visible * w, size.height), CornerRadius(4f))
        drawRoundRect(Wood.Gold, Offset(start * w, 0f), Size(visible * w, size.height), CornerRadius(4f), style = Stroke(3.5f))
        // Grip lines in the middle of the window.
        val cx = (start + visible / 2f) * w
        for (k in -1..1) drawRect(Wood.Chestnut, Offset(cx + k * 6f - 1f, size.height * 0.25f), Size(2.5f, size.height * 0.5f))
    }
}

/** A strip of real notation showing the next few note groups for the selected hand(s). */
@Composable
private fun SheetPreview(run: SongRun, pb: Playback, modifier: Modifier) {
    val idx = remember(run, run.shown) { run.events.indices.filter { run.playable(it) } }
    val starts = remember(run) { idx.map { run.events[it].start }.distinct().sorted() }
    // Recompose only when the first visible chord changes, not on every frame.
    val firstIdx by remember(run) {
        derivedStateOf { starts.indexOfFirst { it >= pb.time - 0.01 }.let { if (it < 0) starts.size else it } }
    }
    val els = remember(run, firstIdx) {
        starts.drop(firstIdx).take(10).map { s ->
            val group = idx.map { run.events[it] }.filter { it.start == s }
            StaffEl.Note(group.map { it.pitch }.distinctBy { it.midi }, group.minOf { it.duration })
        }
    }
    val clef = when {
        els.isEmpty() -> Clef.TREBLE
        els.all { e -> e.pitches.all { it.midi >= 57 } } -> Clef.TREBLE
        els.all { e -> e.pitches.all { it.midi <= 64 } } -> Clef.BASS
        else -> Clef.GRAND
    }
    CompositionLocalProvider(LocalPQColors provides PQ.colors.copy(staffInk = Color(0xFF2A170D))) {
        Box(modifier.padding(horizontal = 8.dp)) {
            StaffView("", elements = els, clef = clef, highlightIndex = 0, space = if (clef == Clef.GRAND) 5.dp else 6.5.dp)
        }
    }
}
