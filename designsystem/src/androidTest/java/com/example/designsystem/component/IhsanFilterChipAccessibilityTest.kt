package com.example.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.designsystem.theme.IhsanTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IhsanFilterChipAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    @Test fun filtersExposeSelectionAndHaveNonOverlapping48dpBoundsAtLargeFont() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density.density,2f)) {
                IhsanTheme {
                    var selected by remember { mutableStateOf(false) }
                    Column {
                        IhsanFilterChip("الكل",!selected,{ selected=false })
                        IhsanFilterChip("المفضلة",selected,{ selected=true })
                    }
                }
            }
        }
        rule.onNodeWithText("الكل").assertIsSelected()
        rule.onNodeWithText("المفضلة").assertIsNotSelected().performClick().assertIsSelected()
        rule.onNodeWithText("الكل").assertIsNotSelected()
        val first=rule.onNodeWithText("الكل").fetchSemanticsNode().boundsInRoot
        val second=rule.onNodeWithText("المفضلة").fetchSemanticsNode().boundsInRoot
        val minimum=48f * rule.density.density
        assertTrue(first.height >= minimum); assertTrue(second.height >= minimum)
        assertTrue(first.bottom <= second.top)
    }
}
