package com.example.feature.quran.data.repository

import com.example.feature.quran.data.local.dao.ReadingJourneyDao
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.domain.model.ReadingPlan
import com.example.feature.quran.domain.repository.ReadingJourneyRepository
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReadingJourneyRepositoryImpl(private val dao: ReadingJourneyDao, private val quran: QuranDao,
    private val clock: Clock? = null) : ReadingJourneyRepository {
    private val planWrites = Mutex()
    private val noteWrites = Mutex()
    private fun today() = clock?.let { LocalDate.now(it) } ?: LocalDate.now()
    private fun nowMillis() = clock?.millis() ?: System.currentTimeMillis()
    override val plans = dao.observePlans().map { rows -> rows.map { it.toDomain() } }
    override val completedPages = dao.observeCompletions().map { rows ->
        rows.groupBy { it.planId }.mapValues { (_, pages) -> pages.map { it.pageNumber }.toSet() }
    }
    override val notes = dao.observeNotes().map { rows -> rows.map { it.toDomain() } }

    override suspend fun createPlan(firstPage: Int, lastPage: Int, days: Int) = planWrites.withLock {
        require(days in 1..3650) { "اختر مدة بين يوم و3650 يومًا" }
        val today = today().toEpochDay()
        val plan = ReadingPlan(UUID.randomUUID().toString(), today, today + days - 1, firstPage, lastPage)
        dao.replaceActive(ReadingPlanEntity(plan.id, today, plan.dueEpochDay, firstPage, lastPage, true))
    }
    override suspend fun extendPlan(id: String, daysFromToday: Int) = planWrites.withLock {
        require(daysFromToday in 1..3650)
        val plan = requireNotNull(dao.plan(id))
        require(plan.active) { "الخطة مؤرشفة" }
        val due = today().toEpochDay() + daysFromToday - 1
        require(due >= plan.dueEpochDay && due - plan.startEpochDay < 3650) { "اختر تاريخًا لاحقًا ضمن عشر سنوات" }
        dao.extend(id, due)
    }
    override suspend fun confirmPages(id: String, pages: Set<Int>) = planWrites.withLock {
        val plan = requireNotNull(dao.plan(id))
        require(plan.active && pages.all { it in plan.firstPage..plan.lastPage })
        dao.insertCompletions(pages.sorted().map { ReadingPageCompletionEntity(id, it, nowMillis()) })
    }
    override suspend fun clearPage(id: String, page: Int) = planWrites.withLock {
        val plan = requireNotNull(dao.plan(id))
        require(plan.active && page in plan.firstPage..plan.lastPage)
        dao.clearPage(id, page)
    }
    override suspend fun saveNote(id: String?, surahId: Int, ayahNumber: Int, text: String) = noteWrites.withLock {
        require(text.isNotBlank() && text.length <= 4000) { "اكتب ملاحظة من 1 إلى 4000 حرف" }
        requireNotNull(quran.getAyahBySurahAndVerse(surahId, ayahNumber)) { "مرجع الآية غير صحيح أو لم يكتمل تحميل القرآن" }
        val existing = id?.let { requireNotNull(dao.note(it)) { "الملاحظة غير موجودة" } }
        require(existing == null || (existing.surahId == surahId && existing.ayahNumber == ayahNumber))
        dao.saveNote(PrivateAyahNoteEntity(existing?.id ?: UUID.randomUUID().toString(), surahId, ayahNumber,
            text.trim(), existing?.createdAt ?: nowMillis(), nowMillis()))
    }
    override suspend fun deleteNote(id: String) = noteWrites.withLock { dao.deleteNote(id) }
}
