package com.example.feature.asma.data.local

import android.content.Context
import com.example.feature.asma.domain.model.AllahName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

// DTO لتحويل JSON إلى كائنات Kotlin
@Serializable
private data class AsmaJsonDto(
    val id: Int,
    val name: String,
    val transliteration: String,
    val meaning: String,
    val explanation: String
)

class AsmaLocalDataSource(private val context: Context) {

    // 1. الـ Cache: StateFlow لتخزين القائمة في الذاكرة
    private val _asmaCache = MutableStateFlow<List<AllahName>>(emptyList())

    // 2. نعرضها كـ StateFlow غير قابل للتعديل من الخارج
    val asmaFlow: StateFlow<List<AllahName>> = _asmaCache.asStateFlow()

    // 3. دالة لتحميل البيانات مرة واحدة فقط
    suspend fun loadAsmaIfNeeded() {
        // إذا كانت القائمة فارغة، نقوم بالتحميل
        if (_asmaCache.value.isEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val jsonString = context.assets.open("asma.json").bufferedReader().use { it.readText() }
                    val json = Json { ignoreUnknownKeys = true }
                    val dtos = json.decodeFromString<List<AsmaJsonDto>>(jsonString)

                    val asmaList = dtos.map { dto ->
                        AllahName(
                            id = dto.id,
                            name = dto.name,
                            transliteration = dto.transliteration,
                            meaning = dto.meaning,
                            explanation = dto.explanation
                        )
                    }

                    // تحديث الـ Cache
                    _asmaCache.value = asmaList
                } catch (e: IOException) {
                    // في حالة فشل قراءة الملف، نطبع الخطأ (يمكن تحسينه لاحقاً)
                    e.printStackTrace()
                }
            }
        }
    }
}