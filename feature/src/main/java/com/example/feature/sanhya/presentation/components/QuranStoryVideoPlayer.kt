package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.VideoHero

@Composable
fun QuranStoryVideoPlayer(
    videoId: String?,
    coverUrl: String?,
    modifier: Modifier = Modifier,
    startSeconds: Int = 0,
    isOnline: Boolean = true,
    isEmbeddable: Boolean = false,
    onOpenExternalYouTube: () -> Unit = {}
) = VideoHero(
    videoId = videoId,
    image = coverUrl,
    isOnline = isOnline,
    isEmbeddable = isEmbeddable,
    modifier = modifier,
    startSeconds = startSeconds,
    onOpenExternal = onOpenExternalYouTube
)
