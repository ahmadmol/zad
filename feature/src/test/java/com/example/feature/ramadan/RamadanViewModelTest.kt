package com.example.feature.ramadan

import androidx.lifecycle.*
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fasting.data.FastingRepository
import com.example.feature.fasting.domain.*
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.*
import com.example.feature.quran.domain.repository.ReadingJourneyRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RamadanViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    private val facade=mockk<PrayerTimesFacade>()
    private val reading=mockk<ReadingJourneyRepository>()
    private val fasting=mockk<FastingRepository>()
    private val preferences=mockk<UserPreferences>()
    private val store=ViewModelStore()
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every {facade.prayerDay} returns MutableStateFlow(null)
        every {facade.locationState} returns MutableStateFlow(PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION))
        every {reading.plans} returns flowOf(emptyList());every {reading.completedPages} returns flowOf(emptyMap())
        every {fasting.records} returns flowOf(emptyList())
        every {preferences.hijriDayOffset} returns flowOf(0);every {preferences.ramadanHighlightEnabled} returns flowOf(false)
    }
    @After fun teardown(){store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=RamadanViewModel(facade,reading,fasting,preferences) as T
    })[RamadanViewModel::class.java]
    @Test fun `opening hub observes only and never refreshes location or records worship`() = runTest(dispatcher) {
        val vm=vm();advanceUntilIdle()
        assertFalse(vm.state.value.readingLoading);assertFalse(vm.state.value.fastingLoading)
        coVerify(exactly=0) {facade.refreshLocation()};coVerify(exactly=0) {fasting.save(any(),any(),any())}
        coVerify(exactly=0) {reading.confirmPages(any(),any())}
    }
    @Test fun `reading failure does not turn fasting data into zero or block independent section`() = runTest(dispatcher) {
        every {reading.plans} returns flow {throw IllegalStateException("read")}
        every {fasting.records} returns flowOf(listOf(FastingRecord(LocalDate.of(2026,10,20),FastingStatus.FASTED,"")))
        val vm=vm();advanceUntilIdle();assertNotNull(vm.state.value.readingError)
        assertNull(vm.state.value.fastingError);assertEquals(1,vm.state.value.fasting.size)
        every {reading.plans} returns flowOf(emptyList());vm.reloadData();advanceUntilIdle();assertNull(vm.state.value.readingError)
    }
    @Test fun `refresh and preference write failures are explicit and release busy flags`() = runTest(dispatcher) {
        coEvery {facade.refreshLocation()} throws IllegalStateException("location")
        coEvery {preferences.setRamadanHighlightEnabled(true)} throws IllegalStateException("disk")
        val vm=vm();advanceUntilIdle();vm.refreshPrayer();vm.highlight(true);advanceUntilIdle()
        assertNotNull(vm.state.value.prayerError);assertNotNull(vm.state.value.preferencesError)
        assertFalse(vm.state.value.prayerBusy);assertFalse(vm.state.value.preferencesBusy);assertFalse(vm.state.value.highlight)
    }
}
