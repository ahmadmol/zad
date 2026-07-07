package com.example.feature.quran.domain.usecase

import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow

class GetAyahsUseCase(private val repository: QuranRepository) {
    operator fun invoke(surahId: Int): Flow<List<Verse>> = repository.observeAyahs(surahId)
}
