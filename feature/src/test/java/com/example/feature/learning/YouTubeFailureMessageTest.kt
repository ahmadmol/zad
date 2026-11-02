package com.example.feature.learning

import com.example.feature.learning.presentation.components.youtubeFailureMessage
import org.junit.Assert.*
import org.junit.Test

class YouTubeFailureMessageTest {
    @Test fun `embed restriction and client identity failures offer truthful official fallback`() {
        assertEquals(youtubeFailureMessage(101), youtubeFailureMessage(150))
        assertTrue(youtubeFailureMessage(101).contains("لا يسمح الناشر"))
        assertTrue(youtubeFailureMessage(153).contains("هوية المشغل"))
        assertTrue(youtubeFailureMessage(100).contains("خاصًا"))
        assertFalse(youtubeFailureMessage(999).contains("مؤكد"))
    }
}
