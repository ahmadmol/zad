package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.usecase.GetDonationByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DonationDetailUiState(
    val donation: Donation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DonationDetailViewModel(
    private val getDonationByIdUseCase: GetDonationByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonationDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDonation(id: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val donation = getDonationByIdUseCase(id)
                _uiState.update { it.copy(isLoading = false, donation = donation) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
