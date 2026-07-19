package com.example.mol.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.designsystem.component.IhsanBottomNavigation
import com.example.designsystem.component.IhsanBottomNavigationItem
import com.example.mol.navigation.AppNavHost
import com.example.mol.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainItems = listOf(
        NavigationItem(Screen.Home, "الرئيسية", Icons.Outlined.Home, Icons.Filled.Home),
        NavigationItem(Screen.Donations, "إحسان", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavigationItem(Screen.Profile, "حسابي", Icons.Outlined.Person, Icons.Filled.Person)
    )

    // Presentation-only: hide bottom bar on Profile to match reference UI.
    // Tab destinations and navigation logic remain unchanged.
    val showBottomBar = currentDestination?.route == Screen.Home.route ||
        currentDestination?.route == Screen.Donations.route

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 12.dp
                            )
                    ) {
                        IhsanBottomNavigation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                        ) {
                            mainItems.forEach { item ->
                                val isSelected =
                                    currentDestination?.hierarchy?.any { it.route == item.screen.route } == true

                                IhsanBottomNavigationItem(
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = item.unselectedIcon,
                                    selectedIcon = item.selectedIcon,
                                    label = item.label
                                )
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
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
