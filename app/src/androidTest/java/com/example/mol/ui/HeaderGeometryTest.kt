package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HeaderGeometryTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun allVariantsKeepTextAndTouchGeometryBetweenThemes() {
        val dark=mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme=dark.value) {
                    Column {
                        listOf(IhsanTopBarStyle.Hero,IhsanTopBarStyle.CompactSkyline,IhsanTopBarStyle.Minimal).forEach { style ->
                            IhsanTopBar("عنوان ${style.name}", style=style, onNavigateBack={},
                                actions={ IconButton({}) { Icon(Icons.Default.Share,"مشاركة ${style.name}") } })
                        }
                    }
                }
            }
        }
        fun bounds(): Map<String,Rect> = listOf("Hero","CompactSkyline","Minimal").flatMap { listOf("عنوان $it","مشاركة $it") }
            .associateWith { label ->
                val node=if(label.startsWith("عنوان")) compose.onNodeWithText(label) else compose.onNodeWithContentDescription(label)
                node.fetchSemanticsNode().boundsInRoot
            }
        compose.waitForIdle(); val light=bounds()
        compose.runOnIdle { dark.value=true }; compose.waitForIdle()
        assertEquals(light,bounds())
    }
}
