package com.example.feature.duas.data.repository

import android.content.Context
import com.example.feature.duas.data.local.dao.DuaDao
import com.example.feature.duas.data.local.entity.DuaEntity
import com.example.feature.duas.data.local.entity.toDomain
import com.example.feature.duas.domain.model.Dua
import com.example.feature.duas.domain.repository.DuaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DuaRepositoryImpl(
    private val context: Context,
    private val dao: DuaDao
) : DuaRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllDuas(): Flow<List<Dua>> = 
        dao.getAllDuas().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteDuas(): Flow<List<Dua>> = 
        dao.getFavoriteDuas().map { list -> list.map { it.toDomain() } }

    override fun getDuasByCategory(category: String): Flow<List<Dua>> = 
        dao.getDuasByCategory(category).map { list -> list.map { it.toDomain() } }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, !isFavorite)
    }

    override suspend fun loadDuasIfNeeded() {
        if (dao.getDuasCount() == 0) {
            val raw = context.assets.open("duas.json").bufferedReader().use { it.readText() }
            val entities = json.decodeFromString<List<DuaEntity>>(raw)
            dao.insertDuas(entities)
        }
    }
}
