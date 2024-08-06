package com.lanier.violet.compose.feature.login

import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanier.violet.compose.widget.TbsWebView
import com.lanier.violet.compose.widget.TbsWebViewClient
import com.lanier.violet.compose.widget.rememberWebViewState
import org.apache.commons.text.StringEscapeUtils

@Composable
fun MainLogin(padding: PaddingValues) {
    var currentChannel by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        LoginWebView()
        LoginChannel(currentChannel) { currentChannel = it }
    }
}

@Composable
private fun LoginWebView(
    modifier: Modifier = Modifier
) {
    val webviewState = rememberWebViewState(url = "")
    val defClient = remember {
        object : TbsWebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                val code = "(function() { return document.documentElement.outerHTML; })();".trimIndent()
                val mUrl = url.toString()
                loadData(mUrl)
                view.evaluateJavascript(code) { value ->
                    val htmlContent = if (value == null) {
                        ""
                    } else {
                        StringEscapeUtils.unescapeEcmaScript(value)
                    }
                    if (url!!.contains("web2.17roco.qq.com/fcgi-bin/login")) {
                        if (htmlContent.isNotEmpty()) {
                            val isNight = htmlContent.contains("res.17roco.qq.com/swf/night.swf")
                            if (isNight) {
                                // todo 已经很晚了~
                            } else {
                                val angelKey = extractString(htmlContent, "&angel_key=", "&skey=")
                                val pskey = extractString(htmlContent, "pskey=", "\"")
                                val qq = extractString(htmlContent, "_uin=", "&ang")
                            }
                        }
                    }
                }
            }
        }
    }
    TbsWebView(
        state = webviewState,
        modifier = modifier
            .height(200.dp),
        client = defClient,
        onCreated = { webView ->
            webView.settings.apply {
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.95 Safari/537.36"
                javaScriptEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    )
}

private fun loadData(url: String) {
    if (url.contains("web2.17roco.qq.com/fcgi-bin/login")) {
        CookieManager.getInstance().run {
            setAcceptCookie(true)
            val mCookie = getCookie(url)
            setCookie("Accept-Encoding", "gzip, deflate")
            setCookie("Connection", "keep-alive")
            setCookie("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.95 Safari/537.36")
            setCookie("Accept-Language", "zh-CN,zh-CN;q=0.8,en-US;q=0.6")
            setCookie("Version", "1.0")
            setCookie("Cookie", mCookie)
            flush()
        }
    }
}

private fun extractString(source: String, startDelimiter: String, endDelimiter: String): String {
    val regex = Regex("""$startDelimiter(.*?)$endDelimiter""")
    val matchResult = regex.find(source)
    return matchResult?.groupValues?.get(1)?:""
}

@Composable
private fun LoginChannel(
    value: String,
    onChannelChanged: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onChannelChanged
    )
}
