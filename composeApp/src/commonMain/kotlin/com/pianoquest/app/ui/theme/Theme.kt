package com.pianoquest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.generated.resources.librebaskerville
import com.pianoquest.app.generated.resources.librebaskerville_italic
import org.jetbrains.compose.resources.Font
import kotlin.math.sin
import kotlin.random.Random

// The palette: ebony, walnut, mahogany, chestnut, caramel, honey, sand, parchment and brass.
object Wood {
    val Ebony = Color(0xFF16100C)
    val Walnut = Color(0xFF2A170D)
    val Mahogany = Color(0xFF4A2416)
    val Chestnut = Color(0xFF6B3C21)
    val Caramel = Color(0xFFA26B3E)
    val Tan = Color(0xFFC99C6D)
    val Honey = Color(0xFFD4A876)
    val Sand = Color(0xFFDCC4A3)
    val Parchment = Color(0xFFF0DFC0)
    val Ivory = Color(0xFFFBF4E4)
    val Brass = Color(0xFFC08A3E)
    val Gold = Color(0xFFD9A84E)
    val Burgundy = Color(0xFF7A2E2A)
    val Olive = Color(0xFF55693A)
    val Felt = Color(0xFF7A1F1F)
}

@Immutable
data class PQColors(
    val success: Color,
    val error: Color,
    val warning: Color,
    val gold: Color,
    val keyWhite: Color,
    val keyWhitePressed: Color,
    val keyBlack: Color,
    val keyBlackPressed: Color,
    val keyTarget: Color,
    val keyDemo: Color,
    val keyRight: Color,
    val keyLeft: Color,
    val staffInk: Color,
    val cardTint: Color,
    val stage: Color,
    val isDark: Boolean,
)

val LocalPQColors = staticCompositionLocalOf { lightPQ }

private val lightPQ = PQColors(
    success = Color(0xFF4E7A3A), error = Color(0xFF9B2C2C), warning = Color(0xFFB7791F), gold = Wood.Gold,
    keyWhite = Color(0xFFFFFBF0), keyWhitePressed = Color(0xFFE6C493), keyBlack = Color(0xFF17110D), keyBlackPressed = Wood.Chestnut,
    keyTarget = Wood.Gold, keyDemo = Color(0xFFA9C48F), keyRight = Color(0xFFC8813A), keyLeft = Color(0xFF5E8C7A),
    staffInk = Wood.Walnut, cardTint = Color(0xFFF3E4C6), stage = Color(0xFF1C130D), isDark = false,
)

private val darkPQ = PQColors(
    success = Color(0xFF8DB36B), error = Color(0xFFD9675E), warning = Color(0xFFE0A84A), gold = Wood.Gold,
    keyWhite = Color(0xFFFBF4E4), keyWhitePressed = Color(0xFFD9B27E), keyBlack = Color(0xFF110C09), keyBlackPressed = Wood.Caramel,
    keyTarget = Wood.Gold, keyDemo = Color(0xFF9DBB84), keyRight = Color(0xFFD9954A), keyLeft = Color(0xFF6FA08C),
    staffInk = Wood.Parchment, cardTint = Color(0xFF2C1D12), stage = Color(0xFF140E0A), isDark = true,
)

private val LightScheme = lightColorScheme(
    primary = Wood.Chestnut,
    onPrimary = Wood.Ivory,
    primaryContainer = Color(0xFFEBD3AC),
    onPrimaryContainer = Wood.Walnut,
    secondary = Color(0xFF9A6A28),
    onSecondary = Wood.Ivory,
    secondaryContainer = Color(0xFFF2DDAE),
    onSecondaryContainer = Wood.Walnut,
    tertiary = Wood.Olive,
    onTertiary = Wood.Ivory,
    background = Color(0xFFF3E7CF),
    onBackground = Wood.Walnut,
    surface = Wood.Ivory,
    onSurface = Wood.Walnut,
    surfaceVariant = Color(0xFFE8D6B5),
    onSurfaceVariant = Color(0xFF6B4E36),
    surfaceContainerLowest = Color(0xFFFFFAF0),
    surfaceContainerLow = Color(0xFFF6EBD5),
    surfaceContainer = Color(0xFFF0E1C4),
    surfaceContainerHigh = Color(0xFFEAD8B7),
    surfaceContainerHighest = Color(0xFFE3CEA9),
    outline = Color(0xFFB08E66),
    outlineVariant = Color(0xFFDCC4A3),
    error = Color(0xFF9B2C2C),
    inverseSurface = Wood.Walnut,
    inverseOnSurface = Wood.Parchment,
)

private val DarkScheme = darkColorScheme(
    primary = Wood.Honey,
    onPrimary = Wood.Walnut,
    primaryContainer = Wood.Chestnut,
    onPrimaryContainer = Wood.Parchment,
    secondary = Wood.Gold,
    onSecondary = Wood.Walnut,
    secondaryContainer = Color(0xFF5A3D18),
    onSecondaryContainer = Wood.Parchment,
    tertiary = Color(0xFF9DBB84),
    background = Wood.Ebony,
    onBackground = Wood.Parchment,
    surface = Color(0xFF22160E),
    onSurface = Wood.Parchment,
    surfaceVariant = Color(0xFF3A2618),
    onSurfaceVariant = Color(0xFFC9AE8C),
    surfaceContainerLowest = Color(0xFF120C08),
    surfaceContainerLow = Color(0xFF1C130C),
    surfaceContainer = Color(0xFF2C1D12),
    surfaceContainerHigh = Color(0xFF3A2618),
    surfaceContainerHighest = Color(0xFF46301F),
    outline = Color(0xFF7A5B40),
    outlineVariant = Color(0xFF4A3322),
    error = Color(0xFFD9675E),
)

@OptIn(ExperimentalTextApi::class)
@Composable
private fun serifFamily(): FontFamily {
    val regular = Font(Res.font.librebaskerville, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400)))
    val bold = Font(Res.font.librebaskerville, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
    val semi = Font(Res.font.librebaskerville, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600)))
    val italic = Font(Res.font.librebaskerville_italic, FontWeight.Normal, FontStyle.Italic, variationSettings = FontVariation.Settings(FontVariation.weight(400)))
    return remember(regular, bold, semi, italic) { FontFamily(regular, semi, bold, italic) }
}

@Composable
private fun appTypography(): Typography {
    val serif = serifFamily()
    val t = Typography()
    return remember(serif) {
        t.copy(
            displayLarge = t.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
            displayMedium = t.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
            displaySmall = t.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
            headlineLarge = t.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
            headlineMedium = t.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
            headlineSmall = t.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
            titleLarge = t.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
            titleMedium = t.titleMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
            titleSmall = t.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = t.bodyLarge.copy(lineHeight = 26.sp),
            bodyMedium = t.bodyMedium.copy(lineHeight = 22.sp),
        )
    }
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

// High contrast: stronger text, outlines and backgrounds for low vision.
private val LightContrast = LightScheme.copy(
    primary = Wood.Mahogany, onSurfaceVariant = Wood.Walnut, outline = Color(0xFF6B4E36), outlineVariant = Color(0xFF8A6A4A),
    background = Color(0xFFFFFBF2), surface = Color.White, surfaceContainerLow = Color(0xFFFFF8EC), surfaceContainer = Color(0xFFF7EAD2),
    surfaceContainerHigh = Color(0xFFEFDDBC), secondary = Color(0xFF7A4E12),
)
private val DarkContrast = DarkScheme.copy(
    primary = Wood.Gold, onSurfaceVariant = Wood.Parchment, outline = Color(0xFFC9AE8C), outlineVariant = Color(0xFF9A7B5C),
    background = Color.Black, surface = Color(0xFF120C08), onBackground = Wood.Ivory, onSurface = Wood.Ivory,
)

/** Okabe-Ito colours: right hand blue, left hand orange, right answers bluish green, wrong answers reddish purple. */
private fun PQColors.colorBlindSafe() = copy(
    keyRight = if (isDark) Color(0xFF56B4E9) else Color(0xFF0072B2),
    keyLeft = Color(0xFFE69F00),
    success = if (isDark) Color(0xFF2EC4A0) else Color(0xFF00876A),
    error = if (isDark) Color(0xFFE08BC0) else Color(0xFF9E4F86),
    keyDemo = if (isDark) Color(0xFF7FC8E8) else Color(0xFF8FC1E3),
)

private fun Typography.bolder(): Typography {
    fun androidx.compose.ui.text.TextStyle.up(w: FontWeight) = copy(fontWeight = maxOf(fontWeight ?: FontWeight.Normal, w))
    return copy(
        bodyLarge = bodyLarge.up(FontWeight.SemiBold), bodyMedium = bodyMedium.up(FontWeight.SemiBold), bodySmall = bodySmall.up(FontWeight.SemiBold),
        labelLarge = labelLarge.up(FontWeight.Bold), labelMedium = labelMedium.up(FontWeight.Bold), labelSmall = labelSmall.up(FontWeight.Bold),
        titleSmall = titleSmall.up(FontWeight.Bold), titleMedium = titleMedium.up(FontWeight.Bold),
    )
}

@Composable
fun KeyscapeTheme(
    themeSetting: String = "system",
    boldText: Boolean = false,
    highContrast: Boolean = false,
    colorBlindSafe: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val pq = (if (dark) darkPQ else lightPQ).let { if (colorBlindSafe) it.colorBlindSafe() else it }
    val typography = appTypography().let { if (boldText) it.bolder() else it }
    CompositionLocalProvider(LocalPQColors provides pq) {
        MaterialTheme(
            colorScheme = when {
                dark && highContrast -> DarkContrast
                dark -> DarkScheme
                highContrast -> LightContrast
                else -> LightScheme
            },
            typography = typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object PQ {
    val colors: PQColors @Composable get() = LocalPQColors.current
}

/**
 * Paints a polished wood panel (gradient plus fine grain lines) behind the content.
 * The grain is drawn once into a bitmap per colour and size and shared, so screens that redraw or recompose every
 * frame only copy an image instead of re-rendering dozens of anti-aliased paths.
 */
fun Modifier.woodGrain(base: Color = Wood.Mahogany, seed: Int = 7, intensity: Float = 1f): Modifier = drawWithCache {
    val w = size.width.toInt()
    val h = size.height.toInt()
    if (w <= 0 || h <= 0) return@drawWithCache onDrawBehind { }
    val image = WoodCache.get(WoodKey(base, seed, intensity, w, h)) {
        ImageBitmap(w, h).also { img ->
            CanvasDrawScope().draw(this, layoutDirection, androidx.compose.ui.graphics.Canvas(img), Size(w.toFloat(), h.toFloat())) {
                paintWoodGrain(base, seed, intensity)
            }
        }
    }
    onDrawBehind { drawImage(image) }
}

private data class WoodKey(val base: Color, val seed: Int, val intensity: Float, val w: Int, val h: Int)

/** A small least-recently-used cache of rendered wood panels (UI thread only). */
private object WoodCache {
    private const val MAX = 32
    private val map = LinkedHashMap<WoodKey, ImageBitmap>()
    fun get(key: WoodKey, make: () -> ImageBitmap): ImageBitmap {
        map.remove(key)?.let { map[key] = it; return it }
        val img = make()
        map[key] = img
        if (map.size > MAX) map.remove(map.keys.first())
        return img
    }
}

private fun DrawScope.paintWoodGrain(base: Color, seed: Int, intensity: Float) {
    val light = lerp(base, Color(0xFFE8C9A0), 0.18f)
    val dark = lerp(base, Color.Black, 0.35f)
    drawRect(Brush.verticalGradient(listOf(light, base, dark)))
    val rnd = Random(seed)
    val lines = (size.height / 5f).toInt().coerceIn(6, 90)
    for (i in 0 until lines) {
        val y0 = rnd.nextFloat() * size.height
        val amp = 2f + rnd.nextFloat() * 6f
        val freq = 0.004f + rnd.nextFloat() * 0.01f
        val phase = rnd.nextFloat() * 6.28f
        val alpha = (0.04f + rnd.nextFloat() * 0.09f) * intensity
        val color = if (rnd.nextBoolean()) Color.Black.copy(alpha = alpha) else Color(0xFFFFE6C0).copy(alpha = alpha * 0.6f)
        val path = Path()
        var x = 0f
        path.moveTo(0f, y0)
        while (x <= size.width) {
            path.lineTo(x, y0 + amp * sin(x * freq + phase) + amp * 0.4f * sin(x * freq * 3.1f + phase * 2))
            x += 12f
        }
        drawPath(path, color, style = Stroke(width = 0.6f + rnd.nextFloat() * 1.6f))
    }
    // Soft sheen along the top edge.
    drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f * intensity), Color.Transparent), endY = size.height * 0.35f),
        topLeft = Offset.Zero)
}
