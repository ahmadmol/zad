package com.example.feature.ehsan.domain.usecase

sealed interface PhoneValidationResult {
    data class Valid(val normalizedPhone: String) : PhoneValidationResult
    data object Empty : PhoneValidationResult
    data object Placeholder : PhoneValidationResult
    data object InvalidFormat : PhoneValidationResult
}

class ValidatePhoneNumberUseCase {
    operator fun invoke(phone: String?): PhoneValidationResult {
        if (phone.isNullOrBlank()) {
            return PhoneValidationResult.Empty
        }

        val trimmed = phone.trim()
        if (trimmed == "0000000000") {
            return PhoneValidationResult.Placeholder
        }

        // Normalize: retain leading + and digits only
        val normalized = trimmed.filter { it.isDigit() || it == '+' }
        
        // Basic digit check: must have at least some digits (e.g., 7+ for most regions)
        val digitsOnly = normalized.filter { it.isDigit() }
        if (digitsOnly.length < 7) {
            return PhoneValidationResult.InvalidFormat
        }

        return PhoneValidationResult.Valid(normalized)
    }
}
