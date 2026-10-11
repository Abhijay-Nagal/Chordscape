package com.pianoquest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.Difficulty
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.Screen
import com.pianoquest.app.ui.components.FitToHeight
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.theme.Wood
import com.pianoquest.app.ui.theme.woodGrain

/** How the notes are shown while a song plays. */
enum class GuideMode(val label: String, val help: String) {
    SHEET_MUSIC("Sheet Music", "Real notation scrolls towards the play line"),
    WATERFALL("Waterfall", "Notes fall as bars - longer bar, longer note"),
    FALLING_NOTE("Fallen Note", "Each note falls as a coloured ball"),
}

private val DIFFICULTY_COLOR = Color(0xFF7A4526)
private val GUIDE_COLOR = Color(0xFF55693A)
private val PLAY_COLOR = Color(0xFF34506A)

/**
 * The screen shown when a song is opened: choose difficulty, guide mode (how notes are shown) and play mode,
 * then Start. Back from the player returns here; back from here returns to the song list.
 */
@Composable
fun SongSetupScreen(songId: String) {
    val nav = LocalNavigator.current
    val song = remember(songId) { SongLibrary.get(songId) } ?: run { nav.back(); return }
    val p by ProgressStore.state.collectAsState()
    val s = p.settings
    val difficulty = runCatching { Difficulty.valueOf(s.difficulty) }.getOrDefault(Difficulty.HARD)
    val guide = runCatching { GuideMode.valueOf(s.guideMode) }.getOrDefault(GuideMode.WATERFALL)
    val mode = runCatching { PlayMode.valueOf(s.playMode) }.getOrDefault(PlayMode.PRACTICE)
    val hand = runCatching { HandChoice.valueOf(s.hand) }.getOrDefault(HandChoice.RIGHT)
    val favorite = songId in p.favoriteSongs
    var info by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().woodGrain(Wood.Walnut, seed = 5)) {
        // Header
        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Wood.Parchment) }
            Column(Modifier.weight(1f)) {
                Text(song.title, color = Wood.Ivory, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${song.composer}  ·  ${song.genre.displayName}  ·  ${formatDuration(song.durationSeconds)}", color = Wood.Sand,
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { ProgressStore.toggleFavorite(songId) }) {
                Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove from favourites" else "Add to favourites",
                    tint = if (favorite) Color(0xFFE0574F) else Wood.Parchment)
            }
            IconButton(onClick = { info = true }) { Icon(Icons.Default.Info, "About this piece", tint = Wood.Parchment) }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)) {
            val wide = maxWidth > maxHeight
            val panels = @Composable { m: Modifier ->
                SetupPanel("Difficulty", difficulty.blurb, DIFFICULTY_COLOR, m) {
                    TileGrid(Difficulty.entries, difficulty, { ProgressStore.updateSettings { st -> st.copy(difficulty = it.name) } }, DIFFICULTY_COLOR,
                        label = { it.label }) { d ->
                        Text("★".repeat(when (d) { Difficulty.EASY -> 1; Difficulty.MEDIUM -> 3; Difficulty.HARD -> 5 }),
                            color = Wood.Brass, fontSize = 20.sp, maxLines = 1)
                    }
                }
                SetupPanel("Guide mode", guide.help, GUIDE_COLOR, m) {
                    TileGrid(GuideMode.entries, guide, { ProgressStore.updateSettings { st -> st.copy(guideMode = it.name) } }, GUIDE_COLOR,
                        label = { it.label }) { g -> GuideIcon(g, Modifier.size(30.dp)) }
                }
                SetupPanel("Play mode", mode.help, PLAY_COLOR, m) {
                    TileGrid(listOf(PlayMode.PRACTICE, PlayMode.PERFORM, PlayMode.LISTEN), mode,
                        { ProgressStore.updateSettings { st -> st.copy(playMode = it.name) } }, PLAY_COLOR, label = { it.label }) { pm ->
                        Icon(playIcon(pm), null, tint = Wood.Chestnut, modifier = Modifier.size(28.dp))
                    }
                }
            }
            if (wide) {
                Row(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) { panels(Modifier.weight(1f).fillMaxHeight()) }
            } else {
                Column(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) { panels(Modifier.weight(1f).fillMaxWidth()) }
            }
        }
        // Bottom bar: hand, auto scroll, start.
        Row(
            Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(16.dp)).background(Wood.Ivory).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Your hand", color = Wood.Walnut, style = MaterialTheme.typography.labelLarge)
            HandChoice.entries.forEach { h ->
                val on = h == hand && mode != PlayMode.LISTEN
                Box(
                    Modifier.clip(RoundedCornerShape(10.dp)).background(if (on) Wood.Chestnut else Wood.Parchment)
                        .clickable(enabled = mode != PlayMode.LISTEN) { ProgressStore.updateSettings { st -> st.copy(hand = h.name) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(h.label, color = if (on) Wood.Ivory else if (mode == PlayMode.LISTEN) Wood.Tan else Wood.Walnut, fontWeight = FontWeight.SemiBold)
                }
            }
            if (mode == PlayMode.LISTEN) {
                Text("In Listen mode, choose which hand's notes to show with the hand button while the song plays.", color = Wood.Chestnut,
                    style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.weight(1f).padding(start = 6.dp))
            } else Spacer(Modifier.weight(1f))
            Button(
                onClick = { nav.push(Screen.SongPlayer(songId)) },
                colors = ButtonDefaults.buttonColors(containerColor = Wood.Chestnut, contentColor = Wood.Ivory),
                modifier = Modifier.height(48.dp).widthIn(min = 160.dp),
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Text(" START", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (info) {
        AlertDialog(
            onDismissRequest = { info = false },
            title = { Text(song.title) },
            text = {
                FitToHeight(Modifier.height(320.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(song.composer, style = MaterialTheme.typography.titleSmall)
                        Text("${song.genre.displayName} · ${song.difficultyLabel} · key ${song.key} · ${song.meter}", style = MaterialTheme.typography.labelMedium)
                        if (song.about.isNotBlank()) RichText(song.about)
                        if (song.tips.isNotEmpty()) RichText(song.tips.joinToString("\n") { "- $it" })
                        Text(song.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { info = false }) { Text("Close") } },
        )
    }
}

@Composable
private fun SetupPanel(title: String, help: String, color: Color, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(color).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title.uppercase(), color = Wood.Ivory, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Text(help, color = Wood.Parchment, style = MaterialTheme.typography.bodyMedium, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        content()
    }
}

@Composable
private fun TileLabel(text: String) {
    Text(text, color = Wood.Walnut, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 1)
}

/** Two tiles on the first row, one wide tile below - the layout of the reference design. */
@Composable
private fun <T> ColumnScope.TileGrid(items: List<T>, selected: T, onSelect: (T) -> Unit, color: Color, label: (T) -> String,
                                      icon: @Composable (T) -> Unit) {
    Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.take(2).forEach { Tile(it == selected, color, { onSelect(it) }, Modifier.weight(1f).fillMaxHeight(), label(it)) { icon(it) } }
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            items.drop(2).forEach { Tile(it == selected, color, { onSelect(it) }, Modifier.weight(1f).fillMaxHeight(), label(it)) { icon(it) } }
        }
    }
}

@Composable
private fun Tile(selected: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier, label: String, icon: @Composable () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    BoxWithConstraints(modifier.clip(shape).background(if (selected) Wood.Ivory else Wood.Ivory.copy(alpha = 0.82f))
        .border(if (selected) 3.dp else 0.dp, if (selected) Wood.Gold else Color.Transparent, shape).clickable(onClick = onClick)) {
        val roomy = maxWidth >= 170.dp
        // Icon and label grow with the tile, so big tiles (tablets) never show tiny content in a large empty box.
        val grow = (minOf(maxWidth / 150.dp, maxHeight / 110.dp)).coerceIn(1f, 1.9f)
        val d = androidx.compose.ui.platform.LocalDensity.current
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides
            androidx.compose.ui.unit.Density(d.density * grow, d.fontScale)) {
        if (maxHeight < 72.dp) {
            // Short tiles (small phones in landscape): icon beside the label so nothing is clipped.
            Row(Modifier.fillMaxSize().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                if (roomy) icon()
                TileLabel(label)
            }
        } else {
            Column(Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) { icon(); TileLabel(label) }
        }
        }
        if (selected) {
            // Check mark in a corner flag, as in the reference.
            Canvas(Modifier.align(Alignment.TopEnd).size(30.dp)) {
                val path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height); close() }
                drawPath(path, color)
                val w = size.width
                drawLine(Wood.Ivory, Offset(w * 0.52f, w * 0.30f), Offset(w * 0.66f, w * 0.44f), strokeWidth = w * 0.08f)
                drawLine(Wood.Ivory, Offset(w * 0.66f, w * 0.44f), Offset(w * 0.90f, w * 0.14f), strokeWidth = w * 0.08f)
            }
        }
    }
}

internal fun playIcon(m: PlayMode): ImageVector = when (m) {
    PlayMode.PRACTICE -> Icons.Default.HourglassTop
    PlayMode.LISTEN -> Icons.Default.Headphones
    PlayMode.PERFORM -> Icons.Default.Piano
}

/** Small pictures of the three guide modes. */
@Composable
fun GuideIcon(g: GuideMode, modifier: Modifier) {
    Canvas(modifier) {
        val ink = Wood.Chestnut
        val w = size.width
        val h = size.height
        when (g) {
            GuideMode.SHEET_MUSIC -> {
                for (i in 0 until 5) drawRect(ink, Offset(0f, h * (0.18f + i * 0.15f)), Size(w, 1.6f))
                drawOval(ink, Offset(w * 0.25f, h * 0.55f), Size(w * 0.22f, h * 0.15f))
                drawRect(ink, Offset(w * 0.45f, h * 0.18f), Size(2.2f, h * 0.45f))
                drawOval(ink, Offset(w * 0.6f, h * 0.40f), Size(w * 0.22f, h * 0.15f))
                drawRect(ink, Offset(w * 0.8f, h * 0.05f), Size(2.2f, h * 0.43f))
            }
            GuideMode.WATERFALL -> {
                val bars = listOf(0.15f to 0.55f, 0.05f to 0.35f, 0.3f to 0.65f, 0.0f to 0.5f, 0.2f to 0.75f)
                bars.forEachIndexed { i, (top, len) ->
                    drawRoundRect(ink, Offset(w * (0.04f + i * 0.195f), h * top), Size(w * 0.15f, h * len), CornerRadius(3f))
                }
                drawRect(Wood.Gold, Offset(0f, h * 0.9f), Size(w, h * 0.08f))
            }
            GuideMode.FALLING_NOTE -> {
                drawCircle(ink, w * 0.42f, Offset(w / 2, h / 2))
                drawOval(Wood.Ivory, Offset(w * 0.30f, h * 0.55f), Size(w * 0.22f, h * 0.16f))
                drawRect(Wood.Ivory, Offset(w * 0.49f, h * 0.24f), Size(w * 0.06f, h * 0.40f))
                drawRect(Wood.Ivory, Offset(w * 0.49f, h * 0.24f), Size(w * 0.2f, h * 0.06f))
            }
        }
    }
}
