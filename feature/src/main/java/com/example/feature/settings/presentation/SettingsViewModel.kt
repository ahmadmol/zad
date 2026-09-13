package com.example.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Application settings owner. Observes existing [SettingsManager] and
 * [UserPreferences] persistence without duplicating storage or calculating prayers.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsManager.fontSizeFlow,
        settingsManager.darkModeFlow,
        settingsManager.vibrationEnabledFlow,
        userPreferences.adhanSoundUri
    ) { fontSize, darkMode, vibration, adhanSound ->
        SettingsUiState(
            fontSize = fontSize,
            isDarkMode = darkMode,
            isVibrationEnabled = vibration,
            adhanSoundUri = adhanSound,
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetFontSize -> viewModelScope.launch {
                settingsManager.setFontSize(action.size)
            }
            is SettingsAction.SetDarkMode -> viewModelScope.launch {
                settingsManager.setDarkMode(action.enabled)
            }
            is SettingsAction.SetVibration -> viewModelScope.launch {
                settingsManager.setVibrationEnabled(action.enabled)
            }
            is SettingsAction.SetAdhanSound -> viewModelScope.launch {
                userPreferences.setAdhanSoundUri(action.uri)
            }
        }
    }
}
