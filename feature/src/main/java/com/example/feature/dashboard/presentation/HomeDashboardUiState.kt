package com.example.feature.dashboard.presentation

import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.prayer.PrayerTime

data class HomeDashboardUiState(
    val data: HomeDashboardData = HomeDashboardData(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class HomeDashboardData(
    val userName: String = "",
    val location: String = "حلب، سوريا",
    val currentTime: String = "",
    val hijriDate: String = "",
    val nextPrayerName: String = "",
    val nextPrayerTimeLeft: String = "",
    val prayerProgress: Float = 0f,
    val allPrayers: List<PrayerTime> = emptyList(),
    val dailyVerse: String = "إِنَّمَا الْمُؤْمِنُونَ إِخْوَةٌ فَأَصْلِحُوا بَيْنَ أَخَوَيْكُمْ",
    val dailyVerseSource: String = "سورة الحجرات، آية ١٠",
    val spotlightAllahName: String = "",
    val spotlightTransliteration: String = "",
    val spotlightMeaning: String = "",
    val dailyZikrTitle: String = "",
    val dailyZikrProgress: Float = 0f,
    val dailyZikrPercentage: String = "٠٪",
    val communityOffersCount: Int = 0,
    val communityRequestsCount: Int = 0,
    val activeVolunteersCount: Int = 0,
    val dailyDuas: List<Zikr> = emptyList(),
    val selectedPrayerIndex: Int? = null,
    val isPrayerSettingsVisible: Boolean = false,
    val isCitySelectionVisible: Boolean = false
)


sealed interface HomeDashboardAction {
    object OnRefresh : HomeDashboardAction
    object OnProfileClick : HomeDashboardAction
    data class OnPrayerClick(val index: Int) : HomeDashboardAction
    object OnDismissPrayerDetails : HomeDashboardAction
    object OnShowPrayerSettings : HomeDashboardAction
    object OnDismissPrayerSettings : HomeDashboardAction
    object OnShowCitySelection : HomeDashboardAction
    object OnDismissCitySelection : HomeDashboardAction
    data class OnUpdateCalculationMethod(val method: String) : HomeDashboardAction
    data class OnUpdateMadhab(val madhab: String) : HomeDashboardAction
    data class OnUpdateLocationMode(val isAuto: Boolean) : HomeDashboardAction
    data class OnSelectCity(val name: String, val lat: Double, val lng: Double) : HomeDashboardAction
    data class OnUpdatePrePrayerNotification(val minutes: Int) : HomeDashboardAction
    data class OnUpdateIqamahNotification(val minutes: Int) : HomeDashboardAction
    data class OnUpdateNotificationSound(val type: String) : HomeDashboardAction
}
