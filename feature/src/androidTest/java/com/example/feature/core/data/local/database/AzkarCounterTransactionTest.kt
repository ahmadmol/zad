package com.example.feature.core.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.feature.azkar.data.local.entity.DailyStatEntity
import com.example.feature.azkar.data.local.entity.ZikrEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*

class AzkarCounterTransactionTest {
    @Test fun failedStatisticsWriteRollsBackTheCounter()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),IhsanDatabase::class.java).build()
        try {
            val dao=db.azkarDao()
            dao.insertZikr(ZikrEntity(id=1,text="synthetic",category="fixture",targetCount=3))
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_stats BEFORE INSERT ON daily_stats BEGIN SELECT RAISE(ABORT,'synthetic failure'); END")
            try {dao.incrementCounterAndStat(1,"2026-11-02");fail("must roll back")}
            catch (_:android.database.sqlite.SQLiteException) { }
            assertEquals(0,dao.getZikrById(1)?.currentCount)
            assertTrue(dao.getAllDailyStats().first().isEmpty())
        } finally {db.close()}
    }
    @Test fun concurrentTapsRespectTargetAndLegacyDuplicateStatisticsIncrementOnce()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),IhsanDatabase::class.java).build()
        try {
            val dao=db.azkarDao()
            dao.insertZikr(ZikrEntity(id=1,text="synthetic",category="fixture",targetCount=3))
            dao.insertOrUpdateDailyStat(DailyStatEntity(date="2026-11-02",totalCount=10))
            dao.insertOrUpdateDailyStat(DailyStatEntity(date="2026-11-02",totalCount=20))
            coroutineScope {repeat(12) {launch(Dispatchers.IO){dao.incrementCounterAndStat(1,"2026-11-02")}}}
            assertEquals(3,dao.getZikrById(1)?.currentCount)
            val stats=dao.getAllDailyStats().first()
            assertEquals(2,stats.size);assertEquals(33,stats.sumOf {it.totalCount})
            assertFalse(dao.incrementCounterAndStat(999,"2026-11-02"))
        } finally {db.close()}
    }
}
