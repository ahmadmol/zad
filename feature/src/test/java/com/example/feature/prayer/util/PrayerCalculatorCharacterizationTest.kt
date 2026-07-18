package com.example.feature.prayer.util

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.example.feature.prayer.PrayerTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Characterization tests locking current [PrayerCalculator] behavior before refactor.
 * Fixed civil dates + coordinates; timestamps compared for order and method/madhab deltas.
 */
class PrayerCalculatorCharacterizationTest {

    private fun dateAt(year: Int, month1Based: Int, day: Int, zone: String = "Asia/Riyadh"): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(zone)).apply {
            clear()
            set(year, month1Based - 1, day, 12, 0, 0)
        }
        return cal.time
    }

    private fun calculate(
        lat: Double,
        lng: Double,
        date: Date,
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI
    ): List<PrayerTime> = PrayerCalculator.calculate(lat, lng, date, method, madhab)

    @Test
    fun `prayer list order is Fajr Sunrise Dhuhr Asr Maghrib Isha`() {
        val times = calculate(21.4225, 39.8262, dateAt(2024, 6, 15))
        assertEquals(
            listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"),
            times.map { it.nameEn }
        )
        assertEquals(
            listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"),
            times.map { it.nameAr }
        )
    }

    @Test
    fun `timestamps are strictly increasing for Mecca`() {
        val times = calculate(21.4225, 39.8262, dateAt(2024, 6, 15))
        times.zipWithNext().forEach { (a, b) ->
            assertTrue("${a.nameEn} < ${b.nameEn}", a.timestamp < b.timestamp)
        }
    }

    @Test
    fun `Jerusalem Cairo London high-latitude produce six times`() {
        val date = dateAt(2024, 3, 20, "UTC")
        listOf(
            31.7683 to 35.2137, // Jerusalem
            30.0444 to 31.2357, // Cairo
            51.5074 to -0.1278, // London
            59.9139 to 10.7522  // Oslo (high latitude)
        ).forEach { (lat, lng) ->
            val times = calculate(lat, lng, date)
            assertEquals(6, times.size)
            times.zipWithNext().forEach { (a, b) ->
                assertTrue(a.timestamp < b.timestamp)
            }
        }
    }

    @Test
    fun `Hanafi Asr is at or after Shafi Asr for Cairo`() {
        val date = dateAt(2024, 6, 15, "Africa/Cairo")
        val shafi = calculate(30.0444, 31.2357, date, madhab = Madhab.SHAFI)
        val hanafi = calculate(30.0444, 31.2357, date, madhab = Madhab.HANAFI)
        val shafiAsr = shafi.first { it.nameEn == "Asr" }.timestamp
        val hanafiAsr = hanafi.first { it.nameEn == "Asr" }.timestamp
        assertTrue("Hanafi Asr >= Shafi Asr", hanafiAsr >= shafiAsr)
    }

    @Test
    fun `Egyptian method Fajr differs from MWL for Cairo`() {
        val date = dateAt(2024, 6, 15, "Africa/Cairo")
        val mwl = calculate(30.0444, 31.2357, date, method = CalculationMethod.MUSLIM_WORLD_LEAGUE)
        val egyptian = calculate(30.0444, 31.2357, date, method = CalculationMethod.EGYPTIAN)
        assertTrue(
            mwl.first { it.nameEn == "Fajr" }.timestamp !=
                egyptian.first { it.nameEn == "Fajr" }.timestamp
        )
    }

    @Test
    fun `next prayer selection before Fajr returns Fajr`() {
        val date = dateAt(2024, 6, 15)
        val times = calculate(21.4225, 39.8262, date)
        val beforeFajr = times.first { it.nameEn == "Fajr" }.timestamp - 60_000
        val next = selectNextPrayer(times, emptyList(), beforeFajr)
        assertEquals("Fajr", next.nameEn)
    }

    @Test
    fun `next prayer selection between Dhuhr and Asr returns Asr`() {
        val date = dateAt(2024, 6, 15)
        val times = calculate(21.4225, 39.8262, date)
        val afterDhuhr = times.first { it.nameEn == "Dhuhr" }.timestamp + 60_000
        val next = selectNextPrayer(times, emptyList(), afterDhuhr)
        assertEquals("Asr", next.nameEn)
    }

    @Test
    fun `next prayer after Isha uses tomorrow Fajr`() {
        val today = dateAt(2024, 6, 15)
        val tomorrow = dateAt(2024, 6, 16)
        val todayTimes = calculate(21.4225, 39.8262, today)
        val tomorrowTimes = calculate(21.4225, 39.8262, tomorrow)
        val afterIsha = todayTimes.first { it.nameEn == "Isha" }.timestamp + 60_000
        val next = selectNextPrayer(todayTimes, tomorrowTimes, afterIsha)
        assertEquals("Fajr", next.nameEn)
        assertEquals(tomorrowTimes.first().timestamp, next.timestamp)
    }

    @Test
    fun `method string mapping falls back to MWL`() {
        assertEquals(
            CalculationMethod.MUSLIM_WORLD_LEAGUE,
            PrayerCalculator.getMethodFromString("NOT_A_METHOD")
        )
        assertEquals(
            CalculationMethod.EGYPTIAN,
            PrayerCalculator.getMethodFromString("EGYPTIAN")
        )
    }

    @Test
    fun `madhab string mapping falls back to SHAFI`() {
        assertEquals(Madhab.SHAFI, PrayerCalculator.getMadhabFromString("NOPE"))
        assertEquals(Madhab.HANAFI, PrayerCalculator.getMadhabFromString("HANAFI"))
    }

    /**
     * Mirrors current Home/Prayer ViewModel next-prayer selection.
     */
    private fun selectNextPrayer(
        today: List<PrayerTime>,
        tomorrow: List<PrayerTime>,
        nowMillis: Long
    ): PrayerTime {
        return today.firstOrNull { it.timestamp > nowMillis }
            ?: tomorrow.first()
    }
}
