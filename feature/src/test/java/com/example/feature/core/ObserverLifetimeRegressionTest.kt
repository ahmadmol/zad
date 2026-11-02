package com.example.feature.core

import androidx.lifecycle.*
import com.example.feature.asma.domain.repository.AsmaRepository
import com.example.feature.asma.presentation.*
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.*
import com.example.feature.ehsan.presentation.EhsanViewModel
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.profile.presentation.ProfileViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ObserverLifetimeRegressionTest {
    private val dispatcher=StandardTestDispatcher()
    private val store=ViewModelStore()
    @Before fun setup() {Dispatchers.setMain(dispatcher)}
    @After fun teardown() {store.clear();Dispatchers.resetMain()}
    private fun <T:ViewModel> managed(type:Class<T>,create:()->T):T = ViewModelProvider(store,object:ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <V:ViewModel> create(modelClass:Class<V>):V=create() as V
    })[type]

    @Test fun `asma retry cancels the initial observer and disposal cancels the replacement`()=runTest(dispatcher) {
        var active=0
        val repo=mockk<AsmaRepository>(relaxUnitFun=true)
        every {repo.getAllAsma()} returns flow {active++;try {emit(emptyList());awaitCancellation()} finally {active--}}
        val vm=managed(AsmaViewModel::class.java){AsmaViewModel(repo,mockk())};runCurrent()
        assertEquals(1,active);vm.onAction(AsmaAction.OnRetry);runCurrent();assertEquals(1,active)
        store.clear();runCurrent();assertEquals(0,active)
    }
    @Test fun `community retry does not leave the initial board observation active`()=runTest(dispatcher) {
        var active=0
        val source=mockk<GetDonationsUseCase>();val location=mockk<PrayerLocationRepository>()
        every {location.observeLocation()} returns emptyFlow()
        every {source()} returns flow {active++;try {emit(emptyList());awaitCancellation()} finally {active--}}
        val vm=managed(EhsanViewModel::class.java){EhsanViewModel(source,location)};runCurrent()
        vm.retry();runCurrent();assertEquals(1,active)
        store.clear();runCurrent();assertEquals(0,active)
    }
    @Test fun `profile retry replaces the initial subscription instead of duplicating it`()=runTest(dispatcher) {
        var active=0
        val users=mockk<UserRepository>();val prefs=mockk<UserPreferences>();val settings=mockk<SettingsManager>()
        every {users.getUser()} returns flow {active++;try {emit(null);awaitCancellation()} finally {active--}}
        every {prefs.userAvatarUri} returns flowOf(null)
        every {settings.darkModeFlow} returns flowOf(false)
        every {settings.fontSizeFlow} returns flowOf(24f)
        every {settings.manualLocationCityFlow} returns flowOf("fixture")
        val vm=managed(ProfileViewModel::class.java){ProfileViewModel(mockk(),mockk(),mockk(),users,settings,prefs)}
        runCurrent();vm.retry();runCurrent();assertEquals(1,active)
        store.clear();runCurrent();assertEquals(0,active)
    }
}
