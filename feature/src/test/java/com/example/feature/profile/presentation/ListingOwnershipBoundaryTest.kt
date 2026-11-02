package com.example.feature.profile.presentation

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ListingOwnershipBoundaryTest {
    @Test fun `same author name never grants listing ownership or management`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val getMine = mockk<GetMyDonationsUseCase>()
            every { getMine("local-profile:1") } returns flowOf(emptyList())
            val update = mockk<UpdateDonationStatusUseCase>(relaxed = true)
            val delete = mockk<DeleteDonationUseCase>(relaxed = true)
            val users = mockk<UserRepository>()
            val settings = mockk<SettingsManager>()
            val prefs = mockk<UserPreferences>()
            every { users.getUser() } returns flowOf(UserEntity(1, "أحمد", "علي", "0912345678", "حلب", ""))
            every { prefs.userAvatarUri } returns flowOf(null)
            every { settings.darkModeFlow } returns flowOf(false)
            every { settings.fontSizeFlow } returns flowOf(24f)
            every { settings.manualLocationCityFlow } returns flowOf("حلب")
            val vm = ProfileViewModel(getMine, update, delete, users, settings, prefs)
            testScheduler.advanceUntilIdle()
            assertTrue(vm.uiState.value.isUserLoggedIn)
            assertTrue(vm.uiState.value.hasTrustedListingOwnership)
            assertEquals("local-profile:1", vm.uiState.value.localOwnerId)
            assertTrue(vm.uiState.value.myDonations.isEmpty())
            vm.updateStatus(1, true)
            vm.deleteDonation(Donation(1, "عرض", "وصف", "كتب", "حلب", donorName = "أحمد علي", phoneNumber = "0912345678"))
            testScheduler.advanceUntilIdle()
            verify(exactly = 1) { getMine("local-profile:1") }
            verify(exactly = 0) { getMine("أحمد علي") }
            coVerify(exactly = 0) { update(any(), any()) }
            coVerify(exactly = 0) { delete(any()) }
        } finally { Dispatchers.resetMain() }
    }
}
