package com.example.feature.quran.data

import com.example.feature.quran.data.local.dao.*
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.data.repository.ReadingJourneyRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.TimeZone

class ReadingJourneyRepositoryTest {
    private val dao = mockk<ReadingJourneyDao>(relaxUnitFun = true)
    private val quran = mockk<QuranDao>()
    private val now = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC)
    private fun repository(): ReadingJourneyRepositoryImpl {
        every { dao.observePlans() } returns flowOf(emptyList())
        every { dao.observeCompletions() } returns flowOf(emptyList())
        every { dao.observeNotes() } returns flowOf(emptyList())
        return ReadingJourneyRepositoryImpl(dao, quran, now)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `concurrent edit and deletion cannot resurrect a deleted private note`() = runTest {
        val repo=repository()
        var stored:PrivateAyahNoteEntity?=PrivateAyahNoteEntity("n",2,3,"old",1,2)
        val release=CompletableDeferred<Unit>()
        coEvery {quran.getAyahBySurahAndVerse(2,3)} returns AyahEntity(10,2,3,"fixture")
        coEvery {dao.note("n")} coAnswers {val snapshot=stored;release.await();snapshot}
        coEvery {dao.saveNote(any())} answers {stored=firstArg()}
        coEvery {dao.deleteNote("n")} answers {stored=null}
        val edit=launch {repo.saveNote("n",2,3,"new")};runCurrent()
        val delete=launch {repo.deleteNote("n")};runCurrent()
        coVerify(exactly=0) {dao.deleteNote(any())}
        release.complete(Unit);joinAll(edit,delete)
        assertNull(stored)
    }
    @Test fun `creating a plan persists inclusive dates without marking pages read`() = runTest {
        val repo = repository()
        repo.createPlan(1, 604, 30)
        coVerify { dao.replaceActive(match { it.firstPage == 1 && it.lastPage == 604 && it.dueEpochDay - it.startEpochDay == 29L }) }
        coVerify(exactly = 0) { dao.insertCompletions(any()) }
    }
    @Test fun `a repository already alive follows system timezone changes for new plans`() = runTest {
        repository() // initialize the DAO flow stubs
        val originalZone = TimeZone.getDefault()
        val starts = mutableListOf<Long>()
        coEvery { dao.replaceActive(any()) } answers { starts += firstArg<ReadingPlanEntity>().startEpochDay }
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-12:00"))
            val repo = ReadingJourneyRepositoryImpl(dao, quran)
            repo.createPlan(1, 604, 30)
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+14:00"))
            repo.createPlan(1, 604, 30)
            assertTrue(starts[1] > starts[0])
        } finally { TimeZone.setDefault(originalZone) }
    }
    @Test fun `confirmation uses a set and rejects pages outside the plan`() = runTest {
        val repo = repository()
        coEvery { dao.plan("p") } returns ReadingPlanEntity("p", 1, 30, 10, 20, true)
        repo.confirmPages("p", setOf(10, 11))
        coVerify { dao.insertCompletions(match { it.map { row -> row.pageNumber } == listOf(10, 11) }) }
        try { repo.confirmPages("p", setOf(9)); fail("invalid page accepted") } catch (_: IllegalArgumentException) { }
        coVerify(exactly = 1) { dao.insertCompletions(any()) }
    }
    @Test fun `archived plans cannot receive new confirmations`() = runTest {
        val repo = repository()
        coEvery { dao.plan("p") } returns ReadingPlanEntity("p", 1, 30, 1, 604, false)
        try { repo.confirmPages("p", setOf(1)); fail("archived plan changed") } catch (_: IllegalArgumentException) { }
        coVerify(exactly = 0) { dao.insertCompletions(any()) }
    }
    @Test fun `note edit retains id verse and creation date`() = runTest {
        val repo = repository()
        coEvery { quran.getAyahBySurahAndVerse(2, 3) } returns AyahEntity(10, 2, 3, "fixture")
        coEvery { dao.note("n") } returns PrivateAyahNoteEntity("n", 2, 3, "old", 123, 124)
        repo.saveNote("n", 2, 3, " new ")
        coVerify { dao.saveNote(match { it.id == "n" && it.createdAt == 123L && it.text == "new" && it.updatedAt == now.millis() }) }
    }
    @Test fun `nonexistent verse cannot acquire a note`() = runTest {
        val repo = repository()
        coEvery { quran.getAyahBySurahAndVerse(any(), any()) } returns null
        try { repo.saveNote(null, 999, 4, "private"); fail("invalid reference accepted") } catch (_: IllegalArgumentException) { }
        coVerify(exactly = 0) { dao.saveNote(any()) }
    }
}
