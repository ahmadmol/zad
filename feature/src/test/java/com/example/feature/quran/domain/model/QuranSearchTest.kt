package com.example.feature.quran.domain.model

import com.example.feature.core.search.ArabicSearch
import org.junit.Assert.*
import org.junit.Test

class QuranSearchTest {
    @Test fun ordinarySpellingFindsDaggerAlifWithoutChangingQuranText() {
        val source = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ"
        assertTrue(QuranSearch.expandedVerseKey(source).contains("العالمين"))
        assertTrue(QuranSearch.expandedVerseKey("مَٰلِكِ يَوْمِ ٱلدِّينِ").contains("مالك"))
        assertEquals("داود", ArabicSearch.key("دَاوُۥدَ"))
    }
    @Test fun chapterIntentFindsVocalizedNames() {
        assertTrue(QuranSearch.matchesSurah("الكَهْف", "سورة الكهف"))
        assertTrue(QuranSearch.matchesSurah("الفاتحة", "الفَاتِحَة"))
        assertTrue(QuranSearch.matchesSurah("الكهف", "سوره الكهف"))
        assertFalse(QuranSearch.matchesSurah("البقرة", "سورة الكهف"))
        assertFalse(QuranSearch.matchesSurah("الكهف", "سورة"))
        assertFalse(QuranSearch.matchesSurah("الكهف", "دعاء السفر"))
    }
    @Test fun emptyNormalizedQueriesNeverMatchWholeCorpus() {
        assertFalse(ArabicSearch.matches("نص المصدر", "َُِ"))
        assertTrue(ArabicSearch.matches("نص المصدر", "   "))
    }
}
