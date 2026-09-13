package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerAlarmRequest
import com.example.feature.prayer.domain.model.PrayerAlertAudio
import com.example.feature.prayer.domain.model.PrayerAlertAudioPolicy
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Builder-level Phase A1 checks. These assert that the alert *schedule* is unchanged in
 * time and that the audio class attached to each scheduled event is correct.
 */
class PrayerScheduleBuilderAudioPolicyTest {

    private val zone = ZoneId.of("Asia/Riyadh")
    private val date = LocalDate.of(2026, 8, 12)
    private val nextDate = date.plusDays(1)
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

    // TEST 12 (builder side): pre-prayer offset is applied to the alert, not the prayer.
    @Test
    fun `pre prayer reminder is ten minutes early while the prayer time is unchanged`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10)).onDate(date)

        val pre = alarms.single { it.prayerName == PrayerName.ASR && it.kind == PrayerAlarmKind.PRE_PRAYER }
        val exact = alarms.single { it.prayerName == PrayerName.ASR && it.kind == PrayerAlarmKind.EXACT }

        assertEquals(epochOf(date, LocalTime.of(16, 12)), pre.triggerEpochMillis)
        assertEquals(epochOf(date, LocalTime.of(16, 22)), exact.triggerEpochMillis)
        assertEquals("2026-08-12_ASR_PRE_PRAYER", pre.eventKey)
        assertEquals("2026-08-12_ASR_EXACT", exact.eventKey)

        assertEquals(PrayerAlertAudio.NOTICE, audioOf(pre))
        assertEquals(PrayerAlertAudio.ADHAN, audioOf(exact))
    }

    @Test
    fun `prayer times are never shifted by the alert policy`() {
        val withReminders = build(
            PrayerSchedulePolicy(prePrayerMinutes = 30, iqamahMinutes = 20)
        )
        val withoutReminders = build(PrayerSchedulePolicy())

        times.forEach { (prayer, time) ->
            val expected = epochOf(date, time)
            listOf(withReminders, withoutReminders).forEach { alarms ->
                val exact = alarms.single {
                    it.prayerName == prayer &&
                        it.kind == PrayerAlarmKind.EXACT &&
                        it.eventKey.startsWith("2026-08-12_")
                }
                assertEquals("$prayer exact trigger must equal the prayer time", expected, exact.triggerEpochMillis)
            }
        }
    }

    // TEST 13
    @Test
    fun `sunrise keeps its calculated time and is never adhan`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 10)).onDate(date)

        val sunriseAlarms = alarms.filter { it.prayerName == PrayerName.SUNRISE }
        val sunriseExact = sunriseAlarms.single { it.kind == PrayerAlarmKind.EXACT }

        assertEquals(epochOf(date, LocalTime.of(5, 46)), sunriseExact.triggerEpochMillis)
        assertEquals("2026-08-12_SUNRISE_EXACT", sunriseExact.eventKey)

        sunriseAlarms.forEach {
            assertEquals("sunrise alert ${it.kind} must be notice-only", PrayerAlertAudio.NOTICE, audioOf(it))
        }
        // Sunrise still has no reminder/iqamah/end events of its own.
        assertEquals(listOf(PrayerAlarmKind.EXACT), sunriseAlarms.map { it.kind })
    }

    @Test
    fun `sunrise remains present in the schedule so the time stays visible`() {
        val alarms = build(PrayerSchedulePolicy())
        assertNotNull(alarms.firstOrNull { it.prayerName == PrayerName.SUNRISE })
    }

    // End-of-prayer reminder is now opt-in.
    @Test
    fun `end reminder is not scheduled under the default policy`() {
        val alarms = build(PrayerSchedulePolicy())
        assertTrue(
            "end reminder must be opt-in",
            alarms.none { it.kind == PrayerAlarmKind.END_REMINDER }
        )
    }

    @Test
    fun `end reminder is notice only when explicitly enabled`() {
        val alarms = build(PrayerSchedulePolicy(endOfPrayerReminderMinutesBeforeNext = 15))
        val endReminders = alarms.filter { it.kind == PrayerAlarmKind.END_REMINDER }

        assertTrue(endReminders.isNotEmpty())
        endReminders.forEach { assertEquals(PrayerAlertAudio.NOTICE, audioOf(it)) }
    }

    // TEST 14 — the Ask Mode collision scenario.
    @Test
    fun `colliding pre prayer and end reminder produce no adhan while asr exact produces one`() {
        val alarms = build(
            PrayerSchedulePolicy(
                prePrayerMinutes = 15,
                endOfPrayerReminderMinutesBeforeNext = 15
            )
        )

        val collisionInstant = epochOf(date, LocalTime.of(16, 7))
        val colliding = alarms.filter { it.triggerEpochMillis == collisionInstant }

        // Both events really do land on the same instant — the scenario is reproduced.
        assertEquals(
            setOf(
                PrayerName.ASR to PrayerAlarmKind.PRE_PRAYER,
                PrayerName.DHUHR to PrayerAlarmKind.END_REMINDER
            ),
            colliding.map { it.prayerName to it.kind }.toSet()
        )
        // They no longer share a stable id, and neither may sound the adhan.
        assertEquals(2, colliding.map { it.stableId }.toSet().size)
        assertEquals(0, colliding.count { audioOf(it) == PrayerAlertAudio.ADHAN })

        val atAsr = alarms.filter { it.triggerEpochMillis == epochOf(date, LocalTime.of(16, 22)) }
        assertEquals(1, atAsr.count { audioOf(it) == PrayerAlertAudio.ADHAN })
    }

    @Test
    fun `each real prayer gets exactly one adhan capable event per day`() {
        val alarms = build(PrayerSchedulePolicy(prePrayerMinutes = 10, iqamahMinutes = 10))

        listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        ).forEach { prayer ->
            val adhanToday = alarms.filter {
                it.prayerName == prayer &&
                    it.eventKey.startsWith("2026-08-12_") &&
                    audioOf(it) == PrayerAlertAudio.ADHAN
            }
            assertEquals("$prayer must have exactly one adhan event", 1, adhanToday.size)
            assertEquals(PrayerAlarmKind.EXACT, adhanToday.single().kind)
        }
    }

    @Test
    fun `event keys are unique across the scheduled window`() {
        val alarms = build(
            PrayerSchedulePolicy(
                prePrayerMinutes = 10,
                iqamahMinutes = 10,
                endOfPrayerReminderMinutesBeforeNext = 15
            )
        )
        val keys = alarms.map { it.eventKey }
        assertEquals(keys.size, keys.toSet().size)
        assertNull(alarms.firstOrNull { it.eventKey.isBlank() })
    }

    private fun audioOf(request: PrayerAlarmRequest): PrayerAlertAudio =
        PrayerAlertAudioPolicy.decide(request.prayerName, request.kind)

    /** The builder always schedules today and tomorrow; most assertions target one day. */
    private fun List<PrayerAlarmRequest>.onDate(day: LocalDate): List<PrayerAlarmRequest> =
        filter { it.eventKey.startsWith("${day}_") }

    private fun build(policy: PrayerSchedulePolicy): List<PrayerAlarmRequest> =
        PrayerScheduleBuilder.build(
            today = dayOf(date),
            tomorrow = dayOf(nextDate),
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
