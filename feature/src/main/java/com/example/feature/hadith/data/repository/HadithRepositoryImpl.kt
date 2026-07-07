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
            val initialHadiths = listOf(
                HadithEntity(
                    text = "إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى",
                    narrator = "عمر بن الخطاب",
                    source = "صحيح البخاري ومسلم",
                    category = "الإيمان"
                    , explanation = "شرح مؤقت: يُعرف أن النية أهم عامل في قبول العمل. TODO: استبدال بشرح تفصيلي"
                ),
                HadithEntity(
                    text = "خيركم من تعلم القرآن وعلمه",
                    narrator = "عثمان بن عفان",
                    source = "صحيح البخاري",
                    category = "القرآن"
                    , explanation = "شرح مؤقت: يحث على تعلم القرآن وتعليمه. TODO: استبدال بشرح تفصيلي"
                ),
                HadithEntity(
                    text = "لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه",
                    narrator = "أنس بن مالك",
                    source = "صحيح البخاري ومسلم",
                    category = "الأخلاق"
                    , explanation = "شرح مؤقت: المبدأ الذاتي للعلاقات والأخلاق الإسلامية. TODO: استبدال بشرح تفصيلي"
                ),
                HadithEntity(
                    text = "من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت",
                    narrator = "أبو هريرة",
                    source = "صحيح البخاري ومسلم",
                    category = "الأخلاق"
                    , explanation = "شرح مؤقت: أهمية ضبط اللسان والتزام الخير. TODO: استبدال بشرح تفصيلي"
                ),
                HadithEntity(
                    text = "الدين النصيحة",
                    narrator = "تميم الداري",
                    source = "صحيح مسلم",
                    category = "الإيمان"
                    , explanation = "شرح مؤقت: الحث على النصيحة كجزء من الدين. TODO: استبدال بشرح تفصيلي"
                )
            )
            dao.insertHadiths(initialHadiths)
        }
    }
}
