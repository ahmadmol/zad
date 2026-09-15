package com.example.feature.settings.presentation

import com.example.feature.azkar.data.local.SettingsManager.ThemeMode

data class SettingsUiState(
    val fontSize: Float = 24f,
    val isDarkMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isVibrationEnabled: Boolean = true,
    val adhanSoundUri: String? = null,
    val locationCity: String = "حلب",
    val useAutoLocation: Boolean = true,
    val morningReminderEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface SettingsAction {
    data class SetFontSize(val size: Float) : SettingsAction
    data class SetDarkMode(val enabled: Boolean) : SettingsAction
    data class SetThemeMode(val mode: ThemeMode) : SettingsAction
    data class SetVibration(val enabled: Boolean) : SettingsAction
    data class SetAdhanSound(val uri: String) : SettingsAction
    data class SetUseAutoLocation(val enabled: Boolean) : SettingsAction
    data class SetManualLocation(val city: String, val lat: Double, val lng: Double) : SettingsAction
    data class SetMorningReminder(val enabled: Boolean) : SettingsAction
}
