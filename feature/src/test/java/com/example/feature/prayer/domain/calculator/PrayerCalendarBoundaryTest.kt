package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
class PrayerCalendarBoundaryTest {
    private val calculator = AdhanPrayerCalculator()
    private val london = PrayerLocation(51.5074, -0.1278, "london")
    private val aleppo = PrayerLocation(36.2021, 37.1343, "aleppo")

    @Test fun `spring daylight saving changes the utc gap between two London noons`() {
        val zone = ZoneId.of("Europe/London")
        val before = day(LocalDate.of(2026, 3, 28), london, zone)
        val after = day(LocalDate.of(2026, 3, 30), london, zone)
        val beforeNoon = Instant.ofEpochMilli(before.instant(PrayerName.DHUHR)!!.epochMillis).atZone(zone)
        val afterNoon = Instant.ofEpochMilli(after.instant(PrayerName.DHUHR)!!.epochMillis).atZone(zone)
        assertNotEquals(beforeNoon.offset, afterNoon.offset)
        assertEquals(1, kotlin.math.abs(afterNoon.hour - beforeNoon.hour))
        assertEquals("Europe/London", before.timeZoneId)
    }

    @Test fun `the requested civil date is the prayer day even when the clock is near midnight`() {
        val zone = ZoneId.of("Asia/Damascus")
        val today = day(LocalDate.of(2026, 10, 14), aleppo, zone)
        val tomorrow = day(LocalDate.of(2026, 10, 15), aleppo, zone)
        assertEquals(LocalDate.of(2026, 10, 14).toEpochDay(), today.dateEpochDay)
        assertNotEquals(today.instant(PrayerName.FAJR)!!.epochMillis, tomorrow.instant(PrayerName.FAJR)!!.epochMillis)
        val fajrDate = Instant.ofEpochMilli(today.instant(PrayerName.FAJR)!!.epochMillis).atZone(zone).toLocalDate()
        assertEquals(LocalDate.of(2026, 10, 14), fajrDate)
    }

    @Test fun `one Aladhan method is not treated as every local convention`() {
        val zone = ZoneId.of("Asia/Damascus").id
        val date = LocalDate.of(2026, 10, 14)
        val league = calculator.calculate(date, aleppo, PrayerCalculationSettings(
            method = PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE
        ), zone, 0).instant(PrayerName.FAJR)!!.epochMillis
        val egyptian = calculator.calculate(date, aleppo, PrayerCalculationSettings(
            method = PrayerCalculationMethod.EGYPTIAN
        ), zone, 0).instant(PrayerName.FAJR)!!.epochMillis
        assertNotEquals(league, egyptian)
    }

    private fun day(date: LocalDate, location: PrayerLocation, zone: ZoneId) =
        calculator.calculate(date, location, PrayerCalculationSettings(), zone.id, 0L)
}
