package com.example.feature.statistics.presentation

/**
 * Aggregated weekly stats for the manual prayer tracker. Computed by
 * [com.example.feature.statistics.domain.PrayerTrackerStatsCalculator] and
 * consumed by the Statistics screen.
 */
data class PrayerTrackerWeekStats(
    val performedThisWeek: Int,
    val loggedThisWeek: Int,
    val performedLastWeek: Int,
    val daysWithAnyLog: Int
)
