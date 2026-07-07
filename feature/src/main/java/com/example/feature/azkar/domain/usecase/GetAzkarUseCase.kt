package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.repository.AzkarRepository
import kotlinx.coroutines.flow.Flow

class GetAzkarUseCase(
    private val repository: AzkarRepository
){
    operator fun invoke(): Flow<List<Zikr>>{
        return repository.getAllAzkar()
    }
}