package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.MediaCard
import com.example.feature.sanhya.domain.model.QuranStory

@Composable
fun QuranStoryCard(story: QuranStory, onClick: () -> Unit, onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier) {
    MediaCard(story.title, story.subtitle, story.coverUrl,
        if (story.totalEpisodesCount > 1) "${story.totalEpisodesCount} حلقات" else story.episodes.firstOrNull()?.durationLabel,
        story.isFavorite, onClick, onFavoriteClick, modifier, story.formattedSurahLabel)
}
