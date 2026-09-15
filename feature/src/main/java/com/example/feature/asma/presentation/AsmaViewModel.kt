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

class AsmaViewModel(
    private val repository: AsmaRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(AsmaUiState())
    val uiState: StateFlow<AsmaUiState> = _uiState.asStateFlow()

    init {
        loadAsma()
        observeAsmaData()
    }

    private fun loadAsma() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.loadAsma()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeAsmaData() {
        viewModelScope.launch {
            repository.getAllAsma()
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
                .collect { asmaList ->
                    val visibleAsmaList = if (_uiState.value.showFavoritesOnly) {
                        asmaList.filter { it.isFavorite }
                    } else {
                        asmaList
                    }

                    _uiState.update { currentState ->
                        val currentSelectedId = currentState.selectedName?.id
                        val updatedSelectedName = if (currentSelectedId != null) {
                            asmaList.firstOrNull { it.id == currentSelectedId } ?: currentState.selectedName
                        } else {
                            AsmaTodayResolver.selectDailyName(asmaList) ?: asmaList.firstOrNull()
                        }

                        val filteredSearch = filterNames(asmaList, currentState.searchQuery)

                        currentState.copy(
                            asmaList = asmaList,
                            visibleAsmaList = visibleAsmaList,
                            searchResults = filteredSearch,
                            selectedName = updatedSelectedName,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onAction(action: AsmaAction) {
        when (action) {
            is AsmaAction.OnRetry -> loadAsma()
            is AsmaAction.OnNameClick -> {
                _uiState.update {
                    it.copy(
                        selectedName = action.name,
                        searchQuery = "",
                        searchResults = emptyList()
                    )
                }
                viewModelScope.launch {
                    userPreferences.markDailyActivityComplete(DailyActivityIds.DAILY_NAME)
                }
            }
            is AsmaAction.OnSearchQueryChange -> {
                val query = action.query
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
                    repository.toggleFavorite(action.id)
                }
            }
            is AsmaAction.OnToggleFavoritesOnly -> {
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
        if (query.isBlank()) return emptyList()
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
