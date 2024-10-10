package com.example.crimealert.activity

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.crimealert.R

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val url = intent.getStringExtra("url")

        if (url != null) {
            val webView: WebView = findViewById(R.id.newsWebView)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
        }
    }
}
