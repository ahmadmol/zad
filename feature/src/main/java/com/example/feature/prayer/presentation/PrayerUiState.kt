package com.example.feature.prayer.presentation

import com.example.feature.prayer.PrayerTime

data class PrayerUiState(
    val prayerTimes: List<PrayerTime> = emptyList(),
    val nextPrayerName: String = "",
    val nextPrayerCountdown: String = "00:00:00",
    val locationName: String = "حلب، سوريا",
    val currentDate: String = "",
    val hijriDate: String = "",
    val isLoading: Boolean = false
)

sealed interface PrayerAction {
    object OnRefresh : PrayerAction
    data class OnToggleNotification(val prayerName: String) : PrayerAction
}
