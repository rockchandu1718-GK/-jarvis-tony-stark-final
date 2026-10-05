package com.darkfighter.jarvis
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "JARVIS ONLINE\nYes Boss\nStep 1 - Black Fixed\nNo Crash"
            textSize = 26f; setTextColor(Color.CYAN); gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK); setPadding(50,50,50,50)
        }
        setContentView(tv)
    }
}
