package com.example.feature.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _loginEvent = MutableSharedFlow<Boolean>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _signUpEvent = MutableSharedFlow<Boolean>()
    val signUpEvent = _signUpEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            userRepository.getUser().collect {
                _currentUser.value = it
            }
        }
    }

    fun signUp(firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            val user = UserEntity(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber
            )
            userRepository.saveUser(user)
            _signUpEvent.emit(true)
        }
    }

    fun login(phoneNumber: String) {
        viewModelScope.launch {
            val user = userRepository.login(phoneNumber)
            if (user != null) {
                _loginEvent.emit(true)
            } else {
                _loginEvent.emit(false)
            }
        }
    }
}
