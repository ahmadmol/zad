package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository

class GetDonationByIdUseCase(
    private val repository: EhsanRepository
) {
    suspend operator fun invoke(id: Long): Donation? {
        return repository.getDonationById(id)
    }
}
