package com.example.feature.settings.presentation

data class SettingsUiState(
    val fontSize: Float = 24f,
    val isDarkMode: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val adhanSoundUri: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface SettingsAction {
    data class SetFontSize(val size: Float) : SettingsAction
    data class SetDarkMode(val enabled: Boolean) : SettingsAction
    data class SetVibration(val enabled: Boolean) : SettingsAction
    data class SetAdhanSound(val uri: String) : SettingsAction
    data object Retry : SettingsAction
}
