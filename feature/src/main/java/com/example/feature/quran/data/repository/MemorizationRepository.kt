package com.example.feature.quran.data.repository

import com.example.feature.quran.data.local.dao.*
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.domain.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class MemorizationRepository(private val dao: MemorizationDao, private val quran: QuranDao,
    private val clock: Clock? = null) {
    val plans = dao.plans()
    val reviews = dao.reviews()
    val surahs = quran.observeAllSurahs()
    private val writes = Mutex()
    fun today() = (clock?.let { LocalDate.now(it) } ?: LocalDate.now()).toEpochDay()
    suspend fun verses(surah: Int, first: Int, last: Int): List<AyahEntity> {
        val rows = quran.getAyahsBySurah(surah).filter { it.verseNumber in first..last }
        require(first > 0 && last >= first && rows.map { it.verseNumber } == (first..last).toList()) {
            "نطاق الآيات غير صحيح أو لم يكتمل تحميل القرآن المعتمد"
        }
        return rows
    }
    suspend fun save(id: String?, surah: Int, first: Int, last: Int) = writes.withLock {
        require(surah in 1..114 && first in 1..286 && last in first..286) { "تحقق من السورة ونطاق الآيات" }
        verses(surah, first, last)
        val replacement = MemorizationPlanEntity(UUID.randomUUID().toString(), surah, first, last, today())
        if (id == null) dao.insert(replacement) else {
            val old = requireNotNull(dao.plan(id)) { "الخطة غير موجودة؛ أعد تحميل السجل" }
            require(!old.archived) { "الخطة مؤرشفة" }
            // Range edits create a revision; prior confirmations and reviews keep their original keys.
            dao.replaceRange(old, replacement)
        }
    }
    suspend fun archive(id: String) = writes.withLock {
        dao.update(requireNotNull(dao.plan(id)) { "الخطة غير موجودة؛ أعد تحميل السجل" }.copy(archived = true))
    }
    suspend fun confirm(id: String) = writes.withLock {
        val plan = requireNotNull(dao.plan(id)) { "الخطة غير موجودة؛ أعد تحميل السجل" }
        require(!plan.archived) { "الخطة مؤرشفة" }
        val day = today()
        if (plan.confirmedDay == null) dao.update(plan.copy(confirmedDay = day, dueDay = day + 1))
    }
    suspend fun reschedule(id: String, dueDay: Long) = writes.withLock {
        val plan = requireNotNull(dao.plan(id)) { "الخطة غير موجودة؛ أعد تحميل السجل" }
        require(!plan.archived && plan.confirmedDay != null) { "أكّد الحفظ أولًا" }
        val day = today()
        require(dueDay in (day+1)..(day+365)) { "اختر موعدًا من الغد وحتى سنة" }
        require(plan.lastReviewDay == null || dueDay > plan.lastReviewDay) { "الموعد يجب أن يلي آخر مراجعة" }
        dao.update(plan.copy(dueDay=dueDay))
    }
    suspend fun review(id: String, result: ReviewResult) = writes.withLock {
        val plan = requireNotNull(dao.plan(id)) { "الخطة غير موجودة؛ أعد تحميل السجل" }
        val day = today()
        require(!plan.archived && plan.confirmedDay != null && plan.dueDay != null) { "أكّد الحفظ أولًا" }
        require(day >= plan.dueDay && (plan.lastReviewDay == null || day > plan.lastReviewDay)) {
            "المراجعة المسجلة متاحة عند موعدها، مرة واحدة في اليوم"
        }
        val next = ReviewSchedule.next(plan.stage, result, day)
        dao.record(plan.copy(stage = next.stage, dueDay = next.dueDay, lastReviewDay = day),
            MemorizationReviewEntity(UUID.randomUUID().toString(), id, day, result.name, next.dueDay))
    }
}
