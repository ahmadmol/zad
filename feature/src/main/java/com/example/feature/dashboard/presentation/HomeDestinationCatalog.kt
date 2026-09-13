package com.example.feature.dashboard.presentation

/**
 * Phase 5 — Home Information Architecture.
 *
 * The Home surface shows two tile groups: a small primary quick-action row and a
 * scrollable services carousel. This catalog is the single, testable declaration of
 * which routes each group owns, so the "every destination appears exactly once on
 * Home" rule can be asserted by a unit test instead of by review.
 *
 * Contextual summary cards (prayer, Quran continue, azkar, ...) are deliberately NOT
 * part of this catalog: they present state, not a duplicated service entry.
 */
object HomeDestinationCatalog {

    /** Routes shown in the compact quick-action row at the top of Home. */
    val primaryRoutes: List<String> = listOf(
        "qibla",
        "asma",
        "dua",
        "quran",
        // Single Live entry — opens the Haram / Nabawi chooser dialog.
        "live_chooser"
    )

    /** Routes shown once in the grouped services carousel. */
    val serviceRoutes: List<String> = listOf(
        "hadith",
        "azkar",
        "tasbih",
        "prayer",
        "search",
        "daily",
        "statistics"
    )

    /** Every route reachable from a Home tile. */
    val allRoutes: List<String> get() = primaryRoutes + serviceRoutes

    /** Routes that open a live stream directly, rather than through the chooser. */
    val directLiveRoutes: List<String> = listOf("haram", "nabawi")
}
