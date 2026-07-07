package com.example.feature.quran.data.repository

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.quran.data.local.QuranAssetLoader
import com.example.feature.quran.data.local.dao.DownloadDao
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.BookmarkEntity
import com.example.feature.quran.data.local.entity.DownloadedAyahEntity
import com.example.feature.quran.data.local.entity.toDomain
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.*

/**
 * Stitches Room data + asset bootstrap behind a single [QuranRepository].
 *
 * The asset loader is invoked lazily on each read entry-point so it works
 * even if the database was wiped or migrated.
 */
class QuranRepositoryImpl(
    private val dao: QuranDao,
    private val downloadDao: DownloadDao,
    private val assetLoader: QuranAssetLoader,
    private val userPreferences: UserPreferences
) : QuranRepository {

    override fun observeAllSurahs(): Flow<List<Surah>> =
        dao.observeAllSurahs()
            .onStart { assetLoader.loadIfNeeded() }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getAllSurahs(): List<Surah> {
        assetLoader.loadIfNeeded()
        return dao.getAllSurahs().map { it.toDomain() }
    }

    override suspend fun getSurahById(surahId: Int): Surah? {
        assetLoader.loadIfNeeded()
        return dao.getSurahById(surahId)?.toDomain()
    }

    override fun observeAyahs(surahId: Int): Flow<List<Verse>> =
        dao.observeAyahsBySurah(surahId)
            .onStart { assetLoader.loadIfNeeded() }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getAyahsBySurah(surahId: Int): List<Verse> {
        assetLoader.loadIfNeeded()
        return dao.getAyahsBySurah(surahId).map { it.toDomain() }
    }

    override suspend fun getAyahBySurahAndVerse(surahId: Int, verseNumber: Int): Verse? {
        assetLoader.loadIfNeeded()
        return dao.getAyahBySurahAndVerse(surahId, verseNumber)?.toDomain()
    }

    override suspend fun saveLastRead(surahId: Int, ayahNumber: Int) {
        userPreferences.saveLastRead(surahId, ayahNumber)
    }

    override suspend fun getKhatmaProgress(): Float {
        val lastRead = observeLastRead().first() ?: return 0f
        val totalAyahs = 6236 // Fixed total
        val currentAyahIndex = dao.getAyahIndex(lastRead.first, lastRead.second)
        return (currentAyahIndex.toFloat() / totalAyahs.toFloat()) * 100f
    }

    override fun observeLastRead(): Flow<Pair<Int, Int>?> {
        return combine(
            userPreferences.lastReadSurahId,
            userPreferences.lastReadAyahNumber
        ) { surahId, ayahNumber ->
            if (surahId != null && ayahNumber != null) {
                surahId to ayahNumber
            } else null
        }
    }

    override fun observeAllBookmarks(): Flow<List<com.example.feature.quran.domain.model.Bookmark>> {
        return dao.observeAllBookmarks().map { entities ->
            entities.map { entity ->
                val surah = dao.getSurahById(entity.surahId)
                val verse = dao.getAyahBySurahAndVerse(entity.surahId, entity.verseNumber)
                entity.toDomain(surah?.name ?: "", verse?.text ?: "")
            }
        }
    }

    override suspend fun toggleBookmark(surahId: Int, verseNumber: Int) {
        val isBookmarked = dao.observeIsBookmarked(surahId, verseNumber).first()
        if (isBookmarked) {
            dao.deleteBookmark(surahId, verseNumber)
        } else {
            dao.insertBookmark(BookmarkEntity(surahId = surahId, verseNumber = verseNumber))
        }
    }

    override fun observeIsBookmarked(surahId: Int, verseNumber: Int): Flow<Boolean> {
        return dao.observeIsBookmarked(surahId, verseNumber)
    }

    override suspend fun searchAyahs(query: String): List<Verse> {
        assetLoader.loadIfNeeded()
        return dao.searchAyahs(query).map { it.toDomain() }
    }

    override fun observeDownloadedAyahs(surahId: Int, readerId: String): Flow<List<Int>> {
        return downloadDao.getDownloadedAyahsBySurah(surahId, readerId).map { entities ->
            entities.map { it.verseNumber }
        }
    }

    override suspend fun getLocalAyahPath(surahId: Int, verseNumber: Int, readerId: String): String? {
        return downloadDao.getDownloadedAyah(surahId, verseNumber, readerId)?.localPath
    }

    override suspend fun saveDownloadedAyah(surahId: Int, verseNumber: Int, readerId: String, localPath: String) {
        downloadDao.insertDownloadedAyah(
            DownloadedAyahEntity(
                surahId = surahId,
                verseNumber = verseNumber,
                readerId = readerId,
                localPath = localPath
            )
        )
    }
}
