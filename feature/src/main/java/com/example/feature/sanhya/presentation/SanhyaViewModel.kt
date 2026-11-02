package com.example.feature.sanhya.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.sanhya.domain.model.*
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import com.example.feature.learning.data.LearningProgressStore
import com.example.feature.learning.data.LearningEpisodeProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class SanhyaViewModel(
    private val sanhyaRepository: SanhyaRepository,
    private val quranRepository: QuranRepository,
    connectivityMonitor: ConnectivityMonitor? = null,
    private val handle: SavedStateHandle = SavedStateHandle(),
    private val progressStore: LearningProgressStore? = null
) : ViewModel() {
    val isOnline = connectivityMonitor?.isOnline ?: MutableStateFlow(true).asStateFlow()
    private val query = MutableStateFlow(handle.get<String>("query") ?: "")
    private val filter = MutableStateFlow(StoryFilterState(
        selectedCategory = handle["category"] ?: "الكل", selectedEpisodeFilter = handle["episodes"] ?: "الكل",
        selectedDurationFilter = handle["duration"] ?: "الكل", sortOption = StorySortOption.entries.find { it.name == handle.get<String>("sort") } ?: StorySortOption.NEWEST_FIRST))
    private val sheet = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    private val _detailState = MutableStateFlow(SanhyaDetailUiState())
    val detailState = _detailState.asStateFlow()
    private val _settingsState = MutableStateFlow(SanhyaSettingsUiState())
    val settingsState = _settingsState.asStateFlow()
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
    private var activeStory: String? = null

    val mainUiState = refresh.flatMapLatest {
        combine(sanhyaRepository.observeStories(), query, filter, sheet) { stories, q, f, visible ->
            SanhyaMainUiState(stories = stories, searchQuery = q, filterState = f, isFilterSheetVisible = visible)
        }.mapLatest { state -> state.copy(filteredStories = sanhyaRepository.filterStories(state.searchQuery, state.filterState)) }
            .onStart { emit(SanhyaMainUiState(searchQuery = query.value, filterState = filter.value, isLoading = true)) }
            .catch { error -> if (error is CancellationException) throw error
                emit(SanhyaMainUiState(searchQuery = query.value, filterState = filter.value, errorMessage = "تعذر قراءة مكتبة سنحيا بالقرآن")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SanhyaMainUiState(isLoading = true))
    val favoritesStories = mainUiState.map { it.stories.filter { story -> story.isFavorite } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchLaterStories = mainUiState.map { it.stories.filter { story -> story.isInWatchLater } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { observeProgress() }
    private fun observeProgress() {
        progressJob?.cancel()
        _progressReady.value = false
        progressJob = progressStore?.observe("sanhya")?.onEach { _progress.value = it; _progressReady.value = true; _actionError.value = null }
            ?.catch { error -> if (error is CancellationException) throw error; _progressReady.value = false; _actionError.value = "تعذر قراءة تقدم التعلم؛ أعد المحاولة قبل تعديله" }?.launchIn(viewModelScope)
    }
    fun retry() { refresh.value++; observeProgress() }
    fun onSearchQueryChanged(value: String) { query.value = value; handle["query"] = value }
    fun initializeSearch(value: String) { if (!handle.contains("query")) onSearchQueryChanged(value) }
    fun onFilterStateChanged(value: StoryFilterState) {
        filter.value = value
        handle["category"] = value.selectedCategory; handle["episodes"] = value.selectedEpisodeFilter
        handle["duration"] = value.selectedDurationFilter; handle["sort"] = value.sortOption.name
    }
    fun setFilterSheetVisible(visible: Boolean) { sheet.value = visible }
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
    fun toggleFavorite(id: String) = mutate { sanhyaRepository.toggleFavorite(id); if (activeStory == id) loadStoryDetails(id) }
    fun toggleWatchLater(id: String) = mutate { sanhyaRepository.toggleWatchLater(id); if (activeStory == id) loadStoryDetails(id) }
    fun saveProgress(id: String, seconds: Int, completed: Boolean) = mutate {
        check(_progressReady.value) { "Progress is not loaded" }
        val story = requireNotNull(_detailState.value.story)
        require(story.episodes.any { it.id == id })
        requireNotNull(progressStore).save("sanhya", id, story.id, seconds, completed)
    }
    fun loadStoryDetails(id: String) {
        activeStory = id; detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailState.value = SanhyaDetailUiState(isLoading = true)
            try {
                val story = sanhyaRepository.getStoryById(id) ?: error("Missing story")
                val savedProgress = try { progressStore?.observe("sanhya")?.first() ?: _progress.value }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (error: Exception) { _actionError.value = "تعذر قراءة تقدم التعلم؛ أعد المحاولة"; _progress.value }
                val stored = handle.get<String>("episode:$id")
                val last = savedProgress.values.filter { it.parentId == id }.maxByOrNull { it.updatedAt }?.id
                val selected = (stored ?: last).takeIf { chosen -> story.episodes.any { it.id == chosen } } ?: story.episodes.firstOrNull()?.id
                ensureActive(); _detailState.value = SanhyaDetailUiState(story = story, selectedEpisodeId = selected)
                val ref = story.quranReferences.firstOrNull() ?: return@launch
                _detailState.update { it.copy(isLoadingVerses = true) }
                try {
                    val ayahs = quranRepository.getAyahsBySurah(ref.surahId).filter { it.verseNumber in ref.startAyah..ref.endAyah }
                    ensureActive(); _detailState.update { it.copy(quranVerses = ayahs, isLoadingVerses = false) }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { _detailState.update { it.copy(isLoadingVerses = false, versesError = "تعذر تحميل الآيات المرتبطة؛ أعد المحاولة") } }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _detailState.value = SanhyaDetailUiState(errorMessage = "القصة غير متاحة أو تعذر تحميل بياناتها") }
        }
    }
    fun selectEpisode(id: String) {
        val story = _detailState.value.story ?: return
        if (story.episodes.none { it.id == id }) return
        handle["episode:${story.id}"] = id
        _detailState.update { it.copy(selectedEpisodeId = id) }
    }
    fun updateVideoQuality(quality: String) { _settingsState.update { it.copy(videoQuality = quality) } }
    fun updateDownloadWifiOnly(enabled: Boolean) { _settingsState.update { it.copy(downloadWifiOnly = enabled) } }
    fun updateNewEpisodesNotification(enabled: Boolean) { _settingsState.update { it.copy(newEpisodesNotification = enabled) } }
}
