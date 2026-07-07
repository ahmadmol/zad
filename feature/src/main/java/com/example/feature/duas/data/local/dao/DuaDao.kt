package com.example.feature.duas.data.local.dao

import androidx.room.*
import com.example.feature.duas.data.local.entity.DuaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    @Query("SELECT * FROM duas")
    fun getAllDuas(): Flow<List<DuaEntity>>

    @Query("SELECT * FROM duas WHERE isFavorite = 1")
    fun getFavoriteDuas(): Flow<List<DuaEntity>>

    @Query("SELECT * FROM duas WHERE category = :category")
    fun getDuasByCategory(category: String): Flow<List<DuaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<DuaEntity>)

    @Query("UPDATE duas SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM duas")
    suspend fun getDuasCount(): Int
}
