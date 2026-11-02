package com.example.feature.ihsanplus.charitytrust.presentation.state

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard

sealed interface IhsanPlusCharityTrustUiState {
    data object Loading : IhsanPlusCharityTrustUiState

    data class Success(
        val data: IhsanPlusCharityTrustDashboard
    ) : IhsanPlusCharityTrustUiState

    data class Error(
        val message: String
    ) : IhsanPlusCharityTrustUiState
}

sealed interface IhsanPlusCharityTrustAction {
    data class OpenCaseDetails(val caseId: String) : IhsanPlusCharityTrustAction
    data class QuickActionClicked(val actionId: String) : IhsanPlusCharityTrustAction
    data object Refresh : IhsanPlusCharityTrustAction
    data object OpenSafetyGuide : IhsanPlusCharityTrustAction
    data object OpenPrivacyGuide : IhsanPlusCharityTrustAction
}
