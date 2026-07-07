package com.example.feature.hadith.data.local.dao

import androidx.room.*
import com.example.feature.hadith.data.local.entity.HadithEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths")
    fun getAllHadiths(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE category = :category")
    fun getHadithsByCategory(category: String): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths ORDER BY RANDOM() LIMIT 1")
    fun getRandomHadith(): Flow<HadithEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

    @Update
    suspend fun updateHadith(hadith: HadithEntity)

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun getCount(): Int
}
