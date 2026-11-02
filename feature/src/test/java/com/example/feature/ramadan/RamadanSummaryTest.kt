package com.example.feature.ramadan

import com.example.feature.prayer.domain.model.*
import com.example.feature.quran.domain.model.ReadingPlan
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class RamadanSummaryTest {
    private val today=LocalDate.of(2026,10,20)
    private val zone=ZoneId.of("Asia/Damascus")
    private val location=PrayerLocation(33.5,36.3,"دمشق")
    private val available=PrayerLocationState.Available(location,PrayerLocationSource.Saved)
    private val day=PrayerDay(today.toEpochDay(),zone.id,location,PrayerCalculationSettings(),
        listOf(PrayerInstant(PrayerName.FAJR,12345678),PrayerInstant(PrayerName.MAGHRIB,87654321)))
    @Test fun `Fajr and Maghrib equal facade instants exactly without additional times`() {
        val result=RamadanSummary.prayer(day,available,today,zone)
        assertEquals(day.instant(PrayerName.FAJR)?.epochMillis,result.fajrMillis)
        assertEquals(day.instant(PrayerName.MAGHRIB)?.epochMillis,result.maghribMillis)
        assertEquals(zone.id,result.zoneId);assertEquals("دمشق",result.locationName)
    }
    @Test fun `stale day changed zone or changed location never leak old times`() {
        listOf(day.copy(dateEpochDay=today.minusDays(1).toEpochDay()),day.copy(timeZoneId="UTC"),
            day.copy(location=location.copy(displayName="مدينة أخرى"))).forEach { row ->
            val result=RamadanSummary.prayer(row,available,today,zone)
            assertNull(result.fajrMillis);assertNull(result.maghribMillis);assertNotNull(result.message)
        }
    }
    @Test fun `no location and missing day are honest unavailable states`() {
        val noLocation=RamadanSummary.prayer(day,PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION),today,zone)
        assertNull(noLocation.fajrMillis);assertNotNull(noLocation.message)
        assertNotNull(RamadanSummary.prayer(null,available,today,zone).message)
        assertTrue(RamadanSummary.prayer(null,PrayerLocationState.Loading,today,zone).loading)
    }
    @Test fun `a missing instant stays unavailable without replacing it with another prayer`() {
        val result=RamadanSummary.prayer(day.copy(instants=listOf(PrayerInstant(PrayerName.MAGHRIB,87654321))),available,today,zone)
        assertNull(result.fajrMillis);assertEquals(87654321L,result.maghribMillis);assertNotNull(result.message)
    }
    @Test fun `reading progress counts only explicit pages inside active plan range`() {
        val plan=ReadingPlan("p",1,30,10,20)
        assertEquals(2,RamadanSummary.confirmedPages(plan,setOf(1,10,20,21,604)))
        assertEquals(0,RamadanSummary.confirmedPages(plan,emptySet()))
    }
    @Test fun `seasonal highlight is opt in and limited to computed Ramadan`() {
        assertFalse(RamadanSummary.shouldHighlight(false,9))
        (1..12).forEach { month -> assertEquals(month==9,RamadanSummary.shouldHighlight(true,month)) }
        assertFalse(RamadanSummary.shouldHighlight(true,null))
    }
}
