package com.example.feature.azkar.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.ZikerEntity

@Database(
    entities = [ZikerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AzkarDatabase: RoomDatabase(){
    abstract fun azkarDao(): AzkarDao
}