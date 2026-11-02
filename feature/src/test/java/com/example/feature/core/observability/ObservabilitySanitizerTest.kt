package com.example.feature.core.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservabilitySanitizerTest {

    @Test
    fun `sanitizes phone email and coordinates`() {
        val raw = "user 0501234567 mail a@b.com at 33.513800 near case"
        val cleaned = ObservabilitySanitizer.sanitize(raw)
        assertFalse(cleaned.contains("0501234567"))
        assertFalse(cleaned.contains("a@b.com"))
        assertFalse(cleaned.contains("33.513800"))
        assertTrue(cleaned.contains("[redacted-phone]"))
        assertTrue(cleaned.contains("[redacted-email]"))
        assertTrue(cleaned.contains("[redacted-coord]"))
    }

    @Test
    fun `assertSafeAttributes rejects forbidden keys`() {
        try {
            ObservabilitySanitizer.assertSafeAttributes(mapOf("phone" to "x"))
            throw AssertionError("expected failure")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("phone"))
        }
    }

    @Test
    fun `operational event names stay non personal`() {
        val event = AppOperationalEvent(
            name = "prayer_schedule_failed",
            category = AppOperationalCategory.PrayerScheduling,
            attributes = mapOf("reason" to "SETTINGS_CHANGED")
        )
        ObservabilitySanitizer.assertSafeAttributes(event.attributes)
        assertEquals("prayer_schedule_failed", event.name)
    }

    @Test
    fun `noop logger does not throw`() {
        NoOpAppLogger.debug("t", "m")
        NoOpAppLogger.warning("t", "m")
        NoOpAppLogger.error("t", "m", null)
        NoOpAppEventReporter.report(
            AppOperationalEvent("x", AppOperationalCategory.ReleaseGate)
        )
    }
}
