package com.example.feature.fasting

import androidx.lifecycle.*
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fasting.data.FastingRepository
import com.example.feature.fasting.domain.*
import com.example.feature.fasting.presentation.FastingViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FastingViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    private val repo=mockk<FastingRepository>()
    private val preferences=mockk<UserPreferences>()
    private val store=ViewModelStore()
    private val day=LocalDate.of(2026,10,20)
    private val rows=MutableStateFlow(listOf(FastingRecord(day,FastingStatus.FASTED,"محفوظ")))
    @Before fun setup(){ Dispatchers.setMain(dispatcher);every {repo.records} returns rows;every {preferences.hijriDayOffset} returns flowOf(0) }
    @After fun teardown(){store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=FastingViewModel(repo,preferences) as T
    })[FastingViewModel::class.java]
    @Test fun `save delete and clear cannot mutate a failed or loading snapshot`() = runTest(dispatcher) {
        every { repo.records } returns flow { throw java.io.IOException("fixture") }
        val vm = vm(); vm.save(day,FastingStatus.FASTED,""); advanceUntilIdle()
        vm.save(day,FastingStatus.FASTED,""); vm.delete(day); vm.clearFastingOnly(); advanceUntilIdle()
        assertFalse(vm.state.value.dataReady)
        coVerify(exactly=0) { repo.save(any(),any(),any()) }
        coVerify(exactly=0) { repo.delete(any()) }
        coVerify(exactly=0) { repo.clearFastingOnly() }
        every { repo.records } returns rows
        coEvery { repo.save(day,FastingStatus.FASTED,"") } just Runs
        vm.reload(); vm.clearFastingOnly(); advanceUntilIdle()
        assertTrue(vm.state.value.dataReady)
        coVerify(exactly=0) { repo.clearFastingOnly() }
        vm.save(day,FastingStatus.FASTED,""); advanceUntilIdle()
        coVerify(exactly=1) { repo.save(day,FastingStatus.FASTED,"") }
    }
    @Test fun `stream failure preserves records but revokes write readiness`() = runTest(dispatcher) {
        val fail = CompletableDeferred<Unit>()
        every { repo.records } returns flow { emit(rows.value); fail.await(); throw java.io.IOException("fixture") }
        val vm=vm(); runCurrent(); assertTrue(vm.state.value.dataReady)
        fail.complete(Unit); runCurrent()
        assertFalse(vm.state.value.dataReady); assertEquals(rows.value,vm.state.value.records)
        vm.clearFastingOnly(); runCurrent()
        coVerify(exactly=0) { repo.clearFastingOnly() }
    }
    @Test fun `observing and changing display offset never write or move record dates`() = runTest(dispatcher) {
        val offset=MutableStateFlow(0);every {preferences.hijriDayOffset} returns offset
        val vm=vm();advanceUntilIdle();offset.value=2;advanceUntilIdle()
        assertEquals(2,vm.state.value.offset);assertEquals(day,vm.state.value.records.single().date)
        coVerify(exactly=0) {repo.save(any(),any(),any())};coVerify(exactly=0) {repo.clearFastingOnly()}
    }
    @Test fun `read error retains prior records and retry restores observation`() = runTest(dispatcher) {
        val vm=vm();advanceUntilIdle()
        every {repo.records} returns flow {throw IllegalStateException("storage")}
        vm.reload();advanceUntilIdle();assertNotNull(vm.state.value.error);assertEquals(1,vm.state.value.records.size)
        every {repo.records} returns rows;vm.reload();advanceUntilIdle();assertNull(vm.state.value.error)
    }
    @Test fun `write failure preserves data and does not emit saved event`() = runTest(dispatcher) {
        coEvery {repo.save(any(),any(),any())} throws IllegalStateException("disk")
        val vm=vm();advanceUntilIdle();vm.save(day,FastingStatus.NOT_FASTED,"draft");advanceUntilIdle()
        assertNotNull(vm.state.value.error);assertFalse(vm.state.value.saved);assertFalse(vm.state.value.busy)
        assertEquals(FastingStatus.FASTED,vm.state.value.records.single().status)
    }
    @Test fun `double delete submits once and emits success only after completion`() = runTest(dispatcher) {
        coEvery {repo.delete(day)} coAnswers {delay(100)}
        val vm=vm();advanceUntilIdle();vm.delete(day);vm.delete(day)
        assertFalse(vm.state.value.saved);advanceUntilIdle()
        coVerify(exactly=1) {repo.delete(day)};assertTrue(vm.state.value.saved)
        vm.acknowledgeSaved();assertFalse(vm.state.value.saved)
    }
}
