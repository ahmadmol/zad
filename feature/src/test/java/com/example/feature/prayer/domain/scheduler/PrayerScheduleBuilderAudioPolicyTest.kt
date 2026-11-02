package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerAlarmRequest
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Builder-level coverage for current timing, kind, ordering, and stable identity. */
class PrayerScheduleBuilderAudioPolicyTest {
    private val zone = ZoneId.of("Asia/Riyadh")
    private val date = LocalDate.of(2026, 8, 12)
    private val location = PrayerLocation(24.7136, 46.6753, "Riyadh")
    private val times = mapOf(
        PrayerName.FAJR to LocalTime.of(4, 10),
        PrayerName.SUNRISE to LocalTime.of(5, 46),
        PrayerName.DHUHR to LocalTime.of(12, 38),
        PrayerName.ASR to LocalTime.of(16, 22),
        PrayerName.MAGHRIB to LocalTime.of(18, 56),
        PrayerName.ISHA to LocalTime.of(20, 15)
    )
    private val now = date.atTime(0, 5).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `pre prayer reminder is offset while exact prayer time is unchanged`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10)).today()
        val pre = alarms.single { it.prayerName == PrayerName.ASR && it.kind == PrayerAlarmKind.PRE_PRAYER }
        val exact = alarms.single { it.prayerName == PrayerName.ASR && it.kind == PrayerAlarmKind.EXACT }

        assertEquals(epochOf(date, LocalTime.of(16, 12)), pre.triggerEpochMillis)
        assertEquals(epochOf(date, LocalTime.of(16, 22)), exact.triggerEpochMillis)
        assertNotEquals(pre.stableId, exact.stableId)
    }

    @Test
    fun `exact alarms retain every calculated time`() {
        val withReminders = build(PrayerSchedulePolicy(prePrayerMinutes = 30, iqamahMinutes = 20)).today()
        val withoutReminders = build(PrayerSchedulePolicy(prePrayerMinutes = 0, iqamahMinutes = 0)).today()

        times.forEach { (prayer, time) ->
            val expected = epochOf(date, time)
            assertEquals(expected, withReminders.single { it.prayerName == prayer && it.kind == PrayerAlarmKind.EXACT }.triggerEpochMillis)
            assertEquals(expected, withoutReminders.single { it.prayerName == prayer && it.kind == PrayerAlarmKind.EXACT }.triggerEpochMillis)
        }
    }

    @Test
    fun `sunrise has only an exact marker even when reminders are enabled`() {
        val sunrise = build(
            PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 10)
        ).today().filter { it.prayerName == PrayerName.SUNRISE }

        assertEquals(listOf(PrayerAlarmKind.EXACT), sunrise.map { it.kind })
        assertEquals(epochOf(date, LocalTime.of(5, 46)), sunrise.single().triggerEpochMillis)
    }

    @Test
    fun `end reminder is disabled by zero and enabled by a positive offset`() {
        val disabled = build(PrayerSchedulePolicy(endOfPrayerReminderMinutesBeforeNext = 0))
        val enabled = build(PrayerSchedulePolicy(endOfPrayerReminderMinutesBeforeNext = 15))

        assertTrue(disabled.none { it.kind == PrayerAlarmKind.END_REMINDER })
        assertTrue(enabled.any { it.kind == PrayerAlarmKind.END_REMINDER })
    }

    @Test
    fun `colliding reminders retain distinct stable ids`() {
        val alarms = build(
            PrayerSchedulePolicy(prePrayerMinutes = 15, endOfPrayerReminderMinutesBeforeNext = 15)
        ).today()
        val collisionInstant = epochOf(date, LocalTime.of(16, 7))
        val colliding = alarms.filter { it.triggerEpochMillis == collisionInstant }

        assertEquals(
            setOf(
                PrayerName.ASR to PrayerAlarmKind.PRE_PRAYER,
                PrayerName.DHUHR to PrayerAlarmKind.END_REMINDER
            ),
            colliding.map { it.prayerName to it.kind }.toSet()
        )
        assertEquals(2, colliding.map { it.stableId }.toSet().size)
    }

    @Test
    fun `stable identities are unique across the two day schedule`() {
        val alarms = build(
            PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 10, endOfPrayerReminderMinutesBeforeNext = 15)
        )
        assertEquals(alarms.size, alarms.map { it.stableId }.toSet().size)
    }

    @Test
    fun `schedule output is chronologically sorted`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 10))
        alarms.zipWithNext().forEach { (a, b) -> assertTrue(a.triggerEpochMillis <= b.triggerEpochMillis) }
    }

    private fun List<PrayerAlarmRequest>.today(): List<PrayerAlarmRequest> {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return filter { it.triggerEpochMillis in start until end }
    }

    private fun build(policy: PrayerSchedulePolicy): List<PrayerAlarmRequest> =
        PrayerScheduleBuilder.build(
            today = dayOf(date),
            tomorrow = dayOf(date.plusDays(1)),
            location = location,
            policy = policy,
            nowEpochMillis = now,
            settingsFingerprint = "test"
        ).alarms

    private fun dayOf(day: LocalDate) = PrayerDay(
        dateEpochDay = day.toEpochDay(),
        timeZoneId = zone.id,
        location = location,
        settings = PrayerCalculationSettings(),
        instants = times.map { (name, time) -> PrayerInstant(name, epochOf(day, time)) }
    )

    private fun epochOf(day: LocalDate, time: LocalTime): Long =
        day.atTime(time).atZone(zone).toInstant().toEpochMilli()
}
