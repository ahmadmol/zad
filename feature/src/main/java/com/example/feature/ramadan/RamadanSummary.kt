package com.example.feature.ramadan

import com.example.feature.prayer.domain.model.*
import com.example.feature.quran.domain.model.ReadingPlan
import java.time.LocalDate
import java.time.ZoneId

data class RamadanPrayerSummary(val fajrMillis: Long? = null, val maghribMillis: Long? = null,
    val zoneId: String? = null, val locationName: String = "", val loading: Boolean = false,
    val message: String? = null)

/** Projects the facade's exact instants. No calculator, Hijri offset or additional religious boundary. */
object RamadanSummary {
    fun shouldHighlight(enabled: Boolean, calculatedMonth: Int?): Boolean = enabled && calculatedMonth==9
    fun prayer(day: PrayerDay?, location: PrayerLocationState, today: LocalDate, zone: ZoneId): RamadanPrayerSummary {
        if (location is PrayerLocationState.Loading) return RamadanPrayerSummary(loading=true)
        if (location !is PrayerLocationState.Available) return RamadanPrayerSummary(
            message="الموقع غير متاح. افتح المواقيت لاختيار مدينة أو مراجعة إذن الموقع.")
        if (day == null || day.dateEpochDay != today.toEpochDay() || day.timeZoneId != zone.id || day.location != location.location)
            return RamadanPrayerSummary(message="مواقيت اليوم غير متاحة أو يجري تحديثها. أعد المحاولة أو افتح المواقيت.")
        return RamadanPrayerSummary(day.instant(PrayerName.FAJR)?.epochMillis,
            day.instant(PrayerName.MAGHRIB)?.epochMillis, day.timeZoneId, day.location.displayName,
            message=if(day.instant(PrayerName.FAJR)==null || day.instant(PrayerName.MAGHRIB)==null)
                "بعض المواقيت غير متاحة من المصدر الحالي." else null)
    }

    fun confirmedPages(plan: ReadingPlan, recorded: Set<Int>): Int = plan.pageCount - plan.remaining(recorded).size
}
