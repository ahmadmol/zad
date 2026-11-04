package com.example.mol.ui

import androidx.compose.material3.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.dashboard.presentation.components.AllServicesScreen
import com.example.mol.navigation.Screen
import com.example.mol.navigation.serviceDestinationRoute
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compiled on host. Execute only during separately authorized Android acceptance. */
class ServicesBackStackRegressionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun servicesToTasbihBackRestoresSearchThenReturnsHome() {
        lateinit var controller: NavHostController
        compose.setContent { IhsanTheme {
            controller = rememberNavController()
            NavHost(controller, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    Button({ controller.navigate(Screen.Services.route) }) { Text("عرض الكل") }
                }
                composable(Screen.Services.route) {
                    AllServicesScreen(onBack = { controller.popBackStack() }, onServiceClick = { key ->
                        serviceDestinationRoute(key)?.let { controller.navigate(it) { launchSingleTop = true } }
                    })
                }
                composable(Screen.Tasbih.route) {
                    IhsanTopBar("عداد التسبيح", onNavigateBack = { controller.popBackStack() }, navigationIconContentDescription = "رجوع")
                }
            }
        } }
        compose.onNodeWithText("عرض الكل").performClick()
        compose.onNodeWithTag("service_search").performTextInput("التسبيح")
        compose.onNodeWithTag("service_tasbih").performClick()
        compose.runOnIdle { assertEquals(Screen.Tasbih.route, controller.currentDestination?.route) }
        compose.onNodeWithContentDescription("رجوع").performClick()
        compose.onNodeWithTag("service_search").assertTextContains("التسبيح")
        compose.runOnIdle {
            assertEquals(Screen.Services.route, controller.currentDestination?.route)
            controller.popBackStack()
            assertEquals(Screen.Home.route, controller.currentDestination?.route)
        }
    }

    @Test fun servicesSearchSurvivesSavedStateRecreation() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { IhsanTheme { AllServicesScreen(onServiceClick = {}, onBack = {}) } }
        compose.onNodeWithTag("service_search").performTextInput("التسبيح")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("service_search").assertTextContains("التسبيح")
        compose.onNodeWithTag("service_tasbih").assertExists()
    }

    @Test fun servicesToDonationsBackReturnsToServices() {
        lateinit var controller: NavHostController
        compose.setContent { IhsanTheme {
            controller = rememberNavController()
            NavHost(controller, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    Button({ controller.navigate(Screen.Services.route) }) { Text("عرض الكل") }
                }
                composable(Screen.Services.route) {
                    AllServicesScreen(onBack = { controller.popBackStack() }, onServiceClick = { key ->
                        serviceDestinationRoute(key)?.let { controller.navigate(it) { launchSingleTop = true } }
                    })
                }
                composable(Screen.Donations.route) {
                    val hasBackStack = controller.previousBackStackEntry != null &&
                        controller.previousBackStackEntry?.destination?.route != Screen.Home.route
                    IhsanTopBar(
                        title = "مجتمع إحسان",
                        onNavigateBack = if (hasBackStack) { { controller.popBackStack() } } else null,
                        navigationIconContentDescription = "رجوع"
                    )
                }
            }
        } }
        compose.onNodeWithText("عرض الكل").performClick()
        compose.onNodeWithTag("service_search").performTextInput("مجتمع")
        compose.onNodeWithTag("service_donations").performClick()
        compose.runOnIdle { assertEquals(Screen.Donations.route, controller.currentDestination?.route) }
        compose.onNodeWithContentDescription("رجوع").performClick()
        compose.runOnIdle {
            assertEquals(Screen.Services.route, controller.currentDestination?.route)
            controller.popBackStack()
            assertEquals(Screen.Home.route, controller.currentDestination?.route)
        }
    }
}
