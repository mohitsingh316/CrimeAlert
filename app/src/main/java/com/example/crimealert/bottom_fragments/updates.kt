package com.example.crimealert.bottom_fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.crimealert.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val FILECHOOSER_RESULTCODE = 1

class updates : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_updates, container, false)
        val webView: WebView = view.findViewById(R.id.webView)

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (mUploadMessage != null) {
                    mUploadMessage?.onReceiveValue(null)
                    mUploadMessage = null
                }

                mUploadMessage = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Image Chooser"), FILECHOOSER_RESULTCODE)

                return true
            }
        }

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Enable desktop mode
        webSettings.userAgentString = webSettings.userAgentString.replace("Mobile", "eliboM").replace("Android", "diordnA")
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        // Load the desired URL
        webView.loadUrl("https://deepfake-detect.com/")

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage == null) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (result != null) {
                mUploadMessage?.onReceiveValue(arrayOf(result))
                mUploadMessage = null
            } else {
                mUploadMessage?.onReceiveValue(null)
                mUploadMessage = null
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            updates().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
