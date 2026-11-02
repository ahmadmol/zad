package com.example.feature.prayer.domain.clock

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Thin wrapper so domain logic can inject a fixed clock in tests.
 */
class PrayerClock(private val clock: Clock? = null) {
    fun nowEpochMillis(): Long = (clock?.instant() ?: Instant.now()).toEpochMilli()

    fun zoneId(): ZoneId = clock?.zone ?: ZoneId.systemDefault()

    fun today(): LocalDate = Instant.ofEpochMilli(nowEpochMillis()).atZone(zoneId()).toLocalDate()

    fun withZone(zoneId: ZoneId): PrayerClock = PrayerClock((clock ?: Clock.systemUTC()).withZone(zoneId))
}
