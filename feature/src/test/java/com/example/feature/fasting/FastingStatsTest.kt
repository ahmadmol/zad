package com.example.feature.fasting

import com.example.feature.fasting.domain.*
import com.example.feature.core.util.HijriDateProvider
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FastingStatsTest {
    @Test fun `empty leap month never implies fasting or breaking fast`() {
        val stats=FastingStats.calculate(emptyList(),LocalDate.of(2024,2,1),LocalDate.of(2024,2,29),LocalDate.of(2024,3,1))
        assertEquals(FastingStats(0,0,0,29),stats)
    }
    @Test fun `month boundaries future and unknown statuses remain distinct`() {
        val today=LocalDate.of(2026,10,20)
        val rows=listOf(FastingRecord(today,FastingStatus.FASTED,""),FastingRecord(today.minusDays(1),FastingStatus.NOT_FASTED,""),
            FastingRecord(today.minusDays(2),null,""),FastingRecord(today.plusDays(1),FastingStatus.FASTED,""),
            FastingRecord(today.minusMonths(1),FastingStatus.FASTED,""))
        assertEquals(FastingStats(1,1,1,17),FastingStats.calculate(rows,today.withDayOfMonth(1),today.withDayOfMonth(31),today))
    }
    @Test fun `a future month has no inferred unrecorded or performed days`() {
        assertEquals(FastingStats(0,0,0,0),FastingStats.calculate(emptyList(),LocalDate.of(2027,1,1),LocalDate.of(2027,1,31),LocalDate.of(2026,12,31)))
    }
    @Test fun `Hijri offset changes visible range but not civil record keys`() {
        val provider=HijriDateProvider()
        val civil=LocalDate.of(2026,10,20)
        val record=FastingRecord(civil,FastingStatus.FASTED,"")
        (-2..2).forEach { offset ->
            val display=provider.date(record.date,offset)
            assertEquals(record.date.toEpochDay()+offset,display.toEpochDay())
            assertEquals(civil,record.date)
            assertEquals(1,FastingStats.calculate(listOf(record),civil,civil,civil).fasted)
        }
    }
}
