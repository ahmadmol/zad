package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.domain.model.PrayerAlarmKind
import com.example.feature.prayer.domain.model.PrayerAlarmRequest
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSchedule
import com.example.feature.prayer.domain.model.PrayerSchedulePolicy
import java.time.Instant
import java.time.ZoneId

object PrayerScheduleBuilder {

    fun build(
        today: PrayerDay,
        tomorrow: PrayerDay,
        location: PrayerLocation,
        policy: PrayerSchedulePolicy,
        nowEpochMillis: Long,
        settingsFingerprint: String
    ): PrayerSchedule {
        val days = listOf(today, tomorrow)
        val alarms = mutableListOf<PrayerAlarmRequest>()

        days.forEach { day ->
            val instants = day.instants
            instants.forEachIndexed { index, instant ->
                val include = policy.includeSunrise || instant.name != PrayerName.SUNRISE
                if (!include) return@forEachIndexed

                val dayOfYear = Instant.ofEpochMilli(instant.epochMillis)
                    .atZone(ZoneId.of(day.timeZoneId))
                    .dayOfYear
                val baseId = stableBaseId(instant.name, dayOfYear)

                if (instant.name.isNotifiable && policy.prePrayerMinutes > 0) {
                    val trigger = instant.epochMillis - policy.prePrayerMinutes * 60_000L
                    if (trigger > nowEpochMillis) {
                        alarms += PrayerAlarmRequest(
                            stableId = baseId + OFFSET_PRE,
                            prayerName = instant.name,
                            kind = PrayerAlarmKind.PRE_PRAYER,
                            triggerEpochMillis = trigger,
                            title = "اقترب وقت صلاة ${instant.name.arabic}",
                            message = "بقي ${policy.prePrayerMinutes} دقائق على صلاة ${instant.name.arabic}"
                        )
                    }
                }

                if (instant.epochMillis > nowEpochMillis) {
                    alarms += PrayerAlarmRequest(
                        stableId = baseId + OFFSET_EXACT,
                        prayerName = instant.name,
                        kind = PrayerAlarmKind.EXACT,
                        triggerEpochMillis = instant.epochMillis,
                        title = if (instant.name == PrayerName.SUNRISE) "وقت الشروق"
                        else "حان الآن موعد صلاة ${instant.name.arabic}",
                        message = if (instant.name == PrayerName.SUNRISE) "حان الآن وقت شروق الشمس"
                        else "الله أكبر، حان وقت صلاة ${instant.name.arabic}"
                    )
                }

                if (instant.name.isNotifiable && policy.iqamahMinutes > 0) {
                    val trigger = instant.epochMillis + policy.iqamahMinutes * 60_000L
                    if (trigger > nowEpochMillis) {
                        alarms += PrayerAlarmRequest(
                            stableId = baseId + OFFSET_IQAMAH,
                            prayerName = instant.name,
                            kind = PrayerAlarmKind.IQAMAH,
                            triggerEpochMillis = trigger,
                            title = "حان وقت إقامة صلاة ${instant.name.arabic}",
                            message = "استعدوا، حان وقت إقامة الصلاة"
                        )
                    }
                }

                if (instant.name.isNotifiable) {
                    val next = instants.getOrNull(index + 1)
                    if (next != null) {
                        val trigger = next.epochMillis - policy.endOfPrayerReminderMinutesBeforeNext * 60_000L
                        if (trigger > nowEpochMillis && trigger > instant.epochMillis) {
                            alarms += PrayerAlarmRequest(
                                stableId = baseId + OFFSET_END,
                                prayerName = instant.name,
                                kind = PrayerAlarmKind.END_REMINDER,
                                triggerEpochMillis = trigger,
                                title = "تذكير بالصلاة",
                                message = "تذكير لمن لم يصلِّ ${instant.name.arabic}"
                            )
                        }
                    }
                }
            }
        }

        return PrayerSchedule(
            dateEpochDay = today.dateEpochDay,
            timeZoneId = today.timeZoneId,
            location = location,
            settingsFingerprint = settingsFingerprint,
            policy = policy,
            alarms = alarms.sortedBy { it.triggerEpochMillis }
        )
    }

    fun stableBaseId(name: PrayerName, dayOfYear: Int): Int =
        name.english.hashCode() + (dayOfYear * 10)

    fun settingsFingerprint(
        method: String,
        madhhab: String,
        pre: Int,
        iqamah: Int,
        lat: Double,
        lng: Double
    ): String = "$method|$madhhab|$pre|$iqamah|${"%.5f".format(lat)}|${"%.5f".format(lng)}"

    private const val OFFSET_PRE = 10_000
    private const val OFFSET_EXACT = 20_000
    private const val OFFSET_IQAMAH = 30_000
    private const val OFFSET_END = 40_000
}
