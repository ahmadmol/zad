package com.example.feature.prayer

import androidx.lifecycle.*
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.presentation.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerFailureRecoveryTest {
    private val dispatcher=StandardTestDispatcher()
    private val facade=mockk<PrayerTimesFacade>(relaxUnitFun=true)
    private val settings=mockk<PrayerSettingsRepository>(relaxUnitFun=true)
    private val store=ViewModelStore()
    private val system=MutableStateFlow(PrayerSystemStatus(PrayerLocationState.Loading,PrayerAlarmPermissionState.Unknown,null,null,null,0))
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { facade.prayerDay } returns MutableStateFlow(null)
        every { facade.nextPrayer } returns MutableStateFlow(null)
        every { facade.locationState } returns MutableStateFlow(PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION))
        every { facade.systemStatus } returns system
        every { settings.observeSettings() } returns flowOf(PrayerCalculationSettings())
    }
    @After fun teardown() {store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=PrayerViewModel(facade,settings) as T
    })[PrayerViewModel::class.java]

    @Test fun `initial refresh failure stays in recoverable state and retry recovers`()=runTest(dispatcher) {
        coEvery { facade.refreshLocation() } throws java.io.IOException("private fixture details")
        val vm=vm();advanceUntilIdle()
        assertNotNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isUpdating)
        assertFalse(vm.uiState.value.error.orEmpty().contains("private fixture"))
        coEvery { facade.refreshLocation() } just Runs
        vm.onAction(PrayerAction.OnRefresh);advanceUntilIdle()
        assertNull(vm.uiState.value.error);assertTrue(vm.uiState.value.locationUnavailable)
        assertEquals("—",vm.uiState.value.nextPrayerCountdown)
    }
    @Test fun `settings observation can fail and resubscribe without invented prayer times`()=runTest(dispatcher) {
        every { settings.observeSettings() } returns flow {throw java.io.IOException("fixture")}
        val vm=vm();advanceUntilIdle()
        assertNotNull(vm.uiState.value.error);assertTrue(vm.uiState.value.prayerTimes.isEmpty())
        every { settings.observeSettings() } returns flowOf(PrayerCalculationSettings(notificationsEnabled=false))
        vm.onAction(PrayerAction.OnRefresh);advanceUntilIdle()
        assertNull(vm.uiState.value.error);assertFalse(vm.uiState.value.adhanEnabled)
    }
    @Test fun `failed setting writes cannot crash and duplicate taps submit only once`()=runTest(dispatcher) {
        coEvery { settings.updatePrePrayerMinutes(5) } coAnswers {delay(100);throw java.io.IOException("fixture")}
        val vm=vm();advanceUntilIdle()
        vm.onAction(PrayerAction.OnUpdatePrePrayerMinutes(5));vm.onAction(PrayerAction.OnUpdatePrePrayerMinutes(5))
        advanceUntilIdle();coVerify(exactly=1) {settings.updatePrePrayerMinutes(5)}
        assertNotNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isUpdating)
        coEvery { settings.updatePrePrayerMinutes(5) } just Runs
        vm.onAction(PrayerAction.OnUpdatePrePrayerMinutes(5));advanceUntilIdle()
        assertNull(vm.uiState.value.error)
    }
    @Test fun `iqamah selection writes the existing setting and reconciles alarms`()=runTest(dispatcher) {
        val vm=vm();advanceUntilIdle()
        vm.onAction(PrayerAction.OnUpdateIqamahMinutes(15));advanceUntilIdle()
        coVerify(exactly=1) {settings.updateIqamahMinutes(15)}
        coVerify {facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)}
        system.value=system.value.copy(lastReconciliationSuccess=false,lastFailureSummary="fixture schedule unavailable")
        runCurrent();assertEquals("fixture schedule unavailable",vm.uiState.value.scheduleWarning)
    }
}
