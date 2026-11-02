package com.example.feature.prayer.presentation

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Format in the calculation zone, independent of the OS language/time zone. */
object PrayerTimeFormatter {
    fun format(epochMillis: Long, zoneId: ZoneId, locale: Locale): String =
        DateTimeFormatter.ofPattern("hh:mm a", locale)
            .format(Instant.ofEpochMilli(epochMillis).atZone(zoneId))
}
