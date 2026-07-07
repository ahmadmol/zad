package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.usecase.AddDonationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEhsanViewModel(
    private val addDonationUseCase: AddDonationUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _event = MutableSharedFlow<AddEhsanEvent>()
    val event = _event.asSharedFlow()

    private val _userName = MutableSharedFlow<String>(replay = 1)
    val userName = _userName.asSharedFlow()

    init {
        viewModelScope.launch {
            _userName.emit(userPreferences.userName.first())
        }
    }

    fun addDonation(
        title: String,
        description: String,
        category: String,
        location: String,
        type: String, // OFFER or REQUEST
        donorName: String,
        phoneNumber: String,
        imageUrl: String?
    ) {
        if (title.isBlank() || donorName.isBlank() || phoneNumber.isBlank()) {
            viewModelScope.launch { _event.emit(AddEhsanEvent.Error("يرجى ملء جميع الحقول الأساسية")) }
            return
        }

        viewModelScope.launch {
            try {
                addDonationUseCase(
                    Donation(
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        type = type,
                        donorName = donorName,
                        phoneNumber = phoneNumber,
                        imageUrl = imageUrl
                    )
                )
                _event.emit(AddEhsanEvent.Success)
            } catch (e: Exception) {
                _event.emit(AddEhsanEvent.Error(e.message ?: "حدث خطأ ما"))
            }
        }
    }
}

sealed interface AddEhsanEvent {
    object Success : AddEhsanEvent
    data class Error(val message: String) : AddEhsanEvent
}
