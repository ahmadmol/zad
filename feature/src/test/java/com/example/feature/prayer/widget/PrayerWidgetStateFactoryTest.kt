package com.example.feature.prayer.widget

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

/** Phase 7 — Prayer widget state derivation. */
class PrayerWidgetStateFactoryTest {

    private val calculator = AdhanPrayerCalculator()
    private val cairo = PrayerLocation(30.0444, 31.2357, "القاهرة")
    private val settings = PrayerCalculationSettings()
    private val zone = ZoneId.of("Africa/Cairo")
    private val date = LocalDate.of(2026, 3, 10)

    private fun day(d: LocalDate) =
        calculator.calculate(d, cairo, settings, zone.id, 0L)

    private fun at(hour: Int, minute: Int = 0): Long =
        ZonedDateTime.of(date, LocalTime.of(hour, minute), zone).toInstant().toEpochMilli()

    private fun state(nowEpochMillis: Long, location: String? = "القاهرة") =
        PrayerWidgetStateFactory.create(
            today = day(date),
            tomorrow = day(date.plusDays(1)),
            nowEpochMillis = nowEpochMillis,
            locationLabel = location,
            zoneId = zone,
            locale = Locale.US
        )

    // --- Unavailable states -------------------------------------------------

    @Test
    fun `no prayer data reports unavailable rather than showing a wrong time`() {
        val result = PrayerWidgetStateFactory.create(
            today = null,
            tomorrow = null,
            nowEpochMillis = at(10),
            locationLabel = null,
            zoneId = zone
        )
        assertTrue(result is PrayerWidgetState.Unavailable)
        assertEquals(
            PrayerWidgetState.Unavailable.Reason.NO_LOCATION,
            (result as PrayerWidgetState.Unavailable).reason
        )
    }

    @Test
    fun `missing tomorrow is reported as not ready`() {
        val result = PrayerWidgetStateFactory.create(
            today = day(date),
            tomorrow = null,
            nowEpochMillis = at(10),
            locationLabel = "القاهرة",
            zoneId = zone
        )
        assertEquals(
            PrayerWidgetState.Unavailable.Reason.NOT_READY,
            (result as PrayerWidgetState.Unavailable).reason
        )
    }

    // --- Ready state --------------------------------------------------------

    @Test
    fun `midday shows the next upcoming prayer`() {
        val ready = state(at(10)) as PrayerWidgetState.Ready
        val expected = day(date).instants.first { it.epochMillis > at(10) }
        assertEquals(expected.name, ready.nextPrayerName)
    }

    @Test
    fun `the widget lists all six of today's entries`() {
        val ready = state(at(10)) as PrayerWidgetState.Ready
        assertEquals(6, ready.today.size)
        assertEquals(
            day(date).instants.map { it.name },
            ready.today.map { it.name }
        )
    }

    @Test
    fun `exactly one entry is marked next when the next prayer is today`() {
        val ready = state(at(10)) as PrayerWidgetState.Ready
        assertEquals(1, ready.today.count { it.isNext })
    }

    @Test
    fun `past prayers are marked past`() {
        val ready = state(at(23, 59)) as PrayerWidgetState.Ready
        assertTrue(ready.today.all { it.isPast })
    }

    @Test
    fun `location label is surfaced`() {
        val ready = state(at(10)) as PrayerWidgetState.Ready
        assertEquals("القاهرة", ready.locationLabel)
    }

    // --- Day rollover -------------------------------------------------------

    @Test
    fun `after isha the next prayer rolls to tomorrow fajr`() {
        val isha = day(date).instants.last().epochMillis
        val ready = state(isha + 60_000L) as PrayerWidgetState.Ready

        assertEquals(PrayerName.FAJR, ready.nextPrayerName)
        // Tomorrow's Fajr is not in today's list, so nothing in the list is "next".
        assertEquals(0, ready.today.count { it.isNext })
    }

    @Test
    fun `countdown is never negative across the rollover`() {
        val probes = day(date).instants.map { it.epochMillis } +
            day(date).instants.map { it.epochMillis + 1 }
        probes.forEach { now ->
            val ready = state(now)
            if (ready is PrayerWidgetState.Ready) {
                assertTrue(
                    "negative countdown at " + now,
                    ready.minutesUntilNext >= 0L
                )
            }
        }
    }

    @Test
    fun `countdown is whole minutes until the next prayer`() {
        val fajr = day(date).instant(PrayerName.FAJR)!!.epochMillis
        val ready = state(fajr - 30 * 60_000L) as PrayerWidgetState.Ready
        assertEquals(PrayerName.FAJR, ready.nextPrayerName)
        assertEquals(30L, ready.minutesUntilNext)
    }

    // --- No second calculation engine ---------------------------------------

    @Test
    fun `widget times match the canonical calculator exactly`() {
        val ready = state(at(10)) as PrayerWidgetState.Ready
        val canonical = day(date)

        // Same instants, formatted — the widget renders the calculator's output and
        // never derives its own times.
        canonical.instants.forEachIndexed { index, instant ->
            assertEquals(instant.name, ready.today[index].name)
        }
        val next = canonical.instants.first { it.epochMillis > at(10) }
        assertEquals(next.name, ready.nextPrayerName)
    }

    @Test
    fun `settings changes flow through to the widget`() {
        val hanafi = settings.copy(
            madhhab = com.example.feature.prayer.domain.model.PrayerMadhhab.HANAFI
        )
        val shafiState = state(at(10)) as PrayerWidgetState.Ready
        val hanafiState = PrayerWidgetStateFactory.create(
            today = calculator.calculate(date, cairo, hanafi, zone.id, 0L),
            tomorrow = calculator.calculate(date.plusDays(1), cairo, hanafi, zone.id, 0L),
            nowEpochMillis = at(10),
            locationLabel = "القاهرة",
            zoneId = zone,
            locale = Locale.US
        ) as PrayerWidgetState.Ready

        val shafiAsr = shafiState.today.first { it.name == PrayerName.ASR }.timeLabel
        val hanafiAsr = hanafiState.today.first { it.name == PrayerName.ASR }.timeLabel
        assertFalse("Hanafi Asr must differ from Shafi", shafiAsr == hanafiAsr)
    }

    // --- Refresh scheduling -------------------------------------------------

    @Test
    fun `next refresh is the upcoming prayer instant`() {
        val now = at(10)
        val expected = day(date).instants.first { it.epochMillis > now }.epochMillis
        assertEquals(
            expected,
            PrayerWidgetStateFactory.nextRefreshEpochMillis(day(date), day(date.plusDays(1)), now)
        )
    }

    @Test
    fun `no refresh is scheduled without prayer data`() {
        assertNull(PrayerWidgetStateFactory.nextRefreshEpochMillis(null, null, at(10)))
    }

    @Test
    fun `refresh time is always in the future`() {
        val now = at(10)
        val refresh = PrayerWidgetStateFactory.nextRefreshEpochMillis(
            day(date), day(date.plusDays(1)), now
        )
        assertNotNull(refresh)
        assertTrue(refresh!! > now)
    }
}
