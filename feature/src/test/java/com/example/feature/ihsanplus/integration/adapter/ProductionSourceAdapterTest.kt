package com.example.feature.ihsanplus.integration.adapter

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import com.example.feature.ihsanplus.integration.model.CharityCapability
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionSourceAdapterTest {

    @Test
    fun `prayer adapter mirrors facade without fabricating data`() = runTest {
        val next = NextPrayer(
            name = PrayerName.DHUHR,
            epochMillis = 1_000L,
            remainingMillis = 500L,
            previousEpochMillis = null
        )
        val day = mockk<PrayerDay>(relaxed = true)
        val location = PrayerLocationState.Unavailable(
            reason = com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason.PERMISSION_DENIED
        )
        val status = PrayerSystemStatus(
            locationState = location,
            alarmPermission = PrayerAlarmPermissionState.Unknown,
            lastReconciliationEpochMillis = null,
            lastReconciliationSuccess = null,
            lastFailureSummary = null,
            scheduledAlarmCount = 0
        )
        val facade = mockk<PrayerTimesFacade>()
        every { facade.prayerDay } returns MutableStateFlow(day)
        every { facade.nextPrayer } returns MutableStateFlow(next)
        every { facade.locationState } returns MutableStateFlow(location)
        every { facade.systemStatus } returns MutableStateFlow(status)

        val snapshot = ProductionPrayerSourceAdapter(facade).observeSnapshot().first()
        assertEquals(day, snapshot.prayerDay)
        assertEquals(next, snapshot.nextPrayer)
        assertEquals(location, snapshot.locationState)
        assertEquals(status, snapshot.systemStatus)
    }

    @Test
    fun `charity adapter is local board only without trust metrics`() = runTest {
        val donations = listOf(
            Donation(
                id = 7,
                title = "Offer",
                description = "desc",
                category = "food",
                location = "city",
                type = "OFFER",
                status = "AVAILABLE",
                donorName = "A",
                phoneNumber = "0500000000"
            )
        )
        val repo = mockk<EhsanRepository>()
        every { repo.getAllDonations() } returns flowOf(donations)
        val prefs = mockk<UserPreferences>()
        every { prefs.userName } returns flowOf("مستخدم إحسان")

        val snapshot = ProductionCharitySourceAdapter(repo, prefs).observeSnapshot().first()
        assertEquals(CharityCapability.LocalBoard, snapshot.capability)
        assertEquals(1, snapshot.listings.size)
        assertEquals(7L, snapshot.listings.first().id)
        assertFalse(snapshot.localProfileAvailable)
        assertEquals("Offer", snapshot.listings.first().title)
        assertTrue(snapshot.listings.first().status == "AVAILABLE")
    }
}
