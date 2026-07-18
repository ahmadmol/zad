package com.example.feature.prayer.domain.facade

import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.domain.usecase.ReconcilePrayerScheduleUseCase
import com.example.feature.prayer.domain.usecase.RefreshPrayerLocationUseCase
import com.example.feature.prayer.domain.usecase.UpdatePrayerSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

interface PrayerTimesFacade {
    val prayerDay: StateFlow<PrayerDay?>
    val nextPrayer: StateFlow<NextPrayer?>
    val locationState: StateFlow<PrayerLocationState>
    val systemStatus: StateFlow<PrayerSystemStatus>

    suspend fun refreshLocation()
    suspend fun updateSettings(settings: PrayerCalculationSettings)
    suspend fun reconcileSchedule(reason: PrayerReconciliationReason)
}

class DefaultPrayerTimesFacade(
    private val locationRepository: PrayerLocationRepository,
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerCalculator,
    private val clock: PrayerClock,
    private val refreshLocationUseCase: RefreshPrayerLocationUseCase,
    private val updateSettingsUseCase: UpdatePrayerSettingsUseCase,
    private val reconcileUseCase: ReconcilePrayerScheduleUseCase,
    private val systemStatusFlow: kotlinx.coroutines.flow.Flow<PrayerSystemStatus>,
    externalScope: CoroutineScope? = null
) : PrayerTimesFacade {

    private val scope = externalScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _prayerDay = MutableStateFlow<PrayerDay?>(null)
    override val prayerDay: StateFlow<PrayerDay?> = _prayerDay.asStateFlow()

    private val _nextPrayer = MutableStateFlow<NextPrayer?>(null)
    override val nextPrayer: StateFlow<NextPrayer?> = _nextPrayer.asStateFlow()

    private val _locationState = MutableStateFlow<PrayerLocationState>(PrayerLocationState.Loading)
    override val locationState: StateFlow<PrayerLocationState> = _locationState.asStateFlow()

    private val _systemStatus = MutableStateFlow(
        PrayerSystemStatus(
            locationState = PrayerLocationState.Loading,
            alarmPermission = com.example.feature.prayer.domain.model.PrayerAlarmPermissionState.Unknown,
            lastReconciliationEpochMillis = null,
            lastReconciliationSuccess = null,
            lastFailureSummary = null,
            scheduledAlarmCount = 0
        )
    )
    override val systemStatus: StateFlow<PrayerSystemStatus> = _systemStatus.asStateFlow()

    private var lastDate: LocalDate? = null
    private var tickerJob: Job? = null

    init {
        scope.launch {
            combine(
                locationRepository.observeLocation(),
                settingsRepository.observeSettings()
            ) { loc, settings -> loc to settings }
                .collectLatest { (loc, settings) ->
                    _locationState.value = loc
                    recalculate(loc, settings)
                    // Idempotent: replaces schedule when location/settings fingerprint changes.
                    reconcileUseCase(PrayerReconciliationReason.SettingsChanged)
                }
        }
        scope.launch {
            systemStatusFlow.collect { _systemStatus.value = it }
        }
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val today = clock.today()
                if (lastDate != null && lastDate != today) {
                    val loc = _locationState.value
                    val settings = settingsRepository.observeSettings().first()
                    recalculate(loc, settings)
                    reconcileSchedule(PrayerReconciliationReason.TimeChanged)
                }
                lastDate = today
                val next = _nextPrayer.value
                if (next != null) {
                    val now = clock.nowEpochMillis()
                    _nextPrayer.value = next.copy(
                        remainingMillis = (next.epochMillis - now).coerceAtLeast(0L)
                    )
                    if (now >= next.epochMillis) {
                        val loc = _locationState.value
                        val settings = settingsRepository.observeSettings().first()
                        recalculate(loc, settings)
                    }
                }
                delay(1_000)
            }
        }
    }

    private suspend fun recalculate(loc: PrayerLocationState, settings: PrayerCalculationSettings) {
        withContext(Dispatchers.Default) {
            when (loc) {
                is PrayerLocationState.Available -> {
                    val zone = clock.zoneId().id
                    val now = clock.nowEpochMillis()
                    val today = calculator.calculate(clock.today(), loc.location, settings, zone, now)
                    val tomorrow = calculator.calculate(
                        clock.today().plusDays(1), loc.location, settings, zone, now
                    )
                    _prayerDay.value = today
                    _nextPrayer.value = NextPrayerSelector.select(today, tomorrow, now)
                }
                else -> {
                    _prayerDay.value = null
                    _nextPrayer.value = null
                }
            }
        }
    }

    override suspend fun refreshLocation() {
        refreshLocationUseCase()
        reconcileSchedule(PrayerReconciliationReason.LocationChanged)
    }

    override suspend fun updateSettings(settings: PrayerCalculationSettings) {
        updateSettingsUseCase.updateMethod(settings.method)
        updateSettingsUseCase.updateMadhhab(settings.madhhab)
        updateSettingsUseCase.updatePrePrayer(settings.prePrayerNotificationMinutes)
        updateSettingsUseCase.updateIqamah(settings.iqamahNotificationMinutes)
        updateSettingsUseCase.updateUseAutoLocation(settings.useAutoLocation)
        reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
    }

    override suspend fun reconcileSchedule(reason: PrayerReconciliationReason) {
        reconcileUseCase(reason)
    }
}
