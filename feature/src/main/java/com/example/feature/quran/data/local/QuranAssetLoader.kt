package com.example.feature.quran.data.local

import android.content.Context
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.AyahEntity
import com.example.feature.quran.data.local.entity.SurahEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuranAssetLoader(
    private val context: Context,
    private val dao: QuranDao
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val editionLock = Mutex()
    private var editionLoaded = false

    suspend fun loadIfNeeded() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        editionLock.withLock {
            if (editionLoaded && dao.countSurahs() == 114 && dao.countAyahs() == 6236) return@withLock
            // Parse/validate both assets before touching any existing rows. Failures propagate to retry UI.
            val metadata = json.decodeFromString<MetadataDto>(
                context.assets.open(FILE_METADATA).bufferedReader().use { it.readText() })
            val surahs = json.decodeFromString<List<FullSurahDto>>(
                context.assets.open(FILE_FULL_QURAN).bufferedReader().use { it.readText() })
            require(metadata.surahs.map { it.id } == (1..114).toList()) { "Invalid Quran metadata" }
            require(surahs.map { it.id } == (1..114).toList()) { "Invalid Quran edition" }
            val allAyahs = surahs.flatMap { surah ->
                val expected = metadata.surahs[surah.id - 1].totalVerses
                require(surah.verses.map { it.id } == (1..expected).toList()) { "Incomplete Quran surah" }
                surah.verses.map { verse ->
                    require(verse.text.isNotBlank()) { "Empty Quran verse" }
                    AyahEntity(surahId = surah.id, verseNumber = verse.id, text = verse.text, tafsir = null)
                }
            }
            require(allAyahs.size == 6236) { "Incomplete canonical Quran asset" }
            dao.applyCanonicalContent(metadata.surahs.map { it.toEntity() }, allAyahs)
            check(dao.countSurahs() == 114 && dao.countAyahs() == 6236) { "Quran database validation failed" }
            editionLoaded = true
        }
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
