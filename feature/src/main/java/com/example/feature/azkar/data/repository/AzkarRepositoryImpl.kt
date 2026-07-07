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

    override fun getAllAzkar(): Flow<List<Zikr>> {
        return dao.getAllAzkar().map { list -> list.map { it.toDomain() } }
    }

    override fun getAzkarByCategory(category: String): Flow<List<Zikr>> {
        return dao.getAzkarByCategory(category).map { list -> list.map { it.toDomain() } }
    }

    override fun getFavoriteAzkar(): Flow<List<Zikr>> {
        return dao.getFavoriteAzkar().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateZikrCount(zikrId: Long, newCount: Int) {
        dao.updateZikrCount(zikrId, newCount, dateProvider.getCurrentDate())
    }

    override suspend fun incrementCounter(zikrId: Long) {
        val date = dateProvider.getCurrentDate()
        val zikr = dao.getZikrById(zikrId) ?: return
        if (zikr.category == "سبحة حرة" || zikr.currentCount < zikr.targetCount) {
            dao.incrementZikrCount(zikrId, date)
            updateDailyStat(date)
        }
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
        return dao.getLast7DaysStats().map { list ->
            list.map { DailyStat(it.date, it.totalCount) }
        }
    }
}