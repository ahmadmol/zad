package com.example.feature.tasbih.presentation

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasbihViewModelTest {

    private val getAzkarUseCase: GetAzkarUseCase = mockk()
    private val incrementCounterUseCase: IncrementCounterUseCase = mockk(relaxed = true)
    private val resetCounterUseCase: ResetCounterUseCase = mockk(relaxed = true)
    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val azkarFlow = MutableStateFlow(
        listOf(
            Zikr(1, "س", "سبحان الله", 5, 33, "تسبيح", false, "seed", 5),
            Zikr(2, "س", "legacy", 1, 100, "سبحة", false, "user", 1)
        )
    )
    private val vibration = MutableStateFlow(true)
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: TasbihViewModel
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getAzkarUseCase() } returns azkarFlow
        every { settingsManager.vibrationEnabledFlow } returns vibration
        coEvery { incrementCounterUseCase(any()) } returns Unit
        coEvery { resetCounterUseCase(any()) } returns Unit
        coEvery { userPreferences.incrementDailyActivityCount(any()) } returns Unit
        viewModel = TasbihViewModel(
            getAzkarUseCase,
            incrementCounterUseCase,
            resetCounterUseCase,
            settingsManager,
            userPreferences
        )
        collectJob = CoroutineScope(dispatcher).launch {
            viewModel.uiState.collect { }
        }
    }

    @After
    fun tearDown() {
        collectJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `only تسبيح category items are production counter items`() = runTest {
        advanceUntilIdle()
        assertEquals(listOf(1L), viewModel.uiState.value.tasbihList.map { it.id })
        assertEquals(5, viewModel.uiState.value.currentTasbih?.currentCount)
    }

    @Test
    fun `increment writes through increment use case and daily activity`() = runTest {
        viewModel.onAction(TasbihAction.OnIncrement(1L))
        advanceUntilIdle()
        coVerify { incrementCounterUseCase(1L) }
        coVerify { userPreferences.incrementDailyActivityCount(DailyActivityIds.TASBEEH) }
    }

    @Test
    fun `reset writes through reset use case`() = runTest {
        viewModel.onAction(TasbihAction.OnReset(1L))
        advanceUntilIdle()
        coVerify { resetCounterUseCase(1L) }
    }

    @Test
    fun `stored progress remains visible after recreation`() = runTest {
        azkarFlow.value = listOf(
            Zikr(1, "س", "سبحان الله", 20, 33, "تسبيح", false, "seed", 20)
        )
        val recreated = TasbihViewModel(
            getAzkarUseCase,
            incrementCounterUseCase,
            resetCounterUseCase,
            settingsManager,
            userPreferences
        )
        val job = CoroutineScope(dispatcher).launch { recreated.uiState.collect { } }
        advanceUntilIdle()
        assertEquals(20, recreated.uiState.value.currentTasbih?.currentCount)
        assertTrue(recreated.uiState.value.tasbihList.all { it.category == "تسبيح" })
        job.cancel()
    }
}
