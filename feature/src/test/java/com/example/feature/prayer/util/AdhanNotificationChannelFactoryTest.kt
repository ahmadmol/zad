package com.example.feature.prayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanNotificationChannelFactoryTest {

    @Test
    fun `channel id uses adhan v3 and differs from legacy ids`() {
        val sound = "android.resource://com.example.mol/2131820544"
        val legacyV1 = "prayer_notifications_DEFAULT_ATHAN_${sound.hashCode()}"
        val legacyV2 = "prayer_notifications_alarm_v2_DEFAULT_ATHAN_${sound.hashCode()}"
        val next = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        assertNotEquals(legacyV1, next)
        assertNotEquals(legacyV2, next)
        assertTrue(next.startsWith("prayer_notifications_adhan_v3_"))
        assertTrue(AdhanNotificationChannelFactory.isLegacyChannelId(legacyV1))
        assertTrue(AdhanNotificationChannelFactory.isLegacyChannelId(legacyV2))
        assertFalse(AdhanNotificationChannelFactory.isLegacyChannelId(next))
    }

    @Test
    fun `same sound and version produce stable channel id`() {
        val sound = "android.resource://com.example.mol/2131820544"
        val first = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        val second = AdhanNotificationChannelFactory.buildChannelId("DEFAULT_ATHAN", sound)
        assertEquals(first, second)
        assertEquals(
            "prayer_notifications_adhan_v3_DEFAULT_ATHAN_${sound.hashCode()}",
            first
        )
    }

    @Test
    fun `different sound produces different channel id`() {
        val a = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            "android.resource://com.example.mol/1"
        )
        val b = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            "content://settings/system/alarm_alert"
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
        assertTrue(silent.contains("adhan_v3"))
    }

    @Test
    fun `unrelated channel strategies are not treated as prayer legacy`() {
        assertFalse(AdhanNotificationChannelFactory.isLegacyChannelId("adhan_notifications_-12345"))
        assertFalse(
            AdhanNotificationChannelFactory.isLegacyChannelId("media3_playback_channel")
        )
    }
}
