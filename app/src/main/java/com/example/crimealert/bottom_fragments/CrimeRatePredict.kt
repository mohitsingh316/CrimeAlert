package com.example.crimealert.bottom_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.crimealert.R

class CrimeRatePredict : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_rate_predict, container, false)

        // Initialize the WebView
        webView = view.findViewById(R.id.web_view)

        // Enable JavaScript in WebView and load the URL
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://crime-rate-prediction-1.onrender.com/")

        return view
    }
}
