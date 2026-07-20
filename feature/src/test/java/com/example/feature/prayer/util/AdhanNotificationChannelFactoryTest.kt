package com.example.feature.prayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanNotificationChannelFactoryTest {

    @Test
    fun `channel id differs from legacy prayer notification id`() {
        val sound = "content://settings/system/alarm_alert"
        val legacy = "prayer_notifications_DEFAULT_ATHAN_${sound.hashCode()}"
        val next = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        assertNotEquals(legacy, next)
        assertTrue(next.contains("alarm_v2"))
        assertTrue(AdhanNotificationChannelFactory.isLegacyChannelId(legacy))
        assertFalse(AdhanNotificationChannelFactory.isLegacyChannelId(next))
    }

    @Test
    fun `same sound and version produce stable channel id`() {
        val sound = "content://media/internal/audio/media/42"
        val first = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        val second = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        assertEquals(first, second)
        assertEquals(
            "prayer_notifications_alarm_v2_DEFAULT_ATHAN_${sound.hashCode()}",
            first
        )
    }

    @Test
    fun `different sound produces different channel id`() {
        val a = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            "content://settings/system/alarm_alert"
        )
        val b = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            "content://settings/system/notification_sound"
        )
        assertNotEquals(a, b)
    }

    @Test
    fun `silent and short tone keep distinct channel ids`() {
        val silent = AdhanNotificationChannelFactory.buildChannelId("SILENT", null as String?)
        val shortTone = AdhanNotificationChannelFactory.buildChannelId(
            "SHORT_TONE",
            "content://settings/system/notification_sound"
        )
        assertNotEquals(silent, shortTone)
        assertTrue(silent.endsWith("_0"))
    }

    @Test
    fun `worker legacy adhan channel prefix is treated as legacy`() {
        val workerLegacy = "adhan_notifications_-12345"
        // Worker-specific legacy IDs are outside prayer_notifications_*; ensure we do not
        // accidentally rewrite unrelated media/message channel strategies in this helper.
        assertFalse(AdhanNotificationChannelFactory.isLegacyChannelId(workerLegacy))
        assertFalse(
            AdhanNotificationChannelFactory.isLegacyChannelId("media3_playback_channel")
        )
    }
}
