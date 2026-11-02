package com.example.feature.dashboard
import com.example.feature.dashboard.presentation.GlobalLearningSearchViewModel
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.nabiihsan.data.datasource.NabiIhsanFixtureData
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class GlobalLearningSearchRecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 @Test fun `one failed source preserves other hits then retry clears error and blank clears results`()=runTest {
  val sanhya=mockk<SanhyaRepository>();val nabi=mockk<NabiIhsanRepository>();val fahm=mockk<FahmRepository>()
  var failed=true
  every { sanhya.observeStories() } answers { if(failed) flow { throw IOException() } else flowOf(emptyList()) }
  val ep=NabiIhsanFixtureData.defaultEpisodes.first()
  every { nabi.observeEpisodes() } returns flowOf(listOf(ep));every { fahm.getEpisodes() } returns flowOf(emptyList())
  val vm=GlobalLearningSearchViewModel(sanhya,nabi,fahm)
  val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
  vm.search(ep.title);runCurrent()
  assertEquals(listOf("سنحيا بالقرآن"),vm.uiState.value.failedSources);assertEquals(ep.id,vm.uiState.value.hits.single().id)
  failed=false;vm.retry();runCurrent();assertTrue(vm.uiState.value.failedSources.isEmpty())
  vm.search("");runCurrent();assertTrue(vm.uiState.value.hits.isEmpty());job.cancel()
 }
}
