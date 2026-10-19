package com.example.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.DeleteDonationUseCase
import com.example.feature.ehsan.domain.usecase.GetMyDonationsUseCase
import com.example.feature.ehsan.domain.usecase.UpdateDonationStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val myDonations: List<Donation> = emptyList(),
    val userName: String = "زائر",
    val userPhone: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isDarkMode: Boolean = false,
    val fontSize: Float = 24f,
    val hasTrustedListingOwnership: Boolean = false,
    val localOwnerId: String? = null,
    val managementError: String? = null,
    val isManaging: Boolean = false,
    val city: String = "حلب"
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val getMyDonationsUseCase: GetMyDonationsUseCase,
    private val updateDonationStatusUseCase: UpdateDonationStatusUseCase,
    private val deleteDonationUseCase: DeleteDonationUseCase,
    private val userRepository: UserRepository,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences,
    private val listings: com.example.feature.ehsan.domain.repository.EhsanRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observeUserAndSettings()
    }

    private fun observeUserAndSettings() {
        combine(
            userRepository.getUser(),
            userPreferences.userAvatarUri,
            settingsManager.darkModeFlow,
            settingsManager.fontSizeFlow,
            settingsManager.manualLocationCityFlow
        ) { user, avatarUri, darkMode, fontSize, city ->
            val fullName = if (user != null) "${user.firstName} ${user.lastName}".trim() else "زائر"
            _uiState.update {
                it.copy(
                    userName = if (fullName.isNotBlank()) fullName else "زائر",
                    userPhone = user?.phoneNumber ?: "",
                    avatarUrl = if (user != null) avatarUri else null,
                    isUserLoggedIn = user != null,
                    localOwnerId = user?.id?.takeIf { id -> id > 0 }?.let { id -> "local-profile:$id" },
                    hasTrustedListingOwnership = user != null && user.id > 0,
                    isDarkMode = darkMode,
                    fontSize = fontSize,
                    city = if (!user?.city.isNullOrBlank()) user.city else city.ifBlank { "حلب" }
                )
            }
            user
        }
        .flatMapLatest { user ->
            if (user != null && user.id > 0) getMyDonationsUseCase("local-profile:${user.id}")
            else kotlinx.coroutines.flow.flowOf(emptyList<Donation>())
        }
        .catch { _uiState.update { it.copy(isLoading = false, managementError = "تعذر تحميل سجلاتك المحلية") } }
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

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(enabled)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            settingsManager.setFontSize(size)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            userPreferences.setUserAvatarUri(null)
            userPreferences.clearUserPresentation()
        }
    }

    fun updateStatus(id: Long, isCompleted: Boolean) {
        if (!owns(id)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isManaging = true, managementError = null) }
            try { updateDonationStatusUseCase(id, if (isCompleted) "COMPLETED" else "AVAILABLE") }
            catch (e: Exception) { _uiState.update { it.copy(managementError = e.message ?: "تعذر تعديل الحالة") } }
            finally { _uiState.update { it.copy(isManaging = false) } }
        }
    }

    fun deleteDonation(donation: Donation) {
        if (!owns(donation.id)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isManaging = true, managementError = null) }
            try { deleteDonationUseCase(donation) }
            catch (e: Exception) { _uiState.update { it.copy(managementError = e.message ?: "تعذر حذف السجل") } }
            finally { _uiState.update { it.copy(isManaging = false) } }
        }
    }

    private fun owns(id: Long): Boolean = _uiState.value.myDonations.any {
        it.id == id && it.origin == "USER" && it.ownerId == _uiState.value.localOwnerId
    }

    fun editListing(donation: Donation, imageSources: List<String>? = null, onSaved: () -> Unit) {
        if (!owns(donation.id) || _uiState.value.isManaging) return
        viewModelScope.launch {
            _uiState.update { it.copy(isManaging = true, managementError = null) }
            try {
                val repository = requireNotNull(listings) { "تعديل السجل غير متاح" }
                if (imageSources == null) repository.updateDonation(donation)
                else repository.updateDonation(donation, imageSources)
                onSaved()
            } catch (e: Exception) { _uiState.update { it.copy(managementError = e.message ?: "تعذر حفظ التعديل") } }
            finally { _uiState.update { it.copy(isManaging = false) } }
        }
    }
}
