package com.example.feature.dashboard.data

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Clock

class DailyActivityRepositoryImpl(
    private val userPreferences: UserPreferences,
    private val clock: Clock? = null,
    private val azkarDao: AzkarDao? = null
) : DailyActivityRepository {

    private fun dayFlow() = kotlinx.coroutines.flow.flow {
        while (true) { emit(todayKey()); kotlinx.coroutines.delay(30_000) }
    }

    override fun observeToday(): Flow<HomeDailyActivitySummary> =
        combine(
            userPreferences.dailyActivitySnapshot,
            dayFlow(),
            azkarDao?.getAllAzkar() ?: kotlinx.coroutines.flow.flowOf(emptyList())
        ) { snapshot, today, rows ->
            val counts = if (snapshot.first == today) snapshot.second else emptyMap()
            // Room owns accepted taps. Recover a lagging secondary summary after process
            // interruption without replaying writes or inferring from lifetime counts.
            val recorded = rows.asSequence()
                .filter { it.category == "تسبيح" && it.lastUpdatedDate == today }
                .sumOf { it.dailyProgress.coerceAtLeast(0).toLong() }
                .coerceAtMost(DailyActivityIds.targetFor(DailyActivityIds.TASBEEH).toLong()).toInt()
            val recovered = counts + (DailyActivityIds.TASBEEH to maxOf(counts[DailyActivityIds.TASBEEH] ?: 0, recorded))
            HomeDailyActivitySummary(dateKey=today,items=DailyActivityMapper.mapItems(recovered))
        }.distinctUntilChanged()

    override suspend fun increment(activityId: String): Result<Unit> = runCatching {
        require(DailyActivityMapper.isValidId(activityId)) { "invalid_activity_id" }
        userPreferences.incrementDailyActivityCount(activityId)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }

    override suspend fun markComplete(activityId: String): Result<Unit> = runCatching {
        require(DailyActivityMapper.isValidId(activityId)) { "invalid_activity_id" }
        userPreferences.markDailyActivityComplete(activityId)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }

    override suspend fun resetIfRequired(): Result<Unit> = runCatching {
        val today = todayKey()
        userPreferences.ensureDailyActivitiesForDate(today)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }

    private fun todayKey(): String = (clock?.let { java.time.LocalDate.now(it) }
        ?: java.time.LocalDate.now()).toString()
}
