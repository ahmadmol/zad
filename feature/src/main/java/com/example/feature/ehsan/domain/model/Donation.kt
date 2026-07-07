package com.example.feature.ehsan.domain.model

data class Donation(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String = "OFFER", // OFFER or REQUEST
    val status: String = "AVAILABLE", // AVAILABLE, PENDING, COMPLETED
    val donorName: String,
    val phoneNumber: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
