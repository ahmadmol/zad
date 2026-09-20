package com.example.feature.nabiihsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.nabiihsan.domain.model.NabiIhsanFilterState
import com.example.feature.nabiihsan.domain.model.NabiSearchResult
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NabiIhsanViewModel(
    private val repository: NabiIhsanRepository,
    private val connectivityMonitor: ConnectivityMonitor? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(NabiIhsanFilterState())
    private val _isFilterSheetVisible = MutableStateFlow(false)

    private val _episodeDetailState = MutableStateFlow(NabiEpisodeDetailUiState())
    val episodeDetailState: StateFlow<NabiEpisodeDetailUiState> = _episodeDetailState.asStateFlow()

    private val _recipeDetailState = MutableStateFlow(NabiRecipeDetailUiState())
    val recipeDetailState: StateFlow<NabiRecipeDetailUiState> = _recipeDetailState.asStateFlow()

    private val _searchUiState = MutableStateFlow(NabiSearchUiState())
    val searchUiState: StateFlow<NabiSearchUiState> = _searchUiState.asStateFlow()

    private val _favoritesTab = MutableStateFlow(0)
    val favoritesTab: StateFlow<Int> = _favoritesTab.asStateFlow()

    private val _settingsState = MutableStateFlow(NabiSettingsUiState())
    val settingsState: StateFlow<NabiSettingsUiState> = _settingsState.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityMonitor?.isOnline
        ?: MutableStateFlow(true).asStateFlow()

    val favoriteEpisodes = repository.observeFavoriteEpisodes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val favoriteRecipes = repository.observeFavoriteRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val watchLaterEpisodes = repository.observeWatchLaterEpisodes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val allRecipes = repository.observeRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
            initialValue = emptyList()
        )

    val mainUiState: StateFlow<NabiIhsanMainUiState> = combine(
        repository.observeEpisodes(),
        repository.observeRecipes(),
        _searchQuery,
        _filterState,
        _isFilterSheetVisible
    ) { allEpisodes, recipes, query, filter, isSheetVisible ->
        val searchResult = repository.search(query, filter)

        NabiIhsanMainUiState(
            episodes = allEpisodes,
            filteredEpisodes = searchResult.episodes,
            recipes = recipes,
            searchQuery = query,
            filterState = filter,
            isFilterSheetVisible = isSheetVisible,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubsubscribedOrFiveSeconds(),
        initialValue = NabiIhsanMainUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        performSearch(query, _filterState.value)
    }

    fun onFilterStateChanged(filterState: NabiIhsanFilterState) {
        _filterState.value = filterState
        performSearch(_searchQuery.value, filterState)
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _isFilterSheetVisible.value = visible
    }

    fun performSearch(query: String, filterState: NabiIhsanFilterState) {
        viewModelScope.launch {
            _searchUiState.update { it.copy(isLoading = true, query = query, filterState = filterState) }
            val result = repository.search(query, filterState)
            _searchUiState.update {
                it.copy(
                    query = query,
                    filterState = filterState,
                    searchResult = result,
                    isLoading = false
                )
            }
        }
    }

    fun toggleEpisodeFavorite(episodeId: String) {
        viewModelScope.launch {
            repository.toggleEpisodeFavorite(episodeId)
            refreshEpisodeDetailIfActive(episodeId)
        }
    }

    fun toggleRecipeFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.toggleRecipeFavorite(recipeId)
            refreshRecipeDetailIfActive(recipeId)
        }
    }

    fun toggleEpisodeWatchLater(episodeId: String) {
        viewModelScope.launch {
            repository.toggleEpisodeWatchLater(episodeId)
            refreshEpisodeDetailIfActive(episodeId)
        }
    }

    fun loadEpisodeDetail(episodeId: String) {
        viewModelScope.launch {
            _episodeDetailState.update { it.copy(isLoading = true) }
            val ep = repository.getEpisodeById(episodeId)
            if (ep != null) {
                val linkedRecipes = repository.getRecipesForEpisode(episodeId)
                val allEpisodes = repository.search("", NabiIhsanFilterState()).episodes
                val currentIndex = allEpisodes.indexOfFirst { it.id == episodeId }
                val prevId = if (currentIndex > 0) allEpisodes[currentIndex - 1].id else null
                val nextId = if (currentIndex in 0 until allEpisodes.size - 1) allEpisodes[currentIndex + 1].id else null

                _episodeDetailState.update {
                    it.copy(
                        episode = ep,
                        linkedRecipes = linkedRecipes,
                        previousEpisodeId = prevId,
                        nextEpisodeId = nextId,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } else {
                _episodeDetailState.update {
                    it.copy(isLoading = false, errorMessage = "الحلقة غير موجودة")
                }
            }
        }
    }

    fun loadRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _recipeDetailState.update { it.copy(isLoading = true) }
            val recipe = repository.getRecipeById(recipeId)
            if (recipe != null) {
                val relatedEps = repository.getEpisodesForRecipe(recipeId)
                _recipeDetailState.update {
                    it.copy(
                        recipe = recipe,
                        relatedEpisodes = relatedEps,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } else {
                _recipeDetailState.update {
                    it.copy(isLoading = false, errorMessage = "الوصفة غير موجودة")
                }
            }
        }
    }

    fun setFavoritesTab(tabIndex: Int) {
        _favoritesTab.value = tabIndex
    }

    fun updateDownloadWifiOnly(enabled: Boolean) {
        _settingsState.update { it.copy(downloadWifiOnly = enabled) }
    }

    fun updateNewEpisodesNotification(enabled: Boolean) {
        _settingsState.update { it.copy(newEpisodesNotification = enabled) }
    }

    fun updateVideoQuality(quality: String) {
        _settingsState.update { it.copy(videoQuality = quality) }
    }

    private suspend fun refreshEpisodeDetailIfActive(episodeId: String) {
        val currentEp = _episodeDetailState.value.episode
        if (currentEp != null && currentEp.id == episodeId) {
            val updated = repository.getEpisodeById(episodeId)
            if (updated != null) {
                _episodeDetailState.update { it.copy(episode = updated) }
            }
        }
    }

    private suspend fun refreshRecipeDetailIfActive(recipeId: String) {
        val currentRecipe = _recipeDetailState.value.recipe
        if (currentRecipe != null && currentRecipe.id == recipeId) {
            val updated = repository.getRecipeById(recipeId)
            if (updated != null) {
                _recipeDetailState.update { it.copy(recipe = updated) }
            }
        }
    }
}

private fun SharingStarted.Companion.WhileSubsubscribedOrFiveSeconds(): SharingStarted =
    WhileSubscribed(5000)
