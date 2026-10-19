package com.example.feature.hadith.data.repository

import android.content.Context
import com.example.feature.hadith.data.local.dao.HadithDao
import com.example.feature.hadith.data.local.entity.HadithEntity
import com.example.feature.hadith.domain.model.Hadith
import com.example.feature.hadith.domain.repository.HadithRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Publisher's Arabic v1.7.0, downloaded 2026-10-14. No generated religious text. */
class HadithRepositoryImpl(private val dao: HadithDao, private val context: Context) : HadithRepository {
    private val lock = Mutex()
    private var populated = false
    private val canonicalIds by lazy { corpus().records.map { OFFSET + it.id.toLong() }.toSet() }
    private val edition by lazy { Json { ignoreUnknownKeys = true }.decodeFromString<Corpus>(
        context.assets.open("hadeethenc_ar_v1.7.0.json").bufferedReader().use { it.readText() }) }
    private fun corpus() = edition

    override fun getAllHadiths(): Flow<List<Hadith>> = dao.getAllHadiths().map { rows ->
        withContext(Dispatchers.IO) { rows.filter { it.id in canonicalIds }.map { it.toDomain() } }
    }
    override fun getHadithsByCategory(category: String) = getAllHadiths().map { rows -> rows.filter { it.category == category } }
    override fun getRandomHadith() = getAllHadiths().map { it.randomOrNull() }
    override suspend fun toggleFavorite(hadithId: Long) {
        val row = dao.getAllHadiths().first().find { it.id == hadithId } ?: return
        dao.updateHadith(row.copy(isFavorite = !row.isFavorite))
    }
    override suspend fun initialPopulation() = lock.withLock {
        if (populated) return@withLock
        withContext(Dispatchers.IO) {
            val existing = dao.getAllHadiths().first().associateBy { it.id }
            val legacyFavorites = mapOf(
                "إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى" to 4560L,
                "خيركم من تعلم القرآن وعلمه" to 5913L,
                "لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه" to 4717L,
                "من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت" to 5437L,
                "الدين النصيحة" to 4309L
            ).filterKeys { text -> existing.values.any { it.text == text && it.isFavorite } }.values.toSet()
            val rows = corpus().records.map { r ->
                val id = OFFSET + r.id.toLong()
                HadithEntity(id=id, text=r.hadith_text, narrator="مذكور في النص الأصلي",
                    source="HadeethEnc.com — v1.7.0\n${r.takhrij}\nالحكم كما في المصدر: ${r.grade}\n${r.link}",
                    category="موسوعة الأحاديث", isFavorite=existing[id]?.isFavorite ?: (r.id.toLong() in legacyFavorites),
                    explanation="${r.title}\n\n${r.explanation}\n\nمعاني الكلمات\n${r.word_meanings}\n\nالفوائد\n${r.benefits}\n\n${corpus().publisher_notice}")
            }
            require(rows.size == 3582 && rows.map { it.id }.toSet().size == rows.size)
            dao.insertHadiths(rows)
            // Legacy excerpts remain stored for safe rollback; only the licensed edition is displayed.
            populated = true
        }
    }
    @Serializable private data class Corpus(val publisher_notice: String, val records: List<Record>)
    @Serializable private data class Record(val id: String, val title: String, val hadith_text: String,
        val explanation: String, val word_meanings: String, val benefits: String, val grade: String,
        val takhrij: String, val link: String)
    companion object { const val OFFSET = 100_000_000L }
}
