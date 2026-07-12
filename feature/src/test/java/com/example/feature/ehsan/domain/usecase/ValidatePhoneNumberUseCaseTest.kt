package com.example.feature.ehsan.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePhoneNumberUseCaseTest {

    private val useCase = ValidatePhoneNumberUseCase()

    @Test
    fun `valid phone returns Valid`() {
        val phone = "+963 912 345 678"
        val result = useCase(phone)
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+963912345678", (result as PhoneValidationResult.Valid).normalizedPhone)
    }

    @Test
    fun `blank phone returns Empty`() {
        assertEquals(PhoneValidationResult.Empty, useCase(""))
        assertEquals(PhoneValidationResult.Empty, useCase("   "))
        assertEquals(PhoneValidationResult.Empty, useCase(null))
    }

    @Test
    fun `placeholder phone returns Placeholder`() {
        assertEquals(PhoneValidationResult.Placeholder, useCase("0000000000"))
    }

    @Test
    fun `too few digits returns InvalidFormat`() {
        assertEquals(PhoneValidationResult.InvalidFormat, useCase("123456"))
    }

    @Test
    fun `only symbols returns InvalidFormat`() {
        assertEquals(PhoneValidationResult.InvalidFormat, useCase("+++ ---"))
    }
}
