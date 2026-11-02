package com.example.feature.qibla.presentation

import androidx.lifecycle.*
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.qibla.util.QiblaManager
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class QiblaFailureRecoveryTest {
    private val dispatcher=StandardTestDispatcher()
    private val store=ViewModelStore()
    private val location=PrayerLocation(36.2,37.1,"fixture city")
    private val repo=mockk<PrayerLocationRepository>()
    private val manager=mockk<QiblaManager>(relaxed=true)
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every {manager.hasCompassSensors} returns true
        every {manager.getRotationFlow()} returns emptyFlow()
        every {manager.getAccuracyFlow()} returns emptyFlow()
    }
    @After fun teardown() {store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=QiblaViewModel(manager,repo) as T
    })[QiblaViewModel::class.java]
    @Test fun `failed location observation can restart and thrown refresh does not remove the saved bearing`()=runTest(dispatcher) {
        every {repo.observeLocation()} returns flow {throw java.io.IOException("fixture")}
        coEvery {repo.refreshLocation()} throws java.io.IOException("fixture")
        val vm=vm();runCurrent();assertFalse(vm.uiState.value.isLoading);assertNotNull(vm.uiState.value.error)
        every {repo.observeLocation()} returns flowOf(PrayerLocationState.Available(location,PrayerLocationSource.Manual))
        vm.updateLocationAndCalculateQibla();runCurrent()
        assertTrue(vm.uiState.value.hasCalculatedBearing);assertEquals("موقع يدوي",vm.uiState.value.cityAndCountry)
        assertFalse(vm.uiState.value.isLoading);assertNotNull(vm.uiState.value.error)
    }
    @Test fun `concurrent refresh taps call the location provider once`()=runTest(dispatcher) {
        every {repo.observeLocation()} returns flowOf(PrayerLocationState.Available(location,PrayerLocationSource.Saved))
        coEvery {repo.refreshLocation()} coAnswers {delay(100);Result.failure(java.io.IOException("fixture"))}
        val vm=vm();runCurrent();vm.updateLocationAndCalculateQibla();vm.updateLocationAndCalculateQibla();advanceUntilIdle()
        coVerify(exactly=1) {repo.refreshLocation()};assertTrue(vm.uiState.value.hasCalculatedBearing)
    }
    @Test fun `failed compass subscriptions expose capability failure instead of crashing or aligning`()=runTest(dispatcher) {
        every {repo.observeLocation()} returns flowOf(PrayerLocationState.Available(location,PrayerLocationSource.Saved))
        every {manager.getRotationFlow()} returns flow {throw java.io.IOException("fixture")}
        every {manager.getAccuracyFlow()} returns flow {throw java.io.IOException("fixture")}
        val vm=vm();runCurrent();vm.setCompassActive(true);runCurrent()
        assertFalse(vm.uiState.value.hasCompassSensors);assertFalse(vm.uiState.value.isAligned)
        assertNull(vm.uiState.value.sensorAccuracy);assertNotNull(vm.uiState.value.calibrationMessage)
        assertTrue(vm.uiState.value.hasCalculatedBearing)
    }
}
