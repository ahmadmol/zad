package com.example.feature.duas.presentation

import org.junit.Assert.*
import org.junit.Test

class DuaSectionsTest {
    @Test fun `prophetic section includes both existing sunnah categories`() {
        assertTrue(DuaSections.matches("أدعية نبوية", "من السنة النبوية"))
        assertTrue(DuaSections.matches("أدعية نبوية", "أدعية من السنة"))
        assertFalse(DuaSections.matches("أدعية من القرآن الكريم", "من السنة النبوية"))
    }
    @Test fun `missing sections stay empty and legacy category selections remain valid`() {
        assertFalse(DuaSections.matches("أدعية الصباح", "الرزق"))
        assertFalse(DuaSections.matches("أدعية للأهل والأصدقاء", "الهم والحزن"))
        assertTrue(DuaSections.matches("السفر", "السفر"))
        assertTrue(DuaSections.matches("أدعية في الشدائد", "الهم والحزن"))
    }
}
