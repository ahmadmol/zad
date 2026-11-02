package com.example.feature.prayer.util

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.R
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
    fun bundledAdhanUriUsesAndroidResourceAndCurrentPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = AdhanNotificationChannelFactory.bundledAdhanUri(context)
        assertEquals(ContentResolver.SCHEME_ANDROID_RESOURCE, uri.scheme)
        assertEquals(context.packageName, uri.authority)
        assertTrue(uri.toString().contains(R.raw.adhan_default.toString()))
    }

    @Test
    fun resolveDefaultAthanUsesBundledResourceWhenNoCustomUri() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = AdhanNotificationChannelFactory.resolveSoundUri(
            context = context,
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null
        )
        assertNotNull(uri)
        assertEquals(AdhanNotificationChannelFactory.bundledAdhanUri(context), uri)
        assertTrue(uri!!.toString().startsWith("android.resource://"))
    }

    @Test
    fun resolveDefaultAthanPrefersValidCustomUri() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val custom = Settings.System.DEFAULT_ALARM_ALERT_URI.toString()
        val uri = AdhanNotificationChannelFactory.resolveSoundUri(
            context = context,
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = custom
        )
        assertEquals(custom, uri?.toString())
    }

    @Test
    fun ensureChannelCreatesHighImportanceAdhanV3Channel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val soundUri = AdhanNotificationChannelFactory.resolveSoundUri(
            context = context,
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null
        )
        val channelId = AdhanNotificationChannelFactory.ensureChannel(
            context = context,
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )

        assertTrue(channelId.startsWith("prayer_notifications_adhan_v3_"))
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
        assertTrue(channel.sound.toString().startsWith("android.resource://"))
    }

    @Test
    fun builtNotificationUsesAlarmCategory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val soundUri = AdhanNotificationChannelFactory.resolveSoundUri(
            context = context,
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null
        )
        val channelId = AdhanNotificationChannelFactory.ensureChannel(
            context = context,
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )
        val notification = AdhanNotificationChannelFactory.buildNotification(
            context = context,
            channelId = channelId,
            title = "اختبار أذان",
            message = "تحقق صوت الأذان المضمّن",
            soundType = "DEFAULT_ATHAN",
            soundUri = soundUri
        )
        assertEquals(NotificationCompat.CATEGORY_ALARM, notification.category)
        assertTrue(channelId.contains("adhan_v3"))
    }

    @Test
    fun showNotificationPostsOnAdhanV3Channel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationId = 424243
        AdhanNotificationChannelFactory.showNotification(
            context = context,
            title = "AdhanTest",
            message = "BundledAdhanCheck",
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null,
            notificationId = notificationId
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val expectedChannelId = AdhanNotificationChannelFactory.buildChannelId(
            "DEFAULT_ATHAN",
            AdhanNotificationChannelFactory.resolveSoundUri(context, "DEFAULT_ATHAN", null)
        )
        val channel = manager.getNotificationChannel(expectedChannelId)
        assertNotNull(channel)
        assertTrue(expectedChannelId.startsWith("prayer_notifications_adhan_v3_"))
        assertEquals(AudioAttributes.USAGE_ALARM, channel!!.audioAttributes.usage)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertNotNull(channel.sound)
    }
}
