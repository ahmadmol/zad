package com.example.feature.ihsanplus.daily.presentation.state

import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyExperience

sealed interface IhsanPlusDailyUiState {
    data object Loading : IhsanPlusDailyUiState
    data class Success(
        val data: IhsanPlusDailyExperience
    ) : IhsanPlusDailyUiState
    data class Error(
        val message: String
    ) : IhsanPlusDailyUiState
}

sealed interface IhsanPlusDailyAction {
    data object Refresh : IhsanPlusDailyAction
    data object OnAddDhikr : IhsanPlusDailyAction
    data class OnActivityClick(val id: String) : IhsanPlusDailyAction
    data object OnTogglePrayerNotification : IhsanPlusDailyAction
    data object OnOpenQuran : IhsanPlusDailyAction
    data object OnDonateClick : IhsanPlusDailyAction
    data object OnShareDua : IhsanPlusDailyAction
    data object OnShareHadith : IhsanPlusDailyAction
}
