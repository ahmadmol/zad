package com.example.feature.live

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.view.ViewGroup
import com.example.feature.learning.presentation.components.VideoWebChromeClient
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
import androidx.compose.foundation.layout.heightIn
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
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.component.IhsanTopBarStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import com.example.designsystem.theme.IhsanTheme
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

    var playbackMode by remember(activeSource) { mutableStateOf(if (LiveStreamSources.DIRECT_HLS_APPROVED) LivePlaybackMode.Hls else LivePlaybackMode.Youtube) }
    var isLoading by remember(activeSource) { mutableStateOf(true) }
    var isLivePlaying by remember(activeSource) { mutableStateOf(false) }
    var errorMessage by remember(activeSource) { mutableStateOf<String?>(null) }
    var playbackAttempt by remember(activeSource) { mutableStateOf(0) }
    var isFullscreen by remember { mutableStateOf(false) }

    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF003B46)
    val darkTealSubtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF1B535D)
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
        playbackAttempt++
        errorMessage = null
        isLoading = true
        isLivePlaying = false
        playbackMode = if (LiveStreamSources.DIRECT_HLS_APPROVED) LivePlaybackMode.Hls else LivePlaybackMode.Youtube
    }

    // A movable composition keeps the ExoPlayer/WebView owner and playback state alive while
    // its visual host moves between the inline card and fullscreen dialog.
    val playerContent = remember(activeSource) {
        movableContentOf {
            androidx.compose.runtime.key(playbackAttempt) {
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
                    errorMessage = "تعذر تشغيل البث داخل التطبيق. تحقق من الاتصال أو افتح المصدر الرسمي."
                    isLoading = false
                    isLivePlaying = false
                },
                onRefresh = ::refreshPlayer,
                onOpenYoutubeApp = ::openInYoutubeApp
            )
            }
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
                    .height(120.dp)
            )

            // Scrim overlay for Dark mode legibility
            if (isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Brush.verticalGradient(listOf(IhsanTheme.colors.surfaceBase.copy(alpha = .20f), IhsanTheme.colors.surfaceBase.copy(alpha = .82f))))
                )
            }

            // 2. Main Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // Top Navigation Header Row
                IhsanTopBar(
                    title = "البث المباشر",
                    subtitle = "من الحرم المكي والنبوي الشريف",
                    style = IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background,
                    onNavigateBack = onBack
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Segmented Control Switcher (Touch targets >= 48dp)
                LiveSourceSegmentedControl(
                    selectedSource = activeSource,
                    onSourceSelected = ::handleSourceSelect,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Main Player Card Container
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = .5.dp),
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
                                .heightIn(min = 200.dp).aspectRatio(16f / 9f)
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
                        .padding(horizontal = 12.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.TextButton(
                    colors = com.example.designsystem.theme.ihsanTextButtonColors(),
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LiveStreamSources.OFFICIAL_LIVE_PORTAL)))
                        }.onFailure {
                            com.example.feature.core.notification.UserMessageNotifier.notify(context, "تعذر فتح المصدر في المتصفح")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("فتح البث في بوابة الحرمين الرسمية") }

                // 6. Alternate Sources Section ("مصادر أخرى")
                val alternateSource = if (activeSource == LiveSourceType.HARAM) LiveSourceType.NABAWI else LiveSourceType.HARAM
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
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
        Image(painterResource(R.drawable.ihsan_live_mosque_sunset), null, Modifier.matchParentSize(), contentScale = ContentScale.Fit)

        when {
            errorMessage != null -> {
                // Error State Overlay
                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.72f)))
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
                                containerColor = if (IhsanTheme.isDark) MaterialTheme.colorScheme.primary else Color(0xFF00695C)
                            ),
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("إعادة المحاولة", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onOpenYoutubeApp,
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                        , colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))) {
                            Text("فتح المصدر الرسمي", color = Color.White, fontSize = 13.sp)
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
                    onIsPlayingChanged = onIsPlayingChanged,
                    onError = onWebError
                )
            }
        }

        if (playbackMode == LivePlaybackMode.Hls && isLoading && errorMessage == null) Image(painterResource(R.drawable.ihsan_live_mosque_sunset),
            null, Modifier.matchParentSize(), contentScale = ContentScale.Fit)

        // Live / Neutral Status Badge Overlay (Product Truth: only show 'مباشر' when actively playing in ExoPlayer)
        if (playbackMode == LivePlaybackMode.Hls) Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd)
        ) {
            LiveStatusBadge(isPlaying = isLivePlaying && errorMessage == null && playbackMode == LivePlaybackMode.Hls)
        }

        // Loading Spinner Overlay
        if (playbackMode == LivePlaybackMode.Hls && isLoading && errorMessage == null) {
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
    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF003B46)

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
                        .clip(RoundedCornerShape(16.dp))
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
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else {
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
                if (isPlaying) MaterialTheme.colorScheme.error
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
    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF003B46)

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
                tint = if (isDark) IhsanTheme.colors.textSecondary else darkTealColor,
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
            .heightIn(min = 200.dp).aspectRatio(16f / 9f)
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
    onIsPlayingChanged: (Boolean) -> Unit,
    onError: () -> Unit
) {
    val embedUrl = remember(channelId) { LiveStreamSources.youtubeEmbedUrl(channelId) }
    val latestOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val latestOnError by rememberUpdatedState(onError)
    val latestOnIsPlayingChanged by rememberUpdatedState(onIsPlayingChanged)
    var hasPlayed by remember(channelId) { mutableStateOf(false) }
    var isPlaying by remember(channelId) { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val playerContext = androidx.compose.ui.platform.LocalContext.current
    val fullscreenClient = remember(playerContext) { VideoWebChromeClient(playerContext) }

    androidx.compose.runtime.LaunchedEffect(channelId) {
        kotlinx.coroutines.delay(25_000)
        if (!hasPlayed) {
            android.util.Log.w("IhsanYouTube", "Playback timeout channel=$channelId")
            latestOnError()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webViewRef) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> webViewRef?.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> webViewRef?.apply {
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

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                fullscreenClient.onHideCustomView()
                stopLoading()
                removeJavascriptInterface("IhsanPlayback")
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
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                addJavascriptInterface(com.example.feature.learning.presentation.components.YouTubePlaybackBridge(
                    onReady = { /* Readiness is not proof of playback. */ },
                    onPlaying = { hasPlayed = true; isPlaying = true; latestOnLoadingChanged(false); latestOnIsPlayingChanged(true) },
                    onStopped = { isPlaying = false; latestOnIsPlayingChanged(false) },
                    onError = { latestOnIsPlayingChanged(false); latestOnError() }
                ), "IhsanPlayback")
                webChromeClient = fullscreenClient
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // The iframe can finish loading while displaying an unavailable video.
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
                val origin = "https://${ctx.packageName.lowercase(java.util.Locale.ROOT)}/"
                val html = """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="referrer" content="strict-origin-when-cross-origin"><style>html,body{margin:0;width:100%;height:100%;background:black}iframe{width:100%;height:100%;border:0}</style></head><body><iframe id="player" src="$embedUrl&enablejsapi=1&origin=${origin.removeSuffix("/")}" allow="autoplay; encrypted-media; fullscreen" allowfullscreen referrerpolicy="strict-origin-when-cross-origin"></iframe><script>var player; function onYouTubeIframeAPIReady(){player=new YT.Player('player',{events:{onReady:function(){IhsanPlayback.ready();},onStateChange:function(e){if(e.data===1)IhsanPlayback.playing();else IhsanPlayback.stopped();},onError:function(e){IhsanPlayback.failed(e.data);}}});}</script><script src="https://www.youtube.com/iframe_api"></script></body></html>"""
                loadDataWithBaseURL(origin, html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize().semantics {
            stateDescription = if (isPlaying) "تشغيل البث مؤكد" else "جارٍ تحميل البث"
        }
    )
}
