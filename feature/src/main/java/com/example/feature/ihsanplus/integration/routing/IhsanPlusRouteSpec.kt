package com.example.feature.ihsanplus.integration.routing

import com.example.feature.ihsanplus.integration.model.IhsanPlusCapability

/**
 * Contract-only route definitions. Must NOT be registered in Screen.kt / AppNavHost in Part 5.
 */
sealed interface IhsanPlusRouteSpec {
    val route: String
    val productionApproved: Boolean
    val requiredCapabilities: Set<IhsanPlusCapability>

    data object Daily : IhsanPlusRouteSpec {
        override val route: String = "ihsan_plus_daily"
        override val productionApproved: Boolean = false
        override val requiredCapabilities: Set<IhsanPlusCapability> =
            setOf(IhsanPlusCapability.DailyReadOnly)
    }

    data object PrayerAssist : IhsanPlusRouteSpec {
        override val route: String = "ihsan_plus_prayer_assist"
        override val productionApproved: Boolean = false
        override val requiredCapabilities: Set<IhsanPlusCapability> =
            setOf(IhsanPlusCapability.PrayerReadOnly)
    }

    data object CharityTrust : IhsanPlusRouteSpec {
        override val route: String = "ihsan_plus_charity_trust"
        override val productionApproved: Boolean = false
        override val requiredCapabilities: Set<IhsanPlusCapability> =
            setOf(IhsanPlusCapability.CharityBackendVerified)
    }

    companion object {
        val ALL: List<IhsanPlusRouteSpec> = listOf(Daily, PrayerAssist, CharityTrust)
    }
}
