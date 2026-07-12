package com.example.feature.ehsan.presentation

import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.domain.usecase.AddDonationUseCase
import com.example.feature.ehsan.domain.usecase.ValidatePhoneNumberUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEhsanViewModelTest {

    private val addDonationUseCase: AddDonationUseCase = mockk()
    private val userRepository: UserRepository = mockk()
    private val validatePhoneNumberUseCase = ValidatePhoneNumberUseCase()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: AddEhsanViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.getUser() } returns flowOf(
            UserEntity(1, "Ahmed", "Ali", "0912345678", "Aleppo", "Street 1")
        )
        viewModel = AddEhsanViewModel(addDonationUseCase, userRepository, validatePhoneNumberUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects user profile`() = runTest {
        assertEquals("Ahmed Ali", viewModel.uiState.value.currentUserName)
        assertEquals("0912345678", viewModel.uiState.value.currentUserPhone)
    }

    @Test
    fun `submitRequest with valid data calls use case`() = runTest {
        // Given
        coEvery { addDonationUseCase(any()) } just Runs
        
        // When
        viewModel.submitRequest("Food", "I need food", "Food", "Aleppo", "REQUEST", null)

        // Then
        coVerify { 
            addDonationUseCase(match { 
                it.title == "Food" && it.phoneNumber == "0912345678" && it.type == "REQUEST"
            }) 
        }
        assertTrue(viewModel.uiState.value.submissionSuccess)
    }

    @Test
    fun `submitRequest with invalid phone blocks submission`() = runTest {
        // Given
        every { userRepository.getUser() } returns flowOf(
            UserEntity(1, "Ahmed", "Ali", "0000000000", "Aleppo", "Street 1")
        )
        // Re-init to pick up new user
        viewModel = AddEhsanViewModel(addDonationUseCase, userRepository, validatePhoneNumberUseCase)

        // When
        viewModel.submitRequest("Food", "I need food", "Food", "Aleppo", "REQUEST", null)

        // Then
        coVerify(exactly = 0) { addDonationUseCase(any()) }
        assertNotNull(viewModel.uiState.value.phoneError)
        assertFalse(viewModel.uiState.value.submissionSuccess)
    }

    @Test
    fun `submitRequest when no user blocks submission`() = runTest {
        // Given
        every { userRepository.getUser() } returns flowOf(null)
        viewModel = AddEhsanViewModel(addDonationUseCase, userRepository, validatePhoneNumberUseCase)

        // When
        viewModel.submitRequest("Food", "I need food", "Food", "Aleppo", "REQUEST", null)

        // Then
        coVerify(exactly = 0) { addDonationUseCase(any()) }
        assertNotNull(viewModel.uiState.value.phoneError)
    }

    @Test
    fun `submitRequest when already submitting does nothing`() = runTest {
        // Given
        val viewModel = AddEhsanViewModel(addDonationUseCase, userRepository, validatePhoneNumberUseCase)
        coEvery { addDonationUseCase(any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            Unit
        }
        
        // Start first submission
        val job = launch {
            viewModel.submitRequest("Food 1", "I need food", "Food", "Aleppo", "REQUEST", null)
        }
        
        // Wait for it to start
        advanceTimeBy(100)
        assertTrue(viewModel.uiState.value.isSubmitting)

        // Start second submission
        viewModel.submitRequest("Food 2", "I need food", "Food", "Aleppo", "REQUEST", null)

        // Then
        coVerify(exactly = 1) { addDonationUseCase(any()) }
        job.cancel()
    }
}
