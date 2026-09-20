package com.example.feature.nabiihsan.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.IhsanYouTubePlayer

@Composable
fun NabiIhsanVideoPlayer(
    videoId: String?,
    coverUrl: String?,
    modifier: Modifier = Modifier,
    startSeconds: Int = 0,
    isOnline: Boolean = true,
    isEmbeddable: Boolean = false,
    onOpenExternalYouTube: () -> Unit = {}
) = IhsanYouTubePlayer(
    videoId = videoId,
    thumbnailUrl = coverUrl,
    isOnline = isOnline,
    isEmbeddable = isEmbeddable,
    modifier = modifier,
    startSeconds = startSeconds,
    onOpenExternal = onOpenExternalYouTube
)
