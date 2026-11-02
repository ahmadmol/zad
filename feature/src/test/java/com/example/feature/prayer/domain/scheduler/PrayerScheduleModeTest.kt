package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Current schedule-policy and settings-fingerprint behavior. */
class PrayerScheduleModeTest {
    private val location = PrayerLocation(30.0, 31.0, "Cairo")
    private val now = 1_000_000L
    private val step = 3_600_000L

    @Test
    fun `include sunrise schedules exact alarm for every time marker`() {
        val exact = build(PrayerSchedulePolicy(includeSunrise = true)).alarms
            .filter { it.kind == PrayerAlarmKind.EXACT }
            .map { it.prayerName }.toSet()
        assertEquals(PrayerName.entries.toSet(), exact)
    }

    @Test
    fun `excluding sunrise preserves the five prayer exact alarms`() {
        val exact = build(PrayerSchedulePolicy(includeSunrise = false)).alarms
            .filter { it.kind == PrayerAlarmKind.EXACT }
            .map { it.prayerName }.toSet()
        assertFalse(exact.contains(PrayerName.SUNRISE))
        assertEquals(PrayerName.entries.filter { it.isNotifiable }.toSet(), exact)
    }

    @Test
    fun `schedule remains sorted for reminder policy`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 5)).alarms
        alarms.zipWithNext().forEach { (a, b) ->
            assertTrue(a.triggerEpochMillis <= b.triggerEpochMillis)
        }
    }

    @Test
    fun `fingerprint is stable for identical inputs`() {
        val a = fingerprint(pre = 10, iqamah = 5)
        val b = fingerprint(pre = 10, iqamah = 5)
        assertEquals(a, b)
    }

    @Test
    fun `fingerprint changes when scheduling settings change`() {
        assertNotEquals(fingerprint(pre = 10, iqamah = 5), fingerprint(pre = 5, iqamah = 20))
    }

    private fun fingerprint(pre: Int, iqamah: Int) = PrayerScheduleBuilder.settingsFingerprint(
        "MWL", "SHAFI", pre, iqamah, location.latitude, location.longitude
    )

    private fun build(policy: PrayerSchedulePolicy) = PrayerScheduleBuilder.build(
        today = day(0),
        tomorrow = day(1),
        location = location,
        policy = policy,
        nowEpochMillis = now,
        settingsFingerprint = "fp"
    )

    private fun day(offsetDays: Int) = PrayerDay(
        dateEpochDay = LocalDate.of(2026, 3, 1).plusDays(offsetDays.toLong()).toEpochDay(),
        timeZoneId = "Africa/Cairo",
        location = location,
        settings = PrayerCalculationSettings(),
        instants = PrayerName.entries.mapIndexed { index, name ->
            PrayerInstant(name, now + offsetDays * 24 * step + (index + 1) * step)
        }
    )
}
