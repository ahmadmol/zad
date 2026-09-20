package com.example.feature.sanhya.presentation

import com.example.feature.quran.domain.model.Verse
import com.example.feature.sanhya.domain.model.QuranStory
import com.example.feature.sanhya.domain.model.StoryFilterState

data class SanhyaMainUiState(
    val stories: List<QuranStory> = emptyList(),
    val filteredStories: List<QuranStory> = emptyList(),
    val searchQuery: String = "",
    val filterState: StoryFilterState = StoryFilterState(),
    val isFilterSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SanhyaDetailUiState(
    val story: QuranStory? = null,
    val selectedEpisodeId: String? = null,
    val quranVerses: List<Verse> = emptyList(),
    val isLoadingVerses: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SanhyaSettingsUiState(
    val videoQuality: String = "تلقائية (موصى بها)",
    val downloadWifiOnly: Boolean = true,
    val newEpisodesNotification: Boolean = true
)
