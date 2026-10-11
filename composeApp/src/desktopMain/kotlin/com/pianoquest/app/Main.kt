package com.pianoquest.app

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.generated.resources.keyscape_logo
import org.jetbrains.compose.resources.painterResource
import com.pianoquest.app.input.PianoInput

fun main() {
    Audio.start()
    application {
        val state = rememberWindowState(width = 1280.dp, height = 820.dp, position = WindowPosition.PlatformDefault)
        Window(
            onCloseRequest = {
                PianoInput.releaseAll()
                Audio.pause()
                exitApplication()
            },
            title = "Keyscape - learn piano from your first note to the concert hall",
            state = state,
            icon = painterResource(Res.drawable.keyscape_logo),
        ) {
            window.minimumSize = java.awt.Dimension(800, 560)
            App()
        }
    }
}
