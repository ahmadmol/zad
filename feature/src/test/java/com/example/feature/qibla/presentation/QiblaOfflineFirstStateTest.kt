package com.example.feature.qibla.presentation

import com.example.feature.qibla.util.QiblaManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QiblaOfflineFirstStateTest {

    @Test
    fun `bearing is ready before a later successful label`() {
        val locating = QiblaStateReducer.refreshStarted(QiblaUiState())
        val bearing = QiblaStateReducer.bearingResolved(locating, 170f)

        assertEquals(170f, bearing.qiblaAngle)
        assertFalse(bearing.isLoading)
        assertEquals("", bearing.locationName)

        val labeled = QiblaStateReducer.labelsResolved(bearing, "حي الجميلية" to "حلب، سوريا")
        assertEquals(170f, labeled.qiblaAngle)
        assertEquals("حي الجميلية", labeled.locationName)
    }

    @Test
    fun `geocoder failure fallback cannot remove a usable bearing`() {
        val bearing = QiblaStateReducer.bearingResolved(QiblaUiState(), 170f)
        val fallback = QiblaStateReducer.labelsResolved(bearing, QiblaViewModel.FALLBACK_LOCATION_LABELS)

        assertEquals(170f, fallback.qiblaAngle)
        assertFalse(fallback.isLoading)
        assertEquals("الموقع الحالي", fallback.locationName)
    }

    @Test
    fun `geocoder timeout fallback leaves no blocking loading`() {
        val bearing = QiblaStateReducer.bearingResolved(QiblaUiState(), 170f)
        val timedOut = QiblaStateReducer.labelsResolved(bearing, QiblaViewModel.FALLBACK_LOCATION_LABELS)

        assertFalse(timedOut.isLoading)
        assertEquals(170f, timedOut.qiblaAngle)
        assertTrue(QiblaViewModel.GEOCODER_TIMEOUT_MS > 0L)
    }

    @Test
    fun `cached location makes the first bearing immediately usable`() {
        val state = QiblaStateReducer.bearingResolved(
            QiblaStateReducer.refreshStarted(QiblaUiState()),
            169f
        )

        assertEquals(169f, state.qiblaAngle)
        assertFalse(state.isLoading)
        assertTrue(state.isRefreshingLocation)
    }

    @Test
    fun `fresh location updates bearing without clearing cached bearing first`() {
        val cached = QiblaStateReducer.bearingResolved(QiblaUiState(), 169f)
        val refreshing = QiblaStateReducer.refreshStarted(cached)
        assertEquals(169f, refreshing.qiblaAngle)
        assertFalse(refreshing.isLoading)

        val fresh = QiblaStateReducer.bearingResolved(refreshing, 170f)
        assertEquals(170f, fresh.qiblaAngle)
        assertFalse(fresh.isLoading)
    }

    @Test
    fun `no location produces a location-specific non-network error`() {
        val state = QiblaStateReducer.locationUnavailable(QiblaUiState())

        assertNull(state.qiblaAngle)
        assertFalse(state.isLoading)
        assertTrue(state.error.orEmpty().contains("الموقع"))
        assertFalse(state.error.orEmpty().contains("الإنترنت"))
        assertFalse(state.error.orEmpty().contains("الشبكة"))
    }

    @Test
    fun `permission denial is explicit`() {
        val state = QiblaStateReducer.permissionDenied(QiblaUiState())

        assertFalse(state.hasLocationPermission)
        assertFalse(state.isLoading)
        assertTrue(state.error.orEmpty().contains("صلاحية الموقع"))
    }

    @Test
    fun `geocoder result changes labels only`() {
        val before = QiblaStateReducer.bearingResolved(
            QiblaUiState(compassRotation = 42f, isAligned = true),
            170f
        )
        val after = QiblaStateReducer.labelsResolved(before, "الجميلية" to "حلب، سوريا")

        assertEquals(before.qiblaAngle, after.qiblaAngle)
        assertEquals(before.compassRotation, after.compassRotation)
        assertEquals(before.isAligned, after.isAligned)
        assertEquals("الجميلية", after.locationName)
    }

    @Test
    fun `production source publishes bearing before launching geocoding`() {
        val source = productionSource("QiblaViewModel.kt")
        val method = source.substringAfter("private fun publishBearing")
            .substringBefore("private fun publishLocationUnavailable")

        assertTrue(method.indexOf("QiblaStateReducer.bearingResolved") >= 0)
        assertTrue(method.indexOf("QiblaStateReducer.bearingResolved") < method.indexOf("geocodingJob ="))
    }

    @Test
    fun `bearing calculation remains unchanged for Aleppo`() {
        assertEquals(169.3f, QiblaManager.calculateQiblaDirection(36.2021, 37.1343), 1.0f)
    }

    @Test
    fun `stored manual bearing clears loading without needing network`() {
        val state = QiblaStateReducer.storedManualBearing(
            QiblaUiState(), 169.3f, "حلب", QiblaLocationSource.Saved
        )

        assertEquals(169.3f, state.qiblaAngle)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshingLocation)
        assertNull(state.error)
        assertEquals("حلب", state.locationName)
        assertEquals(QiblaLocationSource.Saved, state.locationSource)
    }

    @Test
    fun `stored manual bearing preserves a previously empty label`() {
        val state = QiblaStateReducer.storedManualBearing(
            QiblaUiState(), 169.3f, "", QiblaLocationSource.Manual
        )

        // Empty source label must not overwrite existing locationName.
        assertEquals("", state.locationName)
        assertEquals(QiblaLocationSource.Manual, state.locationSource)
    }

    private fun productionSource(fileName: String): String {
        val repoRoot = File(".").canonicalFile.let { dir ->
            generateSequence(dir) { it.parentFile }
                .firstOrNull { File(it, "settings.gradle.kts").exists() }
                ?: dir
        }
        return File(
            repoRoot,
            "feature/src/main/java/com/example/feature/qibla/presentation/$fileName"
        ).readText()
    }
}
