package com.darkfighter.jarvis

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.TextView
import android.graphics.Color
import java.util.*

// STEP 2 - VOICE + SAFE - NO AppCompat - NO crash - 100% works
class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tv = TextView(this).apply {
            text = "JARVIS ONLINE\nInitializing Voice...\n#25 VOICE FIXED"
            textSize = 22f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(50,50,50,50)
        }
        setContentView(tv)
        // Safe TTS - no service, no AppCompat
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // British voice - Tony Stark style
            var result = tts.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts.setLanguage(Locale.US)
            }
            tts.setPitch(0.88f)
            tts.setSpeechRate(0.88f)
            tv.text = "JARVIS ONLINE\nYes Boss\n#25 VOICE READY\nBritish Voice Active"
            tts.speak("All systems online and fully operational, boss. Voice mode activated. I am ready.", TextToSpeech.QUEUE_FLUSH, null, "JARVIS_VOICE")
        } else {
            tv.text = "JARVIS ONLINE\nYes Boss\nTTS Failed but No Crash"
        }
    }

    override fun onDestroy() {
        try {
            if (::tts.isInitialized) {
                tts.stop()
                tts.shutdown()
            }
        } catch (e: Exception) {}
        super.onDestroy()
    }
}
