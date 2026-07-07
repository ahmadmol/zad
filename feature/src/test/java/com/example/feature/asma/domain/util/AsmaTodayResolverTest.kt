package com.example.feature.asma.domain.util

import com.example.feature.asma.domain.model.AllahName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar
import java.util.Date

class AsmaTodayResolverTest {

    @Test
    fun `selectDailyName returns same name for same day`() {
        val names = listOf(
            AllahName(1, "الرحمن", "Ar-Rahman", "الرحيم", "شرح"),
            AllahName(2, "الرحيم", "Ar-Raheem", "الرحيم", "شرح")
        )

        val firstDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val secondDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 20, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val first = AsmaTodayResolver.selectDailyName(names, firstDate)
        val second = AsmaTodayResolver.selectDailyName(names, secondDate)

        assertNotNull(first)
        assertEquals(first?.id, second?.id)
    }

    @Test
    fun `selectDailyName uses the list size when date changes`() {
        val names = listOf(
            AllahName(1, "الرحمن", "Ar-Rahman", "الرحيم", "شرح"),
            AllahName(2, "الرحيم", "Ar-Raheem", "الرحيم", "شرح")
        )

        val date = Date()
        val selected = AsmaTodayResolver.selectDailyName(names, date)

        assertNotNull(selected)
    }
}
