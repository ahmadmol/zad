package com.example.feature.ihsanplus.integration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ihsanplus.integration.contract.IhsanPlusDailySource
import com.example.feature.ihsanplus.integration.model.IhsanPlusDailySourceSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface ControlledDailyUiState {
    data object Loading : ControlledDailyUiState
    data class Content(val snapshot: IhsanPlusDailySourceSnapshot) : ControlledDailyUiState
    data class Error(val canRetry: Boolean = true) : ControlledDailyUiState
}

/**
 * Read-only ViewModel backed by [IhsanPlusDailySource] production adapter.
 * Does not mutate profile, prayer, Quran, or charity data.
 */
class ControlledDailyViewModel(
    private val dailySource: IhsanPlusDailySource
) : ViewModel() {

    private val _uiState = MutableStateFlow<ControlledDailyUiState>(ControlledDailyUiState.Loading)
    val uiState: StateFlow<ControlledDailyUiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    fun refresh() = observe()

    private fun observe() {
        viewModelScope.launch {
            _uiState.value = ControlledDailyUiState.Loading
            dailySource.observeSnapshot()
                .catch { _uiState.value = ControlledDailyUiState.Error(canRetry = true) }
                .collect { snapshot ->
                    _uiState.value = ControlledDailyUiState.Content(snapshot)
                }
        }
    }
}
