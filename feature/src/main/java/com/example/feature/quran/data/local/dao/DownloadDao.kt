package com.example.feature.quran.data.local.dao

import androidx.room.*
import com.example.feature.quran.data.local.entity.DownloadedAyahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaded_ayahs WHERE surahId = :surahId AND verseNumber = :verseNumber AND readerId = :readerId")
    suspend fun getDownloadedAyah(surahId: Int, verseNumber: Int, readerId: String): DownloadedAyahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedAyah(ayah: DownloadedAyahEntity)

    @Query("SELECT * FROM downloaded_ayahs WHERE surahId = :surahId AND readerId = :readerId")
    fun getDownloadedAyahsBySurah(surahId: Int, readerId: String): Flow<List<DownloadedAyahEntity>>

    @Query("DELETE FROM downloaded_ayahs WHERE surahId = :surahId AND readerId = :readerId")
    suspend fun deleteSurahDownloads(surahId: Int, readerId: String)
}
