package com.example.feature.ehsan.presentation

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IhsanDetailsViewModelTest {

    private val repository: EhsanRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: IhsanDetailsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = IhsanDetailsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `canonical route loads donation by id`() = runTest {
        coEvery { repository.getDonationById(7L) } returns sampleDonation(id = 7L, type = "OFFER")
        viewModel.loadItem(7L)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(7L, viewModel.uiState.value.item?.id)
        assertEquals("OFFER", viewModel.uiState.value.item?.type)
        assertEquals("0912345678", viewModel.uiState.value.item?.phoneNumber)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `missing id produces error state`() = runTest {
        coEvery { repository.getDonationById(99L) } returns null
        viewModel.loadItem(99L)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.item)
        assertEquals("لم يتم العثور على الحالة", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `request type is preserved for rendering`() = runTest {
        coEvery { repository.getDonationById(3L) } returns sampleDonation(id = 3L, type = "REQUEST")
        viewModel.loadItem(3L)
        advanceUntilIdle()
        assertEquals("REQUEST", viewModel.uiState.value.item?.type)
    }

    @Test
    fun `ui model does not invent verification badges`() = runTest {
        coEvery { repository.getDonationById(1L) } returns sampleDonation(id = 1L)
        viewModel.loadItem(1L)
        advanceUntilIdle()
        val item = viewModel.uiState.value.item
        assertNotNull(item)
        // IhsanDetailsUi has no trust/verification fields by design.
        val fieldNames = IhsanDetailsUi::class.java.declaredFields.map { it.name }
        assertTrue(fieldNames.none { it.contains("trust", ignoreCase = true) })
        assertTrue(fieldNames.none { it.contains("verif", ignoreCase = true) })
        assertTrue(fieldNames.none { it.contains("badge", ignoreCase = true) })
    }

    private fun sampleDonation(id: Long, type: String = "OFFER") = Donation(
        id = id,
        title = "title",
        description = "desc",
        type = type,
        category = "طعام",
        location = "حلب",
        status = "AVAILABLE",
        donorName = "أحمد",
        phoneNumber = "0912345678",
        imageUrl = null,
        createdAt = 0L
    )
}
