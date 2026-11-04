package com.example.feature.prayer.domain.scheduler

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerAlarmDeliveryPolicyTest {
    @Test fun `a broadcast arriving inside the grace window is delivered`() {
        val trigger = 1_700_000_000_000L
        assertTrue(PrayerAlarmDeliveryPolicy.shouldDeliver(trigger, trigger + 60_000L))
    }

    @Test fun `a broadcast arriving long after Asr is dropped as a stale task`() {
        val trigger = 1_700_000_000_000L
        assertFalse(PrayerAlarmDeliveryPolicy.shouldDeliver(trigger, trigger + 20 * 60_000L))
    }

    @Test fun `missing trigger time is never delivered`() {
        assertFalse(PrayerAlarmDeliveryPolicy.shouldDeliver(0L, 1_700_000_000_000L))
    }

    @Test fun `delivery ends exactly at fifteen minutes and a broadcast before the trigger is dropped`() {
        val trigger = 1_700_000_000_000L
        assertTrue(PrayerAlarmDeliveryPolicy.shouldDeliver(trigger, trigger + PrayerAlarmDeliveryPolicy.LATE_GRACE_MILLIS))
        assertFalse(PrayerAlarmDeliveryPolicy.shouldDeliver(trigger, trigger + PrayerAlarmDeliveryPolicy.LATE_GRACE_MILLIS + 1))
        assertFalse(PrayerAlarmDeliveryPolicy.shouldDeliver(trigger, trigger - 1))
    }
}

class AsrMadhhabReferenceTest {
    @Test fun `Hanafi Asr in Aleppo is later than Shafi and both stay on the same calculation method`() {
        val calculator = AdhanPrayerCalculator()
        val location = PrayerLocation(36.2021, 37.1343, "aleppo")
        val date = LocalDate.of(2026, 10, 14)
        val zone = ZoneId.of("Asia/Damascus").id
        val shafi = calculator.calculate(date, location, PrayerCalculationSettings(
            method = PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE, madhhab = PrayerMadhhab.SHAFI
        ), zone, 0).instant(PrayerName.ASR)!!.epochMillis
        val hanafi = calculator.calculate(date, location, PrayerCalculationSettings(
            method = PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE, madhhab = PrayerMadhhab.HANAFI
        ), zone, 0).instant(PrayerName.ASR)!!.epochMillis
        val minutes = (hanafi - shafi) / 60_000L
        assertTrue("Hanafi shadow factor must place Asr later, was $minutes min", minutes in 40..90)
    }
}
