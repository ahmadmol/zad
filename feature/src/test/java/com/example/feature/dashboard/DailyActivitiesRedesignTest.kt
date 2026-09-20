package com.example.feature.dashboard

import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.component.summarizeDailyProgress
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class DailyActivitiesRedesignTest {

    @Test
    fun `exactly six daily activities remain present and IDs are unchanged`() {
        val expected = listOf(
            DailyActivityIds.QURAN_READING,
            DailyActivityIds.MORNING_AZKAR,
            DailyActivityIds.EVENING_AZKAR,
            DailyActivityIds.TASBEEH,
            DailyActivityIds.DAILY_DUA,
            DailyActivityIds.DAILY_NAME
        )
        assertEquals(6, DailyActivityIds.ALL.size)
        assertEquals(expected, DailyActivityIds.ALL)
    }

    @Test
    fun `tasbih target is 100 and all other activity targets are 1`() {
        assertEquals(100, DailyActivityIds.targetFor(DailyActivityIds.TASBEEH))
        assertEquals(1, DailyActivityIds.targetFor(DailyActivityIds.QURAN_READING))
        assertEquals(1, DailyActivityIds.targetFor(DailyActivityIds.MORNING_AZKAR))
        assertEquals(1, DailyActivityIds.targetFor(DailyActivityIds.EVENING_AZKAR))
        assertEquals(1, DailyActivityIds.targetFor(DailyActivityIds.DAILY_DUA))
        assertEquals(1, DailyActivityIds.targetFor(DailyActivityIds.DAILY_NAME))
    }

    @Test
    fun `all six activity routes resolve to valid non-empty destinations`() {
        val items = DailyActivityMapper.mapItems(emptyMap())
        assertEquals(6, items.size)
        items.forEach { item ->
            assertTrue("Route for ${item.id} must not be blank", item.route.isNotBlank())
        }
        val quranItem = items.first { it.id == DailyActivityIds.QURAN_READING }
        assertTrue(
            "Quran route must be quran_list or quran",
            quranItem.route == "quran_list" || quranItem.route == "quran"
        )
    }

    @Test
    fun `overall summary ratio is completed items over total items`() {
        val activities = listOf(
            DailyActivityItemData("quran", "قراءة القرآن", 1, 1, "مرة", true, "quran_list"),
            DailyActivityItemData("morning", "أذكار الصباح", 1, 1, "مرة", true, "azkar_screen"),
            DailyActivityItemData("evening", "أذكار المساء", 1, 1, "مرة", true, "azkar_screen"),
            DailyActivityItemData("tasbeeh", "التسبيح", 45, 100, "حبة", false, "tasbih_screen"),
            DailyActivityItemData("dua", "دعاء اليوم", 0, 1, "مرة", false, "dua_screen"),
            DailyActivityItemData("asma", "اسم اليوم", 0, 1, "مرة", false, "asma_screen")
        )

        val summary = summarizeDailyProgress(activities)
        assertEquals(6, summary.totalCount)
        assertEquals(3, summary.doneCount)
        assertEquals(0.5f, summary.overallProgress, 0.0001f)
        assertEquals(50, summary.percentage)
    }

    @Test
    fun `date key is formatted with US locale to ensure ASCII digits in all default locales`() {
        val date = GregorianCalendar(2026, 6, 22).time
        val usFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formattedUs = usFormatter.format(date)

        assertEquals("2026-07-22", formattedUs)
        assertFalse("Date key must not contain Arabic numerals", formattedUs.contains("٢"))
    }
}
