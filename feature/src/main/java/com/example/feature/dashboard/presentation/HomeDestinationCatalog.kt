package com.example.feature.dashboard.presentation

import com.example.feature.dashboard.presentation.components.ihsanServices

/** One catalog supplies the six Home previews and the complete, searchable Services destination. */
object HomeDestinationCatalog {
    val primaryRoutes: List<String> get() = ihsanServices.take(6).map { it.route }
    val serviceRoutes: List<String> get() = ihsanServices.drop(6).map { it.route }
    val allRoutes: List<String> get() = primaryRoutes + serviceRoutes
    val directLiveRoutes: List<String> = listOf("haram", "nabawi")
}
