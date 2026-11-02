package com.example.feature.ihsanplus.daily.domain.usecase

import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyExperience
import com.example.feature.ihsanplus.daily.domain.repository.IhsanPlusDailyRepository
import kotlinx.coroutines.flow.Flow

class GetIhsanPlusDailyExperienceUseCase(
    private val repository: IhsanPlusDailyRepository
) {
    operator fun invoke(): Flow<IhsanPlusDailyExperience> {
        return repository.getDailyExperience()
    }
}
