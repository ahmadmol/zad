package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.feature.quran.domain.model.Verse

@Entity(
    tableName = "ayahs",
    indices = [Index(value = ["surahId", "verseNumber"], unique = true)]
)
data class AyahEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahId: Int,
    val verseNumber: Int,
    val text: String,
    val tafsir: String? = null
)

fun AyahEntity.toDomain(): Verse = Verse(
    id = id.toInt(),
    surahId = surahId,
    verseNumber = verseNumber,
    text = text,
    tafsir = tafsir
)
