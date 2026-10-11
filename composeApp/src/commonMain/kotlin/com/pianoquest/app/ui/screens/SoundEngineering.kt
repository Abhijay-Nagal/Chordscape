package com.pianoquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.data.ProgressStore
import com.pianoquest.app.data.Settings
import com.pianoquest.app.ui.components.FitToHeight
import kotlin.math.roundToInt

/** Advanced sound engineering for the piano stage: pitch, tone, effects and envelope - in one landscape panel. */
@Composable
fun AdvancedSoundDialog(onClose: () -> Unit) {
    val s = ProgressStore.state.collectAsState().value.settings
    fun set(f: (Settings) -> Settings) = ProgressStore.updateSettings(f)
    var bend by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) { onDispose { Audio.setPitchBend(0f) } }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(0.94f).padding(vertical = 12.dp)) {
            FitToHeight(Modifier.padding(16.dp), minScale = 0.55f) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sound engineering", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { set { it.withoutFx() } }) { Text("Reset all") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onClose) { Text("Done") }
                    }
                    Text("These settings shape the sound of Free Play and the song player. Lessons always use a clean grand piano.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f)) {
                            Section("Pitch")
                            FxSlider("Pitch bend (springs back)", "${if (bend > 0) "+" else ""}${"%.1f".fmt(bend)} st", bend, -2f..2f,
                                onFinished = { bend = 0f; Audio.setPitchBend(0f) }) { bend = it; Audio.setPitchBend(it) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Transpose", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = { set { it.copy(fxTranspose = (it.fxTranspose - 1).coerceAtLeast(-12)) } }) { Text("−") }
                                Text("${if (s.fxTranspose > 0) "+" else ""}${s.fxTranspose} semitones", fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { set { it.copy(fxTranspose = (it.fxTranspose + 1).coerceAtMost(12)) } }) { Text("+") }
                            }
                            FxSlider("Fine tuning", "${s.fxTuneCents.roundToInt()} cents", s.fxTuneCents, -50f..50f) { v -> set { it.copy(fxTuneCents = v) } }
                            Section("Tone (EQ)")
                            FxSlider("Bass", db(s.fxBassDb), s.fxBassDb, -12f..12f) { v -> set { it.copy(fxBassDb = v) } }
                            FxSlider("Treble", db(s.fxTrebleDb), s.fxTrebleDb, -12f..12f) { v -> set { it.copy(fxTrebleDb = v) } }
                        }
                        Column(Modifier.weight(1f)) {
                            Section("Effects")
                            FxSlider("Distortion / drive", pct(s.fxDrive), s.fxDrive, 0f..1f) { v -> set { it.copy(fxDrive = v) } }
                            FxSlider("Chorus", pct(s.fxChorus), s.fxChorus, 0f..1f) { v -> set { it.copy(fxChorus = v) } }
                            FxSlider("Tremolo depth", pct(s.fxTremoloDepth), s.fxTremoloDepth, 0f..1f) { v -> set { it.copy(fxTremoloDepth = v) } }
                            FxSlider("Tremolo speed", "${"%.1f".fmt(s.fxTremoloRate)} Hz", s.fxTremoloRate, 1f..12f) { v -> set { it.copy(fxTremoloRate = v) } }
                            Section("Envelope & space")
                            FxSlider("Release time", "×${"%.1f".fmt(s.fxReleaseScale)}", s.fxReleaseScale, 0.25f..4f) { v -> set { it.copy(fxReleaseScale = v) } }
                            FxSlider("Pedal sustain", pct(s.fxSustainLevel), s.fxSustainLevel, 0f..1f) { v -> set { it.copy(fxSustainLevel = v) } }
                            FxSlider("Room size", pct(s.fxRoomSize), s.fxRoomSize, 0f..1f) { v -> set { it.copy(fxRoomSize = v) } }
                        }
                    }
                }
            }
        }
    }
}

private fun String.fmt(v: Float): String {
    val r = (v * 10).roundToInt() / 10f
    return if (r == r.toInt().toFloat()) "${r.toInt()}.0" else "$r"
}

private fun db(v: Float) = "${if (v > 0) "+" else ""}${v.roundToInt()} dB"
private fun pct(v: Float) = "${(v * 100).roundToInt()}%"

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun ColumnScope.FxSlider(label: String, value: String, v: Float, range: ClosedFloatingPointRange<Float>,
                                 onFinished: (() -> Unit)? = null, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Slider(v, onChange, valueRange = range, onValueChangeFinished = onFinished, modifier = Modifier.height(30.dp))
}
