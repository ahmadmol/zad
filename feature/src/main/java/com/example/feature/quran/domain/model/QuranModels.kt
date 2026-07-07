package com.example.feature.quran.domain.model

data class Surah(
    val id: Int,
    val name: String,
    val revelationType: String, // Meccan / Medinan
    val totalVerses: Int,
    val startPage: Int
)

data class Juz(
    val id: Int,
    val name: String,
    val startSurah: String,
    val startPage: Int
)

data class Verse(
    val id: Int,
    val surahId: Int,
    val verseNumber: Int,
    val text: String,
    val tafsir: String? = null
)

data class Reader(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class Bookmark(
    val id: Long,
    val surahId: Int,
    val verseNumber: Int,
    val surahName: String = "",
    val text: String = ""
)
