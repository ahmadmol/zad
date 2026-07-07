package com.example.feature.qibla.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class GetQiblaDirectionUseCaseTest {

    private val getQiblaDirectionUseCase = GetQiblaDirectionUseCase()

    @Test
    fun `invoke should return correct qibla angle for Aleppo`() {
        // Given: Aleppo coordinates
        val aleppoLat = 36.2021
        val aleppoLng = 37.1343
        
        // When
        val result = getQiblaDirectionUseCase(aleppoLat, aleppoLng)
        
        // Then: For Aleppo, Qibla angle should be approx 169 degrees
        assertEquals(169.3f, result, 1.0f)
    }

    @Test
    fun `invoke should return correct qibla angle for London`() {
        // Given: London coordinates
        val londonLat = 51.5074
        val londonLng = -0.1278
        
        // When
        val result = getQiblaDirectionUseCase(londonLat, londonLng)
        
        // Then: For London, Qibla angle should be approx 119 degrees
        assertEquals(118.9f, result, 1.0f)
    }
}
