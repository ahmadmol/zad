package com.example.feature.quran.domain.repository

import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun observeAllSurahs(): Flow<List<Surah>>
    suspend fun getAllSurahs(): List<Surah>
    suspend fun getSurahById(surahId: Int): Surah?
    
    fun observeAyahs(surahId: Int): Flow<List<Verse>>
    suspend fun getAyahsBySurah(surahId: Int): List<Verse>
    suspend fun getAyahBySurahAndVerse(surahId: Int, verseNumber: Int): Verse?

    suspend fun saveLastRead(surahId: Int, ayahNumber: Int)
    fun observeLastRead(): Flow<Pair<Int, Int>?>
    suspend fun getKhatmaProgress(): Float

    fun observeAllBookmarks(): Flow<List<com.example.feature.quran.domain.model.Bookmark>>
    suspend fun toggleBookmark(surahId: Int, verseNumber: Int)
    fun observeIsBookmarked(surahId: Int, verseNumber: Int): Flow<Boolean>

    suspend fun searchAyahs(query: String): List<Verse>

    fun observeDownloadedAyahs(surahId: Int, readerId: String): Flow<List<Int>>
    suspend fun getLocalAyahPath(surahId: Int, verseNumber: Int, readerId: String): String?
    suspend fun saveDownloadedAyah(surahId: Int, verseNumber: Int, readerId: String, localPath: String)
}
