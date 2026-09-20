package com.example.feature.duas.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.duas.domain.repository.DuaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuaViewModel(
    private val repository: DuaRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DuaUiState> = combine(
        repository.getAllDuas(),
        _selectedCategory,
        _searchQuery,
        _showFavoritesOnly,
        combine(_isLoading, _errorMessage) { loading, error -> loading to error }
    ) { allDuas, category, query, favoritesOnly, loadState ->
        val filtered = allDuas.filter { dua ->
            (category == null || dua.category == category) &&
            (query.isBlank() ||
                dua.title.contains(query, ignoreCase = true) ||
                dua.text.contains(query, ignoreCase = true) ||
                dua.category.contains(query, ignoreCase = true)) &&
            (!favoritesOnly || dua.isFavorite)
        }
        DuaUiState(
            allDuas = allDuas,
            duas = filtered,
            isLoading = loadState.first,
            errorMessage = loadState.second,
            selectedCategory = category,
            searchQuery = query,
            showFavoritesOnly = favoritesOnly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DuaUiState(isLoading = true)
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { repository.loadDuasIfNeeded() }
                .onFailure { _errorMessage.value = "load_failed" }
            _isLoading.value = false
        }
    }

    fun onAction(action: DuaAction) {
        when (action) {
            is DuaAction.OnCategorySelected -> _selectedCategory.value = action.category
            is DuaAction.OnSearchQueryChanged -> _searchQuery.value = action.query
            is DuaAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(action.id, action.isFavorite)
                }
            }
            is DuaAction.OnToggleFavoritesOnly -> _showFavoritesOnly.value = action.show
            is DuaAction.OnDuaOpened -> {
                viewModelScope.launch {
                    userPreferences.markDailyActivityComplete(DailyActivityIds.DAILY_DUA)
                }
            }
            DuaAction.Refresh -> loadData()
        }
    }
}
