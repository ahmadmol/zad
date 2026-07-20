package com.example.feature.prayer.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdhanNotificationChannelFactoryInstrumentedTest {

    @Test
    fun alarmAudioAttributesUseAlarmUsageAndSonification() {
        val attrs = AdhanNotificationChannelFactory.buildAlarmAudioAttributes()
        assertEquals(AudioAttributes.USAGE_ALARM, attrs.usage)
        assertEquals(AudioAttributes.CONTENT_TYPE_SONIFICATION, attrs.contentType)
    }

    @Test
    fun resolveDefaultAthanFallsBackToSystemAlarmUri() {
        val uri = AdhanNotificationChannelFactory.resolveSoundUri("DEFAULT_ATHAN", null)
        assertNotNull(uri)
        assertEquals(Settings.System.DEFAULT_ALARM_ALERT_URI, uri)
    }

    @Test
    fun ensureChannelCreatesHighImportanceAlarmChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val soundUri = AdhanNotificationChannelFactory.resolveSoundUri("DEFAULT_ATHAN", null)
        val channelId = AdhanNotificationChannelFactory.ensureChannel(
            context = context,
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId)
        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel!!.importance)
        assertNotNull(channel.sound)
        assertEquals(AudioAttributes.USAGE_ALARM, channel.audioAttributes.usage)
        assertEquals(
            AudioAttributes.CONTENT_TYPE_SONIFICATION,
            channel.audioAttributes.contentType
        )
    }

    @Test
    fun builtNotificationUsesAlarmCategory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val soundUri = AdhanNotificationChannelFactory.resolveSoundUri("DEFAULT_ATHAN", null)
        val channelId = AdhanNotificationChannelFactory.ensureChannel(
            context = context,
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )
        val notification = AdhanNotificationChannelFactory.buildNotification(
            context = context,
            channelId = channelId,
            title = "اختبار أذان",
            message = "تحقق صوت المنبّه",
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )
        assertEquals(NotificationCompat.CATEGORY_ALARM, notification.category)
        assertTrue(channelId.contains("alarm_v2"))
    }

    @Test
    fun showNotificationPostsOnAlarmChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationId = 424242
        AdhanNotificationChannelFactory.showNotification(
            context = context,
            title = "AdhanTest",
            message = "AlarmUsageCheck",
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null,
            notificationId = notificationId
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val expectedChannelId = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            AdhanNotificationChannelFactory.resolveSoundUri("DEFAULT_ATHAN", null)
        )
        val channel = manager.getNotificationChannel(expectedChannelId)
        assertNotNull(channel)
        assertEquals(AudioAttributes.USAGE_ALARM, channel!!.audioAttributes.usage)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertNotNull(channel.sound)
    }
}
