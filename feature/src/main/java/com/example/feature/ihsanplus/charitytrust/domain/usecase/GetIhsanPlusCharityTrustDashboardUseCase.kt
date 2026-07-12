package com.example.feature.ihsanplus.charitytrust.domain.usecase

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard
import com.example.feature.ihsanplus.charitytrust.domain.repository.IhsanPlusCharityTrustRepository

class GetIhsanPlusCharityTrustDashboardUseCase(
    private val repository: IhsanPlusCharityTrustRepository
) {
    operator fun invoke(): IhsanPlusCharityTrustDashboard {
        return repository.getDashboard()
    }
}
