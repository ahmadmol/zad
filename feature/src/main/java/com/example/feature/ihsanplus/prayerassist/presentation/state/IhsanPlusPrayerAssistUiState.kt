package com.example.feature.ihsanplus.prayerassist.presentation.state

import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerAssistDashboard

sealed interface IhsanPlusPrayerAssistUiState {
    data object Loading : IhsanPlusPrayerAssistUiState

    data class Success(
        val data: IhsanPlusPrayerAssistDashboard
    ) : IhsanPlusPrayerAssistUiState

    data class Error(
        val message: String
    ) : IhsanPlusPrayerAssistUiState
}
