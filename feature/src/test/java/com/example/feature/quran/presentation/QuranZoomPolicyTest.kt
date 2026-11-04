package com.example.feature.quran.presentation

import com.example.feature.quran.util.OfficialRecitationLink
import com.example.feature.quran.util.RecitationAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranZoomPolicyTest {
    @Test fun `pinch movement does not commit a new measurement until the gesture scale is applied once`() {
        assertEquals(28f, QuranZoomPolicy.committedFontSize(28f, 1f), 0.01f)
        assertEquals(42f, QuranZoomPolicy.committedFontSize(28f, 1.5f), 0.01f)
        assertEquals(20f, QuranZoomPolicy.committedFontSize(18f, 0.5f), 0.01f)
        assertEquals(48f, QuranZoomPolicy.committedFontSize(40f, 2f), 0.01f)
    }

    @Test fun `one finger at the fitted size does not block the horizontal pager`() {
        assertFalse(QuranZoomPolicy.blocksHorizontalPaging(1f, 1))
        assertTrue(QuranZoomPolicy.blocksHorizontalPaging(1f, 2))
        assertTrue(QuranZoomPolicy.blocksHorizontalPaging(1.2f, 1))
    }
}

class OfficialRecitationLinkTest {
    @Test fun `external verse link addresses the exact surah and ayah and playback stays off`() {
        assertEquals("https://quran.com/4/50", OfficialRecitationLink.quranComVerse(4, 50))
        assertNull(OfficialRecitationLink.quranComVerse(0, 50))
        assertNull(OfficialRecitationLink.quranComVerse(4, 0))
        assertFalse(RecitationAvailability.RECORDINGS_APPROVED)
        assertTrue(OfficialRecitationLink.OWNER_APPROVAL.isNotBlank())
    }
}
