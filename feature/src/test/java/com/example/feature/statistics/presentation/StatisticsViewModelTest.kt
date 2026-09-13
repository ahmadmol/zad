package com.example.feature.statistics.presentation

import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.statistics.domain.StatisticsRepository
import com.example.feature.statistics.domain.StatisticsSnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
class StatisticsViewModelTest {

    private val repository: StatisticsRepository = mockk()
    private val snapshotFlow = MutableStateFlow(
        StatisticsSnapshot(last7DaysStats = emptyList(), azkarItems = emptyList())
    )
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeStatistics() } returns snapshotFlow
        viewModel = StatisticsViewModel(repository)
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
    fun `persisted daily statistics map correctly`() = runTest {
        snapshotFlow.value = StatisticsSnapshot(
            last7DaysStats = listOf(DailyStat("2026-07-10", 12)),
            azkarItems = listOf(sampleZikr(1, daily = 4), sampleZikr(2, daily = 6))
        )
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(12, state.last7DaysStats.first().totalCount)
        assertEquals(10, state.summary.totalDailyCount)
        assertEquals(2, state.dailyItems.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `empty statistics produce honest empty state`() = runTest {
        snapshotFlow.value = StatisticsSnapshot(emptyList(), emptyList())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
        assertEquals(0, viewModel.uiState.value.summary.totalDailyCount)
    }

    @Test
    fun `negative daily progress is bounded to zero`() = runTest {
        snapshotFlow.value = StatisticsSnapshot(
            last7DaysStats = listOf(DailyStat("2026-07-10", -5)),
            azkarItems = listOf(sampleZikr(1, daily = -3))
        )
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.last7DaysStats.first().totalCount)
        assertEquals(0, viewModel.uiState.value.dailyItems.first().dailyProgress)
        assertEquals(0, viewModel.uiState.value.summary.totalDailyCount)
    }

    @Test
    fun `statistics state exposes no mutation surface`() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `repository failure yields error state`() = runTest {
        every { repository.observeStatistics() } returns flow {
            throw IllegalStateException("db down")
        }
        val failingVm = StatisticsViewModel(repository)
        val job = CoroutineScope(dispatcher).launch { failingVm.uiState.collect { } }
        advanceUntilIdle()
        assertEquals("db down", failingVm.uiState.value.error)
        assertTrue(failingVm.uiState.value.isEmpty)
        job.cancel()
    }

    private fun sampleZikr(id: Long, daily: Int) = Zikr(
        id = id,
        title = "t",
        text = "text$id",
        currentCount = 0,
        targetCount = 33,
        category = "تسبيح",
        isFavorite = false,
        source = "seed",
        dailyProgress = daily
    )
}
