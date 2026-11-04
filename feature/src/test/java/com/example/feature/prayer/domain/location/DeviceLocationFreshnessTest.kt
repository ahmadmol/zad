package com.example.feature.prayer.domain.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceLocationFreshnessTest {
    private val now = 1_700_000_000_000L

    @Test fun `a fix from the last thirty minutes can be used`() {
        assertTrue(DeviceLocationFreshness.isFresh(now - 29 * 60_000L, now))
        assertTrue(DeviceLocationFreshness.isFresh(now, now))
    }

    @Test fun `an older cached fix is replaced by a new request`() {
        assertFalse(DeviceLocationFreshness.isFresh(now - DeviceLocationFreshness.MAX_AGE_MILLIS - 1, now))
        assertFalse(DeviceLocationFreshness.isFresh(0L, now))
        assertFalse(DeviceLocationFreshness.isFresh(now + 60_000L, now))
    }
}
