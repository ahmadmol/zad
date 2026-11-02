package com.example.feature.learning
import androidx.lifecycle.SavedStateHandle
import com.example.feature.nabiihsan.presentation.NabiIhsanViewModel
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.nabiihsan.domain.model.*
import com.example.feature.nabiihsan.data.datasource.NabiIhsanFixtureData
import com.example.feature.sanhya.presentation.SanhyaViewModel
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import com.example.feature.sanhya.domain.model.*
import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.quran.domain.repository.QuranRepository
import io.mockk.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class LearningRecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 @Test fun `nabi revokes progress readiness on a later storage failure and restores it on retry`()=runTest {
  val repo=mockk<NabiIhsanRepository>();val progress=mockk<com.example.feature.learning.data.LearningProgressStore>()
  val fail=CompletableDeferred<Unit>();val ep=NabiIhsanFixtureData.defaultEpisodes.first()
  every { progress.observe("nabi") } returns flow { emit(emptyMap());fail.await();throw IOException() }
  val vm=NabiIhsanViewModel(repo,progressStore=progress);runCurrent();assertTrue(vm.progressReady.value)
  fail.complete(Unit);runCurrent();assertFalse(vm.progressReady.value)
  vm.saveProgress(ep.id,60,true);runCurrent()
  coVerify(exactly=0) { progress.save(any(),any(),any(),any(),any()) }
  every { progress.observe("nabi") } returns flowOf(emptyMap())
  coEvery { repo.getEpisodeById(ep.id) } returns ep
  coEvery { progress.save(any(),any(),any(),any(),any()) } just Runs
  vm.retry();runCurrent();assertTrue(vm.progressReady.value)
  vm.saveProgress(ep.id,60,true);runCurrent()
  coVerify(exactly=1) { progress.save("nabi",ep.id,ep.id,60,true) }
 }
 @Test fun `sanhya revokes readiness after a progress stream fails without writing completion`()=runTest {
  val repo=mockk<SanhyaRepository>();val quran=mockk<QuranRepository>()
  val progress=mockk<com.example.feature.learning.data.LearningProgressStore>();val fail=CompletableDeferred<Unit>()
  every { progress.observe("sanhya") } returns flow { emit(emptyMap());fail.await();throw IOException() }
  val vm=SanhyaViewModel(repo,quran,progressStore=progress);runCurrent();assertTrue(vm.progressReady.value)
  fail.complete(Unit);runCurrent();assertFalse(vm.progressReady.value)
  vm.saveProgress("episode",10,true);runCurrent()
  coVerify(exactly=0) { progress.save(any(),any(),any(),any(),any()) }
  every { progress.observe("sanhya") } returns flowOf(emptyMap());vm.retry();runCurrent()
  assertTrue(vm.progressReady.value)
 }
 @Test fun `nabi catches linked recipe failure clears stale detail and retries`()=runTest {
  val repo=mockk<NabiIhsanRepository>(); val ep=NabiIhsanFixtureData.defaultEpisodes.first()
  coEvery { repo.getEpisodeById(any()) } returns ep
  coEvery { repo.getRecipesForEpisode(any()) } throws IOException()
  coEvery { repo.search(any(),any()) } returns NabiSearchResult(episodes=listOf(ep))
  val vm=NabiIhsanViewModel(repo)
  vm.loadEpisodeDetail(ep.id);runCurrent();assertFalse(vm.episodeDetailState.value.isLoading);assertNotNull(vm.episodeDetailState.value.errorMessage)
  coEvery { repo.getRecipesForEpisode(any()) } returns emptyList()
  vm.loadEpisodeDetail(ep.id);runCurrent();assertEquals(ep.id,vm.episodeDetailState.value.episode?.id);assertNull(vm.episodeDetailState.value.errorMessage)
  coEvery { repo.getEpisodeById("missing") } returns null
  vm.loadEpisodeDetail("missing");runCurrent();assertNull(vm.episodeDetailState.value.episode)
 }
 @Test fun `late nabi request cannot overwrite new detail`()=runTest {
  val repo=mockk<NabiIhsanRepository>();val ep=NabiIhsanFixtureData.defaultEpisodes.first()
  coEvery { repo.getEpisodeById("old") } coAnswers { delay(1000); ep }
  coEvery { repo.getEpisodeById("missing") } returns null
  val vm=NabiIhsanViewModel(repo);vm.loadEpisodeDetail("old");runCurrent();vm.loadEpisodeDetail("missing");advanceTimeBy(1100);runCurrent()
  assertNull(vm.episodeDetailState.value.episode);assertNotNull(vm.episodeDetailState.value.errorMessage)
 }
 @Test fun `search query and favorite tab restore without route overwriting draft`()=runTest {
  val repo=mockk<NabiIhsanRepository>();coEvery { repo.search(any(),any()) } returns NabiSearchResult()
  val handle=SavedStateHandle();val vm=NabiIhsanViewModel(repo,handle=handle)
  vm.onSearchQueryChanged("الرحمة");vm.setFavoritesTab(1);runCurrent()
  val restored=NabiIhsanViewModel(repo,handle=handle);restored.initializeSearch("route-value");runCurrent()
  assertEquals("الرحمة",restored.searchUiState.value.query);assertEquals(1,restored.favoritesTab.value)
 }
 @Test fun `sanhya missing and failed detail never retain old story`()=runTest {
  val repo=mockk<SanhyaRepository>();val quran=mockk<QuranRepository>();val story=SanhyaFixtureData.defaultStories.first()
  coEvery { repo.getStoryById(story.id) } returns story
  coEvery { quran.getAyahsBySurah(any()) } returns emptyList()
  coEvery { repo.getStoryById("missing") } returns null
  val vm=SanhyaViewModel(repo,quran);vm.loadStoryDetails(story.id);runCurrent();assertEquals(story.id,vm.detailState.value.story?.id)
  vm.loadStoryDetails("missing");runCurrent();assertNull(vm.detailState.value.story);assertNotNull(vm.detailState.value.errorMessage)
 }
 @Test fun `sanhya selected episode is restored and invalid selection ignored`()=runTest {
  val repo=mockk<SanhyaRepository>();val quran=mockk<QuranRepository>();val story=SanhyaFixtureData.defaultStories.first { it.episodes.size>1 }
  coEvery { repo.getStoryById(story.id) } returns story;coEvery { quran.getAyahsBySurah(any()) } returns emptyList()
  val handle=SavedStateHandle();val vm=SanhyaViewModel(repo,quran,handle=handle)
  vm.loadStoryDetails(story.id);runCurrent();val id=story.episodes.last().id;vm.selectEpisode(id);vm.selectEpisode("missing")
  val restored=SanhyaViewModel(repo,quran,handle=handle);restored.loadStoryDetails(story.id);runCurrent()
  assertEquals(id,restored.detailState.value.selectedEpisodeId)
 }
 @Test fun `favorite storage failure is visible and repeat taps are serialized`()=runTest {
  val repo=mockk<NabiIhsanRepository>();coEvery { repo.toggleEpisodeFavorite(any()) } coAnswers { delay(100);throw IOException() }
  val vm=NabiIhsanViewModel(repo);vm.toggleEpisodeFavorite("a");vm.toggleEpisodeFavorite("a");runCurrent();assertTrue(vm.busy.value)
  advanceTimeBy(101);runCurrent();assertFalse(vm.busy.value);assertNotNull(vm.actionError.value)
  coVerify(exactly=1) { repo.toggleEpisodeFavorite("a") }
 }
 @Test fun `library read failure can retry without losing query`()=runTest {
  val repo=mockk<SanhyaRepository>();val quran=mockk<QuranRepository>();var failed=true
  every { repo.observeStories() } answers { if(failed) flow { throw IOException() } else flowOf(emptyList()) }
  coEvery { repo.filterStories(any(),any()) } returns emptyList()
  val vm=SanhyaViewModel(repo,quran);val job=backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.mainUiState.collect() }
  vm.onSearchQueryChanged("موسى");runCurrent();assertNotNull(vm.mainUiState.value.errorMessage)
  failed=false;vm.retry();runCurrent();assertNull(vm.mainUiState.value.errorMessage);assertEquals("موسى",vm.mainUiState.value.searchQuery)
  job.cancel()
 }
}
