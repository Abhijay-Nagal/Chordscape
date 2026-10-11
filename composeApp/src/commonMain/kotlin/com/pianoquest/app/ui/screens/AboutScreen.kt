package com.pianoquest.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pianoquest.app.AppInfo
import com.pianoquest.app.data.SongLibrary
import com.pianoquest.app.ui.LocalNavigator
import com.pianoquest.app.ui.components.AppEmblem
import com.pianoquest.app.ui.components.Card
import com.pianoquest.app.ui.components.LessonImages
import com.pianoquest.app.ui.components.RichText
import com.pianoquest.app.ui.components.ScreenHeader
import com.pianoquest.app.ui.components.SectionTitle

@Composable
fun AboutScreen() {
    val nav = LocalNavigator.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("About, privacy & licences", onBack = { nav.back() })
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KeyscapeLogo(Modifier.size(72.dp))
                    Column {
                        Text(AppInfo.NAME, style = MaterialTheme.typography.headlineSmall)
                        Text("Version ${AppInfo.VERSION}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                SectionTitle("Privacy")
                Card(Modifier.fillMaxWidth()) {
                    RichText(PRIVACY_SUMMARY, Modifier.padding(16.dp))
                }
                SectionTitle("Piano sound")
                Card(Modifier.fillMaxWidth()) {
                    RichText("""
                        The Grand Piano uses the **Salamander Grand Piano V3** samples (Yamaha C5) by **Alexander Holm**, licensed under
                        **Creative Commons Attribution 3.0** (creativecommons.org/licenses/by/3.0). The samples were resampled to 32 kHz mono and trimmed.
                        All other instruments are synthesised by the app.
                    """.trimIndent(), Modifier.padding(16.dp))
                }
                SectionTitle("Music scores")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        RichText("""
                            The complete classical pieces come from the **Mutopia Project** (mutopiaproject.org), a library of free sheet music typeset by volunteers.
                            Each piece is in the public domain or licensed under Creative Commons, as listed below. The Easy and Medium versions are
                            simplified adaptations made by this app; adaptations of Creative Commons ShareAlike scores are shared under the same licence.
                        """.trimIndent())
                        SongLibrary.all.filter { it.sourceUrl != null }.forEach { s ->
                            Text("• ${s.title} - ${s.composer}. ${s.source.substringAfter("(").removeSuffix(")")}. ${s.sourceUrl}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RichText("""
                            Folk, traditional and holiday melodies are in the public domain. Pieces marked "Keyscape original" were written for this app.
                            Entries for modern popular songs contain only chord progressions (which are not protected by copyright); no melodies or lyrics are included.
                        """.trimIndent())
                    }
                }
                SectionTitle("Pictures")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Portraits and instrument photographs from Wikimedia Commons, all in the public domain or CC0:", style = MaterialTheme.typography.bodyMedium)
                        LessonImages.credits.forEach { (_, credit, url) ->
                            Text("• $credit - $url", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                SectionTitle("Fonts & software")
                Card(Modifier.fillMaxWidth()) {
                    RichText("""
                        - **Bravura** music font by Steinberg Media Technologies - SIL Open Font License 1.1
                        - **Libre Baskerville** by Pablo Impallari & Rodrigo Fuenzalida - SIL Open Font License 1.1
                        - **Kotlin**, **Compose Multiplatform**, **kotlinx.coroutines**, **kotlinx.serialization** (JetBrains) and **AndroidX** (Google) - Apache License 2.0
                    """.trimIndent(), Modifier.padding(16.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

const val PRIVACY_SUMMARY = """
**${AppInfo.NAME} does not collect, share or sell any personal data.**

- No account is needed and there is no sign-in.
- The app has no internet permission: nothing you do leaves your device.
- No advertising, no analytics and no tracking.
- Your name (optional), lesson progress, scores and settings are stored only on your device, in the app's private storage. Uninstalling the app or using "Reset all progress" deletes them.
- MIDI files you import are copied into the app's private storage and never uploaded.
- MIDI keyboard input is processed in real time on the device and is not recorded unless you press Record (recordings stay in memory and are discarded when you close the app).
"""
