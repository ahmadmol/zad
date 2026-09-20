package com.example.mol.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.designsystem.component.IhsanBottomNavDestination
import com.example.designsystem.component.IhsanBottomNavigation
import com.example.mol.navigation.AppNavHost
import com.example.mol.navigation.Screen
import com.example.mol.navigation.resolveBottomBarDestination

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainItems = listOf(
        NavigationItem(Screen.Home, "الرئيسية", Icons.Outlined.Home, Icons.Filled.Home),
        NavigationItem(Screen.Donations, "إحسان", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavigationItem(Screen.Profile, "حسابي", Icons.Outlined.Person, Icons.Filled.Person)
    )

    // Visibility + selection derive only from the route resolver (no Home fallback).
    val resolved = resolveBottomBarDestination(currentRoute)
    val selectedIndex = resolved?.let { dest ->
        mainItems.indexOfFirst { it.screen.route == dest.route }
    } ?: -1
    val showBottomBar = (selectedIndex >= 0) && (currentRoute != Screen.Splash.route) && (currentRoute != Screen.Onboarding.route)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                bottom = 10.dp
                            )
                    ) {
                        IhsanBottomNavigation(
                            destinations = mainItems.map {
                                IhsanBottomNavDestination(
                                    label = it.label,
                                    icon = it.unselectedIcon,
                                    selectedIcon = it.selectedIcon
                                )
                            },
                            selectedIndex = selectedIndex,
                            onDestinationSelected = { index ->
                                val item = mainItems.getOrNull(index) ?: return@IhsanBottomNavigation
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}

private data class NavigationItem(
    val screen: Screen,
    val label: String,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
