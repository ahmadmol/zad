package com.example.feature.nabiihsan.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.MediaCard
import com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode
import com.example.feature.nabiihsan.util.cleanEpisodeTitle

@Composable
fun NabiEpisodeCard(episode: NabiIhsanEpisode, onEpisodeClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit, modifier: Modifier = Modifier, onRemoveLater: (() -> Unit)? = null) {
    MediaCard(cleanEpisodeTitle(episode.title, episode.episodeNumber), episode.subtitle,
        episode.coverUrl, episode.duration, episode.isFavorite, { onEpisodeClick(episode.id) },
        { onFavoriteToggle(episode.id) }, modifier,
        metadata = episode.episodeNumber?.let { "الحلقة $it" }.orEmpty(),
        trailing = if (onRemoveLater != null) {
            { com.example.feature.learning.presentation.components.WatchLaterButton(true, onRemoveLater) }
        } else null)
}
