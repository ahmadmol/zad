package com.example.feature.statistics.presentation

import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr

data class StatisticsSummary(
    val totalDailyCount: Int
)

data class DailyStatisticItem(
    val id: Long,
    val text: String,
    val dailyProgress: Int
)

data class StatisticsUiState(
    val last7DaysStats: List<DailyStat> = emptyList(),
    val summary: StatisticsSummary = StatisticsSummary(totalDailyCount = 0),
    val dailyItems: List<DailyStatisticItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEmpty: Boolean = false
)

sealed interface StatisticsAction {
    data object Retry : StatisticsAction
}
