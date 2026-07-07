package com.example.feature.quran.domain.usecase

import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow

class GetSurahsUseCase(private val repository: QuranRepository) {
    operator fun invoke(): Flow<List<Surah>> = repository.observeAllSurahs()
}
