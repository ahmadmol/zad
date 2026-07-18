package com.example.feature.prayer.data.calculator

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerName
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AdhanPrayerCalculator : PrayerCalculator {

    override fun calculate(
        date: LocalDate,
        location: PrayerLocation,
        settings: PrayerCalculationSettings,
        timeZoneId: String,
        nowEpochMillis: Long
    ): PrayerDay {
        val coordinates = Coordinates(location.latitude, location.longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val method = mapMethod(settings.method)
        val madhab = mapMadhab(settings.madhhab)
        val params = method.parameters.copy(madhab = madhab)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val instants = listOf(
            PrayerName.FAJR to prayerTimes.fajr,
            PrayerName.SUNRISE to prayerTimes.sunrise,
            PrayerName.DHUHR to prayerTimes.dhuhr,
            PrayerName.ASR to prayerTimes.asr,
            PrayerName.MAGHRIB to prayerTimes.maghrib,
            PrayerName.ISHA to prayerTimes.isha
        ).map { (name, instant) ->
            val offsetMillis = TimeUnit.MINUTES.toMillis(settings.offsets.minutesFor(name).toLong())
            PrayerInstant(
                name = name,
                epochMillis = instant.toEpochMilliseconds() + offsetMillis
            )
        }

        return PrayerDay(
            dateEpochDay = date.toEpochDay(),
            timeZoneId = timeZoneId,
            location = location,
            settings = settings,
            instants = instants
        )
    }

    companion object {
        fun mapMethod(method: PrayerCalculationMethod): CalculationMethod = when (method) {
            PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            PrayerCalculationMethod.EGYPTIAN -> CalculationMethod.EGYPTIAN
            PrayerCalculationMethod.KARACHI -> CalculationMethod.KARACHI
            PrayerCalculationMethod.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA
            PrayerCalculationMethod.DUBAI -> CalculationMethod.DUBAI
            PrayerCalculationMethod.MOON_SIGHTING_COMMITTEE -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            PrayerCalculationMethod.NORTH_AMERICA -> CalculationMethod.NORTH_AMERICA
            PrayerCalculationMethod.KUWAIT -> CalculationMethod.KUWAIT
            PrayerCalculationMethod.QATAR -> CalculationMethod.QATAR
            PrayerCalculationMethod.SINGAPORE -> CalculationMethod.SINGAPORE
            PrayerCalculationMethod.OTHER -> CalculationMethod.OTHER
        }

        fun mapMadhab(madhhab: PrayerMadhhab): Madhab = when (madhhab) {
            PrayerMadhhab.SHAFI -> Madhab.SHAFI
            PrayerMadhhab.HANAFI -> Madhab.HANAFI
        }

        fun parseMethod(raw: String): PrayerCalculationMethod =
            runCatching { PrayerCalculationMethod.valueOf(raw) }
                .getOrDefault(PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE)

        fun parseMadhhab(raw: String): PrayerMadhhab =
            runCatching { PrayerMadhhab.valueOf(raw) }
                .getOrDefault(PrayerMadhhab.SHAFI)
    }
}
