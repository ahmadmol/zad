package com.example.feature.azkar.data.repository

import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.DailyStatEntity
import com.example.feature.azkar.data.local.entity.toDomain
import com.example.feature.azkar.data.local.entity.toEntity
import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.repository.AzkarRepository
import com.example.feature.azkar.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AzkarRepositoryImpl(
    private val dao: AzkarDao,
    private val dateProvider: DateProvider
) : AzkarRepository {

    private val incrementMutex = kotlinx.coroutines.sync.Mutex()
    private fun dayFlow(): Flow<String> = kotlinx.coroutines.flow.flow {
        while (true) { emit(dateProvider.getCurrentDate()); kotlinx.coroutines.delay(30_000) }
    }

    override fun getAllAzkar(): Flow<List<Zikr>> = kotlinx.coroutines.flow.combine(dao.getAllAzkar(), dayFlow()) { list, date ->
        list.map { row -> row.toDomain().let {
            if (row.lastUpdatedDate != date) it.copy(
                currentCount = if (row.category == "تسبيح") it.currentCount else 0, dailyProgress = 0
            ) else it
        } }
    }

    override fun getAzkarByCategory(category: String): Flow<List<Zikr>> {
        return getAllAzkar().map { list -> list.filter { it.category == category } }
    }

    override fun getFavoriteAzkar(): Flow<List<Zikr>> {
        return getAllAzkar().map { list -> list.filter { it.isFavorite } }
    }

    override suspend fun updateZikrCount(zikrId: Long, newCount: Int) {
        dao.updateZikrCount(zikrId, newCount, dateProvider.getCurrentDate())
    }

    override suspend fun incrementCounter(zikrId: Long) {
        incrementMutex.lock()
        try {
            val date = dateProvider.getCurrentDate()
            val zikr = dao.getZikrById(zikrId) ?: return
            val current = if (zikr.category != "تسبيح" && zikr.lastUpdatedDate != date) 0 else zikr.currentCount
            if (zikr.targetCount == 0 || current < zikr.targetCount) {
                dao.incrementZikrCount(zikrId, date)
                updateDailyStat(date)
            }
        } finally { incrementMutex.unlock() }
    }

    private suspend fun updateDailyStat(date: String) {
        val existingStat = dao.getStatByDate(date)
        if (existingStat == null) {
            dao.insertOrUpdateDailyStat(DailyStatEntity(date = date, totalCount = 1))
        } else {
            dao.incrementDailyTotal(date)
        }
    }

    override suspend fun toggleFavorite(zikrId: Long) {
        dao.toggleFavorite(zikrId)
    }

    override suspend fun resetCounter(zikrId: Long) {
        dao.resetZikrCount(zikrId)
    }

    override suspend fun resetCategoryCounter(category: String) {
        dao.resetCategoryCount(category)
    }

    override suspend fun addZikr(zikr: Zikr) {
        dao.insertZikr(zikr.toEntity(dateProvider.getCurrentDate()))
    }

    override suspend fun deleteZikr(zikrId: Long) {
        dao.deleteZikrById(zikrId)
    }

    override fun getLast7DaysStats(): Flow<List<DailyStat>> {
        return kotlinx.coroutines.flow.combine(getAllDailyStats(), dayFlow()) { rows, date ->
            val end = java.time.LocalDate.parse(date)
            rows.filter { row -> runCatching { java.time.LocalDate.parse(row.date) }.getOrNull()?.let { it >= end.minusDays(6) && it <= end } == true }
        }
    }

    override fun getAllDailyStats(): Flow<List<DailyStat>> = dao.getAllDailyStats().map { rows ->
        rows.groupBy { row -> row.date.map { char -> Character.digit(char, 10).takeIf { it >= 0 }?.let { ('0'.code + it).toChar() } ?: char }.joinToString("") }
            .map { (date, entries) -> DailyStat(date, entries.sumOf { it.totalCount }) }.sortedBy { it.date }
    }
}
