package com.example.feature.prayer.domain.usecase

import com.example.feature.prayer.domain.scheduler.PrayerScheduleBuilder
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerOperationalEvent
import com.example.feature.prayer.domain.model.PrayerOperationalEventType
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import com.example.feature.prayer.domain.model.PrayerScheduleResult
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import com.example.feature.prayer.domain.repository.PrayerAlarmGateway
import com.example.feature.prayer.domain.repository.PrayerEventRepository
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ReconcilePrayerScheduleUseCase(
    private val locationRepository: PrayerLocationRepository,
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerCalculator,
    private val alarmGateway: PrayerAlarmGateway,
    private val eventRepository: PrayerEventRepository,
    private val clock: PrayerClock,
    private val systemStatusStore: PrayerSystemStatusStore
) {
    suspend operator fun invoke(reason: PrayerReconciliationReason): PrayerScheduleResult {
        val now = clock.nowEpochMillis()
        eventRepository.record(
            PrayerOperationalEvent(
                type = PrayerOperationalEventType.RECONCILE_STARTED,
                reason = reason,
                summary = "reconcile_started",
                epochMillis = now
            )
        )

        val locationState = locationRepository.observeLocation().first()
        val settings = settingsRepository.observeSettings().first()
        val permission = alarmGateway.observePermissionState().first()

        if (locationState !is PrayerLocationState.Available) {
            alarmGateway.cancelAll()
            val result = PrayerScheduleResult.Skipped("location_unavailable")
            eventRepository.record(
                PrayerOperationalEvent(
                    type = PrayerOperationalEventType.LOCATION_UNAVAILABLE,
                    reason = reason,
                    summary = "location_unavailable",
                    epochMillis = clock.nowEpochMillis()
                )
            )
            systemStatusStore.update(
                PrayerSystemStatus(
                    locationState = locationState,
                    alarmPermission = permission,
                    lastReconciliationEpochMillis = clock.nowEpochMillis(),
                    lastReconciliationSuccess = false,
                    lastFailureSummary = "تعذر تحديد الموقع",
                    scheduledAlarmCount = 0
                )
            )
            return result
        }

        if (!settings.notificationsEnabled) {
            alarmGateway.cancelAll()
            val result = PrayerScheduleResult.Skipped("notifications_disabled")
            recordResult(reason, result)
            systemStatusStore.update(
                PrayerSystemStatus(
                    locationState = locationState,
                    alarmPermission = permission,
                    lastReconciliationEpochMillis = clock.nowEpochMillis(),
                    lastReconciliationSuccess = true,
                    lastFailureSummary = null,
                    scheduledAlarmCount = 0
                )
            )
            return result
        }

        if (permission is PrayerAlarmPermissionState.NotificationsDenied) {
            alarmGateway.cancelAll()
            val result = PrayerScheduleResult.Skipped("notifications_denied")
            recordResult(reason, result)
            systemStatusStore.update(
                PrayerSystemStatus(
                    locationState = locationState,
                    alarmPermission = permission,
                    lastReconciliationEpochMillis = clock.nowEpochMillis(),
                    lastReconciliationSuccess = false,
                    lastFailureSummary = "إذن الإشعارات غير متاح",
                    scheduledAlarmCount = 0
                )
            )
            return result
        }

        val zone = clock.zoneId().id
        val today = calculator.calculate(
            clock.today(), locationState.location, settings, zone, now
        )
        val tomorrow = calculator.calculate(
            clock.today().plusDays(1), locationState.location, settings, zone, now
        )
        val policy = PrayerSchedulePolicy(
            includeSunrise = true,
            prePrayerMinutes = settings.prePrayerNotificationMinutes,
            iqamahMinutes = settings.iqamahNotificationMinutes
        )
        val fingerprint = PrayerScheduleBuilder.settingsFingerprint(
            method = settings.method.name,
            madhhab = settings.madhhab.name,
            pre = settings.prePrayerNotificationMinutes,
            iqamah = settings.iqamahNotificationMinutes,
            lat = locationState.location.latitude,
            lng = locationState.location.longitude
        )
        val schedule = PrayerScheduleBuilder.build(
            today = today,
            tomorrow = tomorrow,
            location = locationState.location,
            policy = policy,
            nowEpochMillis = now,
            settingsFingerprint = fingerprint
        )

        val result = alarmGateway.replaceSchedule(schedule)
        recordResult(reason, result)

        val success = result is PrayerScheduleResult.Scheduled ||
            (result is PrayerScheduleResult.Skipped)
        val failureSummary = when (result) {
            is PrayerScheduleResult.Failed -> "تعذر جدولة بعض التنبيهات"
            is PrayerScheduleResult.Skipped -> when (result.reason) {
                "notifications_denied" -> "إذن الإشعارات غير متاح"
                else -> null
            }
            is PrayerScheduleResult.Scheduled ->
                if (result.usedInexactFallback) "التنبيهات قد تكون غير دقيقة زمنياً" else null
        }
        val count = (result as? PrayerScheduleResult.Scheduled)?.scheduledCount ?: 0

        systemStatusStore.update(
            PrayerSystemStatus(
                locationState = locationState,
                alarmPermission = permission,
                lastReconciliationEpochMillis = clock.nowEpochMillis(),
                lastReconciliationSuccess = result !is PrayerScheduleResult.Failed,
                lastFailureSummary = failureSummary,
                scheduledAlarmCount = count
            )
        )
        return result
    }

    private suspend fun recordResult(reason: PrayerReconciliationReason, result: PrayerScheduleResult) {
        val (type, summary) = when (result) {
            is PrayerScheduleResult.Scheduled ->
                PrayerOperationalEventType.SCHEDULE_SUCCESS to "scheduled:${result.scheduledCount}"
            is PrayerScheduleResult.Skipped ->
                PrayerOperationalEventType.SCHEDULE_SKIPPED to result.reason
            is PrayerScheduleResult.Failed ->
                PrayerOperationalEventType.SCHEDULE_FAILED to result.summary
        }
        eventRepository.record(
            PrayerOperationalEvent(
                type = type,
                reason = reason,
                summary = summary,
                epochMillis = clock.nowEpochMillis()
            )
        )
    }
}

class ObservePrayerSystemStatusUseCase(
    private val systemStatusStore: PrayerSystemStatusStore,
    private val locationRepository: PrayerLocationRepository,
    private val alarmGateway: PrayerAlarmGateway
) {
    operator fun invoke(): Flow<PrayerSystemStatus> = combine(
        systemStatusStore.status,
        locationRepository.observeLocation(),
        alarmGateway.observePermissionState()
    ) { stored, location, permission ->
        stored.copy(locationState = location, alarmPermission = permission)
    }
}

class PrayerSystemStatusStore {
    private val _status = MutableStateFlow(
        PrayerSystemStatus(
            locationState = PrayerLocationState.Loading,
            alarmPermission = PrayerAlarmPermissionState.Unknown,
            lastReconciliationEpochMillis = null,
            lastReconciliationSuccess = null,
            lastFailureSummary = null,
            scheduledAlarmCount = 0
        )
    )
    val status: Flow<PrayerSystemStatus> = _status

    fun update(status: PrayerSystemStatus) {
        _status.value = status
    }

    fun current(): PrayerSystemStatus = _status.value
}
