package com.example.feature.statistics.domain

import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr
import kotlinx.coroutines.flow.Flow

/**
 * Read-only snapshot of persisted azkar statistics for the Statistics screen.
 */
data class StatisticsSnapshot(
    val last7DaysStats: List<DailyStat>,
    val azkarItems: List<Zikr>
)

interface StatisticsRepository {
    fun observeStatistics(): Flow<StatisticsSnapshot>
}
