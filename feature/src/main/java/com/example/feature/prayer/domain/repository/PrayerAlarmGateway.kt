package com.example.feature.prayer.domain.repository

import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerSchedule
import com.example.feature.prayer.domain.model.PrayerScheduleResult
import kotlinx.coroutines.flow.Flow

interface PrayerAlarmGateway {
    suspend fun replaceSchedule(schedule: PrayerSchedule): PrayerScheduleResult

    suspend fun cancelAll(): Result<Unit>

    fun observePermissionState(): Flow<PrayerAlarmPermissionState>
}
