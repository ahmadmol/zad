package com.example.feature.quran.data

import com.example.feature.quran.data.local.dao.*
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.data.repository.MemorizationRepository
import com.example.feature.quran.domain.model.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class MemorizationRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"),ZoneOffset.UTC)
    private val today = LocalDate.now(clock).toEpochDay()
    private val dao = mockk<MemorizationDao>(relaxUnitFun=true)
    private val quran = mockk<QuranDao>()
    private val plan = MemorizationPlanEntity("p",1,1,2,today-10,confirmedDay=today-9,dueDay=today-8)
    private fun repo(): MemorizationRepository {
        every { dao.plans() } returns flowOf(emptyList())
        every { dao.reviews() } returns flowOf(emptyList())
        every { quran.observeAllSurahs() } returns flowOf(emptyList())
        return MemorizationRepository(dao,quran,clock)
    }
    @Test fun `observation and verse opening never confirm or review`() = runTest {
        val r=repo()
        coEvery { quran.getAyahsBySurah(1) } returns listOf(mockk { every { verseNumber } returns 1 },mockk { every { verseNumber } returns 2 })
        assertEquals(2,r.verses(1,1,2).size)
        coVerify(exactly=0) { dao.update(any()) }
        coVerify(exactly=0) { dao.record(any(),any()) }
    }
    @Test fun `missing verse rejects range without inserting`() = runTest {
        val r=repo()
        coEvery { quran.getAyahsBySurah(1) } returns listOf(mockk { every { verseNumber } returns 1 })
        try { r.save(null,1,1,2); fail() } catch (_: IllegalArgumentException) { }
        coVerify(exactly=0) { dao.insert(any()) }
    }
    @Test fun `range editing preserves the old history in a transaction`() = runTest {
        val r=repo()
        coEvery { dao.plan("p") } returns plan
        coEvery { quran.getAyahsBySurah(1) } returns listOf(mockk { every { verseNumber } returns 1 })
        r.save("p",1,1,1)
        coVerify { dao.replaceRange(plan,match { it.id!="p" && it.confirmedDay==null && it.firstAyah==1 && it.lastAyah==1 }) }
    }
    @Test fun `confirmation is idempotent and review starts tomorrow`() = runTest {
        val r=repo(); coEvery { dao.plan("p") } returns plan.copy(confirmedDay=null,dueDay=null)
        r.confirm("p")
        coVerify { dao.update(match { it.confirmedDay==today && it.dueDay==today+1 }) }
        coEvery { dao.plan("p") } returns plan
        r.confirm("p")
        coVerify(exactly=1) { dao.update(any()) }
    }
    @Test fun `overdue review uses actual day and records result atomically`() = runTest {
        val r=repo();coEvery { dao.plan("p") } returns plan
        r.review("p",ReviewResult.GOOD)
        coVerify { dao.record(match { it.dueDay==today+3 && it.lastReviewDay==today && it.stage==1 },
            match { it.planId=="p" && it.result=="GOOD" && it.reviewedDay==today && it.nextDueDay==today+3 }) }
    }
    @Test fun `same-day duplicate future unconfirmed and archived cannot record`() = runTest {
        val r=repo()
        listOf(plan.copy(lastReviewDay=today),plan.copy(lastReviewDay=today+1),plan.copy(dueDay=today+1),
            plan.copy(confirmedDay=null),plan.copy(archived=true)).forEach { row ->
            coEvery { dao.plan("p") } returns row
            try { r.review("p",ReviewResult.GOOD);fail() } catch (_: IllegalArgumentException) { }
        }
        coVerify(exactly=0) { dao.record(any(),any()) }
    }
    @Test fun `schedule has bounded stages reset and hard next day`() {
        var stage=0;var day=today
        listOf(3,7,14,30,30).forEach { interval ->
            val next=ReviewSchedule.next(stage,ReviewResult.GOOD,day)
            assertEquals(day+interval,next.dueDay);stage=next.stage;day=next.dueDay
        }
        assertEquals(NextReview(4,today+1),ReviewSchedule.next(4,ReviewResult.HARD,today))
        assertEquals(NextReview(0,today+1),ReviewSchedule.next(4,ReviewResult.AGAIN,today))
    }
    @Test fun `archive preserves confirmation and schedule`() = runTest {
        val r=repo();coEvery { dao.plan("p") } returns plan
        r.archive("p");coVerify { dao.update(plan.copy(archived=true)) }
    }
    @Test fun `rescheduling keeps progress and rejects past or distant dates`() = runTest {
        val r=repo();coEvery { dao.plan("p") } returns plan.copy(stage=3)
        r.reschedule("p",today+5)
        coVerify { dao.update(plan.copy(stage=3,dueDay=today+5)) }
        for(day in listOf(today-1,today,today+366)) {
            try {r.reschedule("p",day);fail()} catch (_: IllegalArgumentException) {}
        }
        coVerify(exactly=1) { dao.update(any()) }
    }
    @Test fun `confirmation samples civil day once even at midnight`() = runTest {
        repo() // Configure flows without touching any real database.
        var reads=0
        val crossingClock=object: Clock() {
            override fun getZone(): ZoneId = ZoneOffset.UTC
            override fun withZone(zone: ZoneId): Clock = this
            override fun instant(): Instant = if(reads++==0) Instant.parse("2026-10-20T23:59:59Z") else Instant.parse("2026-10-21T00:00:00Z")
        }
        coEvery {dao.plan("p")} returns plan.copy(confirmedDay=null,dueDay=null)
        MemorizationRepository(dao,quran,crossingClock).confirm("p")
        coVerify {dao.update(match {it.confirmedDay==today && it.dueDay==today+1})}
        assertEquals(1,reads)
    }
}
