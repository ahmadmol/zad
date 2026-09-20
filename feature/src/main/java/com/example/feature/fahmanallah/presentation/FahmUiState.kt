package com.example.feature.fahmanallah.presentation

import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmJourneyProgress
import com.example.feature.fahmanallah.domain.model.FahmStation

data class FahmMainUiState(
    val isLoading: Boolean = false,
    val episodes: List<FahmEpisode> = emptyList(),
    val stations: List<FahmStation> = emptyList(),
    val journeyProgress: FahmJourneyProgress = FahmJourneyProgress(0, 29, 0),
    val continueEpisode: FahmEpisode? = null,
    val error: String? = null
)

data class FahmEpisodesUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0 = جميع الدروس, 1 = منازل الروح, 2 = المحفوظة
    val episodes: List<FahmEpisode> = emptyList(),
    val stations: List<FahmStation> = emptyList(),
    val savedEpisodes: List<FahmEpisode> = emptyList(),
    val error: String? = null
)

data class FahmEpisodeDetailUiState(
    val isLoading: Boolean = false,
    val episode: FahmEpisode? = null,
    val previousEpisodeId: String? = null,
    val nextEpisodeId: String? = null,
    val isOnline: Boolean = true,
    val error: String? = null
)

data class FahmSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<FahmEpisode> = emptyList(),
    val error: String? = null
)

data class FahmSavedUiState(
    val selectedTab: Int = 0, // 0 = الدروس المحفوظة, 1 = مشاهدة لاحقاً
    val favoriteEpisodes: List<FahmEpisode> = emptyList(),
    val watchLaterEpisodes: List<FahmEpisode> = emptyList()
)

data class FahmSettingsUiState(
    val dailyNotificationsEnabled: Boolean = true,
    val journeyRemindersEnabled: Boolean = true,
    val loadThumbnailsEnabled: Boolean = true,
    val textSize: String = "عادي", // عادي, كبير
    val autoPlayVideo: Boolean = false
)
