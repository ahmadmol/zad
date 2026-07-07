package com.example.feature.azkar.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.DailyStatEntity
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.duas.data.local.dao.DuaDao
import com.example.feature.duas.data.local.entity.DuaEntity

@Database(
    entities = [ZikrEntity::class, DailyStatEntity::class, DuaEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AzkarDatabase: RoomDatabase(){
    abstract fun azkarDao(): AzkarDao
    abstract fun duaDao(): DuaDao
}