package com.example.feature.prayer.domain.repository

import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationState
import kotlinx.coroutines.flow.Flow

interface PrayerLocationRepository {
    fun observeLocation(): Flow<PrayerLocationState>

    suspend fun refreshLocation(): Result<PrayerLocation>

    suspend fun saveManualLocation(
        latitude: Double,
        longitude: Double,
        displayName: String
    ): Result<Unit>
}
