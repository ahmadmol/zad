package com.example.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val imageStore: EhsanImageStore = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { userRepository.getUser() } returns flowOf(
            UserEntity(1, "أحمد", "علي", "0912345678", "حلب", "شارع 1")
        )
        coEvery { userPreferences.userAvatarUri } returns flowOf("ihsan-image:test.jpg")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads saved avatar from user preferences`() = runTest {
        val viewModel = EditProfileViewModel(
            userRepository,
            userPreferences,
            imageStore,
            SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("ihsan-image:test.jpg", viewModel.uiState.value.avatarUrl)
    }

    @Test
    fun `onAvatarSelected persists image and updates state`() = runTest {
        val sampleUri = mockk<Uri>()
        coEvery { imageStore.persist(sampleUri) } returns "ihsan-image:new_avatar.jpg"

        val viewModel = EditProfileViewModel(
            userRepository,
            userPreferences,
            imageStore,
            SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAvatarSelected(sampleUri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("ihsan-image:new_avatar.jpg", viewModel.uiState.value.avatarUrl)
    }

    @Test
    fun `saveChanges updates user profile and saves avatar in user preferences`() = runTest {
        val viewModel = EditProfileViewModel(
            userRepository,
            userPreferences,
            imageStore,
            SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveChanges()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { userRepository.updateProfile("أحمد", "علي", "حلب", "شارع 1") }
        coVerify { userPreferences.setUserAvatarUri("ihsan-image:test.jpg") }
        assertEquals("تم حفظ التغييرات بنجاح", viewModel.uiState.value.successMessage)
    }
}
