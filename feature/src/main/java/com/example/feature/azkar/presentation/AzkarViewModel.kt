package com.example.feature.azkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.repository.AzkarRepository
import com.example.feature.azkar.domain.usecase.*
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AzkarViewModel(
    private val repository: AzkarRepository,
    private val getAzkarUseCase: GetAzkarUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addCustomZikrUseCase: AddCustomZikrUseCase,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _filters = combine(
        _selectedCategory,
        _showFavoritesOnly,
        _searchQuery
    ) { category, favoritesOnly, query ->
        Triple(category, favoritesOnly, query)
    }

    val uiState: StateFlow<AzkarUiState> = combine(
        getAzkarUseCase(),
        _filters,
        settingsManager.fontSizeFlow,
        settingsManager.vibrationEnabledFlow,
        combine(_isLoading, _error) { loading, error -> loading to error }
    ) { allAzkar, filters, fontSize, vibration, loadingError ->
        val (category, favoritesOnly, query) = filters
        val (loading, error) = loadingError

        val filtered = allAzkar.filter { zikr ->
            (category == null || zikr.category == category) &&
                (!favoritesOnly || zikr.isFavorite) &&
                (query.isBlank() ||
                    zikr.title.contains(query, ignoreCase = true) ||
                    zikr.text.contains(query, ignoreCase = true) ||
                    zikr.category.contains(query, ignoreCase = true))
        }

        AzkarUiState(
            azkarList = filtered,
            availableCategories = allAzkar.map { it.category }.distinct(),
            isLoading = loading,
            error = error,
            selectedCategory = category,
            showFavoritesOnly = favoritesOnly,
            searchQuery = query,
            fontSize = fontSize,
            isVibrationEnabled = vibration
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AzkarUiState(isLoading = true)
    )

    fun onAction(action: AzkarAction) {
        when (action) {
            is AzkarAction.OnIncrement -> handleIncrement(action.zikrId)
            is AzkarAction.OnReset -> handleReset(action.zikrId)
            is AzkarAction.OnResetCategory -> handleResetCategory(action.category)
            is AzkarAction.OnCategorySelected -> {
                _selectedCategory.value = action.category
            }
            is AzkarAction.OnToggleFavorite -> handleToggleFavorite(action.zikrId)
            is AzkarAction.OnToggleShowFavorites -> {
                _showFavoritesOnly.value = action.show
            }
            is AzkarAction.OnSearchQueryChanged -> {
                _searchQuery.value = action.query
            }
            is AzkarAction.OnAddCustomZikr -> {
                viewModelScope.launch {
                    try {
                        addCustomZikrUseCase(action.text, action.targetCount)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
        }
    }

    private fun handleIncrement(zikrId: Long) {
        viewModelScope.launch {
            try {
                val category = uiState.value.azkarList.find { it.id == zikrId }?.category
                    ?: uiState.value.selectedCategory
                incrementCounterUseCase(zikrId)
                when {
                    category?.contains("صباح") == true ->
                        userPreferences.markDailyActivityComplete(DailyActivityIds.MORNING_AZKAR)
                    category?.contains("مساء") == true ->
                        userPreferences.markDailyActivityComplete(DailyActivityIds.EVENING_AZKAR)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun handleReset(zikrId: Long) {
        viewModelScope.launch {
            try {
                resetCounterUseCase(zikrId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun handleResetCategory(category: String) {
        viewModelScope.launch {
            try {
                repository.resetCategoryCounter(category)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun handleToggleFavorite(zikrId: Long) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(zikrId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
