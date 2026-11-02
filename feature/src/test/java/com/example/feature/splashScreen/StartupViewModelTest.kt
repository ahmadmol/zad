package com.example.feature.splashScreen

import androidx.lifecycle.*
import com.example.feature.core.preferences.UserPreferences
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val preferences = mockk<UserPreferences>()
    private val store = ViewModelStore()
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { preferences.hasCompletedOnboarding } returns flowOf(false)
    }
    @After fun teardown() { store.clear(); Dispatchers.resetMain() }
    private fun vm() = ViewModelProvider(store, object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = StartupViewModel(preferences) as T
    })[StartupViewModel::class.java]

    @Test fun `read failure cannot be mistaken for a first launch or overwrite settings`() = runTest(dispatcher) {
        every { preferences.hasCompletedOnboarding } returns flow { throw java.io.IOException("fixture") }
        val vm=vm(); vm.completeOnboarding(); advanceUntilIdle()
        assertNull(vm.state.value.onboardingCompleted)
        assertNotNull(vm.state.value.error); assertFalse(vm.state.value.loading)
        vm.completeOnboarding(); advanceUntilIdle()
        coVerify(exactly=0) { preferences.setOnboardingCompleted(any()) }
        every { preferences.hasCompletedOnboarding } returns flowOf(true)
        vm.retry(); advanceUntilIdle()
        assertEquals(true,vm.state.value.onboardingCompleted); assertNull(vm.state.value.error)
        assertFalse(vm.state.value.completionSaved)
    }
    @Test fun `failed completion stays recoverable and duplicate presses save once`() = runTest(dispatcher) {
        coEvery { preferences.setOnboardingCompleted(true) } coAnswers { delay(100); throw java.io.IOException("fixture") }
        val vm=vm(); advanceUntilIdle()
        vm.completeOnboarding(); vm.completeOnboarding(); advanceUntilIdle()
        coVerify(exactly=1) { preferences.setOnboardingCompleted(true) }
        assertFalse(vm.state.value.completionSaved); assertFalse(vm.state.value.saving)
        assertEquals(false,vm.state.value.onboardingCompleted); assertNotNull(vm.state.value.error)
        coEvery { preferences.setOnboardingCompleted(true) } just Runs
        vm.completeOnboarding(); advanceUntilIdle()
        assertTrue(vm.state.value.completionSaved); assertNull(vm.state.value.error)
        vm.completeOnboarding(); advanceUntilIdle()
        coVerify(exactly=2) { preferences.setOnboardingCompleted(true) }
    }
    @Test fun `viewmodel disposal cancels pending reads without emitting an error or success`() = runTest(dispatcher) {
        every { preferences.hasCompletedOnboarding } returns flow { awaitCancellation() }
        val vm=vm(); runCurrent(); store.clear(); advanceUntilIdle()
        assertNull(vm.state.value.error); assertNull(vm.state.value.onboardingCompleted)
        assertFalse(vm.state.value.completionSaved)
    }
}
