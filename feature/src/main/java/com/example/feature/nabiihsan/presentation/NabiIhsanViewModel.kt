package com.example.feature.nabiihsan.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.nabiihsan.domain.model.*
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.learning.data.LearningProgressStore
import com.example.feature.learning.data.LearningEpisodeProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class NabiIhsanViewModel(
    private val repository: NabiIhsanRepository,
    private val connectivityMonitor: ConnectivityMonitor? = null,
    private val handle: SavedStateHandle = SavedStateHandle(),
    private val progressStore: LearningProgressStore? = null
) : ViewModel() {
    private val query = MutableStateFlow(handle.get<String>("query") ?: "")
    private val filter = MutableStateFlow(NabiIhsanFilterState(
        selectedCategory = handle["category"] ?: "الكل", selectedEpisodeFilter = handle["episodes"] ?: "الكل",
        selectedDurationFilter = handle["duration"] ?: "الكل", sortOption = NabiSortOption.entries.find { it.name == handle.get<String>("sort") } ?: NabiSortOption.NEWEST_FIRST))
    private val sheet = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    private val _episodeDetailState = MutableStateFlow(NabiEpisodeDetailUiState())
    val episodeDetailState = _episodeDetailState.asStateFlow()
    private val _recipeDetailState = MutableStateFlow(NabiRecipeDetailUiState())
    val recipeDetailState = _recipeDetailState.asStateFlow()
    private val _searchUiState = MutableStateFlow(NabiSearchUiState(query = query.value, filterState = filter.value))
    val searchUiState = _searchUiState.asStateFlow()
    private val _favoritesTab = MutableStateFlow((handle.get<Int>("tab") ?: 0).coerceIn(0,1))
    val favoritesTab = _favoritesTab.asStateFlow()
    private val _settingsState = MutableStateFlow(NabiSettingsUiState())
    val settingsState = _settingsState.asStateFlow()
    val isOnline = connectivityMonitor?.isOnline ?: MutableStateFlow(true).asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()
    private val _progress = MutableStateFlow<Map<String, LearningEpisodeProgress>>(emptyMap())
    val progress = _progress.asStateFlow()
    private val _progressReady = MutableStateFlow(false)
    val progressReady = _progressReady.asStateFlow()
    private var progressJob: Job? = null
    private var detailJob: Job? = null
    private var recipeJob: Job? = null
    private var searchJob: Job? = null
    private var activeEpisode: String? = null
    private var activeRecipe: String? = null

    val mainUiState = refresh.flatMapLatest {
        combine(repository.observeEpisodes(), repository.observeRecipes(), query, filter, sheet) { episodes, recipes, q, f, visible ->
            NabiIhsanMainUiState(episodes = episodes, recipes = recipes, searchQuery = q, filterState = f, isFilterSheetVisible = visible)
        }.mapLatest { state -> state.copy(filteredEpisodes = repository.search(state.searchQuery, state.filterState).episodes) }
            .onStart { emit(NabiIhsanMainUiState(searchQuery = query.value, filterState = filter.value, isLoading = true)) }
            .catch { error -> if (error is CancellationException) throw error
                emit(NabiIhsanMainUiState(searchQuery = query.value, filterState = filter.value, errorMessage = "تعذر قراءة مكتبة نبي الإحسان")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NabiIhsanMainUiState(isLoading = true))
    val favoriteEpisodes = mainUiState.map { it.episodes.filter { ep -> ep.isFavorite } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteRecipes = mainUiState.map { it.recipes.filter { recipe -> recipe.isFavorite } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchLaterEpisodes = mainUiState.map { it.episodes.filter { ep -> ep.isWatchLater } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRecipes = mainUiState.map { it.recipes }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { observeProgress() }
    private fun observeProgress() {
        progressJob?.cancel()
        _progressReady.value = false
        progressJob = progressStore?.observe("nabi")?.onEach { _progress.value = it; _progressReady.value = true; _actionError.value = null }
            ?.catch { error -> if (error is CancellationException) throw error; _progressReady.value = false; _actionError.value = "تعذر قراءة تقدم التعلم؛ أعد المحاولة قبل تعديله" }?.launchIn(viewModelScope)
    }
    fun retry() { refresh.value++; observeProgress() }
    fun onSearchQueryChanged(value: String) { query.value = value; handle["query"] = value; performSearch(value, filter.value) }
    fun initializeSearch(value: String) { if (!handle.contains("query")) onSearchQueryChanged(value) else performSearch(query.value, filter.value) }
    fun onFilterStateChanged(value: NabiIhsanFilterState) {
        filter.value = value
        handle["category"] = value.selectedCategory; handle["episodes"] = value.selectedEpisodeFilter
        handle["duration"] = value.selectedDurationFilter; handle["sort"] = value.sortOption.name
        performSearch(query.value, value)
    }
    fun setFilterSheetVisible(visible: Boolean) { sheet.value = visible }
    fun performSearch(value: String, filterState: NabiIhsanFilterState) {
        query.value = value; handle["query"] = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchUiState.value = NabiSearchUiState(query = value, filterState = filterState, isLoading = true)
            try { val result = repository.search(value, filterState)
                ensureActive(); _searchUiState.value = NabiSearchUiState(query = value, filterState = filterState, searchResult = result)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _searchUiState.update { it.copy(isLoading = false, errorMessage = "تعذر تحميل نتائج البحث") } }
        }
    }
    private fun mutate(action: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true; _actionError.value = null
        viewModelScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _actionError.value = "تعذر حفظ التغيير محليًا؛ أعد المحاولة" }
            finally { _busy.value = false }
        }
    }
    fun toggleEpisodeFavorite(id: String) = mutate { repository.toggleEpisodeFavorite(id); if (activeEpisode == id) loadEpisodeDetail(id) }
    fun toggleEpisodeWatchLater(id: String) = mutate { repository.toggleEpisodeWatchLater(id); if (activeEpisode == id) loadEpisodeDetail(id) }
    fun toggleRecipeFavorite(id: String) = mutate { repository.toggleRecipeFavorite(id); if (activeRecipe == id) loadRecipeDetail(id) }
    fun saveProgress(id: String, seconds: Int, completed: Boolean) = mutate {
        check(_progressReady.value) { "Progress is not loaded" }
        requireNotNull(repository.getEpisodeById(id))
        requireNotNull(progressStore).save("nabi", id, id, seconds, completed)
    }
    fun loadEpisodeDetail(id: String) {
        activeEpisode = id; detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _episodeDetailState.value = NabiEpisodeDetailUiState(isLoading = true)
            try {
                val episode = repository.getEpisodeById(id) ?: error("Missing episode")
                val recipes = repository.getRecipesForEpisode(id)
                val all = repository.search("", NabiIhsanFilterState(sortOption = NabiSortOption.SERIES_ORDER)).episodes
                val index = all.indexOfFirst { it.id == id }
                ensureActive()
                _episodeDetailState.value = NabiEpisodeDetailUiState(episode, recipes, all.getOrNull(index - 1)?.id, all.getOrNull(index + 1)?.id)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _episodeDetailState.value = NabiEpisodeDetailUiState(errorMessage = "الحلقة غير متاحة أو تعذر تحميل بياناتها") }
        }
    }
    fun loadRecipeDetail(id: String) {
        activeRecipe = id; recipeJob?.cancel()
        recipeJob = viewModelScope.launch {
            _recipeDetailState.value = NabiRecipeDetailUiState(isLoading = true)
            try {
                val recipe = repository.getRecipeById(id) ?: error("Missing recipe")
                val related = repository.getEpisodesForRecipe(id)
                ensureActive(); _recipeDetailState.value = NabiRecipeDetailUiState(recipe, related)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _recipeDetailState.value = NabiRecipeDetailUiState(errorMessage = "الوصفة غير متاحة أو تعذر تحميل بياناتها") }
        }
    }
    fun setFavoritesTab(index: Int) { _favoritesTab.value = index.coerceIn(0,1); handle["tab"] = _favoritesTab.value }
    fun updateDownloadWifiOnly(enabled: Boolean) { _settingsState.update { it.copy(downloadWifiOnly = enabled) } }
    fun updateNewEpisodesNotification(enabled: Boolean) { _settingsState.update { it.copy(newEpisodesNotification = enabled) } }
    fun updateVideoQuality(quality: String) { _settingsState.update { it.copy(videoQuality = quality) } }
}
