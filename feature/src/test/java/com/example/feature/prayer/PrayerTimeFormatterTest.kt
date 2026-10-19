package com.example.feature.prayer

import com.example.feature.prayer.presentation.PrayerTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class PrayerTimeFormatterTest {
    @Test fun arabicUiUsesArabicPeriodAndCalculationZone() {
        val instant = Instant.parse("2026-10-15T10:30:00Z").toEpochMilli()
        val ar = PrayerTimeFormatter.format(instant, ZoneId.of("Asia/Damascus"), Locale("ar"))
        assertFalse(ar.contains("AM") || ar.contains("PM"))
        assertTrue(ar.contains("م"))
        assertEquals("01:30 PM", PrayerTimeFormatter.format(instant, ZoneId.of("Asia/Damascus"), Locale.US))
        assertEquals("10:30 AM", PrayerTimeFormatter.format(instant, ZoneId.of("UTC"), Locale.US))
    }
}
