package com.example.mol.ui

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mol.MainActivity
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.domain.model.CityProvider
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Actual MainActivity, production navigation and the saved app theme.
 * Compose semantics invoke the real onClick handlers; screenshots are Android framebuffers. */
@OptIn(ExperimentalTestApi::class)
class DarkFinalNativeNavigationTest {
    @get:Rule(order = 0) val preservePreferences = PreserveDevicePreferences()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>(kotlinx.coroutines.Dispatchers.Main.immediate)
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private val output get() = File(inst.targetContext.getExternalFilesDir(null), "dark-native").apply { mkdirs() }
    private fun click(label: String, scroll: Boolean = false, partial: Boolean = false) {
        val matcher = hasText(label, substring = partial) and hasClickAction()
        compose.waitUntil(30_000) { compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        val nodes=compose.onAllNodes(matcher)
        val node=nodes[nodes.fetchSemanticsNodes().lastIndex]
        if(scroll) node.performScrollTo()
        node.performClick();compose.waitForIdle();SystemClock.sleep(300)
    }
    private fun back() {
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle();SystemClock.sleep(300)
    }
    private fun tab(label: String) {
        val matcher=hasText(label) and hasClickAction()
        repeat(4) { if(compose.onAllNodes(matcher).fetchSemanticsNodes().isEmpty()) back() }
        click(label)
    }
    private fun shot(name: String) {
        compose.waitForIdle();SystemClock.sleep(450)
        compose.onRoot().assertIsDisplayed()
        assertTrue("MainActivity lost its foreground window", compose.activity.hasWindowFocus())
        val bitmap=checkNotNull(inst.uiAutomation.takeScreenshot())
        File(output,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
    @Test fun sixReferenceScreensUseSavedThemeAndProductionNavigation() {
        val koin=org.koin.core.context.GlobalContext.get()
        val settings=koin.get<SettingsManager>()
        runBlocking {
            settings.setDarkMode(false)
        }
        compose.waitForIdle();SystemClock.sleep(3500)
        if(compose.onAllNodesWithText("تخطي").fetchSemanticsNodes().isNotEmpty()) click("تخطي")
        compose.waitUntil(30_000) { compose.onAllNodes(hasText("الملف الشخصي") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        for(dark in listOf(false,true)) {
            tab("الملف الشخصي")
            if(dark) click("الوضع الداكن",scroll=true)
            assertEquals(dark,runBlocking { settings.darkModeFlow.first() })
            // Return to a fresh profile route before its native capture, clearing scroll via title.
            tab("الرئيسية");tab("الملف الشخصي")
            compose.onNodeWithText("الملف الشخصي",useUnmergedTree=true).let { }
            val prefix="393_${if(dark) "dark" else "light"}"
            val scrolls=compose.onAllNodes(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.ScrollBy),useUnmergedTree=true)
            if(scrolls.fetchSemanticsNodes().isNotEmpty()) scrolls[0].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f,-100000f) }
            shot("${prefix}_profile")
            click("جميع الإعدادات",scroll=true);shot("${prefix}_settings");back()
            tab("الرئيسية")
            val homeScrolls=compose.onAllNodes(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.ScrollBy),useUnmergedTree=true)
            if(homeScrolls.fetchSemanticsNodes().isNotEmpty()) homeScrolls[0].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f,-100000f) }
            shot("${prefix}_home")
            tab("إحسان")
            click("عروض التبرع");shot("${prefix}_community")
            click("طلبات مساعدة");shot("${prefix}_requests")
            click("عروض التبرع")
            for((label,file) in listOf("الصلاة" to "prayer","القبلة" to "qibla","القرآن" to "quran_list")) {
                tab("الرئيسية");click(label,scroll=true)
                SystemClock.sleep(700);shot("${prefix}_$file")
                if(file=="quran_list") {
                    click("الفاتحة",partial=true)
                    compose.waitUntil(30_000) { compose.onAllNodesWithText("سُورَةُ الفاتحة").fetchSemanticsNodes().isNotEmpty() }
                    shot("${prefix}_quran_reader");back()
                }
                back()
            }
        }
        assertTrue(runBlocking { settings.darkModeFlow.first() })
    }
}
