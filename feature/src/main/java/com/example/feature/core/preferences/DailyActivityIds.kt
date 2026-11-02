package com.example.feature.core.preferences

/** Shared daily checklist activity IDs and targets used across features. */
object DailyActivityIds {
    const val QURAN_READING = "quran_reading"
    const val MORNING_AZKAR = "morning_azkar"
    const val EVENING_AZKAR = "evening_azkar"
    const val TASBEEH = "tasbeeh"
    const val DAILY_DUA = "daily_dua"
    const val DAILY_NAME = "daily_name"

    val ALL = listOf(
        QURAN_READING,
        MORNING_AZKAR,
        EVENING_AZKAR,
        TASBEEH,
        DAILY_DUA,
        DAILY_NAME
    )

    fun targetFor(id: String): Int = when (id) {
        TASBEEH -> 100
        else -> 1
    }
}
