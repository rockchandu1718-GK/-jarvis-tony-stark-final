package com.darkfighter.jarvis
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val tv = TextView(this).apply {
      text = "JARVIS ONLINE\nYes Boss\n#22 ULTRA FIXED\nNo Crash"
      textSize = 28f; setTextColor(Color.CYAN); gravity = Gravity.CENTER
      setBackgroundColor(Color.BLACK); setPadding(60,60,60,60)
    }
    setContentView(tv)
  }
}
