package com.example.feature.hadith.domain.usecase

import com.example.feature.hadith.domain.model.Hadith
import com.example.feature.hadith.domain.repository.HadithRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class GetHadithsUseCase(private val repository: HadithRepository) {
    operator fun invoke(): Flow<List<Hadith>> {
        return repository.getAllHadiths().onStart {
            repository.initialPopulation()
        }
    }
}
