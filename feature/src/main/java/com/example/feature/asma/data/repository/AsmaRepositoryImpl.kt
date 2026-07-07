package com.example.feature.asma.data.repository

import com.example.feature.asma.data.local.AsmaLocalDataSource
import com.example.feature.asma.domain.model.AllahName
import com.example.feature.asma.domain.repository.AsmaRepository
import kotlinx.coroutines.flow.Flow

class AsmaRepositoryImpl(
    private val localDataSource: AsmaLocalDataSource
) : AsmaRepository {

    override fun getAllAsma(): Flow<List<AllahName>> {
        return localDataSource.asmaFlow
    }

    override suspend fun loadAsma() {
        localDataSource.loadAsmaIfNeeded()
    }

    override suspend fun toggleFavorite(id: Int) {
        localDataSource.toggleFavorite(id)
    }
}