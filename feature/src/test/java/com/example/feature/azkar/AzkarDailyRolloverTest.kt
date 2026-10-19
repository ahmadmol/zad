package com.example.feature.azkar

import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.azkar.data.repository.AzkarRepositoryImpl
import com.example.feature.azkar.util.DateProvider
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AzkarDailyRolloverTest {
    @Test fun `new day retains lifetime tasbih but clears yesterday daily totals in every filter`() = runTest {
        val dao = mockk<AzkarDao>()
        every { dao.getAllAzkar() } returns flowOf(listOf(ZikrEntity(id = 3, text = "تسبيح", category = "تسبيح",
            targetCount = 0, currentCount = 120, dailyProgress = 44, isFavorite = true, lastUpdatedDate = "2026-10-14")))
        val repo = AzkarRepositoryImpl(dao, object : DateProvider { override fun getCurrentDate() = "2026-10-15" })
        listOf(repo.getAllAzkar(), repo.getFavoriteAzkar(), repo.getAzkarByCategory("تسبيح")).forEach { flow ->
            val item = flow.first().single()
            assertEquals(120, item.currentCount)
            assertEquals(0, item.dailyProgress)
            assertTrue(item.isFavorite)
        }
    }
    @Test fun `exhausted morning zikr is available on new day without losing favorite`() = runTest {
        val row = ZikrEntity(id = 7, text = "ذكر", targetCount = 3, currentCount = 3,
            category = "أذكار الصباح", isFavorite = true, lastUpdatedDate = "2026-10-13", dailyProgress = 3)
        val dao = mockk<AzkarDao>(relaxed = true)
        every { dao.getAllAzkar() } returns flowOf(listOf(row))
        coEvery { dao.getZikrById(7) } returns row
        val dates = object : DateProvider { override fun getCurrentDate() = "2026-10-14" }
        val repo = AzkarRepositoryImpl(dao, dates)
        val visible = repo.getAllAzkar().first().single()
        assertEquals(0, visible.currentCount)
        assertTrue(visible.isFavorite)
        repo.incrementCounter(7)
        coVerify(exactly = 1) { dao.incrementZikrCount(7, "2026-10-14") }
    }
    @Test fun `same-day target does not increment stats and free counter accepts clicks`() = runTest {
        val dao = mockk<AzkarDao>(relaxed = true)
        coEvery { dao.getZikrById(1) } returns ZikrEntity(id = 1, text = "ذكر", targetCount = 1, currentCount = 1, category = "أذكار المساء", lastUpdatedDate = "2026-10-14")
        coEvery { dao.getZikrById(2) } returns ZikrEntity(id = 2, text = "سبحة", targetCount = 0, currentCount = 10, category = "تسبيح")
        val repo = AzkarRepositoryImpl(dao, object : DateProvider { override fun getCurrentDate() = "2026-10-14" })
        repo.incrementCounter(1); repo.incrementCounter(2)
        coVerify(exactly = 0) { dao.incrementZikrCount(1, any()) }
        coVerify(exactly = 1) { dao.incrementZikrCount(2, any()) }
    }
}
