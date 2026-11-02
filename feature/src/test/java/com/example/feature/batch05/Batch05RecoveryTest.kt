package com.example.feature.batch05
import androidx.lifecycle.SavedStateHandle
import com.example.feature.hadith.presentation.*
import com.example.feature.hadith.domain.repository.HadithRepository
import com.example.feature.hadith.domain.usecase.GetHadithsUseCase
import com.example.feature.duas.presentation.*
import com.example.feature.duas.domain.repository.DuaRepository
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.asma.domain.repository.AsmaRepository
import com.example.feature.asma.presentation.*
import com.example.feature.tasbih.presentation.*
import com.example.feature.azkar.domain.usecase.*
import com.example.feature.azkar.data.local.SettingsManager
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class Batch05RecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 @Test fun `hadith failed source retries and retains restored query`()=runTest {
  val repo=mockk<HadithRepository>();var failed=true
  coEvery { repo.initialPopulation() } just Runs
  every { repo.getAllHadiths() } answers { if(failed) flow { throw IOException() } else flowOf(emptyList()) }
  val vm=HadithViewModel(GetHadithsUseCase(repo),repo,SavedStateHandle(mapOf("query" to "رحمة")))
  val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();assertNotNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isLoading)
  failed=false;vm.onAction(HadithAction.Retry);advanceUntilIdle();assertNull(vm.uiState.value.error);assertEquals("رحمة",vm.uiState.value.searchQuery);job.cancel()
 }
 @Test fun `dua stream errors are not empty success and refresh really resubscribes`()=runTest {
  val repo=mockk<DuaRepository>();var failed=true;coEvery { repo.loadDuasIfNeeded() } just Runs
  every { repo.getAllDuas() } answers { if(failed) flow { throw IOException() } else flowOf(emptyList()) }
  val vm=DuaViewModel(repo,mockk<UserPreferences>(),SavedStateHandle(mapOf("query" to "دعاء")))
  val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();assertNotNull(vm.uiState.value.errorMessage)
  failed=false;vm.onAction(DuaAction.Refresh);runCurrent();assertNull(vm.uiState.value.errorMessage);assertEquals("دعاء",vm.uiState.value.searchQuery);job.cancel()
 }
 @Test fun `asma source load failure remains visible and retries both load and observer`()=runTest {
  val repo=mockk<AsmaRepository>();coEvery { repo.loadAsma() } throws IOException()
  every { repo.getAllAsma() } returns flowOf(emptyList())
  val vm=AsmaViewModel(repo,mockk<UserPreferences>());runCurrent();assertNotNull(vm.uiState.value.error)
  coEvery { repo.loadAsma() } just Runs;vm.onAction(AsmaAction.OnRetry);runCurrent();assertNull(vm.uiState.value.error);assertFalse(vm.uiState.value.isLoading)
 }
 @Test fun `tasbih write error actually reaches public state and retry clears it`()=runTest {
  val get=mockk<GetAzkarUseCase>();every { get() } returns flowOf(emptyList())
  val increment=mockk<IncrementCounterUseCase>();coEvery { increment(any()) } throws IOException()
  val settings=mockk<SettingsManager>();every { settings.vibrationEnabledFlow } returns flowOf(false)
  val vm=TasbihViewModel(get,increment,mockk<ResetCounterUseCase>(),settings,mockk<UserPreferences>())
  val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  runCurrent();vm.onAction(TasbihAction.OnIncrement(1));runCurrent();assertNotNull(vm.uiState.value.error)
  vm.onAction(TasbihAction.Retry);runCurrent();assertNull(vm.uiState.value.error);job.cancel()
 }
}
