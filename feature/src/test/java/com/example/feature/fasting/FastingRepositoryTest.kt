package com.example.feature.fasting

import com.example.feature.fasting.data.*
import com.example.feature.fasting.domain.*
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class FastingRepositoryTest {
    private val dao=mockk<FastingLogDao>(relaxUnitFun=true)
    private val clock=Clock.fixed(Instant.parse("2026-10-20T20:30:00Z"),ZoneId.of("Asia/Damascus"))
    private val today=LocalDate.now(clock)
    private fun repo(rows: List<FastingLogEntity> = emptyList()): FastingRepository {
        every { dao.observeAll() } returns flowOf(rows)
        return FastingRepository(dao,clock)
    }
    @Test fun `observation and Ramadan day do not create any records`() = runTest {
        assertTrue(repo().records.first().isEmpty())
        coVerify(exactly=0) {dao.upsert(any())}
        coVerify(exactly=0) {dao.clearFastingOnly()}
    }
    @Test fun `explicit status and optional text use civil key without any Hijri offset`() = runTest {
        repo().save(today,FastingStatus.FASTED,"  وصف شخصي  ")
        coVerify {dao.upsert(FastingLogEntity(today.toEpochDay(),"FASTED","وصف شخصي",clock.millis()))}
    }
    @Test fun `same date edits address the same primary key for both statuses`() = runTest {
        val r=repo();r.save(today,FastingStatus.FASTED,"");r.save(today,FastingStatus.NOT_FASTED,"تعديل")
        val rows=mutableListOf<FastingLogEntity>()
        coVerify(exactly=2) {dao.upsert(capture(rows))}
        assertEquals(setOf(today.toEpochDay()),rows.map {it.dateEpochDay}.toSet())
        assertEquals(listOf("FASTED","NOT_FASTED"),rows.map {it.status})
    }
    @Test fun `future date and oversized labels never write`() = runTest {
        val r=repo()
        try {r.save(today.plusDays(1),FastingStatus.FASTED,"");fail()} catch (_:IllegalArgumentException) {}
        try {r.save(today,FastingStatus.FASTED,"x".repeat(81));fail()} catch (_:IllegalArgumentException) {}
        coVerify(exactly=0) {dao.upsert(any())}
    }
    @Test fun `unknown rows survive without being classified as fasted or unrecorded`() = runTest {
        val rows=repo(listOf(FastingLogEntity(today.toEpochDay(),"FUTURE_STATUS","محفوظ",0))).records.first()
        assertEquals(1,rows.size);assertNull(rows.single().status);assertEquals("محفوظ",rows.single().typeLabel)
        coVerify(exactly=0) {dao.upsert(any())}
    }
    @Test fun `delete a day and clear fasting target only the fasting DAO`() = runTest {
        val r=repo();r.delete(today);r.clearFastingOnly()
        coVerify(exactly=1) {dao.deleteDay(today.toEpochDay())}
        coVerify(exactly=1) {dao.clearFastingOnly()}
        coVerify(exactly=0) {dao.upsert(any())}
    }
    @Test fun `storage failure propagates without a false successful write`() = runTest {
        val r=repo();coEvery {dao.upsert(any())} throws IllegalStateException("disk")
        try {r.save(today,FastingStatus.FASTED,"");fail()} catch (_:IllegalStateException) {}
        coVerify(exactly=0) {dao.deleteDay(any())}
        coVerify(exactly=0) {dao.clearFastingOnly()}
    }
    @Test fun `local timezone day is used rather than UTC date`() = runTest {
        every {dao.observeAll()} returns flowOf(emptyList())
        val instant=Instant.parse("2026-10-20T22:00:00Z")
        val local=FastingRepository(dao,Clock.fixed(instant,ZoneId.of("Asia/Damascus")))
        assertEquals(LocalDate.of(2026,10,21),local.today())
        local.save(local.today(),FastingStatus.FASTED,"")
        coVerify {dao.upsert(match {it.dateEpochDay==LocalDate.of(2026,10,21).toEpochDay()})}
    }
}
