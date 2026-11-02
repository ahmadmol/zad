package com.example.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Application settings owner. Observes existing [SettingsManager] and
 * [UserPreferences] persistence without duplicating storage or calculating prayers.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences,
    private val reminderCoordinator: com.example.feature.reminders.AzkarReminderCoordinator? = null
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
        settingsManager.manualLocationCityFlow,
        settingsManager.useAutoLocationFlow,
        settingsManager.morningReminderEnabledFlow
    ) { city, autoLoc, morning ->
        SecondarySettings(city, autoLoc, morning)
    }

    private val reload = MutableStateFlow(0)
    private val writeFailure = MutableStateFlow<String?>(null)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SettingsUiState> = reload.flatMapLatest { combine(
        primaryFlow, secondaryFlow, writeFailure
    ) { primary, secondary, error ->
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
            error = error
        )
    }.onStart { emit(SettingsUiState(isLoading = true)) }
      .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error
          emit(SettingsUiState(isLoading = false, error = "تعذر قراءة الإعدادات؛ أعد المحاولة دون مسح البيانات")) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(isLoading = true)
    )

    private fun change(action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action(); writeFailure.value = null }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { writeFailure.value = "تعذر حفظ الإعداد؛ أعد المحاولة" }
        }
    }
    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.Retry -> { writeFailure.value = null; reload.value++ }
            is SettingsAction.SetFontSize -> change {
                settingsManager.setFontSize(action.size)
            }
            is SettingsAction.SetDarkMode -> change {
                settingsManager.setDarkMode(action.enabled)
            }
            is SettingsAction.SetThemeMode -> change {
                settingsManager.setThemeMode(action.mode)
            }
            is SettingsAction.SetVibration -> change {
                settingsManager.setVibrationEnabled(action.enabled)
            }
            is SettingsAction.SetAdhanSound -> change {
                userPreferences.setAdhanSoundUri(action.uri)
            }
            is SettingsAction.SetUseAutoLocation -> change {
                settingsManager.setUseAutoLocation(action.enabled)
            }
            is SettingsAction.SetManualLocation -> change {
                settingsManager.setManualLocation(action.city, action.lat, action.lng)
            }
            is SettingsAction.SetMorningReminder -> change {
                settingsManager.setMorningReminder(action.enabled, 7, 0)
                settingsManager.setEveningReminder(action.enabled, 17, 0)
                reminderCoordinator?.reconcileAll()
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
