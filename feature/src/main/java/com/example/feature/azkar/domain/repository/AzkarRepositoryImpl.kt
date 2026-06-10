package com.example.feature.azkar.domain.repository

import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.toDomain
import com.example.feature.azkar.domain.model.Ziker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AzkarRepositoryImpl(
    private val dao: AzkarDao
): AzkarRepository{
    override fun getAllAzkar(): Flow<List<Ziker>> {
       return dao.getAllAzkar().map{
           entityList -> entityList.map { it.toDomain()}
       }
    }
    override suspend fun incrementCounter(ZikerId: Long) {
        val zikerEntity = dao.getZikrById(ZikerId)
        if (zikerEntity != null) {
            dao.updateZikrCount(zikrId = ZikerId,zikerEntity.currentCount + 1)
        }
    }
    override suspend fun resetCounter(ZikerId: Long) {
     dao.updateZikrCount(zikrId = ZikerId,0)
    }
}