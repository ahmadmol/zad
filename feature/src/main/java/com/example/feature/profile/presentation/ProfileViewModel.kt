package com.example.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.DeleteDonationUseCase
import com.example.feature.ehsan.domain.usecase.GetMyDonationsUseCase
import com.example.feature.ehsan.domain.usecase.UpdateDonationStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val myDonations: List<Donation> = emptyList(),
    val userName: String = "زائر",
    val userPhone: String = "",
    val isLoading: Boolean = false,
    val isUserLoggedIn: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val getMyDonationsUseCase: GetMyDonationsUseCase,
    private val updateDonationStatusUseCase: UpdateDonationStatusUseCase,
    private val deleteDonationUseCase: DeleteDonationUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observeUserAndDonations()
    }

    private fun observeUserAndDonations() {
        userRepository.getUser()
            .onEach { user -> 
                _uiState.update { 
                    it.copy(
                        userName = if (user != null) "${user.firstName} ${user.lastName}" else "زائر",
                        userPhone = user?.phoneNumber ?: "",
                        isUserLoggedIn = user != null
                    ) 
                } 
            }
            .flatMapLatest { user -> 
                val name = if (user != null) "${user.firstName} ${user.lastName}" else ""
                getMyDonationsUseCase(name) 
            }
            .onEach { list ->
                _uiState.update { it.copy(isLoading = false, myDonations = list) }
            }
            .launchIn(viewModelScope)
    }

    fun updateUserName(firstName: String, lastName: String) {
        viewModelScope.launch {
            userRepository.updateName(firstName, lastName)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun updateStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val status = if (isCompleted) "COMPLETED" else "AVAILABLE"
            updateDonationStatusUseCase(id, status)
        }
    }

    fun deleteDonation(donation: Donation) {
        viewModelScope.launch {
            deleteDonationUseCase(donation)
        }
    }
}
