package com.example.feature.ihsanplus.daily.data

import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyExperience
import com.example.feature.ihsanplus.daily.domain.repository.IhsanPlusDailyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class IhsanPlusDailyRepositoryImpl(
    private val fakeDataSource: IhsanPlusDailyFakeDataSource = IhsanPlusDailyFakeDataSource()
) : IhsanPlusDailyRepository {
    override fun getDailyExperience(): Flow<IhsanPlusDailyExperience> = flow {
        emit(fakeDataSource.getDailyExperience())
    }
}
