package com.example.feature.nabiihsan

import com.example.feature.nabiihsan.util.cleanEpisodeTitle
import org.junit.Assert.assertEquals
import org.junit.Test

class NabiIhsanTitleCleanerTest {

    @Test
    fun `cleanEpisodeTitle cleans episode 12 title correctly`() {
        val rawTitle = "حلقة 12/ مرض العصر.. التفكير الزائد والتوتر والقلق ( over thinking ) .. #نبي_الإحسان"
        val expected = "مرض العصر. التفكير الزائد والتوتر والقلق ( over thinking )."
        val actual = cleanEpisodeTitle(rawTitle, 12)
        assertEquals(expected, actual)
    }

    @Test
    fun `cleanEpisodeTitle cleans episode 29 title correctly`() {
        val rawTitle = "حلقة 29 - حلقة الختام..أجواء ومشاعر رائعة.. كل عام وأنتم بخير .. #نبي_الإحسان"
        val expected = "حلقة الختام. أجواء ومشاعر رائعة. كل عام وأنتم بخير."
        val actual = cleanEpisodeTitle(rawTitle, 29)
        assertEquals(expected, actual)
    }

    @Test
    fun `cleanEpisodeTitle handles titles without episode prefixes`() {
        val rawTitle = "طريقة نبوية لسلامتك الداخلية وصحتك النفسية #نبي_الإحسان"
        val expected = "طريقة نبوية لسلامتك الداخلية وصحتك النفسية"
        val actual = cleanEpisodeTitle(rawTitle, 1)
        assertEquals(expected, actual)
    }

    @Test
    fun `cleanEpisodeTitle handles blank title`() {
        val rawTitle = "   "
        val actual = cleanEpisodeTitle(rawTitle, null)
        assertEquals(rawTitle, actual)
    }
}
