package com.example.feature.qibla.presentation

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Presentation boundary checks for the current zero-sentinel Qibla state. */
class QiblaBearingPresentationBoundaryTest {
    private val root = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() } ?: dir
    }
    private val screen = File(root, "feature/src/main/java/com/example/feature/qibla/presentation/QiblaScreen.kt").readText()
    private val viewModel = File(root, "feature/src/main/java/com/example/feature/qibla/presentation/QiblaViewModel.kt").readText()

    @Test
    fun `default state uses zero sentinel while location is loading`() {
        val state = QiblaUiState()
        assertEquals(0f, state.qiblaAngle)
        assertTrue(state.isLoading)
    }

    @Test
    fun `screen does not render compass for unresolved error state`() {
        assertTrue(screen.contains("uiState.error != null && uiState.qiblaAngle == 0f"))
        assertTrue(screen.contains("qiblaDirection = uiState.qiblaAngle"))
    }

    @Test
    fun `screen delegates location work to viewmodel`() {
        assertFalse(screen.contains("LocationServices"))
        assertTrue(screen.contains("viewModel.updateLocationAndCalculateQibla()"))
    }

    @Test
    fun `viewmodel consumes canonical prayer location repository`() {
        assertTrue(viewModel.contains("PrayerLocationRepository"))
        assertTrue(viewModel.contains("locationRepository.observeLocation()"))
        assertTrue(viewModel.contains("is PrayerLocationState.Available -> applyLocation"))
    }

    @Test
    fun `unavailable update preserves an existing bearing`() {
        assertTrue(viewModel.contains("if (_uiState.value.qiblaAngle == 0f)"))
    }

    @Test
    fun `canonical source labels cover device saved and manual`() {
        assertTrue(viewModel.contains("PrayerLocationSource.Device -> \"الموقع الحالي\""))
        assertTrue(viewModel.contains("PrayerLocationSource.Saved -> \"موقع محفوظ\""))
        assertTrue(viewModel.contains("PrayerLocationSource.Manual -> \"موقع يدوي\""))
    }
}
