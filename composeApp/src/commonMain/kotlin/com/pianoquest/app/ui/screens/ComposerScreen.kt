package com.pianoquest.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.data.Composer
import com.pianoquest.app.data.Composers
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.FitToHeight
import com.pianoquest.app.ui.components.LessonImages
import com.pianoquest.app.ui.components.Stars
import com.pianoquest.app.ui.theme.Wood
import com.pianoquest.app.ui.theme.woodGrain
import org.jetbrains.compose.resources.painterResource

/** A composer's portrait, or a brass monogram medallion when no authentic portrait exists. */
@Composable
fun ComposerPortrait(c: Composer, modifier: Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)) {
    val res = c.portrait?.let { LessonImages.resource(it) }
    if (res != null) {
        Image(painterResource(res), c.fullName, modifier.clip(shape), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
    } else {
        Box(modifier.clip(shape).woodGrain(Wood.Mahogany, seed = c.id.length), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize(0.7f).aspectRatio(1f)) {
                drawCircle(Brush.radialGradient(listOf(Color(0xFFE9C27A), Wood.Brass, Color(0xFF7A5420))), size.minDimension / 2)
                drawCircle(Wood.Walnut, size.minDimension / 2 * 0.84f)
                drawCircle(Color(0x66E9C27A), size.minDimension / 2 * 0.84f, style = Stroke(3f))
            }
            Text(c.initials, color = Wood.Gold, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge)
        }
    }
}

private enum class ComposerTab { PIECES, STORY }

@Composable
fun ComposerScreen(composerId: String, openStory: Boolean = false, storyPage: Int = 0) {
    val nav = LocalNavigator.current
    val c = remember(composerId) { Composers.get(composerId) } ?: run { nav.back(); return }
    var tab by remember { mutableStateOf(if (openStory) ComposerTab.STORY else ComposerTab.PIECES) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight
        val sideW = minOf(maxWidth * 0.3f, 280.dp)
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                // Left: who they were.
                Column(Modifier.width(sideW).fillMaxHeight().woodGrain(Wood.Walnut, seed = 9).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Wood.Parchment) }
                        Text(c.era, color = Wood.Sand, style = MaterialTheme.typography.labelLarge)
                    }
                    // The whole portrait, scaled to the space available - never cropped.
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { FullPortrait(c) }
                    Text(c.fullName, color = Wood.Ivory, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                    Text("${c.years} · ${c.country}", color = Wood.Sand, style = MaterialTheme.typography.bodyMedium)
                    Text(c.tagline, color = Wood.Parchment, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, maxLines = 2)
                    TabSwitch(tab, vertical = true) { tab = it }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    when (tab) {
                        ComposerTab.PIECES -> PiecesList(c)
                        ComposerTab.STORY -> Storybook(c, storyPage)
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().woodGrain(Wood.Walnut, seed = 9).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Wood.Parchment) }
                    ComposerPortrait(c, Modifier.size(72.dp), CircleShape)
                    Column(Modifier.weight(1f)) {
                        Text(c.fullName, color = Wood.Ivory, style = MaterialTheme.typography.titleMedium)
                        Text("${c.years} · ${c.era}", color = Wood.Sand, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Box(Modifier.padding(8.dp)) { TabSwitch(tab) { tab = it } }
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        ComposerTab.PIECES -> PiecesList(c)
                        ComposerTab.STORY -> Storybook(c, storyPage)
                    }
                }
            }
        }
    }
}

/** The complete portrait at its own proportions, framed in brass - always the whole picture, never cropped. */
@Composable
private fun FullPortrait(c: Composer) {
    val res = c.portrait?.let { LessonImages.resource(it) }
    if (res == null) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val w = minOf(maxWidth, maxHeight * 0.8f)
            ComposerPortrait(c, Modifier.size(w, w / 0.8f))
        }
        return
    }
    FramedPicture(res, c.fullName, Modifier.fillMaxSize(), frame = Wood.Brass, border = true)
}

/**
 * A picture scaled to fit inside the space it is given at its own proportions (so it is never cut off), in a frame
 * that hugs the picture.
 */
@Composable
internal fun FramedPicture(res: org.jetbrains.compose.resources.DrawableResource, description: String, modifier: Modifier,
                           frame: Color = Wood.Walnut, border: Boolean = false) {
    val painter = painterResource(res)
    val ratio = painter.intrinsicSize.let { if (it.height > 0f && it.width > 0f) it.width / it.height else 0.8f }
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val w = minOf(maxWidth, maxHeight * ratio)
        val h = w / ratio
        val shape = RoundedCornerShape(10.dp)
        Surface(Modifier.size(w, h), shape = shape, color = frame, shadowElevation = 4.dp) {
            Image(painter, description, Modifier.fillMaxSize().then(if (border) Modifier.border(3.dp, Wood.Brass, shape) else Modifier)
                .padding(if (border) 4.dp else 6.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
private fun TabSwitch(tab: ComposerTab, vertical: Boolean = false, onTab: (ComposerTab) -> Unit) {
    val items = listOf(ComposerTab.PIECES to "Pieces", ComposerTab.STORY to "Storybook")
    val button = @Composable { t: ComposerTab, label: String, m: Modifier ->
        val on = t == tab
        Row(m.clip(RoundedCornerShape(10.dp)).background(if (on) Wood.Gold else Color.Transparent)
            .clickable { onTab(t) }.padding(vertical = 8.dp, horizontal = 6.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(if (t == ComposerTab.PIECES) Icons.Default.LibraryMusic else Icons.AutoMirrored.Filled.MenuBook, null,
                tint = if (on) Wood.Walnut else Wood.Parchment, modifier = Modifier.size(18.dp))
            Text(" $label", color = if (on) Wood.Walnut else Wood.Parchment, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
    val bg = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.25f)).padding(3.dp)
    if (vertical) Column(bg, verticalArrangement = Arrangement.spacedBy(3.dp)) { items.forEach { (t, l) -> button(t, l, Modifier.fillMaxWidth()) } }
    else Row(bg) { items.forEach { (t, l) -> button(t, l, Modifier.weight(1f)) } }
}

@Composable
private fun PiecesList(c: Composer) {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val favourites = p.favoriteSongs
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("${c.songs.size} ${if (c.songs.size == 1) "piece" else "pieces"} by ${c.name} - most famous first",
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        }
        items(c.songs, key = { it.id }) { s ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable { nav.openSong(s.id) }) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary) }
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${s.difficultyLabel} · ${formatDuration(s.durationSeconds)}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Stars(p.songStars[s.id] ?: 0, size = 14.dp)
                    val fav = s.id in favourites
                    IconButton(onClick = { ProgressStore.toggleFavorite(s.id) }) {
                        Icon(if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (fav) "Remove from favourites" else "Add to favourites",
                            tint = if (fav) Color(0xFFC0463E) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.PlayArrow, "Play", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** An illustrated, page-turning storybook of the composer's life, ending with a short quiz. */
@Composable
private fun Storybook(c: Composer, startPage: Int = 0) {
    val nav = LocalNavigator.current
    var page by remember(c.id) { mutableIntStateOf(startPage.coerceIn(0, c.story.size + 1)) }
    val pages = c.story.size + 2 // timeline + story pages + quiz
    var drag by remember { mutableStateOf(0f) }
    Column(Modifier.fillMaxSize().background(Color(0xFFF7EEDC)).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth().pointerInput(pages) {
            detectHorizontalDragGestures(onDragEnd = {
                if (drag < -60 && page < pages - 1) page++ else if (drag > 60 && page > 0) page--
                drag = 0f
            }) { _, d -> drag += d }
        }) {
            AnimatedContent(page, transitionSpec = {
                if (targetState > initialState) (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                else (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
            }, modifier = Modifier.fillMaxSize()) { pg ->
                when {
                    pg == 0 -> TimelinePage(c) { page = it + 1 }
                    pg <= c.story.size -> StoryPageView(c, pg - 1) { id -> nav.openSong(id) }
                    else -> QuizPage(c)
                }
            }
        }
        // Page turner
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (page > 0) page-- }, enabled = page > 0) { Icon(Icons.Default.ChevronLeft, null); Text("Back") }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                repeat(pages) { i ->
                    Box(Modifier.padding(3.dp).size(if (i == page) 10.dp else 7.dp).clip(CircleShape)
                        .background(if (i == page) Wood.Chestnut else Wood.Tan.copy(alpha = 0.6f)).clickable { page = i })
                }
            }
            TextButton(onClick = { if (page < pages - 1) page++ }, enabled = page < pages - 1) { Text("Next"); Icon(Icons.Default.ChevronRight, null) }
        }
    }
}

private val InkBrown = Color(0xFF2A170D)

@Composable
private fun TimelinePage(c: Composer, onOpen: (Int) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("The life of ${c.fullName}", color = InkBrown, style = MaterialTheme.typography.headlineSmall)
        Text("${c.years}  ·  ${c.country}  ·  ${c.era}", color = Wood.Chestnut, style = MaterialTheme.typography.titleSmall)
        Text("Tap a chapter on the timeline, or turn the pages.", color = Wood.Caramel, style = MaterialTheme.typography.bodySmall)
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val n = c.story.size
            val w = maxWidth
            val h = maxHeight
            // Picture of each chapter above the line when there is room for it (tablets, large phones).
            val thumbs = h > 260.dp
            val lineY = if (thumbs) 0.66f else 0.45f
            if (thumbs) c.story.forEachIndexed { i, sp ->
                val res = sp.image?.let { LessonImages.resource(it) } ?: c.portrait?.let { LessonImages.resource(it) }
                if (res != null) FramedPicture(res, sp.title, Modifier.align(Alignment.TopStart).offsetX(w * (i.toFloat() / n) + 6.dp)
                    .padding(top = 30.dp).size(w / n - 12.dp, h * lineY - 48.dp).clickable { onOpen(i) })
            }
            // A drawn timeline with one station per chapter.
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height * lineY
                drawLine(Wood.Caramel, Offset(20f, y), Offset(size.width - 20f, y), strokeWidth = 6f)
                for (i in 0 until n) {
                    val x = 20f + (size.width - 40f) * (i + 0.5f) / n
                    drawCircle(Wood.Brass, 16f, Offset(x, y))
                    drawCircle(Wood.Ivory, 8f, Offset(x, y))
                }
            }
            c.story.forEachIndexed { i, sp ->
                val cx = w * ((i + 0.5f) / n)
                Column(Modifier.align(Alignment.TopStart).offsetX(cx - w / n / 2).width(w / n).padding(horizontal = 3.dp).clip(RoundedCornerShape(10.dp)).clickable { onOpen(i) }.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(sp.year, color = Wood.Chestnut, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 1)
                    Spacer(Modifier.height(h * lineY - 12.dp))
                    Text(sp.title, color = InkBrown, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 3)
                }
            }
        }
    }
}

private fun Modifier.offsetX(x: Dp) = this.offset(x = x)

@Composable
private fun StoryPageView(c: Composer, index: Int, onPlay: (String) -> Unit) {
    val sp = c.story[index]
    val res = sp.image?.let { LessonImages.resource(it) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight * 1.1f
        val pictureH = maxHeight * 0.38f
        val text = @Composable {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chapter ${index + 1}  ·  ${sp.year}", color = Wood.Chestnut, style = MaterialTheme.typography.labelLarge)
                Text(sp.title, color = InkBrown, style = MaterialTheme.typography.headlineSmall)
                // A decorative drop cap opens each chapter, like an old storybook.
                Row {
                    Text(sp.text.take(1), color = Wood.Brass, fontSize = 44.sp, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(end = 6.dp))
                    Text(sp.text.drop(1), color = InkBrown, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
                }
                val song = sp.songId?.let { SongLibrary.get(it) }
                if (song != null) FilledTonalButton(onClick = { onPlay(song.id) }) {
                    Icon(Icons.Default.PlayArrow, null); Text("  Hear it: ${song.title}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        val picture = @Composable { m: Modifier ->
            if (res != null) {
                FramedPicture(res, sp.title, m)
            } else {
                // Chapters without their own picture show the composer's portrait - whole, never cropped.
                val portrait = c.portrait?.let { LessonImages.resource(it) }
                if (portrait != null) FramedPicture(portrait, c.fullName, m)
                else BoxWithConstraints(m, contentAlignment = Alignment.Center) {
                    val w = minOf(maxWidth, maxHeight * 0.8f)
                    ComposerPortrait(c, Modifier.size(w, w / 0.8f), RoundedCornerShape(8.dp))
                }
            }
        }
        if (wide) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FitToHeight(Modifier.weight(1.3f).fillMaxHeight(), minScale = 0.7f) { text() }
                picture(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                picture(Modifier.fillMaxWidth().height(pictureH))
                FitToHeight(Modifier.fillMaxWidth().weight(1f), minScale = 0.7f) { text() }
            }
        }
    }
}

@Composable
private fun QuizPage(c: Composer) {
    val answers = remember(c.id) { mutableStateMapOf<Int, String>() }
    val shuffled = remember(c.id) { c.quiz.map { q -> q.options.shuffled(kotlin.random.Random(q.question.hashCode())) } }
    FitToHeight(Modifier.fillMaxSize(), minScale = 0.6f) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Quick quiz: ${c.name}", color = InkBrown, style = MaterialTheme.typography.headlineSmall)
            c.quiz.forEachIndexed { i, q ->
                Text("${i + 1}. ${q.question}", color = InkBrown, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    shuffled[i].forEach { opt ->
                        val chosen = answers[i]
                        val correct = opt == q.options.first()
                        val bg = when {
                            chosen == null -> Wood.Ivory
                            correct -> Color(0xFFCFE3BF)
                            chosen == opt -> Color(0xFFF2C9C2)
                            else -> Wood.Ivory
                        }
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(bg)
                            .border(1.dp, Wood.Tan, RoundedCornerShape(10.dp))
                            .clickable(enabled = chosen == null) { answers[i] = opt }.padding(8.dp), contentAlignment = Alignment.Center) {
                            Text(opt, color = InkBrown, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }
            }
            if (answers.size == c.quiz.size) {
                val score = c.quiz.indices.count { answers[it] == c.quiz[it].options.first() }
                Text(if (score == c.quiz.size) "Perfect - you know ${c.name}'s story!" else "$score / ${c.quiz.size} correct - read the story again and try once more.",
                    color = Wood.Chestnut, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { answers.clear() }) { Text("Try again") }
            }
        }
    }
}
