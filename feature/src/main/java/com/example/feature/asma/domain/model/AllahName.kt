package com.example.feature.asma.domain.model

data class AllahName(
    val id: Int,
    val name: String,
    val transliteration: String,
    val meaning: String,
    val explanation: String,
    val isFavorite: Boolean = false
)