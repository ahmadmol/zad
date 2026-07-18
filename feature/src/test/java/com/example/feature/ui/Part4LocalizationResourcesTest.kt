package com.example.feature.ui

import com.example.feature.R
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ensures Part 4 high-risk / shared string resources resolve in the feature module.
 */
class Part4LocalizationResourcesTest {

    @Test
    fun `common action resources exist`() {
        assertTrue(R.string.common_retry != 0)
        assertTrue(R.string.common_save != 0)
        assertTrue(R.string.common_cancel != 0)
        assertTrue(R.string.common_share != 0)
        assertTrue(R.string.common_whatsapp != 0)
        assertTrue(R.string.cd_back != 0)
    }

    @Test
    fun `trust and safety resources exist`() {
        assertTrue(R.string.ehsan_local_only_notice != 0)
        assertTrue(R.string.ehsan_not_found != 0)
        assertTrue(R.string.profile_local_notice != 0)
        assertTrue(R.string.prayer_retry_location != 0)
        assertTrue(R.string.prayer_reschedule_alarms != 0)
    }

    @Test
    fun `settings and statistics resources exist`() {
        assertTrue(R.string.settings_adhan_sound_title != 0)
        assertTrue(R.string.settings_share_app != 0)
        assertTrue(R.string.statistics_empty_body != 0)
        assertTrue(R.string.statistics_total_count_value != 0)
    }

    @Test
    fun `tasbih accessibility resources exist`() {
        assertTrue(R.string.tasbih_count_semantics != 0)
        assertTrue(R.string.cd_tasbih_increment != 0)
        assertTrue(R.string.cd_tasbih_reset != 0)
    }
}
