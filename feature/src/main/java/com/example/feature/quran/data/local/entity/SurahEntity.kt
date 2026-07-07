package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.quran.domain.model.Surah

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val totalVerses: Int,
    val startPage: Int
)

fun SurahEntity.toDomain(): Surah = Surah(
    id = id,
    name = name,
    revelationType = revelationType,
    totalVerses = totalVerses,
    startPage = startPage
)
