package com.example.feature.quran.presentation

import androidx.lifecycle.*
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.data.repository.MemorizationRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MemorizationViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    private val repository=mockk<MemorizationRepository>()
    private val store=ViewModelStore()
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { repository.plans } returns flowOf(emptyList())
        every { repository.reviews } returns flowOf(emptyList())
        every { repository.surahs } returns flowOf(emptyList())
    }
    @After fun teardown() {store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T:ViewModel> create(modelClass:Class<T>):T=MemorizationViewModel(repository) as T
    })[MemorizationViewModel::class.java]
    @Test fun `mutations wait for every source and remain blocked after read failure until retry`() = runTest(dispatcher) {
        every { repository.reviews } returns flow { throw java.io.IOException("fixture") }
        val vm = vm()
        vm.save(null, 1, 1, 7)
        advanceUntilIdle()
        vm.confirm("p"); vm.archive("p"); vm.reschedule("p", 1)
        vm.review("p", com.example.feature.quran.domain.model.ReviewResult.entries.first())
        advanceUntilIdle()
        assertFalse(vm.state.value.dataReady)
        coVerify(exactly=0) { repository.save(any(),any(),any(),any()) }
        coVerify(exactly=0) { repository.confirm(any()) }
        coVerify(exactly=0) { repository.archive(any()) }
        coVerify(exactly=0) { repository.reschedule(any(),any()) }
        coVerify(exactly=0) { repository.review(any(),any()) }
        every { repository.reviews } returns flowOf(emptyList())
        coEvery { repository.confirm("p") } just Runs
        vm.reload(); vm.confirm("p"); advanceUntilIdle()
        assertTrue(vm.state.value.dataReady)
        coVerify(exactly=0) { repository.confirm(any()) }
        vm.confirm("p"); advanceUntilIdle()
        coVerify(exactly=1) { repository.confirm("p") }
    }
    @Test fun `a later read failure invalidates an already loaded snapshot`() = runTest(dispatcher) {
        val fail = CompletableDeferred<Unit>()
        every { repository.plans } returns flow { emit(emptyList()); fail.await(); throw java.io.IOException("fixture") }
        val vm = vm(); runCurrent()
        assertTrue(vm.state.value.dataReady)
        fail.complete(Unit); runCurrent()
        assertFalse(vm.state.value.dataReady)
        vm.confirm("p"); runCurrent()
        coVerify(exactly=0) { repository.confirm(any()) }
    }
    @Test fun `observation failure leaves retry available and performs no writes`() = runTest(dispatcher) {
        every { repository.plans } returns flow {throw IllegalStateException("read failure")}
        val vm=vm();advanceUntilIdle()
        assertFalse(vm.state.value.loading);assertNotNull(vm.state.value.error)
        every { repository.plans } returns flowOf(emptyList())
        vm.reload();advanceUntilIdle()
        assertNull(vm.state.value.error);assertFalse(vm.state.value.loading)
        coVerify(exactly=0) {repository.confirm(any())}
    }
    @Test fun `double confirmation submits once and failed writes preserve the form`() = runTest(dispatcher) {
        coEvery {repository.confirm("p")} coAnswers {delay(50);throw IllegalStateException("disk full")}
        val vm=vm();advanceUntilIdle()
        vm.confirm("p");vm.confirm("p");advanceUntilIdle()
        coVerify(exactly=1) {repository.confirm("p")}
        assertNotNull(vm.state.value.error);assertFalse(vm.state.value.busy);assertEquals(0,vm.state.value.saved)
    }
    @Test fun `switching tests cannot show the previous range after a delayed read`() = runTest(dispatcher) {
        val a=MemorizationPlanEntity("a",1,1,2,0)
        val b=a.copy(id="b",firstAyah=3,lastAyah=4)
        val verse=mockk<AyahEntity>()
        coEvery {repository.verses(1,1,2)} coAnswers {delay(1000);listOf(mockk())}
        coEvery {repository.verses(1,3,4)} returns listOf(verse)
        val vm=vm();advanceUntilIdle()
        vm.loadVerses(a);runCurrent();vm.loadVerses(b);advanceUntilIdle()
        assertEquals(listOf(verse),vm.state.value.verses)
    }
}
