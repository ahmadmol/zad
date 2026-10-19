package com.example.feature.learning.presentation.components

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/** Only installed in the restricted YouTube player WebView; exposes no files, tokens, or intents. */
internal class YouTubePlaybackBridge(
    private val onReady: () -> Unit,
    private val onPlaying: () -> Unit,
    private val onError: () -> Unit,
    private val onStopped: () -> Unit = {}
) {
    private val main = Handler(Looper.getMainLooper())
    @JavascriptInterface fun ready() { main.post { onReady() } }
    @JavascriptInterface fun playing() { main.post { onPlaying() } }
    @JavascriptInterface fun stopped() { main.post { onStopped() } }
    @JavascriptInterface fun failed(code: Int) {
        android.util.Log.w("IhsanYouTube", "Official player error: $code")
        main.post { onError() }
    }
}
