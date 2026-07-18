package com.example.feature.prayer.domain.repository

import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerOffsets
import kotlinx.coroutines.flow.Flow

interface PrayerSettingsRepository {
    fun observeSettings(): Flow<PrayerCalculationSettings>

    suspend fun updateCalculationMethod(method: PrayerCalculationMethod)

    suspend fun updateMadhhab(madhhab: PrayerMadhhab)

    suspend fun updateOffsets(offsets: PrayerOffsets)

    suspend fun updateUseAutoLocation(enabled: Boolean)

    suspend fun updatePrePrayerMinutes(minutes: Int)

    suspend fun updateIqamahMinutes(minutes: Int)

    suspend fun updateNotificationSoundType(type: String)
}
