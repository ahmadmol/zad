package com.example.feature.core

import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import com.example.feature.calendar.describeCalendarDay
import com.example.feature.core.util.HijriCalendarDay
import com.example.feature.core.util.HijriDateProvider
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CalendarDaySemanticsTest {
    @Test fun `actual Compose semantics writes fasting annotations without reading write-only properties`() {
        val today=LocalDate.of(2026,10,21)
        val day=HijriCalendarDay(today,HijriDateProvider().date(today))
        listOf(null,"صمت","لم أصم").forEach { annotation ->
            val receiver=SemanticsConfiguration()
            receiver.describeCalendarDay(day,today,today.toEpochDay(),annotation)
            val label=receiver[SemanticsProperties.ContentDescription].single()
            assertTrue(label.contains(today.toString()));assertTrue(label.contains("اليوم"))
            if(annotation!=null) assertTrue(label.endsWith(annotation))
            assertTrue(receiver[SemanticsProperties.Selected])
        }
    }
}
