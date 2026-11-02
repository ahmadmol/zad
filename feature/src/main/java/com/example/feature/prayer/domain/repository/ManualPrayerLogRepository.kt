package com.example.feature.prayer.domain.repository

import com.example.feature.prayer.domain.model.ManualPrayerDaySummary
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import com.example.feature.prayer.domain.model.PrayerName
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Phase 6 — Manual Prayer Tracker.
 *
 * Local-only. Records what the user states and nothing more: there is no automatic
 * logging, no inferred "missed" status, and no sharing or ranking.
 */
interface ManualPrayerLogRepository {

    /** Records (or re-records) [prayer] on [date]. Idempotent. */
    suspend fun log(date: LocalDate, prayer: PrayerName, status: ManualPrayerStatus)

    /** Removes the user's entry, returning the prayer to "not recorded". */
    suspend fun clear(date: LocalDate, prayer: PrayerName)

    fun observeDay(date: LocalDate): Flow<ManualPrayerDaySummary>

    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<ManualPrayerDaySummary>>

    /** Phase 8C — part of "delete my local data". */
    suspend fun clearAll()
}
