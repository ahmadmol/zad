package com.example.feature.sanhya.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.sanhya.domain.model.QuranStory
import com.example.feature.sanhya.domain.model.StoryFilterState
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SanhyaViewModel(
    private val sanhyaRepository: SanhyaRepository,
    private val quranRepository: QuranRepository,
    connectivityMonitor: ConnectivityMonitor? = null
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityMonitor?.isOnline
        ?: MutableStateFlow(true).asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(StoryFilterState())
    private val _isFilterSheetVisible = MutableStateFlow(false)

    private val _selectedStory = MutableStateFlow<QuranStory?>(null)
    private val _selectedEpisodeId = MutableStateFlow<String?>(null)

    private val _detailState = MutableStateFlow(SanhyaDetailUiState())
    val detailState: StateFlow<SanhyaDetailUiState> = _detailState.asStateFlow()

    private val _settingsState = MutableStateFlow(SanhyaSettingsUiState())
    val settingsState: StateFlow<SanhyaSettingsUiState> = _settingsState.asStateFlow()

    val favoritesStories: StateFlow<List<QuranStory>> = sanhyaRepository.observeFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val watchLaterStories: StateFlow<List<QuranStory>> = sanhyaRepository.observeWatchLater()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val mainUiState: StateFlow<SanhyaMainUiState> = combine(
        sanhyaRepository.observeStories(),
        _searchQuery,
        _filterState,
        _isFilterSheetVisible
    ) { allStories, query, filter, isSheetVisible ->
        val filtered = allStories.filter { story ->
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    story.title.lowercase().contains(q) ||
                    story.subtitle.lowercase().contains(q) ||
                    story.summary.lowercase().contains(q) ||
                    story.surahs.any { it.lowercase().contains(q) } ||
                    story.keywords.any { it.lowercase().contains(q) }

            val matchesCategory = filter.selectedCategory == "الكل" ||
                    story.category == filter.selectedCategory

            val matchesEpisodes = when (filter.selectedEpisodeFilter) {
                "أكثر من حلقة" -> story.episodes.size > 1
                "أكثر من 5 حلقات" -> story.episodes.size > 5
                else -> true
            }

            matchesQuery && matchesCategory && matchesEpisodes
        }

        SanhyaMainUiState(
            stories = allStories,
            filteredStories = filtered,
            searchQuery = query,
            filterState = filter,
            isFilterSheetVisible = isSheetVisible,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
        initialValue = SanhyaMainUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterStateChanged(filterState: StoryFilterState) {
        _filterState.value = filterState
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _isFilterSheetVisible.value = visible
    }

    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            sanhyaRepository.toggleFavorite(storyId)
            _selectedStory.value?.let { story ->
                if (story.id == storyId) {
                    val updated = sanhyaRepository.getStoryById(storyId)
                    _selectedStory.value = updated
                    _detailState.update { it.copy(story = updated) }
                }
            }
        }
    }

    fun toggleWatchLater(storyId: String) {
        viewModelScope.launch {
            sanhyaRepository.toggleWatchLater(storyId)
            _selectedStory.value?.let { story ->
                if (story.id == storyId) {
                    val updated = sanhyaRepository.getStoryById(storyId)
                    _selectedStory.value = updated
                    _detailState.update { it.copy(story = updated) }
                }
            }
        }
    }

    fun loadStoryDetails(storyId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            val story = sanhyaRepository.getStoryById(storyId)
            val firstEpId = story?.episodes?.firstOrNull()?.id
            _selectedStory.value = story
            _selectedEpisodeId.value = firstEpId
            _detailState.update {
                it.copy(
                    story = story,
                    selectedEpisodeId = firstEpId,
                    isLoading = false
                )
            }
            if (story != null && story.quranReferences.isNotEmpty()) {
                loadQuranVersesForStory(story)
            }
        }
    }

    fun selectEpisode(episodeId: String) {
        _selectedEpisodeId.value = episodeId
        _detailState.update { it.copy(selectedEpisodeId = episodeId) }
    }

    private fun loadQuranVersesForStory(story: QuranStory) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoadingVerses = true) }
            val ref = story.quranReferences.firstOrNull()
            if (ref != null) {
                val allAyahs = quranRepository.getAyahsBySurah(ref.surahId)
                val filteredAyahs = allAyahs.filter {
                    it.verseNumber in ref.startAyah..ref.endAyah
                }
                _detailState.update {
                    it.copy(
                        quranVerses = filteredAyahs,
                        isLoadingVerses = false
                    )
                }
            } else {
                _detailState.update { it.copy(quranVerses = emptyList(), isLoadingVerses = false) }
            }
        }
    }

    fun updateVideoQuality(quality: String) {
        _settingsState.update { it.copy(videoQuality = quality) }
    }

    fun updateDownloadWifiOnly(enabled: Boolean) {
        _settingsState.update { it.copy(downloadWifiOnly = enabled) }
    }

    fun updateNewEpisodesNotification(enabled: Boolean) {
        _settingsState.update { it.copy(newEpisodesNotification = enabled) }
    }
}

private fun SharingStarted.Companion.WhileSubsubscribedOrFiveSeconds(): SharingStarted =
    WhileSubscribed(5000)
