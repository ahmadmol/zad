package com.example.feature.quran.data.local

import android.content.Context
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.AyahEntity
import com.example.feature.quran.data.local.entity.SurahEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class QuranAssetLoader(
    private val context: Context,
    private val dao: QuranDao
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun loadIfNeeded() {
        if (dao.countSurahs() < 114) {
            loadSurahs()
        }
        if (dao.countAyahs() < 6236) { // Ensure full load
            loadAyahs()
        }
        val surahCount = dao.countSurahs()
        val ayahCount = dao.countAyahs()
        if (surahCount == 114 && ayahCount == 6236) {
            android.util.Log.d("QuranAssetLoader", "Data validation successful: 114 Surahs, 6236 Ayahs.")
        } else {
            android.util.Log.e("QuranAssetLoader", "Data validation failed: Surahs: $surahCount, Ayahs: $ayahCount")
        }
    }

    private suspend fun loadSurahs() {
        val raw = try {
            context.assets.open(FILE_METADATA).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            return
        }
        val data = json.decodeFromString<MetadataDto>(raw)
        dao.insertSurahs(data.surahs.map { it.toEntity() })
    }

    private suspend fun loadAyahs() {
        val tafsirMap = try {
            val rawMeta = context.assets.open(FILE_METADATA).bufferedReader().use { it.readText() }
            val meta = json.decodeFromString<MetadataDto>(rawMeta)
            meta.ayahs.associateBy { "${it.surahId}_${it.verseNumber}" }
        } catch (e: Exception) {
            emptyMap()
        }

        val raw = try {
            context.assets.open(FILE_FULL_QURAN).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            return
        }
        val surahs = json.decodeFromString<List<FullSurahDto>>(raw)
        val allAyahs = surahs.flatMap { surah ->
            surah.verses.map { verse ->
                val tafsir = tafsirMap["${surah.id}_${verse.id}"]?.tafsir
                AyahEntity(
                    surahId = surah.id,
                    verseNumber = verse.id,
                    text = verse.text,
                    tafsir = tafsir
                )
            }
        }
        dao.insertAyahs(allAyahs)
    }

    @Serializable
    private data class MetadataDto(
        val surahs: List<SurahDto>,
        val ayahs: List<AyahDto> = emptyList()
    )

    @Serializable
    private data class AyahDto(
        val surahId: Int,
        val verseNumber: Int,
        val tafsir: String? = null
    )

    @Serializable
    private data class FullSurahDto(
        val id: Int,
        val verses: List<FullVerseDto>
    )

    @Serializable
    private data class FullVerseDto(
        val id: Int,
        val text: String
    )

    @Serializable
    private data class SurahDto(
        val id: Int,
        val name: String,
        val englishName: String,
        val revelationType: String,
        val totalVerses: Int,
        val startPage: Int
    ) {
        fun toEntity() = SurahEntity(
            id = id,
            name = name,
            englishName = englishName,
            revelationType = revelationType,
            totalVerses = totalVerses,
            startPage = startPage
        )
    }

    private companion object {
        const val FILE_METADATA = "quran.json"
        const val FILE_FULL_QURAN = "quran_full.json"
    }
}
