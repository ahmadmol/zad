package com.example.feature.dashboard.data

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DailyActivityRepositoryImpl(
    private val userPreferences: UserPreferences,
    private val clock: Clock = Clock.systemDefaultZone()
) : DailyActivityRepository {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun observeToday(): Flow<HomeDailyActivitySummary> =
        combine(
            userPreferences.dailyActivityDate,
            userPreferences.dailyActivityCounts
        ) { date, counts -> date to counts }
            .distinctUntilChanged()
            .transformLatest { (date, counts) ->
                val today = todayKey()
                if (date != today) {
                    userPreferences.resetDailyActivityCounts(today)
                    return@transformLatest
                }
                emit(
                    HomeDailyActivitySummary(
                        dateKey = today,
                        items = DailyActivityMapper.mapItems(counts)
                    )
                )
            }

    override suspend fun increment(activityId: String): Result<Unit> = runCatching {
        require(DailyActivityMapper.isValidId(activityId)) { "invalid_activity_id" }
        resetIfRequired().getOrThrow()
        userPreferences.incrementDailyActivityCount(activityId)
    }

    override suspend fun markComplete(activityId: String): Result<Unit> = runCatching {
        require(DailyActivityMapper.isValidId(activityId)) { "invalid_activity_id" }
        resetIfRequired().getOrThrow()
        userPreferences.markDailyActivityComplete(activityId)
    }

    override suspend fun resetIfRequired(): Result<Unit> = runCatching {
        val today = todayKey()
        val stored = userPreferences.dailyActivityDate.first()
        if (stored != today) {
            userPreferences.resetDailyActivityCounts(today)
        }
    }

    private fun todayKey(): String = dateFormatter.format(Date.from(clock.instant()))
}
