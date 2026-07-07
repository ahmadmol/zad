package com.example.feature.quran.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.AyahEntity
import com.example.feature.quran.data.local.entity.BookmarkEntity
import com.example.feature.quran.data.local.entity.SurahEntity

@Database(
    entities = [SurahEntity::class, AyahEntity::class, BookmarkEntity::class],
    version = 4,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}
