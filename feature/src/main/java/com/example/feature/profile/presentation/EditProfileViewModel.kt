package com.example.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = userRepository.getUser().first()
            user?.let {
                _uiState.value = _uiState.value.copy(
                    name = "${it.firstName} ${it.lastName}",
                    phone = it.phoneNumber,
                    city = it.city,
                    address = it.address
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onCityChange(newCity: String) {
        _uiState.value = _uiState.value.copy(city = newCity)
    }

    fun onAddressChange(newAddress: String) {
        _uiState.value = _uiState.value.copy(address = newAddress)
    }

    fun saveChanges() {
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
}
