package com.example.feature.duas.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.duas.domain.repository.DuaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuaViewModel(
    private val repository: DuaRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<DuaUiState> = combine(
        repository.getAllDuas(),
        _selectedCategory,
        _searchQuery,
        _showFavoritesOnly,
        _isLoading
    ) { allDuas, category, query, favoritesOnly, loading ->
        val filtered = allDuas.filter { dua ->
            (category == null || dua.category == category) &&
            (query.isBlank() || dua.title.contains(query, ignoreCase = true) || dua.text.contains(query, ignoreCase = true)) &&
            (!favoritesOnly || dua.isFavorite)
        }
        DuaUiState(
            allDuas = allDuas,
            duas = filtered,
            isLoading = loading,
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
            repository.loadDuasIfNeeded()
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
            DuaAction.Refresh -> loadData()
        }
    }
}
