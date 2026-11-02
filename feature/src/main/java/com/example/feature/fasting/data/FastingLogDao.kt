package com.example.feature.fasting.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingLogDao {
    @Query("SELECT * FROM fasting_log ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<FastingLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: FastingLogEntity)
    @Query("DELETE FROM fasting_log WHERE dateEpochDay = :day")
    suspend fun deleteDay(day: Long)
    @Query("DELETE FROM fasting_log")
    suspend fun clearFastingOnly()
}
