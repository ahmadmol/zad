package com.example.feature.batch05
import androidx.lifecycle.viewModelScope
import com.example.feature.settings.presentation.*
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.statistics.presentation.StatisticsViewModel
import com.example.feature.statistics.domain.*
import com.example.feature.dashboard.presentation.*
import com.example.feature.dashboard.domain.model.*
import com.example.feature.dashboard.domain.usecase.*
import com.example.feature.prayer.domain.usecase.UpdatePrayerSettingsUseCase
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class Batch05RootRecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 @Test fun `corrupt settings stays an error without reset then explicit retry reopens`()=runTest {
  val settings=mockk<SettingsManager>();val prefs=mockk<UserPreferences>();var failed=true
  every { settings.fontSizeFlow } returns flow { if(failed) throw androidx.datastore.core.CorruptionException("private");emit(26f) }
  every { settings.themeModeFlow } returns flowOf(SettingsManager.ThemeMode.DARK);every { settings.vibrationEnabledFlow } returns flowOf(false)
  every { settings.manualLocationCityFlow } returns flowOf("QA");every { settings.useAutoLocationFlow } returns flowOf(false);every { settings.morningReminderEnabledFlow } returns flowOf(false)
  every { prefs.adhanSoundUri } returns flowOf(null)
  val vm=SettingsViewModel(settings,prefs);val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();assertNotNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isLoading)
  failed=false;vm.onAction(SettingsAction.Retry);runCurrent();assertNull(vm.uiState.value.error);assertEquals(26f,vm.uiState.value.fontSize)
  coVerify(exactly=0) { settings.setFontSize(any()) };job.cancel();vm.viewModelScope.cancel()
 }
 @Test fun `statistics storage failure retry reads records without pretending empty success`()=runTest {
  val repo=mockk<StatisticsRepository>();var failed=true
  every { repo.observeStatistics() } answers { if(failed) flow { throw IOException() } else flowOf(StatisticsSnapshot(emptyList(),emptyList())) }
  val vm=StatisticsViewModel(repo);val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();assertNotNull(vm.uiState.value.error)
  failed=false;vm.retry();runCurrent();assertNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isLoading);job.cancel();vm.viewModelScope.cancel()
 }
 @Test fun `home refresh resubscribes terminated sections and clock stops with owner`()=runTest {
  val profile=mockk<ObserveHomeProfileSummaryUseCase>();var starts=0
  every { profile() } returns flow { starts++;emit(HomeSectionState.Error(canRetry=true));awaitCancellation() }
  val prayer=mockk<ObserveHomePrayerSummaryUseCase>();every { prayer() } returns flowOf(HomeSectionState.Loading)
  val quran=mockk<ObserveHomeQuranSummaryUseCase>();every { quran() } returns flowOf(HomeSectionState.Loading)
  val daily=mockk<ObserveHomeDailyActivitiesUseCase>();every { daily() } returns flowOf(HomeSectionState.Loading)
  val dhikr=mockk<ObserveHomeDhikrSummaryUseCase>();every { dhikr() } returns flowOf(HomeSectionState.Loading)
  val asma=mockk<ObserveHomeAsmaSummaryUseCase>();every { asma() } returns flowOf(HomeSectionState.Loading)
  val charity=mockk<ObserveHomeCharitySummaryUseCase>();every { charity() } returns flowOf(HomeSectionState.Loading)
  val refresh=mockk<RefreshHomeDashboardUseCase>();coEvery { refresh() } returns Result.success(Unit)
  val vm=HomeDashboardViewModel(profile,prayer,quran,daily,dhikr,asma,charity,refresh,mockk<UpdatePrayerSettingsUseCase>(),mockk<PrayerLocationRepository>())
  val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();assertEquals(1,starts);vm.onAction(HomeDashboardAction.OnRefresh);runCurrent();assertEquals(2,starts)
  job.cancel();vm.viewModelScope.cancel()
 }
}
