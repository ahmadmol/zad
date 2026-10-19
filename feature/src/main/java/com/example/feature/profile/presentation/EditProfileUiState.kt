package com.example.feature.profile.presentation

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val city: String = "حلب",
    val address: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
