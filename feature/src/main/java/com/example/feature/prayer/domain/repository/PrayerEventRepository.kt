package com.example.feature.prayer.domain.repository

import com.example.feature.prayer.domain.model.PrayerOperationalEvent
import kotlinx.coroutines.flow.Flow

interface PrayerEventRepository {
    suspend fun record(event: PrayerOperationalEvent)

    fun observeRecent(limit: Int): Flow<List<PrayerOperationalEvent>>

    suspend fun clear()
}
