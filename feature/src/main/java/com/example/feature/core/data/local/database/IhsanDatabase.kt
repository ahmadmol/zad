package com.example.feature.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.quran.data.local.entity.ReadingPlanEntity
import com.example.feature.quran.data.local.entity.ReadingPageCompletionEntity
import com.example.feature.quran.data.local.entity.PrivateAyahNoteEntity
import com.example.feature.quran.data.local.dao.ReadingJourneyDao
import com.example.feature.prayer.data.local.entity.PrayerLogEntity
import com.example.feature.prayer.data.local.dao.PrayerLogDao
import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.DailyStatEntity
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.duas.data.local.dao.DuaDao
import com.example.feature.duas.data.local.entity.DuaEntity
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.ehsan.data.local.entity.DonationEntity
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.hadith.data.local.dao.HadithDao
import com.example.feature.hadith.data.local.entity.HadithEntity
import com.example.feature.quran.data.local.dao.DownloadDao
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.entity.AyahEntity
import com.example.feature.quran.data.local.entity.BookmarkEntity
import com.example.feature.quran.data.local.entity.DownloadedAyahEntity
import com.example.feature.quran.data.local.entity.SurahEntity

@Database(
    entities = [
        ZikrEntity::class, 
        DailyStatEntity::class, 
        DuaEntity::class,
        DonationEntity::class, 
        UserEntity::class, 
        HadithEntity::class,
        SurahEntity::class, 
        AyahEntity::class,
        BookmarkEntity::class,
        DownloadedAyahEntity::class,
        ReadingPlanEntity::class,
        ReadingPageCompletionEntity::class,
        PrivateAyahNoteEntity::class,
        com.example.feature.quran.data.local.entity.MemorizationPlanEntity::class,
        com.example.feature.quran.data.local.entity.MemorizationReviewEntity::class,
        com.example.feature.fasting.data.FastingLogEntity::class,
        PrayerLogEntity::class
    ],
    version = 11,
    exportSchema = true
)
abstract class IhsanDatabase : RoomDatabase() {
    abstract fun azkarDao(): AzkarDao
    abstract fun duaDao(): DuaDao
    abstract fun donationDao(): DonationDao
    abstract fun userDao(): UserDao
    abstract fun hadithDao(): HadithDao
    abstract fun quranDao(): QuranDao
    abstract fun downloadDao(): DownloadDao
    abstract fun readingJourneyDao(): ReadingJourneyDao
    abstract fun memorizationDao(): com.example.feature.quran.data.local.dao.MemorizationDao
    abstract fun fastingLogDao(): com.example.feature.fasting.data.FastingLogDao
    abstract fun prayerLogDao(): PrayerLogDao
}
