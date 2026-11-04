package com.example.mol.ui

import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalDensity
import com.example.designsystem.theme.IhsanTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.Surface
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
fun MainScreen(widgetRequest: com.example.mol.widget.WidgetOpenRequest? = null, onWidgetConsumed: () -> Unit = {}, onStartupResolved: () -> Unit = {}) {
    val preferences: com.example.feature.core.preferences.UserPreferences = org.koin.compose.koinInject()
    val quran: com.example.feature.quran.domain.repository.QuranRepository = org.koin.compose.koinInject()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        if (com.example.mol.navigation.isStartupResolvedRoute(currentRoute)) onStartupResolved()
    }

    androidx.compose.runtime.LaunchedEffect(widgetRequest?.sequence, currentRoute) {
        val request = widgetRequest ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute == Screen.Splash.route || currentRoute == Screen.Onboarding.route || currentRoute == Screen.LocationPermission.route) return@LaunchedEffect
        val route = if (request.action == com.example.mol.widget.WIDGET_OPEN_PRAYER) Screen.Prayer.route else {
            try {
                val position = preferences.lastReadPosition.first()
                val surah = position?.let { quran.getSurahById(it.first) }
                com.example.mol.widget.widgetReadingRoute(position, surah?.totalVerses)
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { Screen.Quran.route }
        }
        navController.navigate(route) { launchSingleTop = true }
        onWidgetConsumed()
    }

    val mainItems = listOf(
        NavigationItem(Screen.Home, stringResource(com.example.feature.R.string.polish_nav_home), Icons.Outlined.Home, Icons.Filled.Home),
        NavigationItem(Screen.Donations, stringResource(com.example.feature.R.string.home_hero_title), Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavigationItem(Screen.Profile, stringResource(com.example.feature.R.string.polish_nav_profile), Icons.Outlined.Person, Icons.Filled.Person)
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
                    Surface(color = IhsanTheme.colors.navigationSurface) {
                        Box(Modifier.fillMaxWidth().navigationBarsPadding()) {
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
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = IhsanTheme.dimens.bottomNavigationMinHeight * LocalDensity.current.fontScale.coerceAtLeast(1f))
                        )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box {
                AppNavHost(
                    navController = navController,
                    onStartupResolved = onStartupResolved,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
                val isImmersiveScreen = currentRoute in listOf(
                    Screen.QuranReader.route,
                    Screen.HaramLive.route,
                    Screen.NabawiLive.route,
                    Screen.Splash.route
                )
                if (!isImmersiveScreen) {
                    Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(IhsanTheme.colors.surfaceBase))
                }
            }
        }
    }
}

private data class NavigationItem(
    val screen: Screen,
    val label: String,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
