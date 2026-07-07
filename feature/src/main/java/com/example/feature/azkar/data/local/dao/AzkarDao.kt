package com.example.feature.azkar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feature.azkar.data.local.entity.DailyStatEntity
import com.example.feature.azkar.data.local.entity.ZikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AzkarDao {
    @Query("SELECT * FROM azkar_table ORDER BY id ASC")
    fun getAllAzkar(): Flow<List<ZikrEntity>>

    @Query("SELECT * FROM azkar_table WHERE category = :category ORDER BY id ASC")
    fun getAzkarByCategory(category: String): Flow<List<ZikrEntity>>

    @Query("SELECT * FROM azkar_table WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteAzkar(): Flow<List<ZikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZikr(zikr: ZikrEntity)

    @Query("UPDATE azkar_table SET currentCount = :newCount, lastUpdatedDate = :date WHERE id = :zikrId")
    suspend fun updateZikrCount(zikrId: Long, newCount: Int, date: String)

    @Query("""
        UPDATE azkar_table 
        SET currentCount = currentCount + 1, 
            dailyProgress = CASE WHEN lastUpdatedDate = :date THEN dailyProgress + 1 ELSE 1 END,
            lastUpdatedDate = :date
        WHERE id = :zikrId
    """)
    suspend fun incrementZikrCount(zikrId: Long, date: String)

    @Query("UPDATE azkar_table SET currentCount = 0 WHERE id = :zikrId")
    suspend fun resetZikrCount(zikrId: Long)

    @Query("UPDATE azkar_table SET currentCount = 0 WHERE category = :category")
    suspend fun resetCategoryCount(category: String)

    @Query("UPDATE azkar_table SET isFavorite = NOT isFavorite WHERE id = :zikrId")
    suspend fun toggleFavorite(zikrId: Long)

    @Query("DELETE FROM azkar_table")
    suspend fun deleteAllAzkar()

    @Query("DELETE FROM azkar_table WHERE id = :zikrId")
    suspend fun deleteZikrById(zikrId: Long)

    @Query("SELECT * FROM azkar_table WHERE id = :zikrId LIMIT 1")
    suspend fun getZikrById(zikrId: Long): ZikrEntity?

    @Query("SELECT COUNT(*) FROM azkar_table")
    suspend fun countZikr(): Int

    // Daily Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStat(stat: DailyStatEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatByDate(date: String): DailyStatEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLast7DaysStats(): Flow<List<DailyStatEntity>>

    @Query("UPDATE daily_stats SET totalCount = totalCount + 1 WHERE date = :date")
    suspend fun incrementDailyTotal(date: String)
}