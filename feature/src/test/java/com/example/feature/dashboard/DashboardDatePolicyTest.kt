package com.example.feature.dashboard

import com.example.feature.dashboard.presentation.DashboardDatePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardDatePolicyTest {

    @Test
    fun `Hijri date refreshes only when the calendar date key changes`() {
        assertFalse(DashboardDatePolicy.shouldRefreshHijriDate("2026-07-22", "2026-07-22"))
        assertTrue(DashboardDatePolicy.shouldRefreshHijriDate("2026-07-22", "2026-07-23"))
        assertTrue(DashboardDatePolicy.shouldRefreshHijriDate(null, "2026-07-22"))
    }
}
