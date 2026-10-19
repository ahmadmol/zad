package com.example.feature.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.feature.quran.data.local.entity.AyahEntity
import com.example.feature.quran.data.local.entity.BookmarkEntity
import com.example.feature.quran.data.local.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // --- Surahs ---

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun observeAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    suspend fun getAllSurahs(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE id = :surahId LIMIT 1")
    suspend fun getSurahById(surahId: Int): SurahEntity?

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun countSurahs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    // --- Ayahs ---

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY verseNumber ASC")
    fun observeAyahsBySurah(surahId: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY verseNumber ASC")
    suspend fun getAyahsBySurah(surahId: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE id = :ayahId LIMIT 1")
    suspend fun getAyahById(ayahId: Long): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId AND verseNumber = :verseNumber LIMIT 1")
    suspend fun getAyahBySurahAndVerse(surahId: Int, verseNumber: Int): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE text LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchAyahs(query: String): List<AyahEntity>

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun countAyahs(): Int

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahId < :surahId OR (surahId = :surahId AND verseNumber <= :verseNumber)")
    suspend fun getAyahIndex(surahId: Int, verseNumber: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT * FROM ayahs ORDER BY surahId, verseNumber")
    suspend fun getAllAyahs(): List<AyahEntity>

    /** Change the edition's text without replacing stable verse IDs or user bookmarks. */
    @Transaction
    suspend fun applyCanonicalEdition(canonical: List<AyahEntity>) {
        val existing = getAllAyahs().associateBy { it.surahId to it.verseNumber }
        val changed = canonical.mapNotNull { verse ->
            val old = existing[verse.surahId to verse.verseNumber]
            when {
                old == null -> verse
                old.text != verse.text -> old.copy(text = verse.text)
                else -> null
            }
        }
        if (changed.isNotEmpty()) insertAyahs(changed)
    }

    // --- Bookmarks ---

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahId = :surahId AND verseNumber = :verseNumber")
    suspend fun deleteBookmark(surahId: Int, verseNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahId = :surahId AND verseNumber = :verseNumber)")
    fun observeIsBookmarked(surahId: Int, verseNumber: Int): Flow<Boolean>
}
