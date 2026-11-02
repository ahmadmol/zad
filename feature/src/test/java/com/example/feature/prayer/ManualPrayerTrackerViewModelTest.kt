package com.example.feature.prayer

import androidx.lifecycle.*
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import com.example.feature.prayer.presentation.ManualPrayerTrackerViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.time.*

@OptIn(ExperimentalCoroutinesApi::class)
class ManualPrayerTrackerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.now(clock)
    private val repository = mockk<ManualPrayerLogRepository>(relaxUnitFun = true)
    private val savedState = SavedStateHandle()
    private val store = ViewModelStore()
    private val firstDay = MutableStateFlow(ManualPrayerDaySummary(today, emptyMap()))
    private val previousDay = MutableStateFlow(ManualPrayerDaySummary(today.minusDays(1), emptyMap()))
    private lateinit var vm: ManualPrayerTrackerViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeDay(today) } returns firstDay
        every { repository.observeDay(today.minusDays(1)) } returns previousDay
        every { repository.observeRange(any(), any()) } returns flowOf(emptyList())
        vm = ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ManualPrayerTrackerViewModel(repository, clock, savedState) as T
        })[ManualPrayerTrackerViewModel::class.java]
    }
    @After fun teardown() { store.clear(); Dispatchers.resetMain() }

    @Test fun `a failed day cannot be logged or cleared and retry must finish first`() = runTest(dispatcher) {
        every { repository.observeDay(today) } returns flow { throw java.io.IOException("fixture") }
        vm.retry(); vm.setStatus(PrayerName.FAJR,ManualPrayerStatus.PERFORMED); advanceUntilIdle()
        vm.onPrayerTapped(PrayerName.FAJR); vm.setStatus(PrayerName.FAJR,null); advanceUntilIdle()
        assertFalse(vm.uiState.value.dataReady)
        coVerify(exactly=0) { repository.log(any(),any(),any()) }
        coVerify(exactly=0) { repository.clear(any(),any()) }
        every { repository.observeDay(today) } returns firstDay
        vm.retry(); vm.onPrayerTapped(PrayerName.FAJR); advanceUntilIdle()
        coVerify(exactly=0) { repository.log(any(),any(),any()) }
        assertTrue(vm.uiState.value.dataReady)
        vm.onPrayerTapped(PrayerName.FAJR); advanceUntilIdle()
        coVerify(exactly=1) { repository.log(today,PrayerName.FAJR,ManualPrayerStatus.PERFORMED) }
    }
    @Test fun `later observation failure revokes readiness without removing recorded prayers`() = runTest(dispatcher) {
        val fail=CompletableDeferred<Unit>()
        val recorded=mapOf(PrayerName.FAJR to ManualPrayerStatus.LATE)
        every { repository.observeDay(today) } returns flow {
            emit(ManualPrayerDaySummary(today,recorded)); fail.await(); throw java.io.IOException("fixture")
        }
        vm.retry(); runCurrent(); assertTrue(vm.uiState.value.dataReady)
        fail.complete(Unit); runCurrent()
        assertEquals(recorded,vm.uiState.value.logs); assertFalse(vm.uiState.value.dataReady)
        vm.onPrayerTapped(PrayerName.FAJR); runCurrent()
        coVerify(exactly=0) { repository.log(any(),any(),any()) }
    }

    @Test fun `observing or changing a date never records a prayer`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.selectDate(today.minusDays(1)); advanceUntilIdle()
        assertEquals(today.minusDays(1), vm.uiState.value.date)
        assertEquals(today.minusDays(1).toEpochDay(), savedState.get<Long>("prayer_journal_day"))
        coVerify(exactly = 0) { repository.log(any(), any(), any()) }
        coVerify(exactly = 0) { repository.clear(any(), any()) }
    }
    @Test fun `a late emission from the old day cannot replace the selected date`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.selectDate(today.minusDays(1)); advanceUntilIdle()
        firstDay.value = ManualPrayerDaySummary(today, mapOf(PrayerName.FAJR to ManualPrayerStatus.PERFORMED))
        advanceUntilIdle()
        assertEquals(today.minusDays(1), vm.uiState.value.date)
        assertTrue(vm.uiState.value.logs.isEmpty())
    }
    @Test fun `an explicit choice targets the selected date and failure is recoverable`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.selectDate(today.minusDays(1)); advanceUntilIdle()
        coEvery { repository.log(any(), any(), any()) } throws IllegalStateException("storage")
        vm.setStatus(PrayerName.FAJR, ManualPrayerStatus.LATE); advanceUntilIdle()
        coVerify { repository.log(today.minusDays(1), PrayerName.FAJR, ManualPrayerStatus.LATE) }
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.busy)
        assertTrue(vm.uiState.value.logs.isEmpty())
    }
    @Test fun `future date selection is ignored`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.selectDate(today.plusDays(1)); advanceUntilIdle()
        assertEquals(today, vm.uiState.value.date)
        verify(exactly = 0) { repository.observeDay(today.plusDays(1)) }
    }
}
