package com.example.feature.quran.domain.usecase

import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.repository.QuranRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSurahUseCaseTest {

    private val repository: QuranRepository = mockk()
    private val getSurahUseCase = GetSurahUseCase(repository)

    @Test
    fun `invoke should return surah from repository`() = runTest {
        // Given
        val surahId = 1
        val expectedSurah = Surah(id = 1, name = "Al-Fatihah", revelationType = "Meccan", totalVerses = 7, startPage = 1)
        coEvery { repository.getSurahById(surahId) } returns expectedSurah

        // When
        val result = getSurahUseCase(surahId)

        // Then
        assertEquals(expectedSurah, result)
    }

    @Test
    fun `invoke should return null when repository returns null`() = runTest {
        // Given
        val surahId = 999
        coEvery { repository.getSurahById(surahId) } returns null

        // When
        val result = getSurahUseCase(surahId)

        // Then
        assertEquals(null, result)
    }
}
