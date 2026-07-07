package com.example.feature.prayer.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.feature.prayer.PrayerTime
import com.example.feature.prayer.worker.PrayerNotificationReceiver
import java.util.Calendar

class PrayerNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedulePrayerNotifications(
        prayers: List<PrayerTime>,
        prePrayerMinutes: Int,
        iqamahMinutes: Int
    ) {
        val now = System.currentTimeMillis()

        prayers.forEach { prayer ->
            // Skip Sunrise
            if (prayer.nameEn == "Sunrise") return@forEach

            // Generate unique day-based ID to avoid collisions between today and tomorrow
            val calendar = Calendar.getInstance().apply { timeInMillis = prayer.timestamp }
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val baseId = prayer.nameEn.hashCode() + (dayOfYear * 10)

            // 1. Pre-prayer notification
            if (prePrayerMinutes > 0) {
                val triggerTime = prayer.timestamp - (prePrayerMinutes * 60 * 1000)
                if (triggerTime > now) {
                    scheduleAlarm(
                        id = baseId + 10000,
                        triggerTime = triggerTime,
                        title = "اقترب وقت صلاة ${prayer.nameAr}",
                        message = "بقي $prePrayerMinutes دقائق على صلاة ${prayer.nameAr}"
                    )
                }
            }

            // 2. Exact Prayer time
            if (prayer.timestamp > now) {
                scheduleAlarm(
                    id = baseId + 20000,
                    triggerTime = prayer.timestamp,
                    title = "حان الآن موعد صلاة ${prayer.nameAr}",
                    message = "الله أكبر، حان وقت صلاة ${prayer.nameAr}"
                )
            }

            // 3. Iqamah notification
            if (iqamahMinutes > 0) {
                val triggerTime = prayer.timestamp + (iqamahMinutes * 60 * 1000)
                if (triggerTime > now) {
                    scheduleAlarm(
                        id = baseId + 30000,
                        triggerTime = triggerTime,
                        title = "حان وقت إقامة صلاة ${prayer.nameAr}",
                        message = "استعدوا، حان وقت إقامة الصلاة"
                    )
                }
            }
        }
    }

    private fun scheduleAlarm(id: Int, triggerTime: Long, title: String, message: String) {
        // Safe check for exact alarms on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If we can't schedule exact, we might fallback to non-exact or just skip
                // For now we'll try to set it, but in a production app we should prompt the user
            }
        }

        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Handle case where SCHEDULE_EXACT_ALARM is not granted (Android 14+)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
