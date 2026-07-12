package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.AddDonationUseCase
import com.example.feature.ehsan.domain.usecase.PhoneValidationResult
import com.example.feature.ehsan.domain.usecase.ValidatePhoneNumberUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RequestHelpUiState(
    val isSubmitting: Boolean = false,
    val phoneError: String? = null,
    val submissionError: String? = null,
    val submissionSuccess: Boolean = false,
    val currentUserName: String = "",
    val currentUserPhone: String = ""
)

class AddEhsanViewModel(
    private val addDonationUseCase: AddDonationUseCase,
    private val userRepository: UserRepository,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestHelpUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddEhsanEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            userRepository.getUser().collect { user ->
                _uiState.update { 
                    it.copy(
                        currentUserName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim(),
                        currentUserPhone = user?.phoneNumber ?: ""
                    )
                }
            }
        }
    }

    fun submitRequest(
        title: String,
        description: String,
        category: String,
        location: String,
        type: String = "REQUEST",
        imageUrl: String?
    ) {
        if (_uiState.value.isSubmitting) return

        val phone = _uiState.value.currentUserPhone
        val validationResult = validatePhoneNumberUseCase(phone)

        if (validationResult !is PhoneValidationResult.Valid) {
            val errorMsg = when (validationResult) {
                PhoneValidationResult.Empty -> "تعذر إرسال الطلب لأن رقم الهاتف غير متوفر. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                PhoneValidationResult.Placeholder -> "تعذر إرسال الطلب لأن رقم الهاتف غير صالح. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                PhoneValidationResult.InvalidFormat -> "تعذر إرسال الطلب لأن تنسيق رقم الهاتف غير صالح. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                else -> "رقم الهاتف غير صالح."
            }
            _uiState.update { it.copy(phoneError = errorMsg) }
            return
        }

        if (title.isBlank() || description.isBlank()) {
            _uiState.update { it.copy(submissionError = "يرجى ملء جميع الحقول المطلوبة") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, phoneError = null, submissionError = null) }

        viewModelScope.launch {
            try {
                addDonationUseCase(
                    Donation(
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        type = type,
                        donorName = _uiState.value.currentUserName.ifBlank { "مستخدم" },
                        phoneNumber = validationResult.normalizedPhone,
                        imageUrl = imageUrl
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, submissionSuccess = true) }
                _event.emit(AddEhsanEvent.Success)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSubmitting = false, 
                        submissionError = e.message ?: "تعذر إرسال الطلب. يرجى المحاولة لاحقاً."
                    )
                }
            }
        }
    }
}

sealed interface AddEhsanEvent {
    object Success : AddEhsanEvent
    data class Error(val message: String) : AddEhsanEvent
}
