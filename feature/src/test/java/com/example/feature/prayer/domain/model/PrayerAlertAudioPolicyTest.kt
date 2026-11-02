package com.example.feature.prayer.domain.model

import com.example.feature.prayer.domain.scheduler.PrayerScheduleBuilder
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Replaces removed audio/extras helpers with current alarm-request identity coverage. */
class PrayerAlertAudioPolicyTest {
    private val location = PrayerLocation(24.7136, 46.6753, "Riyadh")

    @Test
    fun `stable base id is deterministic for the same prayer and day`() {
        assertEquals(
            PrayerScheduleBuilder.stableBaseId(PrayerName.ASR, 224),
            PrayerScheduleBuilder.stableBaseId(PrayerName.ASR, 224)
        )
    }

    @Test
    fun `stable base id changes for prayer or day`() {
        val base = PrayerScheduleBuilder.stableBaseId(PrayerName.ASR, 224)
        assertNotEquals(base, PrayerScheduleBuilder.stableBaseId(PrayerName.FAJR, 224))
        assertNotEquals(base, PrayerScheduleBuilder.stableBaseId(PrayerName.ASR, 225))
    }

    @Test
    fun `each alarm kind for a prayer receives a distinct stable id`() {
        val day = prayerDay(0)
        val alarms = PrayerScheduleBuilder.build(
            today = day,
            tomorrow = prayerDay(1),
            location = location,
            policy = PrayerSchedulePolicy(
                prePrayerMinutes = 10,
                iqamahMinutes = 10,
                endOfPrayerReminderMinutesBeforeNext = 15
            ),
            nowEpochMillis = 0,
            settingsFingerprint = "fp"
        ).alarms.filter { it.prayerName == PrayerName.ASR }

        assertEquals(alarms.size, alarms.map { it.stableId }.toSet().size)
        assertTrue(alarms.map { it.kind }.containsAll(PrayerAlarmKind.entries))
    }

    @Test
    fun `alarm request identity includes prayer kind trigger and stable id`() {
        val request = PrayerAlarmRequest(
            stableId = 42,
            prayerName = PrayerName.FAJR,
            kind = PrayerAlarmKind.EXACT,
            triggerEpochMillis = 1_000L,
            title = "الفجر",
            message = "حان وقت الصلاة"
        )
        val changedKind = request.copy(kind = PrayerAlarmKind.PRE_PRAYER)

        assertNotEquals(request, changedKind)
        assertEquals(42, request.stableId)
        assertEquals(PrayerName.FAJR, request.prayerName)
        assertEquals(PrayerAlarmKind.EXACT, request.kind)
    }

    private fun prayerDay(offsetDays: Int) = PrayerDay(
        dateEpochDay = LocalDate.of(2026, 8, 12).plusDays(offsetDays.toLong()).toEpochDay(),
        timeZoneId = "UTC",
        location = location,
        settings = PrayerCalculationSettings(),
        instants = PrayerName.entries.mapIndexed { index, prayer ->
            PrayerInstant(prayer, offsetDays * 86_400_000L + (index + 2) * 3_600_000L)
        }
    )
}
