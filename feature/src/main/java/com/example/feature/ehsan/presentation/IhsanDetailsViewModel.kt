package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IhsanDetailsViewModel(
    private val repository: EhsanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IhsanDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadItem(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val donation = repository.getDonationById(id)
                if (donation != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        item = IhsanDetailsUi(
                            id = donation.id,
                            title = donation.title,
                            description = donation.description,
                            type = donation.type,
                            category = donation.category,
                            location = donation.location,
                            status = donation.status,
                            donorName = donation.donorName,
                            phoneNumber = donation.phoneNumber,
                            imageUrl = donation.imageUrl
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "لم يتم العثور على الحالة"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "حدث خطأ غير متوقع"
                )
            }
        }
    }
}
