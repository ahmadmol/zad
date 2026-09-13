package com.example.feature.prayer.presentation

import com.example.feature.prayer.PrayerTime

data class PrayerUiState(
    val prayerTimes: List<PrayerTime> = emptyList(),
    val nextPrayerName: String = "",
    val nextPrayerCountdown: String = "00:00:00",
    val locationName: String = "جاري تحديد الموقع...",
    val locationSourceLabel: String? = null,
    val currentDate: String = "",
    val hijriDate: String = "",
    val isLoading: Boolean = false,
    val locationUnavailable: Boolean = false,
    val locationUnavailableMessage: String? = null,
    val systemStatusExpanded: Boolean = false,
    val notificationPermissionLabel: String? = null,
    val lastScheduleSummary: String? = null,
    val canRetryLocation: Boolean = false,
    val calculationMethodName: String = "رابطة العالم الإسلامي",
    val prePrayerReminderMinutes: Int = 10,
    val adhanEnabled: Boolean = true,
    val showSettingsBottomSheet: Boolean = false,
    val showCalendarBottomSheet: Boolean = false,
    val selectedPrayer: PrayerTime? = null
)

sealed interface PrayerAction {
    data object OnRefresh : PrayerAction
    data object OnToggleSystemStatus : PrayerAction
    data object OnRetrySchedule : PrayerAction
    data class OnSelectPrayer(val prayer: PrayerTime?) : PrayerAction
    data class OnToggleSettingsSheet(val show: Boolean) : PrayerAction
    data class OnToggleCalendarSheet(val show: Boolean) : PrayerAction
    data class OnUpdateCalculationMethod(val methodStr: String) : PrayerAction
    data class OnUpdatePrePrayerMinutes(val minutes: Int) : PrayerAction
    data class OnToggleAdhan(val enabled: Boolean) : PrayerAction
}

