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
        if (trimmed.any { !it.isDigit() && !it.isWhitespace() && it !in "+()- ." }) {
            return PhoneValidationResult.InvalidFormat
        }
        val normalized = buildString {
            trimmed.forEach { ch ->
                if (ch == '+') append(ch)
                else if (ch.isDigit()) append(('0'.code + Character.digit(ch, 10)).toChar())
            }
        }
        val digitsOnly = normalized.removePrefix("+")
        if (digitsOnly.length !in 7..15 || digitsOnly.any { !it.isDigit() } ||
            (normalized.startsWith('+') && digitsOnly.startsWith('0'))) return PhoneValidationResult.InvalidFormat
        if (digitsOnly.all { it == '0' }) return PhoneValidationResult.Placeholder
        return PhoneValidationResult.Valid(normalized)
    }
}
