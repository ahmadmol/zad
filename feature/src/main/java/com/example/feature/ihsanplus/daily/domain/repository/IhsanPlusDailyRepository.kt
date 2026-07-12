package com.example.feature.ihsanplus.daily.domain.repository

import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyExperience
import kotlinx.coroutines.flow.Flow

interface IhsanPlusDailyRepository {
    fun getDailyExperience(): Flow<IhsanPlusDailyExperience>
}
