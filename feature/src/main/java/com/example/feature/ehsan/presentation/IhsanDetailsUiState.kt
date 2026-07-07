package com.example.feature.ehsan.presentation

data class IhsanDetailsUi(
    val id: Long,
    val title: String,
    val description: String,
    val type: String, // "OFFER" or "REQUEST"
    val category: String,
    val location: String,
    val status: String,
    val donorName: String,
    val phoneNumber: String,
    val imageUrl: String? = null
)

data class IhsanDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val item: IhsanDetailsUi? = null
)
