package com.example.feature.prayer.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Phase 6 — Manual Prayer Tracker.
 *
 * One row per (day, prayer) that the user has explicitly logged. The app never
 * writes a row on the user's behalf and never infers a "missed" prayer: the absence
 * of a row means "not logged", not "not prayed". Statistics reads only what the user
 * chose to record.
 *
 * The composite primary key makes logging idempotent — re-marking the same prayer
 * on the same day updates the existing row instead of accumulating duplicates.
 */
@Entity(
    tableName = "prayer_log",
    primaryKeys = ["dateEpochDay", "prayerName"],
    indices = [Index("dateEpochDay")]
)
data class PrayerLogEntity(
    /** Local civil date, as [java.time.LocalDate.toEpochDay]. */
    val dateEpochDay: Long,

    /** [com.example.feature.prayer.domain.model.PrayerName] name. */
    val prayerName: String,

    /** [com.example.feature.prayer.domain.model.ManualPrayerStatus] name. */
    val status: String,

    /** When the user recorded it. Used only for ordering / debugging. */
    val loggedAtEpochMillis: Long
)
