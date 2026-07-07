package com.example.feature.hadith.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.hadith.data.local.dao.HadithDao
import com.example.feature.hadith.data.local.entity.HadithEntity

@Database(entities = [HadithEntity::class], version = 1, exportSchema = false)
abstract class HadithDatabase : RoomDatabase() {
    abstract fun hadithDao(): HadithDao
}
