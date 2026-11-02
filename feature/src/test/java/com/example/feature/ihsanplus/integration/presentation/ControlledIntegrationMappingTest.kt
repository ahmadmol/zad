package com.example.feature.ihsanplus.integration.presentation

import com.example.feature.ihsanplus.integration.model.IhsanPlusDailySourceSnapshot
import com.example.feature.ihsanplus.integration.model.IhsanPlusPrayerSourceSnapshot
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ControlledIntegrationMappingTest {

    @Test
    fun `prayer assist maps location unavailable without fabricating next prayer`() {
        val snapshot = IhsanPlusPrayerSourceSnapshot(
            prayerDay = null,
            nextPrayer = null,
            locationState = PrayerLocationState.Unavailable(
                PrayerLocationUnavailableReason.PERMISSION_DENIED
            ),
            systemStatus = PrayerSystemStatus(
                locationState = PrayerLocationState.Unavailable(
                    PrayerLocationUnavailableReason.PERMISSION_DENIED
                ),
                alarmPermission = PrayerAlarmPermissionState.NotificationsDenied,
                lastReconciliationEpochMillis = null,
                lastReconciliationSuccess = false,
                lastFailureSummary = null,
                scheduledAlarmCount = 0
            ),
            generatedAt = Instant.EPOCH
        )
        val model = ControlledPrayerAssistViewModel.map(snapshot)
        assertNull(model.nextPrayerArabic)
        assertFalse(model.locationAvailable)
        assertFalse(model.notificationsOk)
        assertEquals(0, model.scheduledAlarmCount)
    }

    @Test
    fun `prayer assist maps next prayer from facade snapshot only`() {
        val snapshot = IhsanPlusPrayerSourceSnapshot(
            prayerDay = null,
            nextPrayer = NextPrayer(
                name = PrayerName.ASR,
                epochMillis = 100,
                remainingMillis = 50,
                previousEpochMillis = null
            ),
            locationState = PrayerLocationState.Loading,
            systemStatus = PrayerSystemStatus(
                locationState = PrayerLocationState.Loading,
                alarmPermission = PrayerAlarmPermissionState.GrantedExact,
                lastReconciliationEpochMillis = 1L,
                lastReconciliationSuccess = true,
                lastFailureSummary = null,
                scheduledAlarmCount = 5
            ),
            generatedAt = Instant.EPOCH
        )
        val model = ControlledPrayerAssistViewModel.map(snapshot)
        assertEquals("العصر", model.nextPrayerArabic)
        assertTrue(model.notificationsOk)
        assertEquals(5, model.scheduledAlarmCount)
    }

    @Test
    fun `daily snapshot stays free of trust claims`() {
        val snapshot = IhsanPlusDailySourceSnapshot(
            profileDisplayName = null,
            nextPrayerNameArabic = null,
            nextPrayerEpochMillis = null,
            quranSurahId = null,
            quranAyahNumber = null,
            dhikrTodayCount = 0,
            activityIds = emptyList(),
            completedActivityIds = emptyList(),
            generatedAt = Instant.EPOCH
        )
        assertNull(snapshot.profileDisplayName)
        assertTrue(snapshot.activityIds.isEmpty())
    }
}
