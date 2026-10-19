package com.example.feature.duas.data.repository

import android.content.Context
import com.example.feature.duas.data.local.dao.DuaDao
import com.example.feature.duas.data.local.entity.DuaEntity
import com.example.feature.duas.data.local.entity.toDomain
import com.example.feature.duas.domain.model.Dua
import com.example.feature.duas.domain.repository.DuaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DuaRepositoryImpl(private val context: Context, private val dao: DuaDao) : DuaRepository {
    private val lock=Mutex()
    private var loaded=false
    private val edition by lazy { Json { ignoreUnknownKeys=true }.decodeFromString<List<VerifiedDua>>(
        context.assets.open("verified_duas.json").bufferedReader().use { it.readText() }).associateBy { it.id } }
    private fun enriched(row: DuaEntity): Dua = row.toDomain().copy(
        explanation=edition[row.id]?.explanation.orEmpty(), whenToSay=edition[row.id]?.whenToSay.orEmpty())
    override fun getAllDuas(): Flow<List<Dua>> = dao.getAllDuas().map { rows ->
        withContext(Dispatchers.IO) { rows.filter { it.id in edition }.map(::enriched) }
    }
    override fun getFavoriteDuas() = getAllDuas().map { rows -> rows.filter { it.isFavorite } }
    override fun getDuasByCategory(category: String) = getAllDuas().map { rows -> rows.filter { it.category==category } }
    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) { dao.updateFavorite(id,!isFavorite) }
    override suspend fun loadDuasIfNeeded() = lock.withLock {
        if(loaded) return@withLock
        withContext(Dispatchers.IO) {
            val old=dao.getAllDuas().first().associateBy { it.id }
            val previousMatches=mapOf(203293L to 2L,206003L to 4L)
            val rows=edition.values.map { r -> DuaEntity(id=r.id,title=r.title,text=r.text,category=r.category,
                source=r.source,reference=r.reference,isFavorite=old[r.id]?.isFavorite ?: (previousMatches[r.id]?.let(old::get)?.isFavorite==true)) }
            require(rows.size==39)
            dao.insertDuas(rows)
            // Retain unknown old texts/favorites in Room; only the verified edition is displayed.
            loaded=true
        }
    }
    @Serializable private data class VerifiedDua(val id:Long,val title:String,val text:String,val category:String,
        val source:String,val reference:String,val explanation:String="",val whenToSay:String="")
}
