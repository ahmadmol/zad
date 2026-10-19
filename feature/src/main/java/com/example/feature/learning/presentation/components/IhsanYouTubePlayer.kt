package com.example.feature.learning.presentation.components

import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.feature.R

internal const val LEARNING_YOUTUBE_AUTOPLAY = 1
internal const val LEARNING_YOUTUBE_PLAYS_INLINE = 1

@Composable
fun IhsanYouTubePlayer(
    videoId: String?,
    thumbnailUrl: String?,
    isOnline: Boolean,
    isEmbeddable: Boolean,
    modifier: Modifier = Modifier,
    startSeconds: Int = 0,
    onOpenExternal: () -> Unit
) {
    var isPlayerActive by remember(videoId) { mutableStateOf(false) }
    var playerLoadFailed by remember(videoId) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        when {
            !isOnline -> com.example.designsystem.component.IhsanOfflineState(Modifier.fillMaxSize(), compact = true)

            videoId.isNullOrBlank() || !videoId.matches(Regex("[A-Za-z0-9_-]{11}")) -> ThumbnailState(
                thumbnailUrl = thumbnailUrl,
                overlayAlpha = 0.6f
            ) {
                PlayerMessage(
                    icon = { Icon(Icons.Default.SmartDisplay, null, tint = Color.White, modifier = Modifier.size(38.dp)) },
                    message = "الفيديو غير متاح حاليًا من المصدر الرسمي"
                )
            }

            !isEmbeddable -> ThumbnailState(
                thumbnailUrl = thumbnailUrl,
                overlayAlpha = 0.55f
            ) {
                PlayerMessage(
                    icon = { Icon(Icons.Default.SmartDisplay, null, tint = Color.White, modifier = Modifier.size(38.dp)) },
                    message = "لا يدعم المصدر الرسمي التشغيل داخل التطبيق",
                    actionLabel = "مشاهدة على YouTube",
                    onAction = onOpenExternal
                )
            }

            playerLoadFailed -> ThumbnailState(
                thumbnailUrl = thumbnailUrl,
                overlayAlpha = 0.6f
            ) {
                PlayerMessage(
                    icon = { Icon(Icons.Default.SmartDisplay, null, tint = Color.White, modifier = Modifier.size(38.dp)) },
                    message = "تعذر تشغيل الفيديو داخل التطبيق",
                    actionLabel = "المشاهدة على YouTube",
                    onAction = onOpenExternal,
                    onRetry = { playerLoadFailed = false; isPlayerActive = true }
                )
            }

            !isPlayerActive -> ThumbnailState(
                thumbnailUrl = thumbnailUrl,
                overlayAlpha = 0.35f,
                modifier = Modifier.clickable { isPlayerActive = true }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل الفيديو",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            else -> EmbeddedYouTubeWebView(
                videoId = videoId,
                startSeconds = startSeconds,
                onPlayerLoadError = { playerLoadFailed = true }
            )
        }
    }
}

@Composable
private fun ThumbnailState(
    thumbnailUrl: String?,
    overlayAlpha: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ihsan_mosque_sunrise_landscape),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = overlayAlpha)))
        content()
    }
}

@Composable
private fun PlayerMessage(
    icon: @Composable () -> Unit,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(10.dp))
            if (onRetry != null) {
                androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAction, modifier = Modifier.weight(1f)) {
                        Text(actionLabel, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                    Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
                        Text("إعادة المحاولة", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            }
        }
    }
}

@Composable
private fun EmbeddedYouTubeWebView(
    videoId: String,
    startSeconds: Int,
    onPlayerLoadError: () -> Unit
) {
    var webView by remember(videoId) { mutableStateOf<WebView?>(null) }
    var isVideoLoading by remember(videoId) { mutableStateOf(true) }
    var isActuallyPlaying by remember(videoId) { mutableStateOf(false) }
    var hasPlaybackStarted by remember(videoId) { mutableStateOf(false) }
    val currentOnPlayerLoadError by rememberUpdatedState(onPlayerLoadError)
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val fullscreenClient = remember(videoId, context) { VideoWebChromeClient(context) }
    val appReferrer = remember(context.packageName) { "https://${context.packageName.lowercase(java.util.Locale.ROOT)}/" }
    val embedHtml = remember(videoId, startSeconds, appReferrer) {
        buildEmbedHtml(videoId, startSeconds.coerceAtLeast(0), appReferrer.removeSuffix("/"))
    }

    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> webView?.apply {
                    fullscreenClient.onHideCustomView()
                    evaluateJavascript("if(window.player && player.pauseVideo){player.pauseVideo();}", null)
                    onPause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(videoId) {
        onDispose {
            webView?.apply {
                fullscreenClient.onHideCustomView()
                onPause()
                stopLoading()
                removeJavascriptInterface("IhsanPlayback")
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(videoId) {
        kotlinx.coroutines.delay(25_000)
        // IFrame onReady only means its controls initialized. A ready-but-black player
        // must also reach PLAYING; otherwise expose the working external fallback.
        if (!hasPlaybackStarted) {
            android.util.Log.w("IhsanYouTube", "Playback timeout video=$videoId ready=${!isVideoLoading}")
            currentOnPlayerLoadError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().then(Modifier.semantics {
        stateDescription = if (isActuallyPlaying) "تشغيل الفيديو مؤكد" else if (isVideoLoading) "جارٍ تحميل الفيديو" else "مشغل الفيديو جاهز"
    })) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // WRAP_CONTENT gives percentage-height HTML an intrinsic height of zero,
                    // even when Compose measures the native view to the full video rectangle.
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    addJavascriptInterface(YouTubePlaybackBridge(
                        onReady = { isVideoLoading = false },
                        onPlaying = { hasPlaybackStarted = true; isActuallyPlaying = true; isVideoLoading = false },
                        onError = { currentOnPlayerLoadError() },
                        onStopped = { isActuallyPlaying = false }
                    ), "IhsanPlayback")
                    webChromeClient = fullscreenClient
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val uri = request?.url ?: return true
                            if (!request.isForMainFrame) return false
                            if (isAllowedMainFrameUri(uri)) return false
                            if (uri.scheme == "https" && (uri.host == "www.youtube.com" || uri.host == "youtube.com" || uri.host == "youtu.be")) {
                                runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) }
                            }
                            return true
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isVideoLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            // HTML completion is not proof that the actual player is ready.
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            if (request?.isForMainFrame == true) currentOnPlayerLoadError()
                        }

                        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                            if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 0) >= 400) {
                                currentOnPlayerLoadError()
                            }
                        }

                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            currentOnPlayerLoadError()
                            view?.destroy()
                            return true
                        }
                    }
                    tag = embedHtml
                    loadDataWithBaseURL(appReferrer, embedHtml, "text/html", "UTF-8", null)
                }
            },
            update = { view ->
                if (view.tag != embedHtml) {
                    view.tag = embedHtml
                    view.loadDataWithBaseURL(appReferrer, embedHtml, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // The official player owns its loading UI; do not cover its controls with overlays.

    }
}

private fun isAllowedMainFrameUri(uri: Uri): Boolean {
    if (uri.scheme == "about" && uri.toString() == "about:blank") return true
    if (uri.scheme != "https") return false
    val host = uri.host?.lowercase() ?: return false
    if (host != "www.youtube.com" && host != "youtube.com") return false
    return uri.path.isNullOrBlank() || uri.path == "/" || uri.path?.startsWith("/embed/") == true
}

private fun buildEmbedHtml(videoId: String, startSeconds: Int, appOrigin: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <meta name="referrer" content="strict-origin-when-cross-origin">
      <style>
        html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
        iframe { width: 100%; height: 100%; border: 0; }
      </style>
    </head>
    <body>
      <div id="player" style="width:100%;height:100%"></div>
      <script>
        var player;
        function onYouTubeIframeAPIReady() {
          player = new YT.Player('player', {
            videoId: '$videoId',
            playerVars: { origin: '$appOrigin', autoplay: $LEARNING_YOUTUBE_AUTOPLAY, start: $startSeconds, rel: 0,
              playsinline: $LEARNING_YOUTUBE_PLAYS_INLINE, controls: 1 },
            events: {
              onReady: function() { IhsanPlayback.ready(); },
              onStateChange: function(e) { if(e.data === 1) IhsanPlayback.playing(); else IhsanPlayback.stopped(); },
              onError: function(e) { IhsanPlayback.failed(e.data); }
            }
          });
        }
      </script>
      <script src="https://www.youtube.com/iframe_api"></script>
    </body>
    </html>
""".trimIndent()

