package com.example.feature.batch05
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.widget.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class Batch05WidgetTest {
 private val zone=ZoneId.of("Asia/Damascus")
 private val date=LocalDate.of(2026,10,21)
 private val loc=PrayerLocation(33.5,36.3,"QA")
 private fun at(hour:Int)=date.atTime(hour,0).atZone(zone).toInstant().toEpochMilli()
 private fun day()=PrayerDay(date.toEpochDay(),zone.id,loc,PrayerCalculationSettings(),listOf(PrayerInstant(PrayerName.DHUHR,at(12)),PrayerInstant(PrayerName.ISHA,at(19))))
 private val available=PrayerLocationState.Available(loc,PrayerLocationSource.Saved)
 @Test fun `widget uses exact canonical instant not stale remaining milliseconds`() {
  val result=PrayerWidgetStateFactory.fromFacade(day(),NextPrayer(PrayerName.DHUHR,at(12),999999999,null),available,at(11),zone) as PrayerWidgetState.Ready
  assertEquals(60L,result.minutesUntilNext);assertNull(result.locationLabel);assertEquals(PrayerName.DHUHR,result.nextPrayerName)
 }
 @Test fun `missing or old zone day location and expired instant never produce invented time`() {
  val next=NextPrayer(PrayerName.DHUHR,at(12),1,null)
  listOf(day().copy(timeZoneId="UTC"),day().copy(dateEpochDay=date.minusDays(1).toEpochDay()),day().copy(location=loc.copy(latitude=0.0))).forEach {
   assertTrue(PrayerWidgetStateFactory.fromFacade(it,next,available,at(11),zone) is PrayerWidgetState.Unavailable)
  }
  assertTrue(PrayerWidgetStateFactory.fromFacade(day(),next,available,at(12),zone) is PrayerWidgetState.Unavailable)
  assertTrue(PrayerWidgetStateFactory.fromFacade(null,null,PrayerLocationState.Loading,at(11),zone) is PrayerWidgetState.Unavailable)
 }
 @Test fun `after isha canonical next-day fajr works and unrelated future prayer does not`() {
  val next=NextPrayer(PrayerName.FAJR,date.plusDays(1).atTime(5,0).atZone(zone).toInstant().toEpochMilli(),0,at(19))
  assertTrue(PrayerWidgetStateFactory.fromFacade(day(),next,available,at(20),zone) is PrayerWidgetState.Ready)
  assertTrue(PrayerWidgetStateFactory.fromFacade(day(),next.copy(name=PrayerName.ASR),available,at(20),zone) is PrayerWidgetState.Unavailable)
 }
 @Test fun `refresh chooses boundary or civil midnight in destination zone including DST`() {
  val next=NextPrayer(PrayerName.DHUHR,at(12),1,null)
  assertEquals(at(12)+1000,PrayerWidgetStateFactory.refreshAt(at(11),next,zone))
  val midnight=date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
  assertEquals(midnight+1000,PrayerWidgetStateFactory.refreshAt(at(20),null,zone))
  val berlin=ZoneId.of("Europe/Berlin");val now=LocalDate.of(2026,10,25).atStartOfDay(berlin).toInstant().toEpochMilli()
  val end=LocalDate.of(2026,10,26).atStartOfDay(berlin).toInstant().toEpochMilli()
  assertEquals(25*60*60*1000L,end-now);assertEquals(end+1000,PrayerWidgetStateFactory.refreshAt(now,null,berlin))
 }
}
