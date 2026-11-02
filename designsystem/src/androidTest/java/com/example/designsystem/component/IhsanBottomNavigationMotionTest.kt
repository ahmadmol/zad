package com.example.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.example.designsystem.theme.IhsanTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IhsanBottomNavigationMotionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val destinations = listOf(
        IhsanBottomNavDestination("الرئيسية", Icons.Outlined.Home, Icons.Filled.Home),
        IhsanBottomNavDestination("إحسان", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        IhsanBottomNavDestination("حسابي", Icons.Outlined.Person, Icons.Filled.Person)
    )

    @Test
    fun threeItemsExist_andSelectedSemanticsFollowIndex() {
        composeRule.setContent {
            IhsanTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    IhsanBottomNavigation(
                        destinations = destinations,
                        selectedIndex = 1,
                        onDestinationSelected = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    )
                }
            }
        }

        composeRule.onNodeWithTag("ihsan_bottom_navigation").assertExists()
        composeRule.onNodeWithTag("ihsan_bottom_nav_indicator").assertExists()
        composeRule.onAllNodesWithContentDescription("الرئيسية").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("إحسان").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("حسابي").assertCountEquals(1)

        composeRule.onNodeWithContentDescription("إحسان").assertIsSelected()
        composeRule.onNodeWithContentDescription("الرئيسية").assertIsNotSelected()
        composeRule.onNodeWithContentDescription("حسابي").assertIsNotSelected()
    }

    @Test
    fun clickInvokesCallbackOnce_andItemsRemain() {
        var selected = -1
        var clicks = 0

        composeRule.setContent {
            IhsanTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var index by remember { mutableIntStateOf(0) }
                    IhsanBottomNavigation(
                        destinations = destinations,
                        selectedIndex = index,
                        onDestinationSelected = {
                            clicks += 1
                            selected = it
                            index = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("حسابي").performClick()
        composeRule.waitForIdle()

        assertEquals(2, selected)
        assertEquals(1, clicks)
        composeRule.onAllNodesWithContentDescription("الرئيسية").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("إحسان").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("حسابي").assertCountEquals(1)
        composeRule.onNodeWithContentDescription("حسابي").assertIsSelected()
    }

    @Test
    fun rtlClickMapping_matchesDestinationIndex() {
        val clicks = mutableListOf<Int>()

        composeRule.setContent {
            IhsanTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var index by remember { mutableIntStateOf(0) }
                    IhsanBottomNavigation(
                        destinations = destinations,
                        selectedIndex = index,
                        onDestinationSelected = {
                            clicks += it
                            index = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("الرئيسية").performClick()
        composeRule.onNodeWithContentDescription("إحسان").performClick()
        composeRule.onNodeWithContentDescription("حسابي").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(0, 1, 2), clicks)
    }
}
