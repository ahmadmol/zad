package com.example.feature.prayer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.feature.R

/**
 * Shared adhan/prayer notification channel + builder helpers.
 *
 * Channel ID versioning is required because NotificationChannel sound and
 * AudioAttributes are immutable after the channel is first created.
 * v3 migrates the channel sound from the system alarm tone to the bundled
 * [R.raw.adhan_default] recording while keeping USAGE_ALARM.
 */
object AdhanNotificationChannelFactory {

    const val CHANNEL_NAME = "مواقيت الصلاة"
    const val CHANNEL_DESCRIPTION = "تنبيهات الصلاة بصوت الأذان المضمّن عبر مسار المنبّه"
    const val CHANNEL_USAGE_TAG = "adhan"
    const val CHANNEL_VERSION = "v3"

    /** Legacy channel prefix used before bundled-adhan routing. */
    const val LEGACY_CHANNEL_PREFIX = "prayer_notifications_"

    fun buildChannelId(soundType: String, soundUri: Uri?): String {
        return buildChannelId(soundType, soundUri?.toString())
    }

    fun buildChannelId(soundType: String, soundUriString: String?): String {
        val uriKey = soundUriString?.hashCode() ?: 0
        // Versioning required: channel AudioAttributes/sound cannot be changed in place.
        // v3 is required to migrate the channel to the bundled adhan recording.
        return "prayer_notifications_${CHANNEL_USAGE_TAG}_${CHANNEL_VERSION}_${soundType}_$uriKey"
    }

    fun isLegacyChannelId(channelId: String): Boolean {
        if (!channelId.startsWith(LEGACY_CHANNEL_PREFIX)) return false
        val versionMarker = "${CHANNEL_USAGE_TAG}_$CHANNEL_VERSION"
        return !channelId.contains("_${versionMarker}_")
    }

    fun buildAlarmAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    fun bundledAdhanUri(context: Context): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .appendPath(R.raw.adhan_default.toString())
            .build()
    }

    fun resolveSoundUri(
        context: Context,
        soundType: String,
        customAdhanUri: String?
    ): Uri? {
        return when (soundType) {
            "SILENT" -> null
            "SHORT_TONE" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "DEFAULT_ATHAN" -> resolveAdhanUri(context, customAdhanUri)
            else -> resolveAdhanUri(context, customAdhanUri)
        }
    }

    private fun resolveAdhanUri(context: Context, customAdhanUri: String?): Uri? {
        parseValidCustomUri(customAdhanUri)?.let { return it }
        return runCatching { bundledAdhanUri(context) }.getOrNull()
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

    private fun parseValidCustomUri(customAdhanUri: String?): Uri? {
        val raw = customAdhanUri?.trim().orEmpty()
        if (raw.isEmpty()) return null
        return runCatching { Uri.parse(raw) }
            .getOrNull()
            ?.takeIf { it.scheme != null }
    }

    fun ensureChannel(
        context: Context,
        soundType: String,
        soundUri: Uri?
    ): String {
        val channelId = buildChannelId(soundType, soundUri)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return channelId
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = if (soundType == "SILENT") {
            NotificationManager.IMPORTANCE_LOW
        } else {
            NotificationManager.IMPORTANCE_HIGH
        }

        val channel = NotificationChannel(channelId, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(soundType != "SILENT")
            if (soundType == "SILENT" || soundUri == null) {
                setSound(null, null)
            } else {
                setSound(soundUri, buildAlarmAudioAttributes())
            }
        }
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    fun buildNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        soundType: String,
        soundUri: Uri?
    ): android.app.Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                if (soundType == "SILENT") NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        when {
            soundType == "SILENT" -> builder.setSilent(true)
            soundUri != null -> {
                builder.setSound(soundUri)
                builder.setDefaults(
                    NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS
                )
            }
            else -> builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        return builder.build()
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        soundType: String,
        customAdhanUri: String?,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = resolveSoundUri(context, soundType, customAdhanUri)
        val channelId = ensureChannel(context, soundType, soundUri)
        val notification = buildNotification(
            context = context,
            channelId = channelId,
            title = title,
            message = message,
            soundType = soundType,
            soundUri = soundUri
        )
        notificationManager.notify(notificationId, notification)
    }
}
