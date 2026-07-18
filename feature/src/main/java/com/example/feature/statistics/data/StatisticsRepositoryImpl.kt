package com.example.feature.statistics.data

import com.example.feature.azkar.domain.repository.AzkarRepository
import com.example.feature.statistics.domain.StatisticsRepository
import com.example.feature.statistics.domain.StatisticsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class StatisticsRepositoryImpl(
    private val azkarRepository: AzkarRepository
) : StatisticsRepository {

    override fun observeStatistics(): Flow<StatisticsSnapshot> =
        combine(
            azkarRepository.getLast7DaysStats(),
            azkarRepository.getAllAzkar()
        ) { stats, azkar ->
            StatisticsSnapshot(
                last7DaysStats = stats,
                azkarItems = azkar
            )
        }
}
