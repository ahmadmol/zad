package com.example.feature.azkar.domain.model

data class Zikr(
    val id: Long,
    val title: String,
    val text: String,
    val currentCount: Int,
    val targetCount: Int,
    val category: String,
    val isFavorite: Boolean,
    val source: String,
    val dailyProgress: Int = 0,
    val isCompleted: Boolean = if (targetCount > 0) currentCount >= targetCount else false
)
