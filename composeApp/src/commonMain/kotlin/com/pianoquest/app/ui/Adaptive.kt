package com.pianoquest.app.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.pianoquest.app.data.Settings
import com.pianoquest.app.platform.platformAutoScale

enum class FormFactor { PHONE, TABLET }

/** Phone or tablet - decided from the real screen, so layouts can use a tablet's extra room even after scaling. */
val LocalFormFactor = compositionLocalOf { FormFactor.PHONE }

/** Accessibility preferences that components read directly (key labels, haptics, motion, beat flash). */
@Immutable
data class A11y(
    val largeKeyLabels: Boolean = false,
    val haptics: Boolean = false,
    val reduceMotion: Boolean = false,
    val visualMetronome: Boolean = false,
)

val LocalA11y = staticCompositionLocalOf { A11y() }

fun Settings.a11y() = A11y(largeKeyLabels = largeKeyLabels, haptics = hapticKeys, reduceMotion = reduceMotion, visualMetronome = visualMetronome)

object UiScale {
    /** Reference phone in landscape: everything is designed for 892 x 412 dp. */
    private const val REF_LONG = 892f
    private const val REF_SHORT = 412f
    const val MAX_AUTO = 1.35f

    /**
     * Scale for a screen of [widthDp] x [heightDp]: larger screens scale the whole interface up in proportion,
     * so a tablet shows the same well-tested layouts with bigger text, icons and touch targets.
     */
    fun auto(widthDp: Float, heightDp: Float): Float {
        val long = maxOf(widthDp, heightDp)
        val short = minOf(widthDp, heightDp)
        return minOf(short / REF_SHORT, long / REF_LONG).coerceIn(1f, MAX_AUTO)
    }

    /** Tablets have a smallest width of at least 600 dp. */
    fun formFactor(widthDp: Float, heightDp: Float) = if (minOf(widthDp, heightDp) >= 600f) FormFactor.TABLET else FormFactor.PHONE
}

/**
 * Applies the device scale (when the platform has not already done it), the user's display-size and text-size
 * choices, and provides the form factor and accessibility preferences to everything below.
 */
@Composable
fun AdaptiveRoot(settings: Settings, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val base = LocalDensity.current
        val platform = platformAutoScale()
        // Size of the screen before any scaling, for the phone / tablet decision.
        val rawW = maxWidth.value * (platform?.first ?: 1f)
        val rawH = maxHeight.value * (platform?.first ?: 1f)
        val form = platform?.second ?: UiScale.formFactor(rawW, rawH)
        val auto = if (platform != null) 1f else UiScale.auto(maxWidth.value, maxHeight.value)
        val display = settings.displayScale.coerceIn(0.85f, 1.3f)
        val text = settings.textScale.coerceIn(0.85f, 1.6f)
        CompositionLocalProvider(
            LocalDensity provides Density(base.density * auto * display, base.fontScale * text),
            LocalFormFactor provides form,
            LocalA11y provides settings.a11y(),
        ) { content() }
    }
}
