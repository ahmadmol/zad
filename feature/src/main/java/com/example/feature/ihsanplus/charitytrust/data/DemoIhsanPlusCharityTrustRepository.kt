package com.example.feature.ihsanplus.charitytrust.data

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard
import com.example.feature.ihsanplus.charitytrust.domain.repository.IhsanPlusCharityTrustRepository

/**
 * Demo-only charity trust repository.
 * Classification: Demo. Must not be registered in production / release DI.
 */
class DemoIhsanPlusCharityTrustRepository : IhsanPlusCharityTrustRepository {
    override fun getDashboard(): IhsanPlusCharityTrustDashboard {
        return DemoIhsanPlusCharityTrustDataSource.getDashboard()
    }
}
