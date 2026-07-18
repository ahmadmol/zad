package com.example.feature.dashboard.domain.usecase

import com.example.feature.asma.domain.usecase.GetAsmaUseCase
import com.example.feature.asma.domain.util.AsmaTodayResolver
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.dashboard.domain.model.HomeAsmaSummary
import com.example.feature.dashboard.domain.model.HomeCharitySummary
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeDhikrPreviewItem
import com.example.feature.dashboard.domain.model.HomeDhikrSummary
import com.example.feature.dashboard.domain.model.HomePrayerItem
import com.example.feature.dashboard.domain.model.HomePrayerSummary
import com.example.feature.dashboard.domain.model.HomeProfileSummary
import com.example.feature.dashboard.domain.model.HomeQuranSummary
import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DEFAULT_ASMA_NAME = "الرَّحْمَنُ"
private const val DEFAULT_ASMA_TRANSLIT = "Ar-Rahman"
private const val DEFAULT_ASMA_MEANING = "The Entirely Merciful"
private const val DEFAULT_ZIKR_TITLE = "الورد اليومي"
private const val HARDCODED_VOLUNTEERS = 12

class ObserveHomeProfileSummaryUseCase(
    private val userPreferences: UserPreferences
) {
    operator fun invoke(): Flow<HomeSectionState<HomeProfileSummary>> =
        userPreferences.userName
            .map<String, HomeSectionState<HomeProfileSummary>> { name ->
                val trimmed = name.trim()
                val hasProfile = trimmed.isNotEmpty() && trimmed != "مستخدم إحسان"
                HomeSectionState.Content(
                    HomeProfileSummary(
                        displayName = if (trimmed.isEmpty()) "مستخدم إحسان" else trimmed,
                        hasLocalProfile = hasProfile
                    )
                )
            }
            .catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomePrayerSummaryUseCase(
    private val prayerFacade: PrayerTimesFacade
) {
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    operator fun invoke(): Flow<HomeSectionState<HomePrayerSummary>> =
        combine(
            prayerFacade.nextPrayer,
            prayerFacade.prayerDay,
            prayerFacade.locationState
        ) { next, day, location ->
            val now = System.currentTimeMillis()
            val prayers = day?.instants?.map { instant ->
                val activeEnd = instant.epochMillis + 45 * 60 * 1000
                val isActive = now >= instant.epochMillis && now < activeEnd
                HomePrayerItem(
                    nameAr = instant.name.arabic,
                    nameEn = instant.name.english,
                    timeLabel = timeFormat.format(Date(instant.epochMillis)),
                    timestamp = instant.epochMillis,
                    isPast = now > instant.epochMillis && !isActive,
                    isActive = isActive
                )
            }.orEmpty()

            if (location is PrayerLocationState.Unavailable && prayers.isEmpty() && next == null) {
                return@combine HomeSectionState.Error(canRetry = true)
            }

            val remaining = next?.let { n ->
                val diff = n.remainingMillis
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            }.orEmpty()

            val locationLabel = when (location) {
                is PrayerLocationState.Available -> location.location.displayName
                is PrayerLocationState.Unavailable -> "الموقع غير متاح"
                PrayerLocationState.Loading -> "جاري تحديد الموقع..."
            }

            HomeSectionState.Content(
                HomePrayerSummary(
                    nextPrayerNameAr = next?.name?.arabic.orEmpty(),
                    remainingLabel = remaining,
                    progress = next?.let { NextPrayerSelector.progressFraction(it, now) } ?: 0f,
                    locationLabel = locationLabel,
                    prayers = prayers,
                    isLocationUnavailable = location is PrayerLocationState.Unavailable
                )
            )
        }.catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomeQuranSummaryUseCase(
    private val userPreferences: UserPreferences,
    private val quranRepository: QuranRepository
) {
    operator fun invoke(): Flow<HomeSectionState<HomeQuranSummary>> =
        combine(
            userPreferences.lastReadSurahId,
            userPreferences.lastReadAyahNumber
        ) { surahId, ayahNumber ->
            if (surahId == null || ayahNumber == null || surahId <= 0 || ayahNumber <= 0) {
                HomeSectionState.Empty()
            } else {
                val surah = runCatching { quranRepository.getSurahById(surahId) }.getOrNull()
                HomeSectionState.Content(
                    HomeQuranSummary(
                        surahId = surahId,
                        surahName = surah?.name ?: "سورة $surahId",
                        ayahNumber = ayahNumber
                    )
                )
            }
        }.catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomeDailyActivitiesUseCase(
    private val repository: DailyActivityRepository
) {
    operator fun invoke(): Flow<HomeSectionState<HomeDailyActivitySummary>> =
        repository.observeToday()
            .map<HomeDailyActivitySummary, HomeSectionState<HomeDailyActivitySummary>> {
                if (it.items.isEmpty()) HomeSectionState.Empty()
                else HomeSectionState.Content(it)
            }
            .catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomeDhikrSummaryUseCase(
    private val getAzkarUseCase: GetAzkarUseCase
) {
    operator fun invoke(): Flow<HomeSectionState<HomeDhikrSummary>> =
        getAzkarUseCase()
            .map<List<com.example.feature.azkar.domain.model.Zikr>, HomeSectionState<HomeDhikrSummary>> { azkar ->
                if (azkar.isEmpty()) {
                    HomeSectionState.Empty()
                } else {
                    val dailyZikr = azkar.firstOrNull { it.category == "أذكار الصباح" } ?: azkar.first()
                    val totalProgress = azkar.map {
                        if (it.targetCount > 0) it.currentCount.toFloat() / it.targetCount else 0f
                    }.average().toFloat().coerceIn(0f, 1f)
                    HomeSectionState.Content(
                        HomeDhikrSummary(
                            title = dailyZikr.text.ifBlank { DEFAULT_ZIKR_TITLE },
                            progress = totalProgress,
                            percentageLabel = "${(totalProgress * 100).toInt()}٪",
                            previewItems = azkar.take(5).map {
                                HomeDhikrPreviewItem(it.id, it.text, it.category)
                            }
                        )
                    )
                }
            }
            .catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomeAsmaSummaryUseCase(
    private val getAsmaUseCase: GetAsmaUseCase
) {
    operator fun invoke(): Flow<HomeSectionState<HomeAsmaSummary>> =
        getAsmaUseCase()
            .map<List<com.example.feature.asma.domain.model.AllahName>, HomeSectionState<HomeAsmaSummary>> { names ->
                val selected = AsmaTodayResolver.selectDailyName(names)
                if (selected == null) {
                    HomeSectionState.Content(
                        HomeAsmaSummary(DEFAULT_ASMA_NAME, DEFAULT_ASMA_TRANSLIT, DEFAULT_ASMA_MEANING)
                    )
                } else {
                    HomeSectionState.Content(
                        HomeAsmaSummary(
                            name = selected.name,
                            transliteration = selected.transliteration,
                            meaning = selected.meaning
                        )
                    )
                }
            }
            .catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class ObserveHomeCharitySummaryUseCase(
    private val getDonationsUseCase: GetDonationsUseCase
) {
    operator fun invoke(): Flow<HomeSectionState<HomeCharitySummary>> =
        getDonationsUseCase()
            .map<List<com.example.feature.ehsan.domain.model.Donation>, HomeSectionState<HomeCharitySummary>> { donations ->
                if (donations.isEmpty()) {
                    HomeSectionState.Empty()
                } else {
                    HomeSectionState.Content(
                        HomeCharitySummary(
                            offersCount = donations.count { it.type == "OFFER" },
                            requestsCount = donations.count { it.type == "REQUEST" },
                            activeVolunteersCount = HARDCODED_VOLUNTEERS
                        )
                    )
                }
            }
            .catch { emit(HomeSectionState.Error(canRetry = true)) }
}

class RefreshHomeDashboardUseCase(
    private val prayerFacade: PrayerTimesFacade,
    private val dailyActivityRepository: DailyActivityRepository
) {
    suspend operator fun invoke() {
        dailyActivityRepository.resetIfRequired()
        prayerFacade.refreshLocation()
    }
}

fun currentHijriDate(): String = HijriDateFormatter.nowFormatted()
