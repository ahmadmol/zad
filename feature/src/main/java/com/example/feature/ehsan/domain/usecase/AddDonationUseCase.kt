package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository

class AddDonationUseCase(
    private val repository: EhsanRepository
) {
    suspend operator fun invoke(donation: Donation) {
        repository.addDonation(donation)
    }
}
