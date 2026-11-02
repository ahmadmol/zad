package com.example.feature.ui

import com.example.feature.nabiihsan.data.datasource.NabiIhsanFixtureData
import com.example.feature.nabiihsan.util.cleanEpisodeTitle
import com.example.feature.sanhya.domain.model.QuranStory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UiPolishFormattingAndEmptyStatesTest {

    @Test
    fun cleanEpisodeTitle_processesProductionFixtureEpisodesCleanly() {
        val episodes = NabiIhsanFixtureData.defaultEpisodes
        assertTrue("Fixture episodes must not be empty", episodes.isNotEmpty())

        episodes.forEach { episode ->
            val cleaned = cleanEpisodeTitle(episode.title, episode.episodeNumber)

            // 1. Must not contain hashtags
            assertFalse("Cleaned title must not contain hashtags: $cleaned", cleaned.contains("#"))

            // 2. Must not start with duplicate episode number prefix (e.g. "حلقة 29" or "حلقة 12")
            episode.episodeNumber?.let { num ->
                assertFalse("Cleaned title must not contain 'حلقة $num': $cleaned", cleaned.startsWith("حلقة $num"))
            }

            // 3. Must not contain repeated ellipses
            assertFalse("Cleaned title must not contain '..': $cleaned", cleaned.contains(".."))

            // 4. Must preserve meaningful title content
            assertTrue("Cleaned title must not be blank", cleaned.isNotBlank())
        }
    }

    @Test
    fun cleanEpisodeTitle_specificEdgeCasesWithDotSpacing() {
        val ep29 = "حلقة 29 - حلقة الختام..أجواء ومشاعر رائعة.. كل عام وأنتم بخير .. #نبي_الإحسان"
        assertEquals("حلقة الختام. أجواء ومشاعر رائعة. كل عام وأنتم بخير.", cleanEpisodeTitle(ep29, 29))

        val ep12 = "حلقة 12/ مرض العصر.. التفكير الزائد والتوتر والقلق ( over thinking ) .. #نبي_الإحسان"
        assertEquals("مرض العصر. التفكير الزائد والتوتر والقلق ( over thinking ).", cleanEpisodeTitle(ep12, 12))
    }

    @Test
    fun azkarCounterFormat_xmlResourceHasUnambiguousArabicFormat() {
        // Read actual production string resource from values-ar/strings.xml
        val stringsXml = File("src/main/res/values-ar/strings.xml").takeIf { it.exists() }
            ?: File("feature/src/main/res/values-ar/strings.xml")

        assertTrue("Arabic strings.xml file must exist", stringsXml.exists())
        val content = stringsXml.readText()

        val expectedTag = "<string name=\"azkar_counter_format\">المكتمل: %1\$d من %2\$d</string>"
        assertTrue(
            "azkar_counter_format must match 'المكتمل: %1\$d من %2\$d'",
            content.contains(expectedTag)
        )
    }

    @Test
    fun missingAboutTextOrSummary_evaluatesIsNotBlankFalseForEmptyData() {
        // Nabi Ihsan default episode empty aboutText check
        val episode = NabiIhsanFixtureData.defaultEpisodes.first()
        assertEquals("", episode.aboutText)
        assertFalse("Empty aboutText must evaluate to false for isNotBlank()", episode.aboutText.isNotBlank())

        // Sanhya QuranStory empty summary check
        val emptyStory = QuranStory(
            id = "s_01",
            title = "عنوان",
            subtitle = "فرعي",
            category = "عبرة",
            coverUrl = "",
            summary = "",
            surahs = emptyList(),
            quranReferences = emptyList(),
            episodes = emptyList(),
            lessons = emptyList()
        )
        assertFalse("Empty summary must evaluate to false for isNotBlank()", emptyStory.summary.isNotBlank())
        assertFalse("Empty surahs label must evaluate to false for isNotBlank()", emptyStory.formattedSurahLabel.isNotBlank())
    }
}
