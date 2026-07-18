package com.example.feature.dashboard.domain.model

sealed interface HomeSectionState<out T> {
    data object Loading : HomeSectionState<Nothing>

    data class Content<T>(val value: T) : HomeSectionState<T>

    data class Empty(val reason: String? = null) : HomeSectionState<Nothing>

    data class Error(
        val message: String? = null,
        val canRetry: Boolean = true
    ) : HomeSectionState<Nothing>
}

data class HomeProfileSummary(
    val displayName: String,
    val hasLocalProfile: Boolean
)

data class HomePrayerSummary(
    val nextPrayerNameAr: String,
    val remainingLabel: String,
    val progress: Float,
    val locationLabel: String,
    val prayers: List<HomePrayerItem>,
    val isLocationUnavailable: Boolean
)

data class HomePrayerItem(
    val nameAr: String,
    val nameEn: String,
    val timeLabel: String,
    val timestamp: Long,
    val isPast: Boolean,
    val isActive: Boolean
)

data class HomeQuranSummary(
    val surahId: Int,
    val surahName: String,
    val ayahNumber: Int
)

data class HomeDhikrSummary(
    val title: String,
    val progress: Float,
    val percentageLabel: String,
    val previewItems: List<HomeDhikrPreviewItem>
)

data class HomeDhikrPreviewItem(
    val id: Long,
    val text: String,
    val category: String
)

data class HomeAsmaSummary(
    val name: String,
    val transliteration: String,
    val meaning: String
)

data class HomeCharitySummary(
    val offersCount: Int,
    val requestsCount: Int,
    val activeVolunteersCount: Int
)

data class HomeDailyActivityItem(
    val id: String,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val unit: String,
    val isCompleted: Boolean,
    val route: String
)

data class HomeDailyActivitySummary(
    val dateKey: String,
    val items: List<HomeDailyActivityItem>
)

data class HomeDateSummary(
    val hijriDate: String,
    val gregorianClockLabel: String
)
