package com.example.feature.qibla.presentation

import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.qibla.util.QiblaManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QiblaOfflineFirstStateTest {

    @Test
    fun `saved location makes the first bearing immediately usable`() = qiblaTest(
        PrayerLocationState.Available(ALEPPO, PrayerLocationSource.Saved)
    ) { harness ->
        val state = harness.viewModel.uiState.value

        assertEquals(169.3f, state.qiblaAngle, 1.0f)
        assertFalse(state.isLoading)
        assertEquals("حلب", state.locationName)
        assertEquals("موقع محفوظ", state.cityAndCountry)
        assertNull(state.error)
    }

    @Test
    fun `manual location uses the canonical manual source label`() = qiblaTest(
        PrayerLocationState.Available(ALEPPO, PrayerLocationSource.Manual)
    ) { harness ->
        val state = harness.viewModel.uiState.value

        assertEquals("حلب", state.locationName)
        assertEquals("موقع يدوي", state.cityAndCountry)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initial unavailable location produces a location-specific error`() = qiblaTest(
        PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION)
    ) { harness ->
        val state = harness.viewModel.uiState.value

        assertEquals(0f, state.qiblaAngle)
        assertFalse(state.isLoading)
        assertTrue(state.error.orEmpty().contains("موقع"))
        assertFalse(state.error.orEmpty().contains("الإنترنت"))
    }

    @Test
    fun `later location failure cannot remove an existing usable bearing`() = qiblaTest(
        PrayerLocationState.Available(ALEPPO, PrayerLocationSource.Saved)
    ) { harness ->
        val bearing = harness.viewModel.uiState.value.qiblaAngle

        harness.repository.locations.value =
            PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.TIMEOUT)
        runCurrent()

        val state = harness.viewModel.uiState.value
        assertEquals(bearing, state.qiblaAngle)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `refresh success replaces the unavailable state with device location`() = qiblaTest(
        PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.TIMEOUT)
    ) { harness ->
        harness.repository.refreshResult = Result.success(ALEPPO)

        harness.viewModel.updateLocationAndCalculateQibla()
        runCurrent()

        val state = harness.viewModel.uiState.value
        assertEquals(169.3f, state.qiblaAngle, 1.0f)
        assertEquals("الموقع الحالي", state.cityAndCountry)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `refresh failure without cached bearing clears loading and reports location error`() = qiblaTest(
        PrayerLocationState.Loading
    ) { harness ->
        harness.repository.refreshResult = Result.failure(IllegalStateException("provider off"))

        harness.viewModel.updateLocationAndCalculateQibla()
        runCurrent()

        val state = harness.viewModel.uiState.value
        assertEquals(0f, state.qiblaAngle)
        assertFalse(state.isLoading)
        assertTrue(state.error.orEmpty().contains("الموقع"))
    }

    @Test
    fun `alignment uses enter and exit hysteresis around the current bearing`() = qiblaTest(
        PrayerLocationState.Available(ALEPPO, PrayerLocationSource.Saved)
    ) { harness ->
        val bearing = harness.viewModel.uiState.value.qiblaAngle

        harness.rotations.emit(bearing + 2f)
        runCurrent()
        assertTrue(harness.viewModel.uiState.value.isAligned)

        harness.rotations.emit(bearing + 6f)
        runCurrent()
        assertFalse(harness.viewModel.uiState.value.isAligned)
    }

    @Test
    fun `bearing calculation remains unchanged for Aleppo`() {
        assertEquals(169.3f, QiblaManager.calculateQiblaDirection(36.2021, 37.1343), 1.0f)
    }

    private fun qiblaTest(
        initialLocation: PrayerLocationState,
        block: suspend TestScope.(Harness) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val rotations = MutableSharedFlow<Float>(extraBufferCapacity = 1)
            val manager = mockk<QiblaManager>(relaxed = true) {
                every { getRotationFlow() } returns rotations
                every { getAccuracyFlow() } returns emptyFlow()
            }
            val repository = FakeLocationRepository(initialLocation)
            val harness = Harness(QiblaViewModel(manager, repository), repository, rotations)
            runCurrent()
            block(harness)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private data class Harness(
        val viewModel: QiblaViewModel,
        val repository: FakeLocationRepository,
        val rotations: MutableSharedFlow<Float>
    )

    private class FakeLocationRepository(initial: PrayerLocationState) : PrayerLocationRepository {
        val locations = MutableStateFlow(initial)
        var refreshResult: Result<PrayerLocation> = Result.failure(IllegalStateException("not configured"))

        override fun observeLocation(): Flow<PrayerLocationState> = locations
        override suspend fun refreshLocation(): Result<PrayerLocation> = refreshResult
        override suspend fun saveManualLocation(
            latitude: Double,
            longitude: Double,
            displayName: String
        ): Result<Unit> = Result.success(Unit)
    }

    private companion object {
        val ALEPPO = PrayerLocation(36.2021, 37.1343, "حلب")
    }
}
