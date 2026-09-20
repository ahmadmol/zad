package com.example.feature.fahmanallah.domain.model

enum class FahmEpisodeProgressState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

data class FahmEpisode(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String? = null,
    val videoId: String? = null,
    val durationText: String? = null,
    val coverUrl: String = "",
    val publishedAt: String? = null,
    val sourceUrl: String? = null,
    val playlistId: String? = null,
    val playlistPosition: Int? = null,
    val sourceChannelId: String? = null,
    val ruleTitle: String? = null,
    val ruleText: String? = null,
    val understandText: String? = null,
    val takeaways: List<String> = emptyList(),
    val practicalAction: String? = null,
    val quote: String? = null,
    val quoteSource: String? = null,
    val isEmbeddable: Boolean = false,
    val stationId: String? = null,
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false,
    val progressState: FahmEpisodeProgressState = FahmEpisodeProgressState.NOT_STARTED,
    val lastPlayedSeconds: Int = 0
)

data class FahmStation(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String = "",
    val relatedEpisodeIds: List<String> = emptyList()
)

data class FahmJourneyProgress(
    val completedCount: Int,
    val totalEpisodes: Int = 29,
    val percentage: Int,
    val continueEpisode: FahmEpisode? = null
)
