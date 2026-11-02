package com.example.feature.batch05
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.feature.core.preferences.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.time.*
import java.util.concurrent.atomic.AtomicReference
class Batch05DataStoreTest {
 private class MovingClock : Clock() {
  val time=AtomicReference(Instant.parse("2026-10-21T20:59:00Z"));var currentZone=ZoneId.of("Asia/Damascus")
  override fun getZone()=currentZone
  override fun withZone(zone:ZoneId):Clock=Clock.fixed(time.get(),zone)
  override fun instant()=time.get()
 }
 @Test fun `concurrent counters survive a stale reset and reopen using shared civil clock`()=runTest {
  val file=Files.createTempDirectory("batch05-counter").resolve("prefs.preferences_pb").toFile()
  var scope=CoroutineScope(SupervisorJob()+Dispatchers.IO);val clock=MovingClock()
  var prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}),clock)
  prefs.incrementDailyActivityCount(DailyActivityIds.TASBEEH,5)
  clock.time.set(Instant.parse("2026-10-21T21:01:00Z"))
  coroutineScope { repeat(50) { launch { prefs.incrementDailyActivityCount(DailyActivityIds.TASBEEH) } } }
  prefs.ensureDailyActivitiesForDate("2026-10-21")
  val snapshot=prefs.dailyActivitySnapshot.first();assertEquals("2026-10-22",snapshot.first);assertEquals(50,snapshot.second[DailyActivityIds.TASBEEH])
  scope.cancel();scope.coroutineContext[Job]!!.join();scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
  try {
   prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}),clock)
   assertEquals(snapshot,prefs.dailyActivitySnapshot.first())
  } finally { scope.cancel();scope.coroutineContext[Job]!!.join() }
 }
 @Test fun `simultaneous last-read pairs never mix and Asma mutations retain unrelated ids`()=runTest {
  val file=Files.createTempDirectory("batch05-pairs").resolve("prefs.preferences_pb").toFile()
  val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
  try {
   val prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}))
   prefs.setFavoriteAsmaIds(setOf("future-id"))
   val pairs=setOf(1 to 2,2 to 100)
   val seen=mutableListOf<Pair<Int,Int>>()
   val observer=launch { prefs.lastReadPosition.filterNotNull().take(10).collect { seen+=it } }
   coroutineScope { repeat(20) { n -> launch { val p=if(n%2==0) 1 to 2 else 2 to 100;prefs.saveLastRead(p.first,p.second);prefs.toggleFavoriteAsmaId(n+1) } } }
   observer.cancelAndJoin();assertTrue(seen.all { it in pairs })
   val ids=prefs.favoriteAsmaIds.first();assertEquals(21,ids.size);assertTrue("future-id" in ids)
   prefs.toggleFavoriteAsmaId(1);assertFalse("1" in prefs.favoriteAsmaIds.first());assertTrue("future-id" in prefs.favoriteAsmaIds.first())
  } finally { scope.cancel();scope.coroutineContext[Job]!!.join() }
 }
 @Test fun `timezone change uses current civil day without hijri offset affecting records`()=runTest {
  val file=Files.createTempDirectory("batch05-zone").resolve("prefs.preferences_pb").toFile();val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
  try {
   val clock=MovingClock();clock.time.set(Instant.parse("2026-10-21T22:00:00Z"))
   val prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}),clock)
   prefs.setHijriDayOffset(2);prefs.incrementDailyActivityCount(DailyActivityIds.TASBEEH)
   assertEquals("2026-10-22",prefs.dailyActivitySnapshot.first().first)
   clock.currentZone=ZoneOffset.UTC;prefs.incrementDailyActivityCount(DailyActivityIds.TASBEEH)
   assertEquals("2026-10-21",prefs.dailyActivitySnapshot.first().first);assertEquals(1,prefs.dailyActivitySnapshot.first().second[DailyActivityIds.TASBEEH])
  } finally { scope.cancel();scope.coroutineContext[Job]!!.join() }
 }
}
