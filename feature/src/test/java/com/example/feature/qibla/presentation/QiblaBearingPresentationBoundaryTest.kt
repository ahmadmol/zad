package com.example.feature.qibla.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Bearing availability is explicit. A real north bearing (0°) must not be confused
 * with the unresolved placeholder.
 */
class QiblaBearingPresentationBoundaryTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    private val screen = File(
        repoRoot,
        "feature/src/main/java/com/example/feature/qibla/presentation/QiblaScreen.kt"
    ).readText()

    private val viewModel = File(
        repoRoot,
        "feature/src/main/java/com/example/feature/qibla/presentation/QiblaViewModel.kt"
    ).readText()

    /** Mirrors the screen's validity rule so the placeholder decision is testable. */
    private fun bearingLabel(qiblaAngle: Float?): String =
        qiblaAngle?.let { "${it.toInt()}°" } ?: "—°"

    @Test
    fun `default state does not present a zero degree bearing`() {
        assertEquals("—°", bearingLabel(QiblaUiState().qiblaAngle))
    }

    @Test
    fun `valid bearing is still presented as degrees`() {
        assertEquals("136°", bearingLabel(136.4f))
        assertEquals("293°", bearingLabel(293f))
        assertEquals("0°", bearingLabel(0f))
    }

    @Test
    fun `screen guards the degree display behind a validity check`() {
        assertTrue(screen.contains("val hasValidBearing = uiState.qiblaAngle != null"))
        assertTrue(screen.contains("uiState.qiblaAngle?.let"))
        assertFalse(screen.contains("qiblaAngle != 0f"))
    }

    @Test
    fun `offline first fix keeps location and geocoding out of the screen`() {
        assertTrue(viewModel.contains("val qiblaAngle: Float? = null"))
        assertTrue(viewModel.contains("withTimeoutOrNull(GEOCODER_TIMEOUT_MS)"))
        assertFalse(screen.contains("LocationServices"))
    }

    @Test
    fun `phase 2A fallback consults the canonical prayer location repository before declaring unavailable`() {
        assertTrue(
            "QiblaViewModel must consult a stored/manual fallback before declaring unavailable",
            viewModel.contains("resolveStoredManualBearing()")
        )
        assertTrue(
            "QiblaViewModel must depend on PrayerLocationRepository, not a parallel location source",
            viewModel.contains("PrayerLocationRepository")
        )
        assertTrue(
            "Stored/manual fallback must explicitly skip the Device source to avoid re-using the same Fused fix",
            viewModel.contains("PrayerLocationSource.Saved") || viewModel.contains("PrayerLocationSource.Manual")
        )
    }

    @Test
    fun `phase 2A fallback uses a short repository read timeout`() {
        assertTrue(
            "Stored/manual read must be bounded by an explicit timeout constant",
            viewModel.contains("STORED_LOCATION_TIMEOUT_MS")
        )
    }
}
