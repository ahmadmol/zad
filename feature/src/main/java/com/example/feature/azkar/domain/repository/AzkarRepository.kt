package com.example.feature.azkar.domain.repository

import com.example.feature.azkar.domain.model.Zikr
import kotlinx.coroutines.flow.Flow

interface AzkarRepository {
    fun getAllAzkar(): Flow<List<Zikr>>
    fun getAzkarByCategory(category: String): Flow<List<Zikr>>
    fun getFavoriteAzkar(): Flow<List<Zikr>>
    suspend fun updateZikrCount(zikrId: Long, newCount: Int)
    suspend fun incrementCounter(zikrId: Long)
    suspend fun toggleFavorite(zikrId: Long)
    suspend fun resetCounter(zikrId: Long)
    suspend fun resetCategoryCounter(category: String)
    suspend fun addZikr(zikr: Zikr)
    suspend fun deleteZikr(zikrId: Long)
    fun getLast7DaysStats(): Flow<List<com.example.feature.azkar.domain.model.DailyStat>>
}