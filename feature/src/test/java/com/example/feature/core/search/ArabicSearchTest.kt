package com.example.feature.core.search

import org.junit.Assert.*
import org.junit.Test

class ArabicSearchTest {
    @Test fun `uthmani and plain Arabic match without changing the source`() {
        val original = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ"
        assertTrue(ArabicSearch.matches(original, "بسم الله الرحمن"))
        assertEquals("بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ", original)
    }
    @Test fun `marks tatweel alif variants and word order are search keys only`() {
        assertTrue(ArabicSearch.matches("إِحْسَــان إلى الناس", "الناس احسان"))
        assertFalse(ArabicSearch.matches("الرحمن", "الرحيم"))
        assertFalse(ArabicSearch.matches("القرآن", "%"))
    }
}
