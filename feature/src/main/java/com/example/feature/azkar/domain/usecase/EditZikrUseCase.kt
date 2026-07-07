package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.repository.AzkarRepository

class EditZikrUseCase(
    private val repository: AzkarRepository
) {
    suspend operator fun invoke(zikr: Zikr) {
        repository.addZikr(zikr)
    }
}
