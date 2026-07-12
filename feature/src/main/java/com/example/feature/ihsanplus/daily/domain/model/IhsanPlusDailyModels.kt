package com.example.feature.ihsanplus.daily.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class IhsanPlusDailyExperience(
    val greetingName: String,
    val hijriDateLabel: String,
    val gregorianDateLabel: String,
    val nextPrayer: IhsanPlusPrayerSummary,
    val quranProgress: IhsanPlusQuranProgress,
    val dailyDua: IhsanPlusDailyDua,
    val dailyHadith: IhsanPlusDailyHadith,
    val dhikrProgress: IhsanPlusDhikrProgress,
    val charitySuggestion: IhsanPlusCharitySuggestion,
    val activityItems: List<IhsanPlusDailyActivityItem>
)

@Serializable
data class IhsanPlusPrayerSummary(
    val prayerName: String,
    val prayerTimeLabel: String,
    val remainingTimeLabel: String,
    val locationLabel: String,
    val isNotificationEnabled: Boolean
)

@Serializable
data class IhsanPlusQuranProgress(
    val surahName: String,
    val ayahLabel: String,
    val progressPercent: Int,
    val lastReadLabel: String
)

@Serializable
data class IhsanPlusDailyDua(
    val title: String,
    val content: String,
    val source: String
)

@Serializable
data class IhsanPlusDailyHadith(
    val title: String,
    val content: String,
    val source: String
)

@Serializable
data class IhsanPlusDhikrProgress(
    val title: String,
    val currentCount: Int,
    val targetCount: Int
)

@Serializable
data class IhsanPlusCharitySuggestion(
    val title: String,
    val description: String,
    val categoryLabel: String,
    val urgencyLabel: String
)

@Serializable
data class IhsanPlusDailyActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val progressPercent: Int,
    val type: IhsanPlusDailyActivityType
)

@Serializable
enum class IhsanPlusDailyActivityType {
    QURAN,
    PRAYER,
    DUA,
    HADITH,
    DHIKR,
    CHARITY
}
