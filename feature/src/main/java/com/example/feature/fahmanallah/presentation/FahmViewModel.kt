package com.example.feature.fahmanallah.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FahmViewModel(
    private val repository: FahmRepository,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    private val _mainState = MutableStateFlow(FahmMainUiState())
    val mainState: StateFlow<FahmMainUiState> = _mainState.asStateFlow()

    private val _episodesState = MutableStateFlow(FahmEpisodesUiState())
    val episodesState: StateFlow<FahmEpisodesUiState> = _episodesState.asStateFlow()

    private val _detailState = MutableStateFlow(FahmEpisodeDetailUiState())
    val detailState: StateFlow<FahmEpisodeDetailUiState> = _detailState.asStateFlow()

    private val _searchState = MutableStateFlow(FahmSearchUiState())
    val searchState: StateFlow<FahmSearchUiState> = _searchState.asStateFlow()

    private val _savedState = MutableStateFlow(FahmSavedUiState())
    val savedState: StateFlow<FahmSavedUiState> = _savedState.asStateFlow()

    private val _settingsState = MutableStateFlow(FahmSettingsUiState())
    val settingsState: StateFlow<FahmSettingsUiState> = _settingsState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            repository.getEpisodes(),
            repository.getStations(),
            repository.getJourneyProgress(),
            connectivityMonitor.isOnline
        ) { episodes, stations, progress, isOnline ->
            _mainState.update {
                it.copy(
                    episodes = episodes,
                    stations = stations,
                    journeyProgress = progress,
                    continueEpisode = progress.continueEpisode
                )
            }
            _episodesState.update {
                it.copy(
                    episodes = episodes,
                    stations = stations,
                    savedEpisodes = episodes.filter { ep -> ep.isFavorite || ep.isWatchLater }
                )
            }
            _savedState.update {
                it.copy(
                    favoriteEpisodes = episodes.filter { ep -> ep.isFavorite },
                    watchLaterEpisodes = episodes.filter { ep -> ep.isWatchLater }
                )
            }
            _detailState.update {
                it.copy(isOnline = isOnline)
            }
        }.launchIn(viewModelScope)
    }

    fun loadEpisodeDetail(episodeId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            repository.getEpisodes().collect { list ->
                val ep = list.find { it.id == episodeId || it.number.toString() == episodeId }
                if (ep != null) {
                    val currentIndex = list.indexOfFirst { it.id == ep.id }
                    val prevId = if (currentIndex > 0) list[currentIndex - 1].id else null
                    val nextId = if (currentIndex < list.size - 1) list[currentIndex + 1].id else null
                    
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            episode = ep,
                            previousEpisodeId = prevId,
                            nextEpisodeId = nextId,
                            error = null
                        )
                    }
                } else {
                    _detailState.update {
                        it.copy(isLoading = false, error = "الدرس غير موجود")
                    }
                }
            }
        }
    }

    fun setEpisodesTab(tabIndex: Int) {
        _episodesState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setSavedTab(tabIndex: Int) {
        _savedState.update { it.copy(selectedTab = tabIndex) }
    }

    fun search(query: String) {
        _searchState.update { it.copy(query = query, isLoading = true) }
        viewModelScope.launch {
            repository.searchEpisodes(query).collect { results ->
                _searchState.update {
                    it.copy(isLoading = false, results = results)
                }
            }
        }
    }

    fun toggleFavorite(episodeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(episodeId)
        }
    }

    fun toggleWatchLater(episodeId: String) {
        viewModelScope.launch {
            repository.toggleWatchLater(episodeId)
        }
    }

    fun markCompleted(episodeId: String) {
        viewModelScope.launch {
            repository.markCompleted(episodeId)
        }
    }

    fun updateEpisodeProgress(episodeId: String, state: FahmEpisodeProgressState, lastPlayedSeconds: Int = 0) {
        viewModelScope.launch {
            repository.updateEpisodeProgress(episodeId, state, lastPlayedSeconds)
        }
    }

    fun resetFeatureData() {
        viewModelScope.launch {
            repository.resetFeatureData()
        }
    }
}
