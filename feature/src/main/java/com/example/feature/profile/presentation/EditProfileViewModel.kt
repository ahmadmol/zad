package com.example.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val imageStore: EhsanImageStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EditProfileUiState(
            name = savedStateHandle[KEY_NAME] ?: "",
            phone = savedStateHandle[KEY_PHONE] ?: "",
            city = savedStateHandle[KEY_CITY] ?: "حلب",
            address = savedStateHandle[KEY_ADDRESS] ?: "",
            bio = savedStateHandle[KEY_BIO] ?: "",
            avatarUrl = savedStateHandle[KEY_AVATAR]
        )
    )
    val uiState = _uiState.asStateFlow()

    private var ownerId: Long? = null
    private var loadingJob: kotlinx.coroutines.Job? = null
    init { loadUserData() }
    fun retry() = loadUserData()
    private fun loadUserData() {
        loadingJob?.cancel()
        ownerId = null
        _uiState.value = _uiState.value.copy(isLoading = true, dataReady = false, errorMessage = null)
        loadingJob = viewModelScope.launch {
          try {
            val user = userRepository.getUser().first()
            ownerId = user?.id
            if (user == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "افتح ملفًا شخصيًا محليًا أولًا")
                return@launch
            }
            val savedAvatar = userPreferences.userAvatarUri.first()
            val savedBio = userPreferences.userBio.first()
            user?.let {
                val fullName = "${it.firstName} ${it.lastName}".trim()
                _uiState.value = _uiState.value.copy(
                    name = savedStateHandle[KEY_NAME] ?: if (fullName.isNotBlank()) fullName else "",
                    phone = savedStateHandle[KEY_PHONE] ?: if (it.phoneNumber.isNotBlank()) it.phoneNumber else "",
                    city = savedStateHandle[KEY_CITY] ?: if (it.city.isNotBlank()) it.city else "حلب",
                    address = savedStateHandle[KEY_ADDRESS] ?: it.address,
                    avatarUrl = savedStateHandle[KEY_AVATAR] ?: savedAvatar,
                    bio = savedStateHandle[KEY_BIO] ?: savedBio
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false, dataReady = true)
          } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
          catch (error: Exception) { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "تعذر قراءة الملف؛ أعد المحاولة") }
        }
    }

    fun onNameChange(newName: String) {
        if (newName.length > 120) return
        savedStateHandle[KEY_NAME] = newName
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onPhoneChange(newPhone: String) {
        savedStateHandle[KEY_PHONE] = newPhone
        _uiState.value = _uiState.value.copy(phone = newPhone)
    }

    fun onCityChange(newCity: String) {
        savedStateHandle[KEY_CITY] = newCity
        _uiState.value = _uiState.value.copy(city = newCity)
    }

    fun onAddressChange(newAddress: String) {
        if (newAddress.length > 300) return
        savedStateHandle[KEY_ADDRESS] = newAddress
        _uiState.value = _uiState.value.copy(address = newAddress)
    }

    fun onBioChange(newBio: String) {
        if (newBio.length <= 200) {
            savedStateHandle[KEY_BIO] = newBio
            _uiState.value = _uiState.value.copy(bio = newBio)
        }
    }

    fun onAvatarSelected(uri: Uri) {
        if (_uiState.value.isLoading || !_uiState.value.dataReady) return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
            val persistedRef = imageStore.persist(uri)
            if (persistedRef != null) {
                savedStateHandle[KEY_AVATAR] = persistedRef
                _uiState.value = _uiState.value.copy(
                    avatarUrl = persistedRef,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "فشل في حفظ الصورة"
                )
            }
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "تعذر حفظ الصورة؛ بقيت الصورة السابقة")
            } finally { _uiState.value = _uiState.value.copy(isLoading = false) }
        }
    }

    fun saveChanges() {
        if (_uiState.value.isLoading || !_uiState.value.dataReady) return
        val draft = _uiState.value
        if (ownerId == null) { _uiState.value = draft.copy(errorMessage = "أعد تحميل ملفك المحلي قبل الحفظ"); return }
        if (draft.name.length > 120 || draft.city.isBlank() || draft.city.length > 80 || draft.address.length > 300) {
            _uiState.value = draft.copy(errorMessage = "راجع طول الاسم والمدينة والعنوان قبل الحفظ"); return
        }
        if (_uiState.value.name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "الاسم الكامل مطلوب")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            try {
                check(userRepository.getUser().first()?.id == ownerId) { "تغير الملف المحلي" }
                val names = draft.name.trim().split(Regex("\\s+"))
                val firstName = names.getOrNull(0) ?: ""
                val lastName = names.drop(1).joinToString(" ")
                
                userRepository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    city = draft.city.trim(),
                    address = draft.address.trim()
                )
                userPreferences.setUserAvatarUri(draft.avatarUrl)
                userPreferences.setUserBio(draft.bio)

                savedStateHandle.remove<String>(KEY_NAME)
                savedStateHandle.remove<String>(KEY_PHONE)
                savedStateHandle.remove<String>(KEY_CITY)
                savedStateHandle.remove<String>(KEY_ADDRESS)
                savedStateHandle.remove<String>(KEY_BIO)
                savedStateHandle.remove<String>(KEY_AVATAR)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "تم حفظ التغييرات بنجاح"
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "تعذر إتمام حفظ الملف؛ بقيت مدخلاتك. أعد المحاولة."
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    companion object {
        private const val KEY_NAME = "edit_profile_name"
        private const val KEY_PHONE = "edit_profile_phone"
        private const val KEY_CITY = "edit_profile_city"
        private const val KEY_ADDRESS = "edit_profile_address"
        private const val KEY_BIO = "edit_profile_bio"
        private const val KEY_AVATAR = "edit_profile_avatar"
    }
}
