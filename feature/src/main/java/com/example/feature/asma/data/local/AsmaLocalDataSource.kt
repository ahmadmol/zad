package com.example.feature.asma.data.local

import android.content.Context
import com.example.feature.asma.domain.model.AllahName
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Serializable
private data class AsmaJsonDto(
    val id: Int,
    val name: String,
    val transliteration: String,
    val meaning: String,
    val explanation: String
)

class AsmaLocalDataSource(
    private val context: Context,
    private val userPreferences: UserPreferences
) {
    private val _asmaCache = MutableStateFlow<List<AllahName>>(emptyList())
    val asmaFlow: StateFlow<List<AllahName>> = _asmaCache.asStateFlow()

    private val lock = Mutex()
    suspend fun loadAsmaIfNeeded() = lock.withLock {
        withContext(Dispatchers.IO) {
                if (_asmaCache.value.isEmpty()) {
                    val jsonString = context.assets.open("asma.json").bufferedReader().use { it.readText() }
                    val json = Json { ignoreUnknownKeys = true }
                    val dtos = json.decodeFromString<List<AsmaJsonDto>>(jsonString)
                    val favoriteIds = userPreferences.favoriteAsmaIds.first().toSet()

                    val asmaList = dtos.map { dto ->
                        AllahName(
                            id = dto.id,
                            name = dto.name,
                            transliteration = dto.transliteration,
                            meaning = dto.meaning,
                            explanation = dto.explanation,
                            isFavorite = favoriteIds.contains(dto.id.toString())
                        )
                    }

                    _asmaCache.value = asmaList
                } else {
                    val favoriteIds = userPreferences.favoriteAsmaIds.first().toSet()
                    _asmaCache.value = _asmaCache.value.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id.toString()))
                    }
                }
        }
    }

    suspend fun toggleFavorite(id: Int) = lock.withLock {
        if (_asmaCache.value.none { it.id == id }) return@withLock
        userPreferences.toggleFavoriteAsmaId(id)
        val saved = userPreferences.favoriteAsmaIds.first()
        _asmaCache.value = _asmaCache.value.map { it.copy(isFavorite = it.id.toString() in saved) }
    }
}
