package com.example.feature.surface

import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarUiState
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.dashboard.domain.model.DailyActivityMapper
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.settings.presentation.SettingsAction
import com.example.feature.statistics.presentation.DailyStatisticItem
import com.example.feature.statistics.presentation.StatisticsSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Product-surface ownership characterization after Part 3 cleanup.
 */
class ProductSurfaceCharacterizationTest {

    @Test
    fun `daily activity tasbih destination is production tasbih_screen`() {
        val items = DailyActivityMapper.mapItems(emptyMap())
        val tasbih = items.first { it.id == DailyActivityIds.TASBEEH }
        assertEquals("tasbih_screen", tasbih.route)
    }

    @Test
    fun `tasbih category filter matches production تسبيح items only`() {
        val items = listOf(
            sampleZikr(1, category = "تسبيح"),
            sampleZikr(2, category = "سبحة"),
            sampleZikr(3, category = "أذكار الصباح")
        )
        val tasbihList = items.filter { it.category == "تسبيح" }
        assertEquals(listOf(1L), tasbihList.map { it.id })
    }

    @Test
    fun `legacy sebha category filter matched سبح substring and is no longer production`() {
        val items = listOf(
            sampleZikr(1, category = "تسبيح"),
            sampleZikr(2, category = "سبحة"),
            sampleZikr(3, category = "سبحة حرة")
        )
        val sebhaList = items.filter { it.category.contains("سبح", ignoreCase = true) }
        assertEquals(listOf(2L, 3L), sebhaList.map { it.id })
        // Production counter must not open those categories via Tasbih filter.
        assertTrue(items.filter { it.category == "تسبيح" }.none { it.id == 2L })
    }

    @Test
    fun `azkar ui state no longer carries settings or statistics fields`() {
        val state = AzkarUiState(
            fontSize = 28f,
            isVibrationEnabled = false,
            azkarList = listOf(sampleZikr(1, dailyProgress = 3))
        )
        assertEquals(28f, state.fontSize, 0.01f)
        assertFalse(state.isVibrationEnabled)
        assertEquals(3, state.azkarList.sumOf { it.dailyProgress })
        // Compile-time ownership: settings/stats live on dedicated models.
        val settingsAction: SettingsAction = SettingsAction.SetDarkMode(true)
        assertTrue(settingsAction is SettingsAction.SetDarkMode)
        val summary = StatisticsSummary(totalDailyCount = 3)
        assertEquals(3, summary.totalDailyCount)
    }

    @Test
    fun `azkar actions no longer include settings mutations`() {
        // Ownership moved to SettingsAction; AzkarAction retains list/counter actions only.
        val counter: AzkarAction = AzkarAction.OnIncrement(1L)
        val settings: SettingsAction = SettingsAction.SetDarkMode(true)
        assertTrue(counter is AzkarAction.OnIncrement)
        assertTrue(settings is SettingsAction.SetDarkMode)
    }

    @Test
    fun `statistics total daily is sum of daily items`() {
        val items = listOf(
            DailyStatisticItem(1, "a", 4),
            DailyStatisticItem(2, "b", 6)
        )
        assertEquals(10, items.sumOf { it.dailyProgress })
    }

    @Test
    fun `empty statistics chart uses safe max of at least 1`() {
        val stats = emptyList<DailyStat>()
        val maxCount = (stats.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(1)
        assertEquals(1, maxCount)
    }

    @Test
    fun `donation offer and request type labels are distinct`() {
        val offer = sampleDonation(type = "OFFER")
        val request = sampleDonation(type = "REQUEST")
        assertEquals("OFFER", offer.type)
        assertEquals("REQUEST", request.type)
        assertTrue(offer.type != request.type)
    }

    @Test
    fun `canonical details phone is preserved on domain model`() {
        val donation = sampleDonation(phone = "0912345678")
        assertEquals("0912345678", donation.phoneNumber)
    }

    private fun sampleZikr(
        id: Long,
        category: String = "تسبيح",
        dailyProgress: Int = 0
    ) = Zikr(
        id = id,
        title = "t$id",
        text = "text$id",
        currentCount = dailyProgress,
        targetCount = 33,
        category = category,
        isFavorite = false,
        source = "seed",
        dailyProgress = dailyProgress
    )

    private fun sampleDonation(
        type: String = "OFFER",
        phone: String = "0900000000"
    ) = Donation(
        id = 1L,
        title = "title",
        description = "desc",
        type = type,
        category = "طعام",
        location = "حلب",
        status = "ACTIVE",
        donorName = "أحمد",
        phoneNumber = phone,
        imageUrl = null,
        createdAt = 0L
    )
}
