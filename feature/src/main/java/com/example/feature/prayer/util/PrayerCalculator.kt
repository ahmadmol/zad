package com.example.feature.prayer.util

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.example.feature.prayer.PrayerTime
import java.text.SimpleDateFormat
import java.util.*

@OptIn(kotlin.time.ExperimentalTime::class)
object PrayerCalculator {

    fun calculate(
        lat: Double, 
        lng: Double, 
        date: Date = Date(),
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI
    ): List<PrayerTime> {
        val coordinates = Coordinates(lat, lng)
        val calendar = Calendar.getInstance().apply { time = date }
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        val params = method.parameters.copy(madhab = madhab)
        
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        return listOf(
            createPrayerTime("الفجر", "Fajr", prayerTimes.fajr, currentTime, timeFormat),
            createPrayerTime("الشروق", "Sunrise", prayerTimes.sunrise, currentTime, timeFormat),
            createPrayerTime("الظهر", "Dhuhr", prayerTimes.dhuhr, currentTime, timeFormat),
            createPrayerTime("العصر", "Asr", prayerTimes.asr, currentTime, timeFormat),
            createPrayerTime("المغرب", "Maghrib", prayerTimes.maghrib, currentTime, timeFormat),
            createPrayerTime("العشاء", "Isha", prayerTimes.isha, currentTime, timeFormat)
        )
    }

    private fun createPrayerTime(
        nameAr: String,
        nameEn: String,
        instant: kotlin.time.Instant,
        currentTime: Long,
        format: SimpleDateFormat
    ): PrayerTime {
        val prayerTimeMillis = instant.toEpochMilliseconds()
        val date = Date(prayerTimeMillis)
        val isActive = currentTime >= prayerTimeMillis && currentTime < (prayerTimeMillis + 45 * 60 * 1000)
        
        return PrayerTime(
            nameAr = nameAr,
            nameEn = nameEn,
            time = format.format(date),
            timestamp = prayerTimeMillis,
            isPast = currentTime > prayerTimeMillis && !isActive,
            isActive = isActive
        )
    }

    fun getMethodFromString(methodName: String): CalculationMethod {
        return try {
            CalculationMethod.valueOf(methodName)
        } catch (e: Exception) {
            CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    fun getMadhabFromString(madhabName: String): Madhab {
        return try {
            Madhab.valueOf(madhabName)
        } catch (e: Exception) {
            Madhab.SHAFI
        }
    }
}
