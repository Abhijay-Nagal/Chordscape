package com.pianoquest.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import com.pianoquest.app.ui.FormFactor
import com.pianoquest.app.ui.UiScale
import kotlin.math.roundToInt
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.pianoquest.app.audio.Audio
import com.pianoquest.app.input.PianoInput
import com.pianoquest.app.platform.AndroidPlatform
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    private val openMidi = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> AndroidPlatform.onPicked(uri) }

    /**
     * Tablets and other large screens: raise the activity's display density so the whole interface (including dialogs
     * and menus) is drawn in proportion to the screen instead of as a small phone layout in a big window.
     */
    override fun attachBaseContext(newBase: Context) {
        val cfg = newBase.resources.configuration
        val scale = UiScale.auto(cfg.screenWidthDp.toFloat(), cfg.screenHeightDp.toFloat())
        val form = if (cfg.smallestScreenWidthDp >= 600) FormFactor.TABLET else FormFactor.PHONE
        AndroidPlatform.uiScale = scale to form
        if (scale > 1.01f && cfg.densityDpi > 0) {
            val scaled = Configuration(cfg).apply { densityDpi = (cfg.densityDpi * scale).roundToInt() }
            super.attachBaseContext(newBase.createConfigurationContext(scaled))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidPlatform.appContext = applicationContext
        AndroidPlatform.activity = WeakReference(this)
        AndroidPlatform.midiPicker = openMidi
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent { App() }
    }

    override fun onResume() {
        super.onResume()
        AndroidPlatform.activity = WeakReference(this)
        Audio.start()
        com.pianoquest.app.data.Today.refresh()
    }

    override fun onPause() {
        PianoInput.releaseAll()
        Audio.pause()
        super.onPause()
    }
}
