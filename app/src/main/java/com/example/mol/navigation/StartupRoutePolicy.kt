package com.example.mol.navigation

/** A restored destination has already passed startup; native splash must never wait for a removed entry. */
fun isStartupResolvedRoute(route: String?): Boolean = route != null && route != Screen.Splash.route
