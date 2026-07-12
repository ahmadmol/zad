package com.example.feature.ihsanplus.daily.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ihsanplus.daily.domain.usecase.GetIhsanPlusDailyExperienceUseCase
import com.example.feature.ihsanplus.daily.presentation.state.IhsanPlusDailyAction
import com.example.feature.ihsanplus.daily.presentation.state.IhsanPlusDailyUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IhsanPlusDailyViewModel(
    private val getDailyExperienceUseCase: GetIhsanPlusDailyExperienceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IhsanPlusDailyUiState>(IhsanPlusDailyUiState.Loading)
    val uiState: StateFlow<IhsanPlusDailyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = IhsanPlusDailyUiState.Loading
            getDailyExperienceUseCase()
                .onEach { data ->
                    _uiState.value = IhsanPlusDailyUiState.Success(data)
                }
                .catch { e ->
                    _uiState.value = IhsanPlusDailyUiState.Error(e.message ?: "Unknown Error")
                }
                .collect()
        }
    }

    fun onAction(action: IhsanPlusDailyAction) {
        when (action) {
            IhsanPlusDailyAction.Refresh -> loadData()
            IhsanPlusDailyAction.OnAddDhikr -> {
                // Implement dhikr increment logic
            }
            is IhsanPlusDailyAction.OnActivityClick -> {
                // Navigate to activity
            }
            IhsanPlusDailyAction.OnTogglePrayerNotification -> {
                // Toggle notification
            }
            IhsanPlusDailyAction.OnOpenQuran -> {
                // Open Quran
            }
            IhsanPlusDailyAction.OnDonateClick -> {
                // Open donation
            }
            IhsanPlusDailyAction.OnShareDua -> {
                // Share dua
            }
            IhsanPlusDailyAction.OnShareHadith -> {
                // Share hadith
            }
        }
    }
}
