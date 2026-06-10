package com.example.feature.azkar.domain.repository

import com.example.feature.azkar.domain.model.Ziker
import kotlinx.coroutines.flow.Flow

interface AzkarRepository{
    fun getAllAzkar(): Flow<List<Ziker>>
    suspend fun incrementCounter(ZikerId: Long)
    suspend fun resetCounter(ZikerId: Long)
}