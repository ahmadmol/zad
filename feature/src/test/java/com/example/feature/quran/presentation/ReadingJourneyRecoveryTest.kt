package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.feature.quran.domain.repository.ReadingJourneyRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingJourneyRecoveryTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<ReadingJourneyRepository>(relaxUnitFun = true)
    private val store = ViewModelStore()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { repository.plans } returns flowOf(emptyList())
        every { repository.completedPages } returns flowOf(emptyMap())
        every { repository.notes } returns flowOf(emptyList())
    }
    @After fun teardown() { store.clear(); Dispatchers.resetMain() }
    private fun vm() = ViewModelProvider(store, object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReadingJourneyViewModel(repository) as T
    })[ReadingJourneyViewModel::class.java]

    @Test fun `a stream failure after successful loading blocks both writes and export`() = runTest(dispatcher) {
        val fail = CompletableDeferred<Unit>()
        every { repository.notes } returns flow { emit(emptyList()); fail.await(); throw java.io.IOException("fixture") }
        val vm=vm(); runCurrent(); assertTrue(vm.state.value.dataReady)
        fail.complete(Unit); runCurrent()
        assertFalse(vm.state.value.dataReady)
        vm.deleteNote("n"); runCurrent()
        coVerify(exactly=0) { repository.deleteNote(any()) }
        assertFalse(vm.prepareExport(setOf("n")))
        assertNull(vm.pendingExport)
    }

    @Test fun `initial load and failed reads cannot archive a plan or mutate notes`() = runTest(dispatcher) {
        every { repository.plans } returns flow { throw java.io.IOException("fixture read failure") }
        val vm = vm()
        vm.create(1, 604, 30)
        advanceUntilIdle()
        assertFalse(vm.state.value.dataReady)
        assertNotNull(vm.state.value.error)
        vm.create(1, 604, 30)
        vm.saveNote(null, 1, 1, "fixture")
        vm.deleteNote("n")
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.createPlan(any(), any(), any()) }
        coVerify(exactly = 0) { repository.saveNote(any(), any(), any(), any()) }
        coVerify(exactly = 0) { repository.deleteNote(any()) }
    }

    @Test fun `explicit retry restores writes without resetting stored data`() = runTest(dispatcher) {
        every { repository.notes } returns flow { throw java.io.IOException("fixture") }
        val vm = vm(); advanceUntilIdle()
        every { repository.notes } returns flowOf(emptyList())
        vm.reload(); advanceUntilIdle()
        assertTrue(vm.state.value.dataReady)
        assertNull(vm.state.value.error)
        vm.create(10, 20, 7); advanceUntilIdle()
        coVerify(exactly = 1) { repository.createPlan(10, 20, 7) }
    }

    @Test fun `failed deletion has no success event and a deliberate retry succeeds`() = runTest(dispatcher) {
        coEvery { repository.deleteNote("n") } throws java.io.IOException("fixture write failure")
        val vm = vm(); advanceUntilIdle()
        vm.deleteNote("n"); advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.deletedNoteId)
        assertNull(vm.state.value.message)
        assertFalse(vm.state.value.busy)
        coEvery { repository.deleteNote("n") } just Runs
        vm.deleteNote("n"); advanceUntilIdle()
        assertEquals("n", vm.state.value.deletedNoteId)
        assertNull(vm.state.value.error)
        vm.consumeDeletedNote()
        assertNull(vm.state.value.deletedNoteId)
    }

    @Test fun `pending deletion cannot submit twice`() = runTest(dispatcher) {
        coEvery { repository.deleteNote("n") } coAnswers { delay(100) }
        val vm = vm(); advanceUntilIdle()
        vm.deleteNote("n"); vm.deleteNote("n"); advanceUntilIdle()
        coVerify(exactly = 1) { repository.deleteNote("n") }
        assertEquals("n", vm.state.value.deletedNoteId)
    }

    @Test fun `reload blocks mutations against a stale snapshot until every source is ready`() = runTest(dispatcher) {
        val vm = vm(); advanceUntilIdle()
        every { repository.notes } returns flow { delay(100); emit(emptyList()) }
        vm.reload()
        vm.deleteNote("n"); vm.confirm("p", setOf(1))
        assertFalse(vm.state.value.dataReady)
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.deleteNote(any()) }
        coVerify(exactly = 0) { repository.confirmPages(any(), any()) }
        assertTrue(vm.state.value.dataReady)
    }

    @Test fun `export before a successful read returns an actionable error without content`() = runTest(dispatcher) {
        val vm = vm()
        assertFalse(vm.prepareExport(setOf("n")))
        assertNull(vm.pendingExport)
        assertNotNull(vm.state.value.error)
    }
}
