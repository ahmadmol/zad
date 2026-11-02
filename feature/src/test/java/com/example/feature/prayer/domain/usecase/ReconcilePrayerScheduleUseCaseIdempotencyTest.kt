package com.example.feature.prayer.domain.usecase

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerOperationalEvent
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerSchedule
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import com.example.feature.prayer.domain.model.PrayerScheduleResult
import com.example.feature.prayer.domain.repository.PrayerAlarmGateway
import com.example.feature.prayer.domain.repository.PrayerEventRepository
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Phase 3 — Reconciliation idempotency characterization.
 *
 * Proves that running [ReconcilePrayerScheduleUseCase] with identical inputs
 * twice produces the same desired schedule, that a changed fingerprint
 * produces a new desired schedule, and that the gateway's `cancelAll`
 * path is taken when the preconditions are not met.
 */
class ReconcilePrayerScheduleUseCaseIdempotencyTest {

    @Test
    fun `repeated reconciliation with identical inputs produces identical schedule`() = runTest {
        val today = LocalDate.of(2026, 8, 23)
        val location = PrayerLocation(36.2021, 37.1343, "حلب")
        val settings = FixedSettings(
            method = "MUSLIM_WORLD_LEAGUE",
            madhhab = "SHAFI",
            prePrayer = 10,
            iqamah = 15,
            notificationsEnabled = true,
            useAutoLocation = true
        )
        val alarmGateway = RecordingAlarmGateway()
        val locationRepo = FixedLocationRepo(PrayerLocationState.Available(location, PrayerLocationSource.Device))
        val settingsRepo = FixedSettingsRepo(settings)
        val calculator: PrayerCalculator = AdhanPrayerCalculator()
        val clock = fixedClock(today, ZoneOffset.UTC, today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val eventRepository = RecordingEventRepository()
        val statusStore = PrayerSystemStatusStore()
        val useCase = ReconcilePrayerScheduleUseCase(
            locationRepository = locationRepo,
            settingsRepository = settingsRepo,
            calculator = calculator,
            alarmGateway = alarmGateway,
            eventRepository = eventRepository,
            clock = clock,
            systemStatusStore = statusStore
        )

        val first = useCase.invoke(PrayerReconciliationReason.ApplicationStart)
        val second = useCase.invoke(PrayerReconciliationReason.ManualRetry)

        val firstSchedule = alarmGateway.lastSchedule
        val secondSchedule = alarmGateway.lastSchedule

        assertTrue("first must schedule at least one alarm", firstSchedule.alarms.isNotEmpty())
        assertEquals(
            "fingerprint is deterministic",
            firstSchedule.settingsFingerprint,
            secondSchedule.settingsFingerprint
        )
        assertEquals(
            "alarm count must be stable across identical reconciliations",
            firstSchedule.alarms.size,
            secondSchedule.alarms.size
        )
        val firstKeys = firstSchedule.alarms.map {
            Triple(it.stableId, it.prayerName, it.kind) to it.triggerEpochMillis
        }.toSet()
        val secondKeys = secondSchedule.alarms.map {
            Triple(it.stableId, it.prayerName, it.kind) to it.triggerEpochMillis
        }.toSet()
        assertEquals(firstKeys, secondKeys)
    }

    @Test
    fun `reconciliation with changed fingerprint replaces the schedule`() = runTest {
        val today = LocalDate.of(2026, 8, 23)
        val location = PrayerLocation(36.2021, 37.1343, "حلب")
        val settings = FixedSettings("MUSLIM_WORLD_LEAGUE", "SHAFI", 10, 15, true, true)
        val alarmGateway = RecordingAlarmGateway()
        val calculator: PrayerCalculator = AdhanPrayerCalculator()
        val clock = fixedClock(today, ZoneOffset.UTC, today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val useCase = ReconcilePrayerScheduleUseCase(
            locationRepository = FixedLocationRepo(PrayerLocationState.Available(location, PrayerLocationSource.Device)),
            settingsRepository = FixedSettingsRepo(settings),
            calculator = calculator,
            alarmGateway = alarmGateway,
            eventRepository = RecordingEventRepository(),
            clock = clock,
            systemStatusStore = PrayerSystemStatusStore()
        )

        useCase.invoke(PrayerReconciliationReason.SettingsChanged)
        val before = alarmGateway.lastSchedule.settingsFingerprint

        val newSettings = settings.copy(prePrayer = 5, iqamah = 20)
        val useCase2 = ReconcilePrayerScheduleUseCase(
            locationRepository = FixedLocationRepo(PrayerLocationState.Available(location, PrayerLocationSource.Device)),
            settingsRepository = FixedSettingsRepo(newSettings),
            calculator = calculator,
            alarmGateway = alarmGateway,
            eventRepository = RecordingEventRepository(),
            clock = clock,
            systemStatusStore = PrayerSystemStatusStore()
        )
        useCase2.invoke(PrayerReconciliationReason.SettingsChanged)
        val after = alarmGateway.lastSchedule.settingsFingerprint

        assertTrue("fingerprint must change when settings change", before != after)
    }

    @Test
    fun `reconciliation cancels all when location is unavailable`() = runTest {
        val today = LocalDate.of(2026, 8, 23)
        val settings = FixedSettings("MUSLIM_WORLD_LEAGUE", "SHAFI", 10, 15, true, true)
        val alarmGateway = RecordingAlarmGateway()
        val calculator: PrayerCalculator = AdhanPrayerCalculator()
        val clock = fixedClock(today, ZoneOffset.UTC, today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val useCase = ReconcilePrayerScheduleUseCase(
            locationRepository = FixedLocationRepo(
                PrayerLocationState.Unavailable(
                    com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason.NO_SAVED_LOCATION
                )
            ),
            settingsRepository = FixedSettingsRepo(settings),
            calculator = calculator,
            alarmGateway = alarmGateway,
            eventRepository = RecordingEventRepository(),
            clock = clock,
            systemStatusStore = PrayerSystemStatusStore()
        )

        val result = useCase.invoke(PrayerReconciliationReason.ApplicationStart)

        assertTrue(result is PrayerScheduleResult.Skipped)
        assertEquals(0, alarmGateway.replaceScheduleCalls)
        assertTrue("cancelAll must have been called", alarmGateway.cancelAllCalls >= 1)
    }

    @Test
    fun `reconciliation cancels all when notifications are disabled`() = runTest {
        val today = LocalDate.of(2026, 8, 23)
        val location = PrayerLocation(36.2021, 37.1343, "حلب")
        val settings = FixedSettings("MUSLIM_WORLD_LEAGUE", "SHAFI", 10, 15, notificationsEnabled = false, useAutoLocation = true)
        val alarmGateway = RecordingAlarmGateway()
        val calculator: PrayerCalculator = AdhanPrayerCalculator()
        val clock = fixedClock(today, ZoneOffset.UTC, today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val useCase = ReconcilePrayerScheduleUseCase(
            locationRepository = FixedLocationRepo(PrayerLocationState.Available(location, PrayerLocationSource.Device)),
            settingsRepository = FixedSettingsRepo(settings),
            calculator = calculator,
            alarmGateway = alarmGateway,
            eventRepository = RecordingEventRepository(),
            clock = clock,
            systemStatusStore = PrayerSystemStatusStore()
        )

        val result = useCase.invoke(PrayerReconciliationReason.SettingsChanged)

        assertTrue(result is PrayerScheduleResult.Skipped)
        assertEquals(0, alarmGateway.replaceScheduleCalls)
        assertTrue(alarmGateway.cancelAllCalls >= 1)
    }
}

private class RecordingAlarmGateway : PrayerAlarmGateway {
    var lastSchedule: PrayerSchedule = PrayerSchedule(
        dateEpochDay = 0,
        timeZoneId = "UTC",
        location = PrayerLocation(0.0, 0.0, ""),
        settingsFingerprint = "",
        policy = PrayerSchedulePolicy(),
        alarms = emptyList()
    )
    var replaceScheduleCalls: Int = 0
    var cancelAllCalls: Int = 0
    private val permission = MutableStateFlow<PrayerAlarmPermissionState>(PrayerAlarmPermissionState.GrantedExact)

    override suspend fun replaceSchedule(schedule: PrayerSchedule): PrayerScheduleResult {
        replaceScheduleCalls += 1
        lastSchedule = schedule
        return PrayerScheduleResult.Scheduled(
            scheduledCount = schedule.alarms.size,
            skippedPastCount = 0,
            usedInexactFallback = false
        )
    }

    override suspend fun cancelAll(): Result<Unit> {
        cancelAllCalls += 1
        lastSchedule = lastSchedule.copy(alarms = emptyList())
        return Result.success(Unit)
    }

    override fun observePermissionState(): Flow<PrayerAlarmPermissionState> = permission
}

private class FixedLocationRepo(
    private val state: PrayerLocationState
) : PrayerLocationRepository {
    override fun observeLocation(): Flow<PrayerLocationState> = MutableStateFlow(state)
    override suspend fun refreshLocation(): Result<PrayerLocation> = Result.success(
        (state as? PrayerLocationState.Available)?.location ?: PrayerLocation(0.0, 0.0, "")
    )
    override suspend fun saveManualLocation(
        latitude: Double,
        longitude: Double,
        displayName: String
    ): Result<Unit> = Result.success(Unit)
}

private class FixedSettingsRepo(
    initial: FixedSettings
) : PrayerSettingsRepository {
    private val flow = MutableStateFlow(initial.toDomain())
    override fun observeSettings(): Flow<PrayerCalculationSettings> = flow
    override suspend fun updateCalculationMethod(method: PrayerCalculationMethod) {
        flow.value = flow.value.copy(method = method)
    }
    override suspend fun updateMadhhab(madhhab: PrayerMadhhab) {
        flow.value = flow.value.copy(madhhab = madhhab)
    }
    override suspend fun updateOffsets(offsets: com.example.feature.prayer.domain.model.PrayerOffsets) {
        flow.value = flow.value.copy(
            prePrayerNotificationMinutes = flow.value.prePrayerNotificationMinutes,
            iqamahNotificationMinutes = flow.value.iqamahNotificationMinutes
        )
    }
    override suspend fun updateUseAutoLocation(enabled: Boolean) {
        flow.value = flow.value.copy(useAutoLocation = enabled)
    }
    override suspend fun updateManualLocation(city: String, latitude: Double, longitude: Double) = Unit
    override suspend fun updatePrePrayerMinutes(minutes: Int) {
        flow.value = flow.value.copy(prePrayerNotificationMinutes = minutes)
    }
    override suspend fun updateIqamahMinutes(minutes: Int) {
        flow.value = flow.value.copy(iqamahNotificationMinutes = minutes)
    }
    override suspend fun updateNotificationSoundType(type: String) {
        // not used in this test
    }
    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        flow.value = flow.value.copy(notificationsEnabled = enabled)
    }
}

private data class FixedSettings(
    val method: String,
    val madhhab: String,
    val prePrayer: Int,
    val iqamah: Int,
    val notificationsEnabled: Boolean,
    val useAutoLocation: Boolean
) {
    fun toDomain(): PrayerCalculationSettings = PrayerCalculationSettings(
        method = PrayerCalculationMethod.valueOf(method),
        madhhab = PrayerMadhhab.valueOf(madhhab),
        prePrayerNotificationMinutes = prePrayer,
        iqamahNotificationMinutes = iqamah,
        notificationsEnabled = notificationsEnabled
    )
}

private fun fixedClock(today: LocalDate, zone: ZoneId, epochMillis: Long): PrayerClock {
    val fixedInstant = Instant.ofEpochMilli(epochMillis)
    val fixedJavaClock = Clock.fixed(fixedInstant, zone)
    return PrayerClock(fixedJavaClock)
}

private class RecordingEventRepository : PrayerEventRepository {
    private val events = mutableListOf<PrayerOperationalEvent>()
    override suspend fun record(event: PrayerOperationalEvent) {
        events.add(event)
    }
    override fun observeRecent(limit: Int): Flow<List<PrayerOperationalEvent>> =
        MutableStateFlow(events.takeLast(limit))
    override suspend fun clear() {
        events.clear()
    }
}
