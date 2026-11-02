package com.example.feature.dashboard.domain.repository

import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import kotlinx.coroutines.flow.Flow

interface DailyActivityRepository {
    fun observeToday(): Flow<HomeDailyActivitySummary>

    suspend fun increment(activityId: String): Result<Unit>

    suspend fun markComplete(activityId: String): Result<Unit>

    suspend fun resetIfRequired(): Result<Unit>
}
