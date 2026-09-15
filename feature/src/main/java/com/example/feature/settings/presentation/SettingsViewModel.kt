package com.example.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
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

    private val primaryFlow = combine(
        settingsManager.fontSizeFlow,
        settingsManager.themeModeFlow,
        settingsManager.vibrationEnabledFlow,
        userPreferences.adhanSoundUri
    ) { fontSize, themeMode, vibration, adhanSound ->
        PrimarySettings(fontSize, themeMode, vibration, adhanSound)
    }

    private val secondaryFlow = combine(
        settingsManager.manualLocationCityFlow.onStart { emit("حلب") },
        settingsManager.useAutoLocationFlow.onStart { emit(true) },
        settingsManager.morningReminderEnabledFlow.onStart { emit(false) }
    ) { city, autoLoc, morning ->
        SecondarySettings(city, autoLoc, morning)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        primaryFlow,
        secondaryFlow
    ) { primary, secondary ->
        SettingsUiState(
            fontSize = primary.fontSize,
            isDarkMode = primary.themeMode == com.example.feature.azkar.data.local.SettingsManager.ThemeMode.DARK,
            themeMode = primary.themeMode,
            isVibrationEnabled = primary.isVibrationEnabled,
            adhanSoundUri = primary.adhanSoundUri,
            locationCity = secondary.city,
            useAutoLocation = secondary.autoLoc,
            morningReminderEnabled = secondary.morning,
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
            is SettingsAction.SetThemeMode -> viewModelScope.launch {
                settingsManager.setThemeMode(action.mode)
            }
            is SettingsAction.SetVibration -> viewModelScope.launch {
                settingsManager.setVibrationEnabled(action.enabled)
            }
            is SettingsAction.SetAdhanSound -> viewModelScope.launch {
                userPreferences.setAdhanSoundUri(action.uri)
            }
            is SettingsAction.SetUseAutoLocation -> viewModelScope.launch {
                settingsManager.setUseAutoLocation(action.enabled)
            }
            is SettingsAction.SetManualLocation -> viewModelScope.launch {
                settingsManager.setManualLocation(action.city, action.lat, action.lng)
            }
            is SettingsAction.SetMorningReminder -> viewModelScope.launch {
                settingsManager.setMorningReminder(action.enabled, 7, 0)
                settingsManager.setEveningReminder(action.enabled, 17, 0)
            }
        }
    }

    private data class PrimarySettings(
        val fontSize: Float,
        val themeMode: com.example.feature.azkar.data.local.SettingsManager.ThemeMode,
        val isVibrationEnabled: Boolean,
        val adhanSoundUri: String?
    )

    private data class SecondarySettings(
        val city: String,
        val autoLoc: Boolean,
        val morning: Boolean
    )
}
