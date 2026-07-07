package com.example.feature.duas.domain.repository

import com.example.feature.duas.domain.model.Dua
import kotlinx.coroutines.flow.Flow

interface DuaRepository {
    fun getAllDuas(): Flow<List<Dua>>
    fun getFavoriteDuas(): Flow<List<Dua>>
    fun getDuasByCategory(category: String): Flow<List<Dua>>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun loadDuasIfNeeded()
}
