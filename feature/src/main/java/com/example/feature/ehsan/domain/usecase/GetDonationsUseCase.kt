package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.Flow

class GetDonationsUseCase(
    private val repository: EhsanRepository
) {
    operator fun invoke(): Flow<List<Donation>> {
        return repository.getAllDonations()
    }
}
