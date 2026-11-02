package com.example.feature.core.util

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

data class HijriCalendarDay(val civil: LocalDate, val hijri: HijrahDate)

/** Umm al-Qura via the existing java.time desugaring; civil midnight, never prayer calculations. */
class HijriDateProvider(private val clock: Clock = Clock.systemUTC(),
    private val zone: () -> ZoneId = { ZoneId.systemDefault() }) {
    fun today(): LocalDate = LocalDate.now(clock.withZone(zone()))
    fun date(civil: LocalDate, offset: Int = 0): HijrahDate {
        require(offset in -2..2)
        return HijrahDate.from(civil.plusDays(offset.toLong()))
    }
    fun month(year: Int, month: Int, offset: Int = 0): List<HijriCalendarDay> {
        require(offset in -2..2)
        val first = HijrahDate.of(year, month, 1)
        return (0 until first.lengthOfMonth()).map { index ->
            val hijri = first.plus(index.toLong(), ChronoUnit.DAYS)
            HijriCalendarDay(LocalDate.ofEpochDay(hijri.toEpochDay()).minusDays(offset.toLong()), hijri)
        }
    }
    companion object {
        // D-09 approved by owner, Batch 02, 2026-10-20: civil midnight, +/-2 days.
        const val MAX_LOCAL_OFFSET = 2
        val months = listOf("محرّم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة")
        fun format(date: HijrahDate): String = "${date.get(ChronoField.DAY_OF_MONTH)} ${months[date.get(ChronoField.MONTH_OF_YEAR)-1]} ${date.get(ChronoField.YEAR)}"
    }
}
