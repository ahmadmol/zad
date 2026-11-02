package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AdhanPrayerCalculatorTest {

    private val calculator = AdhanPrayerCalculator()
    private val mecca = PrayerLocation(21.4225, 39.8262, "Mecca")
    private val settings = PrayerCalculationSettings()

    @Test
    fun `calculates six ordered prayers for Mecca`() {
        val day = calculator.calculate(
            date = LocalDate.of(2024, 6, 15),
            location = mecca,
            settings = settings,
            timeZoneId = "Asia/Riyadh",
            nowEpochMillis = 0L
        )
        assertEquals(6, day.instants.size)
        day.instants.zipWithNext().forEach { (a, b) ->
            assertTrue(a.epochMillis < b.epochMillis)
        }
    }

    @Test
    fun `hanafi asr is at or after shafi`() {
        val date = LocalDate.of(2024, 6, 15)
        val shafi = calculator.calculate(
            date, mecca, settings.copy(madhhab = PrayerMadhhab.SHAFI), "Asia/Riyadh", 0L
        )
        val hanafi = calculator.calculate(
            date, mecca, settings.copy(madhhab = PrayerMadhhab.HANAFI), "Asia/Riyadh", 0L
        )
        assertTrue(
            hanafi.instant(PrayerName.ASR)!!.epochMillis >=
                shafi.instant(PrayerName.ASR)!!.epochMillis
        )
    }

    @Test
    fun `egyptian method differs from MWL fajr`() {
        val date = LocalDate.of(2024, 6, 15)
        val cairo = PrayerLocation(30.0444, 31.2357, "Cairo")
        val mwl = calculator.calculate(
            date, cairo, settings.copy(method = PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE),
            "Africa/Cairo", 0L
        )
        val egyptian = calculator.calculate(
            date, cairo, settings.copy(method = PrayerCalculationMethod.EGYPTIAN),
            "Africa/Cairo", 0L
        )
        assertTrue(
            mwl.instant(PrayerName.FAJR)!!.epochMillis !=
                egyptian.instant(PrayerName.FAJR)!!.epochMillis
        )
    }
}

class NextPrayerSelectorTest {

    private val calculator = AdhanPrayerCalculator()
    private val mecca = PrayerLocation(21.4225, 39.8262, "Mecca")

    @Test
    fun `after isha selects tomorrow fajr`() {
        val settings = PrayerCalculationSettings()
        val today = calculator.calculate(
            LocalDate.of(2024, 6, 15), mecca, settings, "Asia/Riyadh", 0L
        )
        val tomorrow = calculator.calculate(
            LocalDate.of(2024, 6, 16), mecca, settings, "Asia/Riyadh", 0L
        )
        val afterIsha = today.instant(PrayerName.ISHA)!!.epochMillis + 60_000
        val next = NextPrayerSelector.select(today, tomorrow, afterIsha)
        assertEquals(PrayerName.FAJR, next!!.name)
        assertEquals(tomorrow.instant(PrayerName.FAJR)!!.epochMillis, next.epochMillis)
    }

    @Test
    fun `between dhuhr and asr selects asr`() {
        val settings = PrayerCalculationSettings()
        val today = calculator.calculate(
            LocalDate.of(2024, 6, 15), mecca, settings, "Asia/Riyadh", 0L
        )
        val tomorrow = calculator.calculate(
            LocalDate.of(2024, 6, 16), mecca, settings, "Asia/Riyadh", 0L
        )
        val now = today.instant(PrayerName.DHUHR)!!.epochMillis + 60_000
        val next = NextPrayerSelector.select(today, tomorrow, now)
        assertEquals(PrayerName.ASR, next!!.name)
    }

    @Test
    fun `missing location path yields null day selection when empty`() {
        assertNull(
            NextPrayerSelector.select(
                today = today.copy(instants = emptyList()),
                tomorrow = today.copy(instants = emptyList()),
                nowEpochMillis = 0L
            )
        )
    }

    private val today by lazy {
        calculator.calculate(
            LocalDate.of(2024, 6, 15), mecca, PrayerCalculationSettings(), "Asia/Riyadh", 0L
        )
    }
}
