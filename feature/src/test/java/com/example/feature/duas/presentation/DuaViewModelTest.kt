package com.example.feature.duas.presentation

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.duas.domain.model.Dua
import com.example.feature.duas.domain.repository.DuaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DuaViewModelTest {

    private val repository: DuaRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val duasFlow = MutableStateFlow<List<Dua>>(emptyList())
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DuaViewModel
    private lateinit var collectJob: Job

    private val sampleDuas = listOf(
        Dua(id = 1, title = "دعاء القرآن", text = "ربنا آتنا", category = "أدعية قرآنية", source = "البقرة", reference = "", isFavorite = true),
        Dua(id = 2, title = "دعاء السفر", text = "سبحان الذي", category = "السفر", source = "مسلم", reference = "", isFavorite = false),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllDuas() } returns duasFlow
        coEvery { repository.loadDuasIfNeeded() } returns Unit
        viewModel = DuaViewModel(repository, userPreferences)
        collectJob = CoroutineScope(testDispatcher).launch {
            viewModel.uiState.collect { }
        }
    }

    @After
    fun tearDown() {
        collectJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads duas successfully`() = runTest {
        duasFlow.value = sampleDuas
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.allDuas.size)
        assertEquals(2, state.duas.size)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `filtering by category updates duas list`() = runTest {
        duasFlow.value = sampleDuas
        advanceUntilIdle()

        viewModel.onAction(DuaAction.OnCategorySelected("السفر"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("السفر", state.selectedCategory)
        assertEquals(1, state.duas.size)
        assertEquals("دعاء السفر", state.duas.first().title)
    }

    @Test
    fun `filtering by search query updates duas list`() = runTest {
        duasFlow.value = sampleDuas
        advanceUntilIdle()

        viewModel.onAction(DuaAction.OnSearchQueryChanged("آتنا"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("آتنا", state.searchQuery)
        assertEquals(1, state.duas.size)
        assertEquals(1L, state.duas.first().id)
    }

    @Test
    fun `showing favorites only filters list correctly`() = runTest {
        duasFlow.value = sampleDuas
        advanceUntilIdle()

        viewModel.onAction(DuaAction.OnToggleFavoritesOnly(show = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showFavoritesOnly)
        assertEquals(1, state.duas.size)
        assertTrue(state.duas.first().isFavorite)
    }

    @Test
    fun `toggling favorite calls repository`() = runTest {
        viewModel.onAction(DuaAction.OnToggleFavorite(id = 1L, isFavorite = false))
        advanceUntilIdle()

        coVerify { repository.toggleFavorite(1L, false) }
    }
}
