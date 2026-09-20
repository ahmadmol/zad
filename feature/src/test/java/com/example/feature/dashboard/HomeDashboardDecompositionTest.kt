package com.example.feature.dashboard

import com.example.feature.asma.domain.model.AllahName
import com.example.feature.asma.domain.util.AsmaTodayResolver
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import com.example.feature.dashboard.domain.usecase.RefreshHomeDashboardUseCase
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

class RefreshHomeDashboardFailureTest {

    @Test
    fun `location failure is returned to the caller`() = kotlinx.coroutines.test.runTest {
        val expected = IllegalStateException("location unavailable")
        val result = RefreshHomeDashboardUseCase(
            prayerFacade = FakePrayerFacade(Result.failure(expected)),
            dailyActivityRepository = FakeDailyActivityRepository()
        )()

        assertEquals(expected, result.exceptionOrNull())
    }

    @Test
    fun `daily reset failure is returned before location refresh`() = kotlinx.coroutines.test.runTest {
        val expected = IllegalStateException("reset failed")
        val facade = FakePrayerFacade(Result.success(Unit))
        val result = RefreshHomeDashboardUseCase(
            prayerFacade = facade,
            dailyActivityRepository = FakeDailyActivityRepository(Result.failure(expected))
        )()

        assertEquals(expected, result.exceptionOrNull())
        assertEquals(0, facade.refreshCalls)
    }

    private class FakeDailyActivityRepository(
        private val resetResult: Result<Unit> = Result.success(Unit)
    ) : DailyActivityRepository {
        override fun observeToday() = flowOf(
            com.example.feature.dashboard.domain.model.HomeDailyActivitySummary("", emptyList())
        )
        override suspend fun increment(activityId: String) = Result.success(Unit)
        override suspend fun markComplete(activityId: String) = Result.success(Unit)
        override suspend fun resetIfRequired() = resetResult
    }

    private class FakePrayerFacade(
        private val refreshResult: Result<Unit>
    ) : PrayerTimesFacade {
        var refreshCalls = 0
        override val prayerDay = MutableStateFlow<PrayerDay?>(null)
        override val nextPrayer = MutableStateFlow<NextPrayer?>(null)
        override val locationState = MutableStateFlow<PrayerLocationState>(PrayerLocationState.Loading)
        override val systemStatus = MutableStateFlow(
            PrayerSystemStatus(
                locationState = PrayerLocationState.Loading,
                alarmPermission = com.example.feature.prayer.domain.model.PrayerAlarmPermissionState.Unknown,
                lastReconciliationEpochMillis = null,
                lastReconciliationSuccess = null,
                lastFailureSummary = null,
                scheduledAlarmCount = 0
            )
        )

        override suspend fun refreshLocation() {
            refreshCalls++
            refreshResult.getOrThrow()
        }

        override suspend fun updateSettings(settings: PrayerCalculationSettings) = Unit
        override suspend fun reconcileSchedule(reason: PrayerReconciliationReason) = Unit
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
