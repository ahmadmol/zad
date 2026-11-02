package com.example.feature.core

import com.example.feature.core.util.HijriDateProvider
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class HijriDateProviderTest {
    private val provider=HijriDateProvider()
    @Test fun `offset maps civil days and rejects beyond approved bounds`() {
        val civil=LocalDate.of(2026,10,20)
        (-2..2).forEach { offset -> assertEquals(HijrahDate.from(civil.plusDays(offset.toLong())),provider.date(civil,offset)) }
        listOf(-3,3).forEach { offset -> try { provider.date(civil,offset);fail() } catch (_: IllegalArgumentException) {} }
    }
    @Test fun `all month days round trip across leap years and offset`() {
        for(year in 1446..1449) for(month in 1..12) for(offset in -2..2) {
            val rows=provider.month(year,month,offset)
            assertTrue(rows.size in 29..30)
            rows.forEachIndexed { index, row ->
                assertEquals(index+1,row.hijri.get(ChronoField.DAY_OF_MONTH))
                assertEquals(row.hijri,provider.date(row.civil,offset))
            }
            assertEquals(rows.size-1L,rows.last().civil.toEpochDay()-rows.first().civil.toEpochDay())
        }
    }
    @Test fun `offset crosses hijri month and year without changing civil day`() {
        val newYear=LocalDate.ofEpochDay(HijrahDate.of(1448,1,1).toEpochDay())
        assertEquals(1447,provider.date(newYear,-1).get(ChronoField.YEAR))
        assertEquals(12,provider.date(newYear,-1).get(ChronoField.MONTH_OF_YEAR))
        assertEquals(2,provider.date(newYear,1).get(ChronoField.DAY_OF_MONTH))
    }
    @Test fun `same instant uses the current zone including a zone change`() {
        var zone=ZoneId.of("Asia/Damascus")
        val clock=Clock.fixed(Instant.parse("2026-10-20T22:30:00Z"),ZoneOffset.UTC)
        val dates=HijriDateProvider(clock){zone}
        assertEquals(LocalDate.of(2026,10,21),dates.today())
        zone=ZoneId.of("America/New_York")
        assertEquals(LocalDate.of(2026,10,20),dates.today())
    }
    @Test fun `DST uses civil date rather than fixed day milliseconds`() {
        val zone=ZoneId.of("America/New_York")
        listOf("2026-03-08T06:59:59Z","2026-03-08T07:00:00Z").forEach { instant ->
            assertEquals(LocalDate.of(2026,3,8),HijriDateProvider(Clock.fixed(Instant.parse(instant),ZoneOffset.UTC)){zone}.today())
        }
    }
    @Test fun `civil midnight advances date with no offset applied to logs`() {
        val zone=ZoneId.of("Asia/Damascus")
        val a=HijriDateProvider(Clock.fixed(Instant.parse("2026-10-20T20:59:59Z"),ZoneOffset.UTC)){zone}
        val b=HijriDateProvider(Clock.fixed(Instant.parse("2026-10-20T21:00:00Z"),ZoneOffset.UTC)){zone}
        assertEquals(a.today().plusDays(1),b.today())
        assertEquals(a.date(a.today(),2).toEpochDay()+1,b.date(b.today(),2).toEpochDay())
    }
}
