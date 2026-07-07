package com.example.feature.quran.domain.usecase

import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.repository.QuranRepository

class GetSurahUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(surahId: Int): Surah? = repository.getSurahById(surahId)
}
