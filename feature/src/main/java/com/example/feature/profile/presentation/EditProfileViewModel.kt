package com.example.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EditProfileUiState(
            name = savedStateHandle[KEY_NAME] ?: "",
            city = savedStateHandle[KEY_CITY] ?: "",
            address = savedStateHandle[KEY_ADDRESS] ?: ""
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = userRepository.getUser().first()
            user?.let {
                _uiState.value = _uiState.value.copy(
                    name = savedStateHandle[KEY_NAME] ?: "${it.firstName} ${it.lastName}",
                    phone = it.phoneNumber,
                    city = savedStateHandle[KEY_CITY] ?: it.city,
                    address = savedStateHandle[KEY_ADDRESS] ?: it.address
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
                savedStateHandle.remove<String>(KEY_NAME)
                savedStateHandle.remove<String>(KEY_CITY)
                savedStateHandle.remove<String>(KEY_ADDRESS)
                
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
    }
}
