package com.pianoquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Settings
import com.pianoquest.app.platform.isDesktop
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.Card
import com.pianoquest.app.ui.components.ScreenHeader
import com.pianoquest.app.ui.components.SectionTitle
import com.pianoquest.app.ui.theme.PQ
import kotlin.math.abs

private val TEXT_SIZES = listOf(0.9f to "Small", 1f to "Default", 1.15f to "Large", 1.3f to "Larger", 1.5f to "Largest")
private val DISPLAY_SIZES = listOf(0.9f to "Smaller", 1f to "Default", 1.12f to "Larger", 1.25f to "Largest")

/** Accessibility options with a one-tap reset to the defaults. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccessibilityScreen() {
    val nav = LocalNavigator.current
    val p by ProgressStore.state.collectAsState()
    val s = p.settings
    var confirmReset by remember { mutableStateOf(false) }
    fun set(f: (Settings) -> Settings) = ProgressStore.updateSettings(f)
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Accessibility", onBack = { nav.back() }, subtitle = "Make Keyscape easier to see, read and use") {
            OutlinedButton(onClick = { confirmReset = true }, enabled = !s.accessibilityIsDefault, modifier = Modifier.padding(end = 8.dp)) {
                Icon(Icons.Default.RestartAlt, null, Modifier.size(18.dp)); Text(" Reset to defaults")
            }
        }
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 820.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle("Size")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Text size", style = MaterialTheme.typography.titleSmall)
                        Text("On top of your device's font size.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TEXT_SIZES.forEach { (v, n) -> FilterChip(abs(s.textScale - v) < 0.01f, { set { it.copy(textScale = v) } }, { Text(n) }) }
                        }
                        Text("Display size", style = MaterialTheme.typography.titleSmall)
                        Text("Makes everything bigger or smaller - text, buttons, icons and the piano keys.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DISPLAY_SIZES.forEach { (v, n) -> FilterChip(abs(s.displayScale - v) < 0.01f, { set { it.copy(displayScale = v) } }, { Text(n) }) }
                        }
                        SettingSwitch("Bold text", "Heavier letters are easier to read", s.boldText) { v -> set { it.copy(boldText = v) } }
                        SettingSwitch("Large note names on keys", "Bigger letters on the piano keys", s.largeKeyLabels) { v -> set { it.copy(largeKeyLabels = v) } }
                    }
                }

                SectionTitle("Colour and contrast")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingSwitch("High contrast", "Darker text, stronger outlines and plainer backgrounds", s.highContrast) { v -> set { it.copy(highContrast = v) } }
                        SettingSwitch("Colour-blind friendly colours", "Hands, right and wrong notes use colours that stay distinct with every type of colour blindness",
                            s.colorBlindSafe) { v -> set { it.copy(colorBlindSafe = v) } }
                        val c = PQ.colors
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Swatch(c.keyRight, "Right hand"); Swatch(c.keyLeft, "Left hand"); Swatch(c.success, "Correct"); Swatch(c.error, "Wrong")
                        }
                        Text("Theme (light / dark) is in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                SectionTitle("Motion, sound and touch")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingSwitch("Reduce motion", "Turns off screen transitions and decorative animations", s.reduceMotion) { v -> set { it.copy(reduceMotion = v) } }
                        SettingSwitch("Flash on the beat", "The screen edge flashes with every metronome click - helpful if you can't hear the click well",
                            s.visualMetronome) { v -> set { it.copy(visualMetronome = v) } }
                        if (!isDesktop) SettingSwitch("Vibrate on key press", "A light tap when you touch an on-screen piano key", s.hapticKeys) { v -> set { it.copy(hapticKeys = v) } }
                        SettingSwitch("Keep the screen on", "The display stays awake while Keyscape is open", s.keepScreenOn) { v -> set { it.copy(keepScreenOn = v) } }
                    }
                }
                Text("Keyscape also follows your device's own font size, and works with screen readers such as TalkBack.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("Reset accessibility settings?") },
        text = { Text("Text size, display size, contrast, colours, motion, vibration and screen options go back to their defaults. Nothing else changes.") },
        confirmButton = { TextButton(onClick = { set { it.withDefaultAccessibility() }; confirmReset = false }) { Text("Reset") } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
    )
}

@Composable
private fun Swatch(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
