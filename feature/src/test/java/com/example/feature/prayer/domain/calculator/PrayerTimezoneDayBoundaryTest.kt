package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Phase 3 — timezone / DST / day-boundary characterization.
 *
 * Locks the behaviour that [AdhanPrayerCalculator] produces absolute epoch instants
 * interpreted in the supplied zone, and that [NextPrayerSelector] rolls over correctly
 * across the local midnight boundary and follows manual clock changes.
 */
class PrayerTimezoneDayBoundaryTest {

    private val calculator = AdhanPrayerCalculator()
    private val settings = PrayerCalculationSettings()

    private val cairo = PrayerLocation(30.0444, 31.2357, "Cairo")
    private val london = PrayerLocation(51.5074, -0.1278, "London")

    private fun epochAt(zone: String, date: LocalDate, hour: Int, minute: Int = 0): Long =
        ZonedDateTime.of(date, LocalTime.of(hour, minute), ZoneId.of(zone))
            .toInstant().toEpochMilli()

    private fun day(date: LocalDate, location: PrayerLocation, zone: String, now: Long = 0L) =
        calculator.calculate(date, location, settings, zone, now)

    // --- Timezone identity -------------------------------------------------

    @Test
    fun `absolute instants depend on date and coordinates not on the zone label`() {
        val date = LocalDate.of(2024, 6, 15)
        // The solar events for a civil date at a fixed coordinate are absolute. The
        // timeZoneId is carried on PrayerDay as presentation metadata only, so a
        // device reporting a wrong/changed zone cannot shift the computed instants.
        val asTokyo = day(date, cairo, "Asia/Tokyo")
        val asCairo = day(date, cairo, "Africa/Cairo")

        assertEquals(
            asCairo.instants.map { it.epochMillis },
            asTokyo.instants.map { it.epochMillis }
        )
        assertEquals("Asia/Tokyo", asTokyo.timeZoneId)
        assertEquals("Africa/Cairo", asCairo.timeZoneId)
    }

    @Test
    fun `a different location does change the absolute instants`() {
        val date = LocalDate.of(2024, 6, 15)
        assertNotEquals(
            day(date, cairo, "Africa/Cairo").instant(PrayerName.FAJR)!!.epochMillis,
            day(date, london, "Africa/Cairo").instant(PrayerName.FAJR)!!.epochMillis
        )
    }

    @Test
    fun `calculation is deterministic for a fixed date location and zone`() {
        val date = LocalDate.of(2024, 6, 15)
        val a = day(date, cairo, "Africa/Cairo", now = 0L)
        val b = day(date, cairo, "Africa/Cairo", now = 999_999_999L)
        assertEquals(a.instants, b.instants)
    }

    @Test
    fun `all instants fall inside the local civil day they were requested for`() {
        val zone = "Africa/Cairo"
        val date = LocalDate.of(2024, 6, 15)
        day(date, cairo, zone).instants.forEach { instant ->
            val local = Instant.ofEpochMilli(instant.epochMillis)
                .atZone(ZoneId.of(zone)).toLocalDate()
            assertEquals("" + instant.name + " must fall on " + date, date, local)
        }
    }

    // --- DST ---------------------------------------------------------------

    @Test
    fun `london spring forward day still produces six ordered prayers`() {
        // 2024-03-31 is the UK DST spring-forward date (01:00 -> 02:00 local).
        val d = day(LocalDate.of(2024, 3, 31), london, "Europe/London")
        assertEquals(6, d.instants.size)
        d.instants.zipWithNext().forEach { (a, b) ->
            assertTrue("" + a.name + " must precede " + b.name, a.epochMillis < b.epochMillis)
        }
    }

    @Test
    fun `london autumn back day still produces six ordered prayers`() {
        // 2024-10-27 is the UK DST fall-back date (02:00 -> 01:00 local).
        val d = day(LocalDate.of(2024, 10, 27), london, "Europe/London")
        assertEquals(6, d.instants.size)
        d.instants.zipWithNext().forEach { (a, b) ->
            assertTrue("" + a.name + " must precede " + b.name, a.epochMillis < b.epochMillis)
        }
    }

    @Test
    fun `dst shift moves the wall clock time of fajr by roughly one hour`() {
        val zone = ZoneId.of("Europe/London")
        val before = day(LocalDate.of(2024, 3, 30), london, "Europe/London")
        val after = day(LocalDate.of(2024, 3, 31), london, "Europe/London")

        val localBefore = Instant.ofEpochMilli(before.instant(PrayerName.FAJR)!!.epochMillis)
            .atZone(zone).toLocalTime()
        val localAfter = Instant.ofEpochMilli(after.instant(PrayerName.FAJR)!!.epochMillis)
            .atZone(zone).toLocalTime()

        val deltaMinutes = localAfter.toSecondOfDay() / 60 - localBefore.toSecondOfDay() / 60
        assertTrue(
            "expected a ~60min wall-clock jump, got " + deltaMinutes,
            deltaMinutes in 45..75
        )
    }

    // --- Day boundary / rollover ------------------------------------------

    @Test
    fun `after isha selects tomorrow fajr`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val afterIsha = d.instant(PrayerName.ISHA)!!.epochMillis + 60_000L
        val next = NextPrayerSelector.select(d, t, afterIsha)!!

        assertEquals(PrayerName.FAJR, next.name)
        assertEquals(t.instant(PrayerName.FAJR)!!.epochMillis, next.epochMillis)
    }

    @Test
    fun `exactly at local midnight the next prayer is tomorrow fajr`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val midnight = epochAt(zone, today.plusDays(1), 0, 0)
        val next = NextPrayerSelector.select(d, t, midnight)!!

        assertEquals(PrayerName.FAJR, next.name)
        assertEquals(t.instant(PrayerName.FAJR)!!.epochMillis, next.epochMillis)
    }

    @Test
    fun `one minute before fajr the next prayer is still today fajr`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val fajr = d.instant(PrayerName.FAJR)!!.epochMillis
        val next = NextPrayerSelector.select(d, t, fajr - 60_000L)!!

        assertEquals(PrayerName.FAJR, next.name)
        assertEquals(fajr, next.epochMillis)
    }

    @Test
    fun `exactly at a prayer instant the selector advances to the following prayer`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val dhuhr = d.instant(PrayerName.DHUHR)!!.epochMillis
        // select() uses a strict > comparison, so the current instant is already past.
        assertEquals(PrayerName.ASR, NextPrayerSelector.select(d, t, dhuhr)!!.name)
    }

    @Test
    fun `remaining millis is never negative across the rollover`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val probes = d.instants.map { it.epochMillis } +
            d.instants.map { it.epochMillis + 1 } +
            listOf(epochAt(zone, today.plusDays(1), 0, 0))

        probes.forEach { now ->
            val next = NextPrayerSelector.select(d, t, now)
            if (next != null) {
                assertTrue("remaining must be >= 0 at " + now, next.remainingMillis >= 0L)
            }
        }
    }

    @Test
    fun `progress fraction stays within zero and one across the day`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        var probe = epochAt(zone, today, 0, 0)
        val end = epochAt(zone, today.plusDays(1), 0, 0)
        while (probe < end) {
            val next = NextPrayerSelector.select(d, t, probe)
            if (next != null) {
                val f = NextPrayerSelector.progressFraction(next, probe)
                assertTrue("progress out of range at " + probe + ": " + f, f in 0f..1f)
            }
            probe += 15 * 60_000L
        }
    }

    @Test
    fun `manual clock change backwards re-selects an earlier prayer`() {
        val zone = "Africa/Cairo"
        val today = LocalDate.of(2024, 6, 15)
        val d = day(today, cairo, zone)
        val t = day(today.plusDays(1), cairo, zone)

        val afterAsr = d.instant(PrayerName.ASR)!!.epochMillis + 60_000L
        val beforeDhuhr = d.instant(PrayerName.DHUHR)!!.epochMillis - 60_000L

        assertEquals(PrayerName.MAGHRIB, NextPrayerSelector.select(d, t, afterAsr)!!.name)
        // Clock moved backwards (TIME_SET): selection must follow the clock, not latch.
        assertEquals(PrayerName.DHUHR, NextPrayerSelector.select(d, t, beforeDhuhr)!!.name)
    }
}
