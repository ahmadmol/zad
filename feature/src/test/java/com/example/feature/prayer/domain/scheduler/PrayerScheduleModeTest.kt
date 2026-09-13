package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerAlertMode
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerNotificationModes
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Phase 6 — muted prayers must not be scheduled at all. */
class PrayerScheduleModeTest {

    private val location = PrayerLocation(30.0, 31.0, "Cairo")
    private val now = 1_000_000L
    private val step = 3_600_000L

    private fun day(offsetDays: Int): PrayerDay {
        val base = now + offsetDays * 24 * step
        return PrayerDay(
            dateEpochDay = LocalDate.of(2026, 3, 1).plusDays(offsetDays.toLong()).toEpochDay(),
            timeZoneId = "Africa/Cairo",
            location = location,
            settings = PrayerCalculationSettings(),
            instants = PrayerName.entries.mapIndexed { index, name ->
                PrayerInstant(name, base + (index + 1) * step)
            }
        )
    }

    private fun build(modes: PrayerNotificationModes) = PrayerScheduleBuilder.build(
        today = day(0),
        tomorrow = day(1),
        location = location,
        policy = PrayerSchedulePolicy(modes = modes),
        nowEpochMillis = now,
        settingsFingerprint = "fp"
    )

    private fun exactPrayers(modes: PrayerNotificationModes): Set<PrayerName> =
        build(modes).alarms
            .filter { it.kind == PrayerAlarmKind.EXACT }
            .map { it.prayerName }
            .toSet()

    @Test
    fun `default modes schedule an exact alarm for every prayer`() {
        assertEquals(PrayerName.entries.toSet(), exactPrayers(PrayerNotificationModes.DEFAULT))
    }

    @Test
    fun `a muted prayer gets no exact alarm`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.FAJR, PrayerAlertMode.MUTED)
        val scheduled = exactPrayers(modes)

        assertFalse(scheduled.contains(PrayerName.FAJR))
        assertTrue(scheduled.contains(PrayerName.DHUHR))
    }

    @Test
    fun `muting every prayer removes every exact alarm`() {
        val modes = PrayerName.entries.fold(PrayerNotificationModes.DEFAULT) { acc, p ->
            acc.with(p, PrayerAlertMode.MUTED)
        }
        assertTrue(exactPrayers(modes).isEmpty())
    }

    @Test
    fun `notice mode still schedules the alarm`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.NOTICE)
        assertTrue(exactPrayers(modes).contains(PrayerName.ASR))
    }

    @Test
    fun `muting a prayer preserves its pre prayer reminder semantics`() {
        // Pre-prayer and iqamah reminders are a separate, explicitly enabled feature;
        // muting the adhan must not silently disable a reminder the user turned on.
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.MUTED)
        val schedule = PrayerScheduleBuilder.build(
            today = day(0),
            tomorrow = day(1),
            location = location,
            policy = PrayerSchedulePolicy(
                prePrayerMinutes = 10,
                iqamahMinutes = 5,
                modes = modes
            ),
            nowEpochMillis = now,
            settingsFingerprint = "fp"
        )

        val asrKinds = schedule.alarms
            .filter { it.prayerName == PrayerName.ASR }
            .map { it.kind }
            .toSet()

        assertTrue(asrKinds.contains(PrayerAlarmKind.PRE_PRAYER))
        assertTrue(asrKinds.contains(PrayerAlarmKind.IQAMAH))
        assertFalse(asrKinds.contains(PrayerAlarmKind.EXACT))
    }

    @Test
    fun `the schedule stays sorted after muting`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.DHUHR, PrayerAlertMode.MUTED)
        val alarms = build(modes).alarms
        alarms.zipWithNext().forEach { (a, b) ->
            assertTrue(a.triggerEpochMillis <= b.triggerEpochMillis)
        }
    }

    // --- Fingerprint --------------------------------------------------------

    @Test
    fun `a mode change invalidates the settings fingerprint`() {
        val before = PrayerScheduleBuilder.settingsFingerprint(
            "MWL", "SHAFI", 0, 0, 30.0, 31.0, PrayerNotificationModes.DEFAULT
        )
        val after = PrayerScheduleBuilder.settingsFingerprint(
            "MWL", "SHAFI", 0, 0, 30.0, 31.0,
            PrayerNotificationModes.DEFAULT.with(PrayerName.FAJR, PrayerAlertMode.MUTED)
        )
        assertNotEquals(before, after)
    }

    @Test
    fun `identical modes produce an identical fingerprint`() {
        val a = PrayerScheduleBuilder.settingsFingerprint(
            "MWL", "SHAFI", 0, 0, 30.0, 31.0,
            PrayerNotificationModes.DEFAULT.with(PrayerName.ISHA, PrayerAlertMode.NOTICE)
        )
        val b = PrayerScheduleBuilder.settingsFingerprint(
            "MWL", "SHAFI", 0, 0, 30.0, 31.0,
            PrayerNotificationModes.DEFAULT.with(PrayerName.ISHA, PrayerAlertMode.NOTICE)
        )
        assertEquals(a, b)
    }
}
