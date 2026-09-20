package com.example.feature.learning.presentation.components

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.feature.R

internal const val LEARNING_YOUTUBE_AUTOPLAY = 0
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
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        when {
            !isOnline -> PlayerMessage(
                icon = { Icon(Icons.Default.SignalWifiOff, null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(36.dp)) },
                message = "يتطلب تشغيل الفيديو اتصالًا بالإنترنت"
            )

            videoId.isNullOrBlank() -> ThumbnailState(
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
                    onAction = onOpenExternal
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
                        tint = Color.White,
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
    onAction: (() -> Unit)? = null
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
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    val currentOnPlayerLoadError by rememberUpdatedState(onPlayerLoadError)
    val lifecycleOwner = LocalLifecycleOwner.current
    val embedHtml = remember(videoId, startSeconds) {
        buildEmbedHtml(videoId, startSeconds.coerceAtLeast(0))
    }

    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> webView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(videoId) {
        onDispose {
            webView?.apply {
                onPause()
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webView = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return true
                        if (!request.isForMainFrame) return false
                        return !isAllowedMainFrameUri(uri)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = Unit

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
                loadDataWithBaseURL(YOUTUBE_BASE_URL, embedHtml, "text/html", "UTF-8", null)
            }
        },
        update = { view ->
            if (view.tag != embedHtml) {
                view.tag = embedHtml
                view.loadDataWithBaseURL(YOUTUBE_BASE_URL, embedHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun isAllowedMainFrameUri(uri: Uri): Boolean {
    if (uri.scheme == "about" && uri.toString() == "about:blank") return true
    if (uri.scheme != "https") return false
    val host = uri.host?.lowercase() ?: return false
    if (host != "www.youtube.com" && host != "youtube.com") return false
    return uri.path.isNullOrBlank() || uri.path == "/" || uri.path?.startsWith("/embed/") == true
}

private fun buildEmbedHtml(videoId: String, startSeconds: Int): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>
        html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
        iframe { width: 100%; height: 100%; border: 0; }
      </style>
    </head>
    <body>
      <iframe
        src="https://www.youtube.com/embed/$videoId?autoplay=$LEARNING_YOUTUBE_AUTOPLAY&start=$startSeconds&rel=0&modestbranding=1&playsinline=$LEARNING_YOUTUBE_PLAYS_INLINE"
        allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen>
      </iframe>
    </body>
    </html>
""".trimIndent()

private const val YOUTUBE_BASE_URL = "https://www.youtube.com"
