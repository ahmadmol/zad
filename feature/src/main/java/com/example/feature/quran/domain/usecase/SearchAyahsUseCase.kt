package com.example.feature.quran.domain.usecase

import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.repository.QuranRepository

class SearchAyahsUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(query: String): List<Verse> = repository.searchAyahs(query)
}
