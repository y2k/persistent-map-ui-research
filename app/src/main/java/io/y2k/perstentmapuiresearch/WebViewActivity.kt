package io.y2k.perstentmapuiresearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import io.y2k.research.common.children
import io.y2k.research.common.type
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class WebViewActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true
        setContentView(webView)

        val virView = mkVirtualView()

        Reconciliation.reconcile(
            persistentListOf(),
            persistentListOf(virView),
            webView
        )
    }

    private fun mkVirtualView() =
        persistentMapOf(
            type to "h1",
            children to persistentListOf(
                persistentMapOf(
                    type to "img",
                    "src" to "http://htmlbook.ru/themes/hb/img/browser_sa.png"
                )
            )
        )
}
