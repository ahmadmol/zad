package com.example.mol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // Define main navigation items
    val mainItems = listOf(
        NavigationItem(Screen.Home, "الرئيسية", Icons.Outlined.Home, Icons.Filled.Home),
        NavigationItem(Screen.Donations, "إحسان", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavigationItem(Screen.Profile, "حسابي", Icons.Outlined.Person, Icons.Filled.Person)
    )

    // Only show bottom bar on main screens
    val showBottomBar = mainItems.any { it.screen.route == currentDestination?.route }

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
                                bottom = 16.dp
                            )
                    ) {
                        IhsanBottomNavigation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            mainItems.forEach { item ->
                                val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                
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
