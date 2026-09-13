package com.example.feature.prayer

import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.dashboard.domain.usecase.ObserveHomePrayerSummaryUseCase
import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import com.example.feature.prayer.presentation.PrayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Phase 3 — automated Home / Prayer parity.
 *
 * Both surfaces are driven from the SAME [PrayerTimesFacade] instance. The test proves
 * there is no second prayer calculator: for identical location + settings + clock, the
 * Home dashboard summary and the Prayer screen agree on the next prayer, on the ordered
 * list of prayer instants, and on the location availability state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomePrayerParityTest {

    /** Minimal in-memory facade — the single canonical read model for both surfaces. */
    private class FakePrayerTimesFacade(
        day: PrayerDay?,
        next: NextPrayer?,
        location: PrayerLocationState
    ) : PrayerTimesFacade {
        private val _prayerDay = MutableStateFlow(day)
        private val _nextPrayer = MutableStateFlow(next)
        private val _locationState = MutableStateFlow(location)
        private val _systemStatus = MutableStateFlow(
            PrayerSystemStatus(
                locationState = location,
                alarmPermission = PrayerAlarmPermissionState.GrantedExact,
                lastReconciliationEpochMillis = null,
                lastReconciliationSuccess = null,
                lastFailureSummary = null,
                scheduledAlarmCount = 0
            )
        )

        override val prayerDay: StateFlow<PrayerDay?> = _prayerDay.asStateFlow()
        override val nextPrayer: StateFlow<NextPrayer?> = _nextPrayer.asStateFlow()
        override val locationState: StateFlow<PrayerLocationState> = _locationState.asStateFlow()
        override val systemStatus: StateFlow<PrayerSystemStatus> = _systemStatus.asStateFlow()

        override suspend fun refreshLocation(): Result<Unit> = Result.success(Unit)
        override suspend fun updateSettings(settings: PrayerCalculationSettings) = Unit
        override suspend fun reconcileSchedule(reason: PrayerReconciliationReason) = Unit
    }

    /** In-memory settings so the ViewModel can be built without Android/DataStore. */
    private class FakeSettingsRepository : PrayerSettingsRepository {
        private val settings = MutableStateFlow(PrayerCalculationSettings())
        override fun observeSettings() = settings
        override suspend fun updateCalculationMethod(
            method: com.example.feature.prayer.domain.model.PrayerCalculationMethod
        ) = Unit
        override suspend fun updateMadhhab(
            madhhab: com.example.feature.prayer.domain.model.PrayerMadhhab
        ) = Unit
        override suspend fun updateOffsets(
            offsets: com.example.feature.prayer.domain.model.PrayerOffsets
        ) = Unit
        override suspend fun updateUseAutoLocation(enabled: Boolean) = Unit
        override suspend fun updatePrePrayerMinutes(minutes: Int) = Unit
        override suspend fun updateIqamahMinutes(minutes: Int) = Unit
        override suspend fun updateAlertMode(
            prayer: com.example.feature.prayer.domain.model.PrayerName,
            mode: com.example.feature.prayer.domain.model.PrayerAlertMode
        ) {
            settings.value = settings.value.copy(
                alertModes = settings.value.alertModes.with(prayer, mode)
            )
        }
        override suspend fun updateNotificationSoundType(type: String) = Unit
    }

    private val calculator = AdhanPrayerCalculator()
    private val cairo = PrayerLocation(30.0444, 31.2357, "Cairo")
    private val settings = PrayerCalculationSettings()
    private val zone = "Africa/Cairo"

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun facadeFor(date: LocalDate, now: Long): FakePrayerTimesFacade {
        val today = calculator.calculate(date, cairo, settings, zone, now)
        val tomorrow = calculator.calculate(date.plusDays(1), cairo, settings, zone, now)
        return FakePrayerTimesFacade(
            day = today,
            next = NextPrayerSelector.select(today, tomorrow, now),
            location = PrayerLocationState.Available(cairo, PrayerLocationSource.Manual)
        )
    }

    private fun anchorNow(date: LocalDate): Long =
        java.time.ZonedDateTime.of(date, java.time.LocalTime.of(10, 0), java.time.ZoneId.of(zone))
            .toInstant().toEpochMilli()

    @Test
    fun `home and prayer screen agree on the next prayer name`() = runTest {
        val date = LocalDate.of(2024, 6, 15)
        val facade = facadeFor(date, anchorNow(date))

        val home = ObserveHomePrayerSummaryUseCase(facade).invoke().first()
        val vm = PrayerViewModel(facade, FakeSettingsRepository())
        advanceUntilIdle()
        val prayer = vm.uiState.value

        assertTrue(home is HomeSectionState.Content)
        val summary = (home as HomeSectionState.Content).value

        assertEquals(facade.nextPrayer.value!!.name.arabic, summary.nextPrayerNameAr)
        assertEquals(summary.nextPrayerNameAr, prayer.nextPrayerName)
    }

    @Test
    fun `home and prayer screen agree on the ordered prayer instants`() = runTest {
        val date = LocalDate.of(2024, 6, 15)
        val facade = facadeFor(date, anchorNow(date))

        val home = ObserveHomePrayerSummaryUseCase(facade).invoke().first()
        val vm = PrayerViewModel(facade, FakeSettingsRepository())
        advanceUntilIdle()
        val prayer = vm.uiState.value

        val summary = (home as HomeSectionState.Content).value
        assertEquals(
            summary.prayers.map { it.timestamp },
            prayer.prayerTimes.map { it.timestamp }
        )
        assertEquals(
            summary.prayers.map { it.nameAr },
            prayer.prayerTimes.map { it.nameAr }
        )
        assertEquals(
            facade.prayerDay.value!!.instants.map { it.epochMillis },
            summary.prayers.map { it.timestamp }
        )
    }

    @Test
    fun `parity holds after isha when the next prayer rolls to tomorrow`() = runTest {
        val date = LocalDate.of(2024, 6, 15)
        val today = calculator.calculate(date, cairo, settings, zone, 0L)
        val afterIsha =
            today.instants.last().epochMillis + 60_000L
        val facade = facadeFor(date, afterIsha)

        val home = ObserveHomePrayerSummaryUseCase(facade).invoke().first()
        val vm = PrayerViewModel(facade, FakeSettingsRepository())
        advanceUntilIdle()

        val summary = (home as HomeSectionState.Content).value
        assertEquals("الفجر", summary.nextPrayerNameAr)
        assertEquals(summary.nextPrayerNameAr, vm.uiState.value.nextPrayerName)
    }

    @Test
    fun `both surfaces report location unavailable from the same state`() = runTest {
        val facade = FakePrayerTimesFacade(
            day = null,
            next = null,
            location = PrayerLocationState.Unavailable(
                com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason.PERMISSION_DENIED
            )
        )

        val home = ObserveHomePrayerSummaryUseCase(facade).invoke().first()
        val vm = PrayerViewModel(facade, FakeSettingsRepository())
        advanceUntilIdle()

        // Home degrades the whole section to Error; Prayer surfaces the unavailable flag.
        assertTrue(home is HomeSectionState.Error)
        assertTrue(vm.uiState.value.locationUnavailable)
        assertTrue(vm.uiState.value.prayerTimes.isEmpty())
    }

    @Test
    fun `neither surface constructs its own calculator`() {
        // Structural guard: the parity above is only meaningful because both consumers
        // take the facade as their single dependency. If a second calculator is ever
        // introduced into either presentation layer, this assertion fails.
        val homeSource = ObserveHomePrayerSummaryUseCase::class.java.declaredFields
            .map { it.type.name }
        assertTrue(
            "Home summary must depend on PrayerTimesFacade only",
            homeSource.any { it == PrayerTimesFacade::class.java.name }
        )
        assertTrue(
            "Home summary must not hold a PrayerCalculator",
            homeSource.none { it.endsWith("PrayerCalculator") }
        )

        val prayerSource = PrayerViewModel::class.java.declaredFields.map { it.type.name }
        assertTrue(
            "PrayerViewModel must depend on PrayerTimesFacade",
            prayerSource.any { it == PrayerTimesFacade::class.java.name }
        )
        assertTrue(
            "PrayerViewModel must not hold a PrayerCalculator",
            prayerSource.none { it.endsWith("PrayerCalculator") }
        )
    }
}
