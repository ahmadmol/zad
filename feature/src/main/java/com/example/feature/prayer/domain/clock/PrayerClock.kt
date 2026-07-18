package com.example.feature.prayer.domain.clock

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Thin wrapper so domain logic can inject a fixed clock in tests.
 */
class PrayerClock(private val clock: Clock = Clock.systemDefaultZone()) {
    fun nowEpochMillis(): Long = Instant.now(clock).toEpochMilli()

    fun zoneId(): ZoneId = clock.zone

    fun today(): LocalDate = LocalDate.now(clock)

    fun withZone(zoneId: ZoneId): PrayerClock = PrayerClock(clock.withZone(zoneId))
}
