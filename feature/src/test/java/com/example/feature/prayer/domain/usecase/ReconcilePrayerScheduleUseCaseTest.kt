package com.example.feature.prayer.domain.usecase

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerOperationalEvent
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerSchedule
import com.example.feature.prayer.domain.model.PrayerScheduleResult
import com.example.feature.prayer.domain.repository.PrayerAlarmGateway
import com.example.feature.prayer.domain.repository.PrayerEventRepository
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ReconcilePrayerScheduleUseCaseTest {

    private val zone = ZoneId.of("Asia/Riyadh")
    private val fixedInstant = Instant.parse("2024-06-15T08:00:00Z")
    private val clock = PrayerClock(Clock.fixed(fixedInstant, zone))

    @Test
    fun `idempotent reconcile does not duplicate alarms`() = runTest {
        val gateway = FakeAlarmGateway()
        val useCase = buildUseCase(gateway)

        val first = useCase(PrayerReconciliationReason.ApplicationStart)
        val second = useCase(PrayerReconciliationReason.ApplicationStart)

        assertTrue(first is PrayerScheduleResult.Scheduled)
        assertTrue(second is PrayerScheduleResult.Scheduled)
        assertEquals(1, gateway.replaceCalls)
        assertEquals(gateway.lastScheduledCount, (first as PrayerScheduleResult.Scheduled).scheduledCount)
    }

    @Test
    fun `location change replaces schedule`() = runTest {
        val gateway = FakeAlarmGateway()
        val locationFlow = MutableStateFlow<PrayerLocationState>(
            PrayerLocationState.Available(
                PrayerLocation(21.4225, 39.8262, "Mecca"),
                PrayerLocationSource.Manual
            )
        )
        val useCase = buildUseCase(gateway, locationFlow)

        useCase(PrayerReconciliationReason.ApplicationStart)
        locationFlow.value = PrayerLocationState.Available(
            PrayerLocation(30.0444, 31.2357, "Cairo"),
            PrayerLocationSource.Manual
        )
        useCase(PrayerReconciliationReason.LocationChanged)

        assertEquals(2, gateway.replaceCalls)
    }

    @Test
    fun `unavailable location skips scheduling`() = runTest {
        val gateway = FakeAlarmGateway()
        val useCase = buildUseCase(
            gateway,
            MutableStateFlow(PrayerLocationState.Unavailable(
                com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason.NO_SAVED_LOCATION
            ))
        )
        val result = useCase(PrayerReconciliationReason.ManualRetry)
        assertTrue(result is PrayerScheduleResult.Skipped)
        assertEquals(1, gateway.cancelCalls)
    }

    @Test
    fun `notifications denied skips alarms`() = runTest {
        val gateway = FakeAlarmGateway(permission = PrayerAlarmPermissionState.NotificationsDenied)
        val useCase = buildUseCase(gateway)
        val result = useCase(PrayerReconciliationReason.NotificationPermissionChanged)
        assertTrue(result is PrayerScheduleResult.Skipped)
    }

    private fun buildUseCase(
        gateway: FakeAlarmGateway,
        location: MutableStateFlow<PrayerLocationState> = MutableStateFlow(
            PrayerLocationState.Available(
                PrayerLocation(21.4225, 39.8262, "Mecca"),
                PrayerLocationSource.Manual
            )
        )
    ) = ReconcilePrayerScheduleUseCase(
        locationRepository = object : PrayerLocationRepository {
            override fun observeLocation() = location
            override suspend fun refreshLocation() = Result.success(
                (location.value as PrayerLocationState.Available).location
            )
            override suspend fun saveManualLocation(latitude: Double, longitude: Double, displayName: String) =
                Result.success(Unit)
        },
        settingsRepository = object : PrayerSettingsRepository {
            override fun observeSettings() = flowOf(PrayerCalculationSettings())
            override suspend fun updateCalculationMethod(method: com.example.feature.prayer.domain.model.PrayerCalculationMethod) {}
            override suspend fun updateMadhhab(madhhab: com.example.feature.prayer.domain.model.PrayerMadhhab) {}
            override suspend fun updateOffsets(offsets: com.example.feature.prayer.domain.model.PrayerOffsets) {}
            override suspend fun updateUseAutoLocation(enabled: Boolean) {}
            override suspend fun updatePrePrayerMinutes(minutes: Int) {}
            override suspend fun updateIqamahMinutes(minutes: Int) {}
        },
        calculator = AdhanPrayerCalculator(),
        alarmGateway = gateway,
        eventRepository = object : PrayerEventRepository {
            override suspend fun record(event: PrayerOperationalEvent) {}
            override fun observeRecent(limit: Int) = flowOf(emptyList<PrayerOperationalEvent>())
            override suspend fun clear() {}
        },
        clock = clock,
        systemStatusStore = PrayerSystemStatusStore()
    )

    private class FakeAlarmGateway(
        permission: PrayerAlarmPermissionState = PrayerAlarmPermissionState.GrantedExact
    ) : PrayerAlarmGateway {
        var replaceCalls = 0
        var cancelCalls = 0
        var lastScheduledCount = 0
        private var lastFingerprint: String? = null

        private val permissionFlow = MutableStateFlow(permission)

        override suspend fun replaceSchedule(schedule: PrayerSchedule): PrayerScheduleResult {
            val fp = schedule.settingsFingerprint + "|" + schedule.dateEpochDay + "|" + schedule.alarms.size
            if (fp == lastFingerprint) {
                return PrayerScheduleResult.Scheduled(lastScheduledCount, 0, false)
            }
            replaceCalls++
            lastFingerprint = fp
            lastScheduledCount = schedule.alarms.size
            return PrayerScheduleResult.Scheduled(schedule.alarms.size, 0, false)
        }

        override suspend fun cancelAll(): Result<Unit> {
            cancelCalls++
            lastFingerprint = null
            return Result.success(Unit)
        }

        override fun observePermissionState(): Flow<PrayerAlarmPermissionState> = permissionFlow
    }
}
