package com.example.feature.ihsanplus.charitytrust.domain.repository

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard

interface IhsanPlusCharityTrustRepository {
    fun getDashboard(): IhsanPlusCharityTrustDashboard
}
