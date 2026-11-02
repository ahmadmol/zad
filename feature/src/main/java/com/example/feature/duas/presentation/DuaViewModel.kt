package com.example.feature.duas.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.duas.domain.repository.DuaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuaViewModel @JvmOverloads constructor(
    private val repository: DuaRepository,
    private val userPreferences: UserPreferences,
    private val handle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(handle.get<String>("category"))
    private val _searchQuery = MutableStateFlow(handle.get<String>("query") ?: "")
    private val _showFavoritesOnly = MutableStateFlow(handle.get<Boolean>("favorites") ?: false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val refresh = MutableStateFlow(0)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DuaUiState> = refresh.flatMapLatest { combine(
        flow { emitAll(repository.getAllDuas()) }.onStart { repository.loadDuasIfNeeded() },
        _selectedCategory,
        _searchQuery,
        _showFavoritesOnly,
        combine(_isLoading, _errorMessage) { loading, error -> loading to error }
    ) { allDuas, category, query, favoritesOnly, loadState ->
        val filtered = allDuas.filter { dua ->
            (category == null || DuaSections.matches(category, dua.category)) &&
            com.example.feature.duas.domain.model.DuaSearch.matches(dua.title, dua.text, dua.category, query) &&
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
    }.onStart { emit(DuaUiState(isLoading = true, searchQuery = _searchQuery.value)) }
      .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error
          emit(DuaUiState(errorMessage = "تعذر قراءة الأدعية؛ أعد المحاولة", searchQuery = _searchQuery.value, selectedCategory = _selectedCategory.value, showFavoritesOnly = _showFavoritesOnly.value)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DuaUiState(isLoading = true)
    )

    private fun loadData() { _errorMessage.value = null; refresh.value++ }

    fun onAction(action: DuaAction) {
        when (action) {
            is DuaAction.OnCategorySelected -> { _selectedCategory.value = action.category; handle["category"] = action.category }
            is DuaAction.OnSearchQueryChanged -> { _searchQuery.value = action.query; handle["query"] = action.query }
            is DuaAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    try { repository.toggleFavorite(action.id, action.isFavorite); _errorMessage.value = null }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { _errorMessage.value = "تعذر حفظ المفضلة؛ أعد المحاولة" }
                }
            }
            is DuaAction.OnToggleFavoritesOnly -> { _showFavoritesOnly.value = action.show; handle["favorites"] = action.show }
            is DuaAction.OnDuaOpened -> {
                // Keep the event for callers; completion belongs to the user's explicit checklist.
            }
            DuaAction.Refresh -> loadData()
        }
    }
}
