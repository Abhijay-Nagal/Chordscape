package com.pianoquest.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.AppInfo
import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.generated.resources.keyscape_logo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** Cream behind the logo (matches the Android launch window, so the hand-over is seamless). */
val LogoCream = Color(0xFFFCF4E5)
private val LogoInk = Color(0xFF3A1C10)

/** The official Keyscape logo tile. */
@Composable
fun KeyscapeLogo(modifier: Modifier = Modifier) {
    Image(painterResource(Res.drawable.keyscape_logo), AppInfo.NAME, modifier)
}

/**
 * Launch sequence: the logo appears on its own, then the tagline fades in beneath it, then the app opens.
 * Tapping anywhere skips ahead.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val scale = remember { Animatable(0.86f) }
    val alpha = remember { Animatable(0f) }
    var showIntro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(450)) }
        scale.animateTo(1f, tween(700))
        delay(500)
        showIntro = true
        delay(3600)
        onDone()
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(LogoCream).pointerInput(Unit) { detectTapGestures { onDone() } },
        contentAlignment = Alignment.Center,
    ) {
        val logo = minOf(maxHeight * 0.52f, maxWidth * 0.5f, 260.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            KeyscapeLogo(
                Modifier.size(logo).scale(scale.value).alpha(alpha.value)
                    .shadow(10.dp, RoundedCornerShape(logo * 0.24f), clip = false)
            )
            // The tagline is centred under the logo and exactly as wide as it.
            Box(Modifier.width(logo).height(logo * 0.16f), contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(showIntro, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }) {
                    Tagline(logo, LogoInk)
                }
            }
        }
    }
}

const val TAGLINE = "Play Beyond the Ordinary"

/**
 * The tagline on one line, centred, its visible lettering exactly [width] wide - used under the logo.
 * Sized from the glyphs' ink bounds (not the text advance), so the first and last letters line up with the logo edges.
 */
@Composable
fun Tagline(width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val base = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic, fontSize = 20.sp, color = color)
    val target = with(density) { width.toPx() }
    val layout = remember(base, density, target) {
        val ink = measurer.measure(TAGLINE, base, softWrap = false).getPathForRange(0, TAGLINE.length).getBounds().width
        val size = if (ink > 0f) 20f * target / ink else 20f
        measurer.measure(TAGLINE, base.copy(fontSize = size.sp), softWrap = false, maxLines = 1)
    }
    val inkBounds = remember(layout) { layout.getPathForRange(0, TAGLINE.length).getBounds() }
    val height = with(density) { layout.size.height.toDp() }
    androidx.compose.foundation.Canvas(modifier.width(width).height(height)) {
        // Shift so the ink (not the advance box) starts at the left edge.
        drawText(layout, topLeft = androidx.compose.ui.geometry.Offset(-inkBounds.left, 0f))
    }
}
