package com.matcher.matcher.modules.terms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.matcher.matcher.utils.AppTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.matcher.matcher.R
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

class TermsActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"
        
        fun start(context: Context, url: String, title: String = "Sözleşmeler") {
            val intent = Intent(context, TermsActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Sözleşmeler"
        
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                                val toolbar = Toolbar(context).apply {
                                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                                    setBackgroundColor(ContextCompat.getColor(context, R.color.primaryDarkColor))
                                    setTitleTextColor(ContextCompat.getColor(context, android.R.color.white))
                                    this.title = title
                                    navigationIcon = ContextCompat.getDrawable(context, R.drawable.left_arrow_white)
                                    setNavigationOnClickListener { finish() }
                                    elevation = 0f
                                    // Status bar padding'i kaldırıldı, sadece normal toolbar yüksekliği
                                    setPadding(paddingLeft, 0, paddingRight, paddingBottom)
                                }
                                addView(toolbar)

                                val webView = WebView(context).apply {
                                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
                                    settings.javaScriptEnabled = true
                                    webViewClient = WebViewClient()
                                    loadUrl(url)
                                }
                                addView(webView)
                            }
                        },
                        update = { root ->
                            val webView = (root as LinearLayout).getChildAt(1) as WebView
                            if (webView.url != url) webView.loadUrl(url)
                        }
                    )
                }
            }
        }
    }
}
