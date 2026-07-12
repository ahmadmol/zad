package com.example.feature.ihsanplus.charitytrust.data

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard
import com.example.feature.ihsanplus.charitytrust.domain.repository.IhsanPlusCharityTrustRepository

class IhsanPlusCharityTrustRepositoryImpl : IhsanPlusCharityTrustRepository {
    override fun getDashboard(): IhsanPlusCharityTrustDashboard {
        return IhsanPlusCharityTrustFakeDataSource.getDashboard()
    }
}
