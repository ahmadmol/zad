package com.example.feature.ehsan.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.ehsan.data.local.entity.DonationEntity
import com.example.feature.ehsan.data.local.entity.UserEntity

@Database(entities = [DonationEntity::class, UserEntity::class], version = 4, exportSchema = false)
abstract class EhsanDatabase : RoomDatabase() {
    abstract fun donationDao(): DonationDao
    abstract fun userDao(): UserDao
}
