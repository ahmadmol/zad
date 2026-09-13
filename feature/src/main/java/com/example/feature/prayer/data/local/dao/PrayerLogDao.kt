package com.example.feature.prayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feature.prayer.data.local.entity.PrayerLogEntity
import kotlinx.coroutines.flow.Flow

/** Phase 6 — persistence for the Manual Prayer Tracker. */
@Dao
interface PrayerLogDao {

    /** Idempotent: re-logging the same (day, prayer) replaces the previous entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PrayerLogEntity)

    @Query("DELETE FROM prayer_log WHERE dateEpochDay = :dateEpochDay AND prayerName = :prayerName")
    suspend fun delete(dateEpochDay: Long, prayerName: String)

    @Query("SELECT * FROM prayer_log WHERE dateEpochDay = :dateEpochDay")
    fun observeDay(dateEpochDay: Long): Flow<List<PrayerLogEntity>>

    @Query(
        "SELECT * FROM prayer_log WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay " +
            "ORDER BY dateEpochDay ASC"
    )
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<PrayerLogEntity>>

    @Query(
        "SELECT * FROM prayer_log WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay " +
            "ORDER BY dateEpochDay ASC"
    )
    suspend fun getRange(fromEpochDay: Long, toEpochDay: Long): List<PrayerLogEntity>

    /** Used by the Phase 8C "delete my local data" flow. */
    @Query("DELETE FROM prayer_log")
    suspend fun clearAll()
}
