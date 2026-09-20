package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.repository.EhsanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

/** Verifies the current use-case contract: persist the supplied storage status unchanged. */
class UpdateDonationStatusUseCaseTest {

    private val repository: EhsanRepository = mockk(relaxed = true)
    private val useCase = UpdateDonationStatusUseCase(repository)

    @Test
    fun `canonical status is forwarded to the repository`() = runTest {
        useCase(1, "COORDINATING")

        coVerify(exactly = 1) { repository.updateDonationStatus(1, "COORDINATING") }
    }

    @Test
    fun `legacy status remains unchanged for existing profile caller`() = runTest {
        useCase(7, "COMPLETED")

        coVerify(exactly = 1) { repository.updateDonationStatus(7, "COMPLETED") }
    }

    @Test
    fun `repository failure is not hidden`() = runTest {
        val expected = IllegalStateException("write failed")
        coEvery { repository.updateDonationStatus(3, "ACTIVE") } throws expected

        val actual = runCatching { useCase(3, "ACTIVE") }.exceptionOrNull()

        assertSame(expected, actual)
    }
}
