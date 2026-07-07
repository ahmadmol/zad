package com.example.feature.hadith.domain.repository

import com.example.feature.hadith.domain.model.Hadith
import kotlinx.coroutines.flow.Flow

interface HadithRepository {
    fun getAllHadiths(): Flow<List<Hadith>>
    fun getHadithsByCategory(category: String): Flow<List<Hadith>>
    fun getRandomHadith(): Flow<Hadith?>
    suspend fun toggleFavorite(hadithId: Long)
    suspend fun initialPopulation()
}
