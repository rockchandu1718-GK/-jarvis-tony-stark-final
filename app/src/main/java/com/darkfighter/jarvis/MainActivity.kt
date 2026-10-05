package com.darkfighter.jarvis

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var textView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this).apply {
            text = "JARVIS Initializing...\n38 POINTS ULTIMATE"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(30,30,30,30)
        }
        setContentView(textView)
        tts = TextToSpeech(this, this)
    }
    
    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            tts.language = Locale.US
            tts.setSpeechRate(0.9f)
            tts.setPitch(0.9f)
            textView.text = "JARVIS Yes Boss\nAll Systems Online\nSay: Hey JARVIS"
            tts.speak("Yes Boss, I am online. All systems operational.", TextToSpeech.QUEUE_FLUSH, null, "INIT")
        } else {
            textView.text = "TTS Failed - Install Google TTS"
        }
    }
    
    override fun onDestroy() {
        if(::tts.isInitialized){ tts.stop(); tts.shutdown() }
        super.onDestroy()
    }
}
