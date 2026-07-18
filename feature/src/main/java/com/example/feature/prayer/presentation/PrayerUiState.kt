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
    val canRetryLocation: Boolean = false
)

sealed interface PrayerAction {
    data object OnRefresh : PrayerAction
    data class OnToggleNotification(val prayerName: String) : PrayerAction
    data object OnToggleSystemStatus : PrayerAction
    data object OnRetrySchedule : PrayerAction
}
