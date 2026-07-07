package com.example.feature.asma.domain.usecase

import com.example.feature.asma.domain.repository.AsmaRepository

class LoadAsmaUseCase(
    private val repository: AsmaRepository
) {
    // دالة suspend لتحميل البيانات في الخلفية
    suspend operator fun invoke() {
        repository.loadAsma()
    }
}