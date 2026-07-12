package com.example.feature.tasbih.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasbihViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedTasbihIndex = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TasbihUiState> = combine(
        getAzkarUseCase(),
        _selectedTasbihIndex,
        settingsManager.vibrationEnabledFlow,
        _isLoading,
        _error
    ) { allAzkar, selectedIndex, vibration, loading, error ->
        val tasbihList = allAzkar.filter { it.category == "تسبيح" }
        TasbihUiState(
            tasbihList = tasbihList,
            selectedTasbihIndex = selectedIndex.coerceIn(0, maxOf(0, tasbihList.size - 1)),
            isVibrationEnabled = vibration,
            isLoading = loading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasbihUiState(isLoading = true)
    )

    fun onAction(action: TasbihAction) {
        when (action) {
            is TasbihAction.OnIncrement -> {
                viewModelScope.launch {
                    try {
                        incrementCounterUseCase(action.tasbihId)
                        userPreferences.incrementDailyActivityCount(DailyActivityIds.TASBEEH)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is TasbihAction.OnReset -> {
                viewModelScope.launch {
                    try {
                        resetCounterUseCase(action.tasbihId)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is TasbihAction.OnSelectTasbih -> {
                _selectedTasbihIndex.value = action.index
            }
            TasbihAction.OnToggleVibration -> {
                viewModelScope.launch {
                    settingsManager.setVibrationEnabled(!uiState.value.isVibrationEnabled)
                }
            }
        }
    }
}
