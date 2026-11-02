package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocation
import java.time.LocalDate

interface PrayerCalculator {
    fun calculate(
        date: LocalDate,
        location: PrayerLocation,
        settings: PrayerCalculationSettings,
        timeZoneId: String,
        nowEpochMillis: Long
    ): PrayerDay
}
