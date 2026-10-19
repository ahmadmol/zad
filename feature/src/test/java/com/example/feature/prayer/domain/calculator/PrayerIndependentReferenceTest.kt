package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Independent Aladhan method3/school0 HTTP fixtures, fetched2026-10-14; no network in tests. */
class PrayerIndependentReferenceTest {
    @Test fun `MWL calculation agrees within three minutes in Aleppo and across London DST change`() {
        val calculator=AdhanPrayerCalculator()
        listOf("aleppo","london-before-dst","london-after-dst").forEach { name ->
            val raw=javaClass.classLoader!!.getResourceAsStream("prayer-reference/$name.json")!!.bufferedReader().use { it.readText() }
            val data=Json.parseToJsonElement(raw).jsonObject.getValue("data").jsonObject
            val meta=data.getValue("meta").jsonObject
            assertEquals(3,meta.getValue("method").jsonObject.getValue("id").jsonPrimitive.int)
            val gregorian=data.getValue("date").jsonObject.getValue("gregorian").jsonObject.getValue("date").jsonPrimitive.content
            val date=LocalDate.parse(gregorian,DateTimeFormatter.ofPattern("dd-MM-uuuu"))
            val zone=ZoneId.of(meta.getValue("timezone").jsonPrimitive.content)
            val location=PrayerLocation(meta.getValue("latitude").jsonPrimitive.double,meta.getValue("longitude").jsonPrimitive.double,name)
            val day=calculator.calculate(date,location,PrayerCalculationSettings(method=PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE,madhhab=PrayerMadhhab.SHAFI),zone.id,0)
            val timings=data.getValue("timings").jsonObject
            PrayerName.entries.forEach { prayer ->
                val expectedTime=LocalTime.parse(timings.getValue(prayer.english).jsonPrimitive.content.take(5))
                val expected=date.atTime(expectedTime).atZone(zone).toInstant().toEpochMilli()
                val actual=day.instant(prayer)!!.epochMillis
                assertTrue("$name/$prayer: actual=${Instant.ofEpochMilli(actual).atZone(zone)} expected=$expectedTime",abs(actual-expected)<=180_000L)
            }
        }
    }
}
