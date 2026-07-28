package com.example.mol.navigation

/**
 * Valid root destinations for the Bottom Navigation Bar.
 * Child / detail / unknown screens are not represented here.
 */
enum class BottomBarDestination(val route: String) {
    HOME(Screen.Home.route),
    EHSAN(Screen.Donations.route),
    PROFILE(Screen.Profile.route)
}

/**
 * Resolves a route string to a [BottomBarDestination].
 *
 * Strict Policy:
 * - Only explicit root routes for Home, Ehsan, and Profile return a destination.
 * - Child routes and unknown routes return null.
 * - No default/fallback to Home.
 */
fun resolveBottomBarDestination(route: String?): BottomBarDestination? {
    if (route == null) return null

    return when (route) {
        Screen.Home.route -> BottomBarDestination.HOME
        Screen.Donations.route -> BottomBarDestination.EHSAN
        Screen.Profile.route -> BottomBarDestination.PROFILE
        else -> null
    }
}
