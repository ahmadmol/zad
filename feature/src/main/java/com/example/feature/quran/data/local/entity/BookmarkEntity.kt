package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahId: Int,
    val verseNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

fun BookmarkEntity.toDomain(surahName: String, verseText: String): com.example.feature.quran.domain.model.Bookmark = 
    com.example.feature.quran.domain.model.Bookmark(
        id = id,
        surahId = surahId,
        verseNumber = verseNumber,
        surahName = surahName,
        text = verseText
    )
