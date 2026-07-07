package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.Flow

class GetMyDonationsUseCase(private val repository: EhsanRepository) {
    operator fun invoke(name: String): Flow<List<Donation>> = repository.getMyDonations(name)
}

class UpdateDonationStatusUseCase(private val repository: EhsanRepository) {
    suspend operator fun invoke(id: Long, status: String) = repository.updateDonationStatus(id, status)
}

class DeleteDonationUseCase(private val repository: EhsanRepository) {
    suspend operator fun invoke(donation: Donation) = repository.deleteDonation(donation)
}
