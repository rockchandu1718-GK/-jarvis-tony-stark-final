package com.darkfighter.jarvis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var heardText: TextView
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var ttsReady = false
    private var torchOn = false

    // ===== NOTES =====
    private fun saveNote(note: String){
        val prefs = getSharedPreferences("jarvis_notes", Context.MODE_PRIVATE)
        val existing = prefs.getStringSet("notes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        existing.add("${Date().toLocaleString()}: $note")
        prefs.edit().putStringSet("notes", existing).apply()
    }
    private fun getNotes(): String {
        val prefs = getSharedPreferences("jarvis_notes", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("notes", setOf())
        if(set.isNullOrEmpty()) return "No notes saved Boss"
        return set.joinToString("\n")
    }
    private fun clearNotes(){ getSharedPreferences("jarvis_notes", Context.MODE_PRIVATE).edit().clear().apply() }

    private fun getBattery(): String {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return "Battery is $pct percent Boss - Tony Stark power at $pct%"
    }

    private fun calcSimple(h: String): String {
        try{
            var e = h.replace("calculate","").replace("calc","").replace("what is","").trim()
            e = e.replace("plus","+").replace("minus","-").replace("into","*").replace("x","*").replace(" ","")
            val result = when {
                e.contains("+") -> { val p=e.split("+"); p[0].toDouble()+p[1].toDouble() }
                e.contains("-") -> { val p=e.split("-"); p[0].toDouble()-p[1].toDouble() }
                e.contains("*") -> { val p=e.split("*"); p[0].toDouble()*p[1].toDouble() }
                e.contains("/") -> { val p=e.split("/"); p[0].toDouble()/p[1].toDouble() }
                else -> e.toDouble()
            }
            return "$e equals $result Boss"
        }catch(ex: Exception){ return "Calculation failed Boss, try like 50 plus 50" }
    }

    private fun openUrl(url: String): String {
        try{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            return "Opening ${Uri.parse(url).host} Boss"
        }catch(e: Exception){ return "Cannot open link Boss" }
    }

    private fun openAppPackage(pkg: String, marketUrl: String): String {
        try{
            val intent = packageManager.getLaunchIntentForPackage(pkg)
            if(intent!=null){ startActivity(intent); return "Opening app Boss" }
            else { return openUrl(marketUrl) }
        }catch(e: Exception){ return openUrl(marketUrl) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(30,30,30,30)
        }
        statusText = TextView(this).apply {
            text = "JARVIS Initializing...\n38 POINTS ULTIMATE - SILENT MODE"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0,0,0,30)
        }
        heardText = TextView(this).apply {
            text = "Say: Hey JARVIS - No Beep - Silent Always-On"
            textSize = 15f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        }
        layout.addView(statusText)
        layout.addView(heardText)
        setContentView(layout)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE), 101)
        }
        tts = TextToSpeech(this, this)
    }

    private fun setupRecognizer() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                heardText.text = "Speech Recognition Not Available"
                return
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { 
                    // SILENT - no beep - just update UI
                    statusText.text = "JARVIS Listening...\n38 Commands - SILENT MODE - No Tuhu Taha" 
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) { 
                    // Auto restart SILENT - no beep
                    if (isListening) statusText.postDelayed({ startListening() }, 800) 
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val heard = matches[0].lowercase(Locale.ROOT)
                        heardText.text = "You: $heard"
                        val reply = handle38Points(heard)
                        statusText.text = reply
                        speak(reply)
                    }
                    // SILENT LOOP - no beep
                    if (isListening) statusText.postDelayed({ startListening() }, 1000)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch(e: Exception){ statusText.text = "Mic error: ${e.message}" }
    }

    private fun handle38Points(h: String): String {
        // 1-3 Wake
        if((h.contains("hello") && h.contains("jarvis")) || h.contains("hey jarvis") || h.contains("hi jarvis")) return "Hello Boss, 38 features silent ready. No beep. How can I help you?"
        
        // 4-5 Time Date
        if(h.contains("time")){ val t = java.text.SimpleDateFormat("hh mm a", Locale.US).format(Date()); return "Time is $t IST Boss" }
        if(h.contains("date") || h.contains("today")){ val d = java.text.SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.US).format(Date()); return "Today is $d Boss" }
        
        // 6 Battery
        if(h.contains("battery")) return getBattery()
        
        // 7-9 Notes
        if(h.contains("note") && (h.contains("add") || h.contains("rayi") || h.contains("save"))){
            val note = h.replace("note","").replace("add","").replace("rayi","").replace("save","").trim()
            if(note.length>2){ saveNote(note); return "Note saved Boss: $note" }
            return "What note to save Boss?"
        }
        if(h.contains("notes") && (h.contains("show") || h.contains("chupu") || h.contains("list"))){ return getNotes() }
        if(h.contains("clear notes") || h.contains("delete notes")){ clearNotes(); return "All notes cleared Boss" }
        
        // 10 Calc
        if(h.contains("calculate") || h.contains("calc") || h.contains("plus") || h.contains("minus") || h.contains("multiply") || h.contains("divide")) return calcSimple(h)
        
        // 11 Currency
        if(h.contains("dollar") || h.contains("usd") || h.contains("convert") || h.contains("currency")) return "100 USD is approximately 8300 INR Boss. Live rate needs internet."
        
        // 12-13 Translate Dictionary
        if(h.contains("translate")){
            if(h.contains("hello")) return "Hello in Telugu is Namaste Boss"
            if(h.contains("thank")) return "Thank you in Telugu is Dhanyavadamulu Boss"
            if(h.contains("love")) return "Love in Telugu is Prema Boss"
            return "Translate ready Boss. Say translate hello"
        }
        if(h.contains("meaning") || h.contains("dictionary") || h.contains("ardham")) return "Dictionary Boss: Tell me the word, I will explain"
        
        // 14-15 Coin Dice
        if(h.contains("coin") || h.contains("toss")) return if(Random.nextBoolean()) "Heads Boss!" else "Tails Boss!"
        if(h.contains("dice")) return "Dice rolled: ${Random.nextInt(1,7)} Boss"
        
        // 16 Joke
        if(h.contains("joke")){ val jokes = listOf("Teacher: homework? Student: network pothondhi madam!", "Pappu is coding JARVIS at 4 AM!", "Why did JARVIS cross road? To say Yes Boss!", "Tony Stark built this in a cave! With a box of scraps!"); return jokes.random() }
        
        // 17 Flashlight
        if(h.contains("flash") || h.contains("torch") || h.contains("light on") || h.contains("light off")){
            try{
                val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val camId = cm.cameraIdList[0]
                torchOn = !torchOn
                cm.setTorchMode(camId, torchOn)
                return if(torchOn) "Flashlight on Boss - Tony Stark torch activated" else "Flashlight off Boss"
            }catch(e: Exception){ return "Flashlight not supported Boss" }
        }
        
        // 18-20 Personality
        if(h.contains("who are you")) return "I am JARVIS, your personal AI assistant built by you Boss - 38 points ultimate silent edition"
        if(h.contains("how are you")) return "I am fully operational and green across all systems Boss - Silent mode active - No beep"
        if(h.contains("thank")) return "You are welcome Boss, always at your service - Silent mode"
        
        // 21 Open settings
        if(h.contains("open settings") || h.contains("settings open")){ startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)); return "Opening Settings Boss" }
        
        // 22-26 Open sites
        if(h.contains("open youtube") || h.contains("youtube open")) return openUrl("https://youtube.com")
        if(h.contains("open google") || h.contains("google open")) return openUrl("https://google.com")
        if(h.contains("open github")) return openUrl("https://github.com")
        if(h.contains("open instagram") || h.contains("open insta")) return openUrl("https://instagram.com")
        if(h.contains("open facebook") || h.contains("open fb")) return openUrl("https://facebook.com")
        if(h.contains("open spotify")) return openUrl("https://open.spotify.com")
        
        // 27-28 Nano Music
        if(h.contains("nano music") && h.contains("open")) return openUrl("https://music.youtube.com")
        if(h.contains("nano music") && h.contains("play")){
            val q = h.replace("play","").replace("on nano music","").replace("nano music","").trim()
            if(q.isNotEmpty()) return openUrl("https://music.youtube.com/search?q=${Uri.encode(q)}")
            return openUrl("https://music.youtube.com")
        }
        
        // 29-30 Music / YouTube play
        if(h.startsWith("play ") || h.contains("youtube search")){
            val q = h.replace("play","").replace("youtube search","").replace("search on youtube","").trim()
            if(q.length>1) return openUrl("https://www.youtube.com/results?search_query=${Uri.encode(q)}")
        }
        
        // 31-32 WhatsApp / Call
        if(h.contains("whatsapp") || h.contains("whats app")){
            try{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://wa.me/")
                // Try native WhatsApp
                intent.setPackage("com.whatsapp")
                startActivity(intent)
                return "Opening WhatsApp Boss - No beep - Silent mode"
            }catch(e: Exception){
                return openUrl("https://wa.me/")
            }
        }
        if(h.contains("call ")){
            val name = h.replace("call","").trim()
            if(name.isNotEmpty()){
                try{
                    val intent = Intent(Intent.ACTION_DIAL)
                    startActivity(intent)
                    return "Opening dialer for $name Boss"
                }catch(e: Exception){ return "Dialer opening Boss for $name" }
            }
        }
        
        // 33-34 Google search / Wikipedia
        if(h.contains("google search") || h.contains("search google")){
            val q = h.replace("google search","").replace("search google","").replace("for","").trim()
            if(q.length>1) return openUrl("https://www.google.com/search?q=${Uri.encode(q)}")
            return "What to search on Google Boss?"
        }
        if(h.contains("search ") || h.contains("wikipedia")){
            val q = h.replace("search","").replace("wikipedia","").trim()
            if(q.length>1) return openUrl("https://en.wikipedia.org/wiki/Special:Search?search=${Uri.encode(q)}")
        }
        
        // 35-36 Weather News (open)
        if(h.contains("weather")){
            val city = if(h.contains("in ")) h.substringAfter("in ").trim() else "Hyderabad"
            return openUrl("https://www.google.com/search?q=weather+in+${Uri.encode(city)}")
        }
        if(h.contains("news")) return openUrl("https://news.google.com")
        
        // 37 Timer
        if(h.contains("timer") || h.contains("alarm")){
            return "Timer set Boss - Android alarm - Say set timer for 5 minutes (manual for now)"
        }
        
        // 38 Help
        if(h.contains("help") || h.contains("commands") || h.contains("list")) return "38 Commands Ultimate Silent: Hello Jarvis, Time, Date, Battery, Note add/show/clear, Calculator, Currency, Translate, Dictionary, Coin toss, Dice, Joke, Flashlight, Who are you, How are you, Thank you, Open settings, Open YouTube/Google/GitHub/Instagram/Facebook/Spotify, Nano Music open/play, Play song, WhatsApp, Call, Google search, Wikipedia search, Weather, News, Timer, Help - ALL SILENT NO BEEP"
        
        return "You said $h Boss. Say help for 38 silent commands list - No tuhu taha sound"
    }

    private fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) { statusText.text = "Listen Error: ${e.message}" }
    }

    private fun speak(text: String) { if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS") }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Mic Granted - Silent Mode Starting..."
            setupRecognizer()
            if (ttsReady) { isListening = true; startListening() }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var r = tts.setLanguage(Locale.US)
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) r = tts.setLanguage(Locale.ENGLISH)
            tts.setSpeechRate(0.88f); tts.setPitch(0.88f); ttsReady = true
            statusText.text = "JARVIS Yes Boss\n38 POINTS ULTIMATE SILENT\nNo Tuhu Taha - Always On"
            tts.speak("Yes Boss, I am online. 38 features ultimate silent ready. No beep sound. Say Hey Jarvis.", TextToSpeech.QUEUE_FLUSH, null, "INIT")
            setupRecognizer()
            isListening = true; statusText.postDelayed({ startListening() }, 1500)
        } else { statusText.text = "TTS Failed" }
    }

    override fun onDestroy() {
        isListening = false
        try { if (::tts.isInitialized) { tts.stop(); tts.shutdown() }; speechRecognizer?.destroy() } catch (e: Exception) {}
        super.onDestroy()
    }
}
