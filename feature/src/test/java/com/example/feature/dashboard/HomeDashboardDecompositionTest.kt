package com.example.feature.dashboard

import com.example.feature.asma.domain.model.AllahName
import com.example.feature.asma.domain.util.AsmaTodayResolver
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import com.example.feature.dashboard.domain.model.HomeSectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.GregorianCalendar

class DailyActivityMapperTest {

    @Test
    fun `bounds counts to targets and marks completion`() {
        val items = DailyActivityMapper.mapItems(
            mapOf(
                DailyActivityIds.TASBEEH to 150,
                DailyActivityIds.QURAN_READING to 1,
                DailyActivityIds.MORNING_AZKAR to 0
            )
        )
        val tasbih = items.first { it.id == DailyActivityIds.TASBEEH }
        assertEquals(100, tasbih.currentCount)
        assertTrue(tasbih.isCompleted)

        val quran = items.first { it.id == DailyActivityIds.QURAN_READING }
        assertTrue(quran.isCompleted)

        val morning = items.first { it.id == DailyActivityIds.MORNING_AZKAR }
        assertFalse(morning.isCompleted)
    }

    @Test
    fun `includes all known activity templates`() {
        val items = DailyActivityMapper.mapItems(emptyMap())
        assertEquals(DailyActivityIds.ALL.size, items.size)
        assertEquals(DailyActivityIds.ALL.toSet(), items.map { it.id }.toSet())
    }

    @Test
    fun `rejects unknown ids`() {
        assertFalse(DailyActivityMapper.isValidId("unknown"))
        assertTrue(DailyActivityMapper.isValidId(DailyActivityIds.TASBEEH))
    }
}

class HomeSectionIsolationTest {

    @Test
    fun `content and error are distinct section states`() {
        val content = HomeSectionState.Content("ok")
        val error = HomeSectionState.Error(canRetry = true)
        assertTrue(content is HomeSectionState.Content)
        assertTrue(error is HomeSectionState.Error)
    }
}

class AsmaTodayResolverCharacterizationTest {

    @Test
    fun `selection is deterministic for fixed date`() {
        val names = listOf(
            AllahName(1, "أ", "A", "m1", "e1"),
            AllahName(2, "ب", "B", "m2", "e2"),
            AllahName(3, "ج", "C", "m3", "e3")
        )
        val date = GregorianCalendar(2024, 5, 15).time
        val first = AsmaTodayResolver.selectDailyName(names, date)
        val second = AsmaTodayResolver.selectDailyName(names, date)
        assertEquals(first, second)
    }

    @Test
    fun `empty list returns null`() {
        assertEquals(null, AsmaTodayResolver.selectDailyName(emptyList()))
    }
}
