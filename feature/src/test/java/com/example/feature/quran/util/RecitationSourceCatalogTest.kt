package com.example.feature.quran.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecitationSourceCatalogTest {
    @Test fun `reviewed sources stay documented and playback stays disabled`() {
        assertEquals(3, RecitationSourceCatalog.reviewed.size)
        assertTrue(RecitationSourceCatalog.reviewed.all { it.documentationUrl.startsWith("https://") })
        assertFalse(RecitationAvailability.RECORDINGS_APPROVED)
        assertTrue(RecitationSourceCatalog.reviewed.single { it.name == "mp3quran.net" }.decision.contains("Rejected"))
    }
}
