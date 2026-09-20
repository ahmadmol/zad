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
            city = savedStateHandle[KEY_CITY] ?: "",
            address = savedStateHandle[KEY_ADDRESS] ?: "",
            avatarUrl = savedStateHandle[KEY_AVATAR]
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = userRepository.getUser().first()
            val savedAvatar = userPreferences.userAvatarUri.first()
            user?.let {
                _uiState.value = _uiState.value.copy(
                    name = savedStateHandle[KEY_NAME] ?: "${it.firstName} ${it.lastName}",
                    phone = it.phoneNumber,
                    city = savedStateHandle[KEY_CITY] ?: it.city,
                    address = savedStateHandle[KEY_ADDRESS] ?: it.address,
                    avatarUrl = savedStateHandle[KEY_AVATAR] ?: savedAvatar
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        savedStateHandle[KEY_NAME] = newName
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onCityChange(newCity: String) {
        savedStateHandle[KEY_CITY] = newCity
        _uiState.value = _uiState.value.copy(city = newCity)
    }

    fun onAddressChange(newAddress: String) {
        savedStateHandle[KEY_ADDRESS] = newAddress
        _uiState.value = _uiState.value.copy(address = newAddress)
    }

    fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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
        }
    }

    fun saveChanges() {
        if (_uiState.value.name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "الاسم الكامل مطلوب")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val names = _uiState.value.name.split(" ")
                val firstName = names.getOrNull(0) ?: ""
                val lastName = names.drop(1).joinToString(" ")
                
                userRepository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    city = _uiState.value.city,
                    address = _uiState.value.address
                )
                userPreferences.setUserAvatarUri(_uiState.value.avatarUrl)

                savedStateHandle.remove<String>(KEY_NAME)
                savedStateHandle.remove<String>(KEY_CITY)
                savedStateHandle.remove<String>(KEY_ADDRESS)
                savedStateHandle.remove<String>(KEY_AVATAR)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "تم حفظ التغييرات بنجاح"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "حدث خطأ أثناء الحفظ"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    companion object {
        private const val KEY_NAME = "edit_profile_name"
        private const val KEY_CITY = "edit_profile_city"
        private const val KEY_ADDRESS = "edit_profile_address"
        private const val KEY_AVATAR = "edit_profile_avatar"
    }
}
