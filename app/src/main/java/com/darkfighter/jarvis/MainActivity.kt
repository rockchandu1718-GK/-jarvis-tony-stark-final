package com.darkfighter.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    inner class WebAppInterface {
        @JavascriptInterface
        fun sendWhatsApp(phone: String, message: String) {
            try {
                val clean = phone.replace(Regex("[^0-9]"), "")
                val uri = if (clean.length > 5) {
                    Uri.parse("https://wa.me/${clean}?text=${Uri.encode(message)}")
                } else {
                    Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                }
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(message)}"))
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        }

        @JavascriptInterface
        fun playYouTube(query: String) {
            try {
                val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (_: Exception) {}
        }

        @JavascriptInterface
        fun keepScreenOn() {
            runOnUiThread {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        @JavascriptInterface
        fun allowScreenOff() {
            runOnUiThread {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), 1)
        }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }
            }
            webViewClient = WebViewClient()
            addJavascriptInterface(WebAppInterface(), "Android")
        }
        setContentView(webView)
        webView.loadUrl("file:///android_asset/jarvis.html")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
