package com.example.feature.fasting.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_log")
data class FastingLogEntity(@PrimaryKey val dateEpochDay: Long, val status: String,
    val typeLabel: String, val updatedAtEpochMillis: Long)
