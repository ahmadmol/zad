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
    // Keep the installation boundary concrete: Lint resolves Compose remember() results as T.
    internal fun installIn(view: android.webkit.WebView) {
        view.addJavascriptInterface(this, "IhsanPlayback")
    }
    private val main = Handler(Looper.getMainLooper())
    @Volatile private var closed = false
    fun close() { closed = true; main.removeCallbacksAndMessages(null) }
    private fun dispatch(action: () -> Unit) { if (!closed) main.post { if (!closed) action() } }
    @JavascriptInterface fun ready() { dispatch(onReady) }
    @JavascriptInterface fun playing() { dispatch(onPlaying) }
    @JavascriptInterface fun stopped() { dispatch(onStopped) }
    @JavascriptInterface fun failed(code: Int) {
        android.util.Log.w("IhsanYouTube", "Official player error: $code")
        dispatch(onError)
    }
}
