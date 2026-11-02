package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.quran.domain.model.RecitationPosition
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.domain.usecase.QuranDownloadScheduler
import com.example.feature.quran.util.AudioPlayerHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuranSearchUiRecoveryTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<QuranRepository>()
    private val audio = mockk<AudioPlayerHandler>(relaxUnitFun = true)
    private val preferences = mockk<UserPreferences>()
    private val store = ViewModelStore()
    private val handle = SavedStateHandle()
    private lateinit var viewModel: QuranViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeAllSurahs() } returns flowOf(emptyList())
        every { repository.observeAllBookmarks() } returns flowOf(emptyList())
        every { repository.observeLastRead() } returns flowOf(null)
        coEvery { repository.getKhatmaProgress() } returns 0f
        every { preferences.quranFontSize } returns flowOf(28f)
        every { preferences.quranReaderId } returns flowOf("Alafasy_128kbps")
        every { audio.isPlaying } returns MutableStateFlow(false)
        every { audio.isBuffering } returns MutableStateFlow(false)
        every { audio.currentRecitation } returns MutableStateFlow<RecitationPosition?>(null)
        every { audio.currentPosition } returns MutableStateFlow(0L)
        every { audio.duration } returns MutableStateFlow(0L)
        every { audio.playbackError } returns MutableSharedFlow()
        viewModel = ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuranViewModel(repository, audio, mockk<QuranDownloadScheduler>(), preferences, handle) as T
        })[QuranViewModel::class.java]
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun `query survives recreation reruns search and clearing also clears saved state`() = runTest(dispatcher) {
        advanceUntilIdle()
        coEvery {repository.searchAyahs(any())} returns emptyList()
        viewModel.onAction(QuranAction.Search("الرحمن"));advanceUntilIdle()
        assertEquals("الرحمن",handle.get<String>("quran_query"))
        val restoredHandle=SavedStateHandle(mapOf("quran_query" to handle.get<String>("quran_query")))
        val restoredStore=ViewModelStore()
        try {
            val restored=ViewModelProvider(restoredStore,object:ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T =
                    QuranViewModel(repository,audio,mockk(),preferences,restoredHandle) as T
            })[QuranViewModel::class.java]
            advanceUntilIdle()
            assertEquals("الرحمن",restored.uiState.value.searchQuery)
            coVerify(exactly=2) {repository.searchAyahs("الرحمن")}
            restored.onAction(QuranAction.ClearSearch)
            assertEquals("",restoredHandle.get<String>("quran_query"))
        } finally {restoredStore.clear()}
    }

    @Test fun `clearing during debounce cancels search and releases loading`() = runTest(dispatcher) {
        advanceUntilIdle()
        viewModel.onAction(QuranAction.Search("الرحمن"))
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onAction(QuranAction.ClearSearch)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals("", viewModel.uiState.value.searchQuery)
        coVerify(exactly = 0) { repository.searchAyahs(any()) }
    }

    @Test fun `search failure is terminal and retry clears stale errors`() = runTest(dispatcher) {
        advanceUntilIdle()
        coEvery { repository.searchAyahs(any()) } throws IllegalStateException("search failed")
        viewModel.onAction(QuranAction.Search("الرحمن"))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSearching)
        assertTrue(viewModel.uiState.value.errorMessage.orEmpty().contains("تعذر البحث"))
        assertFalse(viewModel.uiState.value.errorMessage.orEmpty().contains("search failed"))
        coEvery { repository.searchAyahs(any()) } returns emptyList()
        viewModel.onAction(QuranAction.Search("الرحمن"))
        assertNull(viewModel.uiState.value.errorMessage)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSearching)
    }
}
