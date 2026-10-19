package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.presentation.AddEhsanScreen
import org.junit.Rule
import org.junit.Test
import androidx.compose.runtime.mutableStateOf
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import android.view.WindowInsets
import java.io.File

class OfferDraftRestorationTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun descriptionAndStageSurviveSavedInstanceRestoration() {
        val restoration=StateRestorationTester(compose)
        restoration.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme { AddEhsanScreen() }
            }
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("عرض محفوظ بعد إعادة الإنشاء")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("تفاصيل لا تضيع عند الرجوع وإعادة إنشاء الشاشة")
        compose.onNodeWithText("التالي").performScrollTo().performClick()
        compose.onNodeWithText("التالي").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("صور العرض (0/3)").assertExists()
        compose.onNodeWithText("المعلومات").performScrollTo().performClick()
        compose.onNodeWithText("عرض محفوظ بعد إعادة الإنشاء").assertExists()
        compose.onNodeWithText("تفاصيل لا تضيع عند الرجوع وإعادة إنشاء الشاشة").assertExists()
    }

    @Test fun profileActionsRemainReachableWithActualKeyboard() {
        val dark = mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = dark.value) {
                    com.example.feature.profile.presentation.EditProfileScreen({})
                }
            }
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), "keyboard-qa").apply { mkdirs() }
        for (night in listOf(false, true)) {
            compose.runOnIdle { dark.value = night }
            compose.onAllNodes(hasSetTextAction()).onLast().performScrollTo().performClick()
            compose.waitUntil(10_000) {
                compose.activity.window.decorView.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true
            }
            compose.onAllNodes(hasSetTextAction()).onLast().performTextInput("نبذة طويلة للتحقق من التمرير مع لوحة المفاتيح")
            compose.onNodeWithText("حفظ التغييرات").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("إلغاء").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("حفظ التغييرات").assertIsDisplayed()
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(output, "393_${if (night) "dark" else "light"}_profile_keyboard.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
    }
}
