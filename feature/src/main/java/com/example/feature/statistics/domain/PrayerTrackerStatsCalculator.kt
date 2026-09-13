package com.example.feature.statistics.domain

import com.example.feature.prayer.domain.model.ManualPrayerDaySummary
import com.example.feature.statistics.presentation.PrayerTrackerWeekStats
import java.time.LocalDate

/**
 * Phase 6 — turns the raw manual prayer log into the weekly Statistics view.
 *
 * Pure and clock-free: the caller supplies `today`. Only user-recorded entries are
 * counted; a prayer with no entry contributes nothing and is never reported as missed.
 */
object PrayerTrackerStatsCalculator {

    /** Inclusive start of the 7-day window ending on [today]. */
    fun weekStart(today: LocalDate): LocalDate = today.minusDays(6)

    /** Inclusive start of the previous 7-day window. */
    fun previousWeekStart(today: LocalDate): LocalDate = today.minusDays(13)

    fun calculate(
        summaries: List<ManualPrayerDaySummary>,
        today: LocalDate
    ): PrayerTrackerWeekStats {
        val thisWeek = weekStart(today)..today
        val lastWeek = previousWeekStart(today)..today.minusDays(7)

        val current = summaries.filter { it.date in thisWeek }
        val previous = summaries.filter { it.date in lastWeek }

        return PrayerTrackerWeekStats(
            performedThisWeek = current.sumOf { it.performedCount },
            loggedThisWeek = current.sumOf { it.loggedCount },
            performedLastWeek = previous.sumOf { it.performedCount },
            daysWithAnyLog = current.count { it.loggedCount > 0 }
        )
    }
}
