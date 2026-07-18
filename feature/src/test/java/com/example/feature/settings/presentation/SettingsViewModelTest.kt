package com.example.feature.settings.presentation

import com.example.feature.azkar.data.local.SettingsManager
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val fontSize = MutableStateFlow(24f)
    private val darkMode = MutableStateFlow(false)
    private val vibration = MutableStateFlow(true)
    private val adhanSound = MutableStateFlow<String?>(null)
    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { settingsManager.fontSizeFlow } returns fontSize
        every { settingsManager.darkModeFlow } returns darkMode
        every { settingsManager.vibrationEnabledFlow } returns vibration
        every { userPreferences.adhanSoundUri } returns adhanSound
        coEvery { settingsManager.setFontSize(any()) } coAnswers {
            fontSize.value = firstArg()
        }
        coEvery { settingsManager.setDarkMode(any()) } coAnswers {
            darkMode.value = firstArg()
        }
        coEvery { settingsManager.setVibrationEnabled(any()) } coAnswers {
            vibration.value = firstArg()
        }
        coEvery { userPreferences.setAdhanSoundUri(any()) } coAnswers {
            adhanSound.value = firstArg()
        }
        viewModel = SettingsViewModel(settingsManager, userPreferences)
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
    fun `initial settings load from persistence flows`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(24f, state.fontSize, 0.01f)
        assertFalse(state.isDarkMode)
        assertTrue(state.isVibrationEnabled)
    }

    @Test
    fun `dark mode update writes settings manager`() = runTest {
        viewModel.onAction(SettingsAction.SetDarkMode(true))
        advanceUntilIdle()
        coVerify { settingsManager.setDarkMode(true) }
        assertTrue(viewModel.uiState.value.isDarkMode)
    }

    @Test
    fun `vibration update writes settings manager`() = runTest {
        viewModel.onAction(SettingsAction.SetVibration(false))
        advanceUntilIdle()
        coVerify { settingsManager.setVibrationEnabled(false) }
        assertFalse(viewModel.uiState.value.isVibrationEnabled)
    }

    @Test
    fun `font size update writes settings manager`() = runTest {
        viewModel.onAction(SettingsAction.SetFontSize(30f))
        advanceUntilIdle()
        coVerify { settingsManager.setFontSize(30f) }
        assertEquals(30f, viewModel.uiState.value.fontSize, 0.01f)
    }

    @Test
    fun `adhan sound update writes user preferences only`() = runTest {
        viewModel.onAction(SettingsAction.SetAdhanSound("content://tone"))
        advanceUntilIdle()
        coVerify { userPreferences.setAdhanSoundUri("content://tone") }
        coVerify(exactly = 0) { settingsManager.setDarkMode(any()) }
        assertEquals("content://tone", viewModel.uiState.value.adhanSoundUri)
    }
}
