package com.example.feature.profile.presentation

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "0930000000",
    val city: String = "",
    val address: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
