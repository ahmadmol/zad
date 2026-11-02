package com.example.feature.duas.domain.model

data class Dua(
    val id: Long,
    val title: String,
    val text: String,
    val category: String,
    val source: String,
    val reference: String,
    val isFavorite: Boolean = false,
    val explanation: String = "",
    val whenToSay: String = "",
    val likesCount: String = ""
)
