package com.example.feature.ehsan.domain.usecase

/** Only constructs a destination; never calls or sends a message. Do not guess a country code. */
fun donationContactDestination(phone: String, method: String): String? {
    val valid = ValidatePhoneNumberUseCase()(phone) as? PhoneValidationResult.Valid ?: return null
    return if (method == "WHATSAPP") {
        if (!valid.normalizedPhone.startsWith('+')) null
        else "https://wa.me/${valid.normalizedPhone.removePrefix("+")}"
    } else "tel:${valid.normalizedPhone}"
}
