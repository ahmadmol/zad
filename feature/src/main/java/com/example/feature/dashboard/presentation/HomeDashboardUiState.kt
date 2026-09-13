package com.example.feature.dashboard.presentation

import com.example.designsystem.component.DailyActivityItemData
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.dashboard.domain.model.HomeAsmaSummary
import com.example.feature.dashboard.domain.model.HomeCharitySummary
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeDhikrSummary
import com.example.feature.dashboard.domain.model.HomePrayerSummary
import com.example.feature.dashboard.domain.model.HomeProfileSummary
import com.example.feature.dashboard.domain.model.HomeQuranSummary
import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.prayer.PrayerTime

data class HomeDashboardUiState(
    val profile: HomeSectionState<HomeProfileSummary> = HomeSectionState.Loading,
    val prayer: HomeSectionState<HomePrayerSummary> = HomeSectionState.Loading,
    val quran: HomeSectionState<HomeQuranSummary> = HomeSectionState.Loading,
    val dailyActivities: HomeSectionState<HomeDailyActivitySummary> = HomeSectionState.Loading,
    val dhikr: HomeSectionState<HomeDhikrSummary> = HomeSectionState.Loading,
    val asma: HomeSectionState<HomeAsmaSummary> = HomeSectionState.Loading,
    val charity: HomeSectionState<HomeCharitySummary> = HomeSectionState.Loading,
    val currentTime: String = "",
    val hijriDate: String = "",
    val isRefreshing: Boolean = false,
    val refreshErrorMessage: String? = null,
    val selectedPrayerIndex: Int? = null,
    val isPrayerSettingsVisible: Boolean = false,
    val isCitySelectionVisible: Boolean = false
) {
    /** Compatibility projection for existing HomeDashboardScreen cards. */
    val data: HomeDashboardData
        get() {
            val prayerContent = (prayer as? HomeSectionState.Content)?.value
            val profileContent = (profile as? HomeSectionState.Content)?.value
            val asmaContent = (asma as? HomeSectionState.Content)?.value
            val dhikrContent = (dhikr as? HomeSectionState.Content)?.value
            val charityContent = (charity as? HomeSectionState.Content)?.value
            val quranContent = (quran as? HomeSectionState.Content)?.value
            val activities = (dailyActivities as? HomeSectionState.Content)?.value?.items.orEmpty()

            return HomeDashboardData(
                userName = profileContent?.displayName.orEmpty(),
                location = prayerContent?.locationLabel ?: "جاري تحديد الموقع...",
                currentTime = currentTime,
                hijriDate = hijriDate,
                nextPrayerName = prayerContent?.nextPrayerNameAr.orEmpty(),
                nextPrayerTimeLeft = prayerContent?.remainingLabel.orEmpty(),
                prayerProgress = prayerContent?.progress ?: 0f,
                allPrayers = prayerContent?.prayers?.map {
                    PrayerTime(
                        nameAr = it.nameAr,
                        nameEn = it.nameEn,
                        time = it.timeLabel,
                        timestamp = it.timestamp,
                        isPast = it.isPast,
                        isActive = it.isActive
                    )
                }.orEmpty(),
                spotlightAllahName = asmaContent?.name.orEmpty(),
                spotlightTransliteration = asmaContent?.transliteration.orEmpty(),
                spotlightMeaning = asmaContent?.meaning.orEmpty(),
                dailyZikrTitle = dhikrContent?.title.orEmpty(),
                dailyZikrProgress = dhikrContent?.progress ?: 0f,
                dailyZikrPercentage = dhikrContent?.percentageLabel ?: "٠٪",
                communityOffersCount = charityContent?.offersCount ?: 0,
                communityRequestsCount = charityContent?.requestsCount ?: 0,
                dailyDuas = dhikrContent?.previewItems?.map {
                    Zikr(
                        id = it.id,
                        title = it.text,
                        text = it.text,
                        currentCount = 0,
                        targetCount = 1,
                        category = it.category,
                        isFavorite = false,
                        source = ""
                    )
                }.orEmpty(),
                dailyActivities = activities.map {
                    DailyActivityItemData(
                        id = it.id,
                        title = it.title,
                        currentCount = it.currentCount,
                        targetCount = it.targetCount,
                        unit = it.unit,
                        isCompleted = it.isCompleted,
                        route = it.route
                    )
                },
                lastReadSurahId = quranContent?.surahId,
                lastReadSurahName = quranContent?.surahName,
                lastReadAyahNumber = quranContent?.ayahNumber,
                selectedPrayerIndex = selectedPrayerIndex,
                isPrayerSettingsVisible = isPrayerSettingsVisible,
                isCitySelectionVisible = isCitySelectionVisible
            )
        }

    val isLoading: Boolean
        get() = listOf(profile, prayer, quran, dailyActivities, dhikr, asma, charity)
            .all { it is HomeSectionState.Loading }

    val error: String? = null
}

data class HomeDashboardData(
    val userName: String = "",
    val location: String = "جاري تحديد الموقع...",
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
    val dailyDuas: List<Zikr> = emptyList(),
    val dailyActivities: List<DailyActivityItemData> = emptyList(),
    val lastReadSurahId: Int? = null,
    val lastReadSurahName: String? = null,
    val lastReadAyahNumber: Int? = null,
    val selectedPrayerIndex: Int? = null,
    val isPrayerSettingsVisible: Boolean = false,
    val isCitySelectionVisible: Boolean = false
)

sealed interface HomeDashboardAction {
    data object OnRefresh : HomeDashboardAction
    data object OnProfileClick : HomeDashboardAction
    data class OnPrayerClick(val index: Int) : HomeDashboardAction
    data object OnDismissPrayerDetails : HomeDashboardAction
    data object OnShowPrayerSettings : HomeDashboardAction
    data object OnDismissPrayerSettings : HomeDashboardAction
    data object OnShowCitySelection : HomeDashboardAction
    data object OnDismissCitySelection : HomeDashboardAction
    data class OnUpdateCalculationMethod(val method: String) : HomeDashboardAction
    data class OnUpdateMadhab(val madhab: String) : HomeDashboardAction
    data class OnUpdateLocationMode(val isAuto: Boolean) : HomeDashboardAction
    data class OnSelectCity(val name: String, val lat: Double, val lng: Double) : HomeDashboardAction
    data class OnUpdatePrePrayerNotification(val minutes: Int) : HomeDashboardAction
    data class OnUpdateIqamahNotification(val minutes: Int) : HomeDashboardAction
    data class OnUpdateNotificationSound(val type: String) : HomeDashboardAction
    data object OnRetryPrayer : HomeDashboardAction
}
