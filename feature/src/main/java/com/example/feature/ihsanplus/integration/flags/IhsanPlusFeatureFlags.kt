package com.example.feature.ihsanplus.integration.flags

import com.example.feature.BuildConfig

/**
 * Compile-time feature flags for controlled IhsanPlus integration.
 * Debug defaults enabled for verification; release defaults disabled until approval.
 */
object IhsanPlusFeatureFlags {
    val dailyEnabled: Boolean get() = BuildConfig.IHSANPLUS_DAILY_ENABLED
    val prayerAssistEnabled: Boolean get() = BuildConfig.IHSANPLUS_PRAYER_ASSIST_ENABLED

    /** Charity Trust fake dashboard is never production-enabled. */
    val charityTrustEnabled: Boolean get() = false
}
