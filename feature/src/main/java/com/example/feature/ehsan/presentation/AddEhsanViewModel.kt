package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.feature.ehsan.data.image.EhsanImageStore
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
    val currentUserPhone: String = "",
    /**
     * Set to true once the user presses the primary action at
     * least once. Used to decide whether to show inline field
     * errors — we don't want to shout at the user before they
     * have even tried to submit.
     */
    val submitAttempted: Boolean = false
)

class AddEhsanViewModel(
    private val addDonationUseCase: AddDonationUseCase,
    private val userRepository: UserRepository,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase,
    private val imageStore: EhsanImageStore
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

        // Mark that the user has tried to submit so the screen
        // can show inline field errors for empty fields.
        _uiState.update { it.copy(submitAttempted = true) }

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
            var ownedImageReference: String? = null
            try {
                if (!imageUrl.isNullOrBlank()) {
                    ownedImageReference = imageStore.persist(Uri.parse(imageUrl))
                    if (ownedImageReference == null) {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submissionError = "تعذر حفظ الصورة المختارة. اختر صورة أخرى ثم أعد المحاولة."
                            )
                        }
                        return@launch
                    }
                }
                addDonationUseCase(
                    Donation(
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        type = type,
                        donorName = _uiState.value.currentUserName.ifBlank { "مستخدم" },
                        phoneNumber = validationResult.normalizedPhone,
                        imageUrl = ownedImageReference
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, submissionSuccess = true) }
                _event.emit(AddEhsanEvent.Success)
            } catch (e: Exception) {
                ownedImageReference?.let { imageStore.delete(it) }
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
