package com.example.feature.fahmanallah

import androidx.lifecycle.*
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.fahmanallah.data.datasource.FahmFixtureData
import com.example.feature.fahmanallah.domain.model.*
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import com.example.feature.fahmanallah.presentation.FahmViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class FahmViewModelRecoveryTest {
    private val dispatcher=StandardTestDispatcher()
    private val repository=mockk<FahmRepository>()
    private val connectivity=mockk<ConnectivityMonitor>()
    private val store=ViewModelStore()
    private val handle=SavedStateHandle()
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every {repository.getEpisodes()} returns flowOf(FahmFixtureData.INITIAL_EPISODES)
        every {repository.getStations()} returns flowOf(emptyList())
        every {repository.getJourneyProgress()} returns flowOf(FahmJourneyProgress(0,29,0))
        every {connectivity.isOnline} returns MutableStateFlow(false)
        every {repository.searchEpisodes(any())} returns flowOf(emptyList())
    }
    @After fun teardown(){store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=FahmViewModel(repository,connectivity,handle) as T
    })[FahmViewModel::class.java]
    @Test fun `failed reset preserves confirmation and retry closes it only after commit`()=runTest(dispatcher) {
        var dismissed=0
        coEvery {repository.resetFeatureData()} coAnswers {delay(50);throw java.io.IOException("fixture")}
        val vm=vm();advanceUntilIdle()
        vm.resetFeatureData {dismissed++};vm.resetFeatureData {dismissed++};advanceUntilIdle()
        assertEquals(0,dismissed);assertNotNull(vm.mainState.value.actionError);assertFalse(vm.mainState.value.busy)
        coVerify(exactly=1) {repository.resetFeatureData()}
        coEvery {repository.resetFeatureData()} just Runs
        vm.resetFeatureData {dismissed++};advanceUntilIdle();assertEquals(1,dismissed)
    }
    @Test fun `offline opening and missing episode never record progress or keep a loading spinner`() = runTest(dispatcher) {
        val vm=vm();advanceUntilIdle();vm.loadEpisodeDetail("missing");advanceUntilIdle()
        assertFalse(vm.detailState.value.isLoading);assertNull(vm.detailState.value.episode);assertNotNull(vm.detailState.value.error)
        assertFalse(vm.detailState.value.isOnline)
        coVerify(exactly=0) {repository.markCompleted(any())};coVerify(exactly=0) {repository.updateEpisodeProgress(any(),any(),any())}
    }
    @Test fun `failed completion does not navigate or claim success`() = runTest(dispatcher) {
        coEvery {repository.markCompleted("ep_01")} throws IllegalStateException("disk")
        var navigated=false
        val vm=vm();advanceUntilIdle();vm.markCompleted("ep_01"){navigated=true};advanceUntilIdle()
        assertFalse(navigated);assertNotNull(vm.detailState.value.actionError);assertFalse(vm.detailState.value.busy)
    }
    @Test fun `successful completion navigates only after write finishes and duplicate tap is ignored`() = runTest(dispatcher) {
        coEvery {repository.markCompleted("ep_01")} coAnswers {delay(100)}
        var completions=0
        val vm=vm();advanceUntilIdle();vm.markCompleted("ep_01"){completions++};vm.markCompleted("ep_01"){completions++}
        assertEquals(0,completions);advanceUntilIdle();assertEquals(1,completions)
        coVerify(exactly=1) {repository.markCompleted("ep_01")}
    }
    @Test fun `search error releases loading and clearing cancels stale results`() = runTest(dispatcher) {
        every {repository.searchEpisodes("failure")} returns flow {throw IllegalStateException("read")}
        val vm=vm();advanceUntilIdle();vm.search("failure");advanceUntilIdle()
        assertFalse(vm.searchState.value.isLoading);assertNotNull(vm.searchState.value.error)
        every {repository.searchEpisodes("old")} returns flow {delay(1000);emit(FahmFixtureData.INITIAL_EPISODES)}
        vm.search("old");runCurrent();vm.search("");advanceUntilIdle()
        assertTrue(vm.searchState.value.results.isEmpty());assertNull(vm.searchState.value.error);assertEquals("",vm.searchState.value.query)
    }
    @Test fun `restored query and tabs survive route initialization with retry after read error`() = runTest(dispatcher) {
        handle["fahm_search_query"]="التوكل";handle["fahm_saved_tab"]=1
        every {repository.getEpisodes()} returns flow {throw IllegalStateException("read")}
        val vm=vm();advanceUntilIdle();vm.initialiseSavedTab(0)
        assertEquals(1,vm.savedState.value.selectedTab);assertEquals("التوكل",vm.searchState.value.query)
        assertNotNull(vm.mainState.value.error)
        every {repository.getEpisodes()} returns flowOf(FahmFixtureData.INITIAL_EPISODES)
        vm.reload();advanceUntilIdle();assertNull(vm.mainState.value.error);assertEquals(29,vm.mainState.value.episodes.size)
    }
}
