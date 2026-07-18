package com.example.feature.ihsanplus.integration.adapter

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeDhikrSummary
import com.example.feature.dashboard.domain.model.HomeProfileSummary
import com.example.feature.dashboard.domain.model.HomeQuranSummary
import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import com.example.feature.dashboard.domain.usecase.ObserveHomeDhikrSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeProfileSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeQuranSummaryUseCase
import com.example.feature.ihsanplus.integration.contract.IhsanPlusDailySource
import com.example.feature.ihsanplus.integration.model.IhsanPlusDailySourceSnapshot
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant

/**
 * Read-only production adapter for future IhsanPlus daily integration.
 * Not registered in production navigation or DI in Part 5.
 */
class ProductionDailySourceAdapter(
    private val profileUseCase: ObserveHomeProfileSummaryUseCase,
    private val quranUseCase: ObserveHomeQuranSummaryUseCase,
    private val dhikrUseCase: ObserveHomeDhikrSummaryUseCase,
    private val dailyActivityRepository: DailyActivityRepository,
    private val prayerFacade: PrayerTimesFacade,
    private val userPreferences: UserPreferences
) : IhsanPlusDailySource {

    override fun observeSnapshot(): Flow<IhsanPlusDailySourceSnapshot> {
        val homeSections = combine(
            profileUseCase(),
            quranUseCase(),
            dhikrUseCase(),
            dailyActivityRepository.observeToday()
        ) { profileState, quranState, dhikrState, activities ->
            HomeDailyBits(profileState, quranState, dhikrState, activities)
        }
        return combine(
            homeSections,
            prayerFacade.nextPrayer,
            userPreferences.userName
        ) { bits, nextPrayer, userName ->
            val profileName = when (val profileState = bits.profileState) {
                is HomeSectionState.Content ->
                    if (profileState.value.hasLocalProfile) profileState.value.displayName else null
                else -> null
            }
            val quran = (bits.quranState as? HomeSectionState.Content)?.value
            val dhikr = (bits.dhikrState as? HomeSectionState.Content)?.value
            IhsanPlusDailySourceSnapshot(
                profileDisplayName = profileName ?: userName.takeIf {
                    it.isNotBlank() && it != "مستخدم إحسان"
                },
                nextPrayerNameArabic = nextPrayer?.name?.arabic,
                nextPrayerEpochMillis = nextPrayer?.epochMillis,
                quranSurahId = quran?.surahId,
                quranAyahNumber = quran?.ayahNumber,
                dhikrTodayCount = ((dhikr?.progress ?: 0f) * 100).toInt().coerceIn(0, 100),
                activityIds = bits.activities.items.map { it.id },
                completedActivityIds = bits.activities.items.filter { it.isCompleted }.map { it.id },
                generatedAt = Instant.now()
            )
        }
    }

    private data class HomeDailyBits(
        val profileState: HomeSectionState<HomeProfileSummary>,
        val quranState: HomeSectionState<HomeQuranSummary>,
        val dhikrState: HomeSectionState<HomeDhikrSummary>,
        val activities: HomeDailyActivitySummary
    )
}
