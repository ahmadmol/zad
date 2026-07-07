package com.example.feature.hadith.domain.model

data class Hadith(
    val id: Long = 0,
    val text: String,
    val narrator: String,
    val source: String,
    val category: String,
    val isFavorite: Boolean = false,
    val explanation: String? = null // TODO: temporary explanation placeholder
)

