package com.example.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.statistics.domain.StatisticsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Read-only statistics owner. Does not mutate live azkar counters.
 */
class StatisticsViewModel(
    statisticsRepository: StatisticsRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = statisticsRepository.observeStatistics()
        .map { snapshot ->
            val dailyItems = snapshot.azkarItems.map { zikr ->
                DailyStatisticItem(
                    id = zikr.id,
                    text = zikr.text,
                    dailyProgress = zikr.dailyProgress.coerceAtLeast(0)
                )
            }
            val totalDaily = dailyItems.sumOf { it.dailyProgress }
            StatisticsUiState(
                last7DaysStats = snapshot.last7DaysStats.map { stat ->
                    stat.copy(totalCount = stat.totalCount.coerceAtLeast(0))
                },
                summary = StatisticsSummary(totalDailyCount = totalDaily),
                dailyItems = dailyItems,
                isLoading = false,
                error = null,
                isEmpty = snapshot.last7DaysStats.isEmpty() && dailyItems.all { it.dailyProgress == 0 }
            )
        }
        .catch { e ->
            emit(
                StatisticsUiState(
                    isLoading = false,
                    error = e.message ?: "تعذر تحميل الإحصائيات",
                    isEmpty = true
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(isLoading = true)
        )

    fun onAction(action: StatisticsAction) {
        when (action) {
            StatisticsAction.Retry -> {
                // Flow-backed; repository re-emits when Room updates.
            }
        }
    }
}
