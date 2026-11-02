package com.example.feature.prayer

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.presentation.observePrayerSheetState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*

class PrayerSheetStateTest {
    @Test fun `every visible calculation method round trips through the canonical calculator`() {
        val choices=com.example.feature.prayer.presentation.prayerMethodChoices
        assertTrue(choices.isNotEmpty())
        choices.forEach { (method,label) ->
            assertTrue(label.isNotBlank())
            assertEquals(method,com.example.feature.prayer.data.calculator.AdhanPrayerCalculator.parseMethod(method.name))
        }
        assertEquals(choices.size,choices.map {it.first}.distinct().size)
    }
    private fun settings()=mockk<SettingsManager>().also { s ->
        every { s.calculationMethodFlow } returns flowOf("EGYPTIAN")
        every { s.madhabFlow } returns flowOf("HANAFI")
        every { s.useAutoLocationFlow } returns flowOf(false)
        every { s.manualLocationCityFlow } returns flowOf("fixture city")
        every { s.notificationSoundTypeFlow } returns flowOf("SILENT")
        every { s.prePrayerNotificationMinutesFlow } returns flowOf(5)
        every { s.iqamahNotificationMinutesFlow } returns flowOf(15)
    }
    @Test fun `a failed preference read blocks settings instead of supplying selected defaults`()=runTest {
        val s=settings();every { s.madhabFlow } returns flow {throw java.io.IOException("fixture")}
        val failed=s.observePrayerSheetState().last()
        assertFalse(failed.ready);assertNotNull(failed.error)
        every { s.madhabFlow } returns flowOf("HANAFI")
        val recovered=s.observePrayerSheetState().last()
        assertTrue(recovered.ready);assertNull(recovered.error)
        assertEquals("EGYPTIAN",recovered.method);assertEquals(15,recovered.iqamahMins)
    }
    @Test fun `cancellation does not become a user error or a successful settings read`()=runTest {
        val s=settings();every { s.madhabFlow } returns flow {throw CancellationException("fixture")}
        try { s.observePrayerSheetState().last();fail("must propagate cancellation") }
        catch (_:CancellationException) { }
    }
}
