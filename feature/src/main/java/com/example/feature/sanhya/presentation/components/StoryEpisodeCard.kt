package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.presentation.components.MediaCard
import com.example.feature.sanhya.domain.model.StoryEpisode

@Composable
fun StoryEpisodeCard(episode: StoryEpisode, coverUrl: String, isSelected: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier) {
    MediaCard(episode.title, episode.partLabel.orEmpty(), episode.thumbnailUrl ?: coverUrl,
        episode.durationLabel, false, onClick, {}, modifier, selected = isSelected,
        showFavorite = false, trailing = { IconButton(onClick = onClick) {
            Icon(Icons.Default.PlayArrow, "اختيار الحلقة", tint = if (isSelected)
                IhsanTheme.colors.selectedContent else IhsanTheme.colors.accentContent)
        } })
}
