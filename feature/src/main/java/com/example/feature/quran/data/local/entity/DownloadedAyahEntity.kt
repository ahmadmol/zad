package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_ayahs")
data class DownloadedAyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahId: Int,
    val verseNumber: Int,
    val readerId: String,
    val localPath: String
)
