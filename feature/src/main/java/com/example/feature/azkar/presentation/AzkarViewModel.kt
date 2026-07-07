package com.example.feature.azkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.repository.AzkarRepository
import com.example.feature.azkar.domain.usecase.*
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
    private val getLast7DaysStatsUseCase: GetLast7DaysStatsUseCase,
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

    private val _settings = combine(
        settingsManager.fontSizeFlow,
        settingsManager.vibrationEnabledFlow,
        settingsManager.darkModeFlow,
        userPreferences.adhanSoundUri
    ) { fontSize, vibration, darkMode, adhanSound ->
        SettingsState(fontSize, vibration, darkMode, adhanSound)
    }

    val uiState: StateFlow<AzkarUiState> = combine(
        getAzkarUseCase(),
        _filters,
        getLast7DaysStatsUseCase(),
        combine(_settings, _isLoading, _error) { s, l, e -> Triple(s, l, e) }
    ) { allAzkar, filters, stats, other ->
        val (category, favoritesOnly, query) = filters
        val (settings, loading, error) = other
        
        val filtered = allAzkar.filter { zikr ->
            (category == null || zikr.category == category) &&
            (!favoritesOnly || zikr.isFavorite) &&
            (query.isBlank() || zikr.text.contains(query, ignoreCase = true))
        }

        AzkarUiState(
            azkarList = filtered,
            isLoading = loading,
            error = error,
            selectedCategory = category,
            showFavoritesOnly = favoritesOnly,
            searchQuery = query,
            last7DaysStats = stats,
            fontSize = settings.fontSize,
            isVibrationEnabled = settings.vibrationEnabled,
            isDarkMode = settings.darkMode,
            adhanSoundUri = settings.adhanSoundUri
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
            is AzkarAction.OnRetry -> { /* Flow handles retry automatically if source flows update */ }
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
            is AzkarAction.OnFontSizeChanged -> {
                viewModelScope.launch { settingsManager.setFontSize(action.size) }
            }
            is AzkarAction.OnVibrationToggle -> {
                viewModelScope.launch { settingsManager.setVibrationEnabled(action.enabled) }
            }
            is AzkarAction.OnDarkModeToggle -> {
                viewModelScope.launch { settingsManager.setDarkMode(action.enabled) }
            }
            is AzkarAction.OnAdhanSoundChanged -> {
                viewModelScope.launch { userPreferences.setAdhanSoundUri(action.uri) }
            }
        }
    }

    private fun handleIncrement(zikrId: Long) {
        viewModelScope.launch {
            try {
                incrementCounterUseCase(zikrId)
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

    private data class SettingsState(
        val fontSize: Float,
        val vibrationEnabled: Boolean,
        val darkMode: Boolean,
        val adhanSoundUri: String?
    )
}
