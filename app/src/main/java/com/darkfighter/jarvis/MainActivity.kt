package com.darkfighter.jarvis

import android.app.Activity
import android.os.Bundle
import android.webkit.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.speech.tts.TextToSpeech
import java.util.*

class MainActivity : Activity() {
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Permission for mic
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), 1)
        }
        
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }
            }
            webViewClient = WebViewClient()
        }
        
        setContentView(webView)
        // Load JARVIS from assets
        webView.loadUrl("file:///android_asset/jarvis.html")
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
