package com.example.feature.ihsanplus.integration.contract

import com.example.feature.ihsanplus.integration.model.IhsanPlusCharitySourceSnapshot
import com.example.feature.ihsanplus.integration.model.IhsanPlusDailySourceSnapshot
import com.example.feature.ihsanplus.integration.model.IhsanPlusIntegrationStatus
import com.example.feature.ihsanplus.integration.model.IhsanPlusPrayerSourceSnapshot
import kotlinx.coroutines.flow.Flow

interface IhsanPlusDailySource {
    fun observeSnapshot(): Flow<IhsanPlusDailySourceSnapshot>
}

interface IhsanPlusPrayerSource {
    fun observeSnapshot(): Flow<IhsanPlusPrayerSourceSnapshot>
}

interface IhsanPlusCharitySource {
    fun observeSnapshot(): Flow<IhsanPlusCharitySourceSnapshot>
}

interface IhsanPlusIntegrationReadinessEvaluator {
    suspend fun evaluate(): IhsanPlusIntegrationStatus
}
