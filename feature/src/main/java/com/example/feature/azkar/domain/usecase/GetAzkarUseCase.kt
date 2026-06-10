package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.model.Ziker
import com.example.feature.azkar.domain.repository.AzkarRepository
import kotlinx.coroutines.flow.Flow

class GetAzkarUseCase(
    private val repository: AzkarRepository
){
    operator fun invoke(): Flow<List<Ziker>>{
        return repository.getAllAzkar()
    }
}