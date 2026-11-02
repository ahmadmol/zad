package com.example.feature.core

import androidx.lifecycle.*
import com.example.feature.calendar.IslamicCalendarViewModel
import com.example.feature.core.preferences.UserPreferences
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class IslamicCalendarViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    private val preferences=mockk<UserPreferences>()
    private val store=ViewModelStore()
    @Before fun setup(){Dispatchers.setMain(dispatcher)}
    @After fun teardown(){store.clear();Dispatchers.resetMain()}
    private fun vm()=ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T:ViewModel> create(modelClass:Class<T>):T=IslamicCalendarViewModel(preferences) as T
    })[IslamicCalendarViewModel::class.java]
    @Test fun `read failure is recoverable and does not invent a saved offset`() = runTest(dispatcher) {
        every { preferences.hijriDayOffset } returns flow {throw IllegalStateException("read")}
        val vm=vm();advanceUntilIdle();assertNotNull(vm.state.value.error);assertFalse(vm.state.value.loading)
        every {preferences.hijriDayOffset} returns flowOf(-2)
        vm.reload();advanceUntilIdle();assertEquals(-2,vm.state.value.offset);assertNull(vm.state.value.error)
    }
    @Test fun `failed write leaves previous saved value and releases busy state`() = runTest(dispatcher) {
        every {preferences.hijriDayOffset} returns flowOf(1)
        coEvery {preferences.setHijriDayOffset(2)} throws IllegalStateException("write")
        val vm=vm();advanceUntilIdle();vm.offset(2);advanceUntilIdle()
        assertEquals(1,vm.state.value.offset);assertNotNull(vm.state.value.error);assertFalse(vm.state.value.busy)
    }
}
