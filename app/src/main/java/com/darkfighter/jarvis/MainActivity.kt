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
            text = "JARVIS ONLINE\nYes Boss\n38 POINTS\nStep 1 Success"
            textSize = 24f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }
        setContentView(tv)
    }
}
