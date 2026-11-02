package com.example.feature.ihsanplus.daily.data

import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyExperience
import com.example.feature.ihsanplus.daily.domain.repository.IhsanPlusDailyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Demo-only repository wrapping [DemoIhsanPlusDailyDataSource].
 * Classification: Demo. Must not be registered in production / release DI.
 */
class DemoIhsanPlusDailyRepository(
    private val demoDataSource: DemoIhsanPlusDailyDataSource = DemoIhsanPlusDailyDataSource()
) : IhsanPlusDailyRepository {
    override fun getDailyExperience(): Flow<IhsanPlusDailyExperience> = flow {
        emit(demoDataSource.getDailyExperience())
    }
}
