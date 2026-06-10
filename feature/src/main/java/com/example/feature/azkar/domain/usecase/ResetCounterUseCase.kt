package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.repository.AzkarRepository

class ResetCounterUseCase(
    private val repository: AzkarRepository
){
    suspend operator fun invoke(zikrId: Long) {
        repository.resetCounter(zikrId)
    }
}