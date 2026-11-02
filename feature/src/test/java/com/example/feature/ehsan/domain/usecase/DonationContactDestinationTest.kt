package com.example.feature.ehsan.domain.usecase

import org.junit.Assert.*
import org.junit.Test

class DonationContactDestinationTest {
    @Test fun internationalWhatsappUsesDigitsAndTheOfficialDestination() {
        assertEquals("https://wa.me/12025550123", donationContactDestination("+١٢٠٢٥٥٥٠١٢٣", "WHATSAPP"))
    }
    @Test fun localPhoneCanDialButDoesNotGuessWhatsappCountryCode() {
        assertEquals("tel:0912345678", donationContactDestination("0912 345 678", "PHONE"))
        assertNull(donationContactDestination("0912 345 678", "WHATSAPP"))
    }
    @Test fun blankDemoAndMalformedNumbersNeverProduceAnActiveDestination() {
        for (phone in listOf("", "private-local", "+12?text=payload", "000000000")) {
            assertNull(donationContactDestination(phone, "PHONE"))
            assertNull(donationContactDestination(phone, "WHATSAPP"))
        }
    }
}
