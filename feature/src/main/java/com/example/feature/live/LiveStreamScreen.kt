package com.example.feature.live

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.feature.R

private enum class LivePlaybackMode {
    Hls,
    Youtube
}

/**
 * Main LiveStreamScreen API accepting [LiveSourceType].
 */
@OptIn(UnstableApi::class)
@Composable
fun LiveStreamScreen(
    source: LiveSourceType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectSource: ((LiveSourceType) -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark = com.example.designsystem.theme.IhsanTheme.isDark

    var activeSource by remember(source) { mutableStateOf(source) }

    var playbackMode by remember(activeSource) { mutableStateOf(LivePlaybackMode.Hls) }
    var isLoading by remember(activeSource) { mutableStateOf(true) }
    var isLivePlaying by remember(activeSource) { mutableStateOf(false) }
    var errorMessage by remember(activeSource) { mutableStateOf<String?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }

    val darkTealColor = Color(0xFF003B46)
    val darkTealSubtext = Color(0xFF1B535D)
    val headerTextColor = if (isDark) MaterialTheme.colorScheme.onBackground else darkTealColor
    val headerSubtextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else darkTealSubtext

    fun openInYoutubeApp() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(LiveStreamSources.youtubeChannelLiveUrl(activeSource.youtubeChannelId))
        )
        runCatching { context.startActivity(intent) }
    }

    fun shareLiveStream() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, activeSource.screenTitle)
            putExtra(
                Intent.EXTRA_TEXT,
                "${activeSource.sourceName}\n${activeSource.description}\n${LiveStreamSources.youtubeChannelLiveUrl(activeSource.youtubeChannelId)}"
            )
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة البث المباشر"))
        }
    }

    fun handleSourceSelect(newSource: LiveSourceType) {
        if (newSource == activeSource) return
        activeSource = newSource
        if (onSelectSource != null) {
            onSelectSource.invoke(newSource)
        }
    }

    fun refreshPlayer() {
        errorMessage = null
        isLoading = true
        isLivePlaying = false
        playbackMode = LivePlaybackMode.Hls
    }

    // A movable composition keeps the ExoPlayer/WebView owner and playback state alive while
    // its visual host moves between the inline card and fullscreen dialog.
    val playerContent = remember(activeSource) {
        movableContentOf {
            PlayerSurfaceContent(
                errorMessage = errorMessage,
                playbackMode = playbackMode,
                activeSource = activeSource,
                isLoading = isLoading,
                isLivePlaying = isLivePlaying,
                onLoadingChanged = { isLoading = it },
                onIsPlayingChanged = { isLivePlaying = it },
                onError = {
                    isLoading = true
                    playbackMode = LivePlaybackMode.Youtube
                },
                onWebError = {
                    errorMessage = "تعذر تحميل البث المباشر حالياً. يرجى التحقق من اتصال الانترنت."
                    isLoading = false
                    isLivePlaying = false
                },
                onRefresh = ::refreshPlayer,
                onOpenYoutubeApp = ::openInYoutubeApp
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Top Skyline Hero Background (Matching HOME design anchor)
            Image(
                painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            // Scrim overlay for Dark mode legibility
            if (isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black.copy(alpha = 0.55f))
                )
            }

            // 2. Main Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // Top Navigation Header Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Back Button (Touch target >= 48dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                else Color.White.copy(alpha = 0.85f)
                            )
                            .clickable(onClick = onBack)
                            .semantics {
                                role = Role.Button
                                contentDescription = "رجوع"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = headerTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Title & Subtitle Centered
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "البث المباشر",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerTextColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "من الحرم المكي والنبوي",
                            fontSize = 12.sp,
                            color = headerSubtextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Segmented Control Switcher (Touch targets >= 48dp)
                LiveSourceSegmentedControl(
                    selectedSource = activeSource,
                    onSourceSelected = ::handleSourceSelect,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Main Player Card Container
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column {
                        // 16:9 Video Surface Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                                .background(Color.Black)
                        ) {
                            if (isFullscreen) {
                                // When Fullscreen is active, render placeholder in Card so ONLY ONE player instance exists
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { isFullscreen = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FullscreenExit,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "جارٍ العرض بالشاشة الكاملة",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                playerContent()
                            }
                        }

                        // Source Metadata Footer
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = activeSource.sourceName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeSource.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Quick Actions Row (Fullscreen, Share, Open in YouTube, Refresh)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "شاشة كاملة",
                        icon = Icons.Default.Fullscreen,
                        onClick = { isFullscreen = true },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "مشاركة",
                        icon = Icons.Default.Share,
                        onClick = ::shareLiveStream,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "تطبيق يوتيوب",
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = ::openInYoutubeApp,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "تحديث",
                        icon = Icons.Default.Refresh,
                        onClick = ::refreshPlayer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Alternate Sources Section ("مصادر أخرى")
                val alternateSource = if (activeSource == LiveSourceType.HARAM) LiveSourceType.NABAWI else LiveSourceType.HARAM
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "مصادر أخرى",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AlternateChannelCard(
                        source = alternateSource,
                        onSelect = { handleSourceSelect(alternateSource) }
                    )
                }
            }

            // Fullscreen Player Dialog Overlay (Renders the ONLY player instance when isFullscreen is true)
            if (isFullscreen) {
                Dialog(
                    onDismissRequest = { isFullscreen = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        playerContent()

                        // Exit Fullscreen Button (Touch target >= 48dp)
                        Box(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { isFullscreen = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "إغلاق الشاشة الكاملة",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload accepting string title, hlsUrl, and youtubeChannelId.
 */
@Composable
fun LiveStreamScreen(
    title: String,
    hlsUrl: String,
    youtubeChannelId: String,
    onBack: () -> Unit,
    onSelectSource: ((LiveSourceType) -> Unit)? = null
) {
    val source = remember(hlsUrl, title, youtubeChannelId) {
        LiveSourceType.fromUrlOrTitle(hlsUrl, title)
    }
    LiveStreamScreen(
        source = source,
        onBack = onBack,
        onSelectSource = onSelectSource
    )
}

@Composable
private fun PlayerSurfaceContent(
    errorMessage: String?,
    playbackMode: LivePlaybackMode,
    activeSource: LiveSourceType,
    isLoading: Boolean,
    isLivePlaying: Boolean,
    onLoadingChanged: (Boolean) -> Unit,
    onIsPlayingChanged: (Boolean) -> Unit,
    onError: () -> Unit,
    onWebError: () -> Unit,
    onRefresh: () -> Unit,
    onOpenYoutubeApp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            errorMessage != null -> {
                // Error State Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00695C)
                            ),
                            modifier = Modifier.defaultMinSize(minWidth = 120.dp, minHeight = 48.dp)
                        ) {
                            Text("إعادة المحاولة", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onOpenYoutubeApp,
                            modifier = Modifier.defaultMinSize(minWidth = 120.dp, minHeight = 48.dp)
                        ) {
                            Text("تطبيق يوتيوب", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            playbackMode == LivePlaybackMode.Hls -> {
                HlsLivePlayer(
                    url = activeSource.hlsUrl,
                    onLoadingChanged = onLoadingChanged,
                    onIsPlayingChanged = onIsPlayingChanged,
                    onError = onError
                )
            }

            else -> {
                YoutubeLiveWebView(
                    channelId = activeSource.youtubeChannelId,
                    onLoadingChanged = onLoadingChanged,
                    onError = onWebError
                )
            }
        }

        // Live / Neutral Status Badge Overlay (Product Truth: only show 'مباشر' when actively playing in ExoPlayer)
        Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd)
        ) {
            LiveStatusBadge(isPlaying = isLivePlaying && errorMessage == null && playbackMode == LivePlaybackMode.Hls)
        }

        // Loading Spinner Overlay
        if (isLoading && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveSourceSegmentedControl(
    selectedSource: LiveSourceType,
    onSourceSelected: (LiveSourceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTealColor = Color(0xFF003B46)
    val isDark = com.example.designsystem.theme.IhsanTheme.isDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isDark) MaterialTheme.colorScheme.surfaceVariant
                else Color.White.copy(alpha = 0.9f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LiveSourceType.entries.forEach { source ->
                val isSelected = source == selectedSource
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) MaterialTheme.colorScheme.primary else darkTealColor
                            } else Color.Transparent
                        )
                        .clickable { onSourceSelected(source) }
                        .semantics {
                            role = Role.Button
                            contentDescription = source.tabTitle
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = source.tabTitle,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else {
                            if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else darkTealColor
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusBadge(isPlaying: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) Color(0xFFE53935)
                else Color.Black.copy(alpha = 0.65f)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPlaying) "مباشر" else "البث المباشر",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.example.designsystem.theme.IhsanTheme.isDark
    val darkTealColor = Color(0xFF003B46)

    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = title
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant
            else Color(0xFFF0F7F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) MaterialTheme.colorScheme.primary else darkTealColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlternateChannelCard(
    source: LiveSourceType,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 110.dp)
            .clickable(onClick = onSelect)
            .semantics {
                role = Role.Button
                contentDescription = "الانتقال إلى ${source.sourceName}"
            },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.ihsan_live_mosque_sunset),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = source.sourceName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = source.description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun HlsLivePlayer(
    url: String,
    onLoadingChanged: (Boolean) -> Unit,
    onIsPlayingChanged: (Boolean) -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    val latestOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val latestOnIsPlayingChanged by rememberUpdatedState(onIsPlayingChanged)
    val latestOnError by rememberUpdatedState(onError)

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    latestOnLoadingChanged(
                        state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                    )
                    if (state == Player.STATE_READY && isPlaying) {
                        latestOnIsPlayingChanged(true)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    latestOnIsPlayingChanged(false)
                    latestOnError()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    latestOnIsPlayingChanged(isPlaying)
                    if (isPlaying) latestOnLoadingChanged(false)
                }
            })
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
                setShowNextButton(false)
                setShowPreviousButton(false)
                setBackgroundColor(AndroidColor.BLACK)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YoutubeLiveWebView(
    channelId: String,
    onLoadingChanged: (Boolean) -> Unit,
    onError: () -> Unit
) {
    val embedUrl = remember(channelId) { LiveStreamSources.youtubeEmbedUrl(channelId) }
    val latestOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val latestOnError by rememberUpdatedState(onError)
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
            webViewRef = null
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef = this
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        latestOnLoadingChanged(false)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            latestOnError()
                        }
                    }
                }
                loadUrl(embedUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
