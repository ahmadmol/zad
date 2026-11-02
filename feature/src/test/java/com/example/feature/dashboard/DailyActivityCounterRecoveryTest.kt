package com.example.feature.dashboard

import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.dashboard.data.DailyActivityRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.time.*

class DailyActivityCounterRecoveryTest {
    private val clock=Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"),ZoneId.of("Asia/Damascus"))
    private val prefs=mockk<UserPreferences>()
    private val dao=mockk<AzkarDao>()
    private fun row(id:Long,day:String,count:Int,category:String="تسبيح")=ZikrEntity(
        id=id,text="synthetic",category=category,targetCount=0,currentCount=900,dailyProgress=count,lastUpdatedDate=day)
    private fun repository(prefDay:String,prefCount:Int,rows:List<ZikrEntity>):DailyActivityRepositoryImpl {
        every {prefs.dailyActivitySnapshot} returns flowOf(prefDay to mapOf(DailyActivityIds.TASBEEH to prefCount))
        every {dao.getAllAzkar()} returns flowOf(rows)
        return DailyActivityRepositoryImpl(prefs,clock,dao)
    }
    @Test fun `home recovers committed daily taps when the secondary summary was not saved`()=runTest {
        val home=repository("2026-11-02",0,listOf(row(1,"2026-11-02",7))).observeToday().first()
        assertEquals(7,home.items.single {it.id==DailyActivityIds.TASBEEH}.currentCount)
        coVerify(exactly=0) {prefs.incrementDailyActivityCount(any(),any())}
        coVerify(exactly=0) {dao.incrementCounterAndStat(any(),any())}
    }
    @Test fun `recovery preserves a higher explicit summary and excludes other days and categories`()=runTest {
        val home=repository("2026-11-02",20,listOf(row(1,"2026-11-02",7),row(2,"2026-11-01",100),
            row(3,"2026-11-02",100,"أذكار الصباح"))).observeToday().first()
        assertEquals(20,home.items.single {it.id==DailyActivityIds.TASBEEH}.currentCount)
        assertEquals(0,home.items.single {it.id==DailyActivityIds.MORNING_AZKAR}.currentCount)
    }
    @Test fun `yesterdays stored summary cannot contaminate todays recovered count`()=runTest {
        val home=repository("2026-11-01",100,listOf(row(1,"2026-11-02",2),row(2,"2026-11-01",100))).observeToday().first()
        assertEquals("2026-11-02",home.dateKey)
        assertEquals(2,home.items.single {it.id==DailyActivityIds.TASBEEH}.currentCount)
        assertTrue(home.items.none {it.isCompleted})
    }
}
