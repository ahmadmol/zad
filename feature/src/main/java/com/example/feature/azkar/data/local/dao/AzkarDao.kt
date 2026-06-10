package com.example.feature.azkar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feature.azkar.data.local.entity.ZikerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AzkarDao {
    @Query("SELECT * FROM azkar_table ORDER BY id ASC")
    fun getAllAzkar(): Flow<List<ZikerEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZiker(ziker: ZikerEntity)
    @Query("UPDATE azkar_table SET currentCount=:newCount WHERE id =:zikrId")
    suspend fun updateZikrCount(zikrId: Long, newCount: Int)
    @Query("DELETE FROM azkar_table")
    suspend fun deleteAllAzkar()
    @Query("SELECT * FROM azkar_table WHERE id =:zikerId LIMIT 1")
    suspend fun getZikrById(zikerId: Long): ZikerEntity?
}