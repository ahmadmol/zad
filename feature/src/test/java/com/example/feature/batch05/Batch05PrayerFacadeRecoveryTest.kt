package com.example.feature.batch05
import com.example.feature.prayer.domain.facade.DefaultPrayerTimesFacade
import com.example.feature.prayer.domain.repository.*
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.usecase.*
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class Batch05PrayerFacadeRecoveryTest {
 @Test fun `read failure exposes unavailable without invented times or writes and manual retry reopens observation`()=runTest {
  val location=mockk<PrayerLocationRepository>();val settings=mockk<PrayerSettingsRepository>();val calculator=mockk<PrayerCalculator>()
  var failed=true
  every { location.observeLocation() } answers { if(failed) flow { throw IOException("private") } else flowOf(PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION)) }
  every { settings.observeSettings() } returns flowOf(PrayerCalculationSettings())
  val reconcile=mockk<ReconcilePrayerScheduleUseCase>();coEvery { reconcile(any()) } returns PrayerScheduleResult.Skipped("QA")
  val facade=DefaultPrayerTimesFacade(location,settings,calculator,PrayerClock(),mockk<RefreshPrayerLocationUseCase>(),mockk<UpdatePrayerSettingsUseCase>(),reconcile,emptyFlow(),backgroundScope)
  runCurrent();assertNull(facade.prayerDay.value);assertNull(facade.nextPrayer.value)
  assertEquals(PrayerLocationUnavailableReason.UNKNOWN,(facade.locationState.value as PrayerLocationState.Unavailable).reason)
  failed=false;facade.reconcileSchedule(PrayerReconciliationReason.ManualRetry);runCurrent()
  assertEquals(PrayerLocationUnavailableReason.NO_SAVED_LOCATION,(facade.locationState.value as PrayerLocationState.Unavailable).reason)
  verify(exactly=0) { calculator.calculate(any(),any(),any(),any(),any()) }
 }
}
