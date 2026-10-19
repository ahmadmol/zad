package com.example.feature.prayer

import com.example.feature.dashboard.presentation.ArabicClockFormatter
import com.example.feature.prayer.domain.clock.PrayerClock
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class TimezoneChangeRegressionTest {
    @Test fun existingDefaultClockAndFormatterFollowChangedSystemZone() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val clock = PrayerClock()
            val fixedDate = Date.from(Instant.parse("2026-10-15T08:00:00Z"))
            assertEquals("08:00 ص", ArabicClockFormatter.format(fixedDate))
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            assertEquals(ZoneId.of("Asia/Tokyo"), clock.zoneId())
            assertEquals("05:00 م", ArabicClockFormatter.format(fixedDate))
            assertEquals("05:00:00 م", ArabicClockFormatter.format(fixedDate, true))
        } finally { TimeZone.setDefault(original) }
    }

    @Test fun injectedFixedClockRetainsItsExplicitZoneAndDate() {
        val original = TimeZone.getDefault()
        try {
            val instant = Instant.parse("2026-10-15T23:30:00Z")
            val clock = PrayerClock(Clock.fixed(instant, ZoneId.of("UTC")))
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            assertEquals(ZoneId.of("UTC"), clock.zoneId())
            assertEquals("2026-10-15", clock.today().toString())
            assertEquals(instant.toEpochMilli(), clock.nowEpochMillis())
            assertEquals("2026-10-16", clock.withZone(ZoneId.of("Asia/Tokyo")).today().toString())
        } finally { TimeZone.setDefault(original) }
    }
}
