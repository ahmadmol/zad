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

class AzkarViewModel @JvmOverloads constructor(
    private val repository: AzkarRepository,
    private val getAzkarUseCase: GetAzkarUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addCustomZikrUseCase: AddCustomZikrUseCase,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences,
    private val handle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(handle.get<String>("category"))
    private val _showFavoritesOnly = MutableStateFlow(handle.get<Boolean>("favorites") ?: false)
    private val _searchQuery = MutableStateFlow(handle.get<String>("query") ?: "")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _filters = combine(
        _selectedCategory,
        _showFavoritesOnly,
        _searchQuery
    ) { category, favoritesOnly, query ->
        Triple(category, favoritesOnly, query)
    }

    private val refresh = MutableStateFlow(0)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AzkarUiState> = refresh.flatMapLatest { combine(
        flow { emitAll(getAzkarUseCase()) },
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
                    com.example.feature.core.search.ArabicSearch.matches(zikr.title, query) ||
                    com.example.feature.core.search.ArabicSearch.matches(zikr.text, query) ||
                    com.example.feature.core.search.ArabicSearch.matches(zikr.category, query))
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
    }.onStart { emit(AzkarUiState(isLoading = true, searchQuery = _searchQuery.value)) }
      .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error
          emit(AzkarUiState(error = "تعذر قراءة الأذكار؛ أعد المحاولة", searchQuery = _searchQuery.value, selectedCategory = _selectedCategory.value, showFavoritesOnly = _showFavoritesOnly.value)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AzkarUiState(isLoading = true)
    )

    fun onAction(action: AzkarAction) {
        when (action) {
            AzkarAction.Retry -> { _error.value = null; refresh.value++ }
            is AzkarAction.OnIncrement -> handleIncrement(action.zikrId)
            is AzkarAction.OnReset -> handleReset(action.zikrId)
            is AzkarAction.OnResetCategory -> handleResetCategory(action.category)
            is AzkarAction.OnCategorySelected -> {
                _selectedCategory.value = action.category; handle["category"] = action.category
            }
            is AzkarAction.OnToggleFavorite -> handleToggleFavorite(action.zikrId)
            is AzkarAction.OnToggleShowFavorites -> {
                _showFavoritesOnly.value = action.show; handle["favorites"] = action.show
            }
            is AzkarAction.OnSearchQueryChanged -> {
                _searchQuery.value = action.query; handle["query"] = action.query
            }
            is AzkarAction.OnAddCustomZikr -> {
                viewModelScope.launch {
                    try {
                        addCustomZikrUseCase(action.text, action.targetCount)
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "تعذر حفظ التغيير؛ أعد المحاولة"
                    }
                }
            }
        }
    }

    private fun handleIncrement(zikrId: Long) {
        viewModelScope.launch {
            var counterSaved = false
            try {
                val category = uiState.value.azkarList.find { it.id == zikrId }?.category
                    ?: uiState.value.selectedCategory
                if (!incrementCounterUseCase(zikrId)) return@launch
                counterSaved = true
                val categoryComplete = repository.getAllAzkar().first().filter { it.category == category }.let { group ->
                    group.isNotEmpty() && group.all { it.targetCount > 0 && it.currentCount >= it.targetCount }
                }
                when {
                    categoryComplete && category?.contains("صباح") == true ->
                        userPreferences.markDailyActivityComplete(DailyActivityIds.MORNING_AZKAR)
                    categoryComplete && category?.contains("مساء") == true ->
                        userPreferences.markDailyActivityComplete(DailyActivityIds.EVENING_AZKAR)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = if (counterSaved) "حُفظ العدّ، وتعذر تحديث ملخص اليوم. لا تحتاج إلى إعادة الضغط لحفظ هذا العدّ."
                    else "تعذر حفظ التغيير؛ أعد المحاولة"
            }
        }
    }

    private fun handleReset(zikrId: Long) {
        viewModelScope.launch {
            try {
                resetCounterUseCase(zikrId)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "تعذر حفظ التغيير؛ أعد المحاولة"
            }
        }
    }

    private fun handleResetCategory(category: String) {
        viewModelScope.launch {
            try {
                repository.resetCategoryCounter(category)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "تعذر حفظ التغيير؛ أعد المحاولة"
            }
        }
    }

    private fun handleToggleFavorite(zikrId: Long) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(zikrId)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "تعذر حفظ التغيير؛ أعد المحاولة"
            }
        }
    }
}
