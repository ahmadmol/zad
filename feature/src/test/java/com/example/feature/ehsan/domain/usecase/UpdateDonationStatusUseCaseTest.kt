package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.model.DonationStatus
import com.example.feature.ehsan.domain.repository.EhsanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 8B — the lifecycle is enforced in the use case, not in the UI. */
class UpdateDonationStatusUseCaseTest {

    private val repository: EhsanRepository = mockk(relaxed = true)
    private val useCase = UpdateDonationStatusUseCase(repository)

    private fun donation(status: DonationStatus) = Donation(
        id = 1,
        title = "t",
        description = "d",
        category = "طعام",
        location = "حلب",
        type = "OFFER",
        status = status,
        donorName = "أحمد",
        phoneNumber = "0900000000"
    )

    @Test
    fun `a valid transition is written using the canonical storage value`() = runTest {
        coEvery { repository.getDonationById(1) } returns donation(DonationStatus.ACTIVE)

        val result = useCase(1, DonationStatus.COORDINATING)

        assertEquals(UpdateDonationStatusUseCase.Result.Updated, result)
        coVerify { repository.updateDonationStatus(1, "COORDINATING") }
    }

    @Test
    fun `an invalid transition is rejected and never written`() = runTest {
        coEvery { repository.getDonationById(1) } returns donation(DonationStatus.FULFILLED)

        val result = useCase(1, DonationStatus.ACTIVE)

        assertTrue(result is UpdateDonationStatusUseCase.Result.Rejected)
        coVerify(exactly = 0) { repository.updateDonationStatus(any(), any()) }
    }

    @Test
    fun `re-applying the current status is a no-op success`() = runTest {
        coEvery { repository.getDonationById(1) } returns donation(DonationStatus.ACTIVE)

        val result = useCase(1, DonationStatus.ACTIVE)

        assertEquals(UpdateDonationStatusUseCase.Result.Updated, result)
        coVerify(exactly = 0) { repository.updateDonationStatus(any(), any()) }
    }

    @Test
    fun `a missing donation is reported rather than written blindly`() = runTest {
        coEvery { repository.getDonationById(99) } returns null

        assertEquals(
            UpdateDonationStatusUseCase.Result.NotFound,
            useCase(99, DonationStatus.FULFILLED)
        )
        coVerify(exactly = 0) { repository.updateDonationStatus(any(), any()) }
    }

    @Test
    fun `a legacy pending row can still be completed`() = runTest {
        // Row written by a pre-Phase-8 build; read back as COORDINATING.
        coEvery { repository.getDonationById(1) } returns donation(
            DonationStatus.fromStorage("PENDING")
        )

        val result = useCase(1, DonationStatus.FULFILLED)

        assertEquals(UpdateDonationStatusUseCase.Result.Updated, result)
        coVerify { repository.updateDonationStatus(1, "FULFILLED") }
    }

    @Test
    fun `every terminal status refuses every onward move`() = runTest {
        listOf(DonationStatus.FULFILLED, DonationStatus.EXPIRED, DonationStatus.CANCELLED)
            .forEach { terminal ->
                coEvery { repository.getDonationById(1) } returns donation(terminal)
                DonationStatus.entries.filter { it != terminal }.forEach { target ->
                    assertTrue(
                        terminal.name + " -> " + target.name + " must be rejected",
                        useCase(1, target) is UpdateDonationStatusUseCase.Result.Rejected
                    )
                }
            }
        coVerify(exactly = 0) { repository.updateDonationStatus(any(), any()) }
    }
}
