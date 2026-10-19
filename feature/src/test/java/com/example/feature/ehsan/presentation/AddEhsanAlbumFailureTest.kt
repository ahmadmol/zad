package com.example.feature.ehsan.presentation

import android.net.Uri
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.AddDonationUseCase
import com.example.feature.ehsan.domain.usecase.ValidatePhoneNumberUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AddEhsanAlbumFailureTest {
    private val dispatcher = StandardTestDispatcher()
    private val add = mockk<AddDonationUseCase>()
    private val users = mockk<UserRepository>()
    private val images = mockk<EhsanImageStore>(relaxed = true)

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { users.getUser() } returns flowOf(UserEntity(1,"أحمد","علي","0912345678","حلب",""))
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk<Uri>()
    }
    @After fun cleanup() { unmockkStatic(Uri::class); Dispatchers.resetMain() }

    @Test fun `database failure rolls back copied album and resets submission`() = runTest {
        coEvery { images.persistAlbum(any()) } returns "ihsan-album:new.json"
        coEvery { add(any()) } throws IllegalStateException("write failed")
        val vm = AddEhsanViewModel(add, users, ValidatePhoneNumberUseCase(), images)
        dispatcher.scheduler.advanceUntilIdle()
        vm.submitRequest("عنوان","وصف","تعليم","حلب",imageUrl=null,imageUrls=listOf("content:a","content:b","content:c"))
        dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly=1) { images.delete("ihsan-album:new.json") }
        assertFalse(vm.uiState.value.isSubmitting)
        assertFalse(vm.uiState.value.submissionSuccess)
        assertEquals("write failed",vm.uiState.value.submissionError)
    }

    @Test fun `more than three images never writes files or record`() = runTest {
        val vm = AddEhsanViewModel(add, users, ValidatePhoneNumberUseCase(), images)
        dispatcher.scheduler.advanceUntilIdle()
        vm.submitRequest("عنوان","وصف","تعليم","حلب",imageUrl=null,imageUrls=listOf("a","b","c","d"))
        dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly=0) { images.persistAlbum(any()) }
        coVerify(exactly=0) { add(any()) }
        assertNotNull(vm.uiState.value.submissionError)
    }
}
