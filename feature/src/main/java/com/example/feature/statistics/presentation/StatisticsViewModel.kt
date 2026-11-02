package com.example.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.statistics.domain.StatisticsRepository
import kotlinx.coroutines.flow.*

/**
 * Read-only statistics owner. Does not mutate live azkar counters.
 */
class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)
    fun retry() { reload.value++ }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = reload.flatMapLatest { flow { emitAll(statisticsRepository.observeStatistics()) }
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
        .onStart { emit(StatisticsUiState(isLoading = true)) }
        .catch { e ->
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(
                StatisticsUiState(
                    isLoading = false,
                    error = "تعذر تحميل الإحصائيات؛ أعد المحاولة",
                    isEmpty = true
                )
            )
        }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(isLoading = true)
        )
}
