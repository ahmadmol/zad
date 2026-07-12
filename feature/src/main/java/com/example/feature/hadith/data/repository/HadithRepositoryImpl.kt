package com.example.feature.hadith.data.repository

import com.example.feature.hadith.data.local.dao.HadithDao
import com.example.feature.hadith.data.local.entity.HadithEntity
import com.example.feature.hadith.domain.model.Hadith
import com.example.feature.hadith.domain.repository.HadithRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HadithRepositoryImpl(private val dao: HadithDao) : HadithRepository {
    override fun getAllHadiths(): Flow<List<Hadith>> = 
        dao.getAllHadiths().map { entities -> entities.map { it.toDomain() } }

    override fun getHadithsByCategory(category: String): Flow<List<Hadith>> =
        dao.getHadithsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override fun getRandomHadith(): Flow<Hadith?> =
        dao.getRandomHadith().map { it?.toDomain() }

    override suspend fun toggleFavorite(hadithId: Long) {
        val all = dao.getAllHadiths().first()
        val hadith = all.find { it.id == hadithId }
        hadith?.let {
            dao.updateHadith(it.copy(isFavorite = !it.isFavorite))
        }
    }

    override suspend fun initialPopulation() {
        if (dao.getCount() == 0) {
            dao.insertHadiths(seedHadiths())
        } else {
            refreshPlaceholderExplanations()
        }
    }

    private suspend fun refreshPlaceholderExplanations() {
        val existing = dao.getAllHadiths().first()
        val explanationsByText = seedHadiths().associate { it.text to it.explanation }
        existing.forEach { entity ->
            val improved = explanationsByText[entity.text]
            val needsUpdate = entity.explanation.isNullOrBlank() ||
                entity.explanation.contains("TODO") ||
                entity.explanation.contains("شرح مؤقت")
            if (improved != null && needsUpdate) {
                dao.updateHadith(entity.copy(explanation = improved))
            }
        }
    }

    private fun seedHadiths(): List<HadithEntity> = listOf(
        HadithEntity(
            text = "إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى",
            narrator = "عمر بن الخطاب",
            source = "صحيح البخاري ومسلم",
            category = "الإيمان",
            explanation = "يبيّن الحديث أن قبول العمل وترتّب الأجر عليه مرتبطان بنية صاحبه، فالعبرة بالمقاصد لا بظاهر الأفعال فقط."
        ),
        HadithEntity(
            text = "خيركم من تعلم القرآن وعلمه",
            narrator = "عثمان بن عفان",
            source = "صحيح البخاري",
            category = "القرآن",
            explanation = "يرفع من شأن من يجمع بين تعلّم القرآن وتعليمه للناس، فالخيرية هنا في العلم والعمل وبذل النفع."
        ),
        HadithEntity(
            text = "لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه",
            narrator = "أنس بن مالك",
            source = "صحيح البخاري ومسلم",
            category = "الأخلاق",
            explanation = "يربط كمال الإيمان بحسن الخلق، ومنه أن يتمنّى المسلم لأخيه من الخير ما يتمناه لنفسه دون حسد."
        ),
        HadithEntity(
            text = "من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت",
            narrator = "أبو هريرة",
            source = "صحيح البخاري ومسلم",
            category = "الأخلاق",
            explanation = "يحثّ على ضبط اللسان؛ فإما كلام فيه نفع وخير، وإما صمت يسلم به المرء من الإثم."
        ),
        HadithEntity(
            text = "الدين النصيحة",
            narrator = "تميم الداري",
            source = "صحيح مسلم",
            category = "الإيمان",
            explanation = "يجعل النصيحة أصلًا من أصول الدين: لله ولكتابه ولرسوله ولأئمة المسلمين وعامتهم، بالنصح الصادق لا بالتعيير."
        )
    )
}
