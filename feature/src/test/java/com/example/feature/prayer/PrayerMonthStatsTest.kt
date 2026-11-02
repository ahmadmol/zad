package com.example.feature.prayer

import com.example.feature.prayer.domain.model.*
import com.example.feature.statistics.domain.PrayerTrackerStatsCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PrayerMonthStatsTest {
    @Test fun `empty leap February includes every day without inferring performance`() {
        val date=LocalDate.of(2024,2,29)
        val result=PrayerTrackerStatsCalculator.month(emptyList(),date,date)
        assertEquals(29,result.days.size);assertEquals(145,result.unrecorded)
        assertEquals(0,result.performed);assertEquals(0,result.explicitlyNotPerformed)
    }
    @Test fun `only selected month manual records and no future days contribute`() {
        val date=LocalDate.of(2026,10,20)
        val logs=listOf(ManualPrayerDaySummary(date,mapOf(PrayerName.FAJR to ManualPrayerStatus.PERFORMED,
            PrayerName.DHUHR to ManualPrayerStatus.LATE,PrayerName.ASR to ManualPrayerStatus.NOT_PERFORMED)),
            ManualPrayerDaySummary(date.withDayOfMonth(21),mapOf(PrayerName.FAJR to ManualPrayerStatus.PERFORMED)),
            ManualPrayerDaySummary(date.minusMonths(1),mapOf(PrayerName.FAJR to ManualPrayerStatus.PERFORMED)))
        val result=PrayerTrackerStatsCalculator.month(logs,date,date)
        assertEquals(20,result.days.size);assertEquals(2,result.performed);assertEquals(3,result.recorded)
        assertEquals(97,result.unrecorded);assertEquals(1,result.explicitlyNotPerformed)
    }
    @Test fun `year boundary excludes adjacent months`() {
        val dec=LocalDate.of(2025,12,31)
        val stats=PrayerTrackerStatsCalculator.month(listOf(ManualPrayerDaySummary(dec.plusDays(1),mapOf(PrayerName.FAJR to ManualPrayerStatus.LATE))),dec,dec.plusDays(5))
        assertEquals(31,stats.days.size);assertEquals(0,stats.recorded)
    }
}
