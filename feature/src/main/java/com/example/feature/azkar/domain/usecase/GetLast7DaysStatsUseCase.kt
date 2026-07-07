package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.repository.AzkarRepository
import kotlinx.coroutines.flow.Flow

class GetLast7DaysStatsUseCase(
    private val repository: AzkarRepository
) {
    operator fun invoke(): Flow<List<DailyStat>> {
        return repository.getLast7DaysStats()
    }
}
