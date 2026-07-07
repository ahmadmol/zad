package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.repository.AzkarRepository

class AddCustomZikrUseCase(
    private val repository: AzkarRepository
) {
    suspend operator fun invoke(text: String, targetCount: Int) {
        val newZikr = Zikr(
            id = 0, // Room will generate ID
            title = "ذكر خاص",
            text = text,
            currentCount = 0,
            targetCount = targetCount,
            category = "أذكاري الخاصة",
            isFavorite = false,
            source = "إضافة من المستخدم",
            dailyProgress = 0
        )
        repository.addZikr(newZikr)
    }
}