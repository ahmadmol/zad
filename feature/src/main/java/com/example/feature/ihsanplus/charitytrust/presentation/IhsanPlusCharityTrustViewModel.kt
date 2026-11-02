package com.example.feature.ihsanplus.charitytrust.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ihsanplus.charitytrust.domain.usecase.GetIhsanPlusCharityTrustDashboardUseCase
import com.example.feature.ihsanplus.charitytrust.presentation.state.IhsanPlusCharityTrustAction
import com.example.feature.ihsanplus.charitytrust.presentation.state.IhsanPlusCharityTrustUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IhsanPlusCharityTrustViewModel(
    private val getDashboardUseCase: GetIhsanPlusCharityTrustDashboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IhsanPlusCharityTrustUiState>(IhsanPlusCharityTrustUiState.Loading)
    val uiState: StateFlow<IhsanPlusCharityTrustUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onAction(action: IhsanPlusCharityTrustAction) {
        when (action) {
            is IhsanPlusCharityTrustAction.Refresh -> loadDashboard()
            is IhsanPlusCharityTrustAction.OpenCaseDetails -> {
                // Local action logic or future navigation
            }
            is IhsanPlusCharityTrustAction.QuickActionClicked -> {
                // Local action logic
            }
            IhsanPlusCharityTrustAction.OpenPrivacyGuide -> {
                // Local action logic
            }
            IhsanPlusCharityTrustAction.OpenSafetyGuide -> {
                // Local action logic
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = IhsanPlusCharityTrustUiState.Loading
            try {
                val data = getDashboardUseCase()
                _uiState.value = IhsanPlusCharityTrustUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = IhsanPlusCharityTrustUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
