package com.example.feature.asma.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.asma.domain.model.AllahName
import com.example.feature.asma.domain.repository.AsmaRepository
import com.example.feature.asma.domain.util.AsmaTodayResolver
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AsmaViewModel @JvmOverloads constructor(
    private val repository: AsmaRepository,
    private val userPreferences: UserPreferences,
    private val handle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AsmaUiState(searchQuery = handle["query"] ?: "", showFavoritesOnly = handle["favorites"] ?: false))
    val uiState: StateFlow<AsmaUiState> = _uiState.asStateFlow()

    private var readJob: kotlinx.coroutines.Job? = null
    init {
        loadAsma()
    }

    private fun loadAsma() {
        readJob?.cancel()
        readJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.loadAsma()
                repository.getAllAsma().collect { names ->
                    _uiState.update { state ->
                        val selected = names.find { it.id == (state.selectedName?.id ?: handle.get<Int>("selected")) }
                            ?: AsmaTodayResolver.selectDailyName(names) ?: names.firstOrNull()
                        state.copy(asmaList = names, visibleAsmaList = if (state.showFavoritesOnly) names.filter { it.isFavorite } else names,
                            searchResults = filterNames(names, state.searchQuery), selectedName = selected, isLoading = false, error = null)
                    }
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { _uiState.update { it.copy(isLoading = false, error = "تعذر قراءة أسماء الله الحسنى؛ أعد المحاولة") } }
        }
    }

    fun onAction(action: AsmaAction) {
        when (action) {
            is AsmaAction.OnRetry -> loadAsma()
            is AsmaAction.OnNameClick -> {
                handle["selected"] = action.name.id
                _uiState.update {
                    it.copy(
                        selectedName = action.name,
                        searchQuery = "",
                        searchResults = emptyList()
                    )
                }
                // Opening content does not imply the user's daily activity is complete.
            }
            is AsmaAction.OnSearchQueryChange -> {
                val query = action.query
                handle["query"] = query
                val filtered = filterNames(_uiState.value.asmaList, query)
                _uiState.update {
                    it.copy(
                        searchQuery = query,
                        searchResults = filtered
                    )
                }
            }
            AsmaAction.OnSelectNext -> {
                val list = _uiState.value.asmaList
                if (list.isNotEmpty()) {
                    val current = _uiState.value.selectedName
                    val currentIndex = list.indexOfFirst { it.id == current?.id }
                    val nextIndex = if (currentIndex != -1) (currentIndex + 1) % list.size else 0
                    val nextName = list[nextIndex]
                    _uiState.update { it.copy(selectedName = nextName) }
                }
            }
            AsmaAction.OnSelectPrevious -> {
                val list = _uiState.value.asmaList
                if (list.isNotEmpty()) {
                    val current = _uiState.value.selectedName
                    val currentIndex = list.indexOfFirst { it.id == current?.id }
                    val prevIndex = if (currentIndex != -1) {
                        (currentIndex - 1 + list.size) % list.size
                    } else 0
                    val prevName = list[prevIndex]
                    _uiState.update { it.copy(selectedName = prevName) }
                }
            }
            is AsmaAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    try { repository.toggleFavorite(action.id) }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { _uiState.update { it.copy(error = "تعذر حفظ المفضلة؛ أعد المحاولة") } }
                }
            }
            is AsmaAction.OnToggleFavoritesOnly -> {
                handle["favorites"] = action.show
                val visibleAsmaList = if (action.show) {
                    _uiState.value.asmaList.filter { it.isFavorite }
                } else {
                    _uiState.value.asmaList
                }
                _uiState.update {
                    it.copy(
                        showFavoritesOnly = action.show,
                        visibleAsmaList = visibleAsmaList
                    )
                }
            }
            AsmaAction.OnOpenDetails -> {
                _uiState.update { it.copy(isDetailsOpen = true) }
            }
            AsmaAction.OnDismissDetails -> {
                _uiState.update { it.copy(isDetailsOpen = false) }
            }
        }
    }

    private fun filterNames(list: List<AllahName>, query: String): List<AllahName> {
        if (query.isBlank() || com.example.feature.core.search.ArabicSearch.key(query).isBlank()) return emptyList()
        val cleanQuery = normalizeArabic(query.trim())
        return list.filter { item ->
            normalizeArabic(item.name).contains(cleanQuery, ignoreCase = true) ||
                    normalizeArabic(item.explanation).contains(cleanQuery, ignoreCase = true) ||
                    item.transliteration.contains(query.trim(), ignoreCase = true) ||
                    item.meaning.contains(query.trim(), ignoreCase = true)
        }
    }

    private fun normalizeArabic(text: String): String {
        return text
            .replace(Regex("[\u064B-\u0652]"), "")
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
    }
}
