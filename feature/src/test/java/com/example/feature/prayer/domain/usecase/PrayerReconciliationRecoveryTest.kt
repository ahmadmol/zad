package com.example.feature.prayer.domain.usecase

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import java.time.*

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerReconciliationRecoveryTest {
    private val location=MutableStateFlow<PrayerLocationState>(PrayerLocationState.Available(
        PrayerLocation(36.2,37.1,"fixture city"),PrayerLocationSource.Manual))
    private val settings=MutableStateFlow(PrayerCalculationSettings())
    private val permission=MutableStateFlow<PrayerAlarmPermissionState>(PrayerAlarmPermissionState.GrantedExact)
    private val gateway=mockk<PrayerAlarmGateway>()
    private val status=PrayerSystemStatusStore()
    private fun useCase():ReconcilePrayerScheduleUseCase {
        val locations=mockk<PrayerLocationRepository>()
        val preferences=mockk<PrayerSettingsRepository>()
        every {locations.observeLocation()} returns location
        every {preferences.observeSettings()} returns settings
        every {gateway.observePermissionState()} returns permission
        return ReconcilePrayerScheduleUseCase(locations,preferences,AdhanPrayerCalculator(),gateway,
            mockk<PrayerEventRepository>(relaxUnitFun=true),
            PrayerClock(Clock.fixed(Instant.parse("2026-11-02T00:00:00Z"),ZoneId.of("Asia/Damascus"))),status)
    }
    @Test fun `failed cancellation cannot report skipped success in any cancellation branch`()=runTest {
        coEvery {gateway.cancelAll()} returns Result.failure(java.io.IOException("fixture"))
        val useCase=useCase()
        settings.value=settings.value.copy(notificationsEnabled=false)
        assertTrue(useCase(PrayerReconciliationReason.SettingsChanged) is PrayerScheduleResult.Failed)
        assertEquals(false,status.current().lastReconciliationSuccess)
        settings.value=settings.value.copy(notificationsEnabled=true)
        permission.value=PrayerAlarmPermissionState.NotificationsDenied
        assertTrue(useCase(PrayerReconciliationReason.NotificationPermissionChanged) is PrayerScheduleResult.Failed)
        permission.value=PrayerAlarmPermissionState.GrantedExact
        location.value=PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION)
        assertTrue(useCase(PrayerReconciliationReason.LocationChanged) is PrayerScheduleResult.Failed)
        coVerify(exactly=0) {gateway.replaceSchedule(any())}
        coEvery {gateway.cancelAll()} returns Result.success(Unit)
        assertTrue(useCase(PrayerReconciliationReason.ManualRetry) is PrayerScheduleResult.Skipped)
    }
    @Test fun `concurrent reconciliation cannot install an old snapshot after a newer one`()=runTest {
        val release=CompletableDeferred<Unit>();val installed=mutableListOf<PrayerSchedule>();var calls=0
        coEvery {gateway.replaceSchedule(any())} coAnswers {
            calls++
            val schedule=firstArg<PrayerSchedule>()
            if(calls==1) release.await()
            installed+=schedule
            PrayerScheduleResult.Scheduled(schedule.alarms.size,0,false)
        }
        val useCase=useCase()
        val first=launch {useCase(PrayerReconciliationReason.ApplicationStart)};runCurrent()
        settings.value=settings.value.copy(prePrayerNotificationMinutes=5)
        val second=launch {useCase(PrayerReconciliationReason.SettingsChanged)};runCurrent()
        assertEquals(1,calls)
        release.complete(Unit);joinAll(first,second)
        assertEquals(2,installed.size)
        assertEquals(5,installed.last().policy.prePrayerMinutes)
        assertNotEquals(installed.first().settingsFingerprint,installed.last().settingsFingerprint)
        assertEquals(true,status.current().lastReconciliationSuccess)
    }
    @Test fun `cancelled reconciliation releases serialization without publishing a false error`()=runTest {
        coEvery {gateway.replaceSchedule(any())} coAnswers {awaitCancellation()}
        val useCase=useCase();val first=launch {useCase(PrayerReconciliationReason.ApplicationStart)}
        runCurrent();first.cancelAndJoin()
        assertNull(status.current().lastReconciliationSuccess)
        coEvery {gateway.replaceSchedule(any())} returns PrayerScheduleResult.Scheduled(1,0,false)
        assertTrue(useCase(PrayerReconciliationReason.ManualRetry) is PrayerScheduleResult.Scheduled)
    }
}
