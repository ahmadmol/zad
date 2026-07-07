package com.example.feature.asma.domain.usecase

import com.example.feature.asma.domain.model.AllahName
import com.example.feature.asma.domain.repository.AsmaRepository
import kotlinx.coroutines.flow.Flow

class GetAsmaUseCase (
    private val repository: AsmaRepository
){
    operator fun invoke(): Flow<List<AllahName>>{
        return repository.getAllAsma()
    }
}