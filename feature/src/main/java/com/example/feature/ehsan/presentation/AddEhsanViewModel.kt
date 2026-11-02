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
    val currentUserId: Long? = null,
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
            userRepository.getUser().catch { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                _uiState.update { it.copy(submissionError = "تعذر قراءة الملف المحلي؛ أعد فتح الشاشة للمحاولة") }
            }.collect { user ->
                _uiState.update { 
                    it.copy(
                        currentUserName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim(),
                        currentUserPhone = user?.phoneNumber ?: "", currentUserId = user?.id
                    )
                }
            }
        }
    }

    fun consumeSuccess() { _uiState.update { it.copy(submissionSuccess = false) } }

    fun submitRequest(
        title: String,
        description: String,
        category: String,
        location: String,
        type: String = "REQUEST",
        imageUrl: String?,
        imageUrls: List<String> = emptyList(),
        contactMethod: String = "PHONE"
    ) {
        if (_uiState.value.isSubmitting) return

        // Mark that the user has tried to submit so the screen
        // can show inline field errors for empty fields.
        _uiState.update { it.copy(submitAttempted = true) }

        if (_uiState.value.currentUserId == null) {
            _uiState.update { it.copy(submissionError = "أنشئ أو افتح ملفًا شخصيًا محليًا لحفظ سجلك") }
            return
        }
        val submittingUser = _uiState.value
        val phone = submittingUser.currentUserPhone
        val validationResult = validatePhoneNumberUseCase(phone)

        if (validationResult !is PhoneValidationResult.Valid) {
            val errorMsg = when (validationResult) {
                PhoneValidationResult.Empty -> "تعذر حفظ السجل المحلي لأن رقم الهاتف غير متوفر. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                PhoneValidationResult.Placeholder -> "تعذر حفظ السجل المحلي لأن رقم الهاتف غير صالح. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                PhoneValidationResult.InvalidFormat -> "تعذر حفظ السجل المحلي لأن تنسيق رقم الهاتف غير صالح. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."
                else -> "رقم الهاتف غير صالح."
            }
            _uiState.update { it.copy(phoneError = errorMsg) }
            return
        }

        val formError = com.example.feature.ehsan.domain.model.LocalListingValidation.error(title, description, category, location, type)
        if (formError != null) {
            _uiState.update { it.copy(submissionError = formError) }
            return
        }

        if (contactMethod == "WHATSAPP" && !validationResult.normalizedPhone.startsWith('+')) {
            _uiState.update { it.copy(phoneError = "لاستخدام واتساب، أضف رمز الدولة مسبوقًا بـ + في رقم الملف الشخصي، أو اختر الاتصال الهاتفي") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, phoneError = null, submissionError = null) }

        viewModelScope.launch {
            var ownedImageReference: String? = null
            try {
                val selectedImages = imageUrls.ifEmpty { listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) }.distinct()
                if (selectedImages.size > 3) throw IllegalArgumentException("يمكنك إضافة ثلاث صور كحد أقصى")
                if (selectedImages.isNotEmpty()) {
                    ownedImageReference = if (selectedImages.size == 1) imageStore.persist(Uri.parse(selectedImages.first()))
                        else imageStore.persistAlbum(selectedImages.map(Uri::parse))
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
                check(_uiState.value.currentUserId == submittingUser.currentUserId) { "تغير الملف المحلي؛ أعد المحاولة" }
                // Finish the row commit once started; cancellation must not remove a referenced image.
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                  addDonationUseCase(
                    Donation(
                        ownerId = submittingUser.currentUserId?.let { "local-profile:$it" },
                        origin = "USER",
                        title = title,
                        description = description,
                        category = category,
                        location = location,
                        type = type,
                        donorName = submittingUser.currentUserName.ifBlank { "مستخدم" },
                        phoneNumber = validationResult.normalizedPhone,
                        imageUrl = ownedImageReference,
                        contactMethod = if (contactMethod == "WHATSAPP") "WHATSAPP" else "PHONE"
                    )
                )
                  ownedImageReference = null // The committed row owns it, even if the caller is cancelled.
                }
                _uiState.update { it.copy(isSubmitting = false, submissionSuccess = true) }
                _event.emit(AddEhsanEvent.Success)
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                    ownedImageReference?.let { runCatching { imageStore.delete(it) } }
                }
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { 
                    it.copy(
                        isSubmitting = false, 
                        submissionError = "تعذر حفظ السجل محليًا. بقيت مدخلاتك؛ أعد المحاولة."
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
