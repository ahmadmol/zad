package com.example.feature.asma.domain.repository

import com.example.feature.asma.domain.model.AllahName
import kotlinx.coroutines.flow.Flow

interface AsmaRepository {
    fun getAllAsma(): Flow<List<AllahName>>
    suspend fun loadAsma()
}