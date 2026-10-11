package com.pianoquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pianoquest.app.data.Genre
import com.pianoquest.app.data.ImportedSongs
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Song
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.platform.canImportFiles
import com.pianoquest.app.platform.pickMidiFile
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.Card
import com.pianoquest.app.ui.components.Stars

private fun genreColor(g: Genre): Long = when (g) {
    Genre.BEGINNER -> 0xFF8A6A3A; Genre.FOLK -> 0xFF55693A; Genre.HOLIDAY -> 0xFF7A2E2A; Genre.BAROQUE -> 0xFF6B3C21
    Genre.CLASSICAL -> 0xFF4A2416; Genre.ROMANTIC -> 0xFF7A2E2A; Genre.MODERN_CLASSICAL -> 0xFF5B4A6A; Genre.RAGTIME -> 0xFFA26B3E
    Genre.JAZZ -> 0xFF2A170D; Genre.BLUES -> 0xFF3A4A5E; Genre.POP -> 0xFF9A5A2A; Genre.CONTEMPORARY -> 0xFF4E6A5E
    Genre.LATIN -> 0xFFA0522D; Genre.GOSPEL -> 0xFF5E3A5A; Genre.EXERCISE -> 0xFF6B4E36; Genre.IMPORTED -> 0xFFC08A3E
}

enum class SongTab(val label: String) { ALL("All songs"), FAVORITES("Favourites"), MINE("My songs") }

/**
 * The library's filters and scroll position live outside the screen, so opening a song and coming back
 * returns you to exactly the same category, level, search and place in the list.
 */
private object SongsUi {
    var tab by mutableStateOf(SongTab.ALL)
    var query by mutableStateOf("")
    var genre by mutableStateOf<Genre?>(null)
    var level by mutableStateOf<Int?>(null)
    var firstItem = 0
    var firstOffset = 0
}

private class PendingImport(val fileName: String, val bytes: ByteArray, val title: String)

@Composable
fun SongsScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<PendingImport?>(null) }
    val imported by ImportedSongs.entries.collectAsState()
    val mine = imported.map(ImportedSongs::toSong)
    val base = when (SongsUi.tab) {
        SongTab.ALL -> SongLibrary.all
        SongTab.FAVORITES -> (mine + SongLibrary.all).filter { it.id in p.favoriteSongs }
        SongTab.MINE -> mine
    }
    val query = SongsUi.query
    val songs = com.pianoquest.app.data.Popularity.sort(base).filter { s ->
        (SongsUi.genre == null || s.genre == SongsUi.genre) && (SongsUi.level == null || s.difficulty == SongsUi.level) &&
            (query.isBlank() || s.title.contains(query, true) || s.composer.contains(query, true) || s.genre.displayName.contains(query, true))
    }
    fun pick() = pickMidiFile { name, bytes ->
        ImportedSongs.validate(name, bytes)
            .onSuccess { title -> message = null; pending = PendingImport(name, bytes, title) }
            .onFailure { message = "Could not open $name: ${it.message ?: "not a valid MIDI file"}" }
    }
    val grid = rememberLazyGridState(SongsUi.firstItem, SongsUi.firstOffset)
    DisposableEffect(Unit) {
        onDispose {
            SongsUi.firstItem = grid.firstVisibleItemIndex
            SongsUi.firstOffset = grid.firstVisibleItemScrollOffset
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Phones: one song per row (room for both hand bars). Tablets: two per row.
        val cols = if (com.pianoquest.app.ui.LocalFormFactor.current == com.pianoquest.app.ui.FormFactor.TABLET && maxWidth > 700.dp) 2 else 1
        LazyVerticalGrid(GridCells.Fixed(cols), Modifier.fillMaxSize(), state = grid, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Song Library", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.weight(1f))
                        OutlinedTextField(SongsUi.query, { SongsUi.query = it }, Modifier.widthIn(max = 340.dp).weight(1f, fill = false),
                            placeholder = { Text("Search songs, composers") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
                            shape = RoundedCornerShape(16.dp))
                    }
                    // Library rows: everything, your favourites, and the MIDI files you saved.
                    com.pianoquest.app.ui.components.ScrollHintRow(Modifier.fillMaxWidth()) {
                        TabButton(SongTab.ALL, Icons.Default.LibraryMusic, SongLibrary.all.size)
                        TabButton(SongTab.FAVORITES, Icons.Default.Favorite, p.favoriteSongs.size)
                        TabButton(SongTab.MINE, Icons.Default.FolderOpen, mine.size)
                        if (canImportFiles) {
                            Spacer(Modifier.width(4.dp))
                            Button(onClick = { pick() }) { Icon(Icons.Default.Add, null); Text(" Upload MIDI file") }
                        }
                    }
                    message?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    // Picking a style shows all its songs: the level resets to "Any level", then can be narrowed down.
                    com.pianoquest.app.ui.components.ScrollHintRow(Modifier.fillMaxWidth(), spacing = 6.dp) {
                        FilterChip(SongsUi.genre == null, { SongsUi.genre = null; SongsUi.level = null }, { Text("All styles") })
                        Genre.entries.filter { g -> g != Genre.IMPORTED || SongsUi.tab != SongTab.ALL }
                            .filter { g -> SongLibrary.all.any { it.genre == g } || g == Genre.IMPORTED }.forEach { g ->
                            FilterChip(SongsUi.genre == g, { SongsUi.genre = if (SongsUi.genre == g) null else g; SongsUi.level = null }, { Text(g.displayName) })
                        }
                    }
                    com.pianoquest.app.ui.components.ScrollHintRow(Modifier.fillMaxWidth(), spacing = 6.dp) {
                        FilterChip(SongsUi.level == null, { SongsUi.level = null }, { Text("Any level") })
                        Song.DIFFICULTY_LABELS.forEachIndexed { i, l ->
                            FilterChip(SongsUi.level == i + 1, { SongsUi.level = if (SongsUi.level == i + 1) null else i + 1 }, { Text(l) })
                        }
                    }
                    SongsUi.genre?.let { Text(it.blurb, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
                }
            }
            items(songs, key = { it.id }) { s ->
                SongCard(s, p.songStars[s.id] ?: 0, p.songBestRight[s.id], p.songBestLeft[s.id], favorite = s.id in p.favoriteSongs,
                    onFavorite = { ProgressStore.toggleFavorite(s.id) },
                    onRemove = if (s.importedFile != null) ({ ImportedSongs.remove(s.id) }) else null) { nav.openSong(s.id) }
            }
            if (songs.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Text(when (SongsUi.tab) {
                    SongTab.FAVORITES -> if (p.favoriteSongs.isEmpty()) "No favourites yet - tap the ♡ on any song to add it here." else "No favourites match your filters."
                    SongTab.MINE -> if (mine.isEmpty()) "No saved MIDI files yet. Tap \"Upload MIDI file\" to add a .mid file from your device." else "No songs match your filters."
                    SongTab.ALL -> "No songs match your filters."
                }, Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge)
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(16.dp)) }
        }
    }
    pending?.let { pi ->
        var title by remember(pi) { mutableStateOf(pi.title) }
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Save this MIDI file?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(pi.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(title, { title = it.take(60) }, label = { Text("Song name") }, singleLine = true)
                    Text("Saved songs appear under My songs and stay on this device.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    ImportedSongs.import(pi.fileName, pi.bytes, title, save = true)
                        .onSuccess { SongsUi.tab = SongTab.MINE; SongsUi.genre = null; SongsUi.level = null; pending = null; nav.openSong(it.id) }
                        .onFailure { message = it.message; pending = null }
                }) { Text("Save to My songs") }
            },
            dismissButton = {
                TextButton(onClick = {
                    ImportedSongs.import(pi.fileName, pi.bytes, title, save = false)
                        .onSuccess { pending = null; nav.openSong(it.id) }
                        .onFailure { message = it.message; pending = null }
                }) { Text("Play without saving") }
            },
        )
    }
}

@Composable
private fun TabButton(tab: SongTab, icon: ImageVector, count: Int) {
    val on = SongsUi.tab == tab
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.clip(shape).background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { SongsUi.tab = tab; SongsUi.firstItem = 0 }.padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val fg = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        Icon(icon, null, tint = if (tab == SongTab.FAVORITES && !on) Color(0xFFC0463E) else fg, modifier = Modifier.size(18.dp))
        Text("${tab.label}  $count", color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SongCard(s: Song, stars: Int, bestRight: Int?, bestLeft: Int?, favorite: Boolean, onFavorite: () -> Unit, onRemove: (() -> Unit)? = null,
                     onClick: () -> Unit) {
    val color = Color(genreColor(s.genre))
    var confirm by remember { mutableStateOf(false) }
    val pq = com.pianoquest.app.ui.theme.PQ.colors
    Card(Modifier.fillMaxWidth(), onClick = onClick) {
        // Wide cards: the two hand bars sit beside the title. Narrow cards (two per row on tablets): below it.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val inline = maxWidth > 640.dp
            Column(Modifier.padding(start = 12.dp, top = 8.dp, bottom = if (inline) 8.dp else 12.dp, end = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(color), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MusicNote, null, tint = Color.White)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(s.composer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(5) { i -> Box(Modifier.size(7.dp).clip(CircleShape).background(if (i < s.difficulty) color else MaterialTheme.colorScheme.outlineVariant)) }
                            Text("${s.genre.displayName} · ${formatDuration(s.durationSeconds)}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    // All-time best accuracy for each hand.
                    if (inline) Column(Modifier.width(210.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        HandBest("Right hand", bestRight, pq.keyRight)
                        HandBest("Left hand", bestLeft, pq.keyLeft)
                    }
                    Stars(stars, size = 14.dp)
                    if (onRemove != null) IconButton(onClick = { confirm = true }) { Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = onFavorite) {
                        Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (favorite) "Remove from favourites" else "Add to favourites",
                            tint = if (favorite) Color(0xFFC0463E) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!inline) Row(Modifier.padding(end = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.weight(1f)) { HandBest("Right hand", bestRight, pq.keyRight) }
                    Box(Modifier.weight(1f)) { HandBest("Left hand", bestLeft, pq.keyLeft) }
                }
            }
        }
    }
    if (confirm && onRemove != null) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("Remove \"${s.title}\"?") },
        text = { Text("The MIDI file is deleted from My songs on this device.") },
        confirmButton = { TextButton(onClick = { confirm = false; onRemove() }) { Text("Remove") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } },
    )
}

@Composable
private fun HandBest(label: String, best: Int?, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
            Text(best?.let { "$it%" } ?: "–", style = MaterialTheme.typography.labelMedium, maxLines = 1,
                color = if (best == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        }
        com.pianoquest.app.ui.components.LinearMeter((best ?: 0) / 100f, Modifier.fillMaxWidth(), color = color, height = 7.dp)
    }
}

fun formatDuration(seconds: Double): String {
    val s = seconds.toInt()
    return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
}
